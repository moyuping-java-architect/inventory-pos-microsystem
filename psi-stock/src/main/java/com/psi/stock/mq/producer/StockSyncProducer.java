package com.psi.stock.mq.producer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.context.VirtualThreadContextWrapper;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.mybatis.entity.BaseEntity;
import com.psi.common.util.IdUtils;
import com.psi.stock.entity.*;
import com.psi.stock.mapper.SyncLogMapper;
import com.psi.stock.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存微服务上行同步MQ生产者
 *
 * <p>将库存相关数据发送到 psi-sync 中间库，供下游拉取。</p>
 * <p>支持增量同步：基于 sync_log 表记录的 last_upload_time，按 update_time 分页查询并发送。</p>
 * <p>支持实时同步：业务写操作成功后调用对应 send 方法单条发送。</p>
 */
@Slf4j
@Component
public class StockSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;

    private final StockService stockService;
    private final StockBatchService stockBatchService;
    private final StockFlowService stockFlowService;
    private final StockCheckMainService stockCheckMainService;
    private final StockCheckItemService stockCheckItemService;
    private final StockLossMainService stockLossMainService;
    private final StockLossItemService stockLossItemService;
    private final StockOverMainService stockOverMainService;
    private final StockOverItemService stockOverItemService;
    private final StockTransferMainService stockTransferMainService;
    private final StockTransferItemService stockTransferItemService;
    private final InventoryInitMainService inventoryInitMainService;
    private final InventoryInitItemService inventoryInitItemService;

    private final SyncLogMapper syncLogMapper;

    public StockSyncProducer(MqMessageFacade mqMessageFacade, ObjectMapper objectMapper,
                             @Lazy StockService stockService, @Lazy StockBatchService stockBatchService,
                             @Lazy StockFlowService stockFlowService, @Lazy StockCheckMainService stockCheckMainService,
                             @Lazy StockCheckItemService stockCheckItemService, @Lazy StockLossMainService stockLossMainService,
                             @Lazy StockLossItemService stockLossItemService, @Lazy StockOverMainService stockOverMainService,
                             @Lazy StockOverItemService stockOverItemService, @Lazy StockTransferMainService stockTransferMainService,
                             @Lazy StockTransferItemService stockTransferItemService, @Lazy InventoryInitMainService inventoryInitMainService,
                             @Lazy InventoryInitItemService inventoryInitItemService, SyncLogMapper syncLogMapper) {
        this.mqMessageFacade = mqMessageFacade;
        this.objectMapper = objectMapper;
        this.stockService = stockService;
        this.stockBatchService = stockBatchService;
        this.stockFlowService = stockFlowService;
        this.stockCheckMainService = stockCheckMainService;
        this.stockCheckItemService = stockCheckItemService;
        this.stockLossMainService = stockLossMainService;
        this.stockLossItemService = stockLossItemService;
        this.stockOverMainService = stockOverMainService;
        this.stockOverItemService = stockOverItemService;
        this.stockTransferMainService = stockTransferMainService;
        this.stockTransferItemService = stockTransferItemService;
        this.inventoryInitMainService = inventoryInitMainService;
        this.inventoryInitItemService = inventoryInitItemService;
        this.syncLogMapper = syncLogMapper;
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BATCH_SIZE = 1000;
    private static final int MAX_PAGES = 1000;
    private static final String DEFAULT_START_TIME = "2024-01-01 00:00:00";
    private static final String SYNC_TYPE_UP = "up";

    /**
     * 异步同步所有库存数据到 sync-ms
     * 基于 sync_log 表 last_upload_time 增量分页同步
     */
    public void syncAllAsync() {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
                String lastUploadTime = getLastUploadTime();
                log.info("库存数据同步开始: lastUploadTime={}", lastUploadTime);

                syncTableByPage("stock", stockService, StockEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_batch", stockBatchService, StockBatchEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_flow", stockFlowService, StockFlowEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_check_main", stockCheckMainService, StockCheckMainEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_check_item", stockCheckItemService, StockCheckItemEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_loss_main", stockLossMainService, StockLossMainEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_loss_item", stockLossItemService, StockLossItemEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_over_main", stockOverMainService, StockOverMainEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_over_item", stockOverItemService, StockOverItemEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_transfer_main", stockTransferMainService, StockTransferMainEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_transfer_item", stockTransferItemService, StockTransferItemEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_inventory_init_main", inventoryInitMainService, InventoryInitMainEntity::getUpdateTime, lastUploadTime, currentTime);
                syncTableByPage("stock_inventory_init_item", inventoryInitItemService, InventoryInitItemEntity::getUpdateTime, lastUploadTime, currentTime);

                updateLastUploadTime(currentTime);
                log.info("库存数据同步全部完成");
            } catch (Exception e) {
                log.error("库存数据同步消息发送失败", e);
            }
        });
    }

    /**
     * 通用增量分页同步
     * 按 update_time 升序分页查询，每页 BATCH_SIZE 条，组装后批量发送
     */
    private <T extends BaseEntity> void syncTableByPage(String tableName, IService<T> service,
                                                        SFunction<T, LocalDateTime> updateTimeFunc,
                                                        String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
                wrapper.ge(updateTimeFunc, lastUploadTime)
                        .orderByAsc(updateTimeFunc)
                        .last("LIMIT " + offset + ", " + BATCH_SIZE);

                List<T> list = service.list(wrapper);

                if (list == null || list.isEmpty()) {
                    log.info("库存表[{}]数据同步完成，共处理 {} 页", tableName, pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                sendList(list, tableName, currentTime);
                log.info("库存表[{}]数据同步: page={}, count={}", tableName, pageNum, list.size());
            } catch (Exception e) {
                log.error("库存表[{}]数据同步失败: page={}", tableName, pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("库存表[{}]数据同步达到最大页数限制: {}", tableName, MAX_PAGES);
        }
    }

    /**
     * 实时同步单条库存数据
     */
    public void sendStock(StockEntity entity) {
        sendEntity(entity, "stock");
    }

    /**
     * 实时同步单条批次库存数据
     */
    public void sendStockBatch(StockBatchEntity entity) {
        sendEntity(entity, "stock_batch");
    }

    /**
     * 实时同步单条库存流水数据
     */
    public void sendStockFlow(StockFlowEntity entity) {
        sendEntity(entity, "stock_flow");
    }

    /**
     * 实时同步单条盘点主单数据
     */
    public void sendStockCheckMain(StockCheckMainEntity entity) {
        sendEntity(entity, "stock_check_main");
    }

    /**
     * 实时同步单条盘点明细数据
     */
    public void sendStockCheckItem(StockCheckItemEntity entity) {
        sendEntity(entity, "stock_check_item");
    }

    /**
     * 实时同步单条报损主单数据
     */
    public void sendStockLossMain(StockLossMainEntity entity) {
        sendEntity(entity, "stock_loss_main");
    }

    /**
     * 实时同步单条报损明细数据
     */
    public void sendStockLossItem(StockLossItemEntity entity) {
        sendEntity(entity, "stock_loss_item");
    }

    /**
     * 实时同步单条报溢主单数据
     */
    public void sendStockOverMain(StockOverMainEntity entity) {
        sendEntity(entity, "stock_over_main");
    }

    /**
     * 实时同步单条报溢明细数据
     */
    public void sendStockOverItem(StockOverItemEntity entity) {
        sendEntity(entity, "stock_over_item");
    }

    /**
     * 实时同步单条调拨主单数据
     */
    public void sendStockTransferMain(StockTransferMainEntity entity) {
        sendEntity(entity, "stock_transfer_main");
    }

    /**
     * 实时同步单条调拨明细数据
     */
    public void sendStockTransferItem(StockTransferItemEntity entity) {
        sendEntity(entity, "stock_transfer_item");
    }

    /**
     * 实时同步单条库存初始化主单数据
     */
    public void sendInventoryInitMain(InventoryInitMainEntity entity) {
        sendEntity(entity, "stock_inventory_init_main");
    }

    /**
     * 实时同步单条库存初始化明细数据
     */
    public void sendInventoryInitItem(InventoryInitItemEntity entity) {
        sendEntity(entity, "stock_inventory_init_item");
    }

    /**
     * 获取上次上传时间
     */
    private String getLastUploadTime() {
        try {
            SyncLogEntity logEntity = syncLogMapper.selectByType(SYNC_TYPE_UP);
            if (logEntity != null && logEntity.getLastTime() != null && !logEntity.getLastTime().trim().isEmpty()) {
                return logEntity.getLastTime();
            }
        } catch (Exception e) {
            log.warn("获取上次上传时间失败，使用默认时间", e);
        }
        return DEFAULT_START_TIME;
    }

    /**
     * 更新上次上传时间
     */
    private void updateLastUploadTime(String lastTime) {
        try {
            SyncLogEntity logEntity = syncLogMapper.selectByType(SYNC_TYPE_UP);
            if (logEntity != null) {
                syncLogMapper.updateLastTime(SYNC_TYPE_UP, lastTime);
            } else {
                logEntity = new SyncLogEntity();
                logEntity.setType(SYNC_TYPE_UP);
                logEntity.setLastTime(lastTime);
                syncLogMapper.insert(logEntity);
            }
        } catch (Exception e) {
            log.error("更新上次上传时间失败: lastTime={}", lastTime, e);
        }
    }

    private void sendEntity(Object entity, String tableName) {
        if (entity == null) {
            return;
        }
        try {
            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
            String batchUuid = IdUtils.snowflakeIdStr();
            List<SyncDataDTO> dataList = new ArrayList<>();
            dataList.add(buildSyncData(batchUuid, tableName, entity));
            sendBatchData(batchUuid, dataList, currentTime);
            log.debug("库存实时同步消息发送成功: tableName={}, batchUuid={}", tableName, batchUuid);
        } catch (Exception e) {
            log.error("库存实时同步消息发送失败: tableName={}", tableName, e);
        }
    }

    private <T> void sendList(List<T> list, String tableName, String currentTime) throws JsonProcessingException {
        if (list == null || list.isEmpty()) {
            return;
        }
        String batchUuid = IdUtils.snowflakeIdStr();
        List<SyncDataDTO> dataList = new ArrayList<>();
        for (T entity : list) {
            dataList.add(buildSyncData(batchUuid, tableName, entity));
            if (dataList.size() >= BATCH_SIZE) {
                sendBatchData(batchUuid, dataList, currentTime);
                dataList = new ArrayList<>();
            }
        }
        sendBatchData(batchUuid, dataList, currentTime);
    }

    private SyncDataDTO buildSyncData(String batchUuid, String tableName, Object entity) throws JsonProcessingException {
        SyncDataDTO dataDTO = new SyncDataDTO();
        dataDTO.setRecordId(IdUtils.snowflakeIdStr());
        dataDTO.setBatchUuid(batchUuid);
        dataDTO.setTenantId("1");
        dataDTO.setTableName(tableName);
        dataDTO.setJsonData(objectMapper.writeValueAsString(entity));
        dataDTO.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));
        dataDTO.setDataType(resolveDataType(tableName));
        dataDTO.setBusinessKey(resolveBusinessKey(tableName, entity));
        return dataDTO;
    }

    private String resolveBusinessKey(String tableName, Object entity) {
        return switch (tableName.toLowerCase()) {
            case "stock_batch" -> ((StockBatchEntity) entity).getBatchNo();
            case "stock_flow" -> ((StockFlowEntity) entity).getSourceNo();
            case "stock_check_main" -> ((StockCheckMainEntity) entity).getCheckNo();
            case "stock_loss_main" -> ((StockLossMainEntity) entity).getLossNo();
            case "stock_over_main" -> ((StockOverMainEntity) entity).getOverNo();
            case "stock_transfer_main" -> ((StockTransferMainEntity) entity).getTransferNo();
            case "stock_inventory_init_main" -> ((InventoryInitMainEntity) entity).getInitNo();
            default -> null;
        };
    }

    private String resolveDataType(String tableName) {
        return switch (tableName.toLowerCase()) {
            case "stock" -> "STOCK";
            case "stock_batch" -> "STOCK_BATCH";
            case "stock_flow" -> "STOCK_FLOW";
            case "stock_check_main" -> "STOCK_CHECK_MAIN";
            case "stock_check_item" -> "STOCK_CHECK_ITEM";
            case "stock_loss_main" -> "STOCK_LOSS_MAIN";
            case "stock_loss_item" -> "STOCK_LOSS_ITEM";
            case "stock_over_main" -> "STOCK_OVER_MAIN";
            case "stock_over_item" -> "STOCK_OVER_ITEM";
            case "stock_transfer_main" -> "STOCK_TRANSFER_MAIN";
            case "stock_transfer_item" -> "STOCK_TRANSFER_ITEM";
            case "stock_inventory_init_main" -> "INVENTORY_INIT_MAIN";
            case "stock_inventory_init_item" -> "INVENTORY_INIT_ITEM";
            default -> "OTHER";
        };
    }

    private void sendBatchData(String batchUuid, List<SyncDataDTO> dataList, String createTime) throws JsonProcessingException {
        if (dataList.isEmpty()) {
            return;
        }
        SyncBatchDTO batchDTO = new SyncBatchDTO();
        batchDTO.setBatchUuid(batchUuid);
        batchDTO.setTenantId("1");
        batchDTO.setCreateTime(createTime);
        batchDTO.setDataList(dataList);

        MqCommonMessage<SyncBatchDTO> message = MessageFactory.create(
                batchDTO,
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                RabbitMQConstant.SYNC_UP_STOCK_ROUTING_KEY,
                "STOCK_SYNC"
        );
        Map<String, String> extParams = new HashMap<>();
        extParams.put("batchUuid", batchUuid);
        extParams.put("dataCount", String.valueOf(dataList.size()));
        message.setExtParams(extParams);

        mqMessageFacade.sendAsync(message);
        log.debug("库存同步消息发送成功: batchUuid={}, dataCount={}", batchUuid, dataList.size());
    }
}
