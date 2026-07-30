package com.psi.flow.service;

import com.psi.flow.dto.ProcessStartDTO;
import com.psi.flow.dto.ProcessApproveDTO;
import com.psi.flow.entity.WfProcessInstance;

/**
 * 流程引擎核心服务接口
 * 提供流程发起和审批流转的核心能力
 */
public interface FlowEngineService {

    /**
     * 发起流程
     * 功能：创建流程实例、绑定业务ID、生成待办任务、记录日志
     *
     * @param startDTO 流程启动参数
     * @return 流程实例
     */
    WfProcessInstance startProcess(ProcessStartDTO startDTO);

    /**
     * 审批流转
     * 功能：更新任务状态、根据当前节点+流程变量匹配连线条件、跳转下一节点、生成新待办
     *
     * @param approveDTO 审批参数
     * @return 是否成功
     */
    boolean approve(ProcessApproveDTO approveDTO);
}