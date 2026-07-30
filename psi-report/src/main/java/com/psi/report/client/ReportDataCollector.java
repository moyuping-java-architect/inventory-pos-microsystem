package com.psi.report.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 从各业务微服务聚合报表数据
 */
@Slf4j
@Component
public class ReportDataCollector {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${psi.report.service.stock-url}")
    private String stockUrl;

    @Value("${psi.report.service.purchase-url}")
    private String purchaseUrl;

    @Value("${psi.report.service.sale-url}")
    private String saleUrl;

    @Value("${psi.report.service.goods-url}")
    private String goodsUrl;

    public ReportDataCollector(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询库存数据
     */
    public List<Map<String, Object>> fetchStockData(Map<String, Object> params, Map<String, String> contextHeaders) {
        String url = stockUrl + "/psi/stock/list";
        Map<String, Object> query = buildPageQuery(params, 1, 500);
        return postForList(url, query, "stock", contextHeaders);
    }

    /**
     * 查询库存流水
     */
    public List<Map<String, Object>> fetchStockFlowData(Map<String, Object> params, Map<String, String> contextHeaders) {
        String url = stockUrl + "/psi/stock/flow/list";
        Map<String, Object> query = buildPageQuery(params, 1, 500);
        return postForList(url, query, "stock-flow", contextHeaders);
    }

    /**
     * 查询采购订单数据
     */
    public List<Map<String, Object>> fetchPurchaseOrderData(Map<String, Object> params, Map<String, String> contextHeaders) {
        String url = purchaseUrl + "/psi/purchase/order/list";
        Map<String, Object> query = buildPageQuery(params, 1, 500);
        return getForList(url, query, "purchase-order", contextHeaders);
    }

    /**
     * 查询销售订单数据
     */
    public List<Map<String, Object>> fetchSaleOrderData(Map<String, Object> params, Map<String, String> contextHeaders) {
        String url = saleUrl + "/psi/sale/order/list";
        Map<String, Object> query = buildPageQuery(params, 1, 500);
        return postForList(url, query, "sale-order", contextHeaders);
    }

    private Map<String, Object> buildPageQuery(Map<String, Object> params, int pageNum, int pageSize) {
        Map<String, Object> query = new HashMap<>();
        if (params != null) {
            query.putAll(params);
        }
        query.put("pageNum", pageNum);
        query.put("pageSize", pageSize);
        return query;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> postForList(String url, Map<String, Object> query, String dataType, Map<String, String> contextHeaders) {
        HttpHeaders headers = buildHeaders(contextHeaders);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(query, headers);

        log.info("Fetch {} data from: {}", dataType, url);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            List<Map<String, Object>> rows = extractRows(response.getBody());
            log.info("Fetched {} {} rows", rows.size(), dataType);
            return rows;
        } catch (Exception e) {
            log.error("Failed to fetch {} data from: {}", dataType, url, e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getForList(String url, Map<String, Object> query, String dataType, Map<String, String> contextHeaders) {
        StringBuilder fullUrl = new StringBuilder(url).append("?");
        if (query != null) {
            query.forEach((k, v) -> {
                if (v != null) {
                    fullUrl.append(k).append("=").append(v).append("&");
                }
            });
        }

        HttpHeaders headers = buildHeaders(contextHeaders);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("Fetch {} data from: {}", dataType, fullUrl);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    fullUrl.toString(),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            List<Map<String, Object>> rows = extractRows(response.getBody());
            log.info("Fetched {} {} rows", rows.size(), dataType);
            return rows;
        } catch (Exception e) {
            log.error("Failed to fetch {} data from: {}", dataType, fullUrl, e);
            return Collections.emptyList();
        }
    }

    private HttpHeaders buildHeaders(Map<String, String> contextHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (contextHeaders != null) {
            contextHeaders.forEach(headers::set);
        }
        return headers;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRows(Map<String, Object> body) {
        if (body == null) {
            return Collections.emptyList();
        }

        Object data = body.get("data");
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }

        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object records = dataMap.get("records");
            if (records instanceof List) {
                return (List<Map<String, Object>>) records;
            }
            Object list = dataMap.get("list");
            if (list instanceof List) {
                return (List<Map<String, Object>>) list;
            }
        }

        // 部分服务直接返回 PageResult（如采购订单），其列表字段在顶层
        Object topList = body.get("list");
        if (topList instanceof List) {
            return (List<Map<String, Object>>) topList;
        }
        Object topRecords = body.get("records");
        if (topRecords instanceof List) {
            return (List<Map<String, Object>>) topRecords;
        }

        return Collections.emptyList();
    }
}
