package com.psi.sale.service.impl;

import com.psi.sale.dto.CustomerPaymentDTO;
import com.psi.sale.dto.CustomerPaymentQueryDTO;
import com.psi.sale.dto.CustomerPaymentSaveDTO;
import com.psi.sale.dto.PaymentAllocationDTO;
import com.psi.sale.entity.CustomerDebtDetailEntity;
import com.psi.sale.entity.CustomerPaymentEntity;
import com.psi.sale.mapper.CustomerDebtDetailMapper;
import com.psi.sale.mapper.CustomerPaymentMapper;
import com.psi.sale.service.CustomerDebtService;
import com.psi.sale.service.CustomerPaymentService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.psi.common.util.IdUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CustomerPaymentServiceImpl extends ServiceImpl<CustomerPaymentMapper, CustomerPaymentEntity> implements CustomerPaymentService {

    private final CustomerDebtService customerDebtService;
    private final CustomerDebtDetailMapper customerDebtDetailMapper;

    public CustomerPaymentServiceImpl(CustomerDebtService customerDebtService, CustomerDebtDetailMapper customerDebtDetailMapper) {
        this.customerDebtService = customerDebtService;
        this.customerDebtDetailMapper = customerDebtDetailMapper;
    }

    @Override
    public CommonResult<CustomerPaymentDTO> getById(Long id) {
        CustomerPaymentEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, CustomerPaymentDTO.class));
    }

    @Override
    public PageResult<CustomerPaymentDTO> list(CustomerPaymentQueryDTO queryDTO) {
        Page<CustomerPaymentEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<CustomerPaymentEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getPaymentNo() != null) {
            wrapper.like(CustomerPaymentEntity::getPaymentNo, queryDTO.getPaymentNo());
        }
        if (queryDTO.getCustomerCode() != null) {
            wrapper.like(CustomerPaymentEntity::getCustomerCode, queryDTO.getCustomerCode());
        }
        if (queryDTO.getCustomerName() != null) {
            wrapper.like(CustomerPaymentEntity::getCustomerName, queryDTO.getCustomerName());
        }
        if (queryDTO.getPaymentDate() != null) {
            wrapper.eq(CustomerPaymentEntity::getPaymentDate, queryDTO.getPaymentDate());
        }
        if (queryDTO.getPaymentMethod() != null) {
            wrapper.eq(CustomerPaymentEntity::getPaymentMethod, queryDTO.getPaymentMethod());
        }
        
        IPage<CustomerPaymentEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, CustomerPaymentDTO.class));
    }

    @Override
    @Transactional
    public CommonResult<CustomerPaymentDTO> save(CustomerPaymentSaveDTO saveDTO) {
        CustomerPaymentEntity entity = BeanUtils.convert(saveDTO, CustomerPaymentEntity.class);
        entity.setPaymentNo("CP" + IdUtils.generateId());
        super.save(entity);
        
        if (saveDTO.getAllocations() != null) {
            BigDecimal totalAllocated = BigDecimal.ZERO;
            for (PaymentAllocationDTO allocation : saveDTO.getAllocations()) {
                CustomerDebtDetailEntity detail = customerDebtDetailMapper.selectById(allocation.getDebtDetailId());
                if (detail != null) {
                    detail.setPaidAmount(detail.getPaidAmount().add(allocation.getAllocateAmount()));
                    detail.setPendingAmount(detail.getPendingAmount().subtract(allocation.getAllocateAmount()));
                    
                    if (detail.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        detail.setStatus(3);
                    } else if (detail.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                        detail.setStatus(2);
                    }
                    customerDebtDetailMapper.updateById(detail);
                    totalAllocated = totalAllocated.add(allocation.getAllocateAmount());
                }
            }
            
            if (totalAllocated.compareTo(BigDecimal.ZERO) > 0) {
                customerDebtService.payDebt(saveDTO.getCustomerId(), totalAllocated);
            }
        }
        
        return CommonResult.success(BeanUtils.convert(entity, CustomerPaymentDTO.class));
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        CustomerPaymentEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        return CommonResult.success();
    }
}