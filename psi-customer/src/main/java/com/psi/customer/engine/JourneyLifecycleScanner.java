package com.psi.customer.engine;

import com.psi.common.event.BusinessEvent;
import com.psi.customer.entity.CustomerMetricsEntity;
import com.psi.customer.engine.CustomerMetricsRefresher;
import com.psi.customer.mapper.CustomerMetricsMapper;
import com.psi.customer.service.CustomerJourneyConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户生命周期扫描器 —— 私域运营"动态分层"的执行入口。
 * <p>
 * 业务事件只会在"客户做了什么"时触发（消费、充值、回款），但流失恰恰发生在
 * "客户什么都没做"的时候。所以必须有一个定时/手动的扫描，把"距上次消费过久"的会员
 * 翻译成合成业务事件，喂给同一套旅程引擎：
 * <pre>
 *   扫描会员指标 → 判定流失等级 → 发 JOURNEY.MEMBER_SILENT / JOURNEY.MEMBER_CHURNED
 *     → 引擎生成 SILENT_WARNING / CHURN 触点 → LIFECYCLE 旅程推进 SILENT / CHURNED 阶段
 * </pre>
 * <p>
 * 阈值与 {@code CustomerJourneyServiceImpl.calculateChurnLevel} 完全对齐（全部读 CHURN_MODEL 配置），
 * 保证"看板实时算的流失等级"和"引擎沉淀的旅程阶段"用的是同一把尺子。
 * 会员回店消费时，真实 ORDER_COMPLETED 事件会把 LIFECYCLE 推进回 LC_ACTIVE
 * （该阶段 allow_rollback=1），天然实现"流失→唤醒"的动态新陈代谢。
 *
 * @author PSI
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JourneyLifecycleScanner {

    private final CustomerMetricsMapper metricsMapper;
    private final CustomerMetricsRefresher metricsRefresher;
    private final CustomerJourneyEngine journeyEngine;
    private final CustomerJourneyConfigService configService;

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 扫描全部会员，对沉默/流失会员发出合成事件。
     *
     * @return 处理统计
     */
    public Map<String, Object> scan() {
        long start = System.currentTimeMillis();

        // 直接按会员档案实时算指标（member_info.last_consume_time 即 Recency），
        // 不读 customer_metrics 表——避免落库指标因租户拦截器 tenant_id 不可见而被漏扫。
        List<Long> memberIds = metricsMapper.selectAllMemberIds();

        int scanned = 0, silent = 0, churned = 0;
        String ym = LocalDate.now().format(YM);
        String tenantId = "1";

        for (Long memberId : memberIds) {
            if (memberId == null) {
                continue;
            }
            CustomerMetricsEntity m = metricsRefresher.refresh(
                    BusinessEvent.SUBJECT_MEMBER, memberId, tenantId);
            if (m.getDaysSinceLast() == null || m.getDaysSinceLast() <= 0) {
                continue;
            }
            m.setSubjectId(memberId);
            scanned++;
            String level = evaluateLevel(m);

            // 沉默预警：处于 AT_RISK / HIGH_RISK / NEW_CHURNED（越过预警线但未彻底流失）
            if ("AT_RISK".equals(level) || "HIGH_RISK".equals(level) || "NEW_CHURNED".equals(level)) {
                BusinessEvent silentEvent = BusinessEvent.of(
                                "JOURNEY.MEMBER_SILENT", BusinessEvent.SUBJECT_MEMBER, m.getSubjectId())
                        .bizKey("SILENT:" + m.getSubjectId() + ":" + ym)
                        .put("daysSinceLast", m.getDaysSinceLast());
                silentEvent.setTenantId(m.getTenantId());
                silentEvent.setOperator("LIFECYCLE_SCANNER");
                journeyEngine.handle(silentEvent);
                silent++;
            }

            // 流失判定：CHURNED / NEW_CHURNED（新客首单后长期未回）
            if ("CHURNED".equals(level) || "NEW_CHURNED".equals(level)) {
                BusinessEvent churnedEvent = BusinessEvent.of(
                                "JOURNEY.MEMBER_CHURNED", BusinessEvent.SUBJECT_MEMBER, m.getSubjectId())
                        .bizKey("CHURNED:" + m.getSubjectId() + ":" + ym)
                        .put("daysSinceLast", m.getDaysSinceLast());
                churnedEvent.setTenantId(m.getTenantId());
                churnedEvent.setOperator("LIFECYCLE_SCANNER");
                journeyEngine.handle(churnedEvent);
                churned++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("scanned", scanned);
        result.put("silentFired", silent);
        result.put("churnedFired", churned);
        result.put("costMs", System.currentTimeMillis() - start);

        log.info("生命周期扫描完成: 扫描{}个会员, 触发沉默{}次, 触发流失{}次, 耗时{}ms",
                scanned, silent, churned, result.get("costMs"));
        return result;
    }

    /**
     * 流失等级判定（镜像 CustomerJourneyServiceImpl.calculateChurnLevel）。
     * 阈值全部来自 CHURN_MODEL 配置，零代码可配。
     */
    private String evaluateLevel(CustomerMetricsEntity m) {
        Integer daysSince = m.getDaysSinceLast();
        Integer totalOrders = m.getOrderCount();
        BigDecimal avgInterval = m.getAvgIntervalDays();
        if (daysSince == null) {
            return "UNKNOWN";
        }

        int newCustomerDays = configService.getInt("CHURN_MODEL", "new_customer_days", 30);
        if (totalOrders == null || totalOrders < 2) {
            return daysSince <= newCustomerDays ? "NEW" : "NEW_CHURNED";
        }

        int maxDays = configService.getInt("CHURN_MODEL", "max_days_churned", 90);
        if (daysSince > maxDays) {
            return "CHURNED";
        }

        if (avgInterval != null && avgInterval.compareTo(BigDecimal.ZERO) > 0) {
            double activeMult = configService.getDouble("CHURN_MODEL", "active_multiplier", 1.5);
            double atRiskMult = configService.getDouble("CHURN_MODEL", "at_risk_multiplier", 2.5);
            double highRiskMult = configService.getDouble("CHURN_MODEL", "high_risk_multiplier", 4.0);
            if (daysSince <= activeMult * avgInterval.doubleValue()) {
                return "ACTIVE";
            }
            if (daysSince <= atRiskMult * avgInterval.doubleValue()) {
                return "AT_RISK";
            }
            if (daysSince <= highRiskMult * avgInterval.doubleValue()) {
                return "HIGH_RISK";
            }
            return "CHURNED";
        }

        int fallbackActive = configService.getInt("CHURN_MODEL", "fallback_active_days", 30);
        int fallbackAtRisk = configService.getInt("CHURN_MODEL", "fallback_at_risk_days", 60);
        if (daysSince <= fallbackActive) {
            return "ACTIVE";
        }
        if (daysSince <= fallbackAtRisk) {
            return "AT_RISK";
        }
        return "HIGH_RISK";
    }
}
