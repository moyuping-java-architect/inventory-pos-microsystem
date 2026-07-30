package com.psi.goods.config;

import com.psi.goods.service.GoodsUpSyncDownloadService;
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

    private final GoodsUpSyncDownloadService goodsUpSyncDownloadService;

    public UpSyncSchedulerConfig(GoodsUpSyncDownloadService goodsUpSyncDownloadService) {
        this.goodsUpSyncDownloadService = goodsUpSyncDownloadService;
    }

    /**
     * 每隔30秒拉取一次上行同步数据
     */
    @Scheduled(fixedDelay = 30000)
    public void scheduledSyncDownload() {
        log.debug("[goods] 定时拉取上行同步数据");
        goodsUpSyncDownloadService.syncDownload();
    }
}
