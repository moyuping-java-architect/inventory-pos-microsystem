package com.psi.sale.service.impl;

import com.psi.sale.entity.SaleOutItemEntity;
import com.psi.sale.mapper.SaleOutItemMapper;
import com.psi.sale.service.SaleOutItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 销售出库明细服务实现
 */
@Service
public class SaleOutItemServiceImpl extends ServiceImpl<SaleOutItemMapper, SaleOutItemEntity> implements SaleOutItemService {
}