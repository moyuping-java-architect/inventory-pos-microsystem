package com.psi.goods.service;

import com.psi.goods.dto.GoodsQueryDTO;
import com.psi.goods.entity.Goods;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品服务接口
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 查询所有启用的商品
     */
    List<Goods> getAllEnabled();

    /**
     * 根据商品编码查询
     */
    Goods getByCode(String goodsCode);

    /**
     * 根据分类ID查询商品
     */
    List<Goods> getByCategoryId(Long categoryId);

    /**
     * 根据品牌ID查询商品
     */
    List<Goods> getByBrandId(Long brandId);

    /**
     * 根据商品名称模糊查询
     */
    List<Goods> getByName(String goodsName);

    /**
     * 分页查询商品
     */
    PageResult<Goods> queryPage(GoodsQueryDTO queryDTO);
}