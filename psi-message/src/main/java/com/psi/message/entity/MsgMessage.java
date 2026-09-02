package com.psi.message.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mq_message_record")
public class MsgMessage extends BaseEntity {

    private String messageId;

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
}