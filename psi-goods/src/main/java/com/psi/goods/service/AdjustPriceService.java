package com.psi.goods.service;

import com.psi.goods.entity.AdjustPriceMainEntity;
import com.psi.goods.entity.AdjustPriceItemEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

import java.util.List;

/**
 * 商品调价单服务接口
 */
public interface AdjustPriceService extends IService<AdjustPriceMainEntity> {

    /**
     * 保存调价单（主表+明细）
     */
    CommonResult<AdjustPriceMainEntity> saveAdjustPrice(AdjustPriceMainEntity main, List<AdjustPriceItemEntity> items);

    /**
     * 根据ID查询调价单（含明细）
     */
    CommonResult<AdjustPriceMainEntity> getDetailById(Long id);

    /**
     * 分页查询调价单
     */
    PageResult<AdjustPriceMainEntity> queryPage(String adjustNo, Integer status, int pageNum, int pageSize);

    /**
     * 审核调价单：更新状态并同步修改 SKU 销售价
     */
    CommonResult<Void> audit(Long id);

    /**
     * 根据单号查询
     */
    AdjustPriceMainEntity getByAdjustNo(String adjustNo);
}
