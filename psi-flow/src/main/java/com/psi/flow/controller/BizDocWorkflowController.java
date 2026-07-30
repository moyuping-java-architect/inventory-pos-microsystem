package com.psi.flow.controller;

import com.psi.common.exception.BusinessException;
import com.psi.common.result.CommonResult;
import com.psi.flow.service.DocWorkflowService;
import com.psi.order.constant.DocTypeConstant;
import com.psi.order.dto.CreateDocRequest;
import com.psi.order.dto.DocResponse;
import com.psi.order.dto.biz.purchase.PurchaseOrderDTO;
import com.psi.order.dto.biz.purchase.PurchaseInDTO;
import com.psi.order.dto.biz.purchase.PurchaseReturnDTO;
import com.psi.order.dto.biz.sale.SaleOrderDTO;
import com.psi.order.dto.biz.sale.SaleOutDTO;
import com.psi.order.dto.biz.sale.SaleReturnDTO;
import com.psi.order.dto.biz.stock.StockCheckDTO;
import com.psi.order.dto.biz.stock.StockTransferDTO;
import com.psi.order.dto.biz.stock.StockOverflowDTO;
import com.psi.order.dto.biz.stock.StockLossDTO;
import com.psi.order.dto.biz.stock.InventoryInitDTO;
import com.psi.order.dto.biz.goods.AdjustPriceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 业务单据工作流控制器
 * 每个业务类型独立一个提交审批接口，方便测试和以后拆分
 */
@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
@Tag(name = "业务单据工作流", description = "各业务类型独立的提交审批接口")
public class BizDocWorkflowController {

    private final DocWorkflowService docWorkflowService;

    // ======================== 采购模块 ========================

    @PostMapping("/purchase-order/submit")
    @Operation(summary = "采购订单提交审批")
    public CommonResult<PurchaseOrderDTO> submitPurchaseOrder(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.PURCHASE_ORDER.getCode());
        validatePurchaseOrder(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(PurchaseOrderDTO.fromResponse(response));
    }

    @PostMapping("/purchase-in/submit")
    @Operation(summary = "采购入库提交审批")
    public CommonResult<PurchaseInDTO> submitPurchaseIn(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.PURCHASE_IN.getCode());
        validatePurchaseIn(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(PurchaseInDTO.fromResponse(response));
    }

    @PostMapping("/purchase-return/submit")
    @Operation(summary = "采购退货提交审批")
    public CommonResult<PurchaseReturnDTO> submitPurchaseReturn(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.PURCHASE_RETURN.getCode());
        validatePurchaseReturn(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(PurchaseReturnDTO.fromResponse(response));
    }

    // ======================== 销售模块 ========================

    @PostMapping("/sale-order/submit")
    @Operation(summary = "销售订单提交审批")
    public CommonResult<SaleOrderDTO> submitSaleOrder(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.SALE_ORDER.getCode());
        validateSaleOrder(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(SaleOrderDTO.fromResponse(response));
    }

    @PostMapping("/sale-out/submit")
    @Operation(summary = "销售出库提交审批")
    public CommonResult<SaleOutDTO> submitSaleOut(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.SALE_OUT.getCode());
        validateSaleOut(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(SaleOutDTO.fromResponse(response));
    }

    @PostMapping("/sale-return/submit")
    @Operation(summary = "销售退货提交审批")
    public CommonResult<SaleReturnDTO> submitSaleReturn(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.SALE_RETURN.getCode());
        validateSaleReturn(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(SaleReturnDTO.fromResponse(response));
    }

    // ======================== 库存模块 ========================

    @PostMapping("/stock-check/submit")
    @Operation(summary = "盘点单提交审批")
    public CommonResult<StockCheckDTO> submitStockCheck(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.STOCK_CHECK.getCode());
        validateStockCheck(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(StockCheckDTO.fromResponse(response));
    }

    @PostMapping("/stock-transfer/submit")
    @Operation(summary = "调拨单提交审批")
    public CommonResult<StockTransferDTO> submitStockTransfer(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.STOCK_TRANSFER.getCode());
        validateStockTransfer(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(StockTransferDTO.fromResponse(response));
    }

