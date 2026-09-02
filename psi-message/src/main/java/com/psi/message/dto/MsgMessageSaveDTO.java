package com.psi.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class MsgMessageSaveDTO implements Serializable {

    @NotBlank(message = "消息ID不能为空")
    private String messageId;

    private String tenantId;

    private String operatorId;

    @NotBlank(message = "生产者微服务名不能为空")
    private String sourceService;

    @NotBlank(message = "交换机名称不能为空")
    private String exchangeName;

    @NotBlank(message = "路由键不能为空")
    private String routingKey;

    @NotBlank(message = "业务事件类型不能为空")
    private String eventType;

    @NotBlank(message = "消息体不能为空")
    private String messageBody;

    private String extParams;

    private Integer msgStatus;

    private Long sendTime;

    private Long consumeTime;

    private String errorMsg;
}