package com.psi.purchase.service;

import com.psi.purchase.dto.SupplierDTO;
import com.psi.purchase.dto.SupplierQueryDTO;
import com.psi.purchase.dto.SupplierSaveDTO;
import com.psi.purchase.entity.SupplierEntity;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SupplierService extends IService<SupplierEntity> {

    CommonResult<SupplierDTO> getById(Long id);

    PageResult<SupplierDTO> list(SupplierQueryDTO queryDTO);

    CommonResult<SupplierDTO> save(SupplierSaveDTO saveDTO);

    CommonResult<SupplierDTO> update(Long id, SupplierSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);
}