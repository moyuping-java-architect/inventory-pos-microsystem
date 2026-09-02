package com.psi.system.controller;

import com.psi.system.service.SystemUpSyncDownloadService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/psi/system/sync")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SystemUpSyncController {

    private final SystemUpSyncDownloadService systemUpSyncDownloadService;

    @PostMapping("/download")
    public CommonResult<String> triggerDownload() {
        log.info("[system] manual trigger up sync download");
        systemUpSyncDownloadService.syncDownload();
        return CommonResult.success("sync download triggered");
    }
}
