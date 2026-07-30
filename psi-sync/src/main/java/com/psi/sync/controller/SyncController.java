package com.psi.sync.controller;

import com.psi.common.dto.sync.DownSyncDTO;
import com.psi.common.result.CommonResult;
import com.psi.sync.entity.DownSyncEntity;
import com.psi.sync.entity.UpSyncEntity;
import com.psi.sync.service.DownSyncService;
import com.psi.sync.service.UpSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 数据同步控制器
 * 提供上行和下行同步的REST API接口
 *
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/psi/sync")
public class SyncController {

    private final DownSyncService downSyncService;
    private final UpSyncService upSyncService;

    public SyncController(DownSyncService downSyncService, UpSyncService upSyncService) {
        this.downSyncService = downSyncService;
        this.upSyncService = upSyncService;
    }

    // ==================== 下行同步：进销存 → POS ====================

    /**
     * 插入下行同步数据
     *
     * @param tenantId  租户ID
     * @param shopCode  商铺编码
     * @param tableName 目标业务表名
     * @param jsonData  批量明细JSON List
     * @return 插入结果
     */
    @PostMapping("/down/insert")
    public CommonResult<DownSyncEntity> insertDownSync(
            @RequestParam(value = "tenantId", defaultValue = "default") String tenantId,
            @RequestParam(value = "shopCode", required = false) String shopCode,
            @RequestParam String tableName,
            @RequestBody String jsonData) {

        log.info("接收到下行同步数据插入请求: tenantId={}, shopCode={}, tableName={}", tenantId, shopCode, tableName);

        DownSyncEntity entity = new DownSyncEntity();
        entity.setBatchUuid(UUID.randomUUID().toString().replace("-", ""));
        entity.setTenantId(tenantId);
        entity.setShopCode(shopCode);
        entity.setTableName(tableName);
        entity.setJsonData(jsonData);

        boolean result = downSyncService.insert(entity);
        if (result) {
            return CommonResult.success(entity);
        } else {
            return CommonResult.fail("插入失败");
        }
    }

    /**
     * POS拉取待下载数据
     *
     * @param lastTime 上次拉取时间
     * @return 待下载数据列表
     */
    @GetMapping("/down/pull")
    public CommonResult<List<DownSyncEntity>> pullDownSync(
            @RequestParam(value = "lastTime", required = false) String lastTime) {

        log.info("POS拉取下行同步数据: lastTime={}", lastTime);

        List<DownSyncEntity> dataList = downSyncService.getPendingDownload(lastTime);
        return CommonResult.success(dataList);
    }

    /**
     * POS确认下载完成
     *
     * @param batchUuid 批次UUID
     * @return 确认结果
     */
    @PostMapping("/down/confirm")
    public CommonResult<Boolean> confirmDownSync(@RequestParam String batchUuid) {
        log.info("POS确认下载完成: batchUuid={}", batchUuid);

        boolean result = downSyncService.updateDownloadStatus(batchUuid);
        if (result) {
            return CommonResult.success(true);
        } else {
            return CommonResult.fail("确认失败");
        }
    }

    /**
     * POS批量确认下载完成
     *
     * @param batchUuids 批次UUID列表
     * @return 确认结果
     */
    @PostMapping("/down/batchConfirm")
    public CommonResult<Integer> batchConfirmDownSync(@RequestBody List<String> batchUuids) {
        log.info("POS批量确认下载完成: count={}", batchUuids.size());

        int count = downSyncService.batchUpdateDownloadStatus(batchUuids);
        return CommonResult.success(count);
    }

    // ==================== 上行同步：POS → 进销存 ====================

    /**
     * POS上传单据数据
     *
     * @param tenantId  租户ID
     * @param shopCode  商铺编码
     * @param posSn     收银机设备编码
     * @param tableName 单据对应表名
     * @param jsonData  单据集合JSON
     * @return 插入结果
     */
    @PostMapping("/up/upload")
    public CommonResult<UpSyncEntity> uploadUpSync(
            @RequestParam(value = "tenantId", defaultValue = "default") String tenantId,
            @RequestParam(value = "shopCode", required = false) String shopCode,
            @RequestParam String posSn,
            @RequestParam String tableName,
            @RequestBody String jsonData) {

        log.info("接收到POS上传单据: tenantId={}, shopCode={}, posSn={}, tableName={}", tenantId, shopCode, posSn, tableName);

        UpSyncEntity entity = new UpSyncEntity();
        entity.setBatchUuid(UUID.randomUUID().toString().replace("-", ""));
        entity.setTenantId(tenantId);
        entity.setShopCode(shopCode);
        entity.setPosSn(posSn);
        entity.setTableName(tableName);
        entity.setJsonData(jsonData);

        boolean result = upSyncService.insert(entity);
        if (result) {
            return CommonResult.success(entity);
        } else {
            return CommonResult.fail("上传失败");
        }
    }

    /**
     * 进销存微服务拉取待处理的上行数据（增量拉取）
     *
     * @param lastTime 上次拉取时间
     * @return 待处理数据列表
     */
    @GetMapping("/up/pull")
    public CommonResult<List<UpSyncEntity>> pullUpSync(
            @RequestParam(value = "lastTime", required = false) String lastTime) {

        log.info("进销存拉取上行同步数据: lastTime={}", lastTime);

        List<UpSyncEntity> dataList = upSyncService.getPendingProcess(lastTime);
        return CommonResult.success(dataList);
    }

    /**
     * 查询待处理的上行单据
     *
     * @return 待处理单据列表
     */
    @GetMapping("/up/pending")
    public CommonResult<List<UpSyncEntity>> getPendingUpSync() {
        log.info("查询待处理的上行单据");

        List<UpSyncEntity> dataList = upSyncService.getPendingProcess();
        return CommonResult.success(dataList);
    }

    /**
     * 更新上行单据处理状态
     *
     * @param id         主键ID
     * @param syncStatus 同步状态: 0待处理 1成功 2失败
     * @return 更新结果
     */
    @PostMapping("/up/status")
    public CommonResult<Boolean> updateUpSyncStatus(
            @RequestParam Long id,
            @RequestParam Integer syncStatus) {

        log.info("更新上行单据处理状态: id={}, syncStatus={}", id, syncStatus);

        boolean result = upSyncService.updateProcessStatus(id, syncStatus);
        if (result) {
            return CommonResult.success(true);
        } else {
            return CommonResult.fail("更新失败");
        }
    }

    /**
     * 进销存微服务批量确认上行数据处理完成
     *
     * @param batchUuids 批次UUID列表
     * @return 确认结果
     */
    @PostMapping("/up/batchConfirm")
    public CommonResult<Integer> batchConfirmUpSync(@RequestBody List<String> batchUuids) {
        log.info("进销存批量确认上行数据处理完成: count={}", batchUuids.size());

        int count = upSyncService.batchUpdateProcessStatus(batchUuids, 1);
        return CommonResult.success(count);
    }

    /**
     * 查询上行单据详情
     *
     * @param batchUuid 批次UUID
     * @return 单据详情
     */
    @GetMapping("/up/detail")
    public CommonResult<UpSyncEntity> getUpSyncDetail(@RequestParam String batchUuid) {
        UpSyncEntity entity = upSyncService.getByBatchUuid(batchUuid);
        if (entity != null) {
            return CommonResult.success(entity);
        } else {
            return CommonResult.fail("单据不存在");
        }
    }
}
