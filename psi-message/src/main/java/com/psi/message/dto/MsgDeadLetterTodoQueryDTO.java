package com.psi.message.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MsgDeadLetterTodoQueryDTO implements Serializable {

    private Long deadLetterId;

    private String messageId;

    private String handler;

    private Integer processStatus;

    private Integer handleType;

    private Integer pageNum;

    private Integer pageSize;
}