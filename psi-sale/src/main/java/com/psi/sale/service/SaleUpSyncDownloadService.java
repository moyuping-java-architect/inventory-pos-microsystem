package com.psi.sale.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.SyncUpFeignClient;
import com.psi.common.mybatis.entity.BaseEntity;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.service.BaseUpSyncDownloadService;
import com.psi.sale.entity.CustomerEntity;
import com.psi.sale.entity.SaleOrderItemEntity;
import com.psi.sale.entity.SaleOrderMainEntity;
import com.psi.sale.entity.SaleOutItemEntity;
import com.psi.sale.entity.SaleOutMainEntity;
import com.psi.sale.entity.SaleReturnItemEntity;
import com.psi.sale.entity.SaleReturnMainEntity;
import com.psi.sale.service.SaleUpSyncBizService.SaleOrderGroup;
import com.psi.sale.service.SaleUpSyncBizService.SaleReturnGroup;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaleUpSyncDownloadService extends BaseUpSyncDownloadService {

    private final ObjectMapper objectMapper;
    private final BatchUtils batchUtils;
    private final CustomerService customerService;
    private final SaleOrderMainService saleOrderMainService;
    private final SaleOrderItemService saleOrderItemService;
    private final SaleOutMainService saleOutMainService;
    private final SaleOutItemService saleOutItemService;
    private final SaleReturnMainService saleReturnMainService;
    private final SaleReturnItemService saleReturnItemService;
    private final SaleUpSyncBizService saleUpSyncBizService;

    private final ThreadLocal<Map<String, SaleOrderMainEntity>> orderMainCache =
            ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, List<SaleOrderItemEntity>>> orderItemCache =
            ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, SaleReturnMainEntity>> returnMainCache =
            ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, List<SaleReturnItemEntity>>> returnItemCache =
            ThreadLocal.withInitial(HashMap::new);

    public SaleUpSyncDownloadService(
            SyncUpFeignClient syncUpFeignClient,
            ObjectMapper objectMapper,
            BatchUtils batchUtils,
            CustomerService customerService,
            SaleOrderMainService saleOrderMainService,
            SaleOrderItemService saleOrderItemService,
            SaleOutMainService saleOutMainService,
            SaleOutItemService saleOutItemService,
            SaleReturnMainService saleReturnMainService,
            SaleReturnItemService saleReturnItemService,
            SaleUpSyncBizService saleUpSyncBizService) {
        super(syncUpFeignClient);
        this.objectMapper = objectMapper;
        this.batchUtils = batchUtils;
        this.customerService = customerService;
        this.saleOrderMainService = saleOrderMainService;
        this.saleOrderItemService = saleOrderItemService;
        this.saleOutMainService = saleOutMainService;
        this.saleOutItemService = saleOutItemService;
        this.saleReturnMainService = saleReturnMainService;
        this.saleReturnItemService = saleReturnItemService;
        this.saleUpSyncBizService = saleUpSyncBizService;
    }

    @Override
    protected String getModuleName() {
        return "sale";
    }

    @Override
    protected void afterProcess(List<Map<String, Object>> dataList) {
        Map<String, SaleOrderMainEntity> orderMains = orderMainCache.get();
        Map<String, List<SaleOrderItemEntity>> orderItems = orderItemCache.get();
        Map<String, SaleReturnMainEntity> returnMains = returnMainCache.get();
        Map<String, List<SaleReturnItemEntity>> returnItems = returnItemCache.get();

        clearCache();

        if (orderMains == null || orderMains.isEmpty()) {
            orderMains = Collections.emptyMap();
        }
        if (orderItems == null || orderItems.isEmpty()) {
            orderItems = Collections.emptyMap();
        }
        if (returnMains == null || returnMains.isEmpty()) {
            returnMains = Collections.emptyMap();
        }
        if (returnItems == null || returnItems.isEmpty()) {
            returnItems = Collections.emptyMap();
        }

        // 收集本批次存在的销售订单，按 orderNo 去重
        List<SaleOrderGroup> saleOrderGroups = new ArrayList<>();
        for (Map.Entry<String, SaleOrderMainEntity> entry : orderMains.entrySet()) {
            String orderNo = entry.getKey();
            SaleOrderMainEntity main = entry.getValue();
            List<SaleOrderItemEntity> items = orderItems.get(orderNo);
            if (main != null && items != null && !items.isEmpty()) {
                saleOrderGroups.add(new SaleOrderGroup(orderNo, main, items));
            } else {
                log.warn("[sale] 销售订单主表或明细缺失，跳过后置业务: orderNo={}", orderNo);
            }
        }

        // 过滤已存在的订单
        List<SaleOrderGroup> toProcessSaleOrders = saleOrderGroups.stream()
                .filter(group -> {
                    boolean exists = saleOrderMainService.count(
                            new LambdaQueryWrapper<SaleOrderMainEntity>()
                                    .eq(SaleOrderMainEntity::getOrderNo, group.orderNo())) > 0;
                    if (exists) {
                        log.info("[sale] 销售订单已存在，跳过: orderNo={}", group.orderNo());
                    }
                    return !exists;
                })
                .collect(Collectors.toList());

        if (!toProcessSaleOrders.isEmpty()) {
            try {
                saleUpSyncBizService.batchProcessSaleOrders(toProcessSaleOrders);
            } catch (Exception e) {
                log.error("[sale] 批量处理销售订单失败: count={}, error={}",
                        toProcessSaleOrders.size(), e.getMessage(), e);
            }
        }

        // 收集本批次存在的销售退货，按 returnNo 去重
        List<SaleReturnGroup> saleReturnGroups = new ArrayList<>();
        for (Map.Entry<String, SaleReturnMainEntity> entry : returnMains.entrySet()) {
            String returnNo = entry.getKey();
            SaleReturnMainEntity main = entry.getValue();
            List<SaleReturnItemEntity> items = returnItems.get(returnNo);
            if (main != null && items != null && !items.isEmpty()) {
                saleReturnGroups.add(new SaleReturnGroup(returnNo, main, items));
            } else {
                log.warn("[sale] 销售退货主表或明细缺失，跳过后置业务: returnNo={}", returnNo);
            }
        }

        List<SaleReturnGroup> toProcessSaleReturns = saleReturnGroups.stream()
                .filter(group -> {
                    boolean exists = saleReturnMainService.count(
                            new LambdaQueryWrapper<SaleReturnMainEntity>()
                                    .eq(SaleReturnMainEntity::getReturnNo, group.returnNo())) > 0;
                    if (exists) {
                        log.info("[sale] 销售退货已存在，跳过: returnNo={}", group.returnNo());
                    }
                    return !exists;
                })
                .collect(Collectors.toList());

        if (!toProcessSaleReturns.isEmpty()) {
            try {
                saleUpSyncBizService.batchProcessSaleReturns(toProcessSaleReturns);
            } catch (Exception e) {
                log.error("[sale] 批量处理销售退货失败: count={}, error={}",
                        toProcessSaleReturns.size(), e.getMessage(), e);
            }
        }
    }

    private void clearCache() {
        orderMainCache.remove();
        orderItemCache.remove();
        returnMainCache.remove();
        returnItemCache.remove();
    }

    @Override
    protected boolean processTableData(String tableName, String jsonData, String batchUuid) {
        log.info("[sale] process up data: tableName={}, batchUuid={}", tableName, batchUuid);
        try {
            switch (tableName) {
                case "customer":
                    return processCustomer(jsonData);
                case "sale_order_main":
                    return processSaleOrderMain(jsonData);
                case "sale_order_item":
                    return processSaleOrderItem(jsonData);
                case "sale_out_main":
                    return processSaleOutMain(jsonData);
                case "sale_out_item":
                    return processSaleOutItem(jsonData);
                case "sale_return_main":
                    return processSaleReturnMain(jsonData);
                case "sale_return_item":
                    return processSaleReturnItem(jsonData);
                default:
                    log.warn("[sale] 未知表名: {}", tableName);
                    return false;
            }
        } catch (Exception e) {
            log.error("[sale] 处理上行数据失败: tableName={}, batchUuid={}, error={}",
                    tableName, batchUuid, e.getMessage(), e);
            return false;
        }
    }

    private boolean processCustomer(String jsonData) throws Exception {
        List<CustomerEntity> entities = parseJsonToEntityList(jsonData, CustomerEntity.class, "customer");
        return batchProcessEntities(entities, customerService);
    }

    private boolean processSaleOrderMain(String jsonData) throws Exception {
        List<SaleOrderMainEntity> entities = parseJsonToEntityList(jsonData, SaleOrderMainEntity.class, "sale_order_main");
        for (SaleOrderMainEntity entity : entities) {
            if (entity.getOrderNo() != null) {
                orderMainCache.get().put(entity.getOrderNo(), entity);
            }
        }
        return true;
    }

    private boolean processSaleOrderItem(String jsonData) throws Exception {
        List<SaleOrderItemEntity> entities = parseJsonToEntityList(jsonData, SaleOrderItemEntity.class, "sale_order_item");
        for (SaleOrderItemEntity entity : entities) {
            if (entity.getOrderNo() != null) {
                orderItemCache.get().computeIfAbsent(entity.getOrderNo(), k -> new ArrayList<>()).add(entity);
            }
        }
        return true;
    }

    private boolean processSaleOutMain(String jsonData) throws Exception {
        List<SaleOutMainEntity> entities = parseJsonToEntityList(jsonData, SaleOutMainEntity.class, "sale_out_main");
        return batchProcessEntities(entities, saleOutMainService);
    }

    private boolean processSaleOutItem(String jsonData) throws Exception {
        List<SaleOutItemEntity> entities = parseJsonToEntityList(jsonData, SaleOutItemEntity.class, "sale_out_item");
        return batchProcessEntities(entities, saleOutItemService);
    }

    private boolean processSaleReturnMain(String jsonData) throws Exception {
        List<SaleReturnMainEntity> entities = parseJsonToEntityList(jsonData, SaleReturnMainEntity.class, "sale_return_main");
        for (SaleReturnMainEntity entity : entities) {
            if (entity.getReturnNo() != null) {
                returnMainCache.get().put(entity.getReturnNo(), entity);
            }
        }
        return true;
    }

    private boolean processSaleReturnItem(String jsonData) throws Exception {
        List<SaleReturnItemEntity> entities = parseJsonToEntityList(jsonData, SaleReturnItemEntity.class, "sale_return_item");
        for (SaleReturnItemEntity entity : entities) {
            if (entity.getReturnNo() != null) {
                returnItemCache.get().computeIfAbsent(entity.getReturnNo(), k -> new ArrayList<>()).add(entity);
            }
        }
        return true;
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
            log.warn("[sale] 实体 dataUuid 均为空，跳过处理");
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

        log.info("[sale] 批量处理完成: 新增={}, 更新={}, 总数={}",
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
                log.error("[sale] JSON解析兼容格式也失败: tableName={}", tableName);
                throw e2;
            }
        }
    }
}
