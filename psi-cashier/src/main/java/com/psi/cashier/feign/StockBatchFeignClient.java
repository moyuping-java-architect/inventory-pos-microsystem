package com.psi.cashier.feign;

import com.psi.cashier.dto.CashierBatchDTO;
import com.psi.common.result.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 库存批次 Feign 客户端
 */
@FeignClient(name = "psi-stock", url = "${psi.stock.base-url:}")
public interface StockBatchFeignClient {

    /**
     * 根据商品编码查询有效批次
     */
    @GetMapping("/psi/stock/batch/goods/{goodsCode}")
    CommonResult<List<CashierBatchDTO>> listValidByGoodsCode(@PathVariable("goodsCode") String goodsCode);
}
