package com.psi.stock.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.feign.DocFeignResponse.DocFeignItemResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.order.constant.DocTypeConstant.DocType;
import com.psi.stock.dto.*;
import com.psi.stock.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 库存流程完成MQ监听器
 * 
 * <p>监听库存报损/报溢/盘点/调拨流程完成队列，生成正式业务数据
 * 
 * <p>通过 Feign 远程调用 psi-flow 获取单据数据并更新状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockProcessCompletedListener {

    private final DocFeignClient docFeignClient;
    private final StockLossMainService stockLossMainService;
    private final StockOverMainService stockOverMainService;
    private final StockCheckMainService stockCheckMainService;
    private final StockTransferMainService stockTransferMainService;
    private final InventoryInitMainService inventoryInitMainService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final MessageIdempotencyService messageIdempotencyService;

    /**
     * 根据商品编码查询默认 SKU 编码（用于前端未传 skuCode 时的兜底）
     */
    private String getDefaultSkuCode(String goodsCode) {
        if (goodsCode == null || goodsCode.trim().isEmpty()) {
            return null;
        }
        try {
            List<String> results = jdbcTemplate.queryForList(
                    "SELECT sku_code FROM psi_goods.goods_sku WHERE goods_code = ? LIMIT 1",
                    String.class, goodsCode);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.warn("查询商品默认SKU失败: goodsCode={}, error={}", goodsCode, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_LOSS_QUEUE)
    public void onLossCompleted(MqCommonMessage<?> message) {
        handleStockCompleted(message, DocType.STOCK_LOSS);
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_OVERFLOW_QUEUE)
    public void onOverflowCompleted(MqCommonMessage<?> message) {
        handleStockCompleted(message, DocType.STOCK_OVERFLOW);
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_CHECK_QUEUE)
    public void onCheckCompleted(MqCommonMessage<?> message) {
        handleStockCompleted(message, DocType.STOCK_CHECK);
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_STOCK_QUEUE)
    public void onStockCompleted(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        String messageId = message.getMessageId();
        log.info("收到库存流程完成消息: bizId={}, messageId={}", bizId, messageId);

        messageIdempotencyService.execute(messageId, () -> {
            try {
                DocFeignResponse doc = resolveDocFromMessage(data, bizId);
                if (doc == null) {
                    return null;
                }
                DocType docType = DocType.fromCode(doc.getDocType());

                switch (docType) {
                    case STOCK_TRANSFER:
                        saveStockTransfer(doc);
                        break;
                    case INVENTORY_INIT:
                        saveInventoryInit(doc);
                        break;
                    default:
                        log.warn("未知库存单据类型: {}", doc.getDocType());
                        break;
                }

                log.info("库存单据已完成: bizId={}, docType={}", bizId, doc.getDocType());
            } catch (Exception e) {
                log.error("处理库存流程完成消息失败: bizId={}, error={}", bizId, e.getMessage(), e);
                throw e;
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private void handleStockCompleted(MqCommonMessage<?> message, DocType docType) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        String messageId = message.getMessageId();
        log.info("收到库存流程完成消息: bizId={}, docType={}, messageId={}", bizId, docType.getCode(), messageId);

        messageIdempotencyService.execute(messageId, () -> {
            try {
                DocFeignResponse doc = resolveDocFromMessage(data, bizId);
                if (doc == null) {
                    return null;
                }

                switch (docType) {
                    case STOCK_LOSS:
                        saveStockLoss(doc);
                        break;
                    case STOCK_OVERFLOW:
                        saveStockOver(doc);
                        break;
                    case STOCK_CHECK:
                        saveStockCheck(doc);
                        break;
                    case STOCK_TRANSFER:
                        saveStockTransfer(doc);
                        break;
                    case INVENTORY_INIT:
                        saveInventoryInit(doc);
                        break;
                    default:
                        log.warn("未知库存单据类型: {}", docType);
                        break;
                }

                log.info("库存单据已完成: bizId={}, docType={}", bizId, docType.getCode());
            } catch (Exception e) {
                log.error("处理库存流程完成消息失败: bizId={}, docType={}, error={}", bizId, docType.getCode(), e.getMessage(), e);
                throw e;
            }
            return null;
        });
    }

    /**
     * 优先从 MQ 消息里取 docData（工作流已查好），没有则 Feign 查询（兼容老消息）
     */
    private DocFeignResponse resolveDocFromMessage(Map<String, Object> data, String bizId) {
        Object docData = data.get("docData");
        if (docData != null && !docData.toString().trim().isEmpty()) {
            try {
                DocFeignResponse doc = objectMapper.readValue(docData.toString(), DocFeignResponse.class);
                log.info("库存流程完成：从MQ消息中解析单据数据: bizId={}", bizId);
                return doc;
            } catch (Exception e) {
                log.warn("库存流程完成：MQ中docData解析失败，降级Feign查询: bizId={}, error={}", bizId, e.getMessage());
            }
        }

        CommonResult<DocFeignResponse> result = docFeignClient.findByDocNo(bizId);
        if (!result.isSuccess() || result.getData() == null) {
            log.warn("库存流程完成：单据不存在或查询失败: bizId={}, msg={}", bizId, result.getMessage());
            return null;
        }
        log.info("库存流程完成：通过Feign查询单据数据: bizId={}", bizId);
        return result.getData();
    }

    private void saveStockLoss(DocFeignResponse doc) {
        StockLossSaveDTO saveDTO = new StockLossSaveDTO();
        saveDTO.setLossNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setRemark(doc.getRemark());
        saveDTO.setItems(buildLossItems(doc.getItems()));
        CommonResult<StockLossMainDTO> result = stockLossMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            stockLossMainService.audit(result.getData().getId(), 1);
        }
        log.info("报损单正式数据已生成: docNo={}", doc.getDocNo());
    }

    private void saveStockOver(DocFeignResponse doc) {
        StockOverSaveDTO saveDTO = new StockOverSaveDTO();
        saveDTO.setOverNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setRemark(doc.getRemark());
        saveDTO.setItems(buildOverItems(doc.getItems()));
        CommonResult<StockOverMainDTO> result = stockOverMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            stockOverMainService.audit(result.getData().getId(), 1);
        }
        log.info("报溢单正式数据已生成: docNo={}", doc.getDocNo());
    }

    private void saveStockCheck(DocFeignResponse doc) {
        StockCheckSaveDTO saveDTO = new StockCheckSaveDTO();
        saveDTO.setCheckNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setRemark(doc.getRemark());
        saveDTO.setItems(buildCheckItems(doc.getItems()));
        CommonResult<StockCheckMainDTO> result = stockCheckMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            stockCheckMainService.audit(result.getData().getId(), 1);
        }
        log.info("盘点单正式数据已生成: docNo={}", doc.getDocNo());
    }

    private void saveStockTransfer(DocFeignResponse doc) {
        StockTransferSaveDTO saveDTO = new StockTransferSaveDTO();
        saveDTO.setTransferNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setFromWarehouseCode(doc.getWarehouseCode());
        saveDTO.setFromWarehouseName(doc.getWarehouseName());
        // 从 extJson 中解析调入仓库信息
        String extJson = doc.getExtJson();
        if (extJson != null && !extJson.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> extMap = mapper.readValue(extJson, java.util.Map.class);
                if (extMap.get("toWarehouseCode") != null) {
                    saveDTO.setToWarehouseCode((String) extMap.get("toWarehouseCode"));
                }
                if (extMap.get("toWarehouseName") != null) {
                    saveDTO.setToWarehouseName((String) extMap.get("toWarehouseName"));
                }
            } catch (Exception e) {
                log.warn("解析调拨单扩展字段失败: docNo={}, extJson={}", doc.getDocNo(), extJson);
            }
        }
        // 如果 extJson 中没有，尝试从 partnerName 中解析（格式："调出仓库 → 调入仓库"）
        if (saveDTO.getToWarehouseName() == null || saveDTO.getToWarehouseName().isEmpty()) {
            String partnerName = doc.getPartnerName();
            if (partnerName != null && partnerName.contains("→")) {
                String toWarehouseName = partnerName.split("→")[1].trim();
                saveDTO.setToWarehouseName(toWarehouseName);
            }
        }
        saveDTO.setRemark(doc.getRemark());
        saveDTO.setItems(buildTransferItems(doc.getItems()));
        CommonResult<StockTransferMainDTO> result = stockTransferMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            stockTransferMainService.audit(result.getData().getId(), 1);
        }
        log.info("调拨单正式数据已生成: docNo={}", doc.getDocNo());
    }

    private void saveInventoryInit(DocFeignResponse doc) {
        InventoryInitSaveDTO saveDTO = new InventoryInitSaveDTO();
        saveDTO.setInitNo(doc.getDocNo());
        saveDTO.setDocName(doc.getDocName());
        saveDTO.setWarehouseCode(doc.getWarehouseCode());
        saveDTO.setWarehouseName(doc.getWarehouseName());
        saveDTO.setRemark(doc.getRemark());
        saveDTO.setItems(buildInitItems(doc.getItems()));
        CommonResult<Long> result = inventoryInitMainService.save(saveDTO);
        if (result.isSuccess() && result.getData() != null) {
            inventoryInitMainService.audit(result.getData(), 1);
        }
        log.info("库存初始化单正式数据已生成: docNo={}", doc.getDocNo());
    }

    private void fillSkuInfo(DocFeignItemResponse di, StockLossItemSaveDTO item) {
        String skuCode = di.getSkuCode() != null ? di.getSkuCode() : getDefaultSkuCode(di.getGoodsCode());
        item.setSkuCode(skuCode);
        item.setSkuName(di.getSkuName() != null ? di.getSkuName() : skuCode);
    }

    private void fillSkuInfo(DocFeignItemResponse di, StockOverItemSaveDTO item) {
        String skuCode = di.getSkuCode() != null ? di.getSkuCode() : getDefaultSkuCode(di.getGoodsCode());
        item.setSkuCode(skuCode);
        item.setSkuName(di.getSkuName() != null ? di.getSkuName() : skuCode);
    }

    private void fillSkuInfo(DocFeignItemResponse di, StockCheckItemSaveDTO item) {
        String skuCode = di.getSkuCode() != null ? di.getSkuCode() : getDefaultSkuCode(di.getGoodsCode());
        item.setSkuCode(skuCode);
        item.setSkuName(di.getSkuName() != null ? di.getSkuName() : skuCode);
    }

    private void fillSkuInfo(DocFeignItemResponse di, StockTransferItemSaveDTO item) {
        String skuCode = di.getSkuCode() != null ? di.getSkuCode() : getDefaultSkuCode(di.getGoodsCode());
        item.setSkuCode(skuCode);
        item.setSkuName(di.getSkuName() != null ? di.getSkuName() : skuCode);
    }

    private void fillSkuInfo(DocFeignItemResponse di, InventoryInitItemSaveDTO item) {
        String skuCode = di.getSkuCode() != null ? di.getSkuCode() : getDefaultSkuCode(di.getGoodsCode());
        item.setSkuCode(skuCode);
        item.setSkuName(di.getSkuName() != null ? di.getSkuName() : skuCode);
    }

    private List<StockLossItemSaveDTO> buildLossItems(List<DocFeignItemResponse> docItems) {
        List<StockLossItemSaveDTO> items = new ArrayList<>();
        if (docItems != null) {
            for (DocFeignItemResponse di : docItems) {
                StockLossItemSaveDTO item = new StockLossItemSaveDTO();
                item.setGoodsId(di.getGoodsId());
                item.setGoodsCode(di.getGoodsCode());
                fillSkuInfo(di, item);
                item.setGoodsName(di.getGoodsName());
                item.setGoodsSpec(di.getGoodsSpec());
                item.setUnit(di.getGoodsUnit());
                item.setConversionRate(di.getConversionRate());
                item.setLossQuantity(di.getQuantity());
                item.setUnitPrice(di.getUnitPrice());
                item.setTaxRate(di.getTaxRate());
                items.add(item);
            }
        }
        return items;
    }

    private List<StockOverItemSaveDTO> buildOverItems(List<DocFeignItemResponse> docItems) {
        List<StockOverItemSaveDTO> items = new ArrayList<>();
        if (docItems != null) {
            for (DocFeignItemResponse di : docItems) {
                StockOverItemSaveDTO item = new StockOverItemSaveDTO();
                item.setGoodsId(di.getGoodsId());
                item.setGoodsCode(di.getGoodsCode());
                fillSkuInfo(di, item);
                item.setGoodsName(di.getGoodsName());
                item.setGoodsSpec(di.getGoodsSpec());
                item.setUnit(di.getGoodsUnit());
                item.setConversionRate(di.getConversionRate());
                item.setOverQuantity(di.getQuantity());
                item.setUnitPrice(di.getUnitPrice());
                item.setTaxRate(di.getTaxRate());
                items.add(item);
            }
        }
        return items;
    }

    private List<StockCheckItemSaveDTO> buildCheckItems(List<DocFeignItemResponse> docItems) {
        List<StockCheckItemSaveDTO> items = new ArrayList<>();
        if (docItems != null) {
            for (DocFeignItemResponse di : docItems) {
                StockCheckItemSaveDTO item = new StockCheckItemSaveDTO();
                item.setGoodsId(di.getGoodsId());
                item.setGoodsCode(di.getGoodsCode());
                fillSkuInfo(di, item);
                item.setGoodsName(di.getGoodsName());
                item.setGoodsSpec(di.getGoodsSpec());
                item.setUnit(di.getGoodsUnit());
                // 流程单据中的数量作为实际盘点数量，账面数量从库存实时查询
                item.setActualQuantity(di.getQuantity());
                item.setUnitPrice(di.getUnitPrice());
                items.add(item);
            }
        }
        return items;
    }

    private List<StockTransferItemSaveDTO> buildTransferItems(List<DocFeignItemResponse> docItems) {
        List<StockTransferItemSaveDTO> items = new ArrayList<>();
        if (docItems != null) {
            for (DocFeignItemResponse di : docItems) {
                StockTransferItemSaveDTO item = new StockTransferItemSaveDTO();
                item.setGoodsId(di.getGoodsId());
                item.setGoodsCode(di.getGoodsCode());
                fillSkuInfo(di, item);
                item.setGoodsName(di.getGoodsName());
                item.setGoodsSpec(di.getGoodsSpec());
                item.setUnit(di.getGoodsUnit());
                item.setConversionRate(di.getConversionRate());
                item.setTransferQuantity(di.getQuantity());
                item.setUnitPrice(di.getUnitPrice());
                item.setTaxRate(di.getTaxRate());
                items.add(item);
            }
        }
        return items;
    }

    private List<InventoryInitItemSaveDTO> buildInitItems(List<DocFeignItemResponse> docItems) {
        List<InventoryInitItemSaveDTO> items = new ArrayList<>();
        if (docItems != null) {
            for (DocFeignItemResponse di : docItems) {
                InventoryInitItemSaveDTO item = new InventoryInitItemSaveDTO();
                item.setGoodsId(di.getGoodsId());
                item.setGoodsCode(di.getGoodsCode());
                fillSkuInfo(di, item);
                item.setGoodsName(di.getGoodsName());
                item.setGoodsSpec(di.getGoodsSpec());
                item.setUnit(di.getGoodsUnit());
                item.setConversionRate(di.getConversionRate());
                item.setInitQuantity(di.getQuantity());
                item.setUnitPrice(di.getUnitPrice());
                item.setTaxRate(di.getTaxRate());
                items.add(item);
            }
        }
        return items;
    }
}