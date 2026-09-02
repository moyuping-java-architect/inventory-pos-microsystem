package com.psi.flow.dto;

import lombok.Data;

import java.util.Map;

/**
 * 流程启动参数DTO
 */
@Data
public class ProcessStartDTO {

    /**
     * 流程唯一标识
     */
    private String processKey;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID
     */
    private String bizId;

    /**
     * 流程标题
     */
    private String title;

    /**
     * 发起人ID
     */
    private String startUserId;

    /**
     * 发起人姓名
     */
    private String startUserName;

    /**
     * 流程变量（金额/天数等）
     */
    private Map<String, Object> variables;

    /**
     * 备注
     */
    private String remark;
}