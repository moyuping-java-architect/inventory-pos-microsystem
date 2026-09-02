package com.psi.goods.service;

import com.psi.goods.dto.CashierGoodsDTO;
import com.psi.goods.dto.CashierGoodsQueryDTO;
import com.psi.goods.dto.GoodsSkuQueryDTO;
import com.psi.goods.entity.GoodsSku;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品SKU服务接口
 */
public interface GoodsSkuService extends IService<GoodsSku> {

    /**
     * 收银端按goods_unify_code分组查询商品
     */
    PageResult<CashierGoodsDTO> queryForCashier(CashierGoodsQueryDTO queryDTO);

    /**
     * 根据统一编码查询所有SKU
     */
    List<GoodsSku> getByUnifyCode(String goodsUnifyCode);

    /**
     * 根据统一编码获取最高价格的SKU
     */
    GoodsSku getHighestPriceSku(String goodsUnifyCode);

    /**
     * 根据条码查询商品（按最高价格展示）
     */
    CashierGoodsDTO getByBarcode(String barcode);

    /**
     * 分页查询SKU
     */
    PageResult<GoodsSku> queryPage(GoodsSkuQueryDTO queryDTO);
}