package com.trademaster.workflow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoDTO {

    private String id;

    private String processInstanceId;

    private Long nodeId;

    private String taskName;

    private String handlerUserId;

    private String handlerUserName;

    private Integer status;

    private String handleNote;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private String bizType;

    private String docType;

    private String docNo;

    private String docName;

    private String creatorId;

    private String creatorName;
}
