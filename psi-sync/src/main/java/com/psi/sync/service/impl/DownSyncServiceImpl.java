package com.psi.sync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.psi.observability.annotation.PsiTrace;
import com.psi.observability.constant.PsiTags;
import com.psi.observability.util.TraceCtx;
import com.psi.sync.entity.DownSyncEntity;
import com.psi.sync.mapper.DownSyncMapper;
import com.psi.sync.service.DownSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 下行同步服务实现类
 * 进销存 → POS
 */
@Slf4j
@Service
public class DownSyncServiceImpl implements DownSyncService {

    private final DownSyncMapper downSyncMapper;

    public DownSyncServiceImpl(DownSyncMapper downSyncMapper) {
        this.downSyncMapper = downSyncMapper;
    }

    @Override
    @Transactional
    @PsiTrace(name = "down-sync.insert", tags = {"batchUuid"})
    public boolean insert(DownSyncEntity entity) {
        // 业务自定义埋点：标记下行数据来源
        TraceCtx.putTag(PsiTags.TENANT_ID, entity.getTenantId());
        TraceCtx.putTag(PsiTags.SYNC_TABLE, entity.getTableName());
        TraceCtx.putTag(PsiTags.SYNC_VERSION, entity.getDataVersion());

        entity.setSyncStatus(0);
        entity.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        int result = downSyncMapper.insert(entity);
        if (result > 0) {
            TraceCtx.putTag(PsiTags.SYNC_ACTION, "INSERT");
            TraceCtx.putTag(PsiTags.SYNC_RESULT, "SUCCESS");
            log.info("下行同步数据插入成功: batchUuid={}, tableName={}", entity.getBatchUuid(), entity.getTableName());
            return true;
        }
        TraceCtx.putTag(PsiTags.SYNC_RESULT, "FAIL");
        return false;
    }

    @Override
    @Transactional
    public int batchInsert(List<DownSyncEntity> entities) {
        entities.forEach(entity -> {
            entity.setSyncStatus(0);
            entity.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        int count = 0;
        for (DownSyncEntity entity : entities) {
            try {
                downSyncMapper.insert(entity);
                count++;
            } catch (Exception e) {
                log.warn("下行同步数据批量插入失败: batchUuid={}, error={}", entity.getBatchUuid(), e.getMessage());
            }
        }
        log.info("下行同步数据批量插入完成, 成功: {}, 失败: {}", count, entities.size() - count);
        return count;
    }

    @Override
    public List<DownSyncEntity> getPendingDownload(String lastTime) {
        return downSyncMapper.selectPendingDownload(lastTime);
    }

    @Override
    @Transactional
    public boolean updateDownloadStatus(String batchUuid) {
        DownSyncEntity entity = downSyncMapper.selectOne(
                Wrappers.<DownSyncEntity>lambdaQuery()
                        .eq(DownSyncEntity::getBatchUuid, batchUuid));
        if (entity != null) {
            entity.setSyncStatus(1);
            entity.setLastDownloadTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            int result = downSyncMapper.updateById(entity);
            log.info("下行同步数据下载状态更新: batchUuid={}", batchUuid);
            return result > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public int batchUpdateDownloadStatus(List<String> batchUuids) {
        if (batchUuids == null || batchUuids.isEmpty()) {
            return 0;
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int count = downSyncMapper.batchUpdateDownloadStatusByBatchUuids(batchUuids, 1, now);

        log.info("批量更新下行同步数据下载状态完成: count={}/{}", count, batchUuids.size());
        return count;
    }

    @Override
    public DownSyncEntity getByBatchUuid(String batchUuid) {
        return downSyncMapper.selectOne(
                Wrappers.<DownSyncEntity>lambdaQuery()
                        .eq(DownSyncEntity::getBatchUuid, batchUuid));
    }
}