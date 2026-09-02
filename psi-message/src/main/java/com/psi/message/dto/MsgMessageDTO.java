package com.psi.message.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MsgMessageDTO implements Serializable {

    private Long id;

    private String messageId;

    private String tenantId;

    private String operatorId;

    private String sourceService;

    private String exchangeName;

    private String routingKey;

    private String eventType;

    private String messageBody;

    private String extParams;

    private Integer msgStatus;

    private Long sendTime;

    private Long consumeTime;

    private String errorMsg;

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer delFlag;

    private Integer status;
}