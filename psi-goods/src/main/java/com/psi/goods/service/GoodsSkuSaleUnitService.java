package com.psi.goods.service;

import com.psi.goods.dto.CashierSaleUnitDTO;
import com.psi.goods.dto.CashierGoodsQueryDTO;
import com.psi.goods.dto.GoodsSkuSaleUnitQueryDTO;
import com.psi.goods.entity.GoodsSkuSaleUnit;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * SKU销售单位服务接口
 */
public interface GoodsSkuSaleUnitService extends IService<GoodsSkuSaleUnit> {

    /**
     * 根据SKU ID查询所有销售单位
     */
    List<GoodsSkuSaleUnit> getBySkuId(Long skuId);

    /**
     * 根据SKU ID查询启用的销售单位
     */
    List<GoodsSkuSaleUnit> getEnabledBySkuId(Long skuId);

    /**
     * 根据SKU ID查询默认销售单位
     */
    GoodsSkuSaleUnit getDefaultBySkuId(Long skuId);

    /**
     * 根据商品统一编码查询所有销售单位
     */
    List<GoodsSkuSaleUnit> getByUnifyCode(String goodsUnifyCode);

    /**
     * 根据商品统一编码分组查询各销售单位的最高价格
     * 用于收银端展示，保证利润最大化
     */
    List<GoodsSkuSaleUnit> getMaxPriceByUnifyCode(String goodsUnifyCode);

    /**
     * 收银端查询商品销售单位（分页）
     * 前端收银查找商品时使用此方法
     */
    PageResult<CashierSaleUnitDTO> queryForCashier(CashierGoodsQueryDTO queryDTO);

    /**
     * 根据商品统一编码查询销售单位（收银端展示）
     */
    List<CashierSaleUnitDTO> getCashierSaleUnits(String goodsUnifyCode);

    /**
     * 根据条码查询销售单位（返回最高价格）
     * 用于收银端扫码查询，保证利润最大化
     */
    List<CashierSaleUnitDTO> getByBarcode(String barcode);

    /**
     * 分页查询SKU销售单位
     */
    PageResult<GoodsSkuSaleUnit> queryPage(GoodsSkuSaleUnitQueryDTO queryDTO);

    /**
     * 批量保存SKU销售单位
     */
    boolean saveBatchBySkuId(Long skuId, List<GoodsSkuSaleUnit> saleUnitList);

    /**
     * 删除SKU所有销售单位
     */
    boolean deleteBySkuId(Long skuId);

    /**
     * 设置默认销售单位
     */
    boolean setDefault(Long skuId, Long saleUnitId);
}