package com.psi.flow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.DocFeignResponse;
import com.psi.flow.dto.ProcessStartDTO;
import com.psi.flow.dto.ProcessApproveDTO;
import com.psi.flow.entity.*;
import com.psi.flow.mapper.*;
import com.psi.flow.service.DocConvertService;
import com.psi.flow.service.FlowEngineService;
import com.psi.flow.util.ElExpressionUtil;
import com.psi.order.dto.DocResponse;
import com.psi.order.service.DocService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 流程引擎核心服务实现
 * 实现发起流程和审批流转的核心业务逻辑
 */
@Slf4j
@Service
public class FlowEngineServiceImpl implements FlowEngineService {

    private final WfProcessDefinitionMapper processDefinitionMapper;
    private final WfProcessNodeMapper processNodeMapper;
    private final WfProcessRelationMapper processRelationMapper;
    private final WfProcessInstanceMapper processInstanceMapper;
    private final WfProcessInstanceBizMapper processInstanceBizMapper;
    private final WfTaskMapper taskMapper;
    private final WfOperationLogMapper operationLogMapper;
    private final WfCcLogMapper ccLogMapper;
    private final DocService docService;
    private final ObjectMapper objectMapper;
    private final FlowProcessCompletedMqService flowProcessCompletedMqService;
    private final DocConvertService docConvertService;

    public FlowEngineServiceImpl(WfProcessDefinitionMapper processDefinitionMapper,
                                 WfProcessNodeMapper processNodeMapper,
                                 WfProcessRelationMapper processRelationMapper,
                                 WfProcessInstanceMapper processInstanceMapper,
                                 WfProcessInstanceBizMapper processInstanceBizMapper,
                                 WfTaskMapper taskMapper,
                                 WfOperationLogMapper operationLogMapper,
                                 WfCcLogMapper ccLogMapper,
                                 DocService docService,
                                 ObjectMapper objectMapper,
                                 FlowProcessCompletedMqService flowProcessCompletedMqService,
                                 DocConvertService docConvertService) {
        this.processDefinitionMapper = processDefinitionMapper;
        this.processNodeMapper = processNodeMapper;
        this.processRelationMapper = processRelationMapper;
        this.processInstanceMapper = processInstanceMapper;
        this.processInstanceBizMapper = processInstanceBizMapper;
        this.taskMapper = taskMapper;
        this.operationLogMapper = operationLogMapper;
        this.ccLogMapper = ccLogMapper;
        this.docService = docService;
        this.objectMapper = objectMapper;
        this.flowProcessCompletedMqService = flowProcessCompletedMqService;
        this.docConvertService = docConvertService;
    }

