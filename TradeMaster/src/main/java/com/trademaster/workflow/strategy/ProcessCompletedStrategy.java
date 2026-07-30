package com.trademaster.workflow.strategy;

import com.trademaster.workflow.entity.WfProcessInstance;

public interface ProcessCompletedStrategy {

    boolean supports(String bizType);

    void handleProcessCompleted(WfProcessInstance processInstance, String bizId);
}
