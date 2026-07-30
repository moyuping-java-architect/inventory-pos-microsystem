package com.psi.cashier.service.impl;

import com.psi.cashier.entity.OrderPendingItemEntity;
import com.psi.cashier.mapper.OrderPendingItemMapper;
import com.psi.cashier.service.OrderPendingItemService;
import com.psi.common.mybatis.util.BatchUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 挂单明细服务实现类
 * 实现挂单明细的数据操作
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPendingItemServiceImpl extends ServiceImpl<OrderPendingItemMapper, OrderPendingItemEntity> implements OrderPendingItemService {

    private final BatchUtils batchUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<OrderPendingItemEntity> items) {
        if (items != null && !items.isEmpty()) {
            batchUtils.saveBatch(this, items);
            log.info("批量保存挂单明细成功，数量：{}", items.size());
        }
    }

    @Override
    public List<OrderPendingItemEntity> getByPendingNo(String pendingNo) {
        return getBaseMapper().selectByPendingNo(pendingNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPendingNo(String pendingNo) {
        getBaseMapper().deleteByPendingNo(pendingNo);
        log.info("删除挂单明细成功，挂单号：{}", pendingNo);
    }
}