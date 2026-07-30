package com.psi.cashier.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.cashier.entity.CustomerEntity;
import com.psi.cashier.entity.OperatorEntity;
import com.psi.cashier.entity.ProductSkuSaleUnit;
import com.psi.cashier.entity.SyncLogEntity;
import com.psi.cashier.entity.SysConfigEntity;
import com.psi.cashier.feign.SyncDownFeignClient;
import com.psi.cashier.mapper.SyncLogMapper;
import com.psi.cashier.mapper.SysConfigMapper;
import com.psi.cashier.service.CashierDownloadService;
import com.psi.cashier.service.CustomerService;
import com.psi.cashier.service.OperatorService;
import com.psi.cashier.service.ProductSkuSaleUnitService;
import com.psi.common.context.VirtualThreadContextWrapper;
import com.psi.common.dto.sync.DownSyncDTO;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.result.CommonResult;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收银数据下载服务实现
 * 通过 Feign 从中间同步微服务拉取下行数据，写入本地 SQLite，并回写确认状态
 * 使用虚拟线程异步执行，不阻塞收银主流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashierDownloadServiceImpl implements CashierDownloadService {

    private final SyncDownFeignClient syncDownFeignClient;
    private final SyncLogMapper syncLogMapper;
    private final SysConfigMapper sysConfigMapper;
    private final ObjectMapper objectMapper;
    private final BatchUtils batchUtils;
    private final OperatorService operatorService;
    private final CustomerService customerService;
    private final ProductSkuSaleUnitService productSkuSaleUnitService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 下行同步类型标识 */
    private static final String SYNC_TYPE_DOWN = "down";

    @Override
    public void syncDownload() {
        String lastTime = getLastDownloadTime(SYNC_TYPE_DOWN);
        log.info("开始下行同步拉取: lastTime={}", lastTime);

        try {
            CommonResult<List<DownSyncDTO>> result = syncDownFeignClient.pullDownSync(lastTime);
            if (result == null || !result.isSuccess()) {
                log.warn("拉取下行同步数据失败: result={}", result);
                return;
            }

            List<DownSyncDTO> dataList = result.getData();
            if (dataList == null || dataList.isEmpty()) {
                log.debug("无新的下行同步数据");
                return;
            }

            log.info("拉取到 {} 条下行同步数据", dataList.size());

            // ========== 1. 按表名分组，批量处理（减少数据库交互次数） ==========
            Map<String, List<DownSyncDTO>> groupedByTable = dataList.stream()
                    .collect(Collectors.groupingBy(DownSyncDTO::getTableName));

            List<String> successBatchUuids = new ArrayList<>();
            int failCount = 0;
            String maxCreateTime = lastTime;

            for (Map.Entry<String, List<DownSyncDTO>> entry : groupedByTable.entrySet()) {
                String tableName = entry.getKey();
                List<DownSyncDTO> dtos = entry.getValue();

                try {
                    // 批量处理整个表组，返回成功的 batchUuid 列表
                    List<String> batchUuids = batchProcessTable(tableName, dtos);
                    successBatchUuids.addAll(batchUuids);

                    // 记录最大的创建时间
                    for (DownSyncDTO dto : dtos) {
                        if (dto.getCreateTime() != null && (maxCreateTime == null || dto.getCreateTime().compareTo(maxCreateTime) > 0)) {
                            maxCreateTime = dto.getCreateTime();
                        }
                    }
                } catch (Exception e) {
                    failCount += dtos.size();
                    log.error("批量处理表数据失败: tableName={}, count={}, error={}",
                            tableName, dtos.size(), e.getMessage());
                }
            }

            // ========== 2. 一次性批量确认下载状态 ==========
            if (!successBatchUuids.isEmpty()) {
                try {
                    syncDownFeignClient.batchConfirmDownSync(successBatchUuids);
                    log.info("批量确认下载完成: count={}", successBatchUuids.size());
                } catch (Exception e) {
                    log.error("批量确认下载失败", e);
                }
            }

            // 更新下载时间
            if (maxCreateTime != null && !maxCreateTime.equals(lastTime)) {
                updateLastDownloadTime(SYNC_TYPE_DOWN, maxCreateTime);
            }

            log.info("下行同步拉取完成: 成功={}, 失败={}, 共={}", successBatchUuids.size(), failCount, dataList.size());

        } catch (Exception e) {
            log.error("下行同步拉取出错", e);
        }
    }

    @Override
    public void asyncDownload() {
        VirtualThreadContextWrapper.executeAsync(() -> {
            log.info("虚拟线程异步触发下行同步");
            try {
                syncDownload();
            } catch (Exception e) {
                log.error("异步下行同步执行失败", e);
            }
        });
    }

    @Override
    public void downloadByTable(String tableName) {
        VirtualThreadContextWrapper.executeAsync(() -> {
            log.info("虚拟线程异步下载指定表数据: tableName={}", tableName);
            try {
                String lastTime = getLastDownloadTime(SYNC_TYPE_DOWN);
                CommonResult<List<DownSyncDTO>> result = syncDownFeignClient.pullDownSync(lastTime);
                if (result == null || !result.isSuccess() || result.getData() == null) {
                    return;
                }

                // 过滤出指定表的数据，然后批量处理
                List<DownSyncDTO> filteredDtos = result.getData().stream()
                        .filter(dto -> tableName.equals(dto.getTableName()))
                        .collect(Collectors.toList());

                if (filteredDtos.isEmpty()) {
                    log.info("无指定表的下行同步数据: tableName={}", tableName);
                    return;
                }

                List<String> batchUuids = batchProcessTable(tableName, filteredDtos);

                // 一次性批量确认
                if (!batchUuids.isEmpty()) {
                    syncDownFeignClient.batchConfirmDownSync(batchUuids);
                }

                updateLastDownloadTime(SYNC_TYPE_DOWN, LocalDateTime.now().format(TIME_FORMATTER));
                log.info("指定表数据下载完成: tableName={}, count={}", tableName, batchUuids.size());
            } catch (Exception e) {
                log.error("指定表数据下载失败: tableName={}", tableName, e);
            }
        });
    }

    @Override
    public String getLastDownloadTime(String syncType) {
        SyncLogEntity logEntity = syncLogMapper.selectByType(syncType);
        return logEntity != null ? logEntity.getLastTime() : null;
    }

    @Override
    public void updateLastDownloadTime(String syncType, String lastTime) {
        int updated = syncLogMapper.updateLastTime(syncType, lastTime);
        if (updated == 0) {
            SyncLogEntity entity = new SyncLogEntity();
            entity.setType(syncType);
            entity.setLastTime(lastTime);
            syncLogMapper.insert(entity);
        }
    }

    // ==================== 批量处理器（按表分组 → 批量查 → 拆插入/更新组） ====================

    /**
     * 批量处理一个表组的所有数据
     * 策略：按表分组 → 解析所有JSON → 一次性查询已存在dataUuid → 拆分为插入组和更新组
     *
     * @return 成功处理的 batchUuid 列表
     */
    private List<String> batchProcessTable(String tableName, List<DownSyncDTO> dtos) {
        List<String> batchUuids;
        switch (tableName) {
            case "pos_config":
                batchUuids = batchProcessPosConfig(dtos);
                break;
            case "pos_operator":
                batchUuids = batchProcessOperator(dtos);
                break;
            case "customer":
                batchUuids = batchProcessCustomer(dtos);
                break;
            case "goods_sku_sale_unit":
                batchUuids = batchProcessProductSkuSaleUnit(dtos);
                break;
            default:
                log.warn("不支持的同步表类型: tableName={}", tableName);
                batchUuids = new ArrayList<>();
        }
        return batchUuids;
    }

    /**
     * 批量处理收银机配置（pos_config → sys_config）
     * sys_config 只有一条记录，直接取最后一个配置覆盖
     */
    private List<String> batchProcessPosConfig(List<DownSyncDTO> dtos) {
        if (dtos.isEmpty()) return Collections.emptyList();

        // 取最后一条配置数据覆盖本地（本地只有一条记录）
        DownSyncDTO lastDto = dtos.get(dtos.size() - 1);
        try {
            String jsonData = lastDto.getJsonData();
            if (jsonData == null || jsonData.isEmpty()) {
                return Collections.emptyList();
            }

            SysConfigEntity posConfig = parseJsonToEntity(jsonData, SysConfigEntity.class, "pos_config");
            if (posConfig == null || posConfig.getDataUuid() == null) {
                log.warn("收银机配置 dataUuid 为空，跳过");
                return Collections.emptyList();
            }

            SysConfigEntity exist = sysConfigMapper.selectFirst();
            String now = LocalDateTime.now().format(TIME_FORMATTER);

            if (exist != null) {
                posConfig.setId(exist.getId());
                posConfig.setUpdateTime(now);
                sysConfigMapper.updateById(posConfig);
            } else {
                posConfig.setUpdateTime(now);
                sysConfigMapper.insert(posConfig);
            }

            return dtos.stream().map(DownSyncDTO::getBatchUuid).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("批量处理收银机配置失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量处理操作员/收银员数据（pos_operator → operator 表）
     * 1. 解析所有JSON → 2. 一次性查询已存在dataUuid → 3. 版本号冲突解决 → 4. 拆分为插入组和更新组
     * 5. BatchUtils批量INSERT + BatchUtils批量UPDATE（仅2次数据库写入）
     */
    private List<String> batchProcessOperator(List<DownSyncDTO> dtos) {
        // 1. 解析所有JSON为操作员实体列表，并映射远程版本号
        List<OperatorEntity> allEntities = new ArrayList<>();
        Map<String, Long> remoteVersionMap = new HashMap<>();
        for (DownSyncDTO dto : dtos) {
            try {
                List<OperatorEntity> parsed = parseJsonToEntityList(
                        dto.getJsonData(), OperatorEntity.class, "pos_operator");
                for (OperatorEntity entity : parsed) {
                    entity.setDataVersion(dto.getDataVersion() != null ? dto.getDataVersion() : 0L);
                    allEntities.add(entity);
                    if (entity.getDataUuid() != null) {
                        remoteVersionMap.put(entity.getDataUuid(), entity.getDataVersion());
                    }
                }
            } catch (Exception e) {
                log.warn("操作员数据解析失败，跳过: batchUuid={}", dto.getBatchUuid());
            }
        }

        if (allEntities.isEmpty()) return Collections.emptyList();

        // 2. 提取所有 dataUuid，一次性查询已存在的记录
        List<String> dataUuids = allEntities.stream()
                .map(OperatorEntity::getDataUuid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (dataUuids.isEmpty()) {
            log.warn("操作员数据 dataUuid 均为空，跳过");
            return Collections.emptyList();
        }

        List<OperatorEntity> existingList = operatorService.list(
                Wrappers.<OperatorEntity>lambdaQuery().in(OperatorEntity::getDataUuid, dataUuids));
        Map<String, Long> localVersionMap = existingList.stream()
                .collect(Collectors.toMap(OperatorEntity::getDataUuid,
                        e -> e.getDataVersion() != null ? e.getDataVersion() : 0L, (a, b) -> a));
        Set<String> existingDataUuids = localVersionMap.keySet();

        // 3. 拆分为插入组和更新组（带版本号冲突解决）
        String now = LocalDateTime.now().format(TIME_FORMATTER);
        List<OperatorEntity> insertGroup = new ArrayList<>();
        List<OperatorEntity> updateGroup = new ArrayList<>();
        int skipCount = 0;

        for (OperatorEntity entity : allEntities) {
            if (entity.getDataUuid() == null) continue;

            // 版本号冲突解决：本地版本 >= 远程版本，跳过（总部旧数据不覆盖本地新数据）
            Long localVersion = localVersionMap.getOrDefault(entity.getDataUuid(), 0L);
            Long remoteVersion = remoteVersionMap.getOrDefault(entity.getDataUuid(), 0L);
            if (localVersion >= remoteVersion) {
                skipCount++;
                continue;
            }

            if (existingDataUuids.contains(entity.getDataUuid())) {
                // 更新组：从已存在记录中获取本地ID
                existingList.stream()
                        .filter(e -> entity.getDataUuid().equals(e.getDataUuid()))
                        .findFirst()
                        .ifPresent(exist -> {
                            entity.setOperatorId(exist.getOperatorId());
                            entity.setCreateTime(exist.getCreateTime());
                            entity.setDelFlag(null);
                            entity.setStatus(null);
                        });
                updateGroup.add(entity);
            } else {
                // 插入组
                if (entity.getDelFlag() == null) entity.setDelFlag(0);
                if (entity.getStatus() == null) entity.setStatus(1);
                if (entity.getCreateTime() == null) entity.setCreateTime(now);
                insertGroup.add(entity);
            }
        }

        // 4. BatchUtils批量操作（自动分片 + JDBC BATCH）
        if (!insertGroup.isEmpty()) {
            batchUtils.saveBatch(operatorService, insertGroup);
        }
        if (!updateGroup.isEmpty()) {
            batchUtils.updateBatchById(operatorService, updateGroup);
        }

        log.info("操作员数据批量处理完成: 新增={}, 更新={}, 跳过={}, 总数={}",
                insertGroup.size(), updateGroup.size(), skipCount, allEntities.size());

        return dtos.stream().map(DownSyncDTO::getBatchUuid).collect(Collectors.toList());
    }

    /**
     * 批量处理客户数据（customer 表）
     * 1. 解析所有JSON → 2. 一次性查询已存在dataUuid → 3. 版本号冲突解决 → 4. 拆分为插入组和更新组
     * 5. BatchUtils批量INSERT + BatchUtils批量UPDATE（仅2次数据库写入）
     */
    private List<String> batchProcessCustomer(List<DownSyncDTO> dtos) {
        // 1. 解析所有JSON为客户实体列表，并映射远程版本号
        List<CustomerEntity> allEntities = new ArrayList<>();
        Map<String, Long> remoteVersionMap = new HashMap<>();
        for (DownSyncDTO dto : dtos) {
            try {
                List<CustomerEntity> parsed = parseJsonToEntityList(
                        dto.getJsonData(), CustomerEntity.class, "customer");
                for (CustomerEntity entity : parsed) {
                    entity.setDataVersion(dto.getDataVersion() != null ? dto.getDataVersion() : 0L);
                    allEntities.add(entity);
                    if (entity.getDataUuid() != null) {
                        remoteVersionMap.put(entity.getDataUuid(), entity.getDataVersion());
                    }
                }
            } catch (Exception e) {
                log.warn("客户数据解析失败，跳过: batchUuid={}", dto.getBatchUuid());
            }
        }

        if (allEntities.isEmpty()) return Collections.emptyList();

        // 2. 提取所有 dataUuid，一次性查询已存在的记录
        List<String> dataUuids = allEntities.stream()
                .map(CustomerEntity::getDataUuid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (dataUuids.isEmpty()) {
            log.warn("客户数据 dataUuid 均为空，跳过");
            return Collections.emptyList();
        }

        List<CustomerEntity> existingList = customerService.list(
                Wrappers.<CustomerEntity>lambdaQuery().in(CustomerEntity::getDataUuid, dataUuids));
        Map<String, Long> localVersionMap = existingList.stream()
                .collect(Collectors.toMap(CustomerEntity::getDataUuid,
                        e -> e.getDataVersion() != null ? e.getDataVersion() : 0L, (a, b) -> a));
        Set<String> existingDataUuids = localVersionMap.keySet();

        // 3. 拆分为插入组和更新组（带版本号冲突解决）
        String now = LocalDateTime.now().format(TIME_FORMATTER);
        List<CustomerEntity> insertGroup = new ArrayList<>();
        List<CustomerEntity> updateGroup = new ArrayList<>();
        int skipCount = 0;

        for (CustomerEntity entity : allEntities) {
            if (entity.getDataUuid() == null) continue;

            // 版本号冲突解决
            Long localVersion = localVersionMap.getOrDefault(entity.getDataUuid(), 0L);
            Long remoteVersion = remoteVersionMap.getOrDefault(entity.getDataUuid(), 0L);
            if (localVersion >= remoteVersion) {
                skipCount++;
                continue;
            }

            if (existingDataUuids.contains(entity.getDataUuid())) {
                existingList.stream()
                        .filter(e -> entity.getDataUuid().equals(e.getDataUuid()))
                        .findFirst()
                        .ifPresent(exist -> {
                            entity.setId(exist.getId());
                            entity.setCreateTime(exist.getCreateTime());
                            entity.setUpdateTime(now);
                            entity.setDelFlag(null);
                            entity.setStatus(null);
                        });
                updateGroup.add(entity);
            } else {
                if (entity.getDelFlag() == null) entity.setDelFlag(0);
                if (entity.getStatus() == null) entity.setStatus(1);
                if (entity.getCreateTime() == null) entity.setCreateTime(now);
                if (entity.getUpdateTime() == null) entity.setUpdateTime(now);
                insertGroup.add(entity);
            }
        }

        // 4. BatchUtils批量操作（自动分片 + JDBC BATCH）
        if (!insertGroup.isEmpty()) {
            batchUtils.saveBatch(customerService, insertGroup);
        }
        if (!updateGroup.isEmpty()) {
            batchUtils.updateBatchById(customerService, updateGroup);
        }

        log.info("客户数据批量处理完成: 新增={}, 更新={}, 跳过={}, 总数={}",
                insertGroup.size(), updateGroup.size(), skipCount, allEntities.size());

        return dtos.stream().map(DownSyncDTO::getBatchUuid).collect(Collectors.toList());
    }

    /**
     * 批量处理 SKU 销售单位（goods_sku_sale_unit → product_sku_sale_unit）
     */
    private List<String> batchProcessProductSkuSaleUnit(List<DownSyncDTO> dtos) {
        List<ProductSkuSaleUnit> allEntities = new ArrayList<>();
        Map<String, Long> remoteVersionMap = new HashMap<>();
        for (DownSyncDTO dto : dtos) {
            try {
                JsonNode node = objectMapper.readTree(dto.getJsonData());
                if (node.isArray()) {
                    for (JsonNode item : node) {
                        ProductSkuSaleUnit entity = convertToProductSkuSaleUnit(item);
                        entity.setDataVersion(dto.getDataVersion() != null ? dto.getDataVersion() : 0L);
                        allEntities.add(entity);
                        if (entity.getDataUuid() != null) {
                            remoteVersionMap.put(entity.getDataUuid(), entity.getDataVersion());
                        }
                    }
                } else {
                    ProductSkuSaleUnit entity = convertToProductSkuSaleUnit(node);
                    entity.setDataVersion(dto.getDataVersion() != null ? dto.getDataVersion() : 0L);
                    allEntities.add(entity);
                    if (entity.getDataUuid() != null) {
                        remoteVersionMap.put(entity.getDataUuid(), entity.getDataVersion());
                    }
                }
            } catch (Exception e) {
                log.warn("SKU销售单位数据解析失败，跳过: batchUuid={}", dto.getBatchUuid());
            }
        }

        if (allEntities.isEmpty()) return Collections.emptyList();

        List<String> dataUuids = allEntities.stream()
                .map(ProductSkuSaleUnit::getDataUuid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (dataUuids.isEmpty()) {
            log.warn("SKU销售单位数据 dataUuid 均为空，跳过");
            return Collections.emptyList();
        }

        List<ProductSkuSaleUnit> existingList = productSkuSaleUnitService.list(
                Wrappers.<ProductSkuSaleUnit>lambdaQuery().in(ProductSkuSaleUnit::getDataUuid, dataUuids));
        Map<String, Long> localVersionMap = existingList.stream()
                .collect(Collectors.toMap(ProductSkuSaleUnit::getDataUuid,
                        e -> e.getDataVersion() != null ? e.getDataVersion() : 0L, (a, b) -> a));
        Set<String> existingDataUuids = localVersionMap.keySet();

        String now = LocalDateTime.now().format(TIME_FORMATTER);
        List<ProductSkuSaleUnit> insertGroup = new ArrayList<>();
        List<ProductSkuSaleUnit> updateGroup = new ArrayList<>();
        int skipCount = 0;

        for (ProductSkuSaleUnit entity : allEntities) {
            if (entity.getDataUuid() == null) continue;
            if (entity.getStatus() == null) entity.setStatus(1);
            if (entity.getDelFlag() == null) entity.setDelFlag(0);

            // 版本号冲突解决
            Long localVersion = localVersionMap.getOrDefault(entity.getDataUuid(), 0L);
            Long remoteVersion = remoteVersionMap.getOrDefault(entity.getDataUuid(), 0L);
            if (localVersion >= remoteVersion) {
                skipCount++;
                continue;
            }

            if (existingDataUuids.contains(entity.getDataUuid())) {
                existingList.stream()
                        .filter(e -> entity.getDataUuid().equals(e.getDataUuid()))
                        .findFirst()
                        .ifPresent(exist -> {
                            entity.setId(exist.getId());
                            entity.setCreateTime(exist.getCreateTime());
                            entity.setUpdateTime(now);
                        });
                updateGroup.add(entity);
            } else {
                if (entity.getCreateTime() == null) entity.setCreateTime(now);
                if (entity.getUpdateTime() == null) entity.setUpdateTime(now);
                insertGroup.add(entity);
            }
        }

        if (!insertGroup.isEmpty()) {
            batchUtils.saveBatch(productSkuSaleUnitService, insertGroup);
        }
        if (!updateGroup.isEmpty()) {
            batchUtils.updateBatchById(productSkuSaleUnitService, updateGroup);
        }

        log.info("SKU销售单位数据批量处理完成: 新增={}, 更新={}, 跳过={}, 总数={}",
                insertGroup.size(), updateGroup.size(), skipCount, allEntities.size());
        return dtos.stream().map(DownSyncDTO::getBatchUuid).collect(Collectors.toList());
    }

    private ProductSkuSaleUnit convertToProductSkuSaleUnit(JsonNode node) {
        ProductSkuSaleUnit entity = new ProductSkuSaleUnit();
        entity.setDataUuid(textNode(node, "dataUuid"));
        entity.setDataVersion(longNode(node, "dataVersion"));
        entity.setTenantId(longNode(node, "tenantId"));
        entity.setSkuId(longNode(node, "skuId"));
        // 商品端使用 skuCode，收银端本地表使用 skuNo
        entity.setSkuNo(textNode(node, "skuCode"));
        entity.setBarcode(textNode(node, "barcode"));
        entity.setGoodsName(textNode(node, "goodsName"));
        entity.setCategoryId(longNode(node, "categoryId"));
        entity.setBrandId(longNode(node, "brandId"));
        entity.setSaleUnitId(longNode(node, "saleUnitId"));
        entity.setSaleUnitName(textNode(node, "saleUnitName"));
        entity.setSaleUnitSymbol(textNode(node, "saleUnitSymbol"));
        entity.setConversionRate(decimalNode(node, "conversionRate"));
        entity.setPackageSpec(textNode(node, "packageSpec"));
        entity.setSalePrice(decimalNode(node, "salePrice"));
        entity.setTaxRate(decimalNode(node, "taxRate"));
        entity.setIsTaxInclusive(intNode(node, "isTaxInclusive"));
        entity.setSalePriceUsd(decimalNode(node, "salePriceUsd"));
        entity.setBatchManaged(intNode(node, "batchManaged"));
        entity.setIsDefault(intNode(node, "isDefault"));
        entity.setStatus(intNode(node, "status"));
        entity.setSortOrder(intNode(node, "sortOrder"));
        entity.setDelFlag(intNode(node, "delFlag"));
        entity.setCreateBy(longNode(node, "createBy"));
        entity.setCreateTime(textNode(node, "createTime"));
        entity.setUpdateBy(longNode(node, "updateBy"));
        entity.setUpdateTime(textNode(node, "updateTime"));
        return entity;
    }

    private String textNode(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    private Long longNode(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asLong() : null;
    }

    private Integer intNode(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asInt() : null;
    }

    private java.math.BigDecimal decimalNode(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) return null;
        return new java.math.BigDecimal(node.get(field).asText());
    }

    // ==================== JSON解析工具 ====================

    /**
     * 将JSON字符串解析为指定实体
     * 兼容格式：直接对象 / 包装在Map的tableName字段中
     */
    private <T> T parseJsonToEntity(String jsonData, Class<T> entityClass, String tableName) throws Exception {
        try {
            return objectMapper.readValue(jsonData, entityClass);
        } catch (Exception e) {
            // 兼容：可能包装在 { "pos_config": {...} } 格式中
            @SuppressWarnings("unchecked")
            Map<String, Object> wrapped = objectMapper.readValue(jsonData, Map.class);
            Object inner = wrapped.get(tableName);
            if (inner != null) {
                String innerJson = objectMapper.writeValueAsString(inner);
                return objectMapper.readValue(innerJson, entityClass);
            }
            throw e;
        }
    }

    /**
     * 将JSON字符串解析为实体列表
     * 兼容格式：JSON数组 / 单个对象 / 包装在Map的tableName字段中
     */
    private <T> List<T> parseJsonToEntityList(String jsonData, Class<T> entityClass, String tableName) throws Exception {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String trimmed = jsonData.trim();
        try {
            if (trimmed.startsWith("[")) {
                return objectMapper.readValue(jsonData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
            } else {
                T single = objectMapper.readValue(jsonData, entityClass);
                return Collections.singletonList(single);
            }
        } catch (Exception e) {
            // 兼容：可能包装在 { "pos_operator": [...] } 或 { "customer": {...} } 格式中
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> wrapped = objectMapper.readValue(jsonData, Map.class);
                Object inner = wrapped.get(tableName);
                if (inner != null) {
                    String innerJson = objectMapper.writeValueAsString(inner);
                    if (innerJson.trim().startsWith("[")) {
                        return objectMapper.readValue(innerJson,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
                    } else {
                        T single = objectMapper.readValue(innerJson, entityClass);
                        return Collections.singletonList(single);
                    }
                }
                throw e;
            } catch (Exception e2) {
                log.error("JSON解析兼容格式也失败: tableName={}, jsonData={}", tableName, jsonData.substring(0, Math.min(100, jsonData.length())));
                throw e2;
            }
        }
    }
}