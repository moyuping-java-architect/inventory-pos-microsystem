package com.psi.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psi.common.result.CommonResult;
import com.psi.customer.engine.JourneyLifecycleScanner;
import com.psi.customer.engine.JourneyRecomputeService;
import com.psi.customer.dto.EventStageLinkReq;
import com.psi.customer.dto.EventStageMappingDTO;
import com.psi.customer.entity.BusinessEventDictEntity;
import com.psi.customer.entity.CustomerJourneyStageEntity;
import com.psi.customer.entity.CustomerJourneyStateEntity;
import com.psi.customer.entity.CustomerJourneyTemplateEntity;
import com.psi.customer.entity.JourneyDictEntity;
import com.psi.customer.entity.TouchpointGenerateRuleEntity;
import com.psi.customer.mapper.BusinessEventDictMapper;
import com.psi.customer.mapper.CustomerJourneyStageMapper;
import com.psi.customer.mapper.CustomerJourneyStateMapper;
import com.psi.customer.mapper.CustomerJourneyTemplateMapper;
import com.psi.customer.mapper.JourneyDictMapper;
import com.psi.customer.mapper.TouchpointGenerateRuleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户旅程引擎配置接口。
 * <p>
 * 页面上能配的东西全在这里：业务事件选什么、条件怎么写、生成什么触点、
 * 旅程分几个阶段、阶段怎么匹配。所有下拉选项都来自字典表，
 * 前端不写死任何枚举，加一种触点类型只需要往字典表插一行。
 *
 * @author PSI
 */
@RestController
@RequestMapping("/psi/customer/journey/engine")
@RequiredArgsConstructor
@Tag(name = "客户旅程引擎", description = "零代码配置：业务事件 → 触点规则 → 旅程阶段 → 客户状态")
public class JourneyEngineController {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 条件构建器可选字段。
     * <p>
     * 与 customer_metrics 的列一一对应。做成常量而不是查表：
     * 这些字段背后是实打实的 SQL 聚合逻辑，加一个就得改代码，
     * 假装它可配置只会让老板配出一个永远不命中的规则。
     */
    private static final Map<String, String> METRIC_FIELDS = new LinkedHashMap<>();

    static {
        METRIC_FIELDS.put("totalAmount", "累计消费金额");
        METRIC_FIELDS.put("orderCount", "累计订单数");
        METRIC_FIELDS.put("avgOrderAmount", "平均客单价");
        METRIC_FIELDS.put("maxOrderAmount", "最大单笔金额");
        METRIC_FIELDS.put("lastOrderAmount", "最近一单金额");
        METRIC_FIELDS.put("avgIntervalDays", "平均购买间隔天数");
        METRIC_FIELDS.put("daysSinceLast", "距上次消费天数");
        METRIC_FIELDS.put("returnCount", "退货次数");
        METRIC_FIELDS.put("returnAmount", "累计退货金额");
        METRIC_FIELDS.put("unpaidAmount", "当前欠款金额");
        METRIC_FIELDS.put("touchpointCount", "累计触点数");
        METRIC_FIELDS.put("memberLevel", "会员等级");
        METRIC_FIELDS.put("points", "当前积分");
    }

    /** 条件运算符 */
    private static final Map<String, String> OPERATORS = new LinkedHashMap<>();

    static {
        OPERATORS.put("GE", "大于等于");
        OPERATORS.put("GT", "大于");
        OPERATORS.put("LE", "小于等于");
        OPERATORS.put("LT", "小于");
        OPERATORS.put("EQ", "等于");
        OPERATORS.put("NE", "不等于");
        OPERATORS.put("IN", "属于（逗号分隔）");
        OPERATORS.put("CONTAINS", "包含");
    }

    private final BusinessEventDictMapper eventDictMapper;
    private final JourneyDictMapper journeyDictMapper;
    private final TouchpointGenerateRuleMapper ruleMapper;
    private final CustomerJourneyTemplateMapper templateMapper;
    private final CustomerJourneyStageMapper stageMapper;
    private final CustomerJourneyStateMapper stateMapper;
    private final JourneyRecomputeService recomputeService;
    private final JourneyLifecycleScanner lifecycleScanner;

    // ==================================================================
    // 配置页下拉数据源
    // ==================================================================

