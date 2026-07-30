package com.psi.system.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.SyncUpFeignClient;
import com.psi.common.mybatis.entity.BaseEntity;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.service.BaseUpSyncDownloadService;
import com.psi.system.entity.PosConfig;
import com.psi.system.entity.PosOperator;
import com.psi.system.entity.ShopInfo;
import com.psi.system.entity.SysOperationLog;
import com.psi.system.entity.SysLoginLog;
import com.psi.system.mapper.PosConfigMapper;
import com.psi.system.mapper.PosOperatorMapper;
import com.psi.system.mapper.ShopInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SystemUpSyncDownloadService extends BaseUpSyncDownloadService {

    private final ObjectMapper objectMapper;
    private final BatchUtils batchUtils;
    private final PosConfigMapper posConfigMapper;
    private final PosOperatorMapper posOperatorMapper;
    private final ShopInfoMapper shopInfoMapper;
    private final SysOperationLogService sysOperationLogService;
    private final SysLoginLogService sysLoginLogService;

    public SystemUpSyncDownloadService(
            SyncUpFeignClient syncUpFeignClient,
            ObjectMapper objectMapper,
            BatchUtils batchUtils,
            PosConfigMapper posConfigMapper,
            PosOperatorMapper posOperatorMapper,
            ShopInfoMapper shopInfoMapper,
            SysOperationLogService sysOperationLogService,
            SysLoginLogService sysLoginLogService) {
        super(syncUpFeignClient);
        this.objectMapper = objectMapper;
        this.batchUtils = batchUtils;
        this.posConfigMapper = posConfigMapper;
        this.posOperatorMapper = posOperatorMapper;
        this.shopInfoMapper = shopInfoMapper;
        this.sysOperationLogService = sysOperationLogService;
        this.sysLoginLogService = sysLoginLogService;
    }

    @Override
    protected String getModuleName() {
        return "system";
    }

    @Override
    protected boolean processTableData(String tableName, String jsonData, String batchUuid) {
        log.info("[system] process up data: tableName={}, batchUuid={}", tableName, batchUuid);
        try {
            switch (tableName) {
                case "pos_config":
                    return processPosConfig(jsonData);
                case "pos_operator":
                    return processPosOperator(jsonData);
                case "shop_info":
                    return processShopInfo(jsonData);
                case "sys_operation_log":
                    return processSysOperationLog(jsonData);
                case "sys_login_log":
                    return processSysLoginLog(jsonData);
                default:
                    log.warn("[system] 未知表名: {}", tableName);
                    return false;
            }
        } catch (Exception e) {
            log.error("[system] 处理上行数据失败: tableName={}, batchUuid={}, error={}",
                    tableName, batchUuid, e.getMessage(), e);
            return false;
        }
    }

    private boolean processPosConfig(String jsonData) throws Exception {
        List<PosConfig> entities = parseJsonToEntityList(jsonData, PosConfig.class, "pos_config");
        return batchProcessByMapper(entities, posConfigMapper, "pos_config");
    }

    private boolean processPosOperator(String jsonData) throws Exception {
        List<PosOperator> entities = parseJsonToEntityList(jsonData, PosOperator.class, "pos_operator");
        return batchProcessByMapper(entities, posOperatorMapper, "pos_operator");
    }

    private boolean processShopInfo(String jsonData) throws Exception {
        List<ShopInfo> entities = parseJsonToEntityList(jsonData, ShopInfo.class, "shop_info");
        return batchProcessByMapper(entities, shopInfoMapper, "shop_info");
    }

    private boolean processSysOperationLog(String jsonData) throws Exception {
        List<SysOperationLog> entities = parseJsonToEntityList(jsonData, SysOperationLog.class, "sys_operation_log");
        // 操作日志直接批量新增，通常不需要更新
        if (entities.isEmpty()) return true;
        if (entities.size() == 1) {
            sysOperationLogService.save(entities.get(0));
        } else {
            sysOperationLogService.saveBatch(entities);
        }
        log.info("[system] 操作日志批量保存完成: count={}", entities.size());
        return true;
    }

    private boolean processSysLoginLog(String jsonData) throws Exception {
        List<SysLoginLog> entities = parseJsonToEntityList(jsonData, SysLoginLog.class, "sys_login_log");
        // 登录日志直接批量新增，通常不需要更新
        if (entities.isEmpty()) return true;
        if (entities.size() == 1) {
            sysLoginLogService.save(entities.get(0));
        } else {
            sysLoginLogService.saveBatch(entities);
        }
        log.info("[system] 登录日志批量保存完成: count={}", entities.size());
        return true;
    }

    /**
     * 针对非 IService 的 Mapper 进行批量新增/更新
     */
    private <T extends BaseEntity> boolean batchProcessByMapper(
            List<T> entities, com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper, String tableName) {
        if (entities == null || entities.isEmpty()) {
            return true;
        }

        List<String> dataUuids = entities.stream()
                .map(BaseEntity::getDataUuid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (dataUuids.isEmpty()) {
            log.warn("[system] {} dataUuid 均为空，跳过处理", tableName);
            return false;
        }

        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("data_uuid", dataUuids);
        List<T> existingList = mapper.selectList(queryWrapper);
        Set<String> existingDataUuids = existingList.stream()
                .map(BaseEntity::getDataUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<T> insertGroup = new ArrayList<>();
        List<T> updateGroup = new ArrayList<>();

        for (T entity : entities) {
            String dataUuid = entity.getDataUuid();
            if (dataUuid == null) continue;

            if (existingDataUuids.contains(dataUuid)) {
                for (T exist : existingList) {
                    if (dataUuid.equals(exist.getDataUuid())) {
                        entity.setId(exist.getId());
                        break;
                    }
                }
                updateGroup.add(entity);
            } else {
                insertGroup.add(entity);
            }
        }

        // 批量执行
        if (!insertGroup.isEmpty()) {
            for (T entity : insertGroup) {
                mapper.insert(entity);
            }
        }
        if (!updateGroup.isEmpty()) {
            for (T entity : updateGroup) {
                mapper.updateById(entity);
            }
        }

        log.info("[system] {} 批量处理完成: 新增={}, 更新={}, 总数={}",
                tableName, insertGroup.size(), updateGroup.size(), entities.size());
        return true;
    }

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
            try {
                Map<String, Object> wrapped = objectMapper.readValue(jsonData,
                        new TypeReference<Map<String, Object>>() {});
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
                log.error("[system] JSON解析兼容格式也失败: tableName={}", tableName);
                throw e2;
            }
        }
    }
}
