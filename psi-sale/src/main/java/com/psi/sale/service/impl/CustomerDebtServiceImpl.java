package com.psi.sale.service.impl;

import com.psi.sale.dto.CustomerDebtDTO;
import com.psi.sale.dto.CustomerDebtQueryDTO;
import com.psi.sale.entity.CustomerDebtDetailEntity;
import com.psi.sale.entity.CustomerDebtEntity;
import com.psi.sale.mapper.CustomerDebtDetailMapper;
import com.psi.sale.mapper.CustomerDebtMapper;
import com.psi.sale.service.CustomerDebtService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CustomerDebtServiceImpl extends ServiceImpl<CustomerDebtMapper, CustomerDebtEntity> implements CustomerDebtService {

    private final CustomerDebtDetailMapper customerDebtDetailMapper;

    public CustomerDebtServiceImpl(CustomerDebtDetailMapper customerDebtDetailMapper) {
        this.customerDebtDetailMapper = customerDebtDetailMapper;
    }

    @Override
    public CommonResult<CustomerDebtDTO> getById(Long id) {
        CustomerDebtEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, CustomerDebtDTO.class));
    }

    @Override
    public PageResult<CustomerDebtDTO> list(CustomerDebtQueryDTO queryDTO) {
        Page<CustomerDebtEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<CustomerDebtEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getCustomerCode() != null) {
            wrapper.like(CustomerDebtEntity::getCustomerCode, queryDTO.getCustomerCode());
        }
        if (queryDTO.getCustomerName() != null) {
            wrapper.like(CustomerDebtEntity::getCustomerName, queryDTO.getCustomerName());
        }
        
        IPage<CustomerDebtEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, CustomerDebtDTO.class));
    }

    @Override
    public CommonResult<CustomerDebtDTO> getByCustomerId(Long customerId) {
        LambdaQueryWrapper<CustomerDebtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerDebtEntity::getCustomerId, customerId);
        CustomerDebtEntity entity = super.getOne(wrapper);
        
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, CustomerDebtDTO.class));
    }

    @Override
    @Transactional
    public void addDebt(Long customerId, String customerCode, String customerName, String billNo,
                        Integer billType, BigDecimal amount, String debtDate, String dueDate) {
        LambdaQueryWrapper<CustomerDebtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerDebtEntity::getCustomerId, customerId);
        CustomerDebtEntity debtEntity = super.getOne(wrapper);
        
        if (debtEntity == null) {
            debtEntity = new CustomerDebtEntity();
            debtEntity.setCustomerId(customerId);
            debtEntity.setCustomerCode(customerCode);
            debtEntity.setCustomerName(customerName);
            debtEntity.setTotalDebtAmount(BigDecimal.ZERO);
            debtEntity.setPaidAmount(BigDecimal.ZERO);
            debtEntity.setPendingAmount(BigDecimal.ZERO);
            super.save(debtEntity);
        }
        
        debtEntity.setTotalDebtAmount(debtEntity.getTotalDebtAmount().add(amount));
        debtEntity.setPendingAmount(debtEntity.getPendingAmount().add(amount));
        if (debtEntity.getCreditLimit() != null) {
            debtEntity.setAvailableCredit(debtEntity.getCreditLimit().subtract(debtEntity.getPendingAmount()));
        }
        super.updateById(debtEntity);
        
        CustomerDebtDetailEntity detailEntity = new CustomerDebtDetailEntity();
        detailEntity.setCustomerId(customerId);
        detailEntity.setCustomerCode(customerCode);
        detailEntity.setCustomerName(customerName);
        detailEntity.setBillType(billType);
        detailEntity.setBillNo(billNo);
        detailEntity.setDebtAmount(amount);
        detailEntity.setPaidAmount(BigDecimal.ZERO);
        detailEntity.setPendingAmount(amount);
        detailEntity.setDebtDate(debtDate);
        detailEntity.setDueDate(dueDate);
        detailEntity.setStatus(1);
        customerDebtDetailMapper.insert(detailEntity);
    }

    @Override
    @Transactional
    public void payDebt(Long customerId, BigDecimal amount) {
        LambdaQueryWrapper<CustomerDebtEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerDebtEntity::getCustomerId, customerId);
        CustomerDebtEntity debtEntity = super.getOne(wrapper);
        
        if (debtEntity != null) {
            debtEntity.setPaidAmount(debtEntity.getPaidAmount().add(amount));
            debtEntity.setPendingAmount(debtEntity.getPendingAmount().subtract(amount));
            if (debtEntity.getCreditLimit() != null) {
                debtEntity.setAvailableCredit(debtEntity.getCreditLimit().subtract(debtEntity.getPendingAmount()));
            }
            super.updateById(debtEntity);
        }
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        CustomerDebtEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        return CommonResult.success();
    }
}