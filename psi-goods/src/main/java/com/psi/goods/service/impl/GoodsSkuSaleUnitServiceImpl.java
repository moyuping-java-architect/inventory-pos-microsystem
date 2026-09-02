package com.psi.goods.service.impl;

import com.psi.goods.dto.CashierSaleUnitDTO;
import com.psi.goods.dto.CashierGoodsQueryDTO;
import com.psi.goods.dto.GoodsSkuSaleUnitQueryDTO;
import com.psi.goods.entity.GoodsSkuSaleUnit;
import com.psi.goods.mapper.GoodsSkuSaleUnitMapper;
import com.psi.goods.mq.producer.GoodsDownSyncProducer;
import com.psi.goods.service.GoodsSkuSaleUnitService;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.common.mybatis.util.BatchUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * SKU销售单位服务实现
 * 支持非洲场景下多种销售单位销售模式
 */
@Service
public class GoodsSkuSaleUnitServiceImpl extends ServiceImpl<GoodsSkuSaleUnitMapper, GoodsSkuSaleUnit> implements GoodsSkuSaleUnitService {

    private static final String CACHE_NAME = "saleUnit";
    private static final String CACHE_NAME_SKU = CACHE_NAME + ":sku";
    private static final String CACHE_NAME_ENABLED = CACHE_NAME + ":enabled";
    private static final String CACHE_NAME_DEFAULT = CACHE_NAME + ":default";
    private static final String CACHE_NAME_UNIFY = CACHE_NAME + ":unify";
    private static final String CACHE_NAME_MAX_PRICE = CACHE_NAME + ":maxPrice";
    private static final String CACHE_NAME_CASHIER = CACHE_NAME + ":cashier";
    private static final String CACHE_NAME_BARCODE = CACHE_NAME + ":barcode";
    private static final String CACHE_NAME_PAGE = CACHE_NAME + ":page";

    private final BatchUtils batchUtils;
    private final GoodsDownSyncProducer goodsDownSyncProducer;

    public GoodsSkuSaleUnitServiceImpl(BatchUtils batchUtils, GoodsDownSyncProducer goodsDownSyncProducer) {
        this.batchUtils = batchUtils;
        this.goodsDownSyncProducer = goodsDownSyncProducer;
    }

    @Override
    @CacheEvict(value = {CACHE_NAME, CACHE_NAME_SKU, CACHE_NAME_ENABLED, CACHE_NAME_DEFAULT, CACHE_NAME_UNIFY, CACHE_NAME_MAX_PRICE, CACHE_NAME_CASHIER, CACHE_NAME_BARCODE, CACHE_NAME_PAGE}, allEntries = true)
    public boolean save(GoodsSkuSaleUnit entity) {
        boolean result = super.save(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsSkuSaleUnit(entity);
        }
        return result;
    }

    @Override
    @CacheEvict(value = {CACHE_NAME, CACHE_NAME_SKU, CACHE_NAME_ENABLED, CACHE_NAME_DEFAULT, CACHE_NAME_UNIFY, CACHE_NAME_MAX_PRICE, CACHE_NAME_CASHIER, CACHE_NAME_BARCODE, CACHE_NAME_PAGE}, allEntries = true)
    public boolean updateById(GoodsSkuSaleUnit entity) {
        boolean result = super.updateById(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsSkuSaleUnit(entity);
        }
        return result;
    }

