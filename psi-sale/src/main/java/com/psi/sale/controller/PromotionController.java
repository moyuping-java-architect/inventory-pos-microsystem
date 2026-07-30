package com.psi.sale.controller;

import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.sale.dto.PromotionCalculateDTO;
import com.psi.sale.dto.PromotionDTO;
import com.psi.sale.dto.PromotionQueryDTO;
import com.psi.sale.dto.PromotionResultDTO;
import com.psi.sale.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sale/promotion")
@Tag(name = "促销管理", description = "促销活动管理")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询促销活动")
    public CommonResult<PageResult<PromotionDTO>> page(PromotionQueryDTO queryDTO) {
        return CommonResult.success(promotionService.page(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询促销活动")
    public CommonResult<PromotionDTO> getById(@PathVariable Long id) {
        return promotionService.getById(id);
    }

    @PostMapping
    @Operation(summary = "新增促销活动")
    public CommonResult<Void> add(@RequestBody PromotionDTO dto) {
        return promotionService.add(dto);
    }

    @PutMapping
    @Operation(summary = "修改促销活动")
    public CommonResult<Void> update(@RequestBody PromotionDTO dto) {
        return promotionService.update(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除促销活动")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return promotionService.delete(id);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新促销状态")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return promotionService.updateStatus(id, status);
    }

    @GetMapping("/active")
    @Operation(summary = "获取进行中的促销活动")
    public CommonResult<List<PromotionDTO>> getActivePromotions(
            @RequestParam(required = false) String warehouseCode) {
        return promotionService.getActivePromotions(warehouseCode);
    }

    @PostMapping("/calculate")
    @Operation(summary = "计算促销优惠")
    public CommonResult<PromotionResultDTO> calculate(@RequestBody PromotionCalculateDTO calculateDTO) {
        return promotionService.calculate(calculateDTO);
    }
}
