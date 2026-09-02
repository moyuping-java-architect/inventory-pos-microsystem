package com.psi.stock.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.result.CommonResult;
import com.psi.stock.config.StockGoProperties;
import com.psi.stock.dto.StockBatchOperateItemDTO;
import com.psi.stock.exception.StockGoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Go 库存服务客户端
 * 用于在 Nacos 开关开启时调用 Go 实现的库存扣减/新增服务
 */
@Slf4j
@Component
public class StockGoClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StockGoProperties properties;

    public StockGoClient(StockGoProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    private String baseUrl() {
        return properties.getUrl();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private CommonResult<Void> post(String path, Object body) {
        String url = baseUrl() + path;
        try {
            HttpEntity<Object> entity = new HttpEntity<>(body, buildHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            int code = node.has("code") ? node.get("code").asInt(500) : 500;
            String message = node.has("message") ? node.get("message").asText("unknown") : "unknown";
            boolean success = node.has("success") ? node.get("success").asBoolean(false) : false;
            if (code == 200 && success) {
                return CommonResult.success(null);
            }
            log.warn("调用 Go 库存服务失败: url={}, code={}, message={}", url, code, message);
            return CommonResult.fail(code, message);
        } catch (ResourceAccessException e) {
            log.error("调用 Go 库存服务网络异常: url={}", url, e);
            throw new StockGoException("Go 库存服务不可用: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("调用 Go 库存服务异常: url={}", url, e);
            throw new StockGoException("Go 库存服务调用异常: " + e.getMessage(), e);
        }
    }

    public CommonResult<Void> increaseStock(String warehouseCode, String goodsCode, String skuCode,
                                            BigDecimal quantity, BigDecimal costPrice,
                                            String sourceNo, String sourceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseCode", warehouseCode);
        params.put("goodsCode", goodsCode);
        params.put("skuCode", skuCode);
        params.put("quantity", quantity);
        params.put("costPrice", costPrice);
        params.put("sourceNo", sourceNo);
        params.put("sourceType", sourceType);
        return post("/psi/stock/go/increase", params);
    }

    public CommonResult<Void> decreaseStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                            String sourceNo, String sourceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseCode", warehouseCode);
        params.put("skuCode", skuCode);
        params.put("quantity", quantity);
        params.put("sourceNo", sourceNo);
        params.put("sourceType", sourceType);
        return post("/psi/stock/go/decrease", params);
    }

    public CommonResult<Void> lockStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                        String sourceNo, String sourceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseCode", warehouseCode);
        params.put("skuCode", skuCode);
        params.put("quantity", quantity);
        params.put("sourceNo", sourceNo);
        params.put("sourceType", sourceType);
        return post("/psi/stock/go/lock", params);
    }

    public CommonResult<Void> releaseStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                           String sourceNo, String sourceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseCode", warehouseCode);
        params.put("skuCode", skuCode);
        params.put("quantity", quantity);
        params.put("sourceNo", sourceNo);
        params.put("sourceType", sourceType);
        return post("/psi/stock/go/release", params);
    }

    public CommonResult<Void> confirmStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                           String sourceNo, String sourceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseCode", warehouseCode);
        params.put("skuCode", skuCode);
        params.put("quantity", quantity);
        params.put("sourceNo", sourceNo);
        params.put("sourceType", sourceType);
        return post("/psi/stock/go/confirm", params);
    }

    public CommonResult<Void> batchDecreaseStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("items", items);
        params.put("sourceNo", sourceNo);
        params.put("sourceType", sourceType);
        return post("/psi/stock/go/batch/decrease", params);
    }
}