    @PostMapping("/stock-overflow/submit")
    @Operation(summary = "报溢单提交审批")
    public CommonResult<StockOverflowDTO> submitStockOverflow(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.STOCK_OVERFLOW.getCode());
        validateStockOverflow(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(StockOverflowDTO.fromResponse(response));
    }

    @PostMapping("/stock-loss/submit")
    @Operation(summary = "报损单提交审批")
    public CommonResult<StockLossDTO> submitStockLoss(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.STOCK_LOSS.getCode());
        validateStockLoss(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(StockLossDTO.fromResponse(response));
    }

    @PostMapping("/inventory-init/submit")
    @Operation(summary = "库存初始化单提交审批")
    public CommonResult<InventoryInitDTO> submitInventoryInit(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.INVENTORY_INIT.getCode());
        validateInventoryInit(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(InventoryInitDTO.fromResponse(response));
    }

    // ======================== 商品模块 ========================

    @PostMapping("/adjust-price/submit")
    @Operation(summary = "调价单提交审批")
    public CommonResult<AdjustPriceDTO> submitAdjustPrice(@RequestBody CreateDocRequest request) {
        request.setDocType(DocTypeConstant.DocType.ADJUST_PRICE.getCode());
        validateAdjustPrice(request);
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(AdjustPriceDTO.fromResponse(response));
    }

    // ======================== 校验方法 ========================

    private void validateItems(CreateDocRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("请添加商品明细");
        }
        for (int i = 0; i < request.getItems().size(); i++) {
            CreateDocRequest.DocItemRequest item = request.getItems().get(i);
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("第" + (i + 1) + "行数量必须大于0");
            }
        }
    }

    private void validatePurchaseOrder(CreateDocRequest request) {
        if (request.getPartnerId() == null || request.getPartnerId().isBlank()) {
            throw new BusinessException("请选择供应商");
        }
        validateItems(request);
    }

    private void validatePurchaseIn(CreateDocRequest request) {
        if (request.getPartnerId() == null || request.getPartnerId().isBlank()) {
            throw new BusinessException("请选择供应商");
        }
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择入库仓库");
        }
        validateItems(request);
    }

    private void validatePurchaseReturn(CreateDocRequest request) {
        if (request.getPartnerId() == null || request.getPartnerId().isBlank()) {
            throw new BusinessException("请选择供应商");
        }
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择退货仓库");
        }
        validateItems(request);
    }

    private void validateSaleOrder(CreateDocRequest request) {
        if (request.getPartnerId() == null || request.getPartnerId().isBlank()) {
            throw new BusinessException("请选择客户");
        }
        validateItems(request);
    }

    private void validateSaleOut(CreateDocRequest request) {
        if (request.getPartnerId() == null || request.getPartnerId().isBlank()) {
            throw new BusinessException("请选择客户");
        }
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择出库仓库");
        }
        validateItems(request);
    }

    private void validateSaleReturn(CreateDocRequest request) {
        if (request.getPartnerId() == null || request.getPartnerId().isBlank()) {
            throw new BusinessException("请选择客户");
        }
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择退货仓库");
        }
        validateItems(request);
    }

    private void validateStockCheck(CreateDocRequest request) {
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择盘点仓库");
        }
        validateItems(request);
    }

    private void validateStockTransfer(CreateDocRequest request) {
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择调出仓库");
        }
        validateItems(request);
    }

    private void validateStockOverflow(CreateDocRequest request) {
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择仓库");
        }
        validateItems(request);
    }

    private void validateStockLoss(CreateDocRequest request) {
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择仓库");
        }
        validateItems(request);
    }

    private void validateInventoryInit(CreateDocRequest request) {
        if (request.getWarehouseId() == null) {
            throw new BusinessException("请选择仓库");
        }
        validateItems(request);
    }

    private void validateAdjustPrice(CreateDocRequest request) {
        validateItems(request);
    }
}