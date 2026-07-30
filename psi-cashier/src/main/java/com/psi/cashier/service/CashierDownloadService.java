package com.psi.cashier.service;

import java.util.List;

/**
 * 收银数据下载服务接口
 * 负责从中间同步微服务拉取下行同步数据（收银机配置、收银员、客户等）
 */
public interface CashierDownloadService {

    /**
     * 同步下载所有待处理的下行数据
     */
    void syncDownload();

    /**
     * 异步触发下载（使用虚拟线程）
     */
    void asyncDownload();

    /**
     * 下载指定表类型的数据
     *
     * @param tableName 表名（pos_config, pos_operator, customer 等）
     */
    void downloadByTable(String tableName);

    /**
     * 获取上次下载时间
     *
     * @param syncType 同步类型标识
     * @return 上次下载时间字符串
     */
    String getLastDownloadTime(String syncType);

    /**
     * 更新下载时间
     *
     * @param syncType 同步类型标识
     * @param lastTime 最后下载时间
     */
    void updateLastDownloadTime(String syncType, String lastTime);
}