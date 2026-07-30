package com.psi.message.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MsgDeadLetterDTO implements Serializable {

    private Long id;

    private String messageId;

    private String originalTopic;

    private String content;

    private String sender;

    private String receiver;

    private String reason;

    private String errorMessage;

    private Integer failedCount;

    private LocalDateTime lastFailedTime;

    /**
     * 是否可重试（0-不可重试，1-可重试）
     */
    private Integer retryable;

    private Long tenantId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}