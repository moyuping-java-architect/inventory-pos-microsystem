package com.psi.order.mapper;

import com.psi.order.entity.DocEntity;
import com.psi.order.constant.DocTypeConstant.DocType;
import com.psi.order.constant.DocTypeConstant.DocStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.ResultType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 单据 Mapper 接口
 * 提供完整的单据查询能力
 */
@Mapper
public interface DocMapper extends BaseMapper<DocEntity> {

    default DocEntity selectByDocNo(String docNo) {
        return selectOne(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getDocNo, docNo));
    }

    default List<DocEntity> selectByDocType(String docType) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getDocType, docType)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByStatus(Integer status) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, status)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByCreatorId(String creatorId) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getCreatorId, creatorId)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByDeptId(String deptId) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getDeptId, deptId)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByWarehouse(Long warehouseId) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getWarehouseId, warehouseId)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByPartnerId(String partnerId) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getPartnerId, partnerId)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectPendingApprove() {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.SUBMITTED.getValue())
                .orderByAsc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectApproving() {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.APPROVING.getValue())
                .orderByAsc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectApproved() {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.APPROVED.getValue())
                .orderByDesc(DocEntity::getApproveTime));
    }

    default List<DocEntity> selectExecuting() {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.EXECUTING.getValue())
                .orderByDesc(DocEntity::getExecuteTime));
    }

    default List<DocEntity> selectCompleted() {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.COMPLETED.getValue())
                .orderByDesc(DocEntity::getCompleteTime));
    }

    default List<DocEntity> selectCancelled() {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.CANCELLED.getValue())
                .orderByDesc(DocEntity::getCancelTime));
    }

    default List<DocEntity> selectRejected() {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.REJECTED.getValue())
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .between(DocEntity::getCreateTime, startDate, endDate)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByDocNoLike(String docNo) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .like(DocEntity::getDocNo, docNo)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByCreatorNameLike(String creatorName) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .like(DocEntity::getCreatorName, creatorName)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default IPage<DocEntity> selectPage(Page<DocEntity> page, 
                                        String docType, 
                                        Integer status, 
                                        String creatorId,
                                        String deptId,
                                        Long warehouseId,
                                        String partnerId,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        String docNoLike) {
        LambdaQueryWrapper<DocEntity> query = Wrappers.lambdaQuery(DocEntity.class);
        
        if (docType != null && !docType.isEmpty()) {
            query.eq(DocEntity::getDocType, docType);
        }
        if (status != null) {
            query.eq(DocEntity::getStatus, status);
        }
        if (creatorId != null && !creatorId.isEmpty()) {
            query.eq(DocEntity::getCreatorId, creatorId);
        }
        if (deptId != null && !deptId.isEmpty()) {
            query.eq(DocEntity::getDeptId, deptId);
        }
        if (warehouseId != null) {
            query.eq(DocEntity::getWarehouseId, warehouseId);
        }
        if (partnerId != null && !partnerId.isEmpty()) {
            query.eq(DocEntity::getPartnerId, partnerId);
        }
        if (startDate != null) {
            query.ge(DocEntity::getCreateTime, startDate);
        }
        if (endDate != null) {
            query.le(DocEntity::getCreateTime, endDate);
        }
        if (docNoLike != null && !docNoLike.isEmpty()) {
            query.like(DocEntity::getDocNo, docNoLike);
        }
        
        query.orderByDesc(DocEntity::getCreateTime);
        return selectPage(page, query);
    }

    default IPage<DocEntity> selectPageByStatusList(Page<DocEntity> page,
                                                     String docType,
                                                     List<Integer> statusList,
                                                     String creatorId) {
        LambdaQueryWrapper<DocEntity> query = Wrappers.lambdaQuery(DocEntity.class);
        
        if (docType != null && !docType.isEmpty()) {
            query.eq(DocEntity::getDocType, docType);
        }
        if (statusList != null && !statusList.isEmpty()) {
            query.in(DocEntity::getStatus, statusList);
        }
        if (creatorId != null && !creatorId.isEmpty()) {
            query.eq(DocEntity::getCreatorId, creatorId);
        }
        
        query.orderByDesc(DocEntity::getCreateTime);
        return selectPage(page, query);
    }

    default List<DocEntity> selectByStatusList(List<Integer> statusList) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .in(DocEntity::getStatus, statusList)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByDocTypeList(List<String> docTypeList) {
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .in(DocEntity::getDocType, docTypeList)
                .orderByDesc(DocEntity::getCreateTime));
    }

    default long countByDocType(String docType) {
        return selectCount(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getDocType, docType));
    }

    default long countByStatus(Integer status) {
        return selectCount(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, status));
    }

    default long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return selectCount(Wrappers.lambdaQuery(DocEntity.class)
                .between(DocEntity::getCreateTime, startDate, endDate));
    }

    default long countByCreatorId(String creatorId) {
        return selectCount(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getCreatorId, creatorId));
    }

    @Select("SELECT doc_type, SUM(total_amount) as total_amount " +
            "FROM doc_entity " +
            "WHERE status = #{status} " +
            "GROUP BY doc_type")
    @ResultType(Map.class)
    List<Map<String, Object>> sumAmountByDocType(@Param("status") Integer status);

    @Select("SELECT status, COUNT(*) as count " +
            "FROM doc_entity " +
            "GROUP BY status")
    @ResultType(Map.class)
    List<Map<String, Object>> countByStatusGroup();

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
            "FROM doc_entity " +
            "WHERE create_time >= #{startDate} AND create_time <= #{endDate} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    @ResultType(Map.class)
    List<Map<String, Object>> countByDateGroup(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    default List<DocEntity> selectRecent(int limit) {
        Page<DocEntity> page = new Page<>(1, limit);
        return selectPage(page, Wrappers.lambdaQuery(DocEntity.class)
                .orderByDesc(DocEntity::getCreateTime)).getRecords();
    }

    default List<DocEntity> selectRecentPending(int limit) {
        Page<DocEntity> page = new Page<>(1, limit);
        return selectPage(page, Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.SUBMITTED.getValue())
                .orderByAsc(DocEntity::getCreateTime)).getRecords();
    }

    default List<DocEntity> selectExpired(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        return selectList(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getStatus, DocStatus.SUBMITTED.getValue())
                .lt(DocEntity::getCreateTime, expireTime)
                .orderByAsc(DocEntity::getCreateTime));
    }

    default List<DocEntity> selectByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        LambdaQueryWrapper<DocEntity> query = Wrappers.lambdaQuery(DocEntity.class);
        if (minAmount != null) {
            query.ge(DocEntity::getTotalAmount, minAmount);
        }
        if (maxAmount != null) {
            query.le(DocEntity::getTotalAmount, maxAmount);
        }
        query.orderByDesc(DocEntity::getTotalAmount);
        return selectList(query);
    }

    default boolean existsByDocNo(String docNo) {
        return selectCount(Wrappers.lambdaQuery(DocEntity.class)
                .eq(DocEntity::getDocNo, docNo)) > 0;
    }

    default Long selectMaxId() {
        DocEntity entity = selectOne(Wrappers.lambdaQuery(DocEntity.class)
                .orderByDesc(DocEntity::getId)
                .last("LIMIT 1"));
        return entity != null ? entity.getId() : 0L;
    }
}