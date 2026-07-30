package com.psi.purchase.mq.producer;

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
import com.psi.purchase.entity.*;
import com.psi.purchase.mapper.SyncLogMapper;
import com.psi.purchase.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.RequiredArgsConstructor;
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
 * 采购微服务上行同步MQ生产者
 *
 * <p>将采购相关数据增量同步到 psi-sync 中间库，供下游拉取。</p>
 */
@Slf4j
@Component
public class PurchaseSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;
    private final SyncLogMapper syncLogMapper;

    private final SupplierService supplierService;
    private final PurchaseOrderMainService purchaseOrderMainService;
    private final PurchaseOrderItemService purchaseOrderItemService;
    private final PurchaseOrderExtService purchaseOrderExtService;
    private final PurchaseInMainService purchaseInMainService;
    private final PurchaseInItemService purchaseInItemService;
    private final PurchaseReturnMainService purchaseReturnMainService;
    private final PurchaseReturnItemService purchaseReturnItemService;

    public PurchaseSyncProducer(MqMessageFacade mqMessageFacade,
                                ObjectMapper objectMapper,
                                SyncLogMapper syncLogMapper,
                                @Lazy SupplierService supplierService,
                                @Lazy PurchaseOrderMainService purchaseOrderMainService,
                                @Lazy PurchaseOrderItemService purchaseOrderItemService,
                                @Lazy PurchaseOrderExtService purchaseOrderExtService,
                                @Lazy PurchaseInMainService purchaseInMainService,
                                @Lazy PurchaseInItemService purchaseInItemService,
                                @Lazy PurchaseReturnMainService purchaseReturnMainService,
                                @Lazy PurchaseReturnItemService purchaseReturnItemService) {
        this.mqMessageFacade = mqMessageFacade;
        this.objectMapper = objectMapper;
        this.syncLogMapper = syncLogMapper;
        this.supplierService = supplierService;
        this.purchaseOrderMainService = purchaseOrderMainService;
        this.purchaseOrderItemService = purchaseOrderItemService;
        this.purchaseOrderExtService = purchaseOrderExtService;
        this.purchaseInMainService = purchaseInMainService;
        this.purchaseInItemService = purchaseInItemService;
        this.purchaseReturnMainService = purchaseReturnMainService;
        this.purchaseReturnItemService = purchaseReturnItemService;
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BATCH_SIZE = 1000;
    private static final String SYNC_TYPE_UP = "up";
    private static final String DEFAULT_START_TIME = "2024-01-01 00:00:00";

    /**
     * 异步同步所有采购数据到 sync-ms（增量 + 批量）
     */
    public void syncAllAsync() {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String lastUploadTime = getLastUploadTime();
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
                log.info("采购数据增量同步开始, lastUploadTime={}", lastUploadTime);

                syncTableByPage(supplierService, "supplier", lastUploadTime, currentTime);
                syncTableByPage(purchaseOrderMainService, "purchase_order_main", lastUploadTime, currentTime);
                syncTableByPage(purchaseOrderItemService, "purchase_order_item", lastUploadTime, currentTime);
                syncTableByPage(purchaseOrderExtService, "purchase_order_ext", lastUploadTime, currentTime);
                syncTableByPage(purchaseInMainService, "purchase_in_main", lastUploadTime, currentTime);
                syncTableByPage(purchaseInItemService, "purchase_in_item", lastUploadTime, currentTime);
                syncTableByPage(purchaseReturnMainService, "purchase_return_main", lastUploadTime, currentTime);
                syncTableByPage(purchaseReturnItemService, "purchase_return_item", lastUploadTime, currentTime);

                updateLastUploadTime(currentTime);
                log.info("采购数据增量同步全部完成, currentTime={}", currentTime);
            } catch (Exception e) {
                log.error("采购数据同步消息发送失败", e);
            }
        });
    }

    /**
     * 实时单条发送供应商数据
     */
    public void sendSupplier(SupplierEntity entity) {
        sendSingleEntity(entity, "supplier");
    }

    /**
     * 实时单条发送采购订单主表数据
     */
    public void sendPurchaseOrderMain(PurchaseOrderMainEntity entity) {
        sendSingleEntity(entity, "purchase_order_main");
    }

    /**
     * 实时单条发送采购订单明细数据
     */
    public void sendPurchaseOrderItem(PurchaseOrderItemEntity entity) {
        sendSingleEntity(entity, "purchase_order_item");
    }

    /**
     * 实时单条发送采购订单扩展数据
     */
    public void sendPurchaseOrderExt(PurchaseOrderExtEntity entity) {
        sendSingleEntity(entity, "purchase_order_ext");
    }

    /**
     * 实时单条发送采购入库主表数据
     */
    public void sendPurchaseInMain(PurchaseInMainEntity entity) {
        sendSingleEntity(entity, "purchase_in_main");
    }

    /**
     * 实时单条发送采购入库明细数据
     */
    public void sendPurchaseInItem(PurchaseInItemEntity entity) {
        sendSingleEntity(entity, "purchase_in_item");
    }

    /**
     * 实时单条发送采购退货主表数据
     */
    public void sendPurchaseReturnMain(PurchaseReturnMainEntity entity) {
        sendSingleEntity(entity, "purchase_return_main");
    }

    /**
     * 实时单条发送采购退货明细数据
     */
    public void sendPurchaseReturnItem(PurchaseReturnItemEntity entity) {
        sendSingleEntity(entity, "purchase_return_item");
    }

    private String getLastUploadTime() {
        SyncLogEntity syncLog = syncLogMapper.selectByType(SYNC_TYPE_UP);
        if (syncLog == null || syncLog.getLastTime() == null || syncLog.getLastTime().trim().isEmpty()) {
            return DEFAULT_START_TIME;
        }
        return syncLog.getLastTime();
    }

    private void updateLastUploadTime(String lastTime) {
        SyncLogEntity syncLog = syncLogMapper.selectByType(SYNC_TYPE_UP);
        if (syncLog == null) {
            SyncLogEntity entity = new SyncLogEntity();
            entity.setType(SYNC_TYPE_UP);
            entity.setLastTime(lastTime);
            syncLogMapper.insert(entity);
        } else {
            syncLogMapper.updateLastTime(SYNC_TYPE_UP, lastTime);
        }
    }

    private <T extends BaseEntity> void syncTableByPage(IService<T> service, String tableName, String lastUploadTime, String currentTime) throws JsonProcessingException {
        int offset = 0;
        String batchUuid = IdUtils.snowflakeIdStr();
        List<SyncDataDTO> dataList = new ArrayList<>();

        while (true) {
            List<T> list = queryIncrementPage(service, lastUploadTime, offset, BATCH_SIZE);
            if (list == null || list.isEmpty()) {
                break;
            }
            for (T entity : list) {
                dataList.add(buildSyncData(batchUuid, tableName, entity));
                if (dataList.size() >= BATCH_SIZE) {
                    sendBatchData(batchUuid, dataList, currentTime);
                    dataList = new ArrayList<>();
                    batchUuid = IdUtils.snowflakeIdStr();
                }
            }
            offset += list.size();
            if (list.size() < BATCH_SIZE) {
                break;
            }
        }
        sendBatchData(batchUuid, dataList, currentTime);
    }

    private <T extends BaseEntity> List<T> queryIncrementPage(IService<T> service, String lastUploadTime, int offset, int batchSize) {
        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.ge(BaseEntity::getUpdateTime, lastUploadTime)
                .orderByAsc(BaseEntity::getUpdateTime)
                .last("LIMIT " + offset + ", " + batchSize);
        return service.list(wrapper);
    }

    private void sendSingleEntity(Object entity, String tableName) {
        try {
            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
            String batchUuid = IdUtils.snowflakeIdStr();
            List<SyncDataDTO> dataList = new ArrayList<>();
            dataList.add(buildSyncData(batchUuid, tableName, entity));
            sendBatchData(batchUuid, dataList, currentTime);
            log.debug("采购实时同步消息发送成功: tableName={}", tableName);
        } catch (Exception e) {
            log.error("采购实时同步消息发送失败: tableName={}", tableName, e);
        }
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
        if (entity instanceof SupplierEntity) {
            return ((SupplierEntity) entity).getSupplierCode();
        } else if (entity instanceof PurchaseOrderMainEntity) {
            return ((PurchaseOrderMainEntity) entity).getOrderNo();
        } else if (entity instanceof PurchaseInMainEntity) {
            return ((PurchaseInMainEntity) entity).getInNo();
        } else if (entity instanceof PurchaseReturnMainEntity) {
            return ((PurchaseReturnMainEntity) entity).getReturnNo();
        }
        return null;
    }

    private String resolveDataType(String tableName) {
        return switch (tableName.toLowerCase()) {
            case "supplier" -> "SUPPLIER";
            case "purchase_order_main" -> "PURCHASE_ORDER_MAIN";
            case "purchase_order_item" -> "PURCHASE_ORDER_ITEM";
            case "purchase_order_ext" -> "PURCHASE_ORDER_EXT";
            case "purchase_in_main" -> "PURCHASE_IN_MAIN";
            case "purchase_in_item" -> "PURCHASE_IN_ITEM";
            case "purchase_return_main" -> "PURCHASE_RETURN_MAIN";
            case "purchase_return_item" -> "PURCHASE_RETURN_ITEM";
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
                RabbitMQConstant.SYNC_UP_PURCHASE_ROUTING_KEY,
                "PURCHASE_SYNC"
        );
        Map<String, String> extParams = new HashMap<>();
        extParams.put("batchUuid", batchUuid);
        extParams.put("dataCount", String.valueOf(dataList.size()));
        message.setExtParams(extParams);

        mqMessageFacade.sendAsync(message);
        log.debug("采购同步消息发送成功: batchUuid={}, dataCount={}", batchUuid, dataList.size());
    }
}
