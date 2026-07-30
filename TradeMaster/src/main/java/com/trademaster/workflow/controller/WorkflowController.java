package com.trademaster.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademaster.common.Result;
import com.trademaster.workflow.dto.ProcessApproveDTO;
import com.trademaster.workflow.dto.ProcessStartDTO;
import com.trademaster.workflow.dto.TodoDTO;
import com.trademaster.workflow.entity.*;
import com.trademaster.workflow.mapper.*;
import com.trademaster.workflow.service.FlowEngineService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/workflow")
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
        this.objectMapper = objectMapper;
    }

    @PostMapping("/start")
    public Result<WfProcessInstance> startProcess(@RequestBody ProcessStartDTO startDTO) {
        try {
            WfProcessInstance instance = flowEngineService.startProcess(startDTO);
            return Result.success(instance);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/approve")
    public Result<Boolean> approve(@RequestBody ProcessApproveDTO approveDTO) {
        try {
            if (approveDTO.getApproveType() == null) {
                approveDTO.setApproveType(1);
            }
            boolean success = flowEngineService.approve(approveDTO);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reject")
    public Result<Boolean> reject(@RequestBody ProcessApproveDTO approveDTO) {
        try {
            approveDTO.setApproveType(2);
            boolean success = flowEngineService.approve(approveDTO);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/todo")
    public Result<List<TodoDTO>> getTodoList(@RequestParam String userId) {
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
                }
                result.add(dto);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/todo/page")
    public Result<IPage<WfTask>> getTodoListPage(
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
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<List<WfOperationLog>> getHistory(@RequestParam(required = false) String processInstanceId,
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
            return Result.success(history);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/definition")
    public Result<List<WfProcessDefinition>> getDefinitionList(
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
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/definition/{id}/detail")
    public Result<Map<String, Object>> getDefinitionDetail(@PathVariable Long id) {
        try {
            WfProcessDefinition definition = definitionMapper.selectById(id);
            if (definition == null) {
                return Result.error("流程定义不存在");
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
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/instance")
    public Result<List<WfProcessInstance>> getInstanceList(
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
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/instance/{id}/detail")
    public Result<Map<String, Object>> getInstanceDetail(@PathVariable String id) {
        try {
            WfProcessInstance instance = instanceMapper.selectById(id);
            if (instance == null) {
                return Result.error("流程实例不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("instance", instance);

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

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}")
    public Result<WfTask> getTaskDetail(@PathVariable String taskId) {
        try {
            WfTask task = taskMapper.selectById(taskId);
            return Result.success(task);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/task/{taskId}/approve")
    public Result<Boolean> approveTask(@PathVariable String taskId, @RequestBody ProcessApproveDTO approveDTO) {
        try {
            approveDTO.setTaskId(taskId);
            approveDTO.setApproveType(1);
            boolean success = flowEngineService.approve(approveDTO);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/task/{taskId}/reject")
    public Result<Boolean> rejectTask(@PathVariable String taskId, @RequestBody ProcessApproveDTO approveDTO) {
        try {
            approveDTO.setTaskId(taskId);
            approveDTO.setApproveType(2);
            boolean success = flowEngineService.approve(approveDTO);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/definition")
    public Result<WfProcessDefinition> createDefinition(@RequestBody Map<String, Object> body) {
        try {
            WfProcessDefinition def = new WfProcessDefinition();
            def.setProcessKey((String) body.get("processKey"));
            def.setProcessName((String) body.get("processName"));
            def.setRemark((String) body.get("remark"));
            def.setVersion(body.get("version") != null ? (Integer) body.get("version") : 1);
            def.setStatus(1);
            def.setDelFlag(0);
            def.setCreateTime(LocalDateTime.now());
            definitionMapper.insert(def);
            return Result.success(def);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/definition/{id}")
    public Result<WfProcessDefinition> updateDefinition(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            WfProcessDefinition def = definitionMapper.selectById(id);
            if (def == null) {
                return Result.error("流程定义不存在");
            }
            def.setProcessKey((String) body.get("processKey"));
            def.setProcessName((String) body.get("processName"));
            def.setRemark((String) body.get("remark"));
            def.setUpdateTime(LocalDateTime.now());
            definitionMapper.updateById(def);
            return Result.success(def);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/definition/{id}")
    public Result<Boolean> deleteDefinition(@PathVariable Long id) {
        try {
            WfProcessDefinition def = definitionMapper.selectById(id);
            if (def == null) {
                return Result.error("流程定义不存在");
            }
            def.setDelFlag(1);
            definitionMapper.updateById(def);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/definition/{id}/save-config")
    public Result<Boolean> saveConfig(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            WfProcessDefinition def = definitionMapper.selectById(id);
            if (def == null) {
                return Result.error("流程定义不存在");
            }

            nodeMapper.delete(new LambdaQueryWrapper<WfProcessNode>().eq(WfProcessNode::getProcessDefId, id));
            relationMapper.delete(new LambdaQueryWrapper<WfProcessRelation>().eq(WfProcessRelation::getProcessDefId, id));
            conditionConfigMapper.delete(new LambdaQueryWrapper<WfProcessConditionConfig>().eq(WfProcessConditionConfig::getProcessDefId, id));

            java.util.function.Function<Object, Long> toLong = (v) -> {
                if (v == null) return null;
                if (v instanceof Long) return (Long) v;
                if (v instanceof Number) return ((Number) v).longValue();
                String s = String.valueOf(v);
                if (s.isEmpty()) return null;
                try { return Long.parseLong(s); } catch (Exception e) { return null; }
            };
            java.util.function.BiFunction<Object, Integer, Integer> toInt = (v, d) -> {
                if (v == null) return d;
                if (v instanceof Number) return ((Number) v).intValue();
                String s = String.valueOf(v);
                if (s.isEmpty()) return d;
                try { return Integer.parseInt(s); } catch (Exception e) { return d; }
            };
            java.util.function.Function<Object, String> toStr = (v) -> v == null ? null : String.valueOf(v);

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
                    node.setCreateTime(LocalDateTime.now());
                    nodeMapper.insert(node);
                    if (node.getNodeKey() != null) {
                        nodeIdMap.put(node.getNodeKey(), node.getId());
                    }
                    Object rawId = nodeMap.get("id");
                    if (rawId != null) {
                        nodeIdMap.put(String.valueOf(rawId), node.getId());
                    }
                }
            }

            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> relList = (java.util.List<Map<String, Object>>) body.get("relations");
            if (relList != null) {
                for (Map<String, Object> relMap : relList) {
                    WfProcessRelation rel = new WfProcessRelation();
                    rel.setProcessDefId(id);
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
                    rel.setCreateTime(LocalDateTime.now());
                    relationMapper.insert(rel);
                }
            }

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
                    cond.setCreateTime(LocalDateTime.now());
                    conditionConfigMapper.insert(cond);
                }
            }

            def.setUpdateTime(LocalDateTime.now());
            definitionMapper.updateById(def);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/instance/{instanceId}/logs")
    public Result<List<WfOperationLog>> getOperationLogs(@PathVariable String instanceId) {
        try {
            LambdaQueryWrapper<WfOperationLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WfOperationLog::getProcessInstanceId, instanceId)
                    .orderByAsc(WfOperationLog::getCreateTime);
            List<WfOperationLog> logs = operationLogMapper.selectList(wrapper);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/instance/{instanceId}/approval-detail")
    public Result<Map<String, Object>> getApprovalDetail(@PathVariable String instanceId) {
        try {
            WfProcessInstance instance = instanceMapper.selectById(instanceId);
            if (instance == null) {
                return Result.error("流程实例不存在");
            }

            WfProcessDefinition definition = definitionMapper.selectById(instance.getProcessDefId());
            if (definition == null) {
                return Result.error("流程定义不存在");
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

            LambdaQueryWrapper<WfProcessInstanceBiz> bizWrapper = new LambdaQueryWrapper<>();
            bizWrapper.eq(WfProcessInstanceBiz::getProcessInstanceId, instanceId);
            WfProcessInstanceBiz bizRelation = bizMapper.selectOne(bizWrapper);
            if (bizRelation != null) {
                result.put("bizType", bizRelation.getBizType());
                result.put("bizId", bizRelation.getBizId());
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
