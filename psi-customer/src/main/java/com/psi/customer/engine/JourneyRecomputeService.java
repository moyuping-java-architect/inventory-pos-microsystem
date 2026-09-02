package com.psi.customer.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psi.common.event.BusinessEvent;
import com.psi.customer.dto.SaleOrderEventRow;
import com.psi.customer.entity.CustomerJourneyStateEntity;
import com.psi.customer.entity.CustomerTouchpointEntity;
import com.psi.customer.mapper.CustomerJourneyStateMapper;
import com.psi.customer.mapper.CustomerMetricsMapper;
import com.psi.customer.mapper.CustomerTouchpointMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 旅程批量重算服务。
 * <p>
 * 有了触点这层事实数据，配置就可以随便改 —— 改完跑一次重算，
 * 历史客户全部按新规则重新落位。这是「触点层可回溯」这个设计的兑现处，
 * 也是老板敢在页面上折腾配置的底气。
 * <p>
 * 三个入口对应三种场景：
 * <ul>
 *   <li>{@link #rebuildJourneyStates()}：改了阶段/旅程配置 → 拿现有触点重放</li>
 *   <li>{@link #backfillFromSaleOrders()}：系统刚上线 → 把历史订单补成触点</li>
 *   <li>{@link #refreshAllMetrics()}：指标口径调整 → 全量重算指标</li>
 * </ul>
 * 三个入口都是幂等的，重复跑不会产生重复数据。
 *
 * @author PSI
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JourneyRecomputeService {

    private final CustomerTouchpointMapper touchpointMapper;
    private final CustomerJourneyStateMapper stateMapper;
    private final CustomerMetricsMapper metricsMapper;
    private final CustomerJourneyEngine journeyEngine;
    private final CustomerMetricsRefresher metricsRefresher;
    private final JourneyLifecycleScanner lifecycleScanner;

    /**
     * 按现有触点重算全部旅程状态。
     * <p>
     * 先清空状态表再按时间正序回放。之所以要清空：阶段配置可能被改小或删掉，
     * 不清就会留下已经不存在的阶段编码，看板上显示一片空白。
     * <p>
     * 触点本身一条不动，所以这个操作是安全可重复的。
     *
     * @return 处理统计
     */
    public Map<String, Object> rebuildJourneyStates() {
        long start = System.currentTimeMillis();

        int cleared = stateMapper.delete(new LambdaQueryWrapper<CustomerJourneyStateEntity>()
                .gt(CustomerJourneyStateEntity::getId, 0));

        int customerCount = replaySubjects(
                touchpointMapper.selectDistinctCustomerIds(), BusinessEvent.SUBJECT_CUSTOMER);
        int memberCount = replaySubjects(
                touchpointMapper.selectDistinctMemberIds(), BusinessEvent.SUBJECT_MEMBER);

        Map<String, Object> result = new HashMap<>();
        result.put("clearedStates", cleared);
        result.put("customers", customerCount);
        result.put("members", memberCount);
        result.put("costMs", System.currentTimeMillis() - start);

        log.info("旅程状态重算完成: 清空{}条, 客户{}个, 会员{}个, 耗时{}ms",
                cleared, customerCount, memberCount, result.get("costMs"));
        return result;
    }

    /**
     * 从历史销售订单回溯生成触点与旅程状态。
     * <p>
     * 每条历史订单都按「下单当时」的指标口径求值，而不是拿今天的累计值去判当年的单子。
     * 否则一个老客户的第一单就会被判成「累计满5000升银卡」，回溯出来的旅程全是错的。
     * <p>
     * 幂等由 journey_rule_fire_log 的单据号保证，重复执行不会重复生成触点。
     *
     * @return 处理统计
     */
    public Map<String, Object> backfillFromSaleOrders() {
        long start = System.currentTimeMillis();
        List<SaleOrderEventRow> orders = metricsMapper.selectApprovedSaleOrders();

        int processed = 0;
        for (SaleOrderEventRow order : orders) {
            if (order.getCustomerId() == null) {
                continue;
            }
            BusinessEvent event = BusinessEvent.of(
                            "SALE.ORDER_APPROVED", BusinessEvent.SUBJECT_CUSTOMER, order.getCustomerId())
                    .bizKey(order.getDocNo())
                    .put("docNo", order.getDocNo())
                    .put("orderAmount", order.getOrderAmount())
                    .put("currency", order.getCurrency());
            event.setOccurTime(order.getOccurTime());
            event.setOperator("SYSTEM_BACKFILL");

            // asOf = 订单时间，persist = false：回溯过程中不要把中间态指标写进表
            journeyEngine.handle(event, order.getOccurTime(), false);
            processed++;
        }

        // 回溯完再把指标按当前口径刷一遍，保证指标表是最终值
        int refreshed = refreshCustomerMetrics();

        Map<String, Object> result = new HashMap<>();
        result.put("orders", processed);
        result.put("metricsRefreshed", refreshed);
        result.put("costMs", System.currentTimeMillis() - start);

        log.info("历史订单回溯完成: 订单{}笔, 指标刷新{}个, 耗时{}ms",
                processed, refreshed, result.get("costMs"));
        return result;
    }

    /**
     * 全量刷新指标表。
     *
     * @return 处理统计
     */
    public Map<String, Object> refreshAllMetrics() {
        long start = System.currentTimeMillis();

        int customers = refreshCustomerMetrics();
        int members = 0;
        String memberTenant = "1";
        for (Long memberId : metricsMapper.selectAllMemberIds()) {
            metricsRefresher.refresh(BusinessEvent.SUBJECT_MEMBER, memberId, memberTenant);
            members++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("customers", customers);
        result.put("members", members);

        // 指标刷新后紧接着跑一次生命周期扫描，让 LIFECYCLE 旅程状态与最新指标同步
        Map<String, Object> lifecycle = lifecycleScanner.scan();
        result.put("lifecycle", lifecycle);
        result.put("costMs", System.currentTimeMillis() - start);

        log.info("指标全量刷新完成: 客户{}个, 会员{}个, 生命周期扫描触发{}次, 耗时{}ms",
                customers, members, lifecycle.get("silentFired"), result.get("costMs"));
        return result;
    }

    // ========== 内部方法 ==========

    private int refreshCustomerMetrics() {
        int count = 0;
        for (Long customerId : metricsMapper.selectAllCustomerIds()) {
            metricsRefresher.refresh(BusinessEvent.SUBJECT_CUSTOMER, customerId, null);
            count++;
        }
        return count;
    }

    /**
     * 按主体回放触点，逐条推进旅程。
     */
    private int replaySubjects(List<Long> subjectIds, String subjectType) {
        int count = 0;
        for (Long subjectId : subjectIds) {
            if (subjectId == null) {
                continue;
            }
            List<CustomerTouchpointEntity> touchpoints =
                    BusinessEvent.SUBJECT_MEMBER.equals(subjectType)
                            ? touchpointMapper.selectByMemberIdAsc(subjectId)
                            : touchpointMapper.selectByCustomerIdAsc(subjectId);

            for (CustomerTouchpointEntity touchpoint : touchpoints) {
                journeyEngine.advanceJourneys(subjectType, subjectId,
                        touchpoint.getTenantId(), touchpoint, "REPLAY");
            }
            count++;
        }
        return count;
    }
}
