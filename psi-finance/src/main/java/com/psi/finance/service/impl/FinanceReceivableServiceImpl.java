package com.psi.finance.service.impl;

import com.psi.finance.dto.FinanceReceivableDTO;
import com.psi.finance.dto.FinanceReceivablePayDTO;
import com.psi.finance.dto.FinanceReceivableQueryDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.entity.FinanceReceivableEntity;
import com.psi.finance.entity.FinanceReceivablePayEntity;
import com.psi.finance.mapper.FinanceReceivableMapper;
import com.psi.finance.mapper.FinanceReceivablePayMapper;
import com.psi.finance.mq.producer.FinanceSyncProducer;
import com.psi.finance.service.FinanceAccountService;
import com.psi.finance.service.FinanceReceivableService;
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
public class FinanceReceivableServiceImpl extends ServiceImpl<FinanceReceivableMapper, FinanceReceivableEntity> implements FinanceReceivableService {

    private final FinanceReceivablePayMapper financeReceivablePayMapper;
    private final FinanceAccountService financeAccountService;
    private final FinanceSyncProducer financeSyncProducer;

    public FinanceReceivableServiceImpl(FinanceReceivablePayMapper financeReceivablePayMapper,
                                        FinanceAccountService financeAccountService,
                                        @Lazy FinanceSyncProducer financeSyncProducer) {
        this.financeReceivablePayMapper = financeReceivablePayMapper;
        this.financeAccountService = financeAccountService;
        this.financeSyncProducer = financeSyncProducer;
    }

    @Override
    public boolean save(FinanceReceivableEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendReceivableSync(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(FinanceReceivableEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendReceivableSync(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(FinanceReceivableEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendReceivableSync(entity);
        }
        return result;
    }

    private void sendReceivableSync(FinanceReceivableEntity entity) {
        try {
            financeSyncProducer.sendFinanceReceivable(entity);
        } catch (Exception e) {
            log.error("应收数据实时同步发送失败", e);
        }
    }

    private void sendReceivablePaySync(FinanceReceivablePayEntity entity) {
        try {
            financeSyncProducer.sendFinanceReceivablePay(entity);
        } catch (Exception e) {
            log.error("应收付款数据实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<FinanceReceivableDTO> getById(Long id) {
        FinanceReceivableEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, FinanceReceivableDTO.class));
    }

    @Override
    public PageResult<FinanceReceivableDTO> list(FinanceReceivableQueryDTO queryDTO) {
        Page<FinanceReceivableEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<FinanceReceivableEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getStoreCode() != null) {
            wrapper.eq(FinanceReceivableEntity::getStoreCode, queryDTO.getStoreCode());
        }
        if (queryDTO.getCustomerCode() != null) {
            wrapper.like(FinanceReceivableEntity::getCustomerCode, queryDTO.getCustomerCode());
        }
        if (queryDTO.getCustomerName() != null) {
            wrapper.like(FinanceReceivableEntity::getCustomerName, queryDTO.getCustomerName());
        }
        
        IPage<FinanceReceivableEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, FinanceReceivableDTO.class));
    }

    @Override
    @Transactional
    public CommonResult<FinanceReceivablePayDTO> pay(Long receivableId, FinancePaySaveDTO saveDTO) {
        FinanceReceivableEntity entity = super.getById(receivableId);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        if (entity.getRemainAmount().compareTo(saveDTO.getPayAmount()) < 0) {
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "还款金额超过剩余欠款");
        }
        
        entity.setPaidAmount(entity.getPaidAmount().add(saveDTO.getPayAmount()));
        entity.setRemainAmount(entity.getRemainAmount().subtract(saveDTO.getPayAmount()));
        super.updateById(entity);
        
        FinanceReceivablePayEntity payEntity = new FinanceReceivablePayEntity();
        payEntity.setStoreCode(entity.getStoreCode());
        payEntity.setStoreName(entity.getStoreName());
        payEntity.setReceivableId(receivableId);
        payEntity.setCustomerCode(entity.getCustomerCode());
        payEntity.setCustomerName(entity.getCustomerName());
        payEntity.setPayAmount(saveDTO.getPayAmount());
        payEntity.setPayMethod(saveDTO.getPayMethod());
        payEntity.setPayNo(saveDTO.getPayNo());
        payEntity.setPayDate(saveDTO.getPayDate());
        payEntity.setRemark(saveDTO.getRemark());
        financeReceivablePayMapper.insert(payEntity);
        sendReceivablePaySync(payEntity);
        
        financeAccountService.addFlow(entity.getStoreCode(), saveDTO.getPayMethod(), 1, 
                                      saveDTO.getPayAmount(), null, "RECEIVABLE_PAY", 
                                      saveDTO.getPayNo(), "客户还款");
        
        return CommonResult.success(BeanUtils.convert(payEntity, FinanceReceivablePayDTO.class));
    }
}