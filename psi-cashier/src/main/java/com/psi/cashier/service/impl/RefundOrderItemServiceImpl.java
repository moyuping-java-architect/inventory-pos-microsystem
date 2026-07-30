package com.psi.cashier.service.impl;

import com.psi.cashier.entity.RefundOrderItemEntity;
import com.psi.cashier.mapper.RefundOrderItemMapper;
import com.psi.cashier.service.RefundOrderItemService;
import com.psi.common.mybatis.util.BatchUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 退货明细服务实现类
 * 实现退货明细的数据操作
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundOrderItemServiceImpl extends ServiceImpl<RefundOrderItemMapper, RefundOrderItemEntity> implements RefundOrderItemService {

    private final BatchUtils batchUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<RefundOrderItemEntity> items) {
        if (items != null && !items.isEmpty()) {
            batchUtils.saveBatch(this, items);
            log.info("批量保存退货明细成功，数量：{}", items.size());
        }
    }

    @Override
    public List<RefundOrderItemEntity> getByRefundNo(String refundNo) {
        return getBaseMapper().selectByRefundNo(refundNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRefundNo(String refundNo) {
        getBaseMapper().deleteByRefundNo(refundNo);
        log.info("删除退货明细成功，退货单号：{}", refundNo);
    }

    @Override
    public List<RefundOrderItemEntity> getBySourceOrderNo(String sourceOrderNo) {
        return getBaseMapper().selectBySourceOrderNo(sourceOrderNo);
    }
}