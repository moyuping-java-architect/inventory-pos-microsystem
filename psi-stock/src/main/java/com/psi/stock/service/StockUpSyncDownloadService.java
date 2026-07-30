package com.psi.stock.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.SyncUpFeignClient;
import com.psi.common.result.CommonResult;
import com.psi.common.service.BaseUpSyncDownloadService;
import com.psi.stock.entity.StockBatchEntity;
import com.psi.stock.entity.StockCheckItemEntity;
import com.psi.stock.entity.StockCheckMainEntity;
import com.psi.stock.entity.StockEntity;
import com.psi.stock.entity.StockFlowEntity;
import com.psi.stock.entity.StockLossItemEntity;
import com.psi.stock.entity.StockLossMainEntity;
import com.psi.stock.entity.StockOverItemEntity;
import com.psi.stock.entity.StockOverMainEntity;
import com.psi.stock.entity.StockTransferItemEntity;
import com.psi.stock.entity.StockTransferMainEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存模块上行同步下载服务
 *
 * <p>从 sync 微服务下载库存相关数据，下载完成后触发后续业务：</p>
 * <ul>
 *   <li>库存汇总、批次、流水：直接保存/更新</li>
 *   <li>报溢单：保存主表+明细，调用 audit(1) 触发库存增加</li>
 *   <li>报损单：保存主表+明细，调用 audit(1) 触发库存扣减</li>
 *   <li>盘点单：保存主表+明细，调用 audit(1) 触发库存盘点调整</li>
 *   <li>调拨单：保存主表+明细，调用 audit(1) 触发库存调拨</li>
 * </ul>
 */
@Slf4j
@Service
public class StockUpSyncDownloadService extends BaseUpSyncDownloadService {

    private final ObjectMapper objectMapper;
    private final StockService stockService;
    private final StockBatchService stockBatchService;
    private final StockFlowService stockFlowService;
    private final StockCheckMainService stockCheckMainService;
    private final StockCheckItemService stockCheckItemService;
    private final StockOverMainService stockOverMainService;
    private final StockOverItemService stockOverItemService;
    private final StockLossMainService stockLossMainService;
    private final StockLossItemService stockLossItemService;
    private final StockTransferMainService stockTransferMainService;
    private final StockTransferItemService stockTransferItemService;

    private final ThreadLocal<Map<String, StockOverMainEntity>> overMainCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, List<StockOverItemEntity>>> overItemCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, StockLossMainEntity>> lossMainCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, List<StockLossItemEntity>>> lossItemCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, StockCheckMainEntity>> checkMainCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, List<StockCheckItemEntity>>> checkItemCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, StockTransferMainEntity>> transferMainCache = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, List<StockTransferItemEntity>>> transferItemCache = ThreadLocal.withInitial(HashMap::new);

    public StockUpSyncDownloadService(
            SyncUpFeignClient syncUpFeignClient,
            ObjectMapper objectMapper,
            StockService stockService,
            StockBatchService stockBatchService,
            StockFlowService stockFlowService,
            StockCheckMainService stockCheckMainService,
            StockCheckItemService stockCheckItemService,
            StockOverMainService stockOverMainService,
            StockOverItemService stockOverItemService,
            StockLossMainService stockLossMainService,
            StockLossItemService stockLossItemService,
            StockTransferMainService stockTransferMainService,
            StockTransferItemService stockTransferItemService) {
        super(syncUpFeignClient);
        this.objectMapper = objectMapper;
        this.stockService = stockService;
        this.stockBatchService = stockBatchService;
        this.stockFlowService = stockFlowService;
        this.stockCheckMainService = stockCheckMainService;
        this.stockCheckItemService = stockCheckItemService;
        this.stockOverMainService = stockOverMainService;
        this.stockOverItemService = stockOverItemService;
        this.stockLossMainService = stockLossMainService;
        this.stockLossItemService = stockLossItemService;
        this.stockTransferMainService = stockTransferMainService;
        this.stockTransferItemService = stockTransferItemService;
    }

    @Override
    protected String getModuleName() {
        return "stock";
    }

