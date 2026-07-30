package com.psi.purchase.service.impl;

import com.psi.purchase.dto.SupplierDTO;
import com.psi.purchase.dto.SupplierQueryDTO;
import com.psi.purchase.dto.SupplierSaveDTO;
import com.psi.purchase.entity.SupplierEntity;
import com.psi.purchase.mapper.SupplierMapper;
import com.psi.purchase.mq.producer.PurchaseSyncProducer;
import com.psi.purchase.service.SupplierService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, SupplierEntity> implements SupplierService {

    private static final String CACHE_NAME = "supplier";

    private final PurchaseSyncProducer purchaseSyncProducer;

    public SupplierServiceImpl(PurchaseSyncProducer purchaseSyncProducer) {
        this.purchaseSyncProducer = purchaseSyncProducer;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#id", unless = "#result == null or #result.code != 200")
    public CommonResult<SupplierDTO> getById(Long id) {
        SupplierEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, SupplierDTO.class));
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":list", key = "#queryDTO.hashCode()", unless = "#result == null")
    public PageResult<SupplierDTO> list(SupplierQueryDTO queryDTO) {
        Page<SupplierEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SupplierEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getSupplierCode() != null) {
            wrapper.like(SupplierEntity::getSupplierCode, queryDTO.getSupplierCode());
        }
        if (queryDTO.getSupplierName() != null) {
            wrapper.like(SupplierEntity::getSupplierName, queryDTO.getSupplierName());
        }
        if (queryDTO.getShortName() != null) {
            wrapper.like(SupplierEntity::getShortName, queryDTO.getShortName());
        }
        if (queryDTO.getContactName() != null) {
            wrapper.like(SupplierEntity::getContactName, queryDTO.getContactName());
        }
        if (queryDTO.getSupplierType() != null) {
            wrapper.eq(SupplierEntity::getSupplierType, queryDTO.getSupplierType());
        }
        if (queryDTO.getIndustry() != null) {
            wrapper.eq(SupplierEntity::getIndustry, queryDTO.getIndustry());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SupplierEntity::getStatus, queryDTO.getStatus());
        }
        
        IPage<SupplierEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, SupplierDTO.class));
    }

    @Override
    @CacheEvict(value = {CACHE_NAME, CACHE_NAME + ":list"}, allEntries = true)
    public CommonResult<SupplierDTO> save(SupplierSaveDTO saveDTO) {
        SupplierEntity entity = BeanUtils.convert(saveDTO, SupplierEntity.class);
        super.save(entity);
        sendSupplier(entity);
        return CommonResult.success(BeanUtils.convert(entity, SupplierDTO.class));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME + ":list", allEntries = true)
    })
    public CommonResult<SupplierDTO> update(Long id, SupplierSaveDTO saveDTO) {
        SupplierEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        BeanUtils.copyProperties(saveDTO, entity);
        super.updateById(entity);
        sendSupplier(entity);
        return CommonResult.success(BeanUtils.convert(entity, SupplierDTO.class));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME + ":list", allEntries = true)
    })
    public CommonResult<Void> delete(Long id) {
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME + ":list", allEntries = true)
    })
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SupplierEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        sendSupplier(entity);
        return CommonResult.success();
    }

    private void sendSupplier(SupplierEntity entity) {
        try {
            purchaseSyncProducer.sendSupplier(entity);
        } catch (Exception e) {
            log.error("供应商实时同步消息发送失败", e);
        }
    }
}