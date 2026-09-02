package com.psi.customer.dto;

import lombok.Data;

/**
 * 业务事件 → 客户旅程阶段 的映射视图对象。
 * <p>
 * 老板在页面上把「某个业务事件」和「某个旅程阶段」连起来，
 * 后端据此生成一条触点规则（touchpoint_generate_rule），引擎就能把
 * 该事件翻译成客户触点并推进到对应阶段——全程零代码。
 *
 * @author PSI
 */
@Data
public class EventStageMappingDTO {

    /** 对应触点规则 id（删除时用） */
    private Long id;

    /** 业务事件编码，如 SALE.ORDER_APPROVED */
    private String eventCode;

    /** 事件中文名 */
    private String eventName;

    /** 事件英文名 */
    private String eventNameEn;

    /** 关联的阶段编码 */
    private String stageCode;

    /** 阶段中文名 */
    private String stageName;

    /** 阶段所属旅程编码 */
    private String journeyCode;

    /** 阶段所属旅程中文名 */
    private String journeyName;

    /** 该规则实际生成的触点类型（= 阶段的匹配触点） */
    private String matchTouchpoint;

    /** 是否启用 */
    private Integer enabled;

    /** 规则名称（展示用） */
    private String ruleName;
}
