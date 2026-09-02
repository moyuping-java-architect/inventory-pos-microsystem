package com.psi.sale.service.impl;

import com.psi.sale.entity.SaleOrderItemEntity;
import com.psi.sale.mapper.SaleOrderItemMapper;
import com.psi.sale.service.SaleOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 销售订单明细服务实现
 */
@Service
public class SaleOrderItemServiceImpl extends ServiceImpl<SaleOrderItemMapper, SaleOrderItemEntity> implements SaleOrderItemService {
}