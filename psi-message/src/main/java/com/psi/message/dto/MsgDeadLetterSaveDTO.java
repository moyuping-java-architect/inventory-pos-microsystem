package com.psi.message.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MsgDeadLetterSaveDTO implements Serializable {

    private String messageId;

    private String originalTopic;

    private String content;

    private String sender;

    private String receiver;

    private String reason;

    private String errorMessage;

    private Integer failedCount;

    /**
     * 是否可重试（0-不可重试，1-可重试）
     */
    private Integer retryable;

    /**
     * 下次可重试时间
     */
    private java.time.LocalDateTime nextRetryTime;
}