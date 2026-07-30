package com.psi.sale.service;

import com.psi.sale.dto.CustomerDTO;
import com.psi.sale.dto.CustomerQueryDTO;
import com.psi.sale.dto.CustomerSaveDTO;
import com.psi.sale.entity.CustomerEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CustomerService extends IService<CustomerEntity> {

    CommonResult<CustomerDTO> getById(Long id);

    PageResult<CustomerDTO> list(CustomerQueryDTO queryDTO);

    CommonResult<CustomerDTO> save(CustomerSaveDTO saveDTO);

    CommonResult<CustomerDTO> update(Long id, CustomerSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);
}