package com.psi.goods.service.impl;

import com.psi.goods.dto.GoodsUnitQueryDTO;
import com.psi.goods.entity.GoodsUnit;
import com.psi.goods.mapper.GoodsUnitMapper;
import com.psi.goods.mq.producer.GoodsDownSyncProducer;
import com.psi.goods.service.GoodsUnitService;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 商品单位服务实现
 */
@Service
public class GoodsUnitServiceImpl extends ServiceImpl<GoodsUnitMapper, GoodsUnit> implements GoodsUnitService {

    private final GoodsDownSyncProducer goodsDownSyncProducer;

    public GoodsUnitServiceImpl(GoodsDownSyncProducer goodsDownSyncProducer) {
        this.goodsDownSyncProducer = goodsDownSyncProducer;
    }

    @Override
    public boolean save(GoodsUnit entity) {
        boolean result = super.save(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsUnit(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(GoodsUnit entity) {
        boolean result = super.updateById(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsUnit(entity);
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        GoodsUnit entity = getById(id);
        if (entity != null) {
            entity.setDelFlag(1);
            goodsDownSyncProducer.sendGoodsUnit(entity);
        }
        return super.removeById(id);
    }

    @Override
    public List<GoodsUnit> getByUnitType(String unitType) {
        return baseMapper.selectByUnitType(unitType);
    }

    @Override
    public List<GoodsUnit> getAllEnabled() {
        return baseMapper.selectAllEnabled();
    }

    @Override
    public GoodsUnit getBySymbol(String unitSymbol) {
        return baseMapper.selectBySymbol(unitSymbol);
    }

    @Override
    public PageResult<GoodsUnit> queryPage(GoodsUnitQueryDTO queryDTO) {
        QueryWrapper<GoodsUnit> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getUnitCode())) {
            wrapper.like("unit_code", queryDTO.getUnitCode());
        }
        
        if (StringUtils.hasText(queryDTO.getUnitName())) {
            wrapper.like("unit_name", queryDTO.getUnitName());
        }
        
        if (StringUtils.hasText(queryDTO.getUnitSymbol())) {
            wrapper.like("unit_symbol", queryDTO.getUnitSymbol());
        }
        
        if (StringUtils.hasText(queryDTO.getUnitType())) {
            wrapper.eq("unit_type", queryDTO.getUnitType());
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
            wrapper.orderByAsc("unit_type", "sort_order");
        }
        
        Page<GoodsUnit> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        baseMapper.selectPage(page, wrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}