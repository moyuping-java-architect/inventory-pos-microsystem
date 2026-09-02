package com.psi.goods.controller;

import com.psi.goods.service.GoodsUpSyncDownloadService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/psi/goods/sync")
@RequiredArgsConstructor
public class GoodsUpSyncController {

    private final GoodsUpSyncDownloadService goodsUpSyncDownloadService;

    @PostMapping("/download")
    public CommonResult<String> triggerDownload() {
        log.info("[goods] manual trigger up sync download");
        goodsUpSyncDownloadService.syncDownload();
        return CommonResult.success("sync download triggered");
    }
}
