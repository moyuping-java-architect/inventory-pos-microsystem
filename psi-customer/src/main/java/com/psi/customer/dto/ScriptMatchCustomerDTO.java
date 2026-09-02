package com.psi.customer.dto;

import lombok.Data;

import java.util.List;

/**
 * 话术库页面客户卡片 DTO
 */
@Data
public class ScriptMatchCustomerDTO {

    /** 客户ID */
    private Long customerId;

    /** 客户名 */
    private String customerName;

    /** 头像缩写 */
    private String avatar;

    /** 渠道：WHATSAPP / PHONE 等 */
    private String channel;

    /** 联系方式 */
    private String contact;

    /** 当前阶段编码 */
    private String stageCode;

    /** 当前阶段名称 */
    private String stageName;

    /** 阶段停留天数 */
    private Integer daysInStage;

    /** 客户标签 */
    private List<CustomerTagDTO> tags;

    /** 最近一次互动时间描述 */
    private String lastInteractText;

    /** 当前应发送的话术（按节点+天数+标签+未发送） */
    private List<ScriptMatchResultDTO> todayScripts;

    /** 今日是否还有待发送话术 */
    private Boolean hasPending;
}
