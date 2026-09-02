package com.psi.cashier.controller;

import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.entity.OrderItemEntity;
import com.psi.cashier.entity.OrderPayEntity;
import com.psi.cashier.entity.RefundOrderEntity;
import com.psi.cashier.service.OrderMainService;
import com.psi.cashier.service.OrderItemService;
import com.psi.cashier.service.OrderPayService;
import com.psi.cashier.service.RefundOrderService;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售订单控制器
 * 提供销售订单的REST API接口
 * 
 * @author PSI
 * @version 1.0.0
 */
@RestController
@RequestMapping("/psi/cashier/order")
public class OrderController {

    private final OrderMainService orderMainService;
    private final OrderItemService orderItemService;
    private final OrderPayService orderPayService;
    private final RefundOrderService refundOrderService;
    private final com.psi.cashier.service.RefundOrderItemService refundOrderItemService;

    public OrderController(OrderMainService orderMainService, OrderItemService orderItemService, OrderPayService orderPayService, RefundOrderService refundOrderService, com.psi.cashier.service.RefundOrderItemService refundOrderItemService) {
        this.orderMainService = orderMainService;
        this.orderItemService = orderItemService;
        this.refundOrderService = refundOrderService;
        this.orderPayService = orderPayService;
        this.refundOrderItemService = refundOrderItemService;
    }

