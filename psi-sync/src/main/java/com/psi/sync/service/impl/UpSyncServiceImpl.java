package com.psi.sync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.psi.observability.annotation.PsiTrace;
import com.psi.observability.constant.PsiTags;
import com.psi.observability.util.TraceCtx;
import com.psi.sync.entity.UpSyncEntity;
import com.psi.sync.mapper.UpSyncMapper;
import com.psi.sync.service.UpSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 上行同步服务实现类
 * POS → 进销存
 */
@Slf4j
@Service
public class UpSyncServiceImpl implements UpSyncService {

    private final UpSyncMapper upSyncMapper;

    public UpSyncServiceImpl(UpSyncMapper upSyncMapper) {
        this.upSyncMapper = upSyncMapper;
    }

    @Override
    @Transactional
    public boolean insert(UpSyncEntity entity) {
        entity.setSyncStatus(0);
        entity.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        int result = upSyncMapper.insert(entity);
        log.info("上行同步数据插入成功: batchUuid={}, tableName={}, posSn={}",
                entity.getBatchUuid(), entity.getTableName(), entity.getPosSn());
        return result > 0;
    }

    @Override
    @Transactional
    public int batchInsert(List<UpSyncEntity> entities) {
        entities.forEach(entity -> {
            entity.setSyncStatus(0);
            entity.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        int count = 0;
        for (UpSyncEntity entity : entities) {
            try {
                upSyncMapper.insert(entity);
                count++;
            } catch (Exception e) {
                log.warn("上行同步数据批量插入失败: batchUuid={}, error={}", entity.getBatchUuid(), e.getMessage());
            }
        }
        log.info("上行同步数据批量插入完成, 成功: {}, 失败: {}", count, entities.size() - count);
        return count;
    }

    @Override
    public List<UpSyncEntity> getPendingProcess() {
        return upSyncMapper.selectPendingProcess();
    }

    @Override
    public List<UpSyncEntity> getPendingProcess(String lastTime) {
        return upSyncMapper.selectPendingProcessWithTime(lastTime);
    }

    @Override
    @Transactional
    public boolean updateProcessStatus(Long id, Integer syncStatus) {
        UpSyncEntity entity = upSyncMapper.selectById(id);
        if (entity != null) {
            entity.setSyncStatus(syncStatus);
            entity.setProcessTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            int result = upSyncMapper.updateById(entity);
            log.info("上行同步数据处理状态更新: id={}, syncStatus={}", id, syncStatus);
            return result > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public int batchUpdateProcessStatus(List<String> batchUuids, Integer syncStatus) {
        if (batchUuids == null || batchUuids.isEmpty()) {
            return 0;
        }
        int count = upSyncMapper.batchUpdateStatusByBatchUuids(batchUuids, syncStatus);
        log.info("批量更新上行同步数据处理状态完成: count={}/{}", count, batchUuids.size());
        return count;
    }

    @Override
    public UpSyncEntity getByBatchUuid(String batchUuid) {
        return upSyncMapper.selectOne(
                Wrappers.<UpSyncEntity>lambdaQuery()
                        .eq(UpSyncEntity::getBatchUuid, batchUuid));
    }

    @Override
    public boolean existsByRecordId(String recordId) {
        if (recordId == null || recordId.isEmpty()) {
            return false;
        }
        Long count = upSyncMapper.selectCount(
                Wrappers.<UpSyncEntity>lambdaQuery()
                        .eq(UpSyncEntity::getRecordId, recordId));
        return count > 0;
    }

    @Override
    @Transactional
    public boolean insertWithRecordId(UpSyncEntity entity, String recordId) {
        entity.setRecordId(recordId);
        entity.setSyncStatus(0);
        entity.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            int result = upSyncMapper.insert(entity);
            if (result > 0) {
                log.info("上行同步数据插入成功: recordId={}, tableName={}", recordId, entity.getTableName());
                return true;
            }
        } catch (Exception e) {
            // 如果是唯一约束冲突，说明已存在，返回true（幂等）
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                log.debug("recordId已存在，跳过: recordId={}", recordId);
                return true;
            }
            log.error("上行同步数据插入失败: recordId={}, error={}", recordId, e.getMessage());
        }
        return false;
    }

    @Override
    @Transactional
    @PsiTrace(name = "up-sync.insert-ignore", tags = {"recordId"})
    public boolean insertIgnore(UpSyncEntity entity) {
        // 业务自定义埋点 —— 这些 tag 会出现在 SkyWalking UI 的 span 详情
        TraceCtx.putTag(PsiTags.TENANT_ID, entity.getTenantId());
        TraceCtx.putTag(PsiTags.SYNC_TABLE, entity.getTableName());
        TraceCtx.putTag(PsiTags.SYNC_KEY, entity.getBusinessKey());
        TraceCtx.putTag(PsiTags.SYNC_VERSION, entity.getDataVersion());
        TraceCtx.putTag(PsiTags.SYNC_RETRY, entity.getRetryCount());

        int affected = upSyncMapper.insertIgnore(entity);
        if (affected > 0) {
            TraceCtx.putTag(PsiTags.SYNC_ACTION, "INSERT");
            TraceCtx.putTag(PsiTags.SYNC_RESULT, "SUCCESS");
            log.info("上行同步数据幂等插入成功: recordId={}, tableName={}",
                    entity.getRecordId(), entity.getTableName());
            return true;
        }
        TraceCtx.putTag(PsiTags.SYNC_ACTION, "SKIP");
        TraceCtx.putTag(PsiTags.SYNC_RESULT, "EXISTS");
        log.debug("上行同步数据已存在，跳过: recordId={}", entity.getRecordId());
        return false;
    }

    @Override
    @Transactional
    public boolean incrementRetryCount(Long id) {
        int affected = upSyncMapper.incrementRetryCount(id);
        return affected > 0;
    }

    @Override
    @Transactional
    public boolean updateStatusById(Long id, Integer syncStatus, String errorMsg) {
        int affected = upSyncMapper.updateStatusById(id, syncStatus, errorMsg);
        return affected > 0;
    }
}
