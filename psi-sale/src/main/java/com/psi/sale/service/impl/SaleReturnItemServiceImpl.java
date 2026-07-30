package com.psi.sale.service.impl;

import com.psi.sale.entity.SaleReturnItemEntity;
import com.psi.sale.mapper.SaleReturnItemMapper;
import com.psi.sale.service.SaleReturnItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 销售退货明细服务实现
 */
@Service
public class SaleReturnItemServiceImpl extends ServiceImpl<SaleReturnItemMapper, SaleReturnItemEntity> implements SaleReturnItemService {
}