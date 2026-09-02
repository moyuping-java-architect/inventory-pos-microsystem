package com.psi.goods.service;

import com.psi.goods.dto.GoodsUnitQueryDTO;
import com.psi.goods.entity.GoodsUnit;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品单位服务接口
 */
public interface GoodsUnitService extends IService<GoodsUnit> {

    /**
     * 根据单位类型查询
     */
    List<GoodsUnit> getByUnitType(String unitType);

    /**
     * 查询所有启用的单位
     */
    List<GoodsUnit> getAllEnabled();

    /**
     * 根据单位符号查询
     */
    GoodsUnit getBySymbol(String unitSymbol);

    /**
     * 分页查询单位
     */
    PageResult<GoodsUnit> queryPage(GoodsUnitQueryDTO queryDTO);
}