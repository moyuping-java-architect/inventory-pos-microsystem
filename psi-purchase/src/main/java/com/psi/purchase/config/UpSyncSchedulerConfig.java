package com.psi.purchase.config;

import com.psi.purchase.service.PurchaseUpSyncDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 上行数据同步调度配置
 * 定时从中间同步微服务拉取POS上传的数据
 * 参照收银微服务下载中间微服务数据模式实现
 */
@Slf4j
@Configuration
@EnableScheduling
public class UpSyncSchedulerConfig {

    private final PurchaseUpSyncDownloadService purchaseUpSyncDownloadService;

    public UpSyncSchedulerConfig(PurchaseUpSyncDownloadService purchaseUpSyncDownloadService) {
        this.purchaseUpSyncDownloadService = purchaseUpSyncDownloadService;
    }

    /**
     * 每隔30秒拉取一次上行同步数据
     */
    @Scheduled(fixedDelay = 30000)
    public void scheduledSyncDownload() {
        log.debug("[purchase] 定时拉取上行同步数据");
        purchaseUpSyncDownloadService.syncDownload();
    }
}