    /**
     * 将 variables Map 转为 JSON 字符串
     */
    private String variablesToJson(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 JSON 字符串解析 variables Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonToVariables(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, HashMap.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 发起流程
     * 功能：创建流程实例、绑定业务ID、生成待办任务、记录日志
     */
    @Override
    @Transactional
    public WfProcessInstance startProcess(ProcessStartDTO startDTO) {
        // 1. 查询最新版本的流程定义
        WfProcessDefinition processDef = processDefinitionMapper.selectLatestByKey(startDTO.getProcessKey());
        if (processDef == null) {
            log.warn("流程定义不存在，跳过工作流启动: processKey={}", startDTO.getProcessKey());
            return null;
        }

        // 2. 创建流程实例
        WfProcessInstance processInstance = new WfProcessInstance();
        processInstance.setId(UUID.randomUUID().toString().replace("-", ""));
        processInstance.setProcessDefId(processDef.getId());
        processInstance.setProcessKey(processDef.getProcessKey());
        processInstance.setTitle(startDTO.getTitle());
        processInstance.setStartUserId(startDTO.getStartUserId());
        processInstance.setStartUserName(startDTO.getStartUserName());
        processInstance.setStatus(1);
        processInstance.setDelFlag(0);
        // 存储流程变量（如采购金额amount等）
        processInstance.setExtJson(variablesToJson(startDTO.getVariables()));
        processInstanceMapper.insert(processInstance);

        // 3. 绑定业务ID
        WfProcessInstanceBiz bizRelation = new WfProcessInstanceBiz();
        bizRelation.setProcessInstanceId(processInstance.getId());
        bizRelation.setBizType(startDTO.getBizType());
        bizRelation.setBizId(startDTO.getBizId());
        processInstanceBizMapper.insert(bizRelation);

        // 4. 查询流程起始节点
        WfProcessNode startNode = processNodeMapper.selectStartNode(processDef.getId());
        if (startNode == null) {
            throw new RuntimeException("流程定义没有配置起始审批节点");
        }

        // 5. 处理起始节点（可能是条件节点，需要自动流转到第一个审批节点）
        Map<String, Object> initVars = startDTO.getVariables() != null ? startDTO.getVariables() : new HashMap<>();
        processNodeByAutoFlow(processInstance, startNode, initVars);

        // 6. 记录操作日志
        saveOperationLog(processInstance.getId(), startDTO.getStartUserId(), 
                startDTO.getStartUserName(), 1, "发起流程");

        return processInstance;
    }

    /**
     * 自动流转到实际审批节点（处理条件节点的自动跳转）
     */
    private void processNodeByAutoFlow(WfProcessInstance processInstance, WfProcessNode node, Map<String, Object> variables) {
        if (node.getNodeType() == null) {
            createTask(processInstance.getId(), node);
            processInstance.setCurrentNodeId(node.getId());
            processInstanceMapper.updateById(processInstance);
            return;
        }

        if (node.getNodeType() == 1) {
            createTask(processInstance.getId(), node);
            processInstance.setCurrentNodeId(node.getId());
            processInstanceMapper.updateById(processInstance);
            return;
        }

        // 条件节点或抄送节点 - 自动流转，返回实际到达的节点ID
        Long actualNodeId = handleNextNode(processInstance.getId(), node, variables);
        processInstance.setCurrentNodeId(actualNodeId);
        processInstanceMapper.updateById(processInstance);
    }

    /**
     * 审批流转
     * 功能：更新任务状态、根据当前节点+流程变量匹配连线条件、跳转下一节点、生成新待办
     */
    @Override
    @Transactional
    public boolean approve(ProcessApproveDTO approveDTO) {
        log.info("=== approve start: taskId={}, approveType={}, variables={}", approveDTO.getTaskId(), approveDTO.getApproveType(), approveDTO.getVariables());
        // 1. 查询任务
        WfTask task = taskMapper.selectById(approveDTO.getTaskId());
        if (task == null || task.getStatus() != 1) {
            log.warn("=== approve: task not found or already processed, taskId={}, task={}", approveDTO.getTaskId(), task);
            throw new RuntimeException("任务不存在或已处理");
        }
        log.info("=== approve: task found, nodeId={}, processInstanceId={}", task.getNodeId(), task.getProcessInstanceId());

        // 2. 查询流程实例
        WfProcessInstance processInstance = processInstanceMapper.selectById(task.getProcessInstanceId());
        if (processInstance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        log.info("=== approve: instance found, processDefId={}, extJson={}", processInstance.getProcessDefId(), processInstance.getExtJson());

        // 3. 查询流程定义
        WfProcessDefinition processDef = processDefinitionMapper.selectById(processInstance.getProcessDefId());
        if (processDef == null) {
            throw new RuntimeException("流程定义不存在");
        }

        // 4. 读取流程变量（从实例ext_json中读取，合并审批时传入的变量）
        Map<String, Object> mergedVariables = jsonToVariables(processInstance.getExtJson());
        if (approveDTO.getVariables() != null && !approveDTO.getVariables().isEmpty()) {
            mergedVariables.putAll(approveDTO.getVariables());
        }

        // 5. 更新任务状态：同意=2, 驳回=3
        task.setStatus(2); // 审批通过
        task.setHandlerUserId(approveDTO.getHandlerUserId());
        task.setHandlerUserName(approveDTO.getHandlerUserName());
        task.setHandleNote(approveDTO.getHandleNote());
        task.setHandleTime(LocalDateTime.now());
        taskMapper.updateById(task);

        // 6. 判断是否驳回
        if (approveDTO.getApproveType() == 3 || approveDTO.getApproveType() == 2) {
            processInstance.setStatus(3);
            processInstance.setEndTime(LocalDateTime.now());
            processInstanceMapper.updateById(processInstance);

            sendProcessCompletedMQ(processInstance, null);

            saveOperationLog(processInstance.getId(), approveDTO.getHandlerUserId(), 
                    approveDTO.getHandlerUserName(), 3, "驳回流程");
            return true;
        }

        // 7. 同意：查找下一节点（使用合并后的流程变量进行条件判断）
        Long nextNodeId = findNextNode(processDef.getId(), task.getNodeId(), mergedVariables);
        log.info("=== approve: nextNodeId={}, mergedVariables={}", nextNodeId, mergedVariables);
        
        if (nextNodeId == null) {
            // 没有下一节点，流程结束
            processInstance.setStatus(2);
            processInstance.setEndTime(LocalDateTime.now());
            processInstance.setCurrentNodeId(null);
            processInstanceMapper.updateById(processInstance);
            
            saveOperationLog(processInstance.getId(), approveDTO.getHandlerUserId(), 
                    approveDTO.getHandlerUserName(), 2, "审批通过，流程完成");

            DocFeignResponse docFeignResponse = updateDocStatus(processInstance);
            
            sendProcessCompletedMQ(processInstance, docFeignResponse);
            
            return true;
        }

        // 8. 查询下一节点信息
        WfProcessNode nextNode = processNodeMapper.selectById(nextNodeId);
        
        // 9. 处理下一节点（自动流转条件节点，返回实际到达的节点ID）
        Long actualNodeId = handleNextNode(processInstance.getId(), nextNode, mergedVariables);
        log.info("=== approve: actualNodeId={}, nextNode type={}", actualNodeId, nextNode != null ? nextNode.getNodeType() : null);

        // 如果下一节点是结束节点(actualNodeId为null)，视为流程完成
        if (actualNodeId == null) {
            processInstance.setStatus(2);
            processInstance.setEndTime(LocalDateTime.now());
            processInstance.setCurrentNodeId(null);
            processInstanceMapper.updateById(processInstance);
            
            saveOperationLog(processInstance.getId(), approveDTO.getHandlerUserId(), 
                    approveDTO.getHandlerUserName(), 2, "审批通过，流程完成");

            DocFeignResponse docFeignResponse = updateDocStatus(processInstance);
            sendProcessCompletedMQ(processInstance, docFeignResponse);
            
            return true;
        }

        // 10. 更新当前节点为下一节点
        processInstance.setCurrentNodeId(actualNodeId);
        processInstance.setExtJson(variablesToJson(mergedVariables));
        processInstanceMapper.updateById(processInstance);

        // 11. 记录操作日志
        saveOperationLog(processInstance.getId(), approveDTO.getHandlerUserId(), 
                approveDTO.getHandlerUserName(), 2, "审批通过");

        return true;
    }

    /**
     * 根据当前节点和流程变量查找下一节点
     */
    private Long findNextNode(Long processDefId, Long currentNodeId, Map<String, Object> variables) {
        // 查询当前节点的所有流出关系
        List<WfProcessRelation> relations = processRelationMapper.selectByFromNode(processDefId, currentNodeId);
        log.info("=== findNextNode: processDefId={}, currentNodeId={}, relations count={}", processDefId, currentNodeId, relations != null ? relations.size() : 0);
        
        if (relations == null || relations.isEmpty()) {
            return null; // 没有后续节点，流程结束
        }

        // 如果只有一条流出关系，直接返回目标节点
        if (relations.size() == 1) {
            return relations.get(0).getToNodeId();
        }

        // 有多条流出关系，需要根据条件表达式匹配
        for (WfProcessRelation relation : relations) {
            String conditionExpr = relation.getConditionExpr();
            // 如果没有条件表达式，作为默认路径
            if (conditionExpr == null || conditionExpr.isEmpty()) {
                continue;
            }
            // 使用EL表达式判断条件是否满足
            if (ElExpressionUtil.evaluateBoolean(conditionExpr, variables)) {
                return relation.getToNodeId();
            }
        }

        // 如果没有匹配的条件，查找默认路径（condition_expr为空的）
        for (WfProcessRelation relation : relations) {
            if (relation.getConditionExpr() == null || relation.getConditionExpr().isEmpty()) {
                return relation.getToNodeId();
            }
        }

        return null;
    }

    /**
     * 处理下一节点
     */
    private Long handleNextNode(String processInstanceId, WfProcessNode nextNode, Map<String, Object> variables) {
        if (nextNode == null) {
            return null;
        }

        switch (nextNode.getNodeType()) {
            case 1: // 审批节点
                createTask(processInstanceId, nextNode);
                return nextNode.getId();
            case 2: // 条件节点
                Long nextNextNodeId = findNextNode(nextNode.getProcessDefId(), nextNode.getId(), variables);
                if (nextNextNodeId != null) {
                    WfProcessNode nextNextNode = processNodeMapper.selectById(nextNextNodeId);
                    return handleNextNode(processInstanceId, nextNextNode, variables);
                }
                return null;
            case 3: // 抄送节点
                createCcLog(processInstanceId, nextNode);
                Long ccNextNodeId = findNextNode(nextNode.getProcessDefId(), nextNode.getId(), variables);
                if (ccNextNodeId != null) {
                    WfProcessNode ccNextNode = processNodeMapper.selectById(ccNextNodeId);
                    return handleNextNode(processInstanceId, ccNextNode, variables);
                }
                return null;
            case 4: // 结束节点
                return null;
            default:
                return null;
        }
    }

    /**
     * 创建待办任务
     * 从节点配置config中解析处理人
     */
    private void createTask(String processInstanceId, WfProcessNode node) {
        String handlerUserId = null;
        String handlerUserName = null;

        // 从节点配置中解析处理人
        String config = node.getConfig();
        if (config != null && !config.isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> configMap = objectMapper.readValue(config, HashMap.class);
                if (configMap.containsKey("handlerUserId")) {
                    handlerUserId = String.valueOf(configMap.get("handlerUserId"));
                }
                if (configMap.containsKey("handlerUserName")) {
                    handlerUserName = String.valueOf(configMap.get("handlerUserName"));
                }
            } catch (Exception e) {
                log.error("解析节点配置失败: nodeId={}", node.getId(), e);
            }
        }

        if (handlerUserId == null || handlerUserName == null) {
            log.error("节点未配置处理人: nodeId={}, nodeName={}, config={}", node.getId(), node.getNodeName(), config);
            handlerUserId = "";
            handlerUserName = "未配置";
        }

        WfTask task = new WfTask();
        task.setId(UUID.randomUUID().toString().replace("-", ""));
        task.setProcessInstanceId(processInstanceId);
        task.setNodeId(node.getId());
        task.setTaskName(node.getNodeName());
        task.setHandlerUserId(handlerUserId);
        task.setHandlerUserName(handlerUserName);
        task.setStatus(1); // 1-待处理
        task.setDelFlag(0);
        taskMapper.insert(task);
    }

    /**
     * 创建抄送记录
     */
    private void createCcLog(String processInstanceId, WfProcessNode node) {
        // TODO: 从节点配置中获取抄送用户
        String ccUserId = "default_cc";
        String ccUserName = "默认抄送人";

        WfCcLog ccLog = new WfCcLog();
        ccLog.setProcessInstanceId(processInstanceId);
        ccLog.setCcUserId(ccUserId);
        ccLog.setCcUserName(ccUserName);
        ccLog.setStatus(1); // 1-未读
        ccLog.setDelFlag(0);
        ccLogMapper.insert(ccLog);
    }

    /**
     * 保存操作日志
     */
    private void saveOperationLog(String processInstanceId, String operatorId, 
                                  String operatorName, Integer operateType, String operateContent) {
        WfOperationLog log = new WfOperationLog();
        log.setProcessInstanceId(processInstanceId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperateType(operateType);
        log.setOperateContent(operateContent);
        operationLogMapper.insert(log);
    }

    /**
     * 流程完成后更新单据状态为审批通过
     *
     * @return 转换后的 DocFeignResponse，供 MQ 使用；失败返回 null
     */
    private DocFeignResponse updateDocStatus(WfProcessInstance processInstance) {
        try {
            WfProcessInstanceBiz bizRelation = processInstanceBizMapper.selectOne(
                    new LambdaQueryWrapper<WfProcessInstanceBiz>()
                            .eq(WfProcessInstanceBiz::getProcessInstanceId, processInstance.getId())
                            .eq(WfProcessInstanceBiz::getDelFlag, 0)
            );
            if (bizRelation == null || bizRelation.getBizId() == null) {
                log.warn("流程完成：未找到业务关联信息，跳过单据状态更新");
                return null;
            }

            DocResponse doc = docService.findByDocNo(bizRelation.getBizId());
            if (doc == null) {
                log.warn("流程完成：单据不存在: bizId={}", bizRelation.getBizId());
                return null;
            }

            docService.approve(doc.getId(), "SYSTEM", "系统");
            docService.complete(doc.getId());
            log.info("流程完成：单据状态已更新为已完成: docNo={}, docType={}", doc.getDocNo(), doc.getDocType());

            return docConvertService.convertToFeignResponse(doc);

        } catch (Exception e) {
            log.error("流程完成：更新单据状态失败: processInstanceId={}, error={}",
                    processInstance.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 流程完成后发送MQ消息
     * 通知业务系统流程已审批通过
     *
     * <p>具体查关联、构建消息、发送 MQ 的逻辑放到 {@link FlowProcessCompletedMqService}
     * 中异步执行，避免阻塞审批接口返回</p>
     *
     * @param docFeignResponse 已查好的单据数据，会一起塞进 MQ；为空则消费者自行 Feign 查询（兼容老消息）
     */
    private void sendProcessCompletedMQ(WfProcessInstance processInstance, DocFeignResponse docFeignResponse) {
        try {
            WfProcessInstanceBiz bizRelation = processInstanceBizMapper.selectOne(
                    new LambdaQueryWrapper<WfProcessInstanceBiz>()
                            .eq(WfProcessInstanceBiz::getProcessInstanceId, processInstance.getId())
                            .eq(WfProcessInstanceBiz::getDelFlag, 0)
            );

            String bizType = null;
            String bizId = null;
            if (bizRelation != null) {
                bizType = bizRelation.getBizType();
                bizId = bizRelation.getBizId();
            }

            final String finalBizType = bizType;
            final String finalBizId = bizId;
            final DocFeignResponse finalDoc = docFeignResponse;

            // 在事务提交后再异步发送 MQ，确保消费者能读取到已提交的单据数据
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    flowProcessCompletedMqService.sendProcessCompletedMq(
                            processInstance.getId(),
                            processInstance.getTitle(),
                            processInstance.getStatus(),
                            finalBizType,
                            finalBizId,
                            finalDoc
                    );
                }
            });
        } catch (Exception e) {
            log.error("流程完成MQ注册失败: processInstanceId={}, error={}", processInstance.getId(), e.getMessage(), e);
            saveOperationLog(processInstance.getId(), "SYSTEM", "系统",
                    5, "流程完成，MQ注册失败: " + e.getMessage());
        }
    }
}