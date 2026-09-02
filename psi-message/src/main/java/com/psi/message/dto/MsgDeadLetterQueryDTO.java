package com.psi.message.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MsgDeadLetterQueryDTO implements Serializable {

    private String messageId;

    private String originalTopic;

    private String sender;

    private String receiver;

    private Integer pageNum;

    private Integer pageSize;
}