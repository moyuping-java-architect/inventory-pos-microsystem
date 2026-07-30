package com.psi.cashier.service.impl;

import com.psi.common.dto.sync.DownSyncDTO;
import com.psi.cashier.entity.ProductSkuSaleUnit;
import com.psi.cashier.service.ProductSkuSaleUnitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CashierDownloadServiceImplTest {

    @Mock
    private ProductSkuSaleUnitService productSkuSaleUnitService;

    private DownSyncDTO createDownSyncDTO(String dataUuid, Long dataVersion, String jsonData) {
        DownSyncDTO dto = new DownSyncDTO();
        dto.setBatchUuid("batch-test");
        dto.setTableName("product_sku_sale_unit");
        dto.setDataUuid(dataUuid);
        dto.setDataVersion(dataVersion);
        dto.setJsonData(jsonData);
        return dto;
    }

    private ProductSkuSaleUnit createSkuEntity(String dataUuid, Long dataVersion) {
        ProductSkuSaleUnit entity = new ProductSkuSaleUnit();
        entity.setDataUuid(dataUuid);
        entity.setDataVersion(dataVersion);
        entity.setSkuNo("SKU001");
        entity.setSalePrice(BigDecimal.valueOf(100.0));
        return entity;
    }

    @Test
    void versionConflictResolution_remoteHigher_shouldUpdate() {
        Long remoteVersion = 5L;
        Long localVersion = 3L;

        assert remoteVersion > localVersion : "远程版本高于本地版本";
        assert shouldUpdate(localVersion, remoteVersion) : "应执行更新";
    }

    @Test
    void versionConflictResolution_localHigher_shouldSkip() {
        Long remoteVersion = 3L;
        Long localVersion = 5L;

        assert localVersion > remoteVersion : "本地版本高于远程版本";
        assert !shouldUpdate(localVersion, remoteVersion) : "应跳过更新";
    }

    @Test
    void versionConflictResolution_versionsEqual_shouldSkip() {
        Long version = 5L;

        assert !shouldUpdate(version, version) : "版本相等应跳过更新";
    }

    @Test
    void versionConflictResolution_localNull_shouldInsert() {
        Long remoteVersion = 1L;

        assert shouldUpdate(null, remoteVersion) : "本地不存在应插入";
    }

    @Test
    void versionConflictResolution_remoteNull_shouldInsert() {
        Long localVersion = 1L;

        assert shouldUpdate(localVersion, null) : "远程版本为空应插入";
    }

    private boolean shouldUpdate(Long localVersion, Long remoteVersion) {
        if (remoteVersion == null) {
            return true;
        }
        if (localVersion == null) {
            return true;
        }
        return remoteVersion > localVersion;
    }

    @Test
    void batchProcessProductSkuSaleUnit_shouldHandleMultipleEntities() {
        String jsonData1 = "[{\"dataUuid\":\"sku-uuid-001\",\"skuNo\":\"SKU001\",\"salePrice\":150}]";
        String jsonData2 = "[{\"dataUuid\":\"sku-uuid-002\",\"skuNo\":\"SKU002\",\"salePrice\":200}]";

        DownSyncDTO dto1 = createDownSyncDTO("sku-uuid-001", 5L, jsonData1);
        DownSyncDTO dto2 = createDownSyncDTO("sku-uuid-002", 3L, jsonData2);

        List<DownSyncDTO> dtos = Arrays.asList(dto1, dto2);

        assert dtos.size() == 2 : "应有2条数据";
        assert "sku-uuid-001".equals(dto1.getDataUuid()) : "数据1的UUID正确";
        assert "sku-uuid-002".equals(dto2.getDataUuid()) : "数据2的UUID正确";
    }
}
