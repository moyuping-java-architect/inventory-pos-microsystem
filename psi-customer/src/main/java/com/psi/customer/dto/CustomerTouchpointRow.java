package com.psi.customer.dto;

/**
 * 客户触点行（用于客户旅程阶段匹配）
 * 聚合 customer_touchpoint 表：customerId + touchpointType + intent
 */
public record CustomerTouchpointRow(Long customerId, String touchpointType, String intent) {}