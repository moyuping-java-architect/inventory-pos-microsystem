package com.psi.message.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MsgDeadLetterTodoSaveDTO implements Serializable {

    private Long deadLetterId;

    private String messageId;

    private String handler;

    private Integer handleType;

    private String remark;
}