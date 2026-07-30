package com.psi.finance.service.impl;

import com.psi.finance.dto.FinancePayableDTO;
import com.psi.finance.dto.FinancePayablePayDTO;
import com.psi.finance.dto.FinancePayableQueryDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.entity.FinancePayableEntity;
import com.psi.finance.entity.FinancePayablePayEntity;
import com.psi.finance.mapper.FinancePayableMapper;
import com.psi.finance.mapper.FinancePayablePayMapper;
import com.psi.finance.mq.producer.FinanceSyncProducer;
import com.psi.finance.service.FinanceAccountService;
import com.psi.finance.service.FinancePayableService;
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

import java.math.BigDecimal;

@Slf4j
@Service
public class FinancePayableServiceImpl extends ServiceImpl<FinancePayableMapper, FinancePayableEntity> implements FinancePayableService {

    private final FinancePayablePayMapper financePayablePayMapper;
    private final FinanceAccountService financeAccountService;
    private final FinanceSyncProducer financeSyncProducer;

    public FinancePayableServiceImpl(FinancePayablePayMapper financePayablePayMapper,
                                     FinanceAccountService financeAccountService,
                                     @Lazy FinanceSyncProducer financeSyncProducer) {
        this.financePayablePayMapper = financePayablePayMapper;
        this.financeAccountService = financeAccountService;
        this.financeSyncProducer = financeSyncProducer;
    }

    @Override
    public boolean save(FinancePayableEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendPayableSync(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(FinancePayableEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendPayableSync(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(FinancePayableEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendPayableSync(entity);
        }
        return result;
    }

    private void sendPayableSync(FinancePayableEntity entity) {
        try {
            financeSyncProducer.sendFinancePayable(entity);
        } catch (Exception e) {
            log.error("应付数据实时同步发送失败", e);
        }
    }

    private void sendPayablePaySync(FinancePayablePayEntity entity) {
        try {
            financeSyncProducer.sendFinancePayablePay(entity);
        } catch (Exception e) {
            log.error("应付付款数据实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<FinancePayableDTO> getById(Long id) {
        FinancePayableEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, FinancePayableDTO.class));
    }

    @Override
    public PageResult<FinancePayableDTO> list(FinancePayableQueryDTO queryDTO) {
        Page<FinancePayableEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<FinancePayableEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getStoreCode() != null) {
            wrapper.eq(FinancePayableEntity::getStoreCode, queryDTO.getStoreCode());
        }
        if (queryDTO.getSupplierCode() != null) {
            wrapper.like(FinancePayableEntity::getSupplierCode, queryDTO.getSupplierCode());
        }
        if (queryDTO.getSupplierName() != null) {
            wrapper.like(FinancePayableEntity::getSupplierName, queryDTO.getSupplierName());
        }
        
        IPage<FinancePayableEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, FinancePayableDTO.class));
    }

    @Override
    @Transactional
    public CommonResult<FinancePayablePayDTO> pay(Long payableId, FinancePaySaveDTO saveDTO) {
        FinancePayableEntity entity = super.getById(payableId);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        entity.setPaidAmount(entity.getPaidAmount().add(saveDTO.getPayAmount()));
        entity.setRemainAmount(entity.getRemainAmount().subtract(saveDTO.getPayAmount()));
        super.updateById(entity);
        
        FinancePayablePayEntity payEntity = new FinancePayablePayEntity();
        payEntity.setStoreCode(entity.getStoreCode());
        payEntity.setStoreName(entity.getStoreName());
        payEntity.setPayableId(payableId);
        payEntity.setSupplierCode(entity.getSupplierCode());
        payEntity.setSupplierName(entity.getSupplierName());
        payEntity.setPayAmount(saveDTO.getPayAmount());
        payEntity.setPayMethod(saveDTO.getPayMethod());
        payEntity.setPayNo(saveDTO.getPayNo());
        payEntity.setPayDate(saveDTO.getPayDate());
        payEntity.setRemark(saveDTO.getRemark());
        financePayablePayMapper.insert(payEntity);
        sendPayablePaySync(payEntity);
        
        financeAccountService.addFlow(entity.getStoreCode(), saveDTO.getPayMethod(), 2, 
                                      saveDTO.getPayAmount(), null, "PAYABLE_PAY", 
                                      saveDTO.getPayNo(), "供应商付款");
        
        return CommonResult.success(BeanUtils.convert(payEntity, FinancePayablePayDTO.class));
    }
}
