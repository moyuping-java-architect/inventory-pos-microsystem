package com.psi.goods.service.impl;

import com.psi.goods.dto.GoodsBrandQueryDTO;
import com.psi.goods.entity.GoodsBrand;
import com.psi.goods.mapper.GoodsBrandMapper;
import com.psi.goods.mq.producer.GoodsDownSyncProducer;
import com.psi.goods.service.GoodsBrandService;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 商品品牌服务实现
 */
@Service
public class GoodsBrandServiceImpl extends ServiceImpl<GoodsBrandMapper, GoodsBrand> implements GoodsBrandService {

    private final GoodsDownSyncProducer goodsDownSyncProducer;

    public GoodsBrandServiceImpl(GoodsDownSyncProducer goodsDownSyncProducer) {
        this.goodsDownSyncProducer = goodsDownSyncProducer;
    }

    @Override
    public boolean save(GoodsBrand entity) {
        boolean result = super.save(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsBrand(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(GoodsBrand entity) {
        boolean result = super.updateById(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsBrand(entity);
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        GoodsBrand entity = getById(id);
        if (entity != null) {
            entity.setDelFlag(1);
            goodsDownSyncProducer.sendGoodsBrand(entity);
        }
        return super.removeById(id);
    }

    @Override
    public List<GoodsBrand> getAllEnabled() {
        return baseMapper.selectAllEnabled();
    }

    @Override
    public GoodsBrand getByCode(String brandCode) {
        return baseMapper.selectByCode(brandCode);
    }

    @Override
    public List<GoodsBrand> getByName(String brandName) {
        return baseMapper.selectByName(brandName);
    }

    @Override
    public PageResult<GoodsBrand> queryPage(GoodsBrandQueryDTO queryDTO) {
        QueryWrapper<GoodsBrand> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getBrandCode())) {
            wrapper.like("brand_code", queryDTO.getBrandCode());
        }
        
        if (StringUtils.hasText(queryDTO.getBrandName())) {
            wrapper.like("brand_name", queryDTO.getBrandName());
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
            wrapper.orderByAsc("sort_order");
        }
        
        Page<GoodsBrand> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        baseMapper.selectPage(page, wrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}