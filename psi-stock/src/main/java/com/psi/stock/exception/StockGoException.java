package com.psi.stock.exception;

/**
 * Go 库存服务调用异常
 * 用于区分网络/连接异常与业务错误，配合 Resilience4j 熔断
 */
public class StockGoException extends RuntimeException {

    public StockGoException(String message) {
        super(message);
    }

    public StockGoException(String message, Throwable cause) {
        super(message, cause);
    }
}
