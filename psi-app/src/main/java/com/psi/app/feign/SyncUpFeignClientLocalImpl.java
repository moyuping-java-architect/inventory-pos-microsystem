package com.psi.app.feign;

import com.psi.common.feign.SyncUpFeignClient;
import com.psi.common.result.CommonResult;
import com.psi.sync.entity.UpSyncEntity;
import com.psi.sync.mapper.UpSyncMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class SyncUpFeignClientLocalImpl implements SyncUpFeignClient {

    private final UpSyncMapper upSyncMapper;

    @Override
    public CommonResult<List<Map<String, Object>>> pullUpSync(String lastTime) {
        log.info("本地调用: pullUpSync, lastTime={}", lastTime);
        List<UpSyncEntity> entities;
        if (lastTime != null && !lastTime.trim().isEmpty()) {
            entities = upSyncMapper.selectPendingProcessWithTime(lastTime);
        } else {
            entities = upSyncMapper.selectPendingProcess();
        }
        List<Map<String, Object>> result = entities.stream()
                .map(this::entityToMap)
                .collect(Collectors.toList());
        log.info("本地调用 pullUpSync 完成: count={}", result.size());
        return CommonResult.success(result);
    }

    @Override
    public CommonResult<Integer> batchConfirmUpSync(List<String> batchUuids) {
        log.info("本地调用: batchConfirmUpSync, batchUuids={}", batchUuids);
        int count = upSyncMapper.batchUpdateStatusByBatchUuids(batchUuids, 1);
        log.info("本地调用 batchConfirmUpSync 完成: updated={}", count);
        return CommonResult.success(count);
    }

    private Map<String, Object> entityToMap(UpSyncEntity entity) {
        return Map.ofEntries(
                Map.entry("id", entity.getId()),
                Map.entry("batchUuid", entity.getBatchUuid()),
                Map.entry("recordId", entity.getRecordId()),
                Map.entry("tableName", entity.getTableName()),
                Map.entry("businessKey", entity.getBusinessKey()),
                Map.entry("dataVersion", entity.getDataVersion()),
                Map.entry("syncStatus", entity.getSyncStatus()),
                Map.entry("posSn", entity.getPosSn()),
                Map.entry("tenantId", entity.getTenantId()),
                Map.entry("shopCode", entity.getShopCode()),
                Map.entry("jsonData", entity.getJsonData()),
                Map.entry("createTime", entity.getCreateTime()),
                Map.entry("processTime", entity.getProcessTime()),
                Map.entry("retryCount", entity.getRetryCount()),
                Map.entry("errorMsg", entity.getErrorMsg())
        );
    }
}