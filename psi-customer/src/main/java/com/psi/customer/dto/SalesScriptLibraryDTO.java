package com.psi.customer.dto;

import lombok.Data;

import java.util.List;

/**
 * 销冠话术库 DTO（旅程触达话术）
 */
@Data
public class SalesScriptLibraryDTO {

    private Long id;
    private String tenantId;
    private String scriptCode;
    private String scriptName;
    private String category;
    private String content;
    private String channel;
    private String stageCode;
    private String stageName;

    /** 匹配标签编码集合 */
    private List<String> tags;

    /** 标签详情 */
    private List<CustomerTagDTO> tagList;

    /** 标签匹配模式：ANY/ALL */
    private String tagMatchMode;

    /** 阶段停留天数开始（含） */
    private Integer dayStart;

    /** 阶段停留天数结束（含） */
    private Integer dayEnd;

    /** 优先级 */
    private Integer priority;

    /** 最大发送次数 */
    private Integer sendLimit;

    /** 启用状态 0-禁用 1-启用 */
    private Integer isEnabled;
}
