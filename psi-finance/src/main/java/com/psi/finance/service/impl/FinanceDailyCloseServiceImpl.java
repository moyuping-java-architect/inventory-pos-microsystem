package com.psi.finance.service.impl;

import com.psi.finance.dto.FinanceDailyCloseDTO;
import com.psi.finance.dto.FinanceDailyCloseQueryDTO;
import com.psi.finance.entity.FinanceAccountEntity;
import com.psi.finance.entity.FinanceDailyCloseEntity;
import com.psi.finance.entity.FinanceDailyLedgerEntity;
import com.psi.finance.mapper.FinanceDailyCloseMapper;
import com.psi.finance.mq.producer.FinanceSyncProducer;
import com.psi.finance.service.FinanceDailyCloseService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FinanceDailyCloseServiceImpl extends ServiceImpl<FinanceDailyCloseMapper, FinanceDailyCloseEntity> implements FinanceDailyCloseService {

    private final FinanceDailyLedgerServiceImpl financeDailyLedgerService;
    private final FinanceAccountServiceImpl financeAccountService;
    private final FinanceSyncProducer financeSyncProducer;

    private static final Map<Integer, String> STATUS_MAP = Map.of(
        0, "未日结",
        1, "已日结",
        2, "已撤销"
    );

    public FinanceDailyCloseServiceImpl(FinanceDailyLedgerServiceImpl financeDailyLedgerService,
                                        FinanceAccountServiceImpl financeAccountService,
                                        @Lazy FinanceSyncProducer financeSyncProducer) {
        this.financeDailyLedgerService = financeDailyLedgerService;
        this.financeAccountService = financeAccountService;
        this.financeSyncProducer = financeSyncProducer;
    }

    @Override
    public boolean save(FinanceDailyCloseEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendDailyCloseSync(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(FinanceDailyCloseEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendDailyCloseSync(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(FinanceDailyCloseEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendDailyCloseSync(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<FinanceDailyCloseEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (FinanceDailyCloseEntity entity : entityList) {
                sendDailyCloseSync(entity);
            }
        }
        return result;
    }

    private void sendDailyCloseSync(FinanceDailyCloseEntity entity) {
        try {
            financeSyncProducer.sendFinanceDailyClose(entity);
        } catch (Exception e) {
            log.error("日结实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<FinanceDailyCloseDTO> getById(Long id) {
        FinanceDailyCloseEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<FinanceDailyCloseDTO> list(FinanceDailyCloseQueryDTO queryDTO) {
        Page<FinanceDailyCloseEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<FinanceDailyCloseEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getStoreCode() != null) {
            wrapper.eq(FinanceDailyCloseEntity::getStoreCode, queryDTO.getStoreCode());
        }
        if (queryDTO.getCloseDateStart() != null) {
            wrapper.ge(FinanceDailyCloseEntity::getCloseDate, queryDTO.getCloseDateStart());
        }
        if (queryDTO.getCloseDateEnd() != null) {
            wrapper.le(FinanceDailyCloseEntity::getCloseDate, queryDTO.getCloseDateEnd());
        }
        if (queryDTO.getCloseStatus() != null) {
            wrapper.eq(FinanceDailyCloseEntity::getCloseStatus, queryDTO.getCloseStatus());
        }
        
        wrapper.orderByDesc(FinanceDailyCloseEntity::getCloseDate);
        
        IPage<FinanceDailyCloseEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<FinanceDailyCloseDTO> close(String storeCode, String closeDate, String closeBy) {
        LambdaQueryWrapper<FinanceDailyCloseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceDailyCloseEntity::getStoreCode, storeCode)
               .eq(FinanceDailyCloseEntity::getCloseDate, closeDate);
        
        FinanceDailyCloseEntity existing = super.getOne(wrapper);
        if (existing != null && existing.getCloseStatus() == 1) {
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "该日期已完成日结");
        }
        
        LambdaQueryWrapper<FinanceDailyLedgerEntity> ledgerWrapper = new LambdaQueryWrapper<>();
        ledgerWrapper.eq(FinanceDailyLedgerEntity::getStoreCode, storeCode)
                     .eq(FinanceDailyLedgerEntity::getLedgerDate, closeDate);
        
        FinanceDailyLedgerEntity ledger = financeDailyLedgerService.getOne(ledgerWrapper);
        
        LambdaQueryWrapper<FinanceAccountEntity> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(FinanceAccountEntity::getStoreCode, storeCode);
        
        List<FinanceAccountEntity> accounts = financeAccountService.list(accountWrapper);
        
        FinanceDailyCloseEntity entity = new FinanceDailyCloseEntity();
        entity.setStoreCode(storeCode);
        
        if (ledger != null) {
            entity.setStoreName(ledger.getStoreName());
            entity.setSaleAmount(ledger.getSaleAmount());
            entity.setCostAmount(ledger.getCostAmount());
            entity.setProfitAmount(ledger.getProfitAmount());
            entity.setCashIn(ledger.getCashIn());
            entity.setCashOut(ledger.getCashOut());
            entity.setTransferIn(ledger.getTransferIn());
            entity.setTransferOut(ledger.getTransferOut());
            entity.setReceivableAmount(ledger.getReceivableAmount());
            entity.setPayableAmount(ledger.getPayableAmount());
        }
        
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (FinanceAccountEntity account : accounts) {
            BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            totalBalance = totalBalance.add(balance);
            
            switch (account.getAccountType()) {
                case "CASH":
                    entity.setCashBalance(balance);
                    break;
                case "WECHAT":
                    entity.setWechatBalance(balance);
                    break;
                case "ALIPAY":
                    entity.setAlipayBalance(balance);
                    break;
                case "BANK":
                    entity.setBankBalance(balance);
                    break;
            }
        }
        entity.setTotalBalance(totalBalance);
        
        entity.setCloseDate(closeDate);
        entity.setCloseBy(closeBy);
        entity.setCloseTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        entity.setCloseStatus(1);
        
        if (existing != null) {
            entity.setId(existing.getId());
            this.updateById(entity);
        } else {
            this.save(entity);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<FinanceDailyCloseDTO> reopen(String storeCode, String closeDate) {
        LambdaQueryWrapper<FinanceDailyCloseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceDailyCloseEntity::getStoreCode, storeCode)
               .eq(FinanceDailyCloseEntity::getCloseDate, closeDate);
        
        FinanceDailyCloseEntity entity = super.getOne(wrapper);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        entity.setCloseStatus(2);
        this.updateById(entity);
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public CommonResult<FinanceDailyCloseDTO> getByDate(String storeCode, String closeDate) {
        LambdaQueryWrapper<FinanceDailyCloseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceDailyCloseEntity::getStoreCode, storeCode)
               .eq(FinanceDailyCloseEntity::getCloseDate, closeDate);
        
        FinanceDailyCloseEntity entity = super.getOne(wrapper);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    private FinanceDailyCloseDTO convertToDTO(FinanceDailyCloseEntity entity) {
        FinanceDailyCloseDTO dto = BeanUtils.convert(entity, FinanceDailyCloseDTO.class);
        dto.setCloseStatusName(STATUS_MAP.getOrDefault(entity.getCloseStatus(), "未知"));
        return dto;
    }
}