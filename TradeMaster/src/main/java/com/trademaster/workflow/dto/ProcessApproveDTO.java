package com.trademaster.workflow.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ProcessApproveDTO {

    private String taskId;

    private Integer approveType;

    private String handlerUserId;

    private String handlerUserName;

    private String handleNote;

    private Map<String, Object> variables;
}