    @Override
    protected boolean processTableData(String tableName, String jsonData, String batchUuid) {
        log.info("[stock] process up data: tableName={}, batchUuid={}", tableName, batchUuid);
        try {
            switch (tableName) {
                case "stock":
                    return processStock(jsonData);
                case "stock_batch":
                    return processStockBatch(jsonData);
                case "stock_flow":
                    return processStockFlow(jsonData);
                case "stock_check_main":
                    return processStockCheckMain(jsonData);
                case "stock_check_item":
                    return processStockCheckItem(jsonData);
                case "stock_over_main":
                    return processStockOverMain(jsonData);
                case "stock_over_item":
                    return processStockOverItem(jsonData);
                case "stock_loss_main":
                    return processStockLossMain(jsonData);
                case "stock_loss_item":
                    return processStockLossItem(jsonData);
                case "stock_transfer_main":
                    return processStockTransferMain(jsonData);
                case "stock_transfer_item":
                    return processStockTransferItem(jsonData);
                default:
                    log.warn("[stock] 未知表名: {}", tableName);
                    return false;
            }
        } catch (Exception e) {
            log.error("[stock] 处理上行数据失败: tableName={}, batchUuid={}, error={}",
                    tableName, batchUuid, e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void afterProcess(List<Map<String, Object>> dataList) {
        processOverAfter();
        processLossAfter();
        processCheckAfter();
        processTransferAfter();
        clearCache();
    }

    private void processOverAfter() {
        List<StockOverGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StockOverMainEntity> entry : overMainCache.get().entrySet()) {
            String overNo = entry.getKey();
            StockOverMainEntity main = entry.getValue();
            List<StockOverItemEntity> items = overItemCache.get().get(overNo);
            if (main != null && items != null && !items.isEmpty()
                    && stockOverMainService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockOverMainEntity>().eq(StockOverMainEntity::getOverNo, overNo)) == 0) {
                groups.add(new StockOverGroup(overNo, main, items));
            } else {
                log.info("[stock] 报溢单已存在或数据不完整，跳过: overNo={}", overNo);
            }
        }
        if (!groups.isEmpty()) {
            try {
                batchProcessOvers(groups);
            } catch (Exception e) {
                log.error("[stock] 批量处理报溢单失败: count={}, error={}", groups.size(), e.getMessage(), e);
            }
        }
    }

    private void processLossAfter() {
        List<StockLossGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StockLossMainEntity> entry : lossMainCache.get().entrySet()) {
            String lossNo = entry.getKey();
            StockLossMainEntity main = entry.getValue();
            List<StockLossItemEntity> items = lossItemCache.get().get(lossNo);
            if (main != null && items != null && !items.isEmpty()
                    && stockLossMainService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockLossMainEntity>().eq(StockLossMainEntity::getLossNo, lossNo)) == 0) {
                groups.add(new StockLossGroup(lossNo, main, items));
            } else {
                log.info("[stock] 报损单已存在或数据不完整，跳过: lossNo={}", lossNo);
            }
        }
        if (!groups.isEmpty()) {
            try {
                batchProcessLosses(groups);
            } catch (Exception e) {
                log.error("[stock] 批量处理报损单失败: count={}, error={}", groups.size(), e.getMessage(), e);
            }
        }
    }

    private void processCheckAfter() {
        List<StockCheckGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StockCheckMainEntity> entry : checkMainCache.get().entrySet()) {
            String checkNo = entry.getKey();
            StockCheckMainEntity main = entry.getValue();
            List<StockCheckItemEntity> items = checkItemCache.get().get(checkNo);
            if (main != null && items != null && !items.isEmpty()
                    && stockCheckMainService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockCheckMainEntity>().eq(StockCheckMainEntity::getCheckNo, checkNo)) == 0) {
                groups.add(new StockCheckGroup(checkNo, main, items));
            } else {
                log.info("[stock] 盘点单已存在或数据不完整，跳过: checkNo={}", checkNo);
            }
        }
        if (!groups.isEmpty()) {
            try {
                batchProcessChecks(groups);
            } catch (Exception e) {
                log.error("[stock] 批量处理盘点单失败: count={}, error={}", groups.size(), e.getMessage(), e);
            }
        }
    }

    private void processTransferAfter() {
        List<StockTransferGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StockTransferMainEntity> entry : transferMainCache.get().entrySet()) {
            String transferNo = entry.getKey();
            StockTransferMainEntity main = entry.getValue();
            List<StockTransferItemEntity> items = transferItemCache.get().get(transferNo);
            if (main != null && items != null && !items.isEmpty()
                    && stockTransferMainService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockTransferMainEntity>().eq(StockTransferMainEntity::getTransferNo, transferNo)) == 0) {
                groups.add(new StockTransferGroup(transferNo, main, items));
            } else {
                log.info("[stock] 调拨单已存在或数据不完整，跳过: transferNo={}", transferNo);
            }
        }
        if (!groups.isEmpty()) {
            try {
                batchProcessTransfers(groups);
            } catch (Exception e) {
                log.error("[stock] 批量处理调拨单失败: count={}, error={}", groups.size(), e.getMessage(), e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchProcessOvers(List<StockOverGroup> groups) {
        log.info("[stock] 批量处理报溢单后置业务: count={}", groups.size());
        for (StockOverGroup group : groups) {
            StockOverMainEntity main = group.main();
            List<StockOverItemEntity> items = group.items();
            main.setStatus(1);
            stockOverMainService.save(main);
            for (StockOverItemEntity item : items) {
                item.setOverId(main.getId());
            }
            stockOverItemService.saveBatch(items);
            CommonResult<Void> result = stockOverMainService.audit(main.getId(), 1);
            if (!result.isSuccess()) {
                throw new RuntimeException("报溢单审核失败: overNo=" + main.getOverNo() + ", msg=" + result.getMessage());
            }
            log.info("[stock] 报溢单库存增加完成: overNo={}", main.getOverNo());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchProcessLosses(List<StockLossGroup> groups) {
        log.info("[stock] 批量处理报损单后置业务: count={}", groups.size());
        for (StockLossGroup group : groups) {
            StockLossMainEntity main = group.main();
            List<StockLossItemEntity> items = group.items();
            main.setStatus(1);
            stockLossMainService.save(main);
            for (StockLossItemEntity item : items) {
                item.setLossId(main.getId());
            }
            stockLossItemService.saveBatch(items);
            CommonResult<Void> result = stockLossMainService.audit(main.getId(), 1);
            if (!result.isSuccess()) {
                throw new RuntimeException("报损单审核失败: lossNo=" + main.getLossNo() + ", msg=" + result.getMessage());
            }
            log.info("[stock] 报损单库存扣减完成: lossNo={}", main.getLossNo());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchProcessChecks(List<StockCheckGroup> groups) {
        log.info("[stock] 批量处理盘点单后置业务: count={}", groups.size());
        for (StockCheckGroup group : groups) {
            StockCheckMainEntity main = group.main();
            List<StockCheckItemEntity> items = group.items();
            main.setStatus(1);
            stockCheckMainService.save(main);
            for (StockCheckItemEntity item : items) {
                item.setCheckId(main.getId());
            }
            stockCheckItemService.saveBatch(items);
            CommonResult<Void> result = stockCheckMainService.audit(main.getId(), 1);
            if (!result.isSuccess()) {
                throw new RuntimeException("盘点单审核失败: checkNo=" + main.getCheckNo() + ", msg=" + result.getMessage());
            }
            log.info("[stock] 盘点单库存调整完成: checkNo={}", main.getCheckNo());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchProcessTransfers(List<StockTransferGroup> groups) {
        log.info("[stock] 批量处理调拨单后置业务: count={}", groups.size());
        for (StockTransferGroup group : groups) {
            StockTransferMainEntity main = group.main();
            List<StockTransferItemEntity> items = group.items();
            main.setStatus(1);
            stockTransferMainService.save(main);
            for (StockTransferItemEntity item : items) {
                item.setTransferId(main.getId());
            }
            stockTransferItemService.saveBatch(items);
            CommonResult<Void> result = stockTransferMainService.audit(main.getId(), 1);
            if (!result.isSuccess()) {
                throw new RuntimeException("调拨单审核失败: transferNo=" + main.getTransferNo() + ", msg=" + result.getMessage());
            }
            log.info("[stock] 调拨单库存调拨完成: transferNo={}", main.getTransferNo());
        }
    }

    private void clearCache() {
        overMainCache.remove();
        overItemCache.remove();
        lossMainCache.remove();
        lossItemCache.remove();
        checkMainCache.remove();
        checkItemCache.remove();
        transferMainCache.remove();
        transferItemCache.remove();
    }

    private boolean processStock(String jsonData) throws Exception {
        List<StockEntity> entities = parseJsonToEntityList(jsonData, StockEntity.class, "stock");
        return batchSaveOrUpdate(entities, stockService);
    }

    private boolean processStockBatch(String jsonData) throws Exception {
        List<StockBatchEntity> entities = parseJsonToEntityList(jsonData, StockBatchEntity.class, "stock_batch");
        return batchSaveOrUpdate(entities, stockBatchService);
    }

    private boolean processStockFlow(String jsonData) throws Exception {
        List<StockFlowEntity> entities = parseJsonToEntityList(jsonData, StockFlowEntity.class, "stock_flow");
        return batchSaveOrUpdate(entities, stockFlowService);
    }

    private boolean processStockCheckMain(String jsonData) throws Exception {
        List<StockCheckMainEntity> entities = parseJsonToEntityList(jsonData, StockCheckMainEntity.class, "stock_check_main");
        for (StockCheckMainEntity entity : entities) {
            if (entity.getCheckNo() != null) {
                checkMainCache.get().put(entity.getCheckNo(), entity);
            }
        }
        return true;
    }

    private boolean processStockCheckItem(String jsonData) throws Exception {
        List<StockCheckItemEntity> entities = parseJsonToEntityList(jsonData, StockCheckItemEntity.class, "stock_check_item");
        for (StockCheckItemEntity entity : entities) {
            if (entity.getCheckNo() != null) {
                checkItemCache.get().computeIfAbsent(entity.getCheckNo(), k -> new ArrayList<>()).add(entity);
            }
        }
        return true;
    }

    private boolean processStockOverMain(String jsonData) throws Exception {
        List<StockOverMainEntity> entities = parseJsonToEntityList(jsonData, StockOverMainEntity.class, "stock_over_main");
        for (StockOverMainEntity entity : entities) {
            if (entity.getOverNo() != null) {
                overMainCache.get().put(entity.getOverNo(), entity);
            }
        }
        return true;
    }

    private boolean processStockOverItem(String jsonData) throws Exception {
        List<StockOverItemEntity> entities = parseJsonToEntityList(jsonData, StockOverItemEntity.class, "stock_over_item");
        for (StockOverItemEntity entity : entities) {
            if (entity.getOverNo() != null) {
                overItemCache.get().computeIfAbsent(entity.getOverNo(), k -> new ArrayList<>()).add(entity);
            }
        }
        return true;
    }

    private boolean processStockLossMain(String jsonData) throws Exception {
        List<StockLossMainEntity> entities = parseJsonToEntityList(jsonData, StockLossMainEntity.class, "stock_loss_main");
        for (StockLossMainEntity entity : entities) {
            if (entity.getLossNo() != null) {
                lossMainCache.get().put(entity.getLossNo(), entity);
            }
        }
        return true;
    }

    private boolean processStockLossItem(String jsonData) throws Exception {
        List<StockLossItemEntity> entities = parseJsonToEntityList(jsonData, StockLossItemEntity.class, "stock_loss_item");
        for (StockLossItemEntity entity : entities) {
            if (entity.getLossNo() != null) {
                lossItemCache.get().computeIfAbsent(entity.getLossNo(), k -> new ArrayList<>()).add(entity);
            }
        }
        return true;
    }

    private boolean processStockTransferMain(String jsonData) throws Exception {
        List<StockTransferMainEntity> entities = parseJsonToEntityList(jsonData, StockTransferMainEntity.class, "stock_transfer_main");
        for (StockTransferMainEntity entity : entities) {
            if (entity.getTransferNo() != null) {
                transferMainCache.get().put(entity.getTransferNo(), entity);
            }
        }
        return true;
    }

    private boolean processStockTransferItem(String jsonData) throws Exception {
        List<StockTransferItemEntity> entities = parseJsonToEntityList(jsonData, StockTransferItemEntity.class, "stock_transfer_item");
        for (StockTransferItemEntity entity : entities) {
            if (entity.getTransferNo() != null) {
                transferItemCache.get().computeIfAbsent(entity.getTransferNo(), k -> new ArrayList<>()).add(entity);
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
                log.error("[stock] JSON解析兼容格式也失败: tableName={}", tableName);
                throw e2;
            }
        }
    }

    private <T> boolean batchSaveOrUpdate(List<T> entities, com.baomidou.mybatisplus.extension.service.IService<T> service) {
        if (entities == null || entities.isEmpty()) {
            return true;
        }
        for (T entity : entities) {
            service.saveOrUpdate(entity);
        }
        return true;
    }

    public record StockOverGroup(String overNo, StockOverMainEntity main, List<StockOverItemEntity> items) {
    }

    public record StockLossGroup(String lossNo, StockLossMainEntity main, List<StockLossItemEntity> items) {
    }

    public record StockCheckGroup(String checkNo, StockCheckMainEntity main, List<StockCheckItemEntity> items) {
    }

    public record StockTransferGroup(String transferNo, StockTransferMainEntity main, List<StockTransferItemEntity> items) {
    }
}
