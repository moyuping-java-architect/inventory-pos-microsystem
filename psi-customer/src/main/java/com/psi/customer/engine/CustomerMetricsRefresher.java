package com.psi.customer.engine;

import com.psi.common.event.BusinessEvent;
import com.psi.customer.dto.SubjectMetricsRow;
import com.psi.customer.entity.CustomerMetricsEntity;
import com.psi.customer.mapper.CustomerMetricsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户指标刷新器。
 * <p>
 * 业务事件到达时先把该主体的指标算一遍，规则条件才有东西可比。
 * 指标是「现算 + 落库」双份：现算的那份立刻用于本次规则求值，
 * 落库的那份供看板查询和批量分析，避免每次开看板都全表聚合。
 * <p>
 * 数据源按主体类型分流：
 * <ul>
 *   <li>CUSTOMER：sale_order_main 实时聚合，口径与看板一致</li>
 *   <li>MEMBER：member_info 上的累计字段（收银结算时回写）</li>
 * </ul>
 *
 * @author PSI
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerMetricsRefresher {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CustomerMetricsMapper metricsMapper;

    /**
     * 刷新并落库某主体的当前指标。
     *
     * @param subjectType 主体类型
     * @param subjectId   主体ID
     * @param tenantId    租户ID
     * @return 刷新后的指标实体
     */
    public CustomerMetricsEntity refresh(String subjectType, Long subjectId, String tenantId) {
        return refresh(subjectType, subjectId, tenantId, null, true);
    }

    /**
     * 计算指标。
     *
     * @param subjectType 主体类型
     * @param subjectId   主体ID
     * @param tenantId    租户ID
     * @param asOf        截止时间，null 表示算到当前；存量回溯时传订单时间
     * @param persist     是否落库。回溯历史订单时传 false，避免把中间状态写进指标表
     * @return 指标实体
     */
    public CustomerMetricsEntity refresh(String subjectType, Long subjectId, String tenantId,
                                         String asOf, boolean persist) {
        CustomerMetricsEntity metrics = new CustomerMetricsEntity();
        metrics.setTenantId(tenantId);
        metrics.setSubjectType(subjectType);
        metrics.setSubjectId(subjectId);

        try {
            if (BusinessEvent.SUBJECT_MEMBER.equals(subjectType)) {
                applyRow(metrics, metricsMapper.selectMemberMetrics(subjectId));
                applyTouchpointStat(metrics, metricsMapper.selectMemberTouchpointStat(subjectId));
            } else {
                SubjectMetricsRow orderRow = metricsMapper.selectCustomerOrderMetrics(subjectId, asOf);
                applyRow(metrics, orderRow);
                metrics.setLastOrderAmount(
                        metricsMapper.selectCustomerLastOrderAmount(subjectId, asOf));
                applyTouchpointStat(metrics, metricsMapper.selectCustomerTouchpointStat(subjectId));
            }
        } catch (Exception e) {
            log.warn("指标聚合失败, 按空指标继续: subjectType={}, subjectId={}, error={}",
                    subjectType, subjectId, e.getMessage());
        }

        fillDefaults(metrics);

        if (persist) {
            persist(metrics);
        }
        return metrics;
    }

    /**
     * 把指标实体摊平成规则求值上下文（字段名 = 实体属性名）。
     */
    public Map<String, Object> toContext(CustomerMetricsEntity m) {
        Map<String, Object> ctx = new HashMap<>();
        if (m == null) {
            return ctx;
        }
        ctx.put("totalAmount", m.getTotalAmount());
        ctx.put("orderCount", m.getOrderCount());
        ctx.put("firstOrderTime", m.getFirstOrderTime());
        ctx.put("lastOrderTime", m.getLastOrderTime());
        ctx.put("avgOrderAmount", m.getAvgOrderAmount());
        ctx.put("maxOrderAmount", m.getMaxOrderAmount());
        ctx.put("lastOrderAmount", m.getLastOrderAmount());
        ctx.put("avgIntervalDays", m.getAvgIntervalDays());
        ctx.put("daysSinceLast", m.getDaysSinceLast());
        ctx.put("returnCount", m.getReturnCount());
        ctx.put("returnAmount", m.getReturnAmount());
        ctx.put("unpaidAmount", m.getUnpaidAmount());
        ctx.put("touchpointCount", m.getTouchpointCount());
        ctx.put("lastTouchpointTime", m.getLastTouchpointTime());
        ctx.put("memberLevel", m.getMemberLevel());
        ctx.put("points", m.getPoints());
        return ctx;
    }

    // ========== 内部方法 ==========

    /**
     * 落库：存在则更新，不存在则插入。
     * uk_subject 唯一索引兜底并发插入。
     */
    private void persist(CustomerMetricsEntity metrics) {
        try {
            CustomerMetricsEntity exists = metricsMapper.selectBySubject(
                    metrics.getSubjectType(), metrics.getSubjectId());
            String now = LocalDateTime.now().format(TIME_FORMATTER);
            metrics.setUpdateTime(now);

            if (exists == null) {
                metrics.setCreateTime(now);
                metricsMapper.insert(metrics);
            } else {
                metrics.setId(exists.getId());
                metrics.setCreateTime(exists.getCreateTime());
                metricsMapper.updateById(metrics);
            }
        } catch (Exception e) {
            // 并发插入撞唯一索引：后到的那次改成更新即可，不影响本次求值
            log.warn("指标落库失败: subjectType={}, subjectId={}, error={}",
                    metrics.getSubjectType(), metrics.getSubjectId(), e.getMessage());
        }
    }

    /**
     * 搬运订单聚合结果。
     */
    private void applyRow(CustomerMetricsEntity target, SubjectMetricsRow row) {
        if (row == null) {
            return;
        }
        target.setTotalAmount(row.getTotalAmount());
        target.setOrderCount(row.getOrderCount());
        target.setFirstOrderTime(row.getFirstOrderTime());
        target.setLastOrderTime(row.getLastOrderTime());
        target.setAvgOrderAmount(row.getAvgOrderAmount());
        target.setMaxOrderAmount(row.getMaxOrderAmount());
        target.setAvgIntervalDays(row.getAvgIntervalDays());
        target.setDaysSinceLast(row.getDaysSinceLast());
        if (row.getMemberLevel() != null) {
            target.setMemberLevel(row.getMemberLevel());
        }
        if (row.getPoints() != null) {
            target.setPoints(row.getPoints());
        }
    }

    /**
     * 搬运触点统计。
     */
    private void applyTouchpointStat(CustomerMetricsEntity target, SubjectMetricsRow row) {
        if (row == null) {
            return;
        }
        target.setTouchpointCount(row.getTouchpointCount());
        target.setLastTouchpointTime(row.getLastTouchpointTime());
    }

    /**
     * 补默认值。
     * <p>
     * 数值指标一律补 0 而不是留 null：规则求值时字段缺失会判定条件不满足，
     * 新客户「累计消费 0」和「查不到累计消费」是两码事，前者应该能参与比较。
     */
    private void fillDefaults(CustomerMetricsEntity m) {
        m.setTotalAmount(nvl(m.getTotalAmount()));
        m.setAvgOrderAmount(nvl(m.getAvgOrderAmount()));
        m.setMaxOrderAmount(nvl(m.getMaxOrderAmount()));
        m.setLastOrderAmount(nvl(m.getLastOrderAmount()));
        m.setReturnAmount(nvl(m.getReturnAmount()));
        m.setUnpaidAmount(nvl(m.getUnpaidAmount()));
        m.setOrderCount(nvl(m.getOrderCount()));
        m.setReturnCount(nvl(m.getReturnCount()));
        m.setTouchpointCount(nvl(m.getTouchpointCount()));
        m.setPoints(nvl(m.getPoints()));
        if (m.getDaysSinceLast() == null) {
            m.setDaysSinceLast(0);
        }
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private Integer nvl(Integer v) {
        return v == null ? 0 : v;
    }
}
