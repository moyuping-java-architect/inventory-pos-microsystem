package com.psi.sale.service;

import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.sale.dto.PromotionCalculateDTO;
import com.psi.sale.dto.PromotionDTO;
import com.psi.sale.dto.PromotionQueryDTO;
import com.psi.sale.dto.PromotionResultDTO;
import com.psi.sale.entity.PromotionEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PromotionService extends IService<PromotionEntity> {

    PageResult<PromotionDTO> page(PromotionQueryDTO queryDTO);

    CommonResult<PromotionDTO> getById(Long id);

    CommonResult<Void> add(PromotionDTO dto);

    CommonResult<Void> update(PromotionDTO dto);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);

    CommonResult<List<PromotionDTO>> getActivePromotions(String warehouseCode);

    CommonResult<PromotionResultDTO> calculate(PromotionCalculateDTO calculateDTO);
}
