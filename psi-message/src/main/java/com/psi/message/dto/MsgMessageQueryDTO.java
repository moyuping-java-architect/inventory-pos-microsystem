package com.psi.message.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MsgMessageQueryDTO implements Serializable {

    private String messageId;

    private String tenantId;

    private String operatorId;

    private String sourceService;

    private String exchangeName;

    private String routingKey;

    private String eventType;

    private Integer msgStatus;

    private Integer pageNum;

    private Integer pageSize;
}