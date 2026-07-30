package com.psi.stock.controller;

import com.psi.stock.service.StockUpSyncDownloadService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/psi/stock/sync")
@RequiredArgsConstructor
public class StockUpSyncController {

    private final StockUpSyncDownloadService stockUpSyncDownloadService;

    @PostMapping("/download")
    public CommonResult<String> triggerDownload() {
        log.info("[stock] manual trigger up sync download");
        stockUpSyncDownloadService.syncDownload();
        return CommonResult.success("sync download triggered");
    }
}
