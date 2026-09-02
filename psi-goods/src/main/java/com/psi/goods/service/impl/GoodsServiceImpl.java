package com.psi.goods.service.impl;

import com.psi.goods.dto.GoodsQueryDTO;
import com.psi.goods.entity.Goods;
import com.psi.goods.mapper.GoodsMapper;
import com.psi.goods.mq.producer.GoodsDownSyncProducer;
import com.psi.goods.service.GoodsService;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 商品服务实现
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private final GoodsDownSyncProducer goodsDownSyncProducer;

    public GoodsServiceImpl(GoodsDownSyncProducer goodsDownSyncProducer) {
        this.goodsDownSyncProducer = goodsDownSyncProducer;
    }

    @Override
    public boolean save(Goods entity) {
        boolean result = super.save(entity);
        if (result) {
            goodsDownSyncProducer.sendGoods(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(Goods entity) {
        boolean result = super.updateById(entity);
        if (result) {
            goodsDownSyncProducer.sendGoods(entity);
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        Goods entity = getById(id);
        if (entity != null) {
            entity.setDelFlag(1);
            goodsDownSyncProducer.sendGoods(entity);
        }
        return super.removeById(id);
    }

    @Override
    public List<Goods> getAllEnabled() {
        return baseMapper.selectAllEnabled();
    }

    @Override
    public Goods getByCode(String goodsCode) {
        return baseMapper.selectByCode(goodsCode);
    }

    @Override
    public List<Goods> getByCategoryId(Long categoryId) {
        return baseMapper.selectByCategoryId(categoryId);
    }

    @Override
    public List<Goods> getByBrandId(Long brandId) {
        return baseMapper.selectByBrandId(brandId);
    }

    @Override
    public List<Goods> getByName(String goodsName) {
        return baseMapper.selectByName(goodsName);
    }

    @Override
    public PageResult<Goods> queryPage(GoodsQueryDTO queryDTO) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getGoodsCode())) {
            wrapper.like("goods_code", queryDTO.getGoodsCode());
        }
        
        if (StringUtils.hasText(queryDTO.getGoodsName())) {
            wrapper.like("goods_name", queryDTO.getGoodsName());
        }
        
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq("category_id", queryDTO.getCategoryId());
        }
        
        if (queryDTO.getBrandId() != null) {
            wrapper.eq("brand_id", queryDTO.getBrandId());
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
        
        Page<Goods> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        baseMapper.selectPage(page, wrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}