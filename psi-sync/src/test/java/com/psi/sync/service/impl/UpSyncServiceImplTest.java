package com.psi.sync.service.impl;

import com.psi.sync.entity.UpSyncEntity;
import com.psi.sync.mapper.UpSyncMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UpSyncServiceImpl 单元测试
 * 测试幂等插入、状态更新、重试计数等核心功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpSyncServiceImplTest {

    @Mock
    private UpSyncMapper upSyncMapper;

    @InjectMocks
    private UpSyncServiceImpl upSyncService;

    private UpSyncEntity createTestEntity(String recordId) {
        UpSyncEntity entity = new UpSyncEntity();
        entity.setRecordId(recordId);
        entity.setTenantId("tenant_001");
        entity.setTableName("order_main");
        entity.setBusinessKey("ORDER20240717001");
        entity.setDataVersion(1L);
        entity.setJsonData("{\"orderNo\":\"ORDER20240717001\",\"amount\":100}");
        return entity;
    }

    @Test
    void insertIgnore_shouldReturnTrueWhenNewRecord() {
        UpSyncEntity entity = createTestEntity("tenant_001:order_main:ORDER20240717001");
        when(upSyncMapper.insertIgnore(any(UpSyncEntity.class))).thenReturn(1);

        boolean result = upSyncService.insertIgnore(entity);

        assert result : "新记录应插入成功";
        verify(upSyncMapper).insertIgnore(any(UpSyncEntity.class));
    }

    @Test
    void insertIgnore_shouldReturnFalseWhenDuplicate() {
        UpSyncEntity entity = createTestEntity("tenant_001:order_main:ORDER20240717001");
        when(upSyncMapper.insertIgnore(any(UpSyncEntity.class))).thenReturn(0);

        boolean result = upSyncService.insertIgnore(entity);

        assert !result : "重复记录应被忽略";
        verify(upSyncMapper).insertIgnore(any(UpSyncEntity.class));
    }

    @Test
    void insertWithRecordId_shouldHandleDuplicateKeyException() {
        UpSyncEntity entity = createTestEntity("tenant_001:order_main:ORDER20240717001");
        when(upSyncMapper.insert(any(UpSyncEntity.class)))
                .thenThrow(new RuntimeException("Duplicate entry 'tenant_001:order_main:ORDER20240717001' for key 'uk_record_id'"));

        boolean result = upSyncService.insertWithRecordId(entity, "tenant_001:order_main:ORDER20240717001");

        assert result : "唯一键冲突应返回true（幂等）";
        verify(upSyncMapper).insert(any(UpSyncEntity.class));
    }

    @Test
    void insertWithRecordId_shouldReturnFalseOnOtherException() {
        UpSyncEntity entity = createTestEntity("tenant_001:order_main:ORDER20240717001");
        when(upSyncMapper.insert(any(UpSyncEntity.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        boolean result = upSyncService.insertWithRecordId(entity, "tenant_001:order_main:ORDER20240717001");

        assert !result : "非唯一键异常应返回false";
        verify(upSyncMapper).insert(any(UpSyncEntity.class));
    }

    @Test
    void updateStatusById_shouldReturnTrueWhenSuccess() {
        when(upSyncMapper.updateStatusById(1L, 1, null)).thenReturn(1);

        boolean result = upSyncService.updateStatusById(1L, 1, null);

        assert result : "更新成功应返回true";
        verify(upSyncMapper).updateStatusById(1L, 1, null);
    }

    @Test
    void updateStatusById_shouldReturnFalseWhenNotFound() {
        when(upSyncMapper.updateStatusById(999L, 1, null)).thenReturn(0);

        boolean result = upSyncService.updateStatusById(999L, 1, null);

        assert !result : "记录不存在应返回false";
        verify(upSyncMapper).updateStatusById(999L, 1, null);
    }

    @Test
    void incrementRetryCount_shouldReturnTrueWhenSuccess() {
        when(upSyncMapper.incrementRetryCount(1L)).thenReturn(1);

        boolean result = upSyncService.incrementRetryCount(1L);

        assert result : "递增成功应返回true";
        verify(upSyncMapper).incrementRetryCount(1L);
    }

    @Test
    void incrementRetryCount_shouldReturnFalseWhenNotFound() {
        when(upSyncMapper.incrementRetryCount(999L)).thenReturn(0);

        boolean result = upSyncService.incrementRetryCount(999L);

        assert !result : "记录不存在应返回false";
        verify(upSyncMapper).incrementRetryCount(999L);
    }

    @Test
    void insertIgnore_shouldPreserveBusinessKeyAndVersion() {
        UpSyncEntity entity = createTestEntity("tenant_001:order_main:ORDER20240717001");
        when(upSyncMapper.insertIgnore(any(UpSyncEntity.class))).thenReturn(1);

        upSyncService.insertIgnore(entity);

        verify(upSyncMapper).insertIgnore(argThat(e ->
                "ORDER20240717001".equals(e.getBusinessKey()) &&
                        1L == (e.getDataVersion() != null ? e.getDataVersion() : 0) &&
                        "tenant_001".equals(e.getTenantId())
        ));
    }
}
