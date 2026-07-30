package com.psi.goods.service.impl;

import com.psi.goods.dto.CashierGoodsDTO;
import com.psi.goods.dto.CashierGoodsQueryDTO;
import com.psi.goods.dto.GoodsSkuQueryDTO;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.mapper.GoodsSkuMapper;
import com.psi.goods.mq.producer.GoodsDownSyncProducer;
import com.psi.goods.service.GoodsSkuService;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 商品SKU服务实现
 */
@Service
public class GoodsSkuServiceImpl extends ServiceImpl<GoodsSkuMapper, GoodsSku> implements GoodsSkuService {

    private static final String CACHE_NAME = "sku";

    private final GoodsDownSyncProducer goodsDownSyncProducer;

    public GoodsSkuServiceImpl(GoodsDownSyncProducer goodsDownSyncProducer) {
        this.goodsDownSyncProducer = goodsDownSyncProducer;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean save(GoodsSku entity) {
        boolean result = super.save(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsSku(entity);
        }
        return result;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean updateById(GoodsSku entity) {
        boolean result = super.updateById(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsSku(entity);
        }
        return result;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean removeById(Serializable id) {
        GoodsSku entity = getById(id);
        if (entity != null) {
            entity.setDelFlag(1);
            goodsDownSyncProducer.sendGoodsSku(entity);
        }
        return super.removeById(id);
    }

    @Override
    public PageResult<CashierGoodsDTO> queryForCashier(CashierGoodsQueryDTO queryDTO) {
        String hasStockCondition = queryDTO.getHasStock() != null && queryDTO.getHasStock()
                ? "s.stock_qty > 0"
                : "1 = 1";

        Integer offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        Integer limit = queryDTO.getPageSize();

        List<CashierGoodsDTO> list = baseMapper.selectForCashier(
                queryDTO.getGoodsUnifyCode(),
                queryDTO.getGoodsName(),
                queryDTO.getCategoryId(),
                queryDTO.getBrandId(),
                hasStockCondition,
                offset,
                limit
        );

        Long total = baseMapper.countForCashier(
                queryDTO.getGoodsUnifyCode(),
                queryDTO.getGoodsName(),
                queryDTO.getCategoryId(),
                queryDTO.getBrandId(),
                hasStockCondition
        );

        return PageResult.success(list, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":unify", key = "#goodsUnifyCode", unless = "#result == null or #result.isEmpty()")
    public List<GoodsSku> getByUnifyCode(String goodsUnifyCode) {
        return baseMapper.selectByUnifyCode(goodsUnifyCode);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":highestPrice", key = "#goodsUnifyCode")
    public GoodsSku getHighestPriceSku(String goodsUnifyCode) {
        List<GoodsSku> skuList = baseMapper.selectByUnifyCode(goodsUnifyCode);
        if (skuList == null || skuList.isEmpty()) {
            return null;
        }
        return skuList.stream()
                .max((s1, s2) -> s1.getSalePrice().compareTo(s2.getSalePrice()))
                .orElse(null);
    }

    @Override
    @Cacheable(value = CACHE_NAME + ":barcode", key = "#barcode")
    public CashierGoodsDTO getByBarcode(String barcode) {
        List<CashierGoodsDTO> result = baseMapper.selectByBarcode(barcode);
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public PageResult<GoodsSku> queryPage(GoodsSkuQueryDTO queryDTO) {
        QueryWrapper<GoodsSku> wrapper = new QueryWrapper<>();
        
        if (queryDTO.getGoodsId() != null) {
            wrapper.eq("goods_id", queryDTO.getGoodsId());
        }
        
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like("sku_code", queryDTO.getKeyword())
                    .or()
                    .like("goods_unify_code", queryDTO.getKeyword())
                    .or()
                    .like("barcode", queryDTO.getKeyword()));
        } else {
            if (StringUtils.hasText(queryDTO.getSkuCode())) {
                wrapper.like("sku_code", queryDTO.getSkuCode());
            }

            if (StringUtils.hasText(queryDTO.getGoodsUnifyCode())) {
                wrapper.eq("goods_unify_code", queryDTO.getGoodsUnifyCode());
            }

            if (StringUtils.hasText(queryDTO.getBarcode())) {
                wrapper.like("barcode", queryDTO.getBarcode());
            }
        }
        
        if (queryDTO.getHasStock() != null && queryDTO.getHasStock()) {
            wrapper.gt("stock_qty", 0);
        }
        
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus());
        } else {
            wrapper.eq("status", 1);
        }
        
        wrapper.eq("del_flag", 0);
        
        if (StringUtils.hasText(queryDTO.getSortBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortDir());
            wrapper.orderBy(true, isAsc, queryDTO.getSortBy());
        } else {
            wrapper.orderByDesc("create_time");
        }
        
        Page<GoodsSku> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        baseMapper.selectPage(page, wrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}