package com.psi.goods.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.util.IdUtils;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.goods.entity.Goods;
import com.psi.goods.entity.GoodsCategory;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.entity.GoodsSkuSaleUnit;
import com.psi.goods.entity.GoodsBrand;
import com.psi.goods.entity.GoodsUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsDownSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendGoods(Goods entity) {
        sendSingleEntity(entity, "goods", entity.getTenantId(), entity.getDataUuid(), null);
    }

    public void sendGoodsCategory(GoodsCategory entity) {
        sendSingleEntity(entity, "goods_category", entity.getTenantId(), entity.getDataUuid(), null);
    }

    public void sendGoodsSku(GoodsSku entity) {
        sendSingleEntity(entity, "goods_sku", entity.getTenantId(), entity.getDataUuid(), null);
    }

    public void sendGoodsBrand(GoodsBrand entity) {
        sendSingleEntity(entity, "goods_brand", entity.getTenantId(), entity.getDataUuid(), null);
    }

    public void sendGoodsUnit(GoodsUnit entity) {
        sendSingleEntity(entity, "goods_unit", entity.getTenantId(), entity.getDataUuid(), null);
    }

    public void sendGoodsSkuSaleUnit(GoodsSkuSaleUnit entity) {
        sendSingleEntity(entity, "goods_sku_sale_unit", entity.getTenantId(), entity.getDataUuid(), null);
    }

    private void sendSingleEntity(Object entity, String tableName, Long tenantId, String dataUuid, String shopCode) {
        try {
            String batchUuid = IdUtils.snowflakeIdStr();
            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

            SyncDataDTO dataDTO = new SyncDataDTO();
            dataDTO.setRecordId(IdUtils.snowflakeIdStr());
            dataDTO.setBatchUuid(batchUuid);
            dataDTO.setTableName(tableName);
            dataDTO.setJsonData(objectMapper.writeValueAsString(entity));
            dataDTO.setTenantId(String.valueOf(tenantId));
            dataDTO.setShopCode(shopCode);
            dataDTO.setCreateTime(currentTime);

            SyncBatchDTO batchDTO = new SyncBatchDTO();
            batchDTO.setBatchUuid(batchUuid);
            batchDTO.setTenantId(String.valueOf(tenantId));
            batchDTO.setShopCode(shopCode);
            batchDTO.setCreateTime(currentTime);
            batchDTO.setDataList(List.of(dataDTO));

            MqCommonMessage<SyncBatchDTO> message = MessageFactory.create(
                    batchDTO,
                    RabbitMQConstant.SYNC_DOWN_EXCHANGE,
                    RabbitMQConstant.SYNC_DOWN_ROUTING_KEY,
                    "GOODS_DOWN_SYNC"
            );

            Map<String, String> extParams = new HashMap<>();
            extParams.put("batchUuid", batchUuid);
            extParams.put("dataCount", "1");
            extParams.put("tableName", tableName);
            message.setExtParams(extParams);

            mqMessageFacade.sendAsync(message);

            log.info("[goods] 下行同步消息已发送: tableName={}, dataUuid={}", tableName, dataUuid);
        } catch (Exception e) {
            log.error("[goods] 下行同步消息发送失败: tableName={}", tableName, e);
        }
    }
}
