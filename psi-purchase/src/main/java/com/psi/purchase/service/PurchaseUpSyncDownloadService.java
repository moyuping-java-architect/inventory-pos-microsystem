package com.psi.purchase.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.SyncUpFeignClient;
import com.psi.common.result.CommonResult;
import com.psi.common.service.BaseUpSyncDownloadService;
import com.psi.purchase.entity.PurchaseInItemEntity;
import com.psi.purchase.entity.PurchaseInMainEntity;
import com.psi.purchase.entity.PurchaseOrderItemEntity;
import com.psi.purchase.entity.PurchaseOrderMainEntity;
import com.psi.purchase.entity.PurchaseReturnItemEntity;
import com.psi.purchase.entity.PurchaseReturnMainEntity;
import com.psi.purchase.entity.SupplierEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购模块上行同步下载服务
 *
 * <p>从 sync 微服务下载采购相关数据，下载完成后触发后续业务：</p>
 * <ul>
 *   <li>供应商：直接保存/更新</li>
 *   <li>采购订单：直接保存/更新（不触发库存）</li>
 *   <li>采购入库：保存主表+明细，调用 audit(2) 触发库存增加</li>
 *   <li>采购退货：保存主表+明细，调用 audit(2) 触发库存扣减</li>
 * </ul>
 */
@Slf4j
@Service
public class PurchaseUpSyncDownloadService extends BaseUpSyncDownloadService {

    private final ObjectMapper objectMapper;
    private final SupplierService supplierService;
    private final PurchaseOrderMainService purchaseOrderMainService;
    private final PurchaseOrderItemService purchaseOrderItemService;
    private final PurchaseInMainService purchaseInMainService;
    private final PurchaseInItemService purchaseInItemService;
    private final PurchaseReturnMainService purchaseReturnMainService;
    private final PurchaseReturnItemService purchaseReturnItemService;

    private final ThreadLocal<Map<String, PurchaseInMainEntity>> inMainCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, List<PurchaseInItemEntity>>> inItemCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, PurchaseReturnMainEntity>> returnMainCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, List<PurchaseReturnItemEntity>>> returnItemCache = ThreadLocal.withInitial(HashMap::new);

    public PurchaseUpSyncDownloadService(
            SyncUpFeignClient syncUpFeignClient,
            ObjectMapper objectMapper,
            SupplierService supplierService,
            PurchaseOrderMainService purchaseOrderMainService,
            PurchaseOrderItemService purchaseOrderItemService,
            PurchaseInMainService purchaseInMainService,
            PurchaseInItemService purchaseInItemService,
            PurchaseReturnMainService purchaseReturnMainService,
            PurchaseReturnItemService purchaseReturnItemService) {
        super(syncUpFeignClient);
        this.objectMapper = objectMapper;
        this.supplierService = supplierService;
        this.purchaseOrderMainService = purchaseOrderMainService;
        this.purchaseOrderItemService = purchaseOrderItemService;
        this.purchaseInMainService = purchaseInMainService;
        this.purchaseInItemService = purchaseInItemService;
        this.purchaseReturnMainService = purchaseReturnMainService;
        this.purchaseReturnItemService = purchaseReturnItemService;
    }

    @Override
    protected String getModuleName() {
        return "purchase";
    }

