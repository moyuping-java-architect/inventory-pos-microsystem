package com.psi.finance.mq.producer;

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
import com.psi.finance.entity.*;
import com.psi.finance.mapper.SyncLogMapper;
import com.psi.finance.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务微服务上行同步MQ生产者
 *
 * <p>将财务相关数据增量批量发送到 psi-sync 中间库，供下游拉取。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;
    private final SyncLogMapper syncLogMapper;

    private final FinanceAccountService financeAccountService;
    private final FinanceAccountFlowService financeAccountFlowService;
    private final FinanceReceivableService financeReceivableService;
    private final FinanceReceivablePayService financeReceivablePayService;
    private final FinancePayableService financePayableService;
    private final FinancePayablePayService financePayablePayService;
    private final FinanceDailyCloseService financeDailyCloseService;
    private final FinanceDailyLedgerService financeDailyLedgerService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BATCH_SIZE = 1000;
    private static final int MAX_PAGES = 1000;

    /**
     * 异步同步所有财务数据到 sync-ms
     */
    public void syncAllAsync() {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
                String lastUploadTime = getLastUploadTime();
                log.info("财务数据同步开始: lastUploadTime={}", lastUploadTime);

                syncTableByPage(financeAccountService.getBaseMapper(), "finance_account", lastUploadTime, currentTime);
                syncTableByPage(financeAccountFlowService.getBaseMapper(), "finance_account_flow", lastUploadTime, currentTime);
                syncTableByPage(financeReceivableService.getBaseMapper(), "finance_receivable", lastUploadTime, currentTime);
                syncTableByPage(financeReceivablePayService.getBaseMapper(), "finance_receivable_pay", lastUploadTime, currentTime);
                syncTableByPage(financePayableService.getBaseMapper(), "finance_payable", lastUploadTime, currentTime);
                syncTableByPage(financePayablePayService.getBaseMapper(), "finance_payable_pay", lastUploadTime, currentTime);
                syncTableByPage(financeDailyCloseService.getBaseMapper(), "finance_daily_close", lastUploadTime, currentTime);
                syncTableByPage(financeDailyLedgerService.getBaseMapper(), "finance_daily_ledger", lastUploadTime, currentTime);

                updateLastUploadTime(currentTime);
                log.info("财务数据同步全部完成");
            } catch (Exception e) {
                log.error("财务数据同步消息发送失败", e);
            }
        });
    }

    /**
     * 获取上次上传时间
     */
    private String getLastUploadTime() {
        SyncLogEntity logEntity = syncLogMapper.selectByType("up");
        if (logEntity != null) {
            return logEntity.getLastTime();
        }
        return "2024-01-01 00:00:00";
    }

    /**
     * 更新上次上传时间
     */
    private void updateLastUploadTime(String lastTime) {
        SyncLogEntity logEntity = syncLogMapper.selectByType("up");
        if (logEntity != null) {
            syncLogMapper.updateLastTime("up", lastTime);
        } else {
            logEntity = new SyncLogEntity();
            logEntity.setType("up");
            logEntity.setLastTime(lastTime);
            syncLogMapper.insert(logEntity);
        }
    }

    /**
     * 通用增量分页同步
     */
    private <T extends BaseEntity> void syncTableByPage(BaseMapper<T> mapper, String tableName,
                                                        String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                List<T> list = selectByUpdateTimeAfterPage(mapper, lastUploadTime, offset, BATCH_SIZE);
                if (list == null || list.isEmpty()) {
                    log.info("{} 数据同步完成，共处理 {} 页", tableName, pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                sendList(list, tableName, currentTime);
                log.info("{} 数据同步: page={}, count={}", tableName, pageNum, list.size());
            } catch (Exception e) {
                log.error("{} 数据同步失败: page={}", tableName, pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("{} 数据同步达到最大页数限制: {}", tableName, MAX_PAGES);
        }
    }

    /**
     * 通用增量分页查询
     */
    private <T extends BaseEntity> List<T> selectByUpdateTimeAfterPage(BaseMapper<T> mapper, String lastUploadTime,
                                                                       int offset, int batchSize) {
        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.ge(T::getUpdateTime, lastUploadTime)
               .orderByAsc(T::getUpdateTime)
               .last("LIMIT " + offset + ", " + batchSize);
        return mapper.selectList(wrapper);
    }

    /**
     * 实时同步财务账户数据
     */
    public void sendFinanceAccount(FinanceAccountEntity entity) {
        sendSingleEntity(entity, "finance_account");
    }

    /**
     * 实时同步财务账户流水数据
     */
    public void sendFinanceAccountFlow(FinanceAccountFlowEntity entity) {
        sendSingleEntity(entity, "finance_account_flow");
    }

    /**
     * 实时同步应收数据
     */
    public void sendFinanceReceivable(FinanceReceivableEntity entity) {
        sendSingleEntity(entity, "finance_receivable");
    }

    /**
     * 实时同步应收付款数据
     */
    public void sendFinanceReceivablePay(FinanceReceivablePayEntity entity) {
        sendSingleEntity(entity, "finance_receivable_pay");
    }

    /**
     * 实时同步应付数据
     */
    public void sendFinancePayable(FinancePayableEntity entity) {
        sendSingleEntity(entity, "finance_payable");
    }

    /**
     * 实时同步应付付款数据
     */
    public void sendFinancePayablePay(FinancePayablePayEntity entity) {
        sendSingleEntity(entity, "finance_payable_pay");
    }

    /**
     * 实时同步日结数据
     */
    public void sendFinanceDailyClose(FinanceDailyCloseEntity entity) {
        sendSingleEntity(entity, "finance_daily_close");
    }

    /**
     * 实时同步日报数据
     */
    public void sendFinanceDailyLedger(FinanceDailyLedgerEntity entity) {
        sendSingleEntity(entity, "finance_daily_ledger");
    }

    /**
     * 实时发送单条数据
     */
    private void sendSingleEntity(BaseEntity entity, String tableName) {
        if (entity == null) {
            return;
        }
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String batchUuid = IdUtils.snowflakeIdStr();
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
                List<SyncDataDTO> dataList = new ArrayList<>();
                dataList.add(buildSyncData(batchUuid, tableName, entity));
                sendBatchData(batchUuid, dataList, currentTime);
                log.info("财务实时同步消息发送成功: tableName={}, batchUuid={}", tableName, batchUuid);
            } catch (Exception e) {
                log.error("财务实时同步消息发送失败: tableName={}", tableName, e);
            }
        });
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
            case "finance_account" -> ((FinanceAccountEntity) entity).getAccountNo();
            case "finance_account_flow" -> ((FinanceAccountFlowEntity) entity).getSourceNo();
            case "finance_receivable" -> ((FinanceReceivableEntity) entity).getSourceNo();
            case "finance_receivable_pay" -> ((FinanceReceivablePayEntity) entity).getPayNo();
            case "finance_payable" -> ((FinancePayableEntity) entity).getSourceNo();
            case "finance_payable_pay" -> ((FinancePayablePayEntity) entity).getPayNo();
            case "finance_daily_close" -> ((FinanceDailyCloseEntity) entity).getCloseDate();
            case "finance_daily_ledger" -> ((FinanceDailyLedgerEntity) entity).getLedgerDate();
            default -> null;
        };
    }

    private String resolveDataType(String tableName) {
        return switch (tableName.toLowerCase()) {
            case "finance_account" -> "FINANCE_ACCOUNT";
            case "finance_account_flow" -> "FINANCE_ACCOUNT_FLOW";
            case "finance_receivable" -> "FINANCE_RECEIVABLE";
            case "finance_receivable_pay" -> "FINANCE_RECEIVABLE_PAY";
            case "finance_payable" -> "FINANCE_PAYABLE";
            case "finance_payable_pay" -> "FINANCE_PAYABLE_PAY";
            case "finance_daily_close" -> "FINANCE_DAILY_CLOSE";
            case "finance_daily_ledger" -> "FINANCE_DAILY_LEDGER";
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
                RabbitMQConstant.SYNC_UP_FINANCE_ROUTING_KEY,
                "FINANCE_SYNC"
        );
        Map<String, String> extParams = new HashMap<>();
        extParams.put("batchUuid", batchUuid);
        extParams.put("dataCount", String.valueOf(dataList.size()));
        message.setExtParams(extParams);

        mqMessageFacade.sendAsync(message);
        log.debug("财务同步消息发送成功: batchUuid={}, dataCount={}", batchUuid, dataList.size());
    }
}
