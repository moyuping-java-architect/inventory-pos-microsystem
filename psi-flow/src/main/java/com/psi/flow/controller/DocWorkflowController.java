package com.psi.flow.controller;

import com.psi.flow.service.DocWorkflowService;
import com.psi.order.dto.CreateDocRequest;
import com.psi.order.dto.DocResponse;
import com.psi.common.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 单据工作流控制器
 * 将单据创建与工作流启动合并处理
 */
@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
@Tag(name = "单据工作流", description = "单据创建+提交审批+工作流启动")
public class DocWorkflowController {

    private final DocWorkflowService docWorkflowService;

    @PostMapping("/submit")
    @Operation(summary = "创建单据并提交审批", description = "创建单据、提交审批、启动对应工作流")
    public CommonResult<DocResponse> createAndSubmit(@RequestBody CreateDocRequest request) {
        DocResponse response = docWorkflowService.createAndSubmit(request);
        return CommonResult.success(response);
    }
}