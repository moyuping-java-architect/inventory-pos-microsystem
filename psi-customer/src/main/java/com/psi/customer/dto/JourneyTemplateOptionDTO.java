package com.psi.customer.dto;

import lombok.Data;

/**
 * Card/list row payload returned by GET /psi/customer/journey/config/journey-templates.
 * Used by the "客户旅程阶段" tab to render the template list with full CRUD controls
 * (新增 / 编辑 / 删除 / 启用).
 */
@Data
public class JourneyTemplateOptionDTO {

    /** DB primary key, used by PUT/DELETE endpoints. */
    private Long id;

    /** 旅程编码，作为业务标识 (e.g. SALES_FUNNEL). */
    private String journeyCode;

    /** 旅程名称，列表显示. */
    private String journeyName;

    /** 主体类型：CUSTOMER / MEMBER. */
    private String subjectType;

    /** 描述，列表副标题. */
    private String description;

    /** 图标 (Element Plus icon name). */
    private String icon;

    /** 是否启用 (1 / 0). */
    private Integer enabled;

    /** 排序. */
    private Integer sortOrder;

    /** 阶段数量（附加计算字段，由 listJourneyTemplates 时通过 countStagesByTemplate 注入. */
    private Long stageCount;
}
