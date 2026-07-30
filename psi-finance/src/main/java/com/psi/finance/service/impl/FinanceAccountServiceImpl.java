package com.psi.finance.service.impl;

import com.psi.finance.dto.FinanceAccountDTO;
import com.psi.finance.dto.FinanceAccountFlowDTO;
import com.psi.finance.dto.FinanceAccountFlowQueryDTO;
import com.psi.finance.entity.FinanceAccountEntity;
import com.psi.finance.entity.FinanceAccountFlowEntity;
import com.psi.finance.mapper.FinanceAccountFlowMapper;
import com.psi.finance.mapper.FinanceAccountMapper;
import com.psi.finance.mq.producer.FinanceSyncProducer;
import com.psi.finance.service.FinanceAccountService;
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
import java.util.Map;

@Slf4j
@Service
public class FinanceAccountServiceImpl extends ServiceImpl<FinanceAccountMapper, FinanceAccountEntity> implements FinanceAccountService {

    private final FinanceAccountFlowMapper financeAccountFlowMapper;
    private final FinanceSyncProducer financeSyncProducer;
    
    private static final Map<String, String> ACCOUNT_TYPE_MAP = Map.of(
        "CASH", "现金",
        "WECHAT", "微信",
        "ALIPAY", "支付宝",
        "BANK", "对公卡"
    );

    private static final Map<Integer, String> FLOW_TYPE_MAP = Map.of(
        1, "收入",
        2, "支出"
    );

    public FinanceAccountServiceImpl(FinanceAccountFlowMapper financeAccountFlowMapper,
                                     @Lazy FinanceSyncProducer financeSyncProducer) {
        this.financeAccountFlowMapper = financeAccountFlowMapper;
        this.financeSyncProducer = financeSyncProducer;
    }

    @Override
    public boolean save(FinanceAccountEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendAccountSync(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(FinanceAccountEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendAccountSync(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(FinanceAccountEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendAccountSync(entity);
        }
        return result;
    }

    private void sendAccountSync(FinanceAccountEntity entity) {
        try {
            financeSyncProducer.sendFinanceAccount(entity);
        } catch (Exception e) {
            log.error("财务账户实时同步发送失败", e);
        }
    }

    private void sendAccountFlowSync(FinanceAccountFlowEntity entity) {
        try {
            financeSyncProducer.sendFinanceAccountFlow(entity);
        } catch (Exception e) {
            log.error("财务账户流水实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<FinanceAccountDTO> getById(Long id) {
        FinanceAccountEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<FinanceAccountDTO> listByStore(String storeCode) {
        Page<FinanceAccountEntity> page = new Page<>(1, 100);
        LambdaQueryWrapper<FinanceAccountEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceAccountEntity::getStoreCode, storeCode);
        
        IPage<FinanceAccountEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    public PageResult<FinanceAccountFlowDTO> listFlow(FinanceAccountFlowQueryDTO queryDTO) {
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
        
        IPage<FinanceAccountFlowEntity> result = financeAccountFlowMapper.selectPage(page, wrapper);
        return PageResult.convert(result, entity -> convertFlowToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> addFlow(String storeCode, String accountType, Integer flowType, 
                                       BigDecimal amount, String sourceNo, String sourceType, 
                                       String payNo, String remark) {
        LambdaQueryWrapper<FinanceAccountEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceAccountEntity::getStoreCode, storeCode)
               .eq(FinanceAccountEntity::getAccountType, accountType);
        
        FinanceAccountEntity account = super.getOne(wrapper);
        if (account == null) {
            account = new FinanceAccountEntity();
            account.setStoreCode(storeCode);
            account.setAccountType(accountType);
            account.setAccountName(ACCOUNT_TYPE_MAP.getOrDefault(accountType, accountType));
            account.setBalance(BigDecimal.ZERO);
            super.save(account);
        }
        
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter;
        
        if (flowType == 1) {
            balanceAfter = balanceBefore.add(amount);
        } else {
            balanceAfter = balanceBefore.subtract(amount);
        }
        
        account.setBalance(balanceAfter);
        super.updateById(account);
        
        FinanceAccountFlowEntity flowEntity = new FinanceAccountFlowEntity();
        flowEntity.setStoreCode(storeCode);
        flowEntity.setAccountType(accountType);
        flowEntity.setAccountName(account.getAccountName());
        flowEntity.setFlowType(flowType);
        flowEntity.setInAmount(flowType == 1 ? amount : BigDecimal.ZERO);
        flowEntity.setOutAmount(flowType == 2 ? amount : BigDecimal.ZERO);
        flowEntity.setBalanceBefore(balanceBefore);
        flowEntity.setBalanceAfter(balanceAfter);
        flowEntity.setSourceNo(sourceNo);
        flowEntity.setSourceType(sourceType);
        flowEntity.setPayNo(payNo);
        flowEntity.setRemark(remark);
        
        financeAccountFlowMapper.insert(flowEntity);
        sendAccountFlowSync(flowEntity);
        
        return CommonResult.success();
    }

    private FinanceAccountDTO convertToDTO(FinanceAccountEntity entity) {
        FinanceAccountDTO dto = BeanUtils.convert(entity, FinanceAccountDTO.class);
        dto.setAccountTypeName(ACCOUNT_TYPE_MAP.getOrDefault(entity.getAccountType(), entity.getAccountType()));
        return dto;
    }

    private FinanceAccountFlowDTO convertFlowToDTO(FinanceAccountFlowEntity entity) {
        FinanceAccountFlowDTO dto = BeanUtils.convert(entity, FinanceAccountFlowDTO.class);
        dto.setAccountTypeName(ACCOUNT_TYPE_MAP.getOrDefault(entity.getAccountType(), entity.getAccountType()));
        dto.setFlowTypeName(FLOW_TYPE_MAP.getOrDefault(entity.getFlowType(), "未知"));
        return dto;
    }
}