    /**
     * 规则配置页需要的全部下拉选项，一次返回，省掉前端串行请求。
     */
    @GetMapping("/options")
    @Operation(summary = "配置页下拉选项（事件/触点类型/意图/渠道/字段/运算符）")
    public CommonResult<Map<String, Object>> options() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("events", eventDictMapper.selectList(
                new LambdaQueryWrapper<BusinessEventDictEntity>()
                        .orderByAsc(BusinessEventDictEntity::getId)));
        data.put("touchpointTypes", dictOf(JourneyDictEntity.TYPE_TOUCHPOINT));
        data.put("intents", dictOf(JourneyDictEntity.TYPE_INTENT));
        data.put("channels", dictOf(JourneyDictEntity.TYPE_CHANNEL));
        data.put("metricFields", toOptionList(METRIC_FIELDS));
        data.put("operators", toOptionList(OPERATORS));
        return CommonResult.success(data);
    }

    private List<JourneyDictEntity> dictOf(String dictType) {
        return journeyDictMapper.selectList(new LambdaQueryWrapper<JourneyDictEntity>()
                .eq(JourneyDictEntity::getDictType, dictType)
                .eq(JourneyDictEntity::getEnabled, 1)
                .eq(JourneyDictEntity::getDelFlag, 0)
                .orderByAsc(JourneyDictEntity::getSortOrder));
    }

    private List<Map<String, String>> toOptionList(Map<String, String> source) {
        List<Map<String, String>> list = new ArrayList<>();
        source.forEach((k, v) -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("value", k);
            item.put("label", v);
            list.add(item);
        });
        return list;
    }

    // ==================================================================
    // 触点生成规则
    // ==================================================================

    @GetMapping("/rules")
    @Operation(summary = "规则列表")
    public CommonResult<List<TouchpointGenerateRuleEntity>> listRules(
            @RequestParam(required = false) String eventCode) {
        LambdaQueryWrapper<TouchpointGenerateRuleEntity> wrapper =
                new LambdaQueryWrapper<TouchpointGenerateRuleEntity>()
                        .eq(TouchpointGenerateRuleEntity::getDelFlag, 0)
                        .orderByAsc(TouchpointGenerateRuleEntity::getEventCode)
                        .orderByAsc(TouchpointGenerateRuleEntity::getSortOrder);
        if (eventCode != null && !eventCode.isBlank()) {
            wrapper.eq(TouchpointGenerateRuleEntity::getEventCode, eventCode);
        }
        return CommonResult.success(ruleMapper.selectList(wrapper));
    }

    @PostMapping("/rules")
    @Operation(summary = "新增或修改规则")
    public CommonResult<Long> saveRule(@RequestBody TouchpointGenerateRuleEntity rule) {
        String now = now();
        rule.setUpdateTime(now);
        if (rule.getId() == null) {
            rule.setCreateTime(now);
            rule.setDelFlag(0);
            if (rule.getEnabled() == null) {
                rule.setEnabled(1);
            }
            if (rule.getOnceOnly() == null) {
                rule.setOnceOnly(0);
            }
            if (rule.getSortOrder() == null) {
                rule.setSortOrder(100);
            }
            ruleMapper.insert(rule);
        } else {
            ruleMapper.updateById(rule);
        }
        return CommonResult.success("保存成功", rule.getId());
    }

    @DeleteMapping("/rules/{id}")
    @Operation(summary = "删除规则（逻辑删除）")
    public CommonResult<Void> deleteRule(@PathVariable Long id) {
        TouchpointGenerateRuleEntity rule = new TouchpointGenerateRuleEntity();
        rule.setId(id);
        rule.setDelFlag(1);
        rule.setUpdateTime(now());
        ruleMapper.updateById(rule);
        return CommonResult.success("已删除", null);
    }

    // ==================================================================
    // 旅程模板与阶段
    // ==================================================================

    @GetMapping("/templates")
    @Operation(summary = "旅程列表")
    public CommonResult<List<CustomerJourneyTemplateEntity>> listTemplates(
            @RequestParam(required = false) Integer enabledOnly) {
        LambdaQueryWrapper<CustomerJourneyTemplateEntity> wrapper =
                new LambdaQueryWrapper<CustomerJourneyTemplateEntity>()
                        .eq(CustomerJourneyTemplateEntity::getDelFlag, 0)
                        .orderByAsc(CustomerJourneyTemplateEntity::getSortOrder);
        if (enabledOnly != null && enabledOnly == 1) {
            wrapper.eq(CustomerJourneyTemplateEntity::getEnabled, 1);
        }
        return CommonResult.success(templateMapper.selectList(wrapper));
    }

    @PostMapping("/templates")
    @Operation(summary = "新增或修改旅程")
    public CommonResult<Long> saveTemplate(@RequestBody CustomerJourneyTemplateEntity template) {
        String now = now();
        template.setUpdateTime(now);
        if (template.getId() == null) {
            template.setCreateTime(now);
            template.setDelFlag(0);
            if (template.getEnabled() == null) {
                template.setEnabled(1);
            }
            if (template.getIsDefault() == null) {
                template.setIsDefault(0);
            }
            if (template.getSortOrder() == null) {
                template.setSortOrder(100);
            }
            templateMapper.insert(template);
        } else {
            templateMapper.updateById(template);
        }
        return CommonResult.success("保存成功", template.getId());
    }

    @DeleteMapping("/templates/{id}")
    @Operation(summary = "删除旅程（逻辑删除，历史状态数据保留）")
    public CommonResult<Void> deleteTemplate(@PathVariable Long id) {
        CustomerJourneyTemplateEntity template = new CustomerJourneyTemplateEntity();
        template.setId(id);
        template.setDelFlag(1);
        template.setUpdateTime(now());
        templateMapper.updateById(template);
        return CommonResult.success("已删除", null);
    }

    @GetMapping("/stages")
    @Operation(summary = "某套旅程的阶段列表")
    public CommonResult<List<CustomerJourneyStageEntity>> listStages(
            @RequestParam String journeyCode) {
        return CommonResult.success(stageMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getJourneyCode, journeyCode)
                        .eq(CustomerJourneyStageEntity::getDelFlag, 0)
                        .orderByAsc(CustomerJourneyStageEntity::getSortOrder)));
    }

    @PostMapping("/stages")
    @Operation(summary = "新增或修改阶段")
    public CommonResult<Long> saveStage(@RequestBody CustomerJourneyStageEntity stage) {
        String now = now();
        stage.setUpdateTime(now);
        if (stage.getId() == null) {
            stage.setCreateTime(now);
            stage.setDelFlag(0);
            if (stage.getAllowRollback() == null) {
                stage.setAllowRollback(0);
            }
            if (stage.getSortOrder() == null) {
                stage.setSortOrder(100);
            }
            stageMapper.insert(stage);
        } else {
            stageMapper.updateById(stage);
        }
        return CommonResult.success("保存成功", stage.getId());
    }

    @DeleteMapping("/stages/{id}")
    @Operation(summary = "删除阶段（逻辑删除）")
    public CommonResult<Void> deleteStage(@PathVariable Long id) {
        CustomerJourneyStageEntity stage = new CustomerJourneyStageEntity();
        stage.setId(id);
        stage.setDelFlag(1);
        stage.setUpdateTime(now());
        stageMapper.updateById(stage);
        return CommonResult.success("已删除", null);
    }

    // ==================================================================
    // 业务事件 → 客户旅程阶段（零代码关联）
    // ==================================================================

    /**
     * 列出「业务事件 → 阶段」的全部关联。
     * <p>
     * 每个关联本质上是一条触点规则（touchpoint_generate_rule），只是把老板看不懂的
     * 触点类型翻译成他能看懂的「事件 + 阶段」组合。stage_code 落库后可直接反查阶段。
     */
    @GetMapping("/event-stage/mappings")
    @Operation(summary = "业务事件 → 旅程阶段 关联列表")
    public CommonResult<List<EventStageMappingDTO>> listEventStageMappings() {
        List<TouchpointGenerateRuleEntity> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<TouchpointGenerateRuleEntity>()
                        .eq(TouchpointGenerateRuleEntity::getDelFlag, 0)
                        .orderByAsc(TouchpointGenerateRuleEntity::getEventCode)
                        .orderByAsc(TouchpointGenerateRuleEntity::getSortOrder));
        if (rules.isEmpty()) {
            return CommonResult.success(Collections.emptyList());
        }

        Map<String, BusinessEventDictEntity> eventMap = eventDictMapper.selectList(
                        new LambdaQueryWrapper<BusinessEventDictEntity>())
                .stream().collect(Collectors.toMap(
                        BusinessEventDictEntity::getEventCode, e -> e, (a, b) -> a));

        List<CustomerJourneyStageEntity> stages = stageMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getDelFlag, 0));
        Map<String, CustomerJourneyStageEntity> stageByCode = stages.stream().collect(
                Collectors.toMap(CustomerJourneyStageEntity::getStageCode, s -> s, (a, b) -> a));
        Map<String, CustomerJourneyStageEntity> stageByTp = new HashMap<>();
        for (CustomerJourneyStageEntity s : stages) {
            if (s.getMatchTouchpoint() == null) continue;
            for (String tp : s.getMatchTouchpoint().split(",")) {
                String t = tp.trim();
                if (!t.isEmpty() && !stageByTp.containsKey(t)) stageByTp.put(t, s);
            }
        }

        Map<String, CustomerJourneyTemplateEntity> tplMap = templateMapper.selectList(
                        new LambdaQueryWrapper<CustomerJourneyTemplateEntity>()
                                .eq(CustomerJourneyTemplateEntity::getDelFlag, 0))
                .stream().collect(Collectors.toMap(
                        CustomerJourneyTemplateEntity::getJourneyCode, t -> t, (a, b) -> a));

        List<EventStageMappingDTO> out = new ArrayList<>();
        for (TouchpointGenerateRuleEntity r : rules) {
            EventStageMappingDTO d = new EventStageMappingDTO();
            d.setId(r.getId());
            d.setEventCode(r.getEventCode());
            BusinessEventDictEntity ev = eventMap.get(r.getEventCode());
            if (ev != null) {
                d.setEventName(ev.getEventName());
                d.setEventNameEn(ev.getEventNameEn());
            }
            CustomerJourneyStageEntity st = (r.getStageCode() != null)
                    ? stageByCode.get(r.getStageCode())
                    : stageByTp.get(r.getTouchpointType());
            if (st != null) {
                d.setStageCode(st.getStageCode());
                d.setStageName(st.getStageName());
                d.setJourneyCode(st.getJourneyCode());
                CustomerJourneyTemplateEntity tpl = tplMap.get(st.getJourneyCode());
                d.setJourneyName(tpl != null ? tpl.getJourneyName() : st.getJourneyCode());
                d.setMatchTouchpoint(r.getTouchpointType());
            }
            d.setEnabled(r.getEnabled());
            d.setRuleName(r.getRuleName());
            out.add(d);
        }
        return CommonResult.success(out);
    }

    /**
     * 建立「业务事件 → 阶段」关联。
     * <p>
     * 后端据此生成一条触点规则：touchpoint_type 取该阶段 match_touchpoint 的首个值，
     * intent 取 match_intent 的首个值，channel=SYSTEM。规则生效即代表「该事件发生后，
     * 客户被推进到这个旅程阶段」，无需改任何代码。tenant 置 NULL 以匹配全部租户。
     */
    @PostMapping("/event-stage/mappings")
    @Operation(summary = "新增 业务事件 → 旅程阶段 关联（零代码创建触点规则）")
    public CommonResult<Long> createEventStageMapping(@RequestBody EventStageLinkReq req) {
        if (req.getEventCode() == null || req.getEventCode().isBlank()
                || req.getStageCode() == null || req.getStageCode().isBlank()) {
            return CommonResult.fail("事件编码与阶段编码均必填");
        }
        BusinessEventDictEntity ev = eventDictMapper.selectOne(
                new LambdaQueryWrapper<BusinessEventDictEntity>()
                        .eq(BusinessEventDictEntity::getEventCode, req.getEventCode()));
        if (ev == null) {
            return CommonResult.fail("业务事件不存在: " + req.getEventCode());
        }
        CustomerJourneyStageEntity st = stageMapper.selectOne(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getStageCode, req.getStageCode())
                        .eq(CustomerJourneyStageEntity::getDelFlag, 0));
        if (st == null) {
            return CommonResult.fail("阶段不存在: " + req.getStageCode());
        }

        String tp = firstToken(st.getMatchTouchpoint());
        String intent = firstToken(st.getMatchIntent());

        // 去重：同一事件+阶段已存在则更新触点类型，避免重复规则
        TouchpointGenerateRuleEntity existing = ruleMapper.selectOne(
                new LambdaQueryWrapper<TouchpointGenerateRuleEntity>()
                        .eq(TouchpointGenerateRuleEntity::getEventCode, req.getEventCode())
                        .eq(TouchpointGenerateRuleEntity::getStageCode, req.getStageCode())
                        .eq(TouchpointGenerateRuleEntity::getDelFlag, 0));
        if (existing != null) {
            existing.setTouchpointType(tp);
            existing.setIntent(intent);
            existing.setEnabled(1);
            existing.setUpdateTime(now());
            ruleMapper.updateById(existing);
            return CommonResult.success("关联已更新", existing.getId());
        }

        CustomerJourneyTemplateEntity tpl = templateMapper.selectOne(
                new LambdaQueryWrapper<CustomerJourneyTemplateEntity>()
                        .eq(CustomerJourneyTemplateEntity::getJourneyCode, st.getJourneyCode())
                        .eq(CustomerJourneyTemplateEntity::getDelFlag, 0));
        String journeyName = tpl != null ? tpl.getJourneyName() : st.getJourneyCode();

        TouchpointGenerateRuleEntity rule = new TouchpointGenerateRuleEntity();
        rule.setEventCode(req.getEventCode());
        rule.setTouchpointType(tp);
        rule.setIntent(intent);
        rule.setChannel("SYSTEM");
        rule.setStageCode(req.getStageCode());
        rule.setRuleName(ev.getEventName() + " → " + journeyName + "/" + st.getStageName());
        rule.setConditionJson(null);
        rule.setOnceOnly(0);
        rule.setEnabled(1);
        rule.setSortOrder(100);
        rule.setDelFlag(0);
        rule.setTenantId(null);
        rule.setCreateTime(now());
        rule.setUpdateTime(now());
        ruleMapper.insert(rule);
        return CommonResult.success("关联已创建", rule.getId());
    }

    @DeleteMapping("/event-stage/mappings/{id}")
    @Operation(summary = "删除 业务事件 → 旅程阶段 关联（逻辑删除对应规则）")
    public CommonResult<Void> deleteEventStageMapping(@PathVariable Long id) {
        TouchpointGenerateRuleEntity rule = new TouchpointGenerateRuleEntity();
        rule.setId(id);
        rule.setDelFlag(1);
        rule.setUpdateTime(now());
        ruleMapper.updateById(rule);
        return CommonResult.success("已删除", null);
    }

    /** 取 CSV 首值（阶段匹配触点/意图可能为多值，规则只取第一个作为触发信号） */
    private String firstToken(String csv) {
        if (csv == null || csv.isBlank()) return null;
        return csv.split(",")[0].trim();
    }

    // ==================================================================
    // 客户旅程状态查询
    // ==================================================================

    @GetMapping("/states")
    @Operation(summary = "某套旅程下的客户状态分布")
    public CommonResult<List<CustomerJourneyStateEntity>> listStates(
            @RequestParam String journeyCode,
            @RequestParam(required = false) String stageCode) {
        LambdaQueryWrapper<CustomerJourneyStateEntity> wrapper =
                new LambdaQueryWrapper<CustomerJourneyStateEntity>()
                        .eq(CustomerJourneyStateEntity::getJourneyCode, journeyCode)
                        .orderByDesc(CustomerJourneyStateEntity::getUpdateTime);
        if (stageCode != null && !stageCode.isBlank()) {
            wrapper.eq(CustomerJourneyStateEntity::getCurrentStageCode, stageCode);
        }
        return CommonResult.success(stateMapper.selectList(wrapper));
    }

    // ==================================================================
    // 重算入口
    // ==================================================================

    @PostMapping("/recompute/states")
    @Operation(summary = "按现有触点重算全部旅程状态（改完阶段配置点这里）")
    public CommonResult<Map<String, Object>> recomputeStates() {
        return CommonResult.success("重算完成", recomputeService.rebuildJourneyStates());
    }

    @PostMapping("/recompute/backfill")
    @Operation(summary = "从历史销售订单回溯生成触点与旅程（首次上线用）")
    public CommonResult<Map<String, Object>> backfill() {
        return CommonResult.success("回溯完成", recomputeService.backfillFromSaleOrders());
    }

    @PostMapping("/recompute/metrics")
    @Operation(summary = "全量刷新客户指标")
    public CommonResult<Map<String, Object>> recomputeMetrics() {
        return CommonResult.success("刷新完成", recomputeService.refreshAllMetrics());
    }

    @PostMapping("/recompute/lifecycle-scan")
    @Operation(summary = "生命周期扫描：把沉默/流失会员转成引擎事件（私域运营动态分层）")
    public CommonResult<Map<String, Object>> lifecycleScan() {
        return CommonResult.success("扫描完成", lifecycleScanner.scan());
    }

    private String now() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
}
