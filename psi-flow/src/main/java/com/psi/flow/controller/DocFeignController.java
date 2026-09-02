package com.psi.flow.controller;

import com.psi.common.feign.DocFeignResponse;
import com.psi.common.result.CommonResult;
import com.psi.common.result.ResultCode;
import com.psi.flow.service.DocConvertService;
import com.psi.order.dto.DocResponse;
import com.psi.order.service.DocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 单据 Feign 控制器
 *
 * <p>为 psi-purchase、psi-sale、psi-stock 等业务模块提供远程单据查询和状态更新</p>
 */
@Slf4j
@RestController
@RequestMapping("/psi/flow/doc")
@RequiredArgsConstructor
public class DocFeignController {

    private final DocService docService;
    private final DocConvertService docConvertService;

    @GetMapping("/{docNo}")
    public CommonResult<DocFeignResponse> findByDocNo(@PathVariable String docNo) {
        try {
            DocResponse doc = docService.findByDocNo(docNo);
            if (doc == null) {
                return CommonResult.fail(ResultCode.NOT_FOUND, "单据不存在: " + docNo);
            }
            return CommonResult.success(docConvertService.convertToFeignResponse(doc));
        } catch (Exception e) {
            log.error("查询单据失败: docNo={}", docNo, e);
            return CommonResult.fail(ResultCode.FAIL, e.getMessage());
        }
    }

    @PutMapping("/{id}/complete")
    public CommonResult<Void> complete(@PathVariable Long id) {
        try {
            docService.complete(id);
            log.info("单据已完成: id={}", id);
            return CommonResult.success();
        } catch (Exception e) {
            log.error("完成单据失败: id={}", id, e);
            return CommonResult.fail(ResultCode.FAIL, e.getMessage());
        }
    }
}
