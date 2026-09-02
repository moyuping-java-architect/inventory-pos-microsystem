package com.psi.customer.controller;

import com.psi.common.result.CommonResult;
import com.psi.customer.dto.*;
import com.psi.customer.service.SalesScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 话术库 * 销冠 接口（旅程触达计划版）
 * 核心：客户在某旅程节点停留第 N 天 + 命中标签 + 未发送过 -> 触达对应话术
 */
@RestController
@RequestMapping("/psi/customer/sales-script")
@RequiredArgsConstructor
public class SalesScriptController {

    private final SalesScriptService salesScriptService;

    // ========== 话术库 ==========

    /** 按节点分组查询话术列表 */
    @GetMapping("/list")
    public CommonResult<List<SalesScriptLibraryDTO>> list(@RequestParam(required = false) String stageCode) {
        return salesScriptService.listScripts(stageCode);
    }

    @GetMapping("/{id}")
    public CommonResult<SalesScriptLibraryDTO> get(@PathVariable Long id) {
        return salesScriptService.getScript(id);
    }

    @PostMapping("/save")
    public CommonResult<Long> save(@RequestBody SalesScriptLibraryDTO dto) {
        return salesScriptService.saveScript(dto);
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody SalesScriptLibraryDTO dto) {
        return salesScriptService.updateScript(dto);
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return salesScriptService.deleteScript(id);
    }

    // ========== 客户触达 ==========

    /** 查询今日应触达的客户列表 */
    @GetMapping("/match/customers")
    public CommonResult<List<ScriptMatchCustomerDTO>> listMatchCustomers(
            @RequestParam(required = false, defaultValue = "50") Integer limit) {
        return salesScriptService.listMatchCustomers(limit);
    }

    /** 查询某客户当前可触达的话术 */
    @GetMapping("/match/{customerId}")
    public CommonResult<List<ScriptMatchResultDTO>> matchForCustomer(@PathVariable Long customerId) {
        return salesScriptService.matchScriptsForCustomer(customerId);
    }

    /** 发送话术（幂等，同一节点-天数-话术不会重复发送） */
    @PostMapping("/send")
    public CommonResult<Void> sendScript(@RequestBody ScriptSendRequest req) {
        return salesScriptService.sendScript(req.getCustomerId(), req.getScriptId(), req.getDayInStage());
    }

    /** 查询某客户的发送记录 */
    @GetMapping("/send-log/{customerId}")
    public CommonResult<List<CustomerScriptSendLogDTO>> listSendLogs(@PathVariable Long customerId) {
        return salesScriptService.listSendLogs(customerId);
    }

    // ========== 标签 ==========

    @GetMapping("/tags")
    public CommonResult<List<CustomerTagDTO>> listTags() {
        return salesScriptService.listTags();
    }

    @PostMapping("/customer/{customerId}/tags")
    public CommonResult<Void> saveCustomerTags(@PathVariable Long customerId,
                                               @RequestBody List<String> tagCodes) {
        return salesScriptService.saveCustomerTags(customerId, tagCodes);
    }
}
