package com.psi.finance.controller;

import com.psi.finance.service.FinanceUpSyncDownloadService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/psi/finance/sync")
@RequiredArgsConstructor
public class FinanceUpSyncController {

    private final FinanceUpSyncDownloadService financeUpSyncDownloadService;

    @PostMapping("/download")
    public CommonResult<String> triggerDownload() {
        log.info("[finance] manual trigger up sync download");
        financeUpSyncDownloadService.syncDownload();
        return CommonResult.success("sync download triggered");
    }
}
