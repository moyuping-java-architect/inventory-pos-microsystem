package com.psi.sale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.sale.entity.SaleOutSelfUseItemEntity;
import com.psi.sale.mapper.SaleOutSelfUseItemMapper;
import com.psi.sale.service.SaleOutSelfUseItemService;
import org.springframework.stereotype.Service;

@Service
public class SaleOutSelfUseItemServiceImpl extends ServiceImpl<SaleOutSelfUseItemMapper, SaleOutSelfUseItemEntity>
        implements SaleOutSelfUseItemService {
}
