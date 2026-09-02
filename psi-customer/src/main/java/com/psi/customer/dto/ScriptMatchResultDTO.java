package com.psi.customer.dto;

import lombok.Data;

import java.util.List;

/**
 * 话术触达结果 DTO
 * 不再使用综合得分，而是判断：节点 + 天数 + 标签 + 未发送过
 */
@Data
public class ScriptMatchResultDTO {

    /** 话术ID */
    private Long scriptId;

    /** 话术编码 */
    private String scriptCode;

    /** 话术名称 */
    private String scriptName;

    /** 分类 */
    private String category;

    /** 渲染后的话术内容（占位符已替换） */
    private String renderedContent;

    /** 原始内容 */
    private String content;

    /** 渠道 */
    private String channel;

    /** 适用节点 */
    private String stageCode;

    /** 适用节点名称 */
    private String stageName;

    /** 适用天数区间 */
    private Integer dayStart;
    private Integer dayEnd;

    /** 是否已发送过 */
    private Boolean alreadySent;

    /** 是否可发送（节点/天数/标签都满足且未发送过） */
    private Boolean canSend;

    /** 不可发送原因 */
    private String blockReason;

    /** 命中的标签 */
    private List<CustomerTagDTO> matchedTags;

    /** 优先级 */
    private Integer priority;
}
