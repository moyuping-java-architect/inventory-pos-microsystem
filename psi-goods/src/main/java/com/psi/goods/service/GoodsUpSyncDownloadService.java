package com.psi.goods.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.SyncUpFeignClient;
import com.psi.common.mybatis.entity.BaseEntity;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.service.BaseUpSyncDownloadService;
import com.psi.goods.entity.Goods;
import com.psi.goods.entity.GoodsBrand;
import com.psi.goods.entity.GoodsCategory;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.entity.GoodsSkuSaleUnit;
import com.psi.goods.entity.GoodsUnit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodsUpSyncDownloadService extends BaseUpSyncDownloadService {

    private final ObjectMapper objectMapper;
    private final BatchUtils batchUtils;
    private final GoodsService goodsService;
    private final GoodsCategoryService goodsCategoryService;
    private final GoodsSkuService goodsSkuService;
    private final GoodsSkuSaleUnitService goodsSkuSaleUnitService;
    private final GoodsUnitService goodsUnitService;
    private final GoodsBrandService goodsBrandService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GoodsUpSyncDownloadService(
            SyncUpFeignClient syncUpFeignClient,
            ObjectMapper objectMapper,
            BatchUtils batchUtils,
            GoodsService goodsService,
            GoodsCategoryService goodsCategoryService,
            GoodsSkuService goodsSkuService,
            GoodsSkuSaleUnitService goodsSkuSaleUnitService,
            GoodsUnitService goodsUnitService,
            GoodsBrandService goodsBrandService) {
        super(syncUpFeignClient);
        this.objectMapper = objectMapper;
        this.batchUtils = batchUtils;
        this.goodsService = goodsService;
        this.goodsCategoryService = goodsCategoryService;
        this.goodsSkuService = goodsSkuService;
        this.goodsSkuSaleUnitService = goodsSkuSaleUnitService;
        this.goodsUnitService = goodsUnitService;
        this.goodsBrandService = goodsBrandService;
    }

    @Override
    protected String getModuleName() {
        return "goods";
    }

    @Override
    protected boolean processTableData(String tableName, String jsonData, String batchUuid) {
        log.info("[goods] process up data: tableName={}, batchUuid={}", tableName, batchUuid);
        try {
            switch (tableName) {
                case "goods":
                    return processGoods(jsonData);
                case "goods_category":
                    return processGoodsCategory(jsonData);
                case "goods_sku":
                    return processGoodsSku(jsonData);
                case "goods_sku_sale_unit":
                    return processGoodsSkuSaleUnit(jsonData);
                case "goods_unit":
                    return processGoodsUnit(jsonData);
                case "goods_brand":
                    return processGoodsBrand(jsonData);
                default:
                    log.warn("[goods] 未知表名: {}", tableName);
                    return false;
            }
        } catch (Exception e) {
            log.error("[goods] 处理上行数据失败: tableName={}, batchUuid={}, error={}",
                    tableName, batchUuid, e.getMessage(), e);
            return false;
        }
    }

    private boolean processGoods(String jsonData) throws Exception {
        List<Goods> entities = parseJsonToEntityList(jsonData, Goods.class, "goods");
        return batchProcessEntities(entities, goodsService);
    }

    private boolean processGoodsCategory(String jsonData) throws Exception {
        List<GoodsCategory> entities = parseJsonToEntityList(jsonData, GoodsCategory.class, "goods_category");
        return batchProcessEntities(entities, goodsCategoryService);
    }

    private boolean processGoodsSku(String jsonData) throws Exception {
        List<GoodsSku> entities = parseJsonToEntityList(jsonData, GoodsSku.class, "goods_sku");
        return batchProcessEntities(entities, goodsSkuService);
    }

    private boolean processGoodsSkuSaleUnit(String jsonData) throws Exception {
        List<GoodsSkuSaleUnit> entities = parseJsonToEntityList(jsonData, GoodsSkuSaleUnit.class, "goods_sku_sale_unit");
        return batchProcessEntities(entities, goodsSkuSaleUnitService);
    }

    private boolean processGoodsUnit(String jsonData) throws Exception {
        List<GoodsUnit> entities = parseJsonToEntityList(jsonData, GoodsUnit.class, "goods_unit");
        return batchProcessEntities(entities, goodsUnitService);
    }

    private boolean processGoodsBrand(String jsonData) throws Exception {
        List<GoodsBrand> entities = parseJsonToEntityList(jsonData, GoodsBrand.class, "goods_brand");
        return batchProcessEntities(entities, goodsBrandService);
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
            log.warn("[goods] 实体 dataUuid 均为空，跳过处理");
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

        log.info("[goods] 批量处理完成: 新增={}, 更新={}, 总数={}",
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
                log.error("[goods] JSON解析兼容格式也失败: tableName={}", tableName);
                throw e2;
            }
        }
    }
}
