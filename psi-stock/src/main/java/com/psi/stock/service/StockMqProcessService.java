package com.psi.stock.service;

import com.psi.stock.entity.StockMqProcessEntity;
import com.psi.common.result.CommonResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface StockMqProcessService extends IService<StockMqProcessEntity> {

    CommonResult<Boolean> isProcessed(String businessCode);

    CommonResult<Void> markProcessed(String businessCode);
}