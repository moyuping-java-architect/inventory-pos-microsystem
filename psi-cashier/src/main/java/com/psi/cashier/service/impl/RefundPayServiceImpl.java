package com.psi.cashier.service.impl;

import com.psi.cashier.entity.RefundPayEntity;
import com.psi.cashier.mapper.RefundPayMapper;
import com.psi.cashier.service.RefundPayService;
import com.psi.common.mybatis.util.BatchUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 退款支付服务实现类
 * 实现退款支付的数据操作
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundPayServiceImpl extends ServiceImpl<RefundPayMapper, RefundPayEntity> implements RefundPayService {

    private final BatchUtils batchUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<RefundPayEntity> pays) {
        if (pays != null && !pays.isEmpty()) {
            batchUtils.saveBatch(this, pays);
            log.info("批量保存退款支付记录成功，数量：{}", pays.size());
        }
    }

    @Override
    public List<RefundPayEntity> getByRefundNo(String refundNo) {
        return getBaseMapper().selectByRefundNo(refundNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRefundNo(String refundNo) {
        getBaseMapper().deleteByRefundNo(refundNo);
        log.info("删除退款支付记录成功，退货单号：{}", refundNo);
    }
}