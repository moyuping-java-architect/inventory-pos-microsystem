package com.trademaster.service;

import com.trademaster.entity.Goods;
import com.trademaster.entity.SaleOrder;
import com.trademaster.mapper.SaleOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CashierService {
    private final SaleOrderMapper saleOrderMapper;
    private final GoodsService goodsService;
    private final CustomerService customerService;

    public CashierService(SaleOrderMapper saleOrderMapper, GoodsService goodsService, CustomerService customerService) {
        this.saleOrderMapper = saleOrderMapper;
        this.goodsService = goodsService;
        this.customerService = customerService;
    }

    @Transactional
    public SaleOrder createOrder(com.trademaster.dto.SaleOrderDTO dto) {
        SaleOrder order = new SaleOrder();
        order.setOrderNo("SO" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 4));
        order.setCustomerId(dto.getCustomerId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setDiscountAmount(dto.getDiscountAmount());
        order.setActualAmount(dto.getActualAmount());
        order.setPaymentType(dto.getPaymentType());
        order.setStatus("COMPLETED");
        order.setCashierId(dto.getCashierId());
        order.setRemark(dto.getRemark());

        saleOrderMapper.insert(order);

        for (com.trademaster.dto.SaleOrderItemDTO item : dto.getItems()) {
            goodsService.updateStock(item.getGoodsId(), item.getQty().negate());
        }

        if (dto.getCustomerId() != null) {
            customerService.updateBalance(dto.getCustomerId(), dto.getActualAmount().negate());
            int points = dto.getActualAmount().intValue();
            customerService.updatePoints(dto.getCustomerId(), points);
            customerService.updateBalance(dto.getCustomerId(), dto.getActualAmount().negate());
        }

        return order;
    }

    public SaleOrder findByOrderNo(String orderNo) {
        return saleOrderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SaleOrder>().eq("order_no", orderNo));
    }
}
