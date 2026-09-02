package com.psi.purchase.controller;

import com.psi.purchase.service.PurchaseUpSyncDownloadService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/psi/purchase/sync")
@RequiredArgsConstructor
public class PurchaseUpSyncController {

    private final PurchaseUpSyncDownloadService purchaseUpSyncDownloadService;

    @PostMapping("/download")
    public CommonResult<String> triggerDownload() {
        log.info("[purchase] manual trigger up sync download");
        purchaseUpSyncDownloadService.syncDownload();
        return CommonResult.success("sync download triggered");
    }
}
