package com.psi.finance.service.impl;

import com.psi.finance.dto.FinancePayablePayDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.entity.FinancePayablePayEntity;
import com.psi.finance.mapper.FinancePayablePayMapper;
import com.psi.finance.mq.producer.FinanceSyncProducer;
import com.psi.finance.service.FinancePayablePayService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Slf4j
@Service
public class FinancePayablePayServiceImpl extends ServiceImpl<FinancePayablePayMapper, FinancePayablePayEntity> implements FinancePayablePayService {

    private final FinanceSyncProducer financeSyncProducer;

    public FinancePayablePayServiceImpl(@Lazy FinanceSyncProducer financeSyncProducer) {
        this.financeSyncProducer = financeSyncProducer;
    }

    @Override
    public boolean save(FinancePayablePayEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendPayablePaySync(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(FinancePayablePayEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendPayablePaySync(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(FinancePayablePayEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendPayablePaySync(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<FinancePayablePayEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (FinancePayablePayEntity entity : entityList) {
                sendPayablePaySync(entity);
            }
        }
        return result;
    }

    private void sendPayablePaySync(FinancePayablePayEntity entity) {
        try {
            financeSyncProducer.sendFinancePayablePay(entity);
        } catch (Exception e) {
            log.error("应付付款实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<FinancePayablePayDTO> getById(Long id) {
        FinancePayablePayEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, FinancePayablePayDTO.class));
    }

    @Override
    public PageResult<FinancePayablePayDTO> list(Long payableId) {
        Page<FinancePayablePayEntity> page = new Page<>(1, 100);
        LambdaQueryWrapper<FinancePayablePayEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancePayablePayEntity::getPayableId, payableId);
        IPage<FinancePayablePayEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, FinancePayablePayDTO.class));
    }

    @Override
    @Transactional
    public CommonResult<FinancePayablePayDTO> pay(FinancePaySaveDTO saveDTO) {
        FinancePayablePayEntity entity = BeanUtils.convert(saveDTO, FinancePayablePayEntity.class);
        this.save(entity);
        return CommonResult.success(BeanUtils.convert(entity, FinancePayablePayDTO.class));
    }
}
