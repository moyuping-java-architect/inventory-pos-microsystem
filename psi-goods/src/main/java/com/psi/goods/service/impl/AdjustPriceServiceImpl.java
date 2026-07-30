package com.psi.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.goods.entity.AdjustPriceItemEntity;
import com.psi.goods.entity.AdjustPriceMainEntity;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.mapper.AdjustPriceItemMapper;
import com.psi.goods.mapper.AdjustPriceMainMapper;
import com.psi.goods.service.AdjustPriceService;
import com.psi.goods.service.GoodsSkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品调价单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdjustPriceServiceImpl extends ServiceImpl<AdjustPriceMainMapper, AdjustPriceMainEntity>
        implements AdjustPriceService {

    private final AdjustPriceItemMapper itemMapper;
    private final GoodsSkuService goodsSkuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<AdjustPriceMainEntity> saveAdjustPrice(AdjustPriceMainEntity main, List<AdjustPriceItemEntity> items) {
        if (main == null) {
            return CommonResult.fail("调价单主表不能为空");
        }
        if (items == null || items.isEmpty()) {
            return CommonResult.fail("调价单明细不能为空");
        }
        main.setItemCount(items.size());
        if (main.getStatus() == null) {
            main.setStatus(0);
        }
        boolean mainSaved = save(main);
        if (!mainSaved) {
            return CommonResult.fail("保存调价单主表失败");
        }
        int sort = 1;
        for (AdjustPriceItemEntity item : items) {
            item.setAdjustId(main.getId());
            item.setSortOrder(sort++);
            if (item.getQuantity() == null) {
                item.setQuantity(BigDecimal.ONE);
            }
            if (item.getAmount() == null && item.getNewPrice() != null && item.getQuantity() != null) {
                item.setAmount(item.getNewPrice().multiply(item.getQuantity()));
            }
        }
        for (AdjustPriceItemEntity item : items) {
            itemMapper.insert(item);
        }
        return CommonResult.success(main);
    }

    @Override
    public CommonResult<AdjustPriceMainEntity> getDetailById(Long id) {
        AdjustPriceMainEntity main = getById(id);
        if (main == null) {
            return CommonResult.fail("调价单不存在");
        }
        main.setItems(itemMapper.selectByAdjustId(id));
        return CommonResult.success(main);
    }

    @Override
    public PageResult<AdjustPriceMainEntity> queryPage(String adjustNo, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<AdjustPriceMainEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdjustPriceMainEntity::getDelFlag, 0);
        if (StringUtils.hasText(adjustNo)) {
            wrapper.like(AdjustPriceMainEntity::getAdjustNo, adjustNo);
        }
        if (status != null) {
            wrapper.eq(AdjustPriceMainEntity::getStatus, status);
        }
        wrapper.orderByDesc(AdjustPriceMainEntity::getCreateTime);
        Page<AdjustPriceMainEntity> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.success(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> audit(Long id) {
        AdjustPriceMainEntity main = getById(id);
        if (main == null) {
            return CommonResult.fail("调价单不存在");
        }
        if (main.getStatus() != null && main.getStatus() == 2) {
            return CommonResult.fail("调价单已审核");
        }
        List<AdjustPriceItemEntity> items = itemMapper.selectByAdjustId(id);
        for (AdjustPriceItemEntity item : items) {
            GoodsSku sku = goodsSkuService.getOne(
                    new LambdaQueryWrapper<GoodsSku>().eq(GoodsSku::getSkuCode, item.getSkuCode())
            );
            if (sku != null) {
                sku.setSalePrice(item.getNewPrice());
                goodsSkuService.updateById(sku);
                log.info("调价单审核更新售价: adjustNo={}, skuCode={}, newPrice={}",
                        main.getAdjustNo(), item.getSkuCode(), item.getNewPrice());
            } else {
                log.warn("调价单审核未找到SKU: adjustNo={}, skuCode={}", main.getAdjustNo(), item.getSkuCode());
            }
        }
        main.setStatus(2);
        updateById(main);
        return CommonResult.success();
    }

    @Override
    public AdjustPriceMainEntity getByAdjustNo(String adjustNo) {
        if (!StringUtils.hasText(adjustNo)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<AdjustPriceMainEntity>()
                .eq(AdjustPriceMainEntity::getAdjustNo, adjustNo)
                .eq(AdjustPriceMainEntity::getDelFlag, 0));
    }
}
