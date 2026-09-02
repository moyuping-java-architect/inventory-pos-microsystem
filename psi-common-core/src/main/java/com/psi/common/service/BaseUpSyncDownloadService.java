package com.psi.common.service;

import com.psi.common.feign.SyncUpFeignClient;
import com.psi.common.result.CommonResult;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 上行数据下载服务基类
 * 进销存微服务继承此类，从中间同步微服务拉取POS上传的数据
 * 功能参照收银微服务下载中间微服务数据
 */
@Slf4j
public abstract class BaseUpSyncDownloadService {

    protected final SyncUpFeignClient syncUpFeignClient;

    /** 每批拉取间隔（秒），避免频繁空拉取 */
    private static final long PULL_INTERVAL_SECONDS = 10;

    /** 上次拉取时间缓存 */
    private volatile String lastPullTime;

    /** 上次拉取时间戳 */
    private volatile long lastPullTimestamp = 0;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected BaseUpSyncDownloadService(SyncUpFeignClient syncUpFeignClient) {
        this.syncUpFeignClient = syncUpFeignClient;
    }

    /**
     * 获取当前微服务模块名称（用于日志和lastTime区分）
     */
    protected abstract String getModuleName();

    /**
     * 处理具体某张表的数据
     * 子类实现具体的数据解析和入库逻辑
     *
     * @param tableName 表名
     * @param jsonData  JSON数据
     * @param batchUuid 批次UUID
     * @return true表示处理成功
     */
    protected abstract boolean processTableData(String tableName, String jsonData, String batchUuid);

    /**
     * 同步下载所有待处理的上行数据（主入口）
     */
    public void syncDownload() {
        String lastTime = getLastDownloadTime();
        log.info("[{}] 开始拉取上行同步数据: lastTime={}", getModuleName(), lastTime);

        try {
            CommonResult<List<Map<String, Object>>> result = syncUpFeignClient.pullUpSync(lastTime);
            if (result == null || result.getData() == null || result.getData().isEmpty()) {
                log.debug("[{}] 没有待处理的上行数据", getModuleName());
                updateLastPullTime();
                return;
            }

            List<Map<String, Object>> dataList = result.getData();
            log.info("[{}] 拉取到 {} 条上行数据", getModuleName(), dataList.size());

            // 收集处理成功的batchUuid
            List<String> successBatchUuids = dataList.stream()
                    .filter(data -> {
                        String tableName = (String) data.get("tableName");
                        String jsonData = (String) data.get("jsonData");
                        String batchUuid = (String) data.get("batchUuid");
                        if (tableName == null || jsonData == null) {
                            log.warn("[{}] 数据异常，跳过: {}", getModuleName(), data);
                            return false;
                        }
                        return processTableData(tableName, jsonData, batchUuid);
                    })
                    .map(data -> (String) data.get("batchUuid"))
                    .collect(Collectors.toList());

            // 批量确认处理完成
            if (!successBatchUuids.isEmpty()) {
                CommonResult<Integer> confirmResult = syncUpFeignClient.batchConfirmUpSync(successBatchUuids);
                log.info("[{}] 批量确认完成: {}/{}", getModuleName(),
                        confirmResult != null ? confirmResult.getData() : 0, successBatchUuids.size());
            }

            // 子类扩展钩子
            afterProcess(dataList);

            updateLastPullTime();

        } catch (Exception e) {
            log.error("[{}] 同步下载上行数据失败: {}", getModuleName(), e.getMessage(), e);
        }
    }

    /**
     * 子类可重写的后处理钩子
     */
    protected void afterProcess(List<Map<String, Object>> dataList) {
        // 默认空实现
    }

    /**
     * 获取上次拉取时间
     */
    protected String getLastDownloadTime() {
        if (lastPullTime == null) {
            // 默认拉取最近一小时的数据
            return LocalDateTime.now().minusHours(1).format(FORMATTER);
        }
        return lastPullTime;
    }

    /**
     * 更新拉取时间
     */
    protected void updateLastPullTime() {
        this.lastPullTime = LocalDateTime.now().format(FORMATTER);
        this.lastPullTimestamp = System.currentTimeMillis();
    }

    /**
     * 检查是否可以拉取（避免频繁拉取）
     */
    protected boolean canPull() {
        return (System.currentTimeMillis() - lastPullTimestamp) >= PULL_INTERVAL_SECONDS * 1000;
    }
}
