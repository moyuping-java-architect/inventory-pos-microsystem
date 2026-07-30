package com.psi.finance.config;

import com.psi.finance.service.FinanceUpSyncDownloadService;
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

    private final FinanceUpSyncDownloadService financeUpSyncDownloadService;

    public UpSyncSchedulerConfig(FinanceUpSyncDownloadService financeUpSyncDownloadService) {
        this.financeUpSyncDownloadService = financeUpSyncDownloadService;
    }

    /**
     * 每隔30秒拉取一次上行同步数据
     */
    @Scheduled(fixedDelay = 30000)
    public void scheduledSyncDownload() {
        log.debug("[finance] 定时拉取上行同步数据");
        financeUpSyncDownloadService.syncDownload();
    }
}
