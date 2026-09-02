package com.psi.purchase.service.impl;

import com.psi.purchase.dto.PurchaseOrderItemDTO;
import com.psi.purchase.dto.PurchaseOrderItemSaveDTO;
import com.psi.purchase.dto.PurchaseOrderMainDTO;
import com.psi.purchase.dto.PurchaseOrderQueryDTO;
import com.psi.purchase.dto.PurchaseOrderSaveDTO;
import com.psi.purchase.entity.PurchaseOrderItemEntity;
import com.psi.purchase.entity.PurchaseOrderMainEntity;
import com.psi.purchase.mapper.PurchaseOrderMainMapper;
import com.psi.purchase.mq.producer.PurchaseSyncProducer;
import com.psi.purchase.service.PurchaseOrderItemService;
import com.psi.purchase.service.PurchaseOrderMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.psi.common.util.IdUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PurchaseOrderMainServiceImpl extends ServiceImpl<PurchaseOrderMainMapper, PurchaseOrderMainEntity> implements PurchaseOrderMainService {

    private final PurchaseOrderItemService purchaseOrderItemService;
    private final PurchaseSyncProducer purchaseSyncProducer;

    public PurchaseOrderMainServiceImpl(PurchaseOrderItemService purchaseOrderItemService, PurchaseSyncProducer purchaseSyncProducer) {
        this.purchaseOrderItemService = purchaseOrderItemService;
        this.purchaseSyncProducer = purchaseSyncProducer;
    }

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.16");

    @Override
    public CommonResult<PurchaseOrderMainDTO> getById(Long id) {
        PurchaseOrderMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<PurchaseOrderMainDTO> list(PurchaseOrderQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new PurchaseOrderQueryDTO();
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        Page<PurchaseOrderMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<PurchaseOrderMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getOrderNo() != null) {
            wrapper.like(PurchaseOrderMainEntity::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getSupplierCode() != null) {
            wrapper.like(PurchaseOrderMainEntity::getSupplierCode, queryDTO.getSupplierCode());
        }
        if (queryDTO.getSupplierName() != null) {
            wrapper.like(PurchaseOrderMainEntity::getSupplierName, queryDTO.getSupplierName());
        }
        if (queryDTO.getOrderDate() != null) {
            wrapper.eq(PurchaseOrderMainEntity::getOrderDate, queryDTO.getOrderDate());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(PurchaseOrderMainEntity::getCreateTime, java.time.LocalDate.parse(queryDTO.getStartDate()).atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(PurchaseOrderMainEntity::getCreateTime, java.time.LocalDate.parse(queryDTO.getEndDate()).plusDays(1).atStartOfDay());
        }
        if (queryDTO.getOrderStatus() != null) {
            wrapper.eq(PurchaseOrderMainEntity::getOrderStatus, queryDTO.getOrderStatus());
        }
        if (queryDTO.getAuditStatus() != null) {
            wrapper.eq(PurchaseOrderMainEntity::getAuditStatus, queryDTO.getAuditStatus());
        }
        
        IPage<PurchaseOrderMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<PurchaseOrderMainDTO> save(PurchaseOrderSaveDTO saveDTO) {
        PurchaseOrderMainEntity entity = BeanUtils.convert(saveDTO, PurchaseOrderMainEntity.class);
        if (entity.getOrderNo() == null || entity.getOrderNo().trim().isEmpty()) {
            entity.setOrderNo("PO" + IdUtils.generateId());
        }
        entity.setOrderStatus(1);
        entity.setAuditStatus(0);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        List<PurchaseOrderItemSaveDTO> itemList = saveDTO.getItems();
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        for (PurchaseOrderItemSaveDTO itemSaveDTO : itemList) {
            TaxInfo taxInfo = computeItemTax(itemSaveDTO);
            BigDecimal itemDiscount = taxInfo.netAmount.multiply(
                    itemSaveDTO.getDiscountRate() != null ? itemSaveDTO.getDiscountRate() : BigDecimal.ZERO);

            totalAmount = totalAmount.add(taxInfo.netAmount);
            taxAmount = taxAmount.add(taxInfo.taxAmount);
            discountAmount = discountAmount.add(itemDiscount);
        }

        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        entity.setDiscountAmount(discountAmount);
        entity.setPayAmount(totalAmount.add(taxAmount).subtract(discountAmount));

        super.save(entity);

        List<PurchaseOrderItemEntity> items = new ArrayList<>();
        List<PurchaseOrderItemSaveDTO> updateItemList = saveDTO.getItems();
        if (updateItemList == null) {
            updateItemList = new ArrayList<>();
        }
        for (PurchaseOrderItemSaveDTO itemSaveDTO : updateItemList) {
            PurchaseOrderItemEntity item = BeanUtils.convert(itemSaveDTO, PurchaseOrderItemEntity.class);
            item.setOrderId(entity.getId());
            item.setOrderNo(entity.getOrderNo());
            TaxInfo taxInfo = computeItemTax(itemSaveDTO);
            BigDecimal itemDiscount = taxInfo.netAmount.multiply(
                    itemSaveDTO.getDiscountRate() != null ? itemSaveDTO.getDiscountRate() : BigDecimal.ZERO);

            item.setAmount(taxInfo.netAmount);
            item.setTaxRate(taxInfo.taxRate);
            item.setIsTaxInclusive(taxInfo.isTaxInclusive);
            item.setTaxAmount(taxInfo.taxAmount);
            item.setDiscountAmount(itemDiscount);
            item.setNetAmount(taxInfo.netAmount.add(taxInfo.taxAmount).subtract(itemDiscount));
            items.add(item);
        }

        if (!items.isEmpty()) {
            purchaseOrderItemService.saveBatch(items);
        }

        sendPurchaseOrderMain(entity);
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<PurchaseOrderMainDTO> update(Long id, PurchaseOrderSaveDTO saveDTO) {
        PurchaseOrderMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        for (PurchaseOrderItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            TaxInfo taxInfo = computeItemTax(itemSaveDTO);
            BigDecimal itemDiscount = taxInfo.netAmount.multiply(
                    itemSaveDTO.getDiscountRate() != null ? itemSaveDTO.getDiscountRate() : BigDecimal.ZERO);

            totalAmount = totalAmount.add(taxInfo.netAmount);
            taxAmount = taxAmount.add(taxInfo.taxAmount);
            discountAmount = discountAmount.add(itemDiscount);
        }

        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        entity.setDiscountAmount(discountAmount);
        entity.setPayAmount(totalAmount.add(taxAmount).subtract(discountAmount));

        super.updateById(entity);

        purchaseOrderItemService.remove(new LambdaQueryWrapper<PurchaseOrderItemEntity>().eq(PurchaseOrderItemEntity::getOrderId, id));

        List<PurchaseOrderItemEntity> items = new ArrayList<>();
        for (PurchaseOrderItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            PurchaseOrderItemEntity item = BeanUtils.convert(itemSaveDTO, PurchaseOrderItemEntity.class);
            item.setOrderId(entity.getId());
            item.setOrderNo(entity.getOrderNo());
            TaxInfo taxInfo = computeItemTax(itemSaveDTO);
            BigDecimal itemDiscount = taxInfo.netAmount.multiply(
                    itemSaveDTO.getDiscountRate() != null ? itemSaveDTO.getDiscountRate() : BigDecimal.ZERO);

            item.setAmount(taxInfo.netAmount);
            item.setTaxRate(taxInfo.taxRate);
            item.setIsTaxInclusive(taxInfo.isTaxInclusive);
            item.setTaxAmount(taxInfo.taxAmount);
            item.setDiscountAmount(itemDiscount);
            item.setNetAmount(taxInfo.netAmount.add(taxInfo.taxAmount).subtract(itemDiscount));
            items.add(item);
        }

        if (!items.isEmpty()) {
            purchaseOrderItemService.saveBatch(items);
        }

        sendPurchaseOrderMain(entity);
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        purchaseOrderItemService.remove(new LambdaQueryWrapper<PurchaseOrderItemEntity>().eq(PurchaseOrderItemEntity::getOrderId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> audit(Long id, Integer auditStatus) {
        PurchaseOrderMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setAuditStatus(auditStatus);
        entity.setAuditTime(LocalDateTime.now());
        if (auditStatus == 1) {
            entity.setOrderStatus(2);
        }
        super.updateById(entity);
        sendPurchaseOrderMain(entity);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        PurchaseOrderMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        sendPurchaseOrderMain(entity);
        return CommonResult.success();
    }

    private void sendPurchaseOrderMain(PurchaseOrderMainEntity entity) {
        try {
            purchaseSyncProducer.sendPurchaseOrderMain(entity);
        } catch (Exception e) {
            log.error("采购订单主表实时同步消息发送失败", e);
        }
    }

    private PurchaseOrderMainDTO convertToDTO(PurchaseOrderMainEntity entity) {
        PurchaseOrderMainDTO dto = BeanUtils.convert(entity, PurchaseOrderMainDTO.class);

        List<PurchaseOrderItemEntity> items = purchaseOrderItemService.list(
            new LambdaQueryWrapper<PurchaseOrderItemEntity>().eq(PurchaseOrderItemEntity::getOrderId, entity.getId())
        );

        if (items == null) {
            items = new ArrayList<>();
        }

        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, PurchaseOrderItemDTO.class)).toList());
        return dto;
    }

    private TaxInfo computeItemTax(PurchaseOrderItemSaveDTO item) {
        BigDecimal taxRate = item.getTaxRate() != null ? item.getTaxRate() : DEFAULT_TAX_RATE;
        int isTaxInclusive = item.getIsTaxInclusive() != null && item.getIsTaxInclusive() == 1 ? 1 : 0;
        BigDecimal grossAmount = item.getQuantity().multiply(item.getUnitPrice());
        BigDecimal netAmount;
        BigDecimal taxAmount;
        if (isTaxInclusive == 1) {
            netAmount = grossAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.HALF_UP);
            taxAmount = netAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
            netAmount = grossAmount.subtract(taxAmount);
        } else {
            netAmount = grossAmount;
            taxAmount = grossAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        }
        return new TaxInfo(taxRate, isTaxInclusive, netAmount.setScale(2, RoundingMode.HALF_UP), taxAmount);
    }

    private record TaxInfo(BigDecimal taxRate, int isTaxInclusive, BigDecimal netAmount, BigDecimal taxAmount) {
    }
}