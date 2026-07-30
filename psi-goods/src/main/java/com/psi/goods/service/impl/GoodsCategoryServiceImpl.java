package com.psi.goods.service.impl;

import com.psi.goods.dto.GoodsCategoryQueryDTO;
import com.psi.goods.entity.GoodsCategory;
import com.psi.goods.mapper.GoodsCategoryMapper;
import com.psi.goods.mq.producer.GoodsDownSyncProducer;
import com.psi.goods.service.GoodsCategoryService;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现
 */
@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {

    private final GoodsDownSyncProducer goodsDownSyncProducer;

    public GoodsCategoryServiceImpl(GoodsDownSyncProducer goodsDownSyncProducer) {
        this.goodsDownSyncProducer = goodsDownSyncProducer;
    }

    @Override
    public boolean save(GoodsCategory entity) {
        boolean result = super.save(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsCategory(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(GoodsCategory entity) {
        boolean result = super.updateById(entity);
        if (result) {
            goodsDownSyncProducer.sendGoodsCategory(entity);
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        GoodsCategory entity = getById(id);
        if (entity != null) {
            entity.setDelFlag(1);
            goodsDownSyncProducer.sendGoodsCategory(entity);
        }
        return super.removeById(id);
    }

    @Override
    public List<GoodsCategory> getTopLevel() {
        return baseMapper.selectTopLevel();
    }

    @Override
    public List<GoodsCategory> getByParentId(Long parentId) {
        return baseMapper.selectByParentId(parentId);
    }

    @Override
    public List<GoodsCategory> getAllEnabled() {
        return baseMapper.selectAllEnabled();
    }

    @Override
    public GoodsCategory getByCode(String categoryCode) {
        return baseMapper.selectByCode(categoryCode);
    }

    @Override
    public List<GoodsCategory> getCategoryTree() {
        List<GoodsCategory> allCategories = baseMapper.selectAllEnabled();
        
        Map<Long, List<GoodsCategory>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(GoodsCategory::getParentId));

        List<GoodsCategory> tree = new ArrayList<>();
        for (GoodsCategory category : allCategories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                addChildren(category, childrenMap);
                tree.add(category);
            }
        }
        return tree;
    }

    private void addChildren(GoodsCategory parent, Map<Long, List<GoodsCategory>> childrenMap) {
        List<GoodsCategory> children = childrenMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            for (GoodsCategory child : children) {
                addChildren(child, childrenMap);
            }
        }
    }

    @Override
    public PageResult<GoodsCategory> queryPage(GoodsCategoryQueryDTO queryDTO) {
        QueryWrapper<GoodsCategory> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getCategoryCode())) {
            wrapper.like("category_code", queryDTO.getCategoryCode());
        }
        
        if (StringUtils.hasText(queryDTO.getCategoryName())) {
            wrapper.like("category_name", queryDTO.getCategoryName());
        }
        
        if (queryDTO.getParentId() != null) {
            wrapper.eq("parent_id", queryDTO.getParentId());
        }
        
        if (queryDTO.getLevel() != null) {
            wrapper.eq("level", queryDTO.getLevel());
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
            wrapper.orderByAsc("level", "sort_order");
        }
        
        Page<GoodsCategory> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        baseMapper.selectPage(page, wrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}