    @Override
    protected boolean processTableData(String tableName, String jsonData, String batchUuid) {
        log.info("[purchase] process up data: tableName={}, batchUuid={}", tableName, batchUuid);
        try {
            switch (tableName) {
                case "supplier":
                    return processSupplier(jsonData);
                case "purchase_order_main":
                    return processPurchaseOrderMain(jsonData);
                case "purchase_order_item":
                    return processPurchaseOrderItem(jsonData);
                case "purchase_in_main":
                    return processPurchaseInMain(jsonData);
                case "purchase_in_item":
                    return processPurchaseInItem(jsonData);
                case "purchase_return_main":
                    return processPurchaseReturnMain(jsonData);
                case "purchase_return_item":
                    return processPurchaseReturnItem(jsonData);
                default:
                    log.warn("[purchase] 未知表名: {}", tableName);
                    return false;
            }
        } catch (Exception e) {
            log.error("[purchase] 处理上行数据失败: tableName={}, batchUuid={}, error={}",
                    tableName, batchUuid, e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void afterProcess(List<Map<String, Object>> dataList) {
        Map<String, PurchaseInMainEntity> inMains = inMainCache.get();
        Map<String, List<PurchaseInItemEntity>> inItems = inItemCache.get();
        Map<String, PurchaseReturnMainEntity> returnMains = returnMainCache.get();
        Map<String, List<PurchaseReturnItemEntity>> returnItems = returnItemCache.get();

        List<PurchaseInGroup> toProcessInGroups = new ArrayList<>();
        for (Map.Entry<String, PurchaseInMainEntity> entry : inMains.entrySet()) {
            String inNo = entry.getKey();
            PurchaseInMainEntity main = entry.getValue();
            List<PurchaseInItemEntity> items = inItems.get(inNo);
            if (main != null && items != null && !items.isEmpty()) {
                if (purchaseInMainService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseInMainEntity>()
                        .eq(PurchaseInMainEntity::getInNo, inNo)) == 0) {
                    toProcessInGroups.add(new PurchaseInGroup(inNo, main, items));
                } else {
                    log.info("[purchase] 采购入库已存在，跳过: inNo={}", inNo);
                }
            } else {
                log.warn("[purchase] 采购入库主表或明细缺失，跳过后置业务: inNo={}", inNo);
            }
        }

        List<PurchaseReturnGroup> toProcessReturnGroups = new ArrayList<>();
        for (Map.Entry<String, PurchaseReturnMainEntity> entry : returnMains.entrySet()) {
            String returnNo = entry.getKey();
            PurchaseReturnMainEntity main = entry.getValue();
            List<PurchaseReturnItemEntity> items = returnItems.get(returnNo);
            if (main != null && items != null && !items.isEmpty()) {
                if (purchaseReturnMainService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseReturnMainEntity>()
                        .eq(PurchaseReturnMainEntity::getReturnNo, returnNo)) == 0) {
                    toProcessReturnGroups.add(new PurchaseReturnGroup(returnNo, main, items));
                } else {
                    log.info("[purchase] 采购退货已存在，跳过: returnNo={}", returnNo);
                }
            } else {
                log.warn("[purchase] 采购退货主表或明细缺失，跳过后置业务: returnNo={}", returnNo);
            }
        }

        clearCache();

        if (!toProcessInGroups.isEmpty()) {
            try {
                batchProcessPurchaseIns(toProcessInGroups);
            } catch (Exception e) {
                log.error("[purchase] 批量处理采购入库失败: count={}, error={}", toProcessInGroups.size(), e.getMessage(), e);
            }
        }

        if (!toProcessReturnGroups.isEmpty()) {
            try {
                batchProcessPurchaseReturns(toProcessReturnGroups);
            } catch (Exception e) {
                log.error("[purchase] 批量处理采购退货失败: count={}, error={}", toProcessReturnGroups.size(), e.getMessage(), e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchProcessPurchaseIns(List<PurchaseInGroup> groups) {
        log.info("[purchase] 批量处理采购入库后置业务: count={}", groups.size());
        for (PurchaseInGroup group : groups) {
            try {
                processSinglePurchaseIn(group);
            } catch (Exception e) {
                log.error("[purchase] 处理单个采购入库失败: inNo={}, error={}", group.inNo(), e.getMessage(), e);
                throw e;
            }
        }
    }

    private void processSinglePurchaseIn(PurchaseInGroup group) {
        PurchaseInMainEntity main = group.main();
        List<PurchaseInItemEntity> items = group.items();

        main.setInStatus(1);
        main.setAuditStatus(0);
        purchaseInMainService.save(main);
        for (PurchaseInItemEntity item : items) {
            item.setInId(main.getId());
        }
        purchaseInItemService.saveBatch(items);
        log.info("[purchase] 采购入库单已保存: inNo={}, id={}", main.getInNo(), main.getId());

        CommonResult<Void> auditResult = purchaseInMainService.audit(main.getId(), 1);
        if (!auditResult.isSuccess()) {
            throw new RuntimeException("采购入库审核失败: inNo=" + main.getInNo() + ", msg=" + auditResult.getMessage());
        }
        log.info("[purchase] 采购入库库存增加完成: inNo={}", main.getInNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchProcessPurchaseReturns(List<PurchaseReturnGroup> groups) {
        log.info("[purchase] 批量处理采购退货后置业务: count={}", groups.size());
        for (PurchaseReturnGroup group : groups) {
            try {
                processSinglePurchaseReturn(group);
            } catch (Exception e) {
                log.error("[purchase] 处理单个采购退货失败: returnNo={}, error={}", group.returnNo(), e.getMessage(), e);
                throw e;
            }
        }
    }

    private void processSinglePurchaseReturn(PurchaseReturnGroup group) {
        PurchaseReturnMainEntity main = group.main();
        List<PurchaseReturnItemEntity> items = group.items();

        main.setReturnStatus(1);
        main.setAuditStatus(0);
        purchaseReturnMainService.save(main);
        for (PurchaseReturnItemEntity item : items) {
            item.setReturnId(main.getId());
        }
        purchaseReturnItemService.saveBatch(items);
        log.info("[purchase] 采购退货单已保存: returnNo={}, id={}", main.getReturnNo(), main.getId());

        CommonResult<Void> auditResult = purchaseReturnMainService.audit(main.getId(), 1);
        if (!auditResult.isSuccess()) {
            throw new RuntimeException("采购退货审核失败: returnNo=" + main.getReturnNo() + ", msg=" + auditResult.getMessage());
        }
        log.info("[purchase] 采购退货库存扣减完成: returnNo={}", main.getReturnNo());
    }

    private void clearCache() {
        inMainCache.remove();
        inItemCache.remove();
        returnMainCache.remove();
        returnItemCache.remove();
    }

    private boolean processSupplier(String jsonData) throws Exception {
        List<SupplierEntity> entities = parseJsonToEntityList(jsonData, SupplierEntity.class, "supplier");
        return batchSaveOrUpdate(entities, supplierService, SupplierEntity::getSupplierCode);
    }

    private boolean processPurchaseOrderMain(String jsonData) throws Exception {
        List<PurchaseOrderMainEntity> entities = parseJsonToEntityList(jsonData, PurchaseOrderMainEntity.class, "purchase_order_main");
        return batchSaveOrUpdate(entities, purchaseOrderMainService, PurchaseOrderMainEntity::getOrderNo);
    }

    private boolean processPurchaseOrderItem(String jsonData) throws Exception {
        List<PurchaseOrderItemEntity> entities = parseJsonToEntityList(jsonData, PurchaseOrderItemEntity.class, "purchase_order_item");
        return batchSaveOrUpdate(entities, purchaseOrderItemService, PurchaseOrderItemEntity::getOrderNo);
    }

    private boolean processPurchaseInMain(String jsonData) throws Exception {
        List<PurchaseInMainEntity> entities = parseJsonToEntityList(jsonData, PurchaseInMainEntity.class, "purchase_in_main");
        for (PurchaseInMainEntity entity : entities) {
            if (entity.getInNo() != null) {
                inMainCache.get().put(entity.getInNo(), entity);
            }
        }
        return true;
    }

    private boolean processPurchaseInItem(String jsonData) throws Exception {
        List<PurchaseInItemEntity> entities = parseJsonToEntityList(jsonData, PurchaseInItemEntity.class, "purchase_in_item");
        for (PurchaseInItemEntity entity : entities) {
            if (entity.getInNo() != null) {
                inItemCache.get().computeIfAbsent(entity.getInNo(), k -> new ArrayList<>()).add(entity);
            }
        }
        return true;
    }

    private boolean processPurchaseReturnMain(String jsonData) throws Exception {
        List<PurchaseReturnMainEntity> entities = parseJsonToEntityList(jsonData, PurchaseReturnMainEntity.class, "purchase_return_main");
        for (PurchaseReturnMainEntity entity : entities) {
            if (entity.getReturnNo() != null) {
                returnMainCache.get().put(entity.getReturnNo(), entity);
            }
        }
        return true;
    }

    private boolean processPurchaseReturnItem(String jsonData) throws Exception {
        List<PurchaseReturnItemEntity> entities = parseJsonToEntityList(jsonData, PurchaseReturnItemEntity.class, "purchase_return_item");
        for (PurchaseReturnItemEntity entity : entities) {
            if (entity.getReturnNo() != null) {
                returnItemCache.get().computeIfAbsent(entity.getReturnNo(), k -> new ArrayList<>()).add(entity);
            }
        }
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
                log.error("[purchase] JSON解析兼容格式也失败: tableName={}", tableName);
                throw e2;
            }
        }
    }

    private <T> boolean batchSaveOrUpdate(List<T> entities, com.baomidou.mybatisplus.extension.service.IService<T> service, java.util.function.Function<T, String> keyExtractor) {
        if (entities == null || entities.isEmpty()) {
            return true;
        }
        for (T entity : entities) {
            service.saveOrUpdate(entity);
        }
        return true;
    }

    public record PurchaseInGroup(String inNo, PurchaseInMainEntity main, List<PurchaseInItemEntity> items) {
    }

    public record PurchaseReturnGroup(String returnNo, PurchaseReturnMainEntity main, List<PurchaseReturnItemEntity> items) {
    }
}
