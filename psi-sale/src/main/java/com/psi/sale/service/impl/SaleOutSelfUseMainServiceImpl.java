package com.psi.sale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.feign.DocFeignResponse.DocFeignItemResponse;
import com.psi.common.util.BeanUtils;
import com.psi.sale.entity.SaleOutSelfUseItemEntity;
import com.psi.sale.entity.SaleOutSelfUseMainEntity;
import com.psi.sale.mapper.SaleOutSelfUseItemMapper;
import com.psi.sale.mapper.SaleOutSelfUseMainMapper;
import com.psi.sale.service.SaleOutSelfUseItemService;
import com.psi.sale.service.SaleOutSelfUseMainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SaleOutSelfUseMainServiceImpl extends ServiceImpl<SaleOutSelfUseMainMapper, SaleOutSelfUseMainEntity>
        implements SaleOutSelfUseMainService {

    private final SaleOutSelfUseItemService saleOutSelfUseItemService;

    public SaleOutSelfUseMainServiceImpl(SaleOutSelfUseItemService saleOutSelfUseItemService) {
        this.saleOutSelfUseItemService = saleOutSelfUseItemService;
    }

    @Override
    public SaleOutSelfUseMainEntity getDetail(Long id) {
        SaleOutSelfUseMainEntity main = getById(id);
        if (main != null) {
            List<SaleOutSelfUseItemEntity> items = saleOutSelfUseItemService.lambdaQuery()
                    .eq(SaleOutSelfUseItemEntity::getOutId, id)
                    .eq(SaleOutSelfUseItemEntity::getDelFlag, 0)
                    .list();
            main.setItems(items);
        }
        return main;
    }

    @Override
    @Transactional
    public SaleOutSelfUseMainEntity saveFromDraft(DocFeignResponse doc) {
        SaleOutSelfUseMainEntity main = new SaleOutSelfUseMainEntity();
        main.setOutNo(doc.getDocNo());
        main.setDocName(doc.getDocName());
        main.setCustomerCode(doc.getPartnerCode());
        main.setCustomerName(doc.getPartnerName());
        main.setWarehouseCode(doc.getWarehouseCode());
        main.setWarehouseName(doc.getWarehouseName());
        main.setOutDate(doc.getDocDate());
        main.setRemark(doc.getRemark());
        main.setStatus(2);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        List<SaleOutSelfUseItemEntity> items = new ArrayList<>();

        if (doc.getItems() != null && !doc.getItems().isEmpty()) {
            int itemNo = 1;
            for (DocFeignItemResponse docItem : doc.getItems()) {
                SaleOutSelfUseItemEntity item = new SaleOutSelfUseItemEntity();
                item.setOutNo(doc.getDocNo());
                item.setItemNo(itemNo++);
                item.setGoodsId(docItem.getGoodsId());
                item.setGoodsCode(docItem.getGoodsCode());
                item.setSkuCode(docItem.getSkuCode());
                item.setSkuName(docItem.getSkuName());
                item.setGoodsName(docItem.getGoodsName());
                item.setGoodsSpec(docItem.getGoodsSpec());
                item.setUnitCode(docItem.getGoodsUnit());
                item.setConversionRate(docItem.getConversionRate());
                item.setOutQuantity(docItem.getQuantity());
                item.setUnitPrice(docItem.getUnitPrice());
                item.setTaxRate(docItem.getTaxRate());
                item.setBatchNo(docItem.getBatchNo());
                item.setExpireDate(docItem.getExpiryDate());
                item.setRemark(docItem.getRemark());
                item.setStatus(2);

                BigDecimal itemAmount = docItem.getQuantity().multiply(docItem.getUnitPrice());
                BigDecimal itemTax = itemAmount.multiply(docItem.getTaxRate() != null ? docItem.getTaxRate() : BigDecimal.ZERO);
                item.setAmount(itemAmount);
                item.setTaxAmount(itemTax);

                totalAmount = totalAmount.add(itemAmount);
                taxAmount = taxAmount.add(itemTax);
                items.add(item);
            }
        }

        main.setTotalAmount(totalAmount);
        main.setTaxAmount(taxAmount);
        super.save(main);

        for (SaleOutSelfUseItemEntity item : items) {
            item.setOutId(main.getId());
        }
        if (!items.isEmpty()) {
            saleOutSelfUseItemService.saveBatch(items);
        }

        log.info("自用出库单正式数据已生成: outNo={}, totalAmount={}", main.getOutNo(), main.getTotalAmount());
        return main;
    }
}
