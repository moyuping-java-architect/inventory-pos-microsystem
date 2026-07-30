package com.psi.system.controller;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.result.CommonResult;
import com.psi.system.entity.SysLoginLog;
import com.psi.system.service.SysLoginLogService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 登录日志Controller
 * 
 * @author PSI
 * @version 1.0.0
 */
@RestController
@RequestMapping("/psi/admin/login-log")
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;
    private final BatchUtils batchUtils;

    public SysLoginLogController(SysLoginLogService sysLoginLogService, BatchUtils batchUtils) {
        this.sysLoginLogService = sysLoginLogService;
        this.batchUtils = batchUtils;
    }

    /**
     * 查询登录日志列表
     */
    @GetMapping("/list")
    public CommonResult<List<SysLoginLog>> list(SysLoginLog sysLoginLog) {
        List<SysLoginLog> list = sysLoginLogService.list();
        return CommonResult.success(list);
    }

    /**
     * 查询登录日志详细
     */
    @GetMapping("/{id}")
    public CommonResult<SysLoginLog> getById(@PathVariable Long id) {
        SysLoginLog loginLog = sysLoginLogService.getById(id);
        return loginLog != null ? CommonResult.success(loginLog) : CommonResult.fail("记录不存在");
    }

    /**
     * 删除登录日志
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysLoginLogService.removeById(id) ? CommonResult.success() : CommonResult.fail("删除失败");
    }

    /**
     * 批量删除登录日志
     */
    @DeleteMapping("/batch")
    public CommonResult<Void> deleteBatch(@RequestBody Long[] ids) {
        return batchUtils.removeByIds(sysLoginLogService, Arrays.asList(ids)) ? CommonResult.success() : CommonResult.fail("删除失败");
    }
}