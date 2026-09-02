package com.psi.customer.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建「业务事件 → 旅程阶段」关联的请求体。
 *
 * @author PSI
 */
@Data
public class EventStageLinkReq implements Serializable {

    /** 业务事件编码（取自 business_event_dict） */
    private String eventCode;

    /** 阶段所属旅程编码（仅用于前端联动，后端以 stageCode 解析） */
    private String journeyCode;

    /** 目标阶段编码（取自 customer_journey_stage） */
    private String stageCode;
}
