package com.trademaster.workflow.service;

import com.trademaster.workflow.dto.ProcessApproveDTO;
import com.trademaster.workflow.dto.ProcessStartDTO;
import com.trademaster.workflow.entity.WfProcessInstance;

public interface FlowEngineService {

    WfProcessInstance startProcess(ProcessStartDTO startDTO);

    boolean approve(ProcessApproveDTO approveDTO);
}
