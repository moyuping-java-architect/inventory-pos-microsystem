package com.psi.sale.service;

import com.psi.sale.dto.CustomerPaymentDTO;
import com.psi.sale.dto.CustomerPaymentQueryDTO;
import com.psi.sale.dto.CustomerPaymentSaveDTO;
import com.psi.sale.entity.CustomerPaymentEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CustomerPaymentService extends IService<CustomerPaymentEntity> {

    CommonResult<CustomerPaymentDTO> getById(Long id);

    PageResult<CustomerPaymentDTO> list(CustomerPaymentQueryDTO queryDTO);

    CommonResult<CustomerPaymentDTO> save(CustomerPaymentSaveDTO saveDTO);

    CommonResult<Void> updateStatus(Long id, Integer status);
}