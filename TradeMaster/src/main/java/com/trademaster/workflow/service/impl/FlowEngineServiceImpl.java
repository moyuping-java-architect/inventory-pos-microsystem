package com.trademaster.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trademaster.workflow.dto.ProcessApproveDTO;
import com.trademaster.workflow.dto.ProcessStartDTO;
import com.trademaster.workflow.entity.*;
import com.trademaster.workflow.mapper.*;
import com.trademaster.workflow.service.FlowEngineService;
import com.trademaster.workflow.service.ProcessCompletedAsyncService;
import com.trademaster.workflow.util.ElExpressionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final ObjectMapper objectMapper;
    private final ProcessCompletedAsyncService processCompletedAsyncService;

    public FlowEngineServiceImpl(WfProcessDefinitionMapper processDefinitionMapper,
                                 WfProcessNodeMapper processNodeMapper,
                                 WfProcessRelationMapper processRelationMapper,
                                 WfProcessInstanceMapper processInstanceMapper,
                                 WfProcessInstanceBizMapper processInstanceBizMapper,
                                 WfTaskMapper taskMapper,
                                 WfOperationLogMapper operationLogMapper,
                                 WfCcLogMapper ccLogMapper,
                                 ObjectMapper objectMapper,
                                 ProcessCompletedAsyncService processCompletedAsyncService) {
        this.processDefinitionMapper = processDefinitionMapper;
        this.processNodeMapper = processNodeMapper;
        this.processRelationMapper = processRelationMapper;
        this.processInstanceMapper = processInstanceMapper;
        this.processInstanceBizMapper = processInstanceBizMapper;
        this.taskMapper = taskMapper;
        this.operationLogMapper = operationLogMapper;
        this.ccLogMapper = ccLogMapper;
        this.objectMapper = objectMapper;
        this.processCompletedAsyncService = processCompletedAsyncService;
    }

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

    @Override
    @Transactional
    public WfProcessInstance startProcess(ProcessStartDTO startDTO) {
        WfProcessDefinition processDef = processDefinitionMapper.selectLatestByKey(startDTO.getProcessKey());
        if (processDef == null) {
            log.warn("流程定义不存在，跳过工作流启动: processKey={}", startDTO.getProcessKey());
            return null;
        }

        WfProcessInstance processInstance = new WfProcessInstance();
        processInstance.setId(UUID.randomUUID().toString().replace("-", ""));
        processInstance.setProcessDefId(processDef.getId());
        processInstance.setProcessKey(processDef.getProcessKey());
        processInstance.setTitle(startDTO.getTitle());
        processInstance.setStartUserId(startDTO.getStartUserId());
        processInstance.setStartUserName(startDTO.getStartUserName());
        processInstance.setStatus(1);
        processInstance.setDelFlag(0);
        processInstance.setExtJson(variablesToJson(startDTO.getVariables()));
        processInstanceMapper.insert(processInstance);

        WfProcessInstanceBiz bizRelation = new WfProcessInstanceBiz();
        bizRelation.setProcessInstanceId(processInstance.getId());
        bizRelation.setBizType(startDTO.getBizType());
        bizRelation.setBizId(startDTO.getBizId());
        processInstanceBizMapper.insert(bizRelation);

        WfProcessNode startNode = processNodeMapper.selectStartNode(processDef.getId());
        if (startNode == null) {
            throw new RuntimeException("流程定义没有配置起始审批节点");
        }

        Map<String, Object> initVars = startDTO.getVariables() != null ? startDTO.getVariables() : new HashMap<>();
        processNodeByAutoFlow(processInstance, startNode, initVars);

        saveOperationLog(processInstance.getId(), startDTO.getStartUserId(),
                startDTO.getStartUserName(), 1, "发起流程");

        return processInstance;
    }

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

        Long actualNodeId = handleNextNode(processInstance.getId(), node, variables);
        processInstance.setCurrentNodeId(actualNodeId);
        processInstanceMapper.updateById(processInstance);
    }

    @Override
    @Transactional
    public boolean approve(ProcessApproveDTO approveDTO) {
        log.info("approve start: taskId={}, approveType={}, variables={}", approveDTO.getTaskId(), approveDTO.getApproveType(), approveDTO.getVariables());

        WfTask task = taskMapper.selectById(approveDTO.getTaskId());
        if (task == null || task.getStatus() != 1) {
            log.warn("approve: task not found or already processed, taskId={}", approveDTO.getTaskId());
            throw new RuntimeException("任务不存在或已处理");
        }

        WfProcessInstance processInstance = processInstanceMapper.selectById(task.getProcessInstanceId());
        if (processInstance == null) {
            throw new RuntimeException("流程实例不存在");
        }

        WfProcessDefinition processDef = processDefinitionMapper.selectById(processInstance.getProcessDefId());
        if (processDef == null) {
            throw new RuntimeException("流程定义不存在");
        }

        Map<String, Object> mergedVariables = jsonToVariables(processInstance.getExtJson());
        if (approveDTO.getVariables() != null && !approveDTO.getVariables().isEmpty()) {
            mergedVariables.putAll(approveDTO.getVariables());
        }

        task.setStatus(2);
        task.setHandlerUserId(approveDTO.getHandlerUserId());
        task.setHandlerUserName(approveDTO.getHandlerUserName());
        task.setHandleNote(approveDTO.getHandleNote());
        task.setHandleTime(LocalDateTime.now());
        taskMapper.updateById(task);

        if (approveDTO.getApproveType() == 3 || approveDTO.getApproveType() == 2) {
            processInstance.setStatus(3);
            processInstance.setEndTime(LocalDateTime.now());
            processInstanceMapper.updateById(processInstance);

            saveOperationLog(processInstance.getId(), approveDTO.getHandlerUserId(),
                    approveDTO.getHandlerUserName(), 3, "驳回流程");
            return true;
        }

        Long nextNodeId = findNextNode(processDef.getId(), task.getNodeId(), mergedVariables);
        log.info("approve: nextNodeId={}, mergedVariables={}", nextNodeId, mergedVariables);

        if (nextNodeId == null) {
            processInstance.setStatus(2);
            processInstance.setEndTime(LocalDateTime.now());
            processInstance.setCurrentNodeId(null);
            processInstanceMapper.updateById(processInstance);

            saveOperationLog(processInstance.getId(), approveDTO.getHandlerUserId(),
                    approveDTO.getHandlerUserName(), 2, "审批通过，流程完成");

            triggerProcessCompletedAsync(processInstance);
            return true;
        }

        WfProcessNode nextNode = processNodeMapper.selectById(nextNodeId);
        Long actualNodeId = handleNextNode(processInstance.getId(), nextNode, mergedVariables);
        log.info("approve: actualNodeId={}, nextNode type={}", actualNodeId, nextNode != null ? nextNode.getNodeType() : null);

        if (actualNodeId == null) {
            processInstance.setStatus(2);
            processInstance.setEndTime(LocalDateTime.now());
            processInstance.setCurrentNodeId(null);
            processInstanceMapper.updateById(processInstance);

            saveOperationLog(processInstance.getId(), approveDTO.getHandlerUserId(),
                    approveDTO.getHandlerUserName(), 2, "审批通过，流程完成");

            triggerProcessCompletedAsync(processInstance);
            return true;
        }

        processInstance.setCurrentNodeId(actualNodeId);
        processInstance.setExtJson(variablesToJson(mergedVariables));
        processInstanceMapper.updateById(processInstance);

        saveOperationLog(processInstance.getId(), approveDTO.getHandlerUserId(),
                approveDTO.getHandlerUserName(), 2, "审批通过");

        return true;
    }

    private Long findNextNode(Long processDefId, Long currentNodeId, Map<String, Object> variables) {
        List<WfProcessRelation> relations = processRelationMapper.selectByFromNode(processDefId, currentNodeId);
        log.info("findNextNode: processDefId={}, currentNodeId={}, relations count={}", processDefId, currentNodeId, relations != null ? relations.size() : 0);

        if (relations == null || relations.isEmpty()) {
            return null;
        }

        if (relations.size() == 1) {
            return relations.get(0).getToNodeId();
        }

        for (WfProcessRelation relation : relations) {
            String conditionExpr = relation.getConditionExpr();
            if (conditionExpr == null || conditionExpr.isEmpty()) {
                continue;
            }
            if (ElExpressionUtil.evaluateBoolean(conditionExpr, variables)) {
                return relation.getToNodeId();
            }
        }

        for (WfProcessRelation relation : relations) {
            if (relation.getConditionExpr() == null || relation.getConditionExpr().isEmpty()) {
                return relation.getToNodeId();
            }
        }

        return null;
    }

    private Long handleNextNode(String processInstanceId, WfProcessNode nextNode, Map<String, Object> variables) {
        if (nextNode == null) {
            return null;
        }

        switch (nextNode.getNodeType()) {
            case 1:
                createTask(processInstanceId, nextNode);
                return nextNode.getId();
            case 2:
                Long nextNextNodeId = findNextNode(nextNode.getProcessDefId(), nextNode.getId(), variables);
                if (nextNextNodeId != null) {
                    WfProcessNode nextNextNode = processNodeMapper.selectById(nextNextNodeId);
                    return handleNextNode(processInstanceId, nextNextNode, variables);
                }
                return null;
            case 3:
                createCcLog(processInstanceId, nextNode);
                Long ccNextNodeId = findNextNode(nextNode.getProcessDefId(), nextNode.getId(), variables);
                if (ccNextNodeId != null) {
                    WfProcessNode ccNextNode = processNodeMapper.selectById(ccNextNodeId);
                    return handleNextNode(processInstanceId, ccNextNode, variables);
                }
                return null;
            case 4:
                return null;
            default:
                return null;
        }
    }

    private void createTask(String processInstanceId, WfProcessNode node) {
        String handlerUserId = null;
        String handlerUserName = null;

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
        task.setStatus(1);
        task.setDelFlag(0);
        taskMapper.insert(task);
    }

    private void createCcLog(String processInstanceId, WfProcessNode node) {
        String ccUserId = "default_cc";
        String ccUserName = "默认抄送人";

        WfCcLog ccLog = new WfCcLog();
        ccLog.setProcessInstanceId(processInstanceId);
        ccLog.setCcUserId(ccUserId);
        ccLog.setCcUserName(ccUserName);
        ccLog.setStatus(1);
        ccLog.setDelFlag(0);
        ccLogMapper.insert(ccLog);
    }

    private void triggerProcessCompletedAsync(WfProcessInstance processInstance) {
        try {
            WfProcessInstanceBiz bizRelation = processInstanceBizMapper.selectOne(
                    new LambdaQueryWrapper<WfProcessInstanceBiz>()
                            .eq(WfProcessInstanceBiz::getProcessInstanceId, processInstance.getId())
                            .eq(WfProcessInstanceBiz::getDelFlag, 0)
            );

            if (bizRelation != null && bizRelation.getBizId() != null) {
                log.info("触发流程完成异步处理: processInstanceId={}, bizType={}, bizId={}",
                        processInstance.getId(), bizRelation.getBizType(), bizRelation.getBizId());
                processCompletedAsyncService.handleProcessCompleted(
                        processInstance,
                        bizRelation.getBizType(),
                        bizRelation.getBizId()
                );
            } else {
                log.info("流程完成：未找到业务关联信息，跳过异步处理: processInstanceId={}", processInstance.getId());
            }
        } catch (Exception e) {
            log.error("触发流程完成异步处理失败: processInstanceId={}, error={}",
                    processInstance.getId(), e.getMessage(), e);
        }
    }

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
}
