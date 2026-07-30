package com.psi.cashier.mq.producer;

import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;

import com.psi.cashier.entity.*;
import com.psi.cashier.mapper.*;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.context.VirtualThreadContextWrapper;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.message.MessageFactory;
import com.psi.common.util.IdUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * 收银微服务MQ生产者
 * 使用虚拟线程异步调用数据库，组装数据后发送到sync-ms
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CashierSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;

    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderPayMapper orderPayMapper;
    private final MemberMapper memberMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final RefundOrderItemMapper refundOrderItemMapper;
    private final RefundPayMapper refundPayMapper;
    private final CashierSettlementMapper cashierSettlementMapper;
    private final CashierShiftMapper cashierShiftMapper;
    private final CashierShiftPayMapper cashierShiftPayMapper;
    private final SyncLogMapper syncLogMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 每次批量同步的数据量限制（防止内存溢出）
     */
    private static final int BATCH_SIZE = 1000;
    
    /**
     * 最大分页数量（防止无限循环）
     */
    private static final int MAX_PAGES = 1000;

    /**
     * 异步同步所有数据到sync-ms
     * 从sync_log表获取上次上传时间，查询update_time大于该时间的所有数据
     * 使用分页处理，避免内存溢出
     */
    public void syncAllAsync() {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
                
                // 获取上次上传时间
                String lastUploadTime = getLastUploadTime();
                
                log.info("数据同步开始: lastUploadTime={}", lastUploadTime);

                // 分页同步订单数据
                syncOrdersByPage(lastUploadTime, currentTime);
                
                // 分页同步退货数据
                syncRefundsByPage(lastUploadTime, currentTime);
                
                // 分页同步日结数据
                syncSettlementsByPage(lastUploadTime, currentTime);
                
                // 分页同步班次数据
                syncShiftsByPage(lastUploadTime, currentTime);

                // 更新上次上传时间
                updateLastUploadTime(currentTime);

                log.info("数据同步全部完成");

            } catch (Exception e) {
                log.error("数据同步消息发送失败", e);
            }
        });
    }

    /**
     * 分页同步订单数据
     * 跳出条件：1. 查询结果为空  2. 达到最大页数限制
     */
    private void syncOrdersByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;
        
        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;
            
            try {
                // 分页查询订单主表
                List<OrderMainEntity> orderMains = orderMainMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);
                
                // 跳出条件1：查询结果为空，说明没有更多数据
                if (orderMains == null || orderMains.isEmpty()) {
                    log.info("订单数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }
                
                // 收集订单号用于查询关联数据
                List<String> orderNos = orderMains.stream()
                        .map(OrderMainEntity::getOrderNo)
                        .collect(java.util.stream.Collectors.toList());
                
                // 并行查询关联数据
                CompletableFuture<List<OrderItemEntity>> itemsFuture = CompletableFuture.supplyAsync(() ->
                        orderItemMapper.selectByOrderNos(orderNos), Executors.newVirtualThreadPerTaskExecutor());
                
                CompletableFuture<List<OrderPayEntity>> paysFuture = CompletableFuture.supplyAsync(() ->
                        orderPayMapper.selectByOrderNos(orderNos), Executors.newVirtualThreadPerTaskExecutor());
                
                CompletableFuture.allOf(itemsFuture, paysFuture).join();
                
                // 组装批次数据并发送
                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();
                
                for (OrderMainEntity orderMain : orderMains) {
                    dataList.add(buildSyncData(batchUuid, orderMain.getTenantId(), orderMain.getShopCode(),
                            "order_main", orderMain));
                }
                
                for (OrderItemEntity item : itemsFuture.get()) {
                    dataList.add(buildSyncData(batchUuid, item.getTenantId(), item.getShopCode(),
                            "order_item", item));
                }
                
                for (OrderPayEntity pay : paysFuture.get()) {
                    dataList.add(buildSyncData(batchUuid, pay.getTenantId(), pay.getShopCode(),
                            "order_pay", pay));
                }
                
                sendBatchData(batchUuid, dataList, currentTime);
                log.info("订单数据同步: page={}, count={}", pageNum, dataList.size());
                
            } catch (Exception e) {
                log.error("订单数据同步失败: page={}", pageNum, e);
            }
            
            pageNum++;
        }
        
        // 跳出条件2：达到最大页数限制时的日志
        if (pageNum > MAX_PAGES) {
            log.warn("订单数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 分页同步退货数据
     * 跳出条件：1. 查询结果为空  2. 达到最大页数限制
     */
    private void syncRefundsByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;
        
        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;
            
            try {
                List<RefundOrderEntity> refundOrders = refundOrderMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);
                
                // 跳出条件1：查询结果为空，说明没有更多数据
                if (refundOrders == null || refundOrders.isEmpty()) {
                    log.info("退货数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }
                
                List<String> refundNos = refundOrders.stream()
                        .map(RefundOrderEntity::getRefundNo)
                        .collect(java.util.stream.Collectors.toList());
                
                CompletableFuture<List<RefundOrderItemEntity>> itemsFuture = CompletableFuture.supplyAsync(() ->
                        refundOrderItemMapper.selectByRefundNos(refundNos), Executors.newVirtualThreadPerTaskExecutor());
                
                CompletableFuture<List<RefundPayEntity>> paysFuture = CompletableFuture.supplyAsync(() ->
                        refundPayMapper.selectByRefundNos(refundNos), Executors.newVirtualThreadPerTaskExecutor());
                
                CompletableFuture.allOf(itemsFuture, paysFuture).join();
                
                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();
                
                for (RefundOrderEntity refund : refundOrders) {
                    dataList.add(buildSyncData(batchUuid, refund.getTenantId(), refund.getShopCode(),
                            "refund_order", refund));
                }
                
                for (RefundOrderItemEntity item : itemsFuture.get()) {
                    dataList.add(buildSyncData(batchUuid, item.getTenantId(), item.getShopCode(),
                            "refund_order_item", item));
                }
                
                for (RefundPayEntity pay : paysFuture.get()) {
                    dataList.add(buildSyncData(batchUuid, pay.getTenantId(), pay.getShopCode(),
                            "refund_pay", pay));
                }
                
                sendBatchData(batchUuid, dataList, currentTime);
                log.info("退货数据同步: page={}, count={}", pageNum, dataList.size());
                
            } catch (Exception e) {
                log.error("退货数据同步失败: page={}", pageNum, e);
            }
            
            pageNum++;
        }
        
        // 跳出条件2：达到最大页数限制时的日志
        if (pageNum > MAX_PAGES) {
            log.warn("退货数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 分页同步日结数据
     * 跳出条件：1. 查询结果为空  2. 达到最大页数限制
     */
    private void syncSettlementsByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;
        
        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;
            
            try {
                List<CashierSettlementEntity> settlements = cashierSettlementMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);
                
                // 跳出条件1：查询结果为空，说明没有更多数据
                if (settlements == null || settlements.isEmpty()) {
                    log.info("日结数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }
                
                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();
                
                for (CashierSettlementEntity settlement : settlements) {
                    dataList.add(buildSyncData(batchUuid, settlement.getTenantId(), settlement.getShopCode(),
                            "cashier_settlement", settlement));
                }
                
                sendBatchData(batchUuid, dataList, currentTime);
                log.info("日结数据同步: page={}, count={}", pageNum, dataList.size());
                
            } catch (Exception e) {
                log.error("日结数据同步失败: page={}", pageNum, e);
            }
            
            pageNum++;
        }
        
        // 跳出条件2：达到最大页数限制时的日志
        if (pageNum > MAX_PAGES) {
            log.warn("日结数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 分页同步班次数据
     * 跳出条件：1. 查询结果为空  2. 达到最大页数限制
     */
    private void syncShiftsByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;
        
        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;
            
            try {
                List<CashierShiftEntity> shifts = cashierShiftMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);
                
                // 跳出条件1：查询结果为空，说明没有更多数据
                if (shifts == null || shifts.isEmpty()) {
                    log.info("班次数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }
                
                List<String> shiftNos = shifts.stream()
                        .map(CashierShiftEntity::getShiftNo)
                        .collect(java.util.stream.Collectors.toList());
                
                CompletableFuture<List<CashierShiftPayEntity>> paysFuture = CompletableFuture.supplyAsync(() ->
                        cashierShiftPayMapper.selectByShiftNos(shiftNos), Executors.newVirtualThreadPerTaskExecutor());
                
                paysFuture.join();
                
                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();
                
                for (CashierShiftEntity shift : shifts) {
                    dataList.add(buildSyncData(batchUuid, shift.getTenantId(), shift.getShopCode(),
                            "cashier_shift", shift));
                }
                
                for (CashierShiftPayEntity pay : paysFuture.get()) {
                    dataList.add(buildSyncData(batchUuid, pay.getTenantId(), pay.getShopCode(),
                            "cashier_shift_pay", pay));
                }
                
                sendBatchData(batchUuid, dataList, currentTime);
                log.info("班次数据同步: page={}, count={}", pageNum, dataList.size());
                
            } catch (Exception e) {
                log.error("班次数据同步失败: page={}", pageNum, e);
            }
            
            pageNum++;
        }
        
        // 跳出条件2：达到最大页数限制时的日志
        if (pageNum > MAX_PAGES) {
            log.warn("班次数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 发送批次数据到MQ
     */
    private void sendBatchData(String batchUuid, List<SyncDataDTO> dataList, String createTime) {
        if (dataList.isEmpty()) {
            return;
        }
        
        try {
            SyncBatchDTO batchDTO = new SyncBatchDTO();
            batchDTO.setBatchUuid(batchUuid);
            batchDTO.setTenantId("1");
            batchDTO.setCreateTime(createTime);
            batchDTO.setDataList(dataList);
            
            sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_UP_ROUTING_KEY);
        } catch (Exception e) {
            log.error("发送批次数据失败: batchUuid={}", batchUuid, e);
        }
    }

    /**
     * 获取上次上传时间
     */
    private String getLastUploadTime() {
        SyncLogEntity logEntity = syncLogMapper.selectByType("up");
        if (logEntity != null) {
            return logEntity.getLastTime();
        }
        // 默认返回一个较早的时间
        return "2024-01-01 00:00:00";
    }

    /**
     * 获取上次下载时间
     */
    private String getLastDownloadTime() {
        SyncLogEntity logEntity = syncLogMapper.selectByType("down");
        if (logEntity != null) {
            return logEntity.getLastTime();
        }
        // 默认返回一个较早的时间
        return "2024-01-01 00:00:00";
    }

    /**
     * 更新上次上传时间
     */
    private void updateLastUploadTime(String lastTime) {
        updateSyncTime("up", lastTime);
    }

    /**
     * 更新上次下载时间
     */
    private void updateLastDownloadTime(String lastTime) {
        updateSyncTime("down", lastTime);
    }

    /**
     * 更新同步时间
     */
    private void updateSyncTime(String type, String lastTime) {
        SyncLogEntity logEntity = syncLogMapper.selectByType(type);
        if (logEntity != null) {
            syncLogMapper.updateLastTime(type, lastTime);
        } else {
            logEntity = new SyncLogEntity();
            logEntity.setType(type);
            logEntity.setLastTime(lastTime);
            syncLogMapper.insert(logEntity);
        }
    }

    /**
     * 异步同步会员数据到sync-ms
     */
    public void syncMemberAsync(String phone) {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                MemberEntity member = memberMapper.selectByPhone(null, phone);
                if (member == null) {
                    log.warn("会员不存在: phone={}", phone);
                    return;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

                SyncBatchDTO batchDTO = new SyncBatchDTO();
                batchDTO.setBatchUuid(batchUuid);
                batchDTO.setTenantId(member.getTenantId());
                batchDTO.setShopCode(null);
                batchDTO.setCreateTime(currentTime);

                List<SyncDataDTO> dataList = new ArrayList<>();
                dataList.add(buildSyncData(batchUuid, member.getTenantId(), null,
                        "member", member));

                batchDTO.setDataList(dataList);

                sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_UP_ROUTING_KEY);

                log.info("会员同步消息发送成功: phone={}, batchUuid={}", phone, batchUuid);

            } catch (Exception e) {
                log.error("会员同步消息发送失败: phone={}", phone, e);
            }
        });
    }

    /**
     * 异步同步退款数据到sync-ms
     * 使用虚拟线程并行查询退款主表、明细表、支付表
     */
    public void syncRefundAsync(String refundNo) {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String batchUuid = IdUtils.snowflakeIdStr();
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

                // 并行查询退款相关数据
                CompletableFuture<RefundOrderEntity> refundFuture = CompletableFuture.supplyAsync(() ->
                        refundOrderMapper.selectByRefundNo(refundNo), java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

                CompletableFuture<List<RefundOrderItemEntity>> itemsFuture = CompletableFuture.supplyAsync(() ->
                        refundOrderItemMapper.selectByRefundNo(refundNo), java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

                CompletableFuture<List<RefundPayEntity>> paysFuture = CompletableFuture.supplyAsync(() ->
                        refundPayMapper.selectByRefundNo(refundNo), java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

                CompletableFuture.allOf(refundFuture, itemsFuture, paysFuture).join();

                RefundOrderEntity refundOrder = refundFuture.get();
                List<RefundOrderItemEntity> refundItems = itemsFuture.get();
                List<RefundPayEntity> refundPays = paysFuture.get();

                SyncBatchDTO batchDTO = new SyncBatchDTO();
                batchDTO.setBatchUuid(batchUuid);
                batchDTO.setTenantId(refundOrder != null ? refundOrder.getTenantId() : "1");
                batchDTO.setShopCode(refundOrder != null ? refundOrder.getShopCode() : null);
                batchDTO.setCreateTime(currentTime);

                List<SyncDataDTO> dataList = new ArrayList<>();

                if (refundOrder != null) {
                    dataList.add(buildSyncData(batchUuid, refundOrder.getTenantId(), refundOrder.getShopCode(),
                            "refund_order", refundOrder));
                }

                for (RefundOrderItemEntity item : refundItems) {
                    dataList.add(buildSyncData(batchUuid, item.getTenantId(), item.getShopCode(),
                            "refund_order_item", item));
                }

                for (RefundPayEntity pay : refundPays) {
                    dataList.add(buildSyncData(batchUuid, pay.getTenantId(), pay.getShopCode(),
                            "refund_pay", pay));
                }

                batchDTO.setDataList(dataList);

                sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_UP_ROUTING_KEY);

                log.info("退款同步消息发送成功: refundNo={}, batchUuid={}, dataCount={}",
                        refundNo, batchUuid, dataList.size());

            } catch (Exception e) {
                log.error("退款同步消息发送失败: refundNo={}", refundNo, e);
            }
        });
    }

    /**
     * 异步同步日结数据到sync-ms
     */
    public void syncSettlementAsync(String settleNo) {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                CashierSettlementEntity settlement = cashierSettlementMapper.selectBySettleNo(settleNo);
                if (settlement == null) {
                    log.warn("日结单不存在: settleNo={}", settleNo);
                    return;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

                SyncBatchDTO batchDTO = new SyncBatchDTO();
                batchDTO.setBatchUuid(batchUuid);
                batchDTO.setTenantId(settlement.getTenantId());
                batchDTO.setShopCode(settlement.getShopCode());
                batchDTO.setCreateTime(currentTime);

                List<SyncDataDTO> dataList = new ArrayList<>();
                dataList.add(buildSyncData(batchUuid, settlement.getTenantId(), settlement.getShopCode(),
                        "cashier_settlement", settlement));

                batchDTO.setDataList(dataList);

                sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_UP_ROUTING_KEY);

                log.info("日结同步消息发送成功: settleNo={}, batchUuid={}", settleNo, batchUuid);

            } catch (Exception e) {
                log.error("日结同步消息发送失败: settleNo={}", settleNo, e);
            }
        });
    }

    /**
     * 异步同步班次数据到sync-ms
     * 使用虚拟线程并行查询班次主表和支付表
     */
    public void syncShiftAsync(String shiftNo) {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String batchUuid = IdUtils.snowflakeIdStr();
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

                CompletableFuture<CashierShiftEntity> shiftFuture = CompletableFuture.supplyAsync(() ->
                        cashierShiftMapper.selectByShiftNo(null, shiftNo), java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

                CompletableFuture<List<CashierShiftPayEntity>> paysFuture = CompletableFuture.supplyAsync(() ->
                        cashierShiftPayMapper.selectByShiftNo(null, shiftNo), java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());

                CompletableFuture.allOf(shiftFuture, paysFuture).join();

                CashierShiftEntity shift = shiftFuture.get();
                List<CashierShiftPayEntity> shiftPays = paysFuture.get();

                SyncBatchDTO batchDTO = new SyncBatchDTO();
                batchDTO.setBatchUuid(batchUuid);
                batchDTO.setTenantId(shift != null ? shift.getTenantId() : "1");
                batchDTO.setShopCode(shift != null ? shift.getShopCode() : null);
                batchDTO.setCreateTime(currentTime);

                List<SyncDataDTO> dataList = new ArrayList<>();

                if (shift != null) {
                    dataList.add(buildSyncData(batchUuid, shift.getTenantId(), shift.getShopCode(),
                            "cashier_shift", shift));
                }

                for (CashierShiftPayEntity pay : shiftPays) {
                    dataList.add(buildSyncData(batchUuid, pay.getTenantId(), pay.getShopCode(),
                            "cashier_shift_pay", pay));
                }

                batchDTO.setDataList(dataList);

                sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_UP_ROUTING_KEY);

                log.info("班次同步消息发送成功: shiftNo={}, batchUuid={}, dataCount={}",
                        shiftNo, batchUuid, dataList.size());

            } catch (Exception e) {
                log.error("班次同步消息发送失败: shiftNo={}", shiftNo, e);
            }
        });
    }

    /**
     * 构建同步数据DTO
     *
     * @param batchUuid 批次UUID
     * @param tenantId 租户ID
     * @param shopCode 商铺编码
     * @param tableName 表名
     * @param entity 实体对象
     * @return SyncDataDTO
     */
    private SyncDataDTO buildSyncData(String batchUuid, String tenantId, String shopCode,
                                       String tableName, Object entity) throws JsonProcessingException {
        SyncDataDTO dataDTO = new SyncDataDTO();

        // 先提取业务主键和数据类型
        setDataTypeAndBusinessKey(dataDTO, tableName, entity);

        // 记录唯一ID基于业务键生成，确保同一业务数据的幂等性
        // 格式：{tenantId}:{tableName}:{businessKey}
        String recordId = generateRecordId(tenantId, tableName, dataDTO.getBusinessKey());
        dataDTO.setRecordId(recordId);

        dataDTO.setBatchUuid(batchUuid);
        dataDTO.setTenantId(tenantId);
        dataDTO.setShopCode(shopCode);
        dataDTO.setTableName(tableName);
        dataDTO.setJsonData(objectMapper.writeValueAsString(entity));
        dataDTO.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));

        // 提取版本号（update_time 时间戳作为版本号）
        dataDTO.setDataVersion(extractVersion(entity));

        return dataDTO;
    }

    /**
     * 生成全局唯一幂等键
     * 格式：{tenantId}:{tableName}:{businessKey}
     */
    private String generateRecordId(String tenantId, String tableName, String businessKey) {
        return String.format("%s:%s:%s",
                tenantId != null ? tenantId : "default",
                tableName != null ? tableName : "unknown",
                businessKey != null ? businessKey : IdUtils.snowflakeIdStr());
    }

    /**
     * 从实体中提取版本号（使用 update_time 的时间戳作为版本号）
     */
    private Long extractVersion(Object entity) {
        try {
            if (entity instanceof OrderMainEntity) {
                return parseVersion(((OrderMainEntity) entity).getUpdateTime());
            } else if (entity instanceof MemberEntity) {
                return parseVersion(((MemberEntity) entity).getUpdateTime());
            } else if (entity instanceof RefundOrderEntity) {
                return parseVersion(((RefundOrderEntity) entity).getUpdateTime());
            } else if (entity instanceof CashierSettlementEntity) {
                return parseVersion(((CashierSettlementEntity) entity).getUpdateTime());
            } else if (entity instanceof CashierShiftEntity) {
                return parseVersion(((CashierShiftEntity) entity).getUpdateTime());
            }
        } catch (Exception e) {
            log.warn("提取版本号失败，使用默认版本0", e);
        }
        return 0L;
    }

    /**
     * 将时间字符串转为版本号（取时间戳秒数）
     */
    private Long parseVersion(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return 0L;
        }
        try {
            LocalDateTime dt = LocalDateTime.parse(timeStr, TIME_FORMATTER);
            return dt.toEpochSecond(java.time.ZoneOffset.ofHours(8));
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 设置数据类型和业务主键
     */
    private void setDataTypeAndBusinessKey(SyncDataDTO dataDTO, String tableName, Object entity) {
        String dataType = resolveDataType(tableName);
        dataDTO.setDataType(dataType);

        // 根据实体类型提取业务主键和原始ID
        if (entity instanceof OrderMainEntity) {
            OrderMainEntity order = (OrderMainEntity) entity;
            dataDTO.setBusinessKey(order.getOrderNo());
            dataDTO.setOriginalId(order.getId() != null ? order.getId().longValue() : null);
        } else if (entity instanceof OrderItemEntity) {
            OrderItemEntity item = (OrderItemEntity) entity;
            dataDTO.setBusinessKey(item.getOrderNo() + ":" + item.getId());
            dataDTO.setOriginalId(item.getId() != null ? item.getId().longValue() : null);
        } else if (entity instanceof OrderPayEntity) {
            OrderPayEntity pay = (OrderPayEntity) entity;
            dataDTO.setBusinessKey(pay.getOrderNo() + ":" + pay.getId());
            dataDTO.setOriginalId(pay.getId() != null ? pay.getId().longValue() : null);
        } else if (entity instanceof MemberEntity) {
            MemberEntity member = (MemberEntity) entity;
            dataDTO.setBusinessKey(member.getPhone());
            dataDTO.setOriginalId(member.getMemberId() != null ? member.getMemberId().longValue() : null);
        } else if (entity instanceof RefundOrderEntity) {
            RefundOrderEntity refund = (RefundOrderEntity) entity;
            dataDTO.setBusinessKey(refund.getRefundNo());
            dataDTO.setOriginalId(refund.getId() != null ? refund.getId().longValue() : null);
        } else if (entity instanceof RefundOrderItemEntity) {
            RefundOrderItemEntity item = (RefundOrderItemEntity) entity;
            dataDTO.setBusinessKey(item.getRefundNo() + ":" + item.getId());
            dataDTO.setOriginalId(item.getId() != null ? item.getId().longValue() : null);
        } else if (entity instanceof RefundPayEntity) {
            RefundPayEntity pay = (RefundPayEntity) entity;
            dataDTO.setBusinessKey(pay.getRefundNo() + ":" + pay.getId());
            dataDTO.setOriginalId(pay.getId() != null ? pay.getId().longValue() : null);
        } else if (entity instanceof CashierSettlementEntity) {
            CashierSettlementEntity settlement = (CashierSettlementEntity) entity;
            dataDTO.setBusinessKey(settlement.getSettleNo());
            dataDTO.setOriginalId(settlement.getId() != null ? settlement.getId().longValue() : null);
        } else if (entity instanceof CashierShiftEntity) {
            CashierShiftEntity shift = (CashierShiftEntity) entity;
            dataDTO.setBusinessKey(shift.getShiftNo());
            dataDTO.setOriginalId(shift.getId() != null ? shift.getId().longValue() : null);
        } else if (entity instanceof CashierShiftPayEntity) {
            CashierShiftPayEntity pay = (CashierShiftPayEntity) entity;
            dataDTO.setBusinessKey(pay.getShiftNo() + ":" + pay.getId());
            dataDTO.setOriginalId(pay.getId() != null ? pay.getId().longValue() : null);
        }
    }

    /**
     * 根据表名解析数据类型
     */
    private String resolveDataType(String tableName) {
        if (tableName == null) {
            return "UNKNOWN";
        }
        
        return switch (tableName.toLowerCase()) {
            case "order_main", "order_item", "order_pay", 
                 "cashier_order", "cashier_order_item", "cashier_pay" -> "ORDER";
            case "member" -> "MEMBER";
            case "refund_order", "refund_order_item", "refund_pay" -> "REFUND";
            case "cashier_settlement" -> "SETTLEMENT";
            case "cashier_shift", "cashier_shift_pay" -> "SHIFT";
            default -> "OTHER";
        };
    }

    /**
     * 发送同步消息到MQ
     * 使用 MessageFactory 创建消息，确保消息格式统一
     */
    private void sendSyncMessage(SyncBatchDTO batchDTO, String routingKey) throws JsonProcessingException {
        // 使用 MessageFactory 创建消息（参照 system 微服务的方式）
        MqCommonMessage<SyncBatchDTO> message = MessageFactory.create(
                batchDTO,
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                routingKey,
                "CASHIER_SYNC"
        );

        // 设置扩展参数
        Map<String, String> extParams = new HashMap<>();
        extParams.put("batchUuid", batchDTO.getBatchUuid());
        extParams.put("dataCount", String.valueOf(batchDTO.getDataList().size()));
        message.setExtParams(extParams);

        // 使用 MqMessageFacade 异步发送
        mqMessageFacade.sendAsync(message);
        
        log.debug("同步消息发送成功: batchUuid={}, dataCount={}", 
                batchDTO.getBatchUuid(), batchDTO.getDataList().size());
    }
}