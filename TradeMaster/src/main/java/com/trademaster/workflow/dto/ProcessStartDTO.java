package com.trademaster.workflow.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ProcessStartDTO {

    private String processKey;

    private String title;

    private String startUserId;

    private String startUserName;

    private String bizType;

    private String bizId;

    private Map<String, Object> variables;
}
