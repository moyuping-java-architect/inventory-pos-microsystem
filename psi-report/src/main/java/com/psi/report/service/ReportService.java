package com.psi.report.service;

import com.psi.report.client.PythonReportClient;
import com.psi.report.client.ReportDataCollector;
import com.psi.report.dto.ReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 报表服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final PythonReportClient pythonReportClient;
    private final ReportDataCollector dataCollector;

    /**
     * 生成报表
     *
     * @param request       报表请求
     * @param contextHeaders 从上游请求透传过来的租户等头部
     * @return 报表文件字节数组
     */
    public byte[] generateReport(ReportRequest request, Map<String, String> contextHeaders) {
        String reportType = request.getReportType();
        String format = resolveFormat(request.getFormat());
        Map<String, Object> params = request.getParams() != null ? request.getParams() : new HashMap<>();

        List<Map<String, Object>> data;
        if (request.getData() != null && !request.getData().isEmpty()) {
            // 前端显式传入数据时优先使用（常用于测试或前端已聚合数据）
            data = request.getData();
        } else {
            data = switch (reportType) {
                case "inventory" -> dataCollector.fetchStockData(params, contextHeaders);
                case "purchase_order" -> dataCollector.fetchPurchaseOrderData(params, contextHeaders);
                case "sale_order" -> dataCollector.fetchSaleOrderData(params, contextHeaders);
                case "stock_flow" -> dataCollector.fetchStockFlowData(params, contextHeaders);
                case "psi_summary" -> buildPsiSummary(params, contextHeaders);
                default -> Collections.emptyList();
            };
        }

        Map<String, Object> enrichedParams = new HashMap<>(params);
        enrichedParams.put("reportType", reportType);
        enrichedParams.put("generatedAt", new Date());

        return pythonReportClient.generateReport(reportType, format, enrichedParams, data);
    }

    /**
     * 构建进销存汇总数据
     */
    private List<Map<String, Object>> buildPsiSummary(Map<String, Object> params, Map<String, String> contextHeaders) {
        List<Map<String, Object>> stockList = dataCollector.fetchStockData(params, contextHeaders);
        List<Map<String, Object>> purchaseList = dataCollector.fetchPurchaseOrderData(params, contextHeaders);
        List<Map<String, Object>> saleList = dataCollector.fetchSaleOrderData(params, contextHeaders);

        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();

        for (Map<String, Object> stock : stockList) {
            String key = Objects.toString(stock.get("goodsCode"), "") + "-" + Objects.toString(stock.get("skuCode"), "");
            Map<String, Object> row = grouped.computeIfAbsent(key, k -> new LinkedHashMap<>());
            row.put("goodsCode", stock.get("goodsCode"));
            row.put("skuCode", stock.get("skuCode"));
            row.put("goodsName", stock.get("goodsName"));
            row.put("warehouseName", stock.get("warehouseName"));
            row.put("unit", stock.get("unit"));
            row.put("currentQuantity", toBigDecimal(stock.get("quantity")));
            row.put("availableQuantity", toBigDecimal(stock.get("availableQuantity")));
            row.put("avgCostPrice", toBigDecimal(stock.get("avgCostPrice")));
            row.put("stockAmount", toBigDecimal(stock.get("totalAmount")));
        }

        Map<String, BigDecimal> purchaseAmountByGoods = new HashMap<>();
        for (Map<String, Object> po : purchaseList) {
            String goodsCode = Objects.toString(po.get("supplierCode"), "SUP");
            BigDecimal amount = toBigDecimal(po.get("totalAmount"));
            purchaseAmountByGoods.merge(goodsCode, amount, BigDecimal::add);
        }

        Map<String, BigDecimal> saleAmountByGoods = new HashMap<>();
        for (Map<String, Object> so : saleList) {
            String goodsCode = Objects.toString(so.get("customerCode"), "CUS");
            BigDecimal amount = toBigDecimal(so.get("totalAmount"));
            saleAmountByGoods.merge(goodsCode, amount, BigDecimal::add);
        }

        for (Map<String, Object> row : grouped.values()) {
            String goodsCode = Objects.toString(row.get("goodsCode"), "");
            row.put("purchaseAmount", purchaseAmountByGoods.getOrDefault(goodsCode, BigDecimal.ZERO));
            row.put("saleAmount", saleAmountByGoods.getOrDefault(goodsCode, BigDecimal.ZERO));
            BigDecimal current = toBigDecimal(row.get("currentQuantity"));
            BigDecimal beginning = current.subtract(BigDecimal.ZERO);
            row.put("beginningQuantity", beginning.compareTo(BigDecimal.ZERO) > 0 ? beginning : BigDecimal.ZERO);
            row.put("inQuantity", BigDecimal.ZERO);
            row.put("outQuantity", BigDecimal.ZERO);
        }

        if (grouped.isEmpty()) {
            Map<String, Object> emptyRow = new LinkedHashMap<>();
            emptyRow.put("message", "暂无进销存数据");
            return List.of(emptyRow);
        }

        return new ArrayList<>(grouped.values());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String resolveFormat(String format) {
        if (format == null || format.isEmpty()) {
            return "excel";
        }
        String lower = format.toLowerCase();
        if ("xlsx".equals(lower) || "xls".equals(lower)) {
            return "excel";
        }
        if ("pdf".equals(lower)) {
            return "pdf";
        }
        return lower;
    }
}
