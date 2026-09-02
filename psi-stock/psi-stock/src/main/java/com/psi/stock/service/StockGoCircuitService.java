package com.psi.stock.service;

import com.psi.common.result.CommonResult;
import com.psi.common.result.ResultCode;
import com.psi.stock.client.StockGoClient;
import com.psi.stock.dto.StockBatchOperateItemDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Go 库存服务熔断器包装层
 * 当 Go 服务不可用或响应超时时，触发熔断并快速失败，由上层决定是否降级到本地 Java 实现
 */
@Slf4j
@Service
public class StockGoCircuitService {

    private static final String CIRCUIT_NAME = "stockGo";
    private static final String FALLBACK_MSG = "CIRCUIT_OPEN";

    private final StockGoClient stockGoClient;

    public StockGoCircuitService(StockGoClient stockGoClient) {
        this.stockGoClient = stockGoClient;
    }

    @CircuitBreaker(name = CIRCUIT_NAME, fallbackMethod = "increaseFallback")
    public CommonResult<Void> increaseStock(String warehouseCode, String goodsCode, String skuCode,
                                            BigDecimal quantity, BigDecimal costPrice,
                                            String sourceNo, String sourceType) {
        return stockGoClient.increaseStock(warehouseCode, goodsCode, skuCode, quantity, costPrice, sourceNo, sourceType);
    }

    public CommonResult<Void> increaseFallback(String warehouseCode, String goodsCode, String skuCode,
                                               BigDecimal quantity, BigDecimal costPrice,
                                               String sourceNo, String sourceType, Throwable t) {
        log.warn("Go库存新增熔断降级: warehouseCode={}, skuCode={}, error={}", warehouseCode, skuCode, t.getMessage());
        return CommonResult.fail(ResultCode.FAIL.getCode(), FALLBACK_MSG);
    }

    @CircuitBreaker(name = CIRCUIT_NAME, fallbackMethod = "decreaseFallback")
    public CommonResult<Void> decreaseStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                            String sourceNo, String sourceType) {
        return stockGoClient.decreaseStock(warehouseCode, skuCode, quantity, sourceNo, sourceType);
    }

    public CommonResult<Void> decreaseFallback(String warehouseCode, String skuCode, BigDecimal quantity,
                                               String sourceNo, String sourceType, Throwable t) {
        log.warn("Go库存扣减熔断降级: warehouseCode={}, skuCode={}, error={}", warehouseCode, skuCode, t.getMessage());
        return CommonResult.fail(ResultCode.FAIL.getCode(), FALLBACK_MSG);
    }

    @CircuitBreaker(name = CIRCUIT_NAME, fallbackMethod = "lockFallback")
    public CommonResult<Void> lockStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                        String sourceNo, String sourceType) {
        return stockGoClient.lockStock(warehouseCode, skuCode, quantity, sourceNo, sourceType);
    }

    public CommonResult<Void> lockFallback(String warehouseCode, String skuCode, BigDecimal quantity,
                                           String sourceNo, String sourceType, Throwable t) {
        log.warn("Go库存预占熔断降级: warehouseCode={}, skuCode={}, error={}", warehouseCode, skuCode, t.getMessage());
        return CommonResult.fail(ResultCode.FAIL.getCode(), FALLBACK_MSG);
    }

    @CircuitBreaker(name = CIRCUIT_NAME, fallbackMethod = "releaseFallback")
    public CommonResult<Void> releaseStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                           String sourceNo, String sourceType) {
        return stockGoClient.releaseStock(warehouseCode, skuCode, quantity, sourceNo, sourceType);
    }

    public CommonResult<Void> releaseFallback(String warehouseCode, String skuCode, BigDecimal quantity,
                                              String sourceNo, String sourceType, Throwable t) {
        log.warn("Go库存释放熔断降级: warehouseCode={}, skuCode={}, error={}", warehouseCode, skuCode, t.getMessage());
        return CommonResult.fail(ResultCode.FAIL.getCode(), FALLBACK_MSG);
    }

    @CircuitBreaker(name = CIRCUIT_NAME, fallbackMethod = "confirmFallback")
    public CommonResult<Void> confirmStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                           String sourceNo, String sourceType) {
        return stockGoClient.confirmStock(warehouseCode, skuCode, quantity, sourceNo, sourceType);
    }

    public CommonResult<Void> confirmFallback(String warehouseCode, String skuCode, BigDecimal quantity,
                                              String sourceNo, String sourceType, Throwable t) {
        log.warn("Go库存确认出库熔断降级: warehouseCode={}, skuCode={}, error={}", warehouseCode, skuCode, t.getMessage());
        return CommonResult.fail(ResultCode.FAIL.getCode(), FALLBACK_MSG);
    }

    @CircuitBreaker(name = CIRCUIT_NAME, fallbackMethod = "batchDecreaseFallback")
    public CommonResult<Void> batchDecreaseStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType) {
        return stockGoClient.batchDecreaseStock(items, sourceNo, sourceType);
    }

    public CommonResult<Void> batchDecreaseFallback(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType, Throwable t) {
        log.warn("Go批量库存扣减熔断降级: items={}, error={}", items.size(), t.getMessage());
        return CommonResult.fail(ResultCode.FAIL.getCode(), FALLBACK_MSG);
    }

    /**
     * 判断结果是否为熔断降级标记
     */
    public static boolean isCircuitOpen(CommonResult<?> result) {
        return result != null && FALLBACK_MSG.equals(result.getMessage());
    }
}
