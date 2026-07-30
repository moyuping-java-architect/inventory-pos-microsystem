package com.psi.flow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.flow.dto.ProcessStartDTO;
import com.psi.flow.dto.ProcessApproveDTO;
import com.psi.flow.dto.TodoDTO;
import com.psi.flow.entity.WfProcessInstance;
import com.psi.flow.entity.WfProcessDefinition;
import com.psi.flow.entity.WfProcessNode;
import com.psi.flow.entity.WfProcessRelation;
import com.psi.flow.entity.WfProcessConditionConfig;
import com.psi.flow.entity.WfProcessInstanceBiz;
import com.psi.flow.entity.WfTask;
import com.psi.flow.entity.WfOperationLog;
import com.psi.flow.service.FlowEngineService;
import com.psi.flow.mapper.*;
import com.psi.order.dto.DocResponse;
import com.psi.order.service.DocService;
import com.psi.common.result.CommonResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/psi/workflow")
public class WorkflowController {

    private final FlowEngineService flowEngineService;
    private final WfTaskMapper taskMapper;
    private final WfOperationLogMapper operationLogMapper;
    private final WfProcessDefinitionMapper definitionMapper;
    private final WfProcessInstanceMapper instanceMapper;
    private final WfProcessNodeMapper nodeMapper;
    private final WfProcessRelationMapper relationMapper;
    private final WfProcessConditionConfigMapper conditionConfigMapper;
    private final WfProcessInstanceBizMapper bizMapper;
    private final DocService docService;
    private final ObjectMapper objectMapper;

