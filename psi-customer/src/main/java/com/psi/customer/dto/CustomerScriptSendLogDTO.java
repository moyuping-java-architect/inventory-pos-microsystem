package com.psi.customer.dto;

import lombok.Data;

/**
 * 话术发送记录 DTO
 */
@Data
public class CustomerScriptSendLogDTO {

    private Long id;
    private String tenantId;
    private Long customerId;
    private Long scriptId;
    private String scriptName;
    private String stageCode;
    private String stageName;
    private Integer dayInStage;
    private String channel;
    private Integer sendStatus;
    private String sendTime;
    private String operatorName;
    private String remark;
}
