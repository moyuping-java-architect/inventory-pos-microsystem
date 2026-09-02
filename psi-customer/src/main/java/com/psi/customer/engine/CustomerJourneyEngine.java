package com.psi.customer.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psi.common.event.BusinessEvent;
import com.psi.customer.dto.RuleCondition;
import com.psi.customer.entity.CustomerJourneyStageEntity;
import com.psi.customer.entity.CustomerJourneyStateEntity;
import com.psi.customer.entity.CustomerJourneyTemplateEntity;
import com.psi.customer.entity.CustomerMetricsEntity;
import com.psi.customer.entity.CustomerTouchpointEntity;
import com.psi.customer.entity.JourneyRuleFireLogEntity;
import com.psi.customer.entity.TouchpointGenerateRuleEntity;
import com.psi.customer.mapper.CustomerJourneyStageMapper;
import com.psi.customer.mapper.CustomerJourneyStateMapper;
import com.psi.customer.mapper.CustomerJourneyTemplateMapper;
import com.psi.customer.mapper.CustomerTouchpointMapper;
import com.psi.customer.mapper.JourneyRuleFireLogMapper;
import com.psi.customer.mapper.TouchpointGenerateRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用客户旅程引擎 —— 整套零代码方案的执行内核。
 * <p>
 * 链路四层，每层职责单一：
 * <pre>
 *   业务事件  →  触点生成规则  →  客户触点（事实层）  →  旅程阶段匹配  →  旅程状态（结论层）
 * </pre>
 * <p>
 * 为什么中间非要有「触点」这一层：如果事件直接推状态，老板改一次阶段配置，
 * 历史数据就全废了——事件早就消费完不可能重放。触点落库之后，
 * 改配置只要拿触点重算一遍就行，这是这套设计真正值钱的地方。
 * <p>
 * 三条硬约束：
 * <ul>
 *   <li><b>一客一旅程一条</b>：customer_journey_state 靠 uk_subject_journey 唯一索引兜底</li>
 *   <li><b>只进不退</b>：新阶段 sortOrder 必须更大，除非目标阶段声明了 allowRollback</li>
 *   <li><b>幂等</b>：journey_rule_fire_log 唯一索引挡住重复消费和 onceOnly 规则的二次触发</li>
 * </ul>
 * 业务模块对这三条一无所知，它们只管往总线上扔 {@link BusinessEvent}。
 *
 * @author PSI
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerJourneyEngine {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 占位符 {fieldName} */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");

    private final TouchpointGenerateRuleMapper ruleMapper;
    private final CustomerTouchpointMapper touchpointMapper;
    private final JourneyRuleFireLogMapper fireLogMapper;
    private final CustomerJourneyTemplateMapper templateMapper;
    private final CustomerJourneyStageMapper stageMapper;
    private final CustomerJourneyStateMapper stateMapper;
    private final CustomerMetricsRefresher metricsRefresher;
    private final RuleConditionEvaluator conditionEvaluator;

    // ==================================================================
    // 主入口
    // ==================================================================

    /**
     * 处理一条业务事件。
     * <p>
     * 全程不抛异常：客户旅程是旁路能力，再怎么出问题也不能把主业务流程带崩。
     *
     * @param event 业务事件
     */
    public void handle(BusinessEvent event) {
        handle(event, null, true);
    }

    /**
     * 处理一条业务事件，可指定指标口径。
     *
     * @param event         业务事件
     * @param asOf          指标截止时间，null 表示按当前值算
     * @param persistMetric 是否把指标落库
     */
    public void handle(BusinessEvent event, String asOf, boolean persistMetric) {
        if (event == null || event.getEventCode() == null || event.getSubjectId() == null) {
            return;
        }

        String subjectType = event.getSubjectType() == null
                ? BusinessEvent.SUBJECT_CUSTOMER : event.getSubjectType();

        try {
            // 1. 刷新指标：规则要拿累计消费、订单数这些值做判断
            CustomerMetricsEntity metrics = metricsRefresher.refresh(
                    subjectType, event.getSubjectId(), event.getTenantId(), asOf, persistMetric);

            // 2. 组装求值上下文 = 指标 + 事件载荷 + 事件本身的元信息
            Map<String, Object> context = buildContext(event, metrics);

            // 3. 按规则生成触点
            List<CustomerTouchpointEntity> touchpoints = generateTouchpoints(event, subjectType, context);
            if (touchpoints.isEmpty()) {
                log.debug("旅程引擎: 无规则命中, eventCode={}, subjectId={}",
                        event.getEventCode(), event.getSubjectId());
                return;
            }

            // 4. 每条新触点都去各套旅程里试着推进一格
            for (CustomerTouchpointEntity touchpoint : touchpoints) {
                advanceJourneys(subjectType, event.getSubjectId(), event.getTenantId(),
                        touchpoint, event.getEventCode());
            }
        } catch (Exception e) {
            log.error("旅程引擎处理异常: eventCode={}, subjectId={}, error={}",
                    event.getEventCode(), event.getSubjectId(), e.getMessage(), e);
        }
    }

    // ==================================================================
    // 第一层：事件 → 触点
    // ==================================================================

    /**
     * 按规则把事件翻译成触点。命中几条规则就生成几个触点。
     */
    private List<CustomerTouchpointEntity> generateTouchpoints(BusinessEvent event,
                                                               String subjectType,
                                                               Map<String, Object> context) {
        List<CustomerTouchpointEntity> created = new ArrayList<>();

        List<TouchpointGenerateRuleEntity> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<TouchpointGenerateRuleEntity>()
                        .eq(TouchpointGenerateRuleEntity::getEventCode, event.getEventCode())
                        .eq(TouchpointGenerateRuleEntity::getEnabled, 1)
                        .eq(TouchpointGenerateRuleEntity::getDelFlag, 0)
                        // 出厂预置规则 tenant_id 为空，对所有租户生效；
                        // 租户自建的规则只对自己生效
                        .and(w -> w.isNull(TouchpointGenerateRuleEntity::getTenantId)
                                .or().eq(TouchpointGenerateRuleEntity::getTenantId, event.getTenantId()))
                        .orderByAsc(TouchpointGenerateRuleEntity::getSortOrder));

        for (TouchpointGenerateRuleEntity rule : rules) {
            List<RuleCondition> conditions = conditionEvaluator.parse(rule.getConditionJson());
            if (!conditionEvaluator.matches(conditions, context)) {
                continue;
            }

            CustomerTouchpointEntity touchpoint = fireRule(rule, event, subjectType, context);
            if (touchpoint != null) {
                created.add(touchpoint);
            }
        }
        return created;
    }

    /**
     * 触发一条规则：先抢幂等锁，再写触点。
     * <p>
     * 顺序不能反。先写触点再判重，并发下会留下孤儿触点；
     * 先抢锁则唯一索引直接把重复请求挡在门外，失败的那次什么都没落地。
     *
     * @return 生成的触点，被幂等拦下返回 null
     */
    private CustomerTouchpointEntity fireRule(TouchpointGenerateRuleEntity rule,
                                              BusinessEvent event,
                                              String subjectType,
                                              Map<String, Object> context) {
        boolean onceOnly = rule.getOnceOnly() != null && rule.getOnceOnly() == 1;
        String bizKey = resolveBizKey(rule, event, onceOnly);

        JourneyRuleFireLogEntity fireLog = new JourneyRuleFireLogEntity();
        fireLog.setTenantId(event.getTenantId());
        fireLog.setRuleId(rule.getId());
        fireLog.setSubjectType(subjectType);
        fireLog.setSubjectId(event.getSubjectId());
        fireLog.setBizKey(bizKey);
        fireLog.setFireTime(now());

        try {
            fireLogMapper.insert(fireLog);
        } catch (DuplicateKeyException e) {
            log.debug("旅程引擎: 规则已触发过, 跳过. rule={}, subjectId={}, bizKey={}",
                    rule.getRuleName(), event.getSubjectId(), bizKey);
            return null;
        }

        try {
            CustomerTouchpointEntity touchpoint = buildTouchpoint(rule, event, subjectType, context);
            touchpointMapper.insert(touchpoint);

            fireLog.setTouchpointId(touchpoint.getId() == null ? null : touchpoint.getId().longValue());
            fireLogMapper.updateById(fireLog);

            log.info("旅程引擎: 触点已生成. rule={}, subjectType={}, subjectId={}, touchpoint={}/{}",
                    rule.getRuleName(), subjectType, event.getSubjectId(),
                    touchpoint.getTouchpointType(), touchpoint.getIntent());
            return touchpoint;
        } catch (Exception e) {
            // 触点没写成，把幂等锁退回去，否则这条规则以后永远不会再触发
            fireLogMapper.deleteById(fireLog.getId());
            log.error("旅程引擎: 触点写入失败, 已回滚幂等记录. rule={}, error={}",
                    rule.getRuleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 决定幂等键。
     * <ul>
     *   <li>onceOnly 规则：固定 ONCE，同一客户终身一次</li>
     *   <li>普通规则：单据号，防同一单据重复消费</li>
     *   <li>连单据号都没有：给个随机值，等于不做幂等（宁可多记也不能漏记）</li>
     * </ul>
     */
    private String resolveBizKey(TouchpointGenerateRuleEntity rule, BusinessEvent event, boolean onceOnly) {
        if (onceOnly) {
            return JourneyRuleFireLogEntity.ONCE_KEY;
        }
        if (event.getBizKey() != null && !event.getBizKey().isBlank()) {
            return event.getBizKey();
        }
        return event.getEventCode() + ":" + UUID.randomUUID().toString().substring(0, 12);
    }

    /**
     * 按规则模板拼出触点记录。
     */
    private CustomerTouchpointEntity buildTouchpoint(TouchpointGenerateRuleEntity rule,
                                                     BusinessEvent event,
                                                     String subjectType,
                                                     Map<String, Object> context) {
        CustomerTouchpointEntity touchpoint = new CustomerTouchpointEntity();
        touchpoint.setTenantId(event.getTenantId());

        if (BusinessEvent.SUBJECT_MEMBER.equals(subjectType)) {
            touchpoint.setMemberId(event.getSubjectId());
        } else {
            touchpoint.setCustomerId(event.getSubjectId());
        }

        touchpoint.setTouchpointType(rule.getTouchpointType());
        // 记住是谁生成的：后面推进阶段时要靠它拿到老板配的 stage_code
        touchpoint.setSourceRuleId(rule.getId());
        touchpoint.setChannel(rule.getChannel() == null ? "SYSTEM" : rule.getChannel());
        touchpoint.setIntent(render(rule.getIntent(), context));
        touchpoint.setSummary(render(rule.getSummaryTemplate(), context));
        touchpoint.setContactTime(event.getOccurTime() == null ? now() : event.getOccurTime());
        touchpoint.setFollowUpDone(0);
        touchpoint.setOperator(event.getOperator() == null ? "SYSTEM_AUTO" : event.getOperator());
        touchpoint.setCreateTime(now());
        touchpoint.setUpdateTime(now());
        touchpoint.setDelFlag(0);
        return touchpoint;
    }

    // ==================================================================
    // 第二层：触点 → 旅程阶段
    // ==================================================================

    /**
     * 拿一条触点去所有匹配主体类型的旅程里试着推进。
     * <p>
     * 同一条触点可以同时推进多套旅程 —— 一次成交既是销售漏斗的「签约」，
     * 也可能是会员体系的「首购」，两套状态各记各的。
     */
    public void advanceJourneys(String subjectType, Long subjectId, String tenantId,
                                CustomerTouchpointEntity touchpoint, String eventCode) {
        List<CustomerJourneyTemplateEntity> templates = templateMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyTemplateEntity>()
                        .eq(CustomerJourneyTemplateEntity::getSubjectType, subjectType)
                        .eq(CustomerJourneyTemplateEntity::getEnabled, 1)
                        .eq(CustomerJourneyTemplateEntity::getDelFlag, 0)
                        .and(w -> w.isNull(CustomerJourneyTemplateEntity::getTenantId)
                                .or().eq(CustomerJourneyTemplateEntity::getTenantId, tenantId)));

        for (CustomerJourneyTemplateEntity template : templates) {
            CustomerJourneyStageEntity stage = matchStage(template.getJourneyCode(), touchpoint, tenantId);
            if (stage == null) {
                continue;
            }
            upsertState(template.getJourneyCode(), subjectType, subjectId, tenantId,
                    stage, touchpoint, eventCode);
        }
    }

    /**
     * 在一套旅程里找这条触点对应的阶段。
     * <p>
     * 两条通路，优先级从高到低：
     * <ol>
     *   <li><b>规则直连</b>：触点由哪条规则生成是记了的，规则上如果配了 stageCode，
     *       直接按编码取阶段。这是老板在配置页上「业务事件 → 旅程阶段」那一步的产物，
     *       他选了就一定生效，不经过任何字符串约定。</li>
     *   <li><b>触点类型匹配</b>：人工录入的触点没有来源规则，
     *       老规则也可能还没配 stageCode，这时退回按 matchTouchpoint 比对。</li>
     * </ol>
     * 之所以要有第一条，是踩过坑：事件码、触点码、阶段匹配码历史上是三套各写各的，
     * 阶段里写 CONTRACT_SIGNED、库里躺着 CONTRACT，旅程就一声不响地永远不动，
     * 页面上还看不出任何报错。直连之后，配置即生效，编码不一致最多影响回退通路。
     * <p>
     * matchIntent 为空表示不限意图。回退通路上多个阶段同时命中时取最靠后的那个 ——
     * 客户既然已经走到更后面的阶段，就不该被更早的阶段定义盖住。
     */
    private CustomerJourneyStageEntity matchStage(String journeyCode, CustomerTouchpointEntity touchpoint,
                                                  String tenantId) {
        // 通路一：规则直连
        CustomerJourneyStageEntity linked = matchStageByRule(journeyCode, touchpoint, tenantId);
        if (linked != null) {
            return linked;
        }

        // 通路二：按触点类型匹配
        List<CustomerJourneyStageEntity> stages = stageMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getJourneyCode, journeyCode)
                        .eq(CustomerJourneyStageEntity::getMatchTouchpoint, touchpoint.getTouchpointType())
                        .eq(CustomerJourneyStageEntity::getDelFlag, 0)
                        .and(w -> w.isNull(CustomerJourneyStageEntity::getTenantId)
                                .or().eq(CustomerJourneyStageEntity::getTenantId, tenantId)));

        return stages.stream()
                .filter(s -> intentMatches(s.getMatchIntent(), touchpoint.getIntent()))
                .max(Comparator.comparingInt(s -> s.getSortOrder() == null ? 0 : s.getSortOrder()))
                .orElse(null);
    }

    /**
     * 顺着触点的来源规则，取老板配好的目标阶段。
     * <p>
     * 规则上的 stageCode 属于哪套旅程是确定的，所以本方法只在 journeyCode 对得上时返回，
     * 否则返回 null 让调用方走回退通路 —— 一条触点可以同时推进多套旅程，
     * 销售漏斗那条配了直连，会员体系那条照样按触点类型自己匹配，两者互不干扰。
     */
    private CustomerJourneyStageEntity matchStageByRule(String journeyCode,
                                                        CustomerTouchpointEntity touchpoint,
                                                        String tenantId) {
        if (touchpoint.getSourceRuleId() == null) {
            return null;
        }
        TouchpointGenerateRuleEntity rule = ruleMapper.selectById(touchpoint.getSourceRuleId());
        if (rule == null || rule.getStageCode() == null || rule.getStageCode().isBlank()) {
            return null;
        }
        return stageMapper.selectOne(new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                .eq(CustomerJourneyStageEntity::getJourneyCode, journeyCode)
                .eq(CustomerJourneyStageEntity::getStageCode, rule.getStageCode())
                .eq(CustomerJourneyStageEntity::getDelFlag, 0)
                .and(w -> w.isNull(CustomerJourneyStageEntity::getTenantId)
                        .or().eq(CustomerJourneyStageEntity::getTenantId, tenantId))
                .last("LIMIT 1"));
    }

    private boolean intentMatches(String required, String actual) {
        if (required == null || required.isBlank()) {
            return true;
        }
        return required.equalsIgnoreCase(actual);
    }

    /**
     * 写入或推进旅程状态。同一客户同一旅程只有一条记录。
     * <p>
     * 推进条件：新阶段更靠后，或目标阶段显式允许回退（流失、降级这类阶段要能往回走）。
     */
    private void upsertState(String journeyCode, String subjectType, Long subjectId, String tenantId,
                             CustomerJourneyStageEntity stage, CustomerTouchpointEntity touchpoint,
                             String eventCode) {
        int newOrder = stage.getSortOrder() == null ? 0 : stage.getSortOrder();
        Long touchpointId = touchpoint.getId() == null ? null : touchpoint.getId().longValue();

        CustomerJourneyStateEntity existing = findState(journeyCode, subjectType, subjectId);

        if (existing == null) {
            CustomerJourneyStateEntity state = new CustomerJourneyStateEntity();
            state.setTenantId(tenantId);
            state.setJourneyCode(journeyCode);
            state.setSubjectType(subjectType);
            state.setSubjectId(subjectId);
            state.setCurrentStageCode(stage.getStageCode());
            state.setCurrentStageOrder(newOrder);
            state.setEnterStageTime(touchpoint.getContactTime());
            state.setLastTouchpointId(touchpointId);
            state.setLastEventCode(eventCode);
            state.setAdvanceCount(1);
            state.setCreateTime(now());
            state.setUpdateTime(now());

            try {
                stateMapper.insert(state);
                log.info("旅程引擎: 进入旅程. journey={}, subject={}/{}, stage={}",
                        journeyCode, subjectType, subjectId, stage.getStageCode());
                return;
            } catch (DuplicateKeyException e) {
                // 并发下别的线程刚插进去，退回去走更新分支
                existing = findState(journeyCode, subjectType, subjectId);
                if (existing == null) {
                    return;
                }
            }
        }

        int currentOrder = existing.getCurrentStageOrder() == null ? 0 : existing.getCurrentStageOrder();
        boolean allowRollback = stage.getAllowRollback() != null && stage.getAllowRollback() == 1;

        if (newOrder <= currentOrder && !allowRollback) {
            log.debug("旅程引擎: 阶段未前进, 保持原状. journey={}, subject={}/{}, current={}, incoming={}",
                    journeyCode, subjectType, subjectId, existing.getCurrentStageCode(), stage.getStageCode());
            return;
        }

        existing.setCurrentStageCode(stage.getStageCode());
        existing.setCurrentStageOrder(newOrder);
        existing.setEnterStageTime(touchpoint.getContactTime());
        existing.setLastTouchpointId(touchpointId);
        existing.setLastEventCode(eventCode);
        existing.setAdvanceCount((existing.getAdvanceCount() == null ? 0 : existing.getAdvanceCount()) + 1);
        existing.setUpdateTime(now());
        stateMapper.updateById(existing);

        log.info("旅程引擎: 阶段推进. journey={}, subject={}/{}, stage={} (order {} -> {})",
                journeyCode, subjectType, subjectId, stage.getStageCode(), currentOrder, newOrder);
    }

    private CustomerJourneyStateEntity findState(String journeyCode, String subjectType, Long subjectId) {
        return stateMapper.selectOne(new LambdaQueryWrapper<CustomerJourneyStateEntity>()
                .eq(CustomerJourneyStateEntity::getJourneyCode, journeyCode)
                .eq(CustomerJourneyStateEntity::getSubjectType, subjectType)
                .eq(CustomerJourneyStateEntity::getSubjectId, subjectId)
                .last("LIMIT 1"));
    }

    // ==================================================================
    // 上下文与模板
    // ==================================================================

    /**
     * 组装求值上下文。
     * <p>
     * 载荷放在指标之后，同名时以载荷为准：
     * 事件里的 orderAmount 说的是这一单，指标里的 totalAmount 说的是累计，
     * 万一撞名了，「这一单」的语义更贴近用户配规则时的直觉。
     */
    private Map<String, Object> buildContext(BusinessEvent event, CustomerMetricsEntity metrics) {
        Map<String, Object> context = new HashMap<>(metricsRefresher.toContext(metrics));
        context.put("eventCode", event.getEventCode());
        context.put("subjectType", event.getSubjectType());
        context.put("subjectId", event.getSubjectId());
        context.put("bizKey", event.getBizKey());
        context.put("occurTime", event.getOccurTime());
        if (event.getPayload() != null) {
            context.putAll(event.getPayload());
        }
        return context;
    }

    /**
     * 渲染 {field} 占位符。取不到值的占位符原样保留，方便老板一眼看出字段名写错了。
     */
    private String render(String template, Map<String, Object> context) {
        if (template == null || template.isBlank()) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            Object value = context.get(matcher.group(1));
            String replacement = value == null ? matcher.group(0) : String.valueOf(value);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String now() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
}
