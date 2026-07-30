package com.psi.stock.service.impl;

import com.psi.stock.dto.StockFlowDTO;
import com.psi.stock.dto.StockFlowQueryDTO;
import com.psi.stock.entity.StockFlowEntity;
import com.psi.stock.mapper.StockFlowMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockFlowService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Service
public class StockFlowServiceImpl extends ServiceImpl<StockFlowMapper, StockFlowEntity> implements StockFlowService {

    private static final Map<Integer, String> FLOW_TYPE_MAP = Map.of(
        1, "入库",
        2, "出库",
        3, "报损",
        4, "报溢",
        5, "调拨出",
        6, "调拨入"
    );

    private final StockSyncProducer stockSyncProducer;

    public StockFlowServiceImpl(StockSyncProducer stockSyncProducer) {
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockFlowEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockFlowEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockFlowEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockFlowEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockFlowEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockFlowEntity entity) {
        try {
            stockSyncProducer.sendStockFlow(entity);
        } catch (Exception e) {
            log.error("库存流水实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<StockFlowDTO> getById(Long id) {
        StockFlowEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<StockFlowDTO> list(StockFlowQueryDTO queryDTO) {
        Page<StockFlowEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<StockFlowEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getWarehouseCode() != null) {
            wrapper.like(StockFlowEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (queryDTO.getGoodsCode() != null) {
            wrapper.like(StockFlowEntity::getGoodsCode, queryDTO.getGoodsCode());
        }
        if (queryDTO.getSkuCode() != null) {
            wrapper.like(StockFlowEntity::getSkuCode, queryDTO.getSkuCode());
        }
        if (queryDTO.getFlowType() != null) {
            wrapper.eq(StockFlowEntity::getFlowType, queryDTO.getFlowType());
        }
        if (queryDTO.getSourceNo() != null) {
            wrapper.like(StockFlowEntity::getSourceNo, queryDTO.getSourceNo());
        }
        if (queryDTO.getSourceType() != null) {
            wrapper.eq(StockFlowEntity::getSourceType, queryDTO.getSourceType());
        }
        
        wrapper.orderByDesc(StockFlowEntity::getCreateTime);
        
        IPage<StockFlowEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    public CommonResult<Void> addFlow(String warehouseCode, String warehouseName, String goodsCode, String skuCode, String goodsName,
                                      String goodsSpec, String unit, Integer flowType, BigDecimal inQuantity,
                                      BigDecimal outQuantity, BigDecimal beforeQuantity, BigDecimal afterQuantity,
                                      BigDecimal costPrice, BigDecimal amount, String sourceNo, String sourceType, String remark) {
        StockFlowEntity entity = new StockFlowEntity();
        entity.setWarehouseCode(warehouseCode);
        entity.setWarehouseName(warehouseName);
        entity.setGoodsCode(goodsCode);
        entity.setSkuCode(skuCode);
        entity.setGoodsName(goodsName);
        entity.setGoodsSpec(goodsSpec);
        entity.setUnit(unit);
        entity.setFlowType(flowType);
        entity.setInQuantity(inQuantity != null ? inQuantity : BigDecimal.ZERO);
        entity.setOutQuantity(outQuantity != null ? outQuantity : BigDecimal.ZERO);
        entity.setBeforeQuantity(beforeQuantity != null ? beforeQuantity : BigDecimal.ZERO);
        entity.setAfterQuantity(afterQuantity != null ? afterQuantity : BigDecimal.ZERO);
        entity.setCostPrice(costPrice != null ? costPrice : BigDecimal.ZERO);
        entity.setAmount(amount != null ? amount : BigDecimal.ZERO);
        entity.setSourceNo(sourceNo);
        entity.setSourceType(sourceType);
        entity.setRemark(remark);
        
        super.save(entity);
        syncSend(entity);
        return CommonResult.success();
    }

    private StockFlowDTO convertToDTO(StockFlowEntity entity) {
        StockFlowDTO dto = BeanUtils.convert(entity, StockFlowDTO.class);
        dto.setFlowTypeName(FLOW_TYPE_MAP.getOrDefault(entity.getFlowType(), "未知"));
        return dto;
    }
}
