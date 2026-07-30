package com.psi.goods.service;

import com.psi.goods.dto.GoodsCategoryQueryDTO;
import com.psi.goods.entity.GoodsCategory;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface GoodsCategoryService extends IService<GoodsCategory> {

    /**
     * 查询顶级分类
     */
    List<GoodsCategory> getTopLevel();

    /**
     * 根据父分类ID查询子分类
     */
    List<GoodsCategory> getByParentId(Long parentId);

    /**
     * 查询所有启用的分类
     */
    List<GoodsCategory> getAllEnabled();

    /**
     * 根据分类编码查询
     */
    GoodsCategory getByCode(String categoryCode);

    /**
     * 获取分类树（递归）
     */
    List<GoodsCategory> getCategoryTree();

    /**
     * 分页查询分类
     */
    PageResult<GoodsCategory> queryPage(GoodsCategoryQueryDTO queryDTO);
}