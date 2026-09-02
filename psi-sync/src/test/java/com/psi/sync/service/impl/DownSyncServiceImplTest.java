package com.psi.sync.service.impl;

import com.psi.sync.entity.DownSyncEntity;
import com.psi.sync.mapper.DownSyncMapper;
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
 * DownSyncServiceImpl 单元测试
 * 测试数据版本号传递、下行数据写入等核心功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DownSyncServiceImplTest {

    @Mock
    private DownSyncMapper downSyncMapper;

    @InjectMocks
    private DownSyncServiceImpl downSyncService;

    private DownSyncEntity createTestEntity(String dataUuid, Long dataVersion) {
        DownSyncEntity entity = new DownSyncEntity();
        entity.setBatchUuid("batch-" + dataUuid);
        entity.setTenantId("tenant_001");
        entity.setTableName("product_sku_sale_unit");
        entity.setDataUuid(dataUuid);
        entity.setDataVersion(dataVersion);
        entity.setJsonData("{\"skuNo\":\"SKU001\",\"price\":100}");
        return entity;
    }

    @Test
    void insert_shouldReturnTrueWhenSuccess() {
        DownSyncEntity entity = createTestEntity("sku-uuid-001", 1L);
        when(downSyncMapper.insert(any(DownSyncEntity.class))).thenReturn(1);

        boolean result = downSyncService.insert(entity);

        assert result : "插入成功应返回true";
        verify(downSyncMapper).insert(any(DownSyncEntity.class));
    }

    @Test
    void insert_shouldReturnFalseWhenFailed() {
        DownSyncEntity entity = createTestEntity("sku-uuid-001", 1L);
        when(downSyncMapper.insert(any(DownSyncEntity.class))).thenReturn(0);

        boolean result = downSyncService.insert(entity);

        assert !result : "插入失败应返回false";
        verify(downSyncMapper).insert(any(DownSyncEntity.class));
    }

    @Test
    void insert_shouldPreserveDataUuidAndVersion() {
        DownSyncEntity entity = createTestEntity("sku-uuid-001", 5L);
        when(downSyncMapper.insert(any(DownSyncEntity.class))).thenReturn(1);

        downSyncService.insert(entity);

        verify(downSyncMapper).insert(argThat(e ->
                "sku-uuid-001".equals(e.getDataUuid()) &&
                        5L == (e.getDataVersion() != null ? e.getDataVersion() : 0) &&
                        "product_sku_sale_unit".equals(e.getTableName())
        ));
    }

    @Test
    void insert_shouldHandleNullDataVersion() {
        DownSyncEntity entity = createTestEntity("sku-uuid-001", null);
        entity.setDataVersion(null);
        when(downSyncMapper.insert(any(DownSyncEntity.class))).thenReturn(1);

        boolean result = downSyncService.insert(entity);

        assert result : "null版本号应能正常插入";
        verify(downSyncMapper).insert(any(DownSyncEntity.class));
    }
}