    public WorkflowController(FlowEngineService flowEngineService,
                             WfTaskMapper taskMapper,
                             WfOperationLogMapper operationLogMapper,
                             WfProcessDefinitionMapper definitionMapper,
                             WfProcessInstanceMapper instanceMapper,
                             WfProcessNodeMapper nodeMapper,
                             WfProcessRelationMapper relationMapper,
                             WfProcessConditionConfigMapper conditionConfigMapper,
                             WfProcessInstanceBizMapper bizMapper,
                             DocService docService,
                             ObjectMapper objectMapper) {
        this.flowEngineService = flowEngineService;
        this.taskMapper = taskMapper;
        this.operationLogMapper = operationLogMapper;
        this.definitionMapper = definitionMapper;
        this.instanceMapper = instanceMapper;
        this.nodeMapper = nodeMapper;
        this.relationMapper = relationMapper;
        this.conditionConfigMapper = conditionConfigMapper;
        this.bizMapper = bizMapper;
        this.docService = docService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/start")
    public CommonResult<WfProcessInstance> startProcess(@RequestBody ProcessStartDTO startDTO) {
        try {
            WfProcessInstance instance = flowEngineService.startProcess(startDTO);
            return CommonResult.success(instance);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @PostMapping("/approve")
    public CommonResult<Boolean> approve(@RequestBody ProcessApproveDTO approveDTO) {
        try {
            if (approveDTO.getApproveType() == null) {
                approveDTO.setApproveType(1);
            }
            boolean success = flowEngineService.approve(approveDTO);
            if (success) {
                return CommonResult.success(true);
            }
            return CommonResult.fail("审批失败");
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @PostMapping("/reject")
    public CommonResult<Boolean> reject(@RequestBody ProcessApproveDTO approveDTO) {
        try {
            approveDTO.setApproveType(2);
            boolean success = flowEngineService.approve(approveDTO);
            if (success) {
                return CommonResult.success(true);
            }
            return CommonResult.fail("驳回失败");
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @GetMapping("/todo")
    public CommonResult<List<TodoDTO>> getTodoList(@RequestParam String userId) {
        try {
            LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WfTask::getHandlerUserId, userId)
                    .eq(WfTask::getStatus, 1)
                    .eq(WfTask::getDelFlag, 0)
                    .orderByDesc(WfTask::getCreateTime);
            List<WfTask> todoList = taskMapper.selectList(wrapper);

            List<TodoDTO> result = new ArrayList<>();
            for (WfTask task : todoList) {
                TodoDTO dto = new TodoDTO();
                dto.setId(task.getId());
                dto.setProcessInstanceId(task.getProcessInstanceId());
                dto.setNodeId(task.getNodeId());
                dto.setTaskName(task.getTaskName());
                dto.setHandlerUserId(task.getHandlerUserId());
                dto.setHandlerUserName(task.getHandlerUserName());
                dto.setStatus(task.getStatus());
                dto.setHandleNote(task.getHandleNote());
                dto.setHandleTime(task.getHandleTime());
                dto.setCreateTime(task.getCreateTime());

                WfProcessInstanceBiz biz = bizMapper.selectOne(
                        new LambdaQueryWrapper<WfProcessInstanceBiz>()
                                .eq(WfProcessInstanceBiz::getProcessInstanceId, task.getProcessInstanceId())
                                .eq(WfProcessInstanceBiz::getDelFlag, 0)
                );
                if (biz != null) {
                    dto.setBizType(biz.getBizType());
                    DocResponse doc = docService.findByDocNo(biz.getBizId());
                    if (doc != null) {
                        dto.setDocType(doc.getDocType());
                        dto.setDocNo(doc.getDocNo());
                        dto.setDocName(doc.getDocName());
                        dto.setCreatorId(doc.getCreatorId());
                        dto.setCreatorName(doc.getCreatorName());
                    }
                }
                result.add(dto);
            }
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @GetMapping("/todo/page")
    public CommonResult<IPage<WfTask>> getTodoListPage(
            @RequestParam String userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<WfTask> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WfTask::getHandlerUserId, userId)
                    .eq(WfTask::getStatus, 1)
                    .eq(WfTask::getDelFlag, 0)
                    .orderByDesc(WfTask::getCreateTime);
            IPage<WfTask> result = taskMapper.selectPage(page, wrapper);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @GetMapping("/history")
    public CommonResult<List<WfOperationLog>> getHistory(@RequestParam(required = false) String processInstanceId,
                                                         @RequestParam(required = false) String userId) {
        try {
            LambdaQueryWrapper<WfOperationLog> wrapper = new LambdaQueryWrapper<>();
            if (processInstanceId != null && !processInstanceId.isEmpty()) {
                wrapper.eq(WfOperationLog::getProcessInstanceId, processInstanceId);
            }
            if (userId != null && !userId.isEmpty()) {
                wrapper.eq(WfOperationLog::getOperatorId, userId);
            }
            wrapper.orderByDesc(WfOperationLog::getCreateTime);
            List<WfOperationLog> history = operationLogMapper.selectList(wrapper);
            return CommonResult.success(history);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @GetMapping("/definition")
    public CommonResult<List<WfProcessDefinition>> getDefinitionList(
            @RequestParam(required = false) String keyword) {
        try {
            LambdaQueryWrapper<WfProcessDefinition> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WfProcessDefinition::getStatus, 1)
                    .eq(WfProcessDefinition::getDelFlag, 0)
                    .orderByDesc(WfProcessDefinition::getCreateTime);
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(WfProcessDefinition::getProcessName, keyword);
            }
            List<WfProcessDefinition> list = definitionMapper.selectList(wrapper);
            return CommonResult.success(list);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 获取流程定义详情（包含节点、条件配置、流转关系）
     */
    @GetMapping("/definition/{id}/detail")
    public CommonResult<Map<String, Object>> getDefinitionDetail(@PathVariable Long id) {
        try {
            WfProcessDefinition definition = definitionMapper.selectById(id);
            if (definition == null) {
                return CommonResult.fail("流程定义不存在");
            }

            List<WfProcessNode> nodes = nodeMapper.selectByProcessDefId(id);
            List<WfProcessConditionConfig> conditions = conditionConfigMapper.selectByProcessDefId(id);
            List<WfProcessRelation> relations = relationMapper.selectList(
                    new LambdaQueryWrapper<WfProcessRelation>()
                            .eq(WfProcessRelation::getProcessDefId, id)
                            .eq(WfProcessRelation::getDelFlag, 0)
                            .eq(WfProcessRelation::getStatus, 1)
            );

            Map<String, Object> result = new HashMap<>();
            result.put("definition", definition);
            result.put("nodes", nodes);
            result.put("conditions", conditions);
            result.put("relations", relations);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @GetMapping("/instance")
    public CommonResult<List<WfProcessInstance>> getInstanceList(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Integer status) {
        try {
            LambdaQueryWrapper<WfProcessInstance> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WfProcessInstance::getDelFlag, 0)
                    .orderByDesc(WfProcessInstance::getCreateTime);
            if (userId != null && !userId.isEmpty()) {
                wrapper.eq(WfProcessInstance::getStartUserId, userId);
            }
            if (status != null) {
                wrapper.eq(WfProcessInstance::getStatus, status);
            }
            List<WfProcessInstance> list = instanceMapper.selectList(wrapper);
            return CommonResult.success(list);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 获取流程实例详情（含流程变量）
     */
    @GetMapping("/instance/{id}/detail")
    public CommonResult<Map<String, Object>> getInstanceDetail(@PathVariable String id) {
        try {
            WfProcessInstance instance = instanceMapper.selectById(id);
            if (instance == null) {
                return CommonResult.fail("流程实例不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("instance", instance);

            // 解析流程变量
            if (instance.getExtJson() != null && !instance.getExtJson().isEmpty()) {
                try {
                    Map<String, Object> variables = objectMapper.readValue(instance.getExtJson(), HashMap.class);
                    result.put("variables", variables);
                } catch (Exception e) {
                    result.put("variables", new HashMap<>());
                }
            } else {
                result.put("variables", new HashMap<>());
            }

            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}")
    public CommonResult<WfTask> getTaskDetail(@PathVariable String taskId) {
        try {
            WfTask task = taskMapper.selectById(taskId);
            return CommonResult.success(task);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @PostMapping("/task/{taskId}/approve")
    public CommonResult<Boolean> approveTask(@PathVariable String taskId, @RequestBody ProcessApproveDTO approveDTO) {
        try {
            approveDTO.setTaskId(taskId);
            approveDTO.setApproveType(1);
            boolean success = flowEngineService.approve(approveDTO);
            if (success) {
                return CommonResult.success(true);
            }
            return CommonResult.fail("审批失败");
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    @PostMapping("/task/{taskId}/reject")
    public CommonResult<Boolean> rejectTask(@PathVariable String taskId, @RequestBody ProcessApproveDTO approveDTO) {
        try {
            approveDTO.setTaskId(taskId);
            approveDTO.setApproveType(2);
            boolean success = flowEngineService.approve(approveDTO);
            if (success) {
                return CommonResult.success(true);
            }
            return CommonResult.fail("驳回失败");
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    // ============ 流程定义配置管理 ============

    /**
     * 新增流程定义
     */
    @PostMapping("/definition")
    public CommonResult<WfProcessDefinition> createDefinition(@RequestBody Map<String, Object> body) {
        try {
            WfProcessDefinition def = new WfProcessDefinition();
            def.setProcessKey((String) body.get("processKey"));
            def.setProcessName((String) body.get("processName"));
            def.setRemark((String) body.get("remark"));
            def.setVersion(body.get("version") != null ? (Integer) body.get("version") : 1);
            def.setStatus(1);
            def.setDelFlag(0);
            def.setTenantId(1L);
            def.setCreateTime(LocalDateTime.now());
            definitionMapper.insert(def);
            return CommonResult.success(def);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 更新流程定义
     */
    @PutMapping("/definition/{id}")
    public CommonResult<WfProcessDefinition> updateDefinition(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            WfProcessDefinition def = definitionMapper.selectById(id);
            if (def == null) {
                return CommonResult.fail("流程定义不存在");
            }
            def.setProcessKey((String) body.get("processKey"));
            def.setProcessName((String) body.get("processName"));
            def.setRemark((String) body.get("remark"));
            def.setUpdateTime(LocalDateTime.now());
            definitionMapper.updateById(def);
            return CommonResult.success(def);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 删除流程定义
     */
    @DeleteMapping("/definition/{id}")
    public CommonResult<Boolean> deleteDefinition(@PathVariable Long id) {
        try {
            WfProcessDefinition def = definitionMapper.selectById(id);
            if (def == null) {
                return CommonResult.fail("流程定义不存在");
            }
            def.setDelFlag(1);
            definitionMapper.updateById(def);
            return CommonResult.success(true);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 保存完整流程配置（节点+关系+条件变量）
     * body: {
     *   definition: {...},
     *   nodes: [{...}],
     *   relations: [{...}],
     *   conditions: [{...}]
     * }
     */
    @PostMapping("/definition/{id}/save-config")
    public CommonResult<Boolean> saveConfig(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            WfProcessDefinition def = definitionMapper.selectById(id);
            if (def == null) {
                return CommonResult.fail("流程定义不存在");
            }

            // 1. 删除旧的节点、关系、条件配置
            nodeMapper.delete(new LambdaQueryWrapper<WfProcessNode>().eq(WfProcessNode::getProcessDefId, id));
            relationMapper.delete(new LambdaQueryWrapper<WfProcessRelation>().eq(WfProcessRelation::getProcessDefId, id));
            conditionConfigMapper.delete(new LambdaQueryWrapper<WfProcessConditionConfig>().eq(WfProcessConditionConfig::getProcessDefId, id));

            // 工具：安全读取对象为 Long
            java.util.function.Function<Object, Long> toLong = (v) -> {
                if (v == null) return null;
                if (v instanceof Long) return (Long) v;
                if (v instanceof Number) return ((Number) v).longValue();
                String s = String.valueOf(v);
                if (s.isEmpty()) return null;
                try { return Long.parseLong(s); } catch (Exception e) { return null; }
            };
            // 工具：安全读取对象为 int
            java.util.function.BiFunction<Object, Integer, Integer> toInt = (v, d) -> {
                if (v == null) return d;
                if (v instanceof Number) return ((Number) v).intValue();
                String s = String.valueOf(v);
                if (s.isEmpty()) return d;
                try { return Integer.parseInt(s); } catch (Exception e) { return d; }
            };
            // 工具：安全读取对象为 String
            java.util.function.Function<Object, String> toStr = (v) -> v == null ? null : String.valueOf(v);

            // 2. 保存节点（记录 nodeKey -> 新ID 映射）
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> nodeList = (java.util.List<Map<String, Object>>) body.get("nodes");
            java.util.Map<String, Long> nodeIdMap = new HashMap<>();
            if (nodeList != null) {
                int sort = 1;
                for (Map<String, Object> nodeMap : nodeList) {
                    WfProcessNode node = new WfProcessNode();
                    node.setProcessDefId(id);
                    node.setNodeKey(toStr.apply(nodeMap.get("nodeKey")));
                    node.setNodeName(toStr.apply(nodeMap.get("nodeName")));
                    node.setNodeType(toInt.apply(nodeMap.get("nodeType"), 1));
                    node.setApproveType(toInt.apply(nodeMap.get("approveType"), 1));
                    node.setSort(sort++);
                    node.setConfig(toStr.apply(nodeMap.get("config")));
                    node.setStatus(1);
                    node.setDelFlag(0);
                    node.setTenantId(1L);
                    node.setCreateTime(LocalDateTime.now());
                    nodeMapper.insert(node);
                    // 用 nodeKey 作为 key（前端可直接传 nodeKey 作为 from/to 标识）
                    if (node.getNodeKey() != null) {
                        nodeIdMap.put(node.getNodeKey(), node.getId());
                    }
                    // 也记录 id（前端传数据库 id）
                    Object rawId = nodeMap.get("id");
                    if (rawId != null) {
                        nodeIdMap.put(String.valueOf(rawId), node.getId());
                    }
                }
            }

            // 3. 保存关系
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> relList = (java.util.List<Map<String, Object>>) body.get("relations");
            if (relList != null) {
                for (Map<String, Object> relMap : relList) {
                    WfProcessRelation rel = new WfProcessRelation();
                    rel.setProcessDefId(id);
                    // 优先直接读 Long，否则从 nodeIdMap 找（按 nodeKey）
                    Object fromKey = relMap.get("fromNodeId");
                    Object toKey = relMap.get("toNodeId");
                    Long fromId = toLong.apply(fromKey);
                    Long toId = toLong.apply(toKey);
                    if (fromId == null && fromKey != null) {
                        fromId = nodeIdMap.get(String.valueOf(fromKey));
                    }
                    if (toId == null && toKey != null) {
                        toId = nodeIdMap.get(String.valueOf(toKey));
                    }
                    rel.setFromNodeId(fromId);
                    rel.setToNodeId(toId);
                    rel.setConditionExpr(toStr.apply(relMap.get("conditionExpr")));
                    rel.setStatus(1);
                    rel.setDelFlag(0);
                    rel.setTenantId(1L);
                    rel.setCreateTime(LocalDateTime.now());
                    relationMapper.insert(rel);
                }
            }

            // 4. 保存条件变量配置
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> condList = (java.util.List<Map<String, Object>>) body.get("conditions");
            if (condList != null) {
                int csort = 1;
                for (Map<String, Object> condMap : condList) {
                    WfProcessConditionConfig cond = new WfProcessConditionConfig();
                    cond.setProcessDefId(id);
                    cond.setConditionName(toStr.apply(condMap.get("conditionName")));
                    cond.setConditionKey(toStr.apply(condMap.get("conditionKey")));
                    cond.setConditionType(toStr.apply(condMap.get("conditionType")));
                    cond.setCompareType(toStr.apply(condMap.get("compareType")));
                    cond.setDefaultValue(toStr.apply(condMap.get("defaultValue")));
                    cond.setSort(csort++);
                    cond.setStatus(1);
                    cond.setDelFlag(0);
                    cond.setTenantId(1L);
                    cond.setCreateTime(LocalDateTime.now());
                    conditionConfigMapper.insert(cond);
                }
            }

            def.setUpdateTime(LocalDateTime.now());
            definitionMapper.updateById(def);
            return CommonResult.success(true);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 获取流程实例所有操作日志
     */
    @GetMapping("/instance/{instanceId}/logs")
    public CommonResult<List<WfOperationLog>> getOperationLogs(@PathVariable String instanceId) {
        try {
            LambdaQueryWrapper<WfOperationLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WfOperationLog::getProcessInstanceId, instanceId)
                    .orderByAsc(WfOperationLog::getCreateTime);
            List<WfOperationLog> logs = operationLogMapper.selectList(wrapper);
            return CommonResult.success(logs);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }

    /**
     * 获取流程实例审批详情（含流程图数据、当前节点、任务、日志、业务单据）
     */
    @GetMapping("/instance/{instanceId}/approval-detail")
    public CommonResult<Map<String, Object>> getApprovalDetail(@PathVariable String instanceId) {
        try {
            WfProcessInstance instance = instanceMapper.selectById(instanceId);
            if (instance == null) {
                return CommonResult.fail("流程实例不存在");
            }

            WfProcessDefinition definition = definitionMapper.selectById(instance.getProcessDefId());
            if (definition == null) {
                return CommonResult.fail("流程定义不存在");
            }

            List<WfProcessNode> nodes = nodeMapper.selectByProcessDefId(instance.getProcessDefId());
            List<WfProcessRelation> relations = relationMapper.selectList(
                    new LambdaQueryWrapper<WfProcessRelation>()
                            .eq(WfProcessRelation::getProcessDefId, instance.getProcessDefId())
                            .eq(WfProcessRelation::getDelFlag, 0)
                            .eq(WfProcessRelation::getStatus, 1)
            );

            LambdaQueryWrapper<WfTask> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.eq(WfTask::getProcessInstanceId, instanceId)
                    .eq(WfTask::getDelFlag, 0)
                    .orderByAsc(WfTask::getCreateTime);
            List<WfTask> tasks = taskMapper.selectList(taskWrapper);

            LambdaQueryWrapper<WfOperationLog> logWrapper = new LambdaQueryWrapper<>();
            logWrapper.eq(WfOperationLog::getProcessInstanceId, instanceId)
                    .orderByAsc(WfOperationLog::getCreateTime);
            List<WfOperationLog> logs = operationLogMapper.selectList(logWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("instance", instance);
            result.put("definition", definition);
            result.put("nodes", nodes);
            result.put("relations", relations);
            result.put("currentNodeId", instance.getCurrentNodeId());
            result.put("tasks", tasks);
            result.put("logs", logs);

            if (instance.getExtJson() != null && !instance.getExtJson().isEmpty()) {
                try {
                    result.put("variables", objectMapper.readValue(instance.getExtJson(), HashMap.class));
                } catch (Exception e) {
                    result.put("variables", new HashMap<>());
                }
            } else {
                result.put("variables", new HashMap<>());
            }

            // 查询业务单据关联
            LambdaQueryWrapper<WfProcessInstanceBiz> bizWrapper = new LambdaQueryWrapper<>();
            bizWrapper.eq(WfProcessInstanceBiz::getProcessInstanceId, instanceId);
            WfProcessInstanceBiz bizRelation = bizMapper.selectOne(bizWrapper);
            if (bizRelation != null) {
                result.put("bizType", bizRelation.getBizType());
                result.put("bizId", bizRelation.getBizId());
                // 查询业务单据详情
                try {
                    DocResponse doc = docService.findByDocNo(bizRelation.getBizId());
                    result.put("doc", doc);
                    if (doc != null) {
                        result.put("docName", doc.getDocName());
                        result.put("docNo", doc.getDocNo());
                        result.put("docType", doc.getDocType());
                    }
                } catch (Exception e) {
                    result.put("doc", null);
                }
            }

            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.fail(e.getMessage());
        }
    }
}