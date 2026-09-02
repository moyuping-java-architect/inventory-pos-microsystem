package com.psi.customer.dto;

import lombok.Data;

/**
 * 发送话术请求
 */
@Data
public class ScriptSendRequest {

    /** 客户ID */
    private Long customerId;

    /** 话术ID */
    private Long scriptId;

    /** 阶段停留天数（不传则取客户当前实际天数） */
    private Integer dayInStage;
}
