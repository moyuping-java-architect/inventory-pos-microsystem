package com.psi.goods.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.context.VirtualThreadContextWrapper;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.util.IdUtils;
import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.dto.sync.SyncDataDTO;
import com.psi.goods.entity.*;
import com.psi.goods.mapper.*;
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
 * 商品微服务MQ生产者
 * 参照收银微服务CashierSyncProducer实现商品相关数据上传功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsSyncProducer {

    private final MqMessageFacade mqMessageFacade;
    private final ObjectMapper objectMapper;

    private final GoodsMapper goodsMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsCategoryMapper goodsCategoryMapper;
    private final GoodsBrandMapper goodsBrandMapper;
    private final GoodsUnitMapper goodsUnitMapper;
    private final GoodsSkuSaleUnitMapper goodsSkuSaleUnitMapper;
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
     * 异步同步所有商品数据到sync-ms
     * 从sync_log表获取上次上传时间，查询update_time大于该时间的所有数据
     */
    public void syncAllAsync() {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

                // 获取上次上传时间
                String lastUploadTime = getLastUploadTime();

                log.info("商品数据同步开始: lastUploadTime={}", lastUploadTime);

                // 分页同步商品主表数据
                syncGoodsByPage(lastUploadTime, currentTime);

                // 分页同步SKU数据
                syncSkuByPage(lastUploadTime, currentTime);

                // 分页同步分类数据
                syncCategoriesByPage(lastUploadTime, currentTime);

                // 分页同步品牌数据
                syncBrandsByPage(lastUploadTime, currentTime);

                // 分页同步单位数据
                syncUnitsByPage(lastUploadTime, currentTime);

                // 分页同步SKU销售单位数据
                syncSkuSaleUnitsByPage(lastUploadTime, currentTime);

                // 更新上次上传时间
                updateLastUploadTime(currentTime);

                log.info("商品数据同步全部完成");

            } catch (Exception e) {
                log.error("商品数据同步消息发送失败", e);
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
        // 默认返回一个较早的时间
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

    // ==================== 分页同步各表数据 ====================

    /**
     * 分页同步商品主表
     */
    private void syncGoodsByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                List<Goods> goodsList = goodsMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);

                if (goodsList == null || goodsList.isEmpty()) {
                    log.info("商品主表数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();

                for (Goods goods : goodsList) {
                    dataList.add(buildSyncData(batchUuid, String.valueOf(goods.getTenantId()), null,
                            "goods", goods));
                }

                sendBatchData(batchUuid, dataList, currentTime);
                log.info("商品主表数据同步: page={}, count={}", pageNum, dataList.size());

            } catch (Exception e) {
                log.error("商品主表数据同步失败: page={}", pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("商品主表数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 分页同步SKU数据
     */
    private void syncSkuByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                List<GoodsSku> skuList = goodsSkuMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);

                if (skuList == null || skuList.isEmpty()) {
                    log.info("SKU数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();

                for (GoodsSku sku : skuList) {
                    dataList.add(buildSyncData(batchUuid, String.valueOf(sku.getTenantId()), null,
                            "goods_sku", sku));
                }

                sendBatchData(batchUuid, dataList, currentTime);
                log.info("SKU数据同步: page={}, count={}", pageNum, dataList.size());

            } catch (Exception e) {
                log.error("SKU数据同步失败: page={}", pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("SKU数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 分页同步分类数据
     */
    private void syncCategoriesByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                List<GoodsCategory> categoryList = goodsCategoryMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);

                if (categoryList == null || categoryList.isEmpty()) {
                    log.info("分类数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();

                for (GoodsCategory category : categoryList) {
                    dataList.add(buildSyncData(batchUuid, String.valueOf(category.getTenantId()), null,
                            "goods_category", category));
                }

                sendBatchData(batchUuid, dataList, currentTime);
                log.info("分类数据同步: page={}, count={}", pageNum, dataList.size());

            } catch (Exception e) {
                log.error("分类数据同步失败: page={}", pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("分类数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 分页同步品牌数据
     */
    private void syncBrandsByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                List<GoodsBrand> brandList = goodsBrandMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);

                if (brandList == null || brandList.isEmpty()) {
                    log.info("品牌数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();

                for (GoodsBrand brand : brandList) {
                    dataList.add(buildSyncData(batchUuid, String.valueOf(brand.getTenantId()), null,
                            "goods_brand", brand));
                }

                sendBatchData(batchUuid, dataList, currentTime);
                log.info("品牌数据同步: page={}, count={}", pageNum, dataList.size());

            } catch (Exception e) {
                log.error("品牌数据同步失败: page={}", pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("品牌数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 分页同步单位数据
     */
    private void syncUnitsByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                List<GoodsUnit> unitList = goodsUnitMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);

                if (unitList == null || unitList.isEmpty()) {
                    log.info("单位数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();

                for (GoodsUnit unit : unitList) {
                    dataList.add(buildSyncData(batchUuid, String.valueOf(unit.getTenantId()), null,
                            "goods_unit", unit));
                }

                sendBatchData(batchUuid, dataList, currentTime);
                log.info("单位数据同步: page={}, count={}", pageNum, dataList.size());

            } catch (Exception e) {
                log.error("单位数据同步失败: page={}", pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("单位数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    /**
     * 分页同步SKU销售单位数据
     */
    private void syncSkuSaleUnitsByPage(String lastUploadTime, String currentTime) {
        int pageNum = 1;
        boolean hasMoreData = true;

        while (hasMoreData && pageNum <= MAX_PAGES) {
            int offset = (pageNum - 1) * BATCH_SIZE;

            try {
                List<GoodsSkuSaleUnit> saleUnitList = goodsSkuSaleUnitMapper.selectByUpdateTimeAfterPage(lastUploadTime, offset, BATCH_SIZE);

                if (saleUnitList == null || saleUnitList.isEmpty()) {
                    log.info("SKU销售单位数据同步完成，共处理 {} 页", pageNum - 1);
                    hasMoreData = false;
                    continue;
                }

                String batchUuid = IdUtils.snowflakeIdStr();
                List<SyncDataDTO> dataList = new ArrayList<>();

                for (GoodsSkuSaleUnit saleUnit : saleUnitList) {
                    dataList.add(buildSyncData(batchUuid, String.valueOf(saleUnit.getTenantId()), null,
                            "goods_sku_sale_unit", saleUnit));
                }

                sendBatchData(batchUuid, dataList, currentTime);
                log.info("SKU销售单位数据同步: page={}, count={}", pageNum, dataList.size());

            } catch (Exception e) {
                log.error("SKU销售单位数据同步失败: page={}", pageNum, e);
            }

            pageNum++;
        }

        if (pageNum > MAX_PAGES) {
            log.warn("SKU销售单位数据同步达到最大页数限制: {}", MAX_PAGES);
        }
    }

    // ==================== 公共方法 ====================

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

            sendSyncMessage(batchDTO, RabbitMQConstant.SYNC_UP_GOODS_ROUTING_KEY);
        } catch (Exception e) {
            log.error("发送批次数据失败: batchUuid={}", batchUuid, e);
        }
    }

    /**
     * 构建同步数据DTO
     */
    private SyncDataDTO buildSyncData(String batchUuid, String tenantId, String shopCode,
                                       String tableName, Object entity) throws JsonProcessingException {
        SyncDataDTO dataDTO = new SyncDataDTO();

        dataDTO.setRecordId(IdUtils.snowflakeIdStr());
        dataDTO.setBatchUuid(batchUuid);
        dataDTO.setTenantId(tenantId);
        dataDTO.setShopCode(shopCode);
        dataDTO.setTableName(tableName);
        dataDTO.setJsonData(objectMapper.writeValueAsString(entity));
        dataDTO.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));

        setDataTypeAndBusinessKey(dataDTO, tableName, entity);

        return dataDTO;
    }

    /**
     * 设置数据类型和业务主键
     */
    private void setDataTypeAndBusinessKey(SyncDataDTO dataDTO, String tableName, Object entity) {
        String dataType = resolveDataType(tableName);
        dataDTO.setDataType(dataType);

        if (entity instanceof Goods) {
            Goods goods = (Goods) entity;
            dataDTO.setBusinessKey(goods.getGoodsCode());
            dataDTO.setOriginalId(goods.getId());
        } else if (entity instanceof GoodsSku) {
            GoodsSku sku = (GoodsSku) entity;
            dataDTO.setBusinessKey(sku.getSkuCode());
            dataDTO.setOriginalId(sku.getId());
        } else if (entity instanceof GoodsCategory) {
            GoodsCategory category = (GoodsCategory) entity;
            dataDTO.setBusinessKey(category.getCategoryCode());
            dataDTO.setOriginalId(category.getId());
        } else if (entity instanceof GoodsBrand) {
            GoodsBrand brand = (GoodsBrand) entity;
            dataDTO.setBusinessKey(brand.getBrandCode());
            dataDTO.setOriginalId(brand.getId());
        } else if (entity instanceof GoodsUnit) {
            GoodsUnit unit = (GoodsUnit) entity;
            dataDTO.setBusinessKey(unit.getUnitCode());
            dataDTO.setOriginalId(unit.getId());
        } else if (entity instanceof GoodsSkuSaleUnit) {
            GoodsSkuSaleUnit saleUnit = (GoodsSkuSaleUnit) entity;
            dataDTO.setBusinessKey(saleUnit.getGoodsUnifyCode());
            dataDTO.setOriginalId(saleUnit.getId());
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
            case "goods" -> "GOODS";
            case "goods_sku" -> "SKU";
            case "goods_category" -> "CATEGORY";
            case "goods_brand" -> "BRAND";
            case "goods_unit" -> "UNIT";
            case "goods_sku_sale_unit" -> "SALE_UNIT";
            default -> "OTHER";
        };
    }

    /**
     * 发送同步消息到MQ
     */
    private void sendSyncMessage(SyncBatchDTO batchDTO, String routingKey) throws JsonProcessingException {
        MqCommonMessage<SyncBatchDTO> message = MessageFactory.create(
                batchDTO,
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                routingKey,
                "GOODS_SYNC"
        );

        Map<String, String> extParams = new HashMap<>();
        extParams.put("batchUuid", batchDTO.getBatchUuid());
        extParams.put("dataCount", String.valueOf(batchDTO.getDataList().size()));
        message.setExtParams(extParams);

        mqMessageFacade.sendAsync(message);

        log.debug("商品同步消息发送成功: batchUuid={}, dataCount={}",
                batchDTO.getBatchUuid(), batchDTO.getDataList().size());
    }
}