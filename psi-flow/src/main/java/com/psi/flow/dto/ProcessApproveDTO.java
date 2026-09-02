package com.psi.flow.dto;

import lombok.Data;

import java.util.Map;

/**
 * 审批参数DTO
 */
@Data
public class ProcessApproveDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 审批类型：1-同意 2-驳回 3-转交
     */
    private Integer approveType;

    /**
     * 处理人ID
     */
    private String handlerUserId;

    /**
     * 处理人姓名
     */
    private String handlerUserName;

    /**
     * 处理意见
     */
    private String handleNote;

    /**
     * 流程变量（审批时可能更新变量）
     */
    private Map<String, Object> variables;

    /**
     * 转交用户ID（仅approveType=3时有效）
     */
    private String transferUserId;

    /**
     * 转交用户姓名（仅approveType=3时有效）
     */
    private String transferUserName;
}