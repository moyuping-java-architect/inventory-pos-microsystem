package com.psi.goods.controller;

import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.goods.entity.AdjustPriceItemEntity;
import com.psi.goods.entity.AdjustPriceMainEntity;
import com.psi.goods.service.AdjustPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品调价单接口
 */
@RestController
@RequestMapping("/psi/goods/adjust-price")
@RequiredArgsConstructor
public class AdjustPriceController {

    private final AdjustPriceService adjustPriceService;

    /**
     * 保存调价单（草稿/提交）
     */
    @PostMapping("/save")
    public CommonResult<AdjustPriceMainEntity> save(@RequestBody AdjustPriceRequest request) {
        return adjustPriceService.saveAdjustPrice(request.getMain(), request.getItems());
    }

    /**
     * 根据ID查询调价单详情
     */
    @GetMapping("/{id}")
    public CommonResult<AdjustPriceMainEntity> getById(@PathVariable Long id) {
        return adjustPriceService.getDetailById(id);
    }

    /**
     * 分页查询调价单
     */
    @GetMapping("/page")
    public PageResult<AdjustPriceMainEntity> queryPage(
            @RequestParam(required = false) String adjustNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return adjustPriceService.queryPage(adjustNo, status, pageNum, pageSize);
    }

    /**
     * 审核调价单
     */
    @PostMapping("/audit/{id}")
    public CommonResult<Void> audit(@PathVariable Long id) {
        return adjustPriceService.audit(id);
    }

    /**
     * 请求体封装
     */
    public static class AdjustPriceRequest {
        private AdjustPriceMainEntity main;
        private List<AdjustPriceItemEntity> items;

        public AdjustPriceMainEntity getMain() {
            return main;
        }

        public void setMain(AdjustPriceMainEntity main) {
            this.main = main;
        }

        public List<AdjustPriceItemEntity> getItems() {
            return items;
        }

        public void setItems(List<AdjustPriceItemEntity> items) {
            this.items = items;
        }
    }
}
