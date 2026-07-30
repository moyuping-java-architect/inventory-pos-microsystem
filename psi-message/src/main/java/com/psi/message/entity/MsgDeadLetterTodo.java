package com.psi.message.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("msg_dead_letter_todo")
public class MsgDeadLetterTodo extends BaseEntity {

    private Long deadLetterId;

    private String messageId;

    private String handler;

    private Integer processStatus;

    private Integer handleType;

    private String remark;

    private LocalDateTime handleTime;
}