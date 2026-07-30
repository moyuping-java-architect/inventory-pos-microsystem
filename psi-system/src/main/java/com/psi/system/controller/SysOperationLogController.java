package com.psi.system.controller;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.result.CommonResult;
import com.psi.system.entity.SysOperationLog;
import com.psi.system.service.SysOperationLogService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 操作日志Controller
 * 
 * @author PSI
 * @version 1.0.0
 */
@RestController
@RequestMapping("/psi/admin/operation-log")
public class SysOperationLogController {

    private final SysOperationLogService sysOperationLogService;
    private final BatchUtils batchUtils;

    public SysOperationLogController(SysOperationLogService sysOperationLogService, BatchUtils batchUtils) {
        this.sysOperationLogService = sysOperationLogService;
        this.batchUtils = batchUtils;
    }

    /**
     * 查询操作日志列表
     */
    @GetMapping("/list")
    public CommonResult<List<SysOperationLog>> list(SysOperationLog sysOperationLog) {
        List<SysOperationLog> list = sysOperationLogService.list();
        return CommonResult.success(list);
    }

    /**
     * 查询操作日志详细
     */
    @GetMapping("/{id}")
    public CommonResult<SysOperationLog> getById(@PathVariable Long id) {
        SysOperationLog operationLog = sysOperationLogService.getById(id);
        return operationLog != null ? CommonResult.success(operationLog) : CommonResult.fail("记录不存在");
    }

    /**
     * 删除操作日志
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysOperationLogService.removeById(id) ? CommonResult.success() : CommonResult.fail("删除失败");
    }

    /**
     * 批量删除操作日志
     */
    @DeleteMapping("/batch")
    public CommonResult<Void> deleteBatch(@RequestBody Long[] ids) {
        return batchUtils.removeByIds(sysOperationLogService, Arrays.asList(ids)) ? CommonResult.success() : CommonResult.fail("删除失败");
    }
}