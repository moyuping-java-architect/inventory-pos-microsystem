package com.psi.goods.service;

import com.psi.goods.dto.GoodsBrandQueryDTO;
import com.psi.goods.entity.GoodsBrand;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品品牌服务接口
 */
public interface GoodsBrandService extends IService<GoodsBrand> {

    /**
     * 查询所有启用的品牌
     */
    List<GoodsBrand> getAllEnabled();

    /**
     * 根据品牌编码查询
     */
    GoodsBrand getByCode(String brandCode);

    /**
     * 根据品牌名称查询
     */
    List<GoodsBrand> getByName(String brandName);

    /**
     * 分页查询品牌
     */
    PageResult<GoodsBrand> queryPage(GoodsBrandQueryDTO queryDTO);
}