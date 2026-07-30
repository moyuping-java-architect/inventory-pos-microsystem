package com.psi.order.controller;

import com.psi.order.dto.CreateDocRequest;
import com.psi.order.dto.DocResponse;
import com.psi.order.service.DocService;
import com.psi.order.constant.DocTypeConstant.DocType;
import com.psi.order.constant.DocTypeConstant.DocStatus;
import com.psi.common.result.CommonResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 单据管理控制器
 */
@RestController
@RequestMapping("/doc")
@RequiredArgsConstructor
@Tag(name = "单据管理", description = "单据CRUD操作")
public class DocController {

    private final DocService docService;

    @PostMapping
    @Operation(summary = "创建单据", description = "创建新的业务单据")
    public CommonResult<DocResponse> create(@RequestBody CreateDocRequest request) {
        DocResponse response = docService.create(request);
        return CommonResult.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询单据详情", description = "根据单据ID查询详细信息")
    public CommonResult<DocResponse> findById(@PathVariable Long id) {
        DocResponse response = docService.findById(id);
        return CommonResult.success(response);
    }

    @GetMapping("/docNo/{docNo}")
    @Operation(summary = "根据单据编号查询", description = "根据单据编号查询单据")
    public CommonResult<DocResponse> findByDocNo(@PathVariable String docNo) {
        DocResponse response = docService.findByDocNo(docNo);
        return CommonResult.success(response);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询单据", description = "分页查询单据列表，status支持逗号分隔多状态如0,1")
    public CommonResult<IPage<DocResponse>> findPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "单据类型") @RequestParam(required = false) String docType,
            @Parameter(description = "单据状态，多个用逗号分隔如0,1") @RequestParam(required = false) String status,
            @Parameter(description = "创建人ID") @RequestParam(required = false) String creatorId) {
        Page<DocResponse> page = new Page<>(pageNum, pageSize);
        
        List<Integer> statusList = parseStatusList(status);
        IPage<DocResponse> result;
        if (statusList != null && !statusList.isEmpty()) {
            result = docService.findPageByStatusList(page, docType, statusList, creatorId);
        } else {
            Integer singleStatus = (status != null && !status.isEmpty()) ? Integer.valueOf(status) : null;
            result = docService.findPage(page, docType, singleStatus, creatorId);
        }
        return CommonResult.success(result);
    }

    private List<Integer> parseStatusList(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        if (status.contains(",")) {
            List<Integer> list = new java.util.ArrayList<>();
            for (String s : status.split(",")) {
                try {
                    list.add(Integer.valueOf(s.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            return list;
        }
        return null;
    }

    @GetMapping("/type/{docType}")
    @Operation(summary = "根据单据类型查询", description = "查询指定类型的单据列表")
    public CommonResult<List<DocResponse>> findByDocType(@PathVariable String docType) {
        List<DocResponse> list = docService.findByDocType(docType);
        return CommonResult.success(list);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询", description = "查询指定状态的单据列表")
    public CommonResult<List<DocResponse>> findByStatus(@PathVariable Integer status) {
        List<DocResponse> list = docService.findByStatus(status);
        return CommonResult.success(list);
    }

    @GetMapping("/pending")
    @Operation(summary = "查询待审批单据", description = "查询所有待审批的单据")
    public CommonResult<List<DocResponse>> findPendingApprove() {
        List<DocResponse> list = docService.findPendingApprove();
        return CommonResult.success(list);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新单据", description = "更新单据信息")
    public CommonResult<DocResponse> update(@PathVariable Long id, @RequestBody CreateDocRequest request) {
        DocResponse response = docService.update(id, request);
        return CommonResult.success(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除单据", description = "删除单据")
    public CommonResult<Void> delete(@PathVariable Long id) {
        docService.delete(id);
        return CommonResult.success();
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交单据", description = "提交单据进行审批")
    public CommonResult<DocResponse> submit(@PathVariable Long id) {
        DocResponse response = docService.submit(id);
        return CommonResult.success(response);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过", description = "审批通过单据")
    public CommonResult<DocResponse> approve(
            @PathVariable Long id,
            @Parameter(description = "审批人ID") @RequestParam String approverId,
            @Parameter(description = "审批人姓名") @RequestParam String approverName) {
        DocResponse response = docService.approve(id, approverId, approverName);
        return CommonResult.success(response);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审批驳回", description = "驳回单据")
    public CommonResult<DocResponse> reject(
            @PathVariable Long id,
            @Parameter(description = "审批人ID") @RequestParam String approverId,
            @Parameter(description = "驳回原因") @RequestParam(required = false) String remark) {
        DocResponse response = docService.reject(id, approverId, remark);
        return CommonResult.success(response);
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "执行单据", description = "执行单据")
    public CommonResult<DocResponse> execute(@PathVariable Long id) {
        DocResponse response = docService.execute(id);
        return CommonResult.success(response);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成单据", description = "完成单据")
    public CommonResult<DocResponse> complete(@PathVariable Long id) {
        DocResponse response = docService.complete(id);
        return CommonResult.success(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消单据", description = "取消单据")
    public CommonResult<DocResponse> cancel(
            @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam(required = false) String remark) {
        DocResponse response = docService.cancel(id, remark);
        return CommonResult.success(response);
    }

    @GetMapping("/types")
    @Operation(summary = "获取单据类型列表", description = "获取所有支持的单据类型")
    public CommonResult<List<DocType>> getAllDocTypes() {
        List<DocType> list = docService.getAllDocTypes();
        return CommonResult.success(list);
    }

    @GetMapping("/statuses")
    @Operation(summary = "获取单据状态列表", description = "获取所有单据状态")
    public CommonResult<List<DocStatus>> getAllDocStatus() {
        List<DocStatus> list = docService.getAllDocStatus();
        return CommonResult.success(list);
    }
}