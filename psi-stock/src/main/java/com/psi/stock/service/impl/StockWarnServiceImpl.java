package com.psi.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.psi.stock.dto.StockWarnDTO;
import com.psi.stock.dto.StockWarnQueryDTO;
import com.psi.stock.entity.StockEntity;
import com.psi.stock.entity.StockWarnEntity;
import com.psi.stock.mapper.StockMapper;
import com.psi.stock.mapper.StockWarnMapper;
import com.psi.stock.service.StockWarnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockWarnServiceImpl extends ServiceImpl<StockWarnMapper, StockWarnEntity> implements StockWarnService {

    private final StockMapper stockMapper;

    public StockWarnServiceImpl(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    @Override
    public PageResult<StockWarnDTO> page(StockWarnQueryDTO queryDTO) {
        LambdaQueryWrapper<StockWarnEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getWarehouseCode())) {
            wrapper.eq(StockWarnEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (StringUtils.hasText(queryDTO.getGoodsCode())) {
            wrapper.like(StockWarnEntity::getGoodsCode, queryDTO.getGoodsCode());
        }
        if (StringUtils.hasText(queryDTO.getSkuCode())) {
            wrapper.like(StockWarnEntity::getSkuCode, queryDTO.getSkuCode());
        }
        if (StringUtils.hasText(queryDTO.getGoodsName())) {
            wrapper.like(StockWarnEntity::getGoodsName, queryDTO.getGoodsName());
        }
        if (queryDTO.getWarnType() != null) {
            wrapper.eq(StockWarnEntity::getWarnType, queryDTO.getWarnType());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(StockWarnEntity::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(StockWarnEntity::getCreateTime);

        Page<StockWarnEntity> page = baseMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                wrapper
        );

        List<StockWarnDTO> list = page.getRecords().stream()
                .map(e -> BeanUtils.convert(e, StockWarnDTO.class))
                .collect(Collectors.toList());

        return PageResult.success(list, page.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional
    public CommonResult<Void> add(StockWarnDTO dto) {
        StockWarnEntity entity = BeanUtils.convert(dto, StockWarnEntity.class);
        baseMapper.insert(entity);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> update(StockWarnDTO dto) {
        StockWarnEntity entity = BeanUtils.convert(dto, StockWarnEntity.class);
        baseMapper.updateById(entity);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        baseMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<List<StockWarnDTO>> getLowStockList(String warehouseCode) {
        List<StockWarnEntity> list = baseMapper.selectList(
                new LambdaQueryWrapper<StockWarnEntity>()
                        .eq(StockWarnEntity::getWarnType, 1)
                        .eq(StockWarnEntity::getStatus, 1)
                        .eq(StringUtils.hasText(warehouseCode), StockWarnEntity::getWarehouseCode, warehouseCode)
                        .orderByAsc(StockWarnEntity::getCurrentQty)
        );

        List<StockWarnDTO> result = list.stream()
                .map(e -> BeanUtils.convert(e, StockWarnDTO.class))
                .collect(Collectors.toList());

        return CommonResult.success(result);
    }

    @Override
    public CommonResult<List<StockWarnDTO>> getHighStockList(String warehouseCode) {
        List<StockWarnEntity> list = baseMapper.selectList(
                new LambdaQueryWrapper<StockWarnEntity>()
                        .eq(StockWarnEntity::getWarnType, 2)
                        .eq(StockWarnEntity::getStatus, 1)
                        .eq(StringUtils.hasText(warehouseCode), StockWarnEntity::getWarehouseCode, warehouseCode)
                        .orderByDesc(StockWarnEntity::getCurrentQty)
        );

        List<StockWarnDTO> result = list.stream()
                .map(e -> BeanUtils.convert(e, StockWarnDTO.class))
                .collect(Collectors.toList());

        return CommonResult.success(result);
    }

    @Override
    public CommonResult<Integer> getLowStockCount(String warehouseCode) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<StockWarnEntity>()
                        .eq(StockWarnEntity::getWarnType, 1)
                        .eq(StockWarnEntity::getStatus, 1)
                        .eq(StringUtils.hasText(warehouseCode), StockWarnEntity::getWarehouseCode, warehouseCode)
        );
        return CommonResult.success(count.intValue());
    }

    @Override
    public CommonResult<Integer> getHighStockCount(String warehouseCode) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<StockWarnEntity>()
                        .eq(StockWarnEntity::getWarnType, 2)
                        .eq(StockWarnEntity::getStatus, 1)
                        .eq(StringUtils.hasText(warehouseCode), StockWarnEntity::getWarehouseCode, warehouseCode)
        );
        return CommonResult.success(count.intValue());
    }

    @Override
    @Transactional
    public void checkAndGenerateWarn(String warehouseCode, String skuCode) {
        StockEntity stock = stockMapper.selectOne(
                new LambdaQueryWrapper<StockEntity>()
                        .eq(StockEntity::getWarehouseCode, warehouseCode)
                        .eq(StockEntity::getSkuCode, skuCode)
                        .last("LIMIT 1")
        );

        if (stock == null) {
            return;
        }

        StockWarnEntity existingWarn = baseMapper.selectOne(
                new LambdaQueryWrapper<StockWarnEntity>()
                        .eq(StockWarnEntity::getWarehouseCode, warehouseCode)
                        .eq(StockWarnEntity::getSkuCode, skuCode)
                        .eq(StockWarnEntity::getStatus, 1)
                        .last("LIMIT 1")
        );

        if (stock.getAvailableQuantity() != null && stock.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal minStock = stock.getAvailableQuantity().multiply(BigDecimal.valueOf(0.1));
            BigDecimal maxStock = stock.getAvailableQuantity().multiply(BigDecimal.valueOf(5));

            if (stock.getAvailableQuantity().compareTo(minStock) <= 0) {
                if (existingWarn == null) {
                    StockWarnEntity warn = new StockWarnEntity();
                    warn.setWarehouseCode(stock.getWarehouseCode());
                    warn.setWarehouseName(stock.getWarehouseName());
                    warn.setGoodsCode(stock.getGoodsCode());
                    warn.setGoodsName(stock.getGoodsName());
                    warn.setSkuCode(stock.getSkuCode());
                    warn.setGoodsSpec(stock.getGoodsSpec());
                    warn.setUnit(stock.getUnit());
                    warn.setMinStockQty(minStock);
                    warn.setMaxStockQty(maxStock);
                    warn.setCurrentQty(stock.getAvailableQuantity());
                    warn.setWarnType(1);
                    warn.setStatus(1);
                    warn.setRemark("库存低于安全库存，请注意补货");
                    baseMapper.insert(warn);
                    log.warn("库存预警：仓库={}, SKU={}, 当前库存={}, 最低库存={}",
                            warehouseCode, skuCode, stock.getAvailableQuantity(), minStock);
                } else {
                    existingWarn.setCurrentQty(stock.getAvailableQuantity());
                    baseMapper.updateById(existingWarn);
                }
            } else if (stock.getAvailableQuantity().compareTo(maxStock) >= 0) {
                if (existingWarn == null) {
                    StockWarnEntity warn = new StockWarnEntity();
                    warn.setWarehouseCode(stock.getWarehouseCode());
                    warn.setWarehouseName(stock.getWarehouseName());
                    warn.setGoodsCode(stock.getGoodsCode());
                    warn.setGoodsName(stock.getGoodsName());
                    warn.setSkuCode(stock.getSkuCode());
                    warn.setGoodsSpec(stock.getGoodsSpec());
                    warn.setUnit(stock.getUnit());
                    warn.setMinStockQty(minStock);
                    warn.setMaxStockQty(maxStock);
                    warn.setCurrentQty(stock.getAvailableQuantity());
                    warn.setWarnType(2);
                    warn.setStatus(1);
                    warn.setRemark("库存超过最高库存，建议促销或减少采购");
                    baseMapper.insert(warn);
                    log.warn("库存超储预警：仓库={}, SKU={}, 当前库存={}, 最高库存={}",
                            warehouseCode, skuCode, stock.getAvailableQuantity(), maxStock);
                } else {
                    existingWarn.setCurrentQty(stock.getAvailableQuantity());
                    baseMapper.updateById(existingWarn);
                }
            } else {
                if (existingWarn != null) {
                    existingWarn.setStatus(0);
                    baseMapper.updateById(existingWarn);
                    log.info("库存恢复正常：仓库={}, SKU={}, 当前库存={}",
                            warehouseCode, skuCode, stock.getAvailableQuantity());
                }
            }
        }
    }
}
