package com.psi.order.mapper;

import com.psi.order.entity.DocItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.ResultType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 单据明细 Mapper 接口
 * 提供完整的单据明细查询能力
 */
@Mapper
public interface DocItemMapper extends BaseMapper<DocItemEntity> {

    default List<DocItemEntity> selectByDocId(Long docId) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getDocId, docId)
                .orderByAsc(DocItemEntity::getLineNo));
    }

    default List<DocItemEntity> selectByDocNo(String docNo) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getDocNo, docNo)
                .orderByAsc(DocItemEntity::getLineNo));
    }

    default List<DocItemEntity> selectByGoodsId(Long goodsId) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getGoodsId, goodsId)
                .orderByDesc(DocItemEntity::getCreateTime));
    }

    default List<DocItemEntity> selectByGoodsCode(String goodsCode) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getGoodsCode, goodsCode)
                .orderByDesc(DocItemEntity::getCreateTime));
    }

    default List<DocItemEntity> selectByBatchNo(String batchNo) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getBatchNo, batchNo)
                .orderByDesc(DocItemEntity::getCreateTime));
    }

    default List<DocItemEntity> selectByStockId(Long stockId) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getStockId, stockId)
                .orderByDesc(DocItemEntity::getCreateTime));
    }

    default BigDecimal sumAmountByDocId(Long docId) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class)
                .select(DocItemEntity::getAmount)
                .eq(DocItemEntity::getDocId, docId);
        List<DocItemEntity> items = selectList(query);
        return items.stream()
                .map(DocItemEntity::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal sumQuantityByDocId(Long docId) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class)
                .select(DocItemEntity::getQuantity)
                .eq(DocItemEntity::getDocId, docId);
        List<DocItemEntity> items = selectList(query);
        return items.stream()
                .map(DocItemEntity::getQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal sumCostAmountByDocId(Long docId) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class)
                .select(DocItemEntity::getCostAmount)
                .eq(DocItemEntity::getDocId, docId);
        List<DocItemEntity> items = selectList(query);
        return items.stream()
                .map(DocItemEntity::getCostAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal sumPayAmountByDocId(Long docId) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class)
                .select(DocItemEntity::getPayAmount)
                .eq(DocItemEntity::getDocId, docId);
        List<DocItemEntity> items = selectList(query);
        return items.stream()
                .map(DocItemEntity::getPayAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal sumDiscountAmountByDocId(Long docId) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class)
                .select(DocItemEntity::getDiscountAmount)
                .eq(DocItemEntity::getDocId, docId);
        List<DocItemEntity> items = selectList(query);
        return items.stream()
                .map(DocItemEntity::getDiscountAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default List<DocItemEntity> selectByGoodsNameLike(String goodsName) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .like(DocItemEntity::getGoodsName, goodsName)
                .orderByDesc(DocItemEntity::getCreateTime));
    }

    default List<DocItemEntity> selectByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class);
        if (minAmount != null) {
            query.ge(DocItemEntity::getAmount, minAmount);
        }
        if (maxAmount != null) {
            query.le(DocItemEntity::getAmount, maxAmount);
        }
        query.orderByDesc(DocItemEntity::getAmount);
        return selectList(query);
    }

    default List<DocItemEntity> selectByQuantityRange(BigDecimal minQuantity, BigDecimal maxQuantity) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class);
        if (minQuantity != null) {
            query.ge(DocItemEntity::getQuantity, minQuantity);
        }
        if (maxQuantity != null) {
            query.le(DocItemEntity::getQuantity, maxQuantity);
        }
        query.orderByDesc(DocItemEntity::getQuantity);
        return selectList(query);
    }

    default BigDecimal sumQuantityByGoodsId(Long goodsId) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class)
                .select(DocItemEntity::getQuantity)
                .eq(DocItemEntity::getGoodsId, goodsId);
        List<DocItemEntity> items = selectList(query);
        return items.stream()
                .map(DocItemEntity::getQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal sumAmountByGoodsId(Long goodsId) {
        LambdaQueryWrapper<DocItemEntity> query = Wrappers.lambdaQuery(DocItemEntity.class)
                .select(DocItemEntity::getAmount)
                .eq(DocItemEntity::getGoodsId, goodsId);
        List<DocItemEntity> items = selectList(query);
        return items.stream()
                .map(DocItemEntity::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default List<DocItemEntity> selectByGoodsIdList(List<Long> goodsIdList) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .in(DocItemEntity::getGoodsId, goodsIdList)
                .orderByDesc(DocItemEntity::getCreateTime));
    }

    default List<DocItemEntity> selectByBatchNoList(List<String> batchNoList) {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .in(DocItemEntity::getBatchNo, batchNoList)
                .orderByDesc(DocItemEntity::getCreateTime));
    }

    default List<DocItemEntity> selectExpiredBatch() {
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .isNotNull(DocItemEntity::getExpiryDate)
                .lt(DocItemEntity::getExpiryDate, java.time.LocalDate.now().toString())
                .orderByDesc(DocItemEntity::getExpiryDate));
    }

    default List<DocItemEntity> selectNearExpiry(int days) {
        java.time.LocalDate expireDate = java.time.LocalDate.now().plusDays(days);
        return selectList(Wrappers.lambdaQuery(DocItemEntity.class)
                .isNotNull(DocItemEntity::getExpiryDate)
                .between(DocItemEntity::getExpiryDate, 
                        java.time.LocalDate.now().toString(), 
                        expireDate.toString())
                .orderByAsc(DocItemEntity::getExpiryDate));
    }

    default long countByDocId(Long docId) {
        return selectCount(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getDocId, docId));
    }

    default long countByGoodsId(Long goodsId) {
        return selectCount(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getGoodsId, goodsId));
    }

    default long countByBatchNo(String batchNo) {
        return selectCount(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getBatchNo, batchNo));
    }

    @Select("SELECT doc_id, COUNT(*) as count " +
            "FROM doc_item_entity " +
            "GROUP BY doc_id")
    @ResultType(Map.class)
    List<Map<String, Object>> countByDocIdGroup();

    @Select("SELECT goods_id, goods_code, goods_name, SUM(quantity) as total_quantity, SUM(amount) as total_amount " +
            "FROM doc_item_entity " +
            "GROUP BY goods_id, goods_code, goods_name " +
            "ORDER BY total_quantity DESC " +
            "LIMIT #{limit}")
    @ResultType(Map.class)
    List<Map<String, Object>> selectGoodsSalesTop(@Param("limit") int limit);

    @Select("SELECT batch_no, SUM(quantity) as total_quantity " +
            "FROM doc_item_entity " +
            "WHERE batch_no IS NOT NULL " +
            "GROUP BY batch_no")
    @ResultType(Map.class)
    List<Map<String, Object>> sumQuantityByBatchNo();

    default Integer selectMaxLineNo(Long docId) {
        DocItemEntity entity = selectOne(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getDocId, docId)
                .orderByDesc(DocItemEntity::getLineNo)
                .last("LIMIT 1"));
        return entity != null ? entity.getLineNo() : 0;
    }

    default DocItemEntity selectByDocIdAndLineNo(Long docId, Integer lineNo) {
        return selectOne(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getDocId, docId)
                .eq(DocItemEntity::getLineNo, lineNo));
    }

    default int deleteByDocId(Long docId) {
        return delete(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getDocId, docId));
    }

    default int deleteByDocIdAndLineNo(Long docId, Integer lineNo) {
        return delete(Wrappers.lambdaQuery(DocItemEntity.class)
                .eq(DocItemEntity::getDocId, docId)
                .eq(DocItemEntity::getLineNo, lineNo));
    }
}