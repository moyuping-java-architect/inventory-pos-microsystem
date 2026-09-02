package com.psi.message.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("msg_dead_letter")
public class MsgDeadLetter extends BaseEntity {

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
     * 根据死信原因自动判断：
     * - 消息过期(TTL)：可重试
     * - 消息被拒绝：需根据具体原因判断
     * - 队列达到最大长度：可重试
     */
    private Integer retryable;

    /**
     * 下次可重试时间（用于指数退避）
     */
    private LocalDateTime nextRetryTime;
}