    @Override
    @CacheEvict(value = {CACHE_NAME, CACHE_NAME_SKU, CACHE_NAME_ENABLED, CACHE_NAME_DEFAULT, CACHE_NAME_UNIFY, CACHE_NAME_MAX_PRICE, CACHE_NAME_CASHIER, CACHE_NAME_BARCODE, CACHE_NAME_PAGE}, allEntries = true)
    public boolean removeById(Serializable id) {
        GoodsSkuSaleUnit entity = getById(id);
        if (entity != null) {
            entity.setDelFlag(1);
            goodsDownSyncProducer.sendGoodsSkuSaleUnit(entity);
        }
        return super.removeById(id);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":sku", key = "#skuId", unless = "#result == null or #result.isEmpty()")
    public List<GoodsSkuSaleUnit> getBySkuId(Long skuId) {
        LambdaQueryWrapper<GoodsSkuSaleUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsSkuSaleUnit::getSkuId, skuId)
                .eq(GoodsSkuSaleUnit::getDelFlag, 0)
                .orderByAsc(GoodsSkuSaleUnit::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":enabled", key = "#skuId", unless = "#result == null or #result.isEmpty()")
    public List<GoodsSkuSaleUnit> getEnabledBySkuId(Long skuId) {
        LambdaQueryWrapper<GoodsSkuSaleUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsSkuSaleUnit::getSkuId, skuId)
                .eq(GoodsSkuSaleUnit::getStatus, 1)
                .eq(GoodsSkuSaleUnit::getDelFlag, 0)
                .orderByAsc(GoodsSkuSaleUnit::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":default", key = "#skuId")
    public GoodsSkuSaleUnit getDefaultBySkuId(Long skuId) {
        LambdaQueryWrapper<GoodsSkuSaleUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsSkuSaleUnit::getSkuId, skuId)
                .eq(GoodsSkuSaleUnit::getIsDefault, 1)
                .eq(GoodsSkuSaleUnit::getStatus, 1)
                .eq(GoodsSkuSaleUnit::getDelFlag, 0);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":unify", key = "#goodsUnifyCode", unless = "#result == null or #result.isEmpty()")
    public List<GoodsSkuSaleUnit> getByUnifyCode(String goodsUnifyCode) {
        return baseMapper.selectByUnifyCode(goodsUnifyCode);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":maxPrice", key = "#goodsUnifyCode", unless = "#result == null or #result.isEmpty()")
    public List<GoodsSkuSaleUnit> getMaxPriceByUnifyCode(String goodsUnifyCode) {
        return baseMapper.selectMaxPriceByUnifyCode(goodsUnifyCode);
    }

    @Override
    public PageResult<CashierSaleUnitDTO> queryForCashier(CashierGoodsQueryDTO queryDTO) {
        Integer offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        Integer limit = queryDTO.getPageSize();

        List<CashierSaleUnitDTO> list = baseMapper.selectForCashier(
                queryDTO.getGoodsUnifyCode(),
                queryDTO.getGoodsName(),
                queryDTO.getCategoryId(),
                queryDTO.getBrandId(),
                offset,
                limit
        );

        Long total = baseMapper.countForCashier(
                queryDTO.getGoodsUnifyCode(),
                queryDTO.getGoodsName(),
                queryDTO.getCategoryId(),
                queryDTO.getBrandId()
        );

        return PageResult.success(list, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":cashier", key = "#goodsUnifyCode", unless = "#result == null or #result.isEmpty()")
    public List<CashierSaleUnitDTO> getCashierSaleUnits(String goodsUnifyCode) {
        return baseMapper.selectForCashier(goodsUnifyCode, null, null, null, 0, Integer.MAX_VALUE);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":barcode", key = "#barcode", unless = "#result == null or #result.isEmpty()")
    public List<CashierSaleUnitDTO> getByBarcode(String barcode) {
        return baseMapper.selectByBarcode(barcode);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":page", key = "#queryDTO.hashCode()", unless = "#result == null")
    public PageResult<GoodsSkuSaleUnit> queryPage(GoodsSkuSaleUnitQueryDTO queryDTO) {
        LambdaQueryWrapper<GoodsSkuSaleUnit> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getSkuId() != null) {
            wrapper.eq(GoodsSkuSaleUnit::getSkuId, queryDTO.getSkuId());
        }
        
        if (StringUtils.hasText(queryDTO.getGoodsUnifyCode())) {
            wrapper.eq(GoodsSkuSaleUnit::getGoodsUnifyCode, queryDTO.getGoodsUnifyCode());
        }
        
        if (queryDTO.getSaleUnitId() != null) {
            wrapper.eq(GoodsSkuSaleUnit::getSaleUnitId, queryDTO.getSaleUnitId());
        }
        
        if (StringUtils.hasText(queryDTO.getSaleUnitName())) {
            wrapper.like(GoodsSkuSaleUnit::getSaleUnitName, queryDTO.getSaleUnitName());
        }
        
        if (StringUtils.hasText(queryDTO.getSaleUnitSymbol())) {
            wrapper.like(GoodsSkuSaleUnit::getSaleUnitSymbol, queryDTO.getSaleUnitSymbol());
        }
        
        if (queryDTO.getIsDefault() != null) {
            wrapper.eq(GoodsSkuSaleUnit::getIsDefault, queryDTO.getIsDefault());
        }
        
        if (queryDTO.getStatus() != null) {
            wrapper.eq(GoodsSkuSaleUnit::getStatus, queryDTO.getStatus());
        } else {
            wrapper.eq(GoodsSkuSaleUnit::getStatus, 1);
        }
        
        wrapper.eq(GoodsSkuSaleUnit::getDelFlag, 0);
        wrapper.orderByAsc(GoodsSkuSaleUnit::getSortOrder);
        
        Page<GoodsSkuSaleUnit> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        baseMapper.selectPage(page, wrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_NAME, CACHE_NAME_SKU, CACHE_NAME_ENABLED, CACHE_NAME_DEFAULT, CACHE_NAME_UNIFY, CACHE_NAME_MAX_PRICE, CACHE_NAME_CASHIER, CACHE_NAME_BARCODE, CACHE_NAME_PAGE}, allEntries = true)
    public boolean saveBatchBySkuId(Long skuId, List<GoodsSkuSaleUnit> saleUnitList) {
        deleteBySkuId(skuId);

        for (GoodsSkuSaleUnit unit : saleUnitList) {
            unit.setSkuId(skuId);
            if (unit.getStatus() == null) {
                unit.setStatus(1);
            }
            if (unit.getIsDefault() == null) {
                unit.setIsDefault(0);
            }
        }

        boolean result = batchUtils.saveBatch(this, saleUnitList);
        if (result) {
            for (GoodsSkuSaleUnit unit : saleUnitList) {
                goodsDownSyncProducer.sendGoodsSkuSaleUnit(unit);
            }
        }
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_NAME, CACHE_NAME_SKU, CACHE_NAME_ENABLED, CACHE_NAME_DEFAULT, CACHE_NAME_UNIFY, CACHE_NAME_MAX_PRICE, CACHE_NAME_CASHIER, CACHE_NAME_BARCODE, CACHE_NAME_PAGE}, allEntries = true)
    public boolean deleteBySkuId(Long skuId) {
        List<GoodsSkuSaleUnit> existingList = getBySkuId(skuId);
        LambdaQueryWrapper<GoodsSkuSaleUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsSkuSaleUnit::getSkuId, skuId);
        boolean result = remove(wrapper);
        if (result) {
            for (GoodsSkuSaleUnit unit : existingList) {
                unit.setDelFlag(1);
                goodsDownSyncProducer.sendGoodsSkuSaleUnit(unit);
            }
        }
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_NAME, CACHE_NAME_SKU, CACHE_NAME_ENABLED, CACHE_NAME_DEFAULT, CACHE_NAME_UNIFY, CACHE_NAME_MAX_PRICE, CACHE_NAME_CASHIER, CACHE_NAME_BARCODE, CACHE_NAME_PAGE}, allEntries = true)
    public boolean setDefault(Long skuId, Long saleUnitId) {
        GoodsSkuSaleUnit currentDefault = getDefaultBySkuId(skuId);
        if (currentDefault != null && !currentDefault.getId().equals(saleUnitId)) {
            currentDefault.setIsDefault(0);
            updateById(currentDefault);
        }

        GoodsSkuSaleUnit newDefault = getById(saleUnitId);
        if (newDefault != null && newDefault.getSkuId().equals(skuId)) {
            newDefault.setIsDefault(1);
            return updateById(newDefault);
        }
        return false;
    }
}