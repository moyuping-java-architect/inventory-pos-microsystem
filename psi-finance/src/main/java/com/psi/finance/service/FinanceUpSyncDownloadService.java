package com.psi.finance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.SyncUpFeignClient;
import com.psi.common.mybatis.entity.BaseEntity;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.service.BaseUpSyncDownloadService;
import com.psi.finance.entity.FinanceReceivableEntity;
import com.psi.finance.entity.FinanceReceivablePayEntity;
import com.psi.finance.entity.FinancePayableEntity;
import com.psi.finance.entity.FinancePayablePayEntity;
import com.psi.finance.entity.FinanceAccountEntity;
import com.psi.finance.entity.FinanceAccountFlowEntity;
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
public class FinanceUpSyncDownloadService extends BaseUpSyncDownloadService {

    private final ObjectMapper objectMapper;
    private final BatchUtils batchUtils;
    private final FinanceReceivableService financeReceivableService;
    private final FinanceReceivablePayService financeReceivablePayService;
    private final FinancePayableService financePayableService;
    private final FinancePayablePayService financePayablePayService;
    private final FinanceAccountService financeAccountService;
    private final FinanceAccountFlowService financeAccountFlowService;

    public FinanceUpSyncDownloadService(
            SyncUpFeignClient syncUpFeignClient,
            ObjectMapper objectMapper,
            BatchUtils batchUtils,
            FinanceReceivableService financeReceivableService,
            FinanceReceivablePayService financeReceivablePayService,
            FinancePayableService financePayableService,
            FinancePayablePayService financePayablePayService,
            FinanceAccountService financeAccountService,
            FinanceAccountFlowService financeAccountFlowService) {
        super(syncUpFeignClient);
        this.objectMapper = objectMapper;
        this.batchUtils = batchUtils;
        this.financeReceivableService = financeReceivableService;
        this.financeReceivablePayService = financeReceivablePayService;
        this.financePayableService = financePayableService;
        this.financePayablePayService = financePayablePayService;
        this.financeAccountService = financeAccountService;
        this.financeAccountFlowService = financeAccountFlowService;
    }

    @Override
    protected String getModuleName() {
        return "finance";
    }

    @Override
    protected boolean processTableData(String tableName, String jsonData, String batchUuid) {
        log.info("[finance] process up data: tableName={}, batchUuid={}", tableName, batchUuid);
        try {
            switch (tableName) {
                case "finance_receivable":
                    return processFinanceReceivable(jsonData);
                case "finance_receivable_pay":
                    return processFinanceReceivablePay(jsonData);
                case "finance_payable":
                    return processFinancePayable(jsonData);
                case "finance_payable_pay":
                    return processFinancePayablePay(jsonData);
                case "finance_account":
                    return processFinanceAccount(jsonData);
                case "finance_account_flow":
                    return processFinanceAccountFlow(jsonData);
                default:
                    log.warn("[finance] 未知表名: {}", tableName);
                    return false;
            }
        } catch (Exception e) {
            log.error("[finance] 处理上行数据失败: tableName={}, batchUuid={}, error={}",
                    tableName, batchUuid, e.getMessage(), e);
            return false;
        }
    }

    private boolean processFinanceReceivable(String jsonData) throws Exception {
        List<FinanceReceivableEntity> entities = parseJsonToEntityList(jsonData, FinanceReceivableEntity.class, "finance_receivable");
        return batchProcessEntities(entities, financeReceivableService);
    }

    private boolean processFinanceReceivablePay(String jsonData) throws Exception {
        List<FinanceReceivablePayEntity> entities = parseJsonToEntityList(jsonData, FinanceReceivablePayEntity.class, "finance_receivable_pay");
        return batchProcessEntities(entities, financeReceivablePayService);
    }

    private boolean processFinancePayable(String jsonData) throws Exception {
        List<FinancePayableEntity> entities = parseJsonToEntityList(jsonData, FinancePayableEntity.class, "finance_payable");
        return batchProcessEntities(entities, financePayableService);
    }

    private boolean processFinancePayablePay(String jsonData) throws Exception {
        List<FinancePayablePayEntity> entities = parseJsonToEntityList(jsonData, FinancePayablePayEntity.class, "finance_payable_pay");
        return batchProcessEntities(entities, financePayablePayService);
    }

    private boolean processFinanceAccount(String jsonData) throws Exception {
        List<FinanceAccountEntity> entities = parseJsonToEntityList(jsonData, FinanceAccountEntity.class, "finance_account");
        return batchProcessEntities(entities, financeAccountService);
    }

    private boolean processFinanceAccountFlow(String jsonData) throws Exception {
        List<FinanceAccountFlowEntity> entities = parseJsonToEntityList(jsonData, FinanceAccountFlowEntity.class, "finance_account_flow");
        return batchProcessEntities(entities, financeAccountFlowService);
    }

    private <T extends BaseEntity> boolean batchProcessEntities(List<T> entities, IService<T> service) {
        if (entities == null || entities.isEmpty()) {
            return true;
        }

        List<String> dataUuids = entities.stream()
                .map(BaseEntity::getDataUuid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (dataUuids.isEmpty()) {
            log.warn("[finance] 实体 dataUuid 均为空，跳过处理");
            return false;
        }

        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("data_uuid", dataUuids);
        List<T> existingList = service.list(queryWrapper);
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

        if (!insertGroup.isEmpty()) {
            batchUtils.saveBatch(service, insertGroup);
        }
        if (!updateGroup.isEmpty()) {
            batchUtils.updateBatchById(service, updateGroup);
        }

        log.info("[finance] 批量处理完成: 新增={}, 更新={}, 总数={}",
                insertGroup.size(), updateGroup.size(), entities.size());
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
                log.error("[finance] JSON解析兼容格式也失败: tableName={}", tableName);
                throw e2;
            }
        }
    }
}
