package com.trademaster.workflow.service.impl;

import com.trademaster.workflow.entity.WfOperationLog;
import com.trademaster.workflow.entity.WfProcessInstance;
import com.trademaster.workflow.mapper.WfOperationLogMapper;
import com.trademaster.workflow.service.ProcessCompletedAsyncService;
import com.trademaster.workflow.strategy.ProcessCompletedStrategy;
import com.trademaster.workflow.strategy.ProcessCompletedStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessCompletedAsyncServiceImpl implements ProcessCompletedAsyncService {

    private final ProcessCompletedStrategyFactory strategyFactory;
    private final WfOperationLogMapper operationLogMapper;

    public ProcessCompletedAsyncServiceImpl(ProcessCompletedStrategyFactory strategyFactory,
                                            WfOperationLogMapper operationLogMapper) {
        this.strategyFactory = strategyFactory;
        this.operationLogMapper = operationLogMapper;
    }

    @Async("workflowAsyncExecutor")
    @Override
    public void handleProcessCompleted(WfProcessInstance processInstance, String bizType, String bizId) {
        try {
            log.info("异步处理流程完成: processInstanceId={}, bizType={}, bizId={}",
                    processInstance.getId(), bizType, bizId);

            ProcessCompletedStrategy strategy = strategyFactory.getStrategy(bizType);
            log.info("匹配策略: {}", strategy.getClass().getSimpleName());

            strategy.handleProcessCompleted(processInstance, bizId);

            saveOperationLog(processInstance.getId(), "流程完成，异步处理成功: " + bizType);
            log.info("流程完成异步处理结束: processInstanceId={}", processInstance.getId());
        } catch (Exception e) {
            log.error("流程完成异步处理失败: processInstanceId={}, error={}",
                    processInstance.getId(), e.getMessage(), e);
            saveOperationLog(processInstance.getId(), "流程完成，异步处理失败: " + e.getMessage());
        }
    }

    private void saveOperationLog(String processInstanceId, String content) {
        try {
            WfOperationLog operationLog = new WfOperationLog();
            operationLog.setProcessInstanceId(processInstanceId);
            operationLog.setOperateType(5);
            operationLog.setOperatorId("SYSTEM");
            operationLog.setOperatorName("系统");
            operationLog.setOperateContent(content);
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.warn("保存流程操作日志失败: processInstanceId={}, error={}", processInstanceId, e.getMessage());
        }
    }
}
