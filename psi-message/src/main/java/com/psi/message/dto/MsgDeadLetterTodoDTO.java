package com.psi.message.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MsgDeadLetterTodoDTO implements Serializable {

    private Long id;

    private Long deadLetterId;

    private String messageId;

    private String handler;

    private Integer processStatus;

    private Integer handleType;

    private String remark;

    private LocalDateTime handleTime;

    private Long tenantId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}