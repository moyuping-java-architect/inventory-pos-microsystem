package com.trademaster.workflow.strategy;

import com.trademaster.workflow.entity.WfProcessInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommonProcessCompletedStrategy implements ProcessCompletedStrategy {

    @Override
    public boolean supports(String bizType) {
        return true;
    }

    @Override
    public void handleProcessCompleted(WfProcessInstance processInstance, String bizId) {
        log.info("通用单据审批通过，处理正式数据: processInstanceId={}, bizId={}", processInstance.getId(), bizId);
    }
}
