package com.psi.cashier.controller;

import com.psi.cashier.service.CashierDownloadService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据下载控制器
 * 触发收银系统从中间微服务拉取下行同步数据（收银机配置、收银员、客户等）
 * 支持手动触发和异步触发
 */
@Slf4j
@RestController
@RequestMapping("/psi/cashier/download")
@RequiredArgsConstructor
public class CashierDownloadController {

    private final CashierDownloadService cashierDownloadService;

    /**
     * 同步触发全量下载（阻塞等待完成）
     */
    @PostMapping("/sync")
    public CommonResult<String> triggerSyncDownload() {
        log.info("手动触发下行同步（同步模式）");
        cashierDownloadService.syncDownload();
        return CommonResult.success("同步下载完成");
    }

    /**
     * 异步触发全量下载（虚拟线程，不阻塞）
     */
    @PostMapping("/async")
    public CommonResult<String> triggerAsyncDownload() {
        log.info("手动触发下行同步（异步模式）");
        cashierDownloadService.asyncDownload();
        return CommonResult.success("已触发异步下载，请稍后查看结果");
    }

    /**
     * 下载指定表类型的数据
     *
     * @param tableName 表名（pos_config, pos_operator, customer）
     */
    @PostMapping("/table/{tableName}")
    public CommonResult<String> downloadByTable(@PathVariable String tableName) {
        log.info("手动触发指定表下载: tableName={}", tableName);
        cashierDownloadService.downloadByTable(tableName);
        return CommonResult.success("已触发" + tableName + "异步下载");
    }

    /**
     * 获取上次下载时间
     */
    @GetMapping("/last-time")
    public CommonResult<String> getLastDownloadTime() {
        String lastTime = cashierDownloadService.getLastDownloadTime("down");
        return CommonResult.success(lastTime != null ? lastTime : "暂无下载记录");
    }
}