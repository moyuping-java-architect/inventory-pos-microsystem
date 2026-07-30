package com.psi.sale.controller;

import com.psi.sale.service.SaleUpSyncDownloadService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/psi/sale/sync")
@RequiredArgsConstructor
public class SaleUpSyncController {

    private final SaleUpSyncDownloadService saleUpSyncDownloadService;

    @PostMapping("/download")
    public CommonResult<String> triggerDownload() {
        log.info("[sale] manual trigger up sync download");
        saleUpSyncDownloadService.syncDownload();
        return CommonResult.success("sync download triggered");
    }
}
