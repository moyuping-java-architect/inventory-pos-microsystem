package com.psi.finance.service.impl;

import com.psi.finance.dto.FinanceAccountFlowDTO;
import com.psi.finance.dto.FinanceAccountFlowQueryDTO;
import com.psi.finance.entity.FinanceAccountFlowEntity;
import com.psi.finance.mapper.FinanceAccountFlowMapper;
import com.psi.finance.mq.producer.FinanceSyncProducer;
import com.psi.finance.service.FinanceAccountFlowService;
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
public class FinanceAccountFlowServiceImpl extends ServiceImpl<FinanceAccountFlowMapper, FinanceAccountFlowEntity> implements FinanceAccountFlowService {

    private final FinanceSyncProducer financeSyncProducer;

    public FinanceAccountFlowServiceImpl(@Lazy FinanceSyncProducer financeSyncProducer) {
        this.financeSyncProducer = financeSyncProducer;
    }

    @Override
    public boolean save(FinanceAccountFlowEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendAccountFlowSync(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(FinanceAccountFlowEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendAccountFlowSync(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(FinanceAccountFlowEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendAccountFlowSync(entity);
        }
        return result;
    }

    private void sendAccountFlowSync(FinanceAccountFlowEntity entity) {
        try {
            financeSyncProducer.sendFinanceAccountFlow(entity);
        } catch (Exception e) {
            log.error("财务账户流水实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<FinanceAccountFlowDTO> getById(Long id) {
        FinanceAccountFlowEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, FinanceAccountFlowDTO.class));
    }

    @Override
    public PageResult<FinanceAccountFlowDTO> list(FinanceAccountFlowQueryDTO queryDTO) {
        Page<FinanceAccountFlowEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<FinanceAccountFlowEntity> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getStoreCode() != null) {
            wrapper.eq(FinanceAccountFlowEntity::getStoreCode, queryDTO.getStoreCode());
        }
        if (queryDTO.getAccountType() != null) {
            wrapper.eq(FinanceAccountFlowEntity::getAccountType, queryDTO.getAccountType());
        }
        if (queryDTO.getFlowType() != null) {
            wrapper.eq(FinanceAccountFlowEntity::getFlowType, queryDTO.getFlowType());
        }
        wrapper.orderByDesc(FinanceAccountFlowEntity::getCreateTime);
        IPage<FinanceAccountFlowEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, FinanceAccountFlowDTO.class));
    }

    @Override
    @Transactional
    public CommonResult<Void> addFlow(String storeCode, String storeName, String accountType, String accountName,
                                                 Integer flowType, String inAmount, String outAmount,
                                                 String balanceBefore, String balanceAfter,
                                                 String sourceNo, String sourceType, String payNo, String remark) {
        FinanceAccountFlowEntity entity = new FinanceAccountFlowEntity();
        entity.setStoreCode(storeCode);
        entity.setStoreName(storeName);
        entity.setAccountType(accountType);
        entity.setAccountName(accountName);
        entity.setFlowType(flowType);
        entity.setInAmount(inAmount != null ? new BigDecimal(inAmount) : BigDecimal.ZERO);
        entity.setOutAmount(outAmount != null ? new BigDecimal(outAmount) : BigDecimal.ZERO);
        entity.setBalanceBefore(balanceBefore != null ? new BigDecimal(balanceBefore) : BigDecimal.ZERO);
        entity.setBalanceAfter(balanceAfter != null ? new BigDecimal(balanceAfter) : BigDecimal.ZERO);
        entity.setSourceNo(sourceNo);
        entity.setSourceType(sourceType);
        entity.setPayNo(payNo);
        entity.setRemark(remark);
        super.save(entity);
        return CommonResult.success();
    }
}
