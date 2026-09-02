package com.psi.stock.service.impl;

import com.psi.stock.entity.StockMqProcessEntity;
import com.psi.stock.mapper.StockMqProcessMapper;
import com.psi.stock.service.StockMqProcessService;
import com.psi.common.result.CommonResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class StockMqProcessServiceImpl extends ServiceImpl<StockMqProcessMapper, StockMqProcessEntity> implements StockMqProcessService {

    @Override
    public CommonResult<Boolean> isProcessed(String businessCode) {
        LambdaQueryWrapper<StockMqProcessEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockMqProcessEntity::getBusinessCode, businessCode);
        
        StockMqProcessEntity entity = super.getOne(wrapper);
        return CommonResult.success(entity != null);
    }

    @Override
    public CommonResult<Void> markProcessed(String businessCode) {
        LambdaQueryWrapper<StockMqProcessEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockMqProcessEntity::getBusinessCode, businessCode);
        
        if (super.count(wrapper) == 0) {
            StockMqProcessEntity entity = new StockMqProcessEntity();
            entity.setBusinessCode(businessCode);
            super.save(entity);
        }
        
        return CommonResult.success();
    }
}