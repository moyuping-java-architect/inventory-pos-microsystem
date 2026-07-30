package com.psi.report.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 报表生成请求
 */
@Data
public class ReportRequest {

    /**
     * 报表类型，如 inventory、purchase、sale
     */
    private String reportType;

    /**
     * 输出格式：excel 或 pdf
     */
    private String format;

    /**
     * 查询参数
     */
    private Map<String, Object> params;

    /**
     * 报表数据（可选，由 Java 端从各业务服务聚合后传入）
     */
    private List<Map<String, Object>> data;
}
