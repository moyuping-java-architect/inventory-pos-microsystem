package com.psi.sale.service.impl;

import com.psi.sale.dto.CustomerDTO;
import com.psi.sale.dto.CustomerQueryDTO;
import com.psi.sale.dto.CustomerSaveDTO;
import com.psi.sale.entity.CustomerEntity;
import com.psi.sale.mapper.CustomerMapper;
import com.psi.sale.mq.producer.CustomerDownSyncProducer;
import com.psi.sale.mq.producer.CustomerSyncProducer;
import com.psi.sale.service.CustomerService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, CustomerEntity> implements CustomerService {

    private final CustomerSyncProducer customerSyncProducer;
    private final CustomerDownSyncProducer customerDownSyncProducer;

    public CustomerServiceImpl(CustomerSyncProducer customerSyncProducer,
                               CustomerDownSyncProducer customerDownSyncProducer) {
        this.customerSyncProducer = customerSyncProducer;
        this.customerDownSyncProducer = customerDownSyncProducer;
    }

    @Override
    public CommonResult<CustomerDTO> getById(Long id) {
        CustomerEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, CustomerDTO.class));
    }

    @Override
    public PageResult<CustomerDTO> list(CustomerQueryDTO queryDTO) {
        Page<CustomerEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<CustomerEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getCustomerCode() != null) {
            wrapper.like(CustomerEntity::getCustomerCode, queryDTO.getCustomerCode());
        }
        if (queryDTO.getCustomerName() != null) {
            wrapper.like(CustomerEntity::getCustomerName, queryDTO.getCustomerName());
        }
        if (queryDTO.getShortName() != null) {
            wrapper.like(CustomerEntity::getShortName, queryDTO.getShortName());
        }
        if (queryDTO.getContactName() != null) {
            wrapper.like(CustomerEntity::getContactName, queryDTO.getContactName());
        }
        if (queryDTO.getContactPhone() != null) {
            wrapper.like(CustomerEntity::getContactPhone, queryDTO.getContactPhone());
        }
        if (queryDTO.getCustomerType() != null) {
            wrapper.eq(CustomerEntity::getCustomerType, queryDTO.getCustomerType());
        }
        if (queryDTO.getCustomerLevel() != null) {
            wrapper.eq(CustomerEntity::getCustomerLevel, queryDTO.getCustomerLevel());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(CustomerEntity::getStatus, queryDTO.getStatus());
        }
        
        IPage<CustomerEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, CustomerDTO.class));
    }

    @Override
    public CommonResult<CustomerDTO> save(CustomerSaveDTO saveDTO) {
        CustomerEntity entity = BeanUtils.convert(saveDTO, CustomerEntity.class);
        super.save(entity);
        // 发送客户同步消息
        customerSyncProducer.sendCustomer(entity);
        // 发送客户下行同步消息到 POS
        customerDownSyncProducer.sendCustomer(entity);
        return CommonResult.success(BeanUtils.convert(entity, CustomerDTO.class));
    }

    @Override
    public CommonResult<CustomerDTO> update(Long id, CustomerSaveDTO saveDTO) {
        CustomerEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        BeanUtils.copyProperties(saveDTO, entity);
        super.updateById(entity);
        // 发送客户同步消息
        customerSyncProducer.sendCustomer(entity);
        // 发送客户下行同步消息到 POS
        customerDownSyncProducer.sendCustomer(entity);
        return CommonResult.success(BeanUtils.convert(entity, CustomerDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        CustomerEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setDelFlag(1);
        super.updateById(entity);
        // 发送客户同步消息（删除标记）
        customerSyncProducer.sendCustomer(entity);
        // 发送客户下行同步消息到 POS（删除标记）
        customerDownSyncProducer.sendCustomer(entity);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        CustomerEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        // 发送客户同步消息
        customerSyncProducer.sendCustomer(entity);
        // 发送客户下行同步消息到 POS
        customerDownSyncProducer.sendCustomer(entity);
        return CommonResult.success();
    }
}