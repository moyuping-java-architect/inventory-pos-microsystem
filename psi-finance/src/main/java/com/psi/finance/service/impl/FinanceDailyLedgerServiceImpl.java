package com.psi.finance.service.impl;

import com.psi.finance.dto.FinanceDailyLedgerDTO;
import com.psi.finance.dto.FinanceDailyLedgerQueryDTO;
import com.psi.finance.entity.FinanceDailyLedgerEntity;
import com.psi.finance.mapper.FinanceDailyLedgerMapper;
import com.psi.finance.mq.producer.FinanceSyncProducer;
import com.psi.finance.service.FinanceDailyLedgerService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FinanceDailyLedgerServiceImpl extends ServiceImpl<FinanceDailyLedgerMapper, FinanceDailyLedgerEntity> implements FinanceDailyLedgerService {

    private final FinanceSyncProducer financeSyncProducer;

    public FinanceDailyLedgerServiceImpl(@Lazy FinanceSyncProducer financeSyncProducer) {
        this.financeSyncProducer = financeSyncProducer;
    }

    @Override
    public boolean save(FinanceDailyLedgerEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendDailyLedgerSync(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(FinanceDailyLedgerEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendDailyLedgerSync(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(FinanceDailyLedgerEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendDailyLedgerSync(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<FinanceDailyLedgerEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (FinanceDailyLedgerEntity entity : entityList) {
                sendDailyLedgerSync(entity);
            }
        }
        return result;
    }

    private void sendDailyLedgerSync(FinanceDailyLedgerEntity entity) {
        try {
            financeSyncProducer.sendFinanceDailyLedger(entity);
        } catch (Exception e) {
            log.error("日报实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<FinanceDailyLedgerDTO> getById(Long id) {
        FinanceDailyLedgerEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, FinanceDailyLedgerDTO.class));
    }

    @Override
    public PageResult<FinanceDailyLedgerDTO> list(FinanceDailyLedgerQueryDTO queryDTO) {
        Page<FinanceDailyLedgerEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<FinanceDailyLedgerEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getStoreCode() != null) {
            wrapper.eq(FinanceDailyLedgerEntity::getStoreCode, queryDTO.getStoreCode());
        }
        
        wrapper.orderByDesc(FinanceDailyLedgerEntity::getLedgerDate);
        
        IPage<FinanceDailyLedgerEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, FinanceDailyLedgerDTO.class));
    }

    @Override
    public CommonResult<FinanceDailyLedgerDTO> getByDate(String storeCode, String ledgerDate) {
        LambdaQueryWrapper<FinanceDailyLedgerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceDailyLedgerEntity::getStoreCode, storeCode)
               .eq(FinanceDailyLedgerEntity::getLedgerDate, ledgerDate);
        
        FinanceDailyLedgerEntity entity = super.getOne(wrapper);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, FinanceDailyLedgerDTO.class));
    }

    @Override
    public CommonResult<Map<String, BigDecimal>> summary(String storeCode, String startDate, String endDate) {
        LambdaQueryWrapper<FinanceDailyLedgerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceDailyLedgerEntity::getStoreCode, storeCode)
               .ge(FinanceDailyLedgerEntity::getLedgerDate, startDate)
               .le(FinanceDailyLedgerEntity::getLedgerDate, endDate);
        
        List<FinanceDailyLedgerEntity> list = super.list(wrapper);
        
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalSale", BigDecimal.ZERO);
        summary.put("totalCost", BigDecimal.ZERO);
        summary.put("totalProfit", BigDecimal.ZERO);
        summary.put("totalCashIn", BigDecimal.ZERO);
        summary.put("totalCashOut", BigDecimal.ZERO);
        
        for (FinanceDailyLedgerEntity entity : list) {
            summary.put("totalSale", summary.get("totalSale").add(entity.getSaleAmount() != null ? entity.getSaleAmount() : BigDecimal.ZERO));
            summary.put("totalCost", summary.get("totalCost").add(entity.getCostAmount() != null ? entity.getCostAmount() : BigDecimal.ZERO));
            summary.put("totalProfit", summary.get("totalProfit").add(entity.getProfitAmount() != null ? entity.getProfitAmount() : BigDecimal.ZERO));
            summary.put("totalCashIn", summary.get("totalCashIn").add(entity.getCashIn() != null ? entity.getCashIn() : BigDecimal.ZERO));
            summary.put("totalCashOut", summary.get("totalCashOut").add(entity.getCashOut() != null ? entity.getCashOut() : BigDecimal.ZERO));
        }
        
        return CommonResult.success(summary);
    }

    @Override
    public CommonResult<Map<String, BigDecimal>> summaryAll(String startDate, String endDate) {
        LambdaQueryWrapper<FinanceDailyLedgerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FinanceDailyLedgerEntity::getLedgerDate, startDate)
               .le(FinanceDailyLedgerEntity::getLedgerDate, endDate);
        
        List<FinanceDailyLedgerEntity> list = super.list(wrapper);
        
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalSale", BigDecimal.ZERO);
        summary.put("totalCost", BigDecimal.ZERO);
        summary.put("totalProfit", BigDecimal.ZERO);
        summary.put("totalCashIn", BigDecimal.ZERO);
        summary.put("totalCashOut", BigDecimal.ZERO);
        
        for (FinanceDailyLedgerEntity entity : list) {
            summary.put("totalSale", summary.get("totalSale").add(entity.getSaleAmount() != null ? entity.getSaleAmount() : BigDecimal.ZERO));
            summary.put("totalCost", summary.get("totalCost").add(entity.getCostAmount() != null ? entity.getCostAmount() : BigDecimal.ZERO));
            summary.put("totalProfit", summary.get("totalProfit").add(entity.getProfitAmount() != null ? entity.getProfitAmount() : BigDecimal.ZERO));
            summary.put("totalCashIn", summary.get("totalCashIn").add(entity.getCashIn() != null ? entity.getCashIn() : BigDecimal.ZERO));
            summary.put("totalCashOut", summary.get("totalCashOut").add(entity.getCashOut() != null ? entity.getCashOut() : BigDecimal.ZERO));
        }
        
        return CommonResult.success(summary);
    }

    @Override
    @Transactional
    public CommonResult<Void> recordSale(String outNo, String customerCode, String customerName, 
                                          BigDecimal totalAmount, BigDecimal payAmount, String payType, String warehouseCode) {
        log.info("记录销售日报，单号: {}, 客户: {}, 金额: {}", outNo, customerName, totalAmount);
        
        String ledgerDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        LambdaQueryWrapper<FinanceDailyLedgerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceDailyLedgerEntity::getStoreCode, warehouseCode)
               .eq(FinanceDailyLedgerEntity::getLedgerDate, ledgerDate);
        
        FinanceDailyLedgerEntity entity = super.getOne(wrapper);
        
        if (entity == null) {
            entity = new FinanceDailyLedgerEntity();
            entity.setStoreCode(warehouseCode);
            entity.setLedgerDate(ledgerDate);
            entity.setSaleAmount(totalAmount);
            entity.setCashIn(payAmount);
            entity.setReceivableAmount(totalAmount.subtract(payAmount));
            this.save(entity);
        } else {
            entity.setSaleAmount(entity.getSaleAmount().add(totalAmount));
            entity.setCashIn(entity.getCashIn().add(payAmount));
            entity.setReceivableAmount(entity.getReceivableAmount().add(totalAmount.subtract(payAmount)));
            this.updateById(entity);
        }
        
        log.info("销售日报记录完成，单号: {}", outNo);
        return CommonResult.success();
    }
}