package com.trademaster.workflow.service;

import com.trademaster.workflow.entity.WfProcessInstance;

public interface ProcessCompletedAsyncService {

    void handleProcessCompleted(WfProcessInstance processInstance, String bizType, String bizId);
}