    @GetMapping("/page")
    public PageResult<OrderMainEntity> queryPage(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) Integer payStatus) {
        return orderMainService.queryPage(pageNum, pageSize, payStatus);
    }

    @GetMapping("/{orderNo}")
    public CommonResult<Map<String, Object>> getByOrderNo(@PathVariable String orderNo) {
        OrderMainEntity order = orderMainService.getByOrderNo(orderNo);
        if (order == null) {
            return CommonResult.fail("订单不存在");
        }
        List<OrderItemEntity> items = orderItemService.getByOrderNo(orderNo);
        List<OrderPayEntity> pays = orderPayService.getByOrderNo(orderNo);
        
        // 获取所有退货订单
        List<RefundOrderEntity> refundOrders = refundOrderService.getBySourceOrderNo(orderNo);
        
        // 计算已退款金额
        BigDecimal refundedAmount = BigDecimal.ZERO;
        if (refundOrders != null && !refundOrders.isEmpty()) {
            for (RefundOrderEntity refundOrder : refundOrders) {
                if (refundOrder.getTotalRefund() != null) {
                    refundedAmount = refundedAmount.add(refundOrder.getTotalRefund());
                }
            }
        }
        
        // 计算可退金额（订单金额 - 已退款金额）
        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal availableAmount = totalAmount.subtract(refundedAmount);
        
        // 计算每个商品的已退货数量
        Map<Integer, BigDecimal> itemRefundedQtyMap = new HashMap<>();
        if (refundOrders != null && !refundOrders.isEmpty()) {
            for (RefundOrderEntity refundOrder : refundOrders) {
                List<com.psi.cashier.entity.RefundOrderItemEntity> refundItems = 
                    refundOrderItemService.getByRefundNo(refundOrder.getRefundNo());
                if (refundItems != null) {
                    for (com.psi.cashier.entity.RefundOrderItemEntity item : refundItems) {
                        BigDecimal current = itemRefundedQtyMap.getOrDefault(item.getSkuId(), BigDecimal.ZERO);
                        itemRefundedQtyMap.put(item.getSkuId(), current.add(item.getRefundQuantity()));
                    }
                }
            }
        }
        
        // 为每个商品添加已退货数量和可退数量
        List<Map<String, Object>> itemsWithRefundInfo = new java.util.ArrayList<>();
        for (OrderItemEntity item : items) {
            Map<String, Object> itemMap = new HashMap<>();
            // 复制所有属性
            itemMap.put("id", item.getId());
            itemMap.put("tenantId", item.getTenantId());
            itemMap.put("shopCode", item.getShopCode());
            itemMap.put("posId", item.getPosId());
            itemMap.put("orderNo", item.getOrderNo());
            itemMap.put("bizType", item.getBizType());
            itemMap.put("skuId", item.getSkuId());
            itemMap.put("skuCode", item.getSkuCode());
            itemMap.put("barCode", item.getBarCode());
            itemMap.put("productName", item.getProductName());
            itemMap.put("saleUnitName", item.getSaleUnitName());
            itemMap.put("saleQuantity", item.getSaleQuantity());
            itemMap.put("unitPrice", item.getUnitPrice());
            itemMap.put("memberPrice", item.getMemberPrice());
            itemMap.put("subtotal", item.getSubtotal());
            itemMap.put("createBy", item.getCreateBy());
            itemMap.put("createTime", item.getCreateTime());
            itemMap.put("updateBy", item.getUpdateBy());
            itemMap.put("updateTime", item.getUpdateTime());
            
            // 添加退货相关信息
            BigDecimal refundedQty = itemRefundedQtyMap.getOrDefault(item.getSkuId(), BigDecimal.ZERO);
            itemMap.put("refundedQuantity", refundedQty);
            BigDecimal availableQty = item.getSaleQuantity().subtract(refundedQty);
            itemMap.put("availableQuantity", availableQty);
            
            itemsWithRefundInfo.add(itemMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("totalAmount", totalAmount);
        result.put("realAmount", order.getRealAmount());
        result.put("payStatus", order.getPayStatus());
        result.put("createTime", order.getCreateTime());
        result.put("items", itemsWithRefundInfo);
        result.put("pays", pays);
        result.put("refundedAmount", refundedAmount);
        result.put("availableAmount", availableAmount);
        
        return CommonResult.success(result);
    }

    @GetMapping("/{orderNo}/items")
    public CommonResult<List<OrderItemEntity>> getOrderItems(@PathVariable String orderNo) {
        List<OrderItemEntity> items = orderItemService.getByOrderNo(orderNo);
        return CommonResult.success(items);
    }

    @GetMapping("/{orderNo}/pays")
    public CommonResult<List<OrderPayEntity>> getOrderPays(@PathVariable String orderNo) {
        List<OrderPayEntity> pays = orderPayService.getByOrderNo(orderNo);
        return CommonResult.success(pays);
    }

    @GetMapping
    public CommonResult<PageResult<OrderMainEntity>> queryOrders(@RequestParam(defaultValue = "1") int pageNum,
                                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                                 @RequestParam(required = false) String orderNo,
                                                                 @RequestParam(required = false) String date) {
        PageResult<OrderMainEntity> result = orderMainService.queryOrders(pageNum, pageSize, orderNo, date);
        return CommonResult.success(result);
    }

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping
    public CommonResult<OrderMainEntity> create(@RequestBody OrderMainEntity order) {
        String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
        UserInfo userInfo = UserContext.get();
        
        // 设置创建人
        if (userInfo != null) {
            order.setCreateBy(userInfo.getUpdateUserId());
            order.setUpdateBy(userInfo.getUpdateUserId());
        } else {
            order.setCreateBy("1");
            order.setUpdateBy("1");
        }
        
        // 设置创建时间和更新时间
        order.setCreateTime(currentTime);
        order.setUpdateTime(currentTime);
        
        // 设置租户ID
        order.setTenantId(UserContext.getTenantId());
        
        orderMainService.save(order);
        return CommonResult.success(order);
    }

    @PutMapping("/{orderNo}")
    public CommonResult<OrderMainEntity> update(@PathVariable String orderNo, @RequestBody OrderMainEntity order) {
        OrderMainEntity existing = orderMainService.getByOrderNo(orderNo);
        if (existing == null) {
            return CommonResult.fail("订单不存在");
        }
        order.setId(existing.getId());
        order.setOrderNo(orderNo);
        
        // 设置更新人
        UserInfo userInfo = UserContext.get();
        if (userInfo != null) {
            order.setUpdateBy(userInfo.getUpdateUserId());
        } else {
            order.setUpdateBy("1");
        }
        
        // 设置更新时间
        order.setUpdateTime(LocalDateTime.now().format(DATETIME_FORMATTER));
        
        // 保留原有的创建人和创建时间
        order.setCreateBy(existing.getCreateBy());
        order.setCreateTime(existing.getCreateTime());
        
        orderMainService.updateById(order);
        return CommonResult.success(order);
    }

    @DeleteMapping("/{orderNo}")
    public CommonResult<Void> delete(@PathVariable String orderNo) {
        OrderMainEntity order = orderMainService.getByOrderNo(orderNo);
        if (order == null) {
            return CommonResult.fail("订单不存在");
        }
        orderMainService.removeById(order.getId());
        return CommonResult.success(null);
    }
}