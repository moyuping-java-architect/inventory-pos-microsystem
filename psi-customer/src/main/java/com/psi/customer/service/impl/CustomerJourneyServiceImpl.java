package com.psi.customer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.result.CommonResult;
import com.psi.customer.dto.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psi.customer.entity.CustomerJourneyConfigEntity;
import com.psi.customer.entity.CustomerJourneyStageEntity;
import com.psi.customer.entity.CustomerJourneyStateEntity;
import com.psi.customer.entity.CustomerJourneyTemplateEntity;
import com.psi.customer.entity.CustomerTouchpointEntity;
import com.psi.customer.mapper.CustomerJourneyMapper;
import com.psi.customer.mapper.CustomerJourneyStageMapper;
import com.psi.customer.mapper.CustomerJourneyStateMapper;
import com.psi.customer.mapper.CustomerJourneyTemplateMapper;
import com.psi.customer.mapper.CustomerTouchpointMapper;
import com.psi.customer.service.CustomerJourneyConfigService;
import com.psi.customer.service.CustomerJourneyService;
import com.psi.customer.service.SalesScriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 客户增长服务实现
 *
 * 核心卖点：把"老板脑子里的隐性客户资产"变成"系统里的显性数据"
 * 防损（进销存）是保险逻辑→砍价；增收（客户旅程）是投资逻辑→愿付
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerJourneyServiceImpl implements CustomerJourneyService {

    private final CustomerJourneyMapper journeyMapper;
    private final CustomerTouchpointMapper touchpointMapper;
    private final CustomerJourneyConfigService configService;
    private final CustomerJourneyStageMapper stageMapper;
    private final CustomerJourneyStateMapper stateMapper;
    private final CustomerJourneyTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;
    private final SalesScriptService salesScriptService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== 看板 ==========

    @Override
    public CustomerJourneyDashboardDTO getDashboard() {
        List<MemberSummaryDTO> allCustomers = journeyMapper.selectCustomerOrderSummaries();

        // 计算每个客户的流失等级
        for (MemberSummaryDTO c : allCustomers) {
            c.setChurnLevel(calculateChurnLevel(c));
            c.setChurnSort(getChurnSort(c.getChurnLevel()));
        }

        CustomerJourneyDashboardDTO dashboard = new CustomerJourneyDashboardDTO();

        // 概览数字
        dashboard.setTotalMembers(allCustomers.size());
        dashboard.setActiveMembers((int) allCustomers.stream().filter(c -> "ACTIVE".equals(c.getChurnLevel())).count());
        dashboard.setAtRiskMembers((int) allCustomers.stream()
                .filter(c -> "AT_RISK".equals(c.getChurnLevel()) || "HIGH_RISK".equals(c.getChurnLevel())).count());
        dashboard.setChurnedMembers((int) allCustomers.stream()
                .filter(c -> "CHURNED".equals(c.getChurnLevel()) || "NEW_CHURNED".equals(c.getChurnLevel())).count());
        dashboard.setNewMembers((int) allCustomers.stream().filter(c -> "NEW".equals(c.getChurnLevel())).count());

        // 财务影响
        dashboard.setRevenueAtRisk(calculateRevenueAtRisk(allCustomers));

        // 月度收入对比
        String thisMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        BigDecimal thisMonthRev = journeyMapper.selectMonthlyRevenue(thisMonth);
        BigDecimal lastMonthRev = journeyMapper.selectMonthlyRevenue(lastMonth);
        dashboard.setTotalRevenueThisMonth(thisMonthRev != null ? thisMonthRev : BigDecimal.ZERO);
        dashboard.setTotalRevenueLastMonth(lastMonthRev != null ? lastMonthRev : BigDecimal.ZERO);

        if (lastMonthRev != null && lastMonthRev.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = thisMonthRev.subtract(lastMonthRev)
                    .divide(lastMonthRev, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
            dashboard.setGrowthRate(growth);
        } else {
            dashboard.setGrowthRate(BigDecimal.ZERO);
        }

        // 客单价
        BigDecimal totalSpent = allCustomers.stream()
                .map(MemberSummaryDTO::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalOrders = allCustomers.stream()
                .mapToInt(c -> c.getTotalOrders() != null ? c.getTotalOrders() : 0)
                .sum();
        dashboard.setAvgOrderValue(totalOrders > 0 ?
                totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO);

        // 复购率
        int repurchaseCount = (int) allCustomers.stream()
                .filter(c -> c.getTotalOrders() != null && c.getTotalOrders() >= 2).count();
        dashboard.setRepurchaseRate(allCustomers.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(repurchaseCount)
                        .divide(BigDecimal.valueOf(allCustomers.size()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP));

        // 待跟进
        dashboard.setPendingFollowUps(journeyMapper.selectPendingFollowUpCount());

        // Top N 客户 (configurable)
        int topCustomerLimit = configService.getInt("DASHBOARD", "top_customers_limit", 5);
        dashboard.setTopCustomers(allCustomers.stream().limit(topCustomerLimit).collect(Collectors.toList()));

        // Top N 流失预警 (configurable)
        int churnAlertLimit = configService.getInt("DASHBOARD", "top_churn_alerts_limit", 10);
        dashboard.setTopChurnAlerts(allCustomers.stream()
                .filter(c -> "AT_RISK".equals(c.getChurnLevel())
                        || "HIGH_RISK".equals(c.getChurnLevel())
                        || "CHURNED".equals(c.getChurnLevel()))
                .sorted(Comparator.comparing(MemberSummaryDTO::getTotalSpent).reversed())
                .limit(churnAlertLimit)
                .map(this::toChurnAlert)
                .collect(Collectors.toList()));

        log.info("Dashboard generated: total={}, active={}, atRisk={}, churned={}",
                dashboard.getTotalMembers(), dashboard.getActiveMembers(),
                dashboard.getAtRiskMembers(), dashboard.getChurnedMembers());

        return dashboard;
    }

    // ========== 流失预警 ==========

    @Override
    public List<ChurnAlertDTO> getChurnAlerts() {
        List<MemberSummaryDTO> allCustomers = journeyMapper.selectCustomerOrderSummaries();
        List<ChurnAlertDTO> alerts = new ArrayList<>();

        for (MemberSummaryDTO c : allCustomers) {
            c.setChurnLevel(calculateChurnLevel(c));
            String level = c.getChurnLevel();

            if ("AT_RISK".equals(level) || "HIGH_RISK".equals(level)
                    || "CHURNED".equals(level) || "NEW_CHURNED".equals(level)) {
                alerts.add(toChurnAlert(c));
            }
        }

        // 按累计消费降序（高价值客户优先召回）
        alerts.sort(Comparator.comparing(ChurnAlertDTO::getTotalSpent).reversed());
        return alerts;
    }

    // ========== 客户旅程时间线 ==========

    @Override
    public MemberJourneyDTO getCustomerJourney(Long customerId) {
        List<MemberSummaryDTO> all = journeyMapper.selectCustomerOrderSummaries();
        MemberSummaryDTO summary = all.stream()
                .filter(c -> c.getMemberId() != null && c.getMemberId().equals(customerId))
                .findFirst()
                .orElse(null);

        MemberJourneyDTO journey = new MemberJourneyDTO();
        if (summary == null) {
            // 客户可能有旅程/标签但无订单记录，从 customer 表补充基础信息
            summary = journeyMapper.selectCustomerById(customerId);
        }
        if (summary == null) {
            summary = new MemberSummaryDTO();
            summary.setMemberId(customerId.intValue());
            summary.setMemberName("客户#" + customerId);
            summary.setPhone("");
            summary.setTotalOrders(0);
            summary.setTotalSpent(java.math.BigDecimal.ZERO);
            summary.setAvgOrderValue(java.math.BigDecimal.ZERO);
        }

        summary.setChurnLevel(calculateChurnLevel(summary));

        journey.setMemberId(summary.getMemberId() != null ? summary.getMemberId().intValue() : customerId.intValue());
        journey.setMemberName(summary.getMemberName());
        journey.setPhone(summary.getPhone());
        journey.setTotalSpent(summary.getTotalSpent());
        journey.setTotalOrders(summary.getTotalOrders());
        journey.setAvgOrderValue(summary.getAvgOrderValue());
        journey.setLastOrderTime(summary.getLastOrderTime());
        journey.setAvgIntervalDays(summary.getAvgIntervalDays());
        journey.setChurnLevel(summary.getChurnLevel());
        journey.setRiskDescription(getRiskDescription(summary));

        // ===== 客户旅程阶段 + 停留天数 + 标签 + 话术 =====
        enrichJourneyStageAndScripts(customerId, journey);

        // 购买事件时间线
        List<MemberJourneyDTO.TimelineItem> timeline = new ArrayList<>(journeyMapper.selectCustomerOrderTimeline(customerId));

        // 触点事件时间线
        List<CustomerTouchpointEntity> touchpoints = touchpointMapper.selectByCustomerId(customerId);
        for (CustomerTouchpointEntity tp : touchpoints) {
            MemberJourneyDTO.TimelineItem item = new MemberJourneyDTO.TimelineItem();
            item.setTime(tp.getContactTime());
            item.setEventType(tp.getTouchpointType());
            item.setSummary(tp.getSummary());
            item.setChannel(tp.getChannel());
            timeline.add(item);
        }

        // 按时间倒序
        timeline.sort((a, b) -> {
            if (b.getTime() == null) return 1;
            if (a.getTime() == null) return -1;
            return b.getTime().compareTo(a.getTime());
        });

        // 限制条数 (configurable)
        int timelineLimit = configService.getInt("DASHBOARD", "timeline_limit", 20);
        if (timeline.size() > timelineLimit) {
            timeline = timeline.subList(0, timelineLimit);
        }
        journey.setTimeline(timeline);

        return journey;
    }

    /**
     * 补充客户旅程阶段、停留天数、标签、待发送话术与已发送历史。
     */
    private void enrichJourneyStageAndScripts(Long customerId, MemberJourneyDTO journey) {
        // 1. 当前旅程阶段
        CustomerJourneyStateEntity state = stateMapper.selectByCustomerId(customerId);
        if (state != null && state.getCurrentStageCode() != null) {
            journey.setCurrentStageCode(state.getCurrentStageCode());
            CustomerJourneyStageEntity stage = stageMapper.selectByStageCode(state.getCurrentStageCode());
            if (stage != null) {
                journey.setCurrentStageName(stage.getStageName());
            }
            if (state.getEnterStageTime() != null) {
                try {
                    java.time.LocalDateTime enter = java.time.LocalDateTime.parse(
                            state.getEnterStageTime(), TIME_FORMATTER);
                    long days = java.time.Duration
                            .between(enter, java.time.LocalDateTime.now())
                            .toDays();
                    journey.setDaysInStage((int) days);
                } catch (Exception e) {
                    log.warn("parse enterStageTime failed: {}", state.getEnterStageTime());
                }
            }
        }

        // 2. 客户标签
        try {
            List<String> tagNames = stateMapper.selectTagNamesByCustomerId(customerId);
            journey.setCustomerTags(tagNames != null ? tagNames : new ArrayList<>());
        } catch (Exception e) {
            log.warn("load customer tags failed, customerId={}", customerId, e);
            journey.setCustomerTags(new ArrayList<>());
        }

        // 3. 待发送话术（基于当前阶段 + 停留天数 + 标签 + 已发送去重）
        try {
            CommonResult<List<ScriptMatchResultDTO>> matched = salesScriptService.matchScriptsForCustomer(customerId);
            journey.setMatchedScripts(matched != null && matched.getData() != null ? matched.getData() : new ArrayList<>());
        } catch (Exception e) {
            log.warn("match scripts failed, customerId={}", customerId, e);
            journey.setMatchedScripts(new ArrayList<>());
        }

        // 4. 已发送历史
        try {
            CommonResult<List<CustomerScriptSendLogDTO>> sent = salesScriptService.listSendLogs(customerId);
            journey.setSentScripts(sent != null && sent.getData() != null ? sent.getData() : new ArrayList<>());
        } catch (Exception e) {
            log.warn("load send logs failed, customerId={}", customerId, e);
            journey.setSentScripts(new ArrayList<>());
        }
    }

    // ========== Top 客户 ==========

    @Override
    public List<MemberSummaryDTO> getTopCustomers(int limit) {
        List<MemberSummaryDTO> top = journeyMapper.selectTopCustomers(limit);
        for (MemberSummaryDTO c : top) {
            c.setChurnLevel(calculateChurnLevel(c));
            c.setChurnSort(getChurnSort(c.getChurnLevel()));
        }
        return top;
    }

    // ========== 召回建议 ==========

    @Override
    public List<WinBackSuggestionDTO> getWinBackSuggestions() {
        List<MemberSummaryDTO> all = journeyMapper.selectCustomerOrderSummaries();
        List<WinBackSuggestionDTO> suggestions = new ArrayList<>();

        for (MemberSummaryDTO c : all) {
            c.setChurnLevel(calculateChurnLevel(c));
            String level = c.getChurnLevel();

            if (!"AT_RISK".equals(level) && !"HIGH_RISK".equals(level)
                    && !"CHURNED".equals(level) && !"NEW_CHURNED".equals(level)) {
                continue;
            }

            // 获取最近买的商品（用于召回话术）
            if (c.getMemberId() != null) {
                String lastProduct = journeyMapper.selectLastProductByCustomer(c.getMemberId().longValue());
                c.setLastProductName(lastProduct);
            }

            WinBackSuggestionDTO s = new WinBackSuggestionDTO();
            s.setMemberId(c.getMemberId() != null ? c.getMemberId().intValue() : null);
            s.setMemberName(c.getMemberName());
            s.setPhone(c.getPhone());
            s.setChurnLevel(level);
            s.setTotalSpent(c.getTotalSpent());
            s.setLastProductName(c.getLastProductName());
            s.setDaysSinceLastOrder(c.getDaysSinceLastOrder());

            // 渠道选择：高价值用电话，普通用WhatsApp (configurable)
            double highValueThreshold = configService.getDouble("CHANNEL_RULE", "high_value_threshold", 5000);
            String defaultChannel = configService.getString("CHANNEL_RULE", "default_channel", "WHATSAPP");
            if (c.getTotalSpent() != null && c.getTotalSpent().compareTo(BigDecimal.valueOf(highValueThreshold)) > 0) {
                s.setChannel("PHONE");
            } else {
                s.setChannel(defaultChannel);
            }

            // 预估回客单价
            s.setEstimatedOrderValue(c.getAvgOrderValue());

            // 召回理由
            s.setReason(getRiskDescription(c));

            // 消息模板
            s.setMessageTemplate(buildMessageTemplate(c));

            suggestions.add(s);
        }

        suggestions.sort(Comparator.comparing(WinBackSuggestionDTO::getTotalSpent,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // Limit results (configurable)
        int winbackLimit = configService.getInt("DASHBOARD", "winback_limit", 20);
        if (suggestions.size() > winbackLimit) {
            suggestions = suggestions.subList(0, winbackLimit);
        }
        return suggestions;
    }

    // ========== 触点录入 ==========

    @Override
    public CommonResult<Integer> recordTouchpoint(CustomerTouchpointDTO dto) {
        CustomerTouchpointEntity entity = new CustomerTouchpointEntity();
        entity.setMemberId(dto.getMemberId() != null ? dto.getMemberId().longValue() : null);
        entity.setCustomerId(dto.getCustomerId() != null ? dto.getCustomerId().longValue() : null);
        entity.setTouchpointType(dto.getTouchpointType());
        entity.setChannel(dto.getChannel());
        entity.setContactTime(dto.getContactTime() != null ?
                dto.getContactTime() : LocalDateTime.now().format(TIME_FORMATTER));
        entity.setSummary(dto.getSummary());
        entity.setIntent(dto.getIntent());
        entity.setFollowUp(dto.getFollowUp());
        entity.setFollowUpDone(0);
        entity.setOperator(dto.getOperator());
        entity.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));
        entity.setUpdateTime(LocalDateTime.now().format(TIME_FORMATTER));
        entity.setDelFlag(0);

        touchpointMapper.insert(entity);
        log.info("Touchpoint recorded: customerId={}, type={}, summary={}",
                dto.getCustomerId(), dto.getTouchpointType(), dto.getSummary());

        return CommonResult.success("Touchpoint recorded", entity.getId());
    }

    // ========== 风险收入 ==========

    @Override
    public BigDecimal getRevenueAtRisk() {
        List<MemberSummaryDTO> all = journeyMapper.selectCustomerOrderSummaries();
        for (MemberSummaryDTO c : all) {
            c.setChurnLevel(calculateChurnLevel(c));
        }
        return calculateRevenueAtRisk(all);
    }

    // ========== 客户旅程分组 ==========

    /**
     * 看板旅程分组。
     * <p>
     * 数据来源全部是"老板配出来的"，代码里不再猜阶段：
     * <ul>
     *   <li>阶段节点 ← {@code customer_journey_stage}（配置页维护，含颜色/图标/提示/顺序）</li>
     *   <li>客户在哪个阶段 ← {@code customer_journey_state}（旅程引擎按规则算出来的结果）</li>
     * </ul>
     * 引擎没算到的客户统一落在第一个阶段（表示"还没动起来"），保证总数不丢人。
     */
    @Override
    public List<StageCustomerGroupDTO> getCustomersByStage(String journeyCode, int perStageLimit) {
        // 1. 解析旅程编码：没传就用默认旅程
        String code = resolveJourneyCode(journeyCode);
        if (code == null) {
            log.warn("看板取不到任何可用旅程，返回空");
            return new ArrayList<>();
        }

        // 2. 阶段节点来自阶段表（老板配置页维护）
        List<StageMeta> loaded = loadStagesByJourney(code);
        if (loaded.isEmpty()) {
            // 兜底：老库还没建阶段表数据时，退回旧的 flat key 配置，避免看板整块空白
            loaded = loadStages();
        }
        if (loaded.isEmpty()) return new ArrayList<>();
        final List<StageMeta> stages = loaded;

        // 3. 客户归属来自引擎算出的旅程状态
        List<CustomerJourneyStateEntity> states = stateMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyStateEntity>()
                        .eq(CustomerJourneyStateEntity::getJourneyCode, code));
        Map<Long, String> stageBySubject = states.stream()
                .filter(s -> s.getSubjectId() != null && s.getCurrentStageCode() != null)
                .collect(Collectors.toMap(
                        CustomerJourneyStateEntity::getSubjectId,
                        CustomerJourneyStateEntity::getCurrentStageCode,
                        (a, b) -> b));

        // 4. 客户明细（消费额/订单数等）仍从订单汇总来
        List<MemberSummaryDTO> all = journeyMapper.selectCustomerOrderSummaries();
        String firstStage = stages.get(0).code;
        java.util.Set<String> validStages = stages.stream()
                .map(s -> s.code).collect(Collectors.toSet());
        for (MemberSummaryDTO c : all) {
            // ⚠️ memberId 是 Integer、subjectId 是 Long，直接 get(Integer) 永远返回 null 且不报错，
            //    必须显式 longValue() 再查，否则所有客户都会被误判成"还没动起来"。
            Long subjectId = c.getMemberId() == null ? null : c.getMemberId().longValue();
            String hit = subjectId == null ? null : stageBySubject.get(subjectId);
            // 引擎没算到 or 算出来的阶段已被老板删掉 → 归到第一个阶段
            c.setChurnLevel(hit != null && validStages.contains(hit) ? hit : firstStage);
        }

        // 4. 按阶段分组（每个阶段按消费降序排，截前 N）
        Map<String, List<MemberSummaryDTO>> byStage = all.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getChurnLevel() != null ? c.getChurnLevel() : stages.get(0).code));

        // 5. 按 sortOrder 顺序组装响应（空阶段也返回，让前端能展示完整漏斗）
        List<StageCustomerGroupDTO> result = new ArrayList<>();
        for (StageMeta meta : stages) {
            StageCustomerGroupDTO group = new StageCustomerGroupDTO();
            group.setStageCode(meta.code);
            group.setStageDisplayName(meta.displayName);
            group.setColor(meta.color);
            group.setIcon(meta.icon);
            group.setTip(meta.tip);
            group.setTagType("info");

            List<MemberSummaryDTO> stageList = byStage.getOrDefault(meta.code, new ArrayList<>())
                    .stream()
                    .sorted(Comparator.comparing(
                            MemberSummaryDTO::getTotalSpent,
                            Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                    .limit(perStageLimit > 0 ? perStageLimit : 10)
                    .collect(Collectors.toList());

            group.setCustomerCount(stageList.size());
            group.setTotalRevenue(stageList.stream()
                    .map(MemberSummaryDTO::getTotalSpent)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            group.setCustomers(stageList);
            result.add(group);
        }
        return result;
    }

    /**
     * 解析看板要展示哪套旅程。
     * 传了就用传的；没传优先取 is_default=1 的启用模板；再没有就取排序最前的启用模板。
     */
    private String resolveJourneyCode(String journeyCode) {
        if (journeyCode != null && !journeyCode.isBlank()) {
            return journeyCode.trim();
        }
        List<CustomerJourneyTemplateEntity> templates = templateMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyTemplateEntity>()
                        .eq(CustomerJourneyTemplateEntity::getDelFlag, 0)
                        .eq(CustomerJourneyTemplateEntity::getEnabled, 1)
                        .orderByDesc(CustomerJourneyTemplateEntity::getIsDefault)
                        .orderByAsc(CustomerJourneyTemplateEntity::getSortOrder));
        return templates.isEmpty() ? null : templates.get(0).getJourneyCode();
    }

    /**
     * 从阶段表加载某套旅程的阶段节点（按 sortOrder 排序）。
     * 这是老板在配置页维护的那张表，改了立刻反映到看板。
     */
    private List<StageMeta> loadStagesByJourney(String journeyCode) {
        List<CustomerJourneyStageEntity> rows = stageMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getJourneyCode, journeyCode)
                        .eq(CustomerJourneyStageEntity::getDelFlag, 0)
                        .orderByAsc(CustomerJourneyStageEntity::getSortOrder));
        List<StageMeta> result = new ArrayList<>();
        for (CustomerJourneyStageEntity s : rows) {
            result.add(new StageMeta(
                    s.getStageCode(),
                    s.getStageName() != null && !s.getStageName().isBlank()
                            ? s.getStageName() : s.getStageCode(),
                    s.getColor() != null && !s.getColor().isBlank() ? s.getColor() : "#909399",
                    s.getIcon() != null && !s.getIcon().isBlank() ? s.getIcon() : "User",
                    s.getTip() != null ? s.getTip() : "",
                    splitCsv(s.getMatchTouchpoint()),
                    splitCsv(s.getMatchIntent()),
                    s.getSortOrder() != null ? s.getSortOrder() : 0
            ));
        }
        return result;
    }

    /** 逗号分隔串转 List，null / 空串返回空列表 */
    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (String part : csv.split(",")) {
            String v = part.trim();
            if (!v.isEmpty()) list.add(v);
        }
        return list;
    }

    /**
     * 【兼容旧库】从 JOURNEY_STAGES flat key 配置加载阶段（按 sortOrder 排序）
     * config_value 是 JSON：{color,icon,matchTouchpoint[],matchIntent[],tip}
     * 仅在阶段表没数据时兜底，新逻辑一律走 {@link #loadStagesByJourney}。
     */
    private List<StageMeta> loadStages() {
        List<CustomerJourneyConfigEntity> configs = configService.getByGroup("JOURNEY_STAGES");
        List<StageMeta> result = new ArrayList<>();
        for (CustomerJourneyConfigEntity c : configs) {
            try {
                com.fasterxml.jackson.databind.JsonNode node =
                        objectMapper.readTree(c.getConfigValue());
                result.add(new StageMeta(
                        c.getConfigKey(),
                        c.getDisplayName() != null && !c.getDisplayName().isBlank()
                                ? c.getDisplayName() : c.getConfigKey(),
                        textOrDefault(node, "color", "#909399"),
                        textOrDefault(node, "icon", "User"),
                        textOrDefault(node, "tip", ""),
                        stringListOrEmpty(node, "matchTouchpoint"),
                        stringListOrEmpty(node, "matchIntent"),
                        c.getSortOrder() != null ? c.getSortOrder() : 0
                ));
            } catch (Exception e) {
                log.warn("解析阶段配置 {} 失败: {}", c.getConfigKey(), e.getMessage());
            }
        }
        result.sort(Comparator.comparingInt(m -> m.sortOrder));
        return result;
    }

    private String textOrDefault(com.fasterxml.jackson.databind.JsonNode node, String field, String def) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : def;
    }

    private List<String> stringListOrEmpty(com.fasterxml.jackson.databind.JsonNode node, String field) {
        List<String> list = new ArrayList<>();
        if (node.has(field) && node.get(field).isArray()) {
            node.get(field).forEach(n -> list.add(n.asText()));
        }
        return list;
    }

    // 说明：原 calculateSalesFunnelStage() 已删除。
    // 它按"订单数 / 消费额"硬编码猜阶段（0单→阶段1、1单→阶段2、消费>5000→末阶段…），
    // 跟老板在配置页配的「业务事件 → 旅程阶段」毫无关系，导致配了也看不见。
    // 现在客户归属一律读旅程引擎算出的 customer_journey_state，单一真相源。

    private record StageMeta(String code, String displayName, String color, String icon,
                             String tip, List<String> matchTouchpoint, List<String> matchIntent,
                             int sortOrder) {}

    // ========== 私有方法 ==========

    /**
     * 自适应流失等级计算
     * 基于每个客户自己的购买频率，阈值全部从配置表读取（零代码可配）
     */
    private String calculateChurnLevel(MemberSummaryDTO m) {
        Integer daysSince = m.getDaysSinceLastOrder();
        Integer totalOrders = m.getTotalOrders();
        Double avgInterval = m.getAvgIntervalDays();

        if (daysSince == null) return "UNKNOWN";

        int newCustomerDays = configService.getInt("CHURN_MODEL", "new_customer_days", 30);
        if (totalOrders == null || totalOrders < 2) {
            return daysSince <= newCustomerDays ? "NEW" : "NEW_CHURNED";
        }

        // 超过配置的最大天数直接判流失
        int maxDays = configService.getInt("CHURN_MODEL", "max_days_churned", 90);
        if (daysSince > maxDays) return "CHURNED";

        // 有购买频率数据 → 自适应阈值（乘数从配置表读取）
        if (avgInterval != null && avgInterval > 0) {
            double activeMult = configService.getDouble("CHURN_MODEL", "active_multiplier", 1.5);
            double atRiskMult = configService.getDouble("CHURN_MODEL", "at_risk_multiplier", 2.5);
            double highRiskMult = configService.getDouble("CHURN_MODEL", "high_risk_multiplier", 4.0);

            if (daysSince <= activeMult * avgInterval) return "ACTIVE";
            if (daysSince <= atRiskMult * avgInterval) return "AT_RISK";
            if (daysSince <= highRiskMult * avgInterval) return "HIGH_RISK";
            return "CHURNED";
        }

        // 无频率数据 → 固定阈值兜底（天数从配置表读取）
        int fallbackActive = configService.getInt("CHURN_MODEL", "fallback_active_days", 30);
        int fallbackAtRisk = configService.getInt("CHURN_MODEL", "fallback_at_risk_days", 60);
        if (daysSince <= fallbackActive) return "ACTIVE";
        if (daysSince <= fallbackAtRisk) return "AT_RISK";
        return "HIGH_RISK";
    }

    private int getChurnSort(String level) {
        if (level == null) return 7;
        return switch (level) {
            case "ACTIVE" -> 1;
            case "NEW" -> 2;
            case "AT_RISK" -> 3;
            case "HIGH_RISK" -> 4;
            case "NEW_CHURNED" -> 5;
            case "CHURNED" -> 6;
            default -> 7;
        };
    }

    private String getRiskDescription(MemberSummaryDTO m) {
        String level = m.getChurnLevel();
        int days = m.getDaysSinceLastOrder() != null ? m.getDaysSinceLastOrder() : 0;

        // Read actual multiplier from config so description matches current thresholds
        double activeMult = configService.getDouble("CHURN_MODEL", "active_multiplier", 1.5);
        double atRiskMult = configService.getDouble("CHURN_MODEL", "at_risk_multiplier", 2.5);

        return switch (level) {
            case "ACTIVE" -> "Active - within normal cycle";
            case "NEW" -> "New customer - nurture phase";
            case "AT_RISK" -> days + " days since last visit (past " + activeMult + "x normal cycle)";
            case "HIGH_RISK" -> days + " days since last visit (past " + atRiskMult + "x normal cycle)";
            case "NEW_CHURNED" -> "New customer who hasn't returned in " + days + " days";
            case "CHURNED" -> "Likely churned - " + days + " days since last visit";
            default -> "Unknown status";
        };
    }

    private String getSuggestedAction(String level) {
        return switch (level) {
            case "AT_RISK" -> configService.getString("SUGGESTED_ACTION", "action_at_risk",
                    "Send WhatsApp: We miss you! 10% off this week");
            case "HIGH_RISK" -> configService.getString("SUGGESTED_ACTION", "action_high_risk",
                    "Call directly: Haven't seen you, everything OK?");
            case "CHURNED" -> configService.getString("SUGGESTED_ACTION", "action_churned",
                    "Last attempt: Special 15% off just for you");
            case "NEW_CHURNED" -> configService.getString("SUGGESTED_ACTION", "action_new_churned",
                    "Follow up: How was your first purchase?");
            default -> "No action needed";
        };
    }

    /**
     * Build WhatsApp message from configurable template.
     * Template selected by churn level, placeholders {name}/{days}/{product} replaced.
     */
    private String buildMessageTemplate(MemberSummaryDTO m) {
        String name = m.getMemberName() != null ? m.getMemberName() : "friend";
        String product = m.getLastProductName() != null ? m.getLastProductName() : "our products";
        String days = String.valueOf(m.getDaysSinceLastOrder() != null ? m.getDaysSinceLastOrder() : 0);

        // Select template by churn level (all templates in config table)
        String level = m.getChurnLevel();
        String templateKey = switch (level) {
            case "AT_RISK" -> "template_at_risk";
            case "HIGH_RISK" -> "template_high_risk";
            case "CHURNED" -> "template_churned";
            case "NEW_CHURNED" -> "template_new_churned";
            default -> "template_at_risk";
        };

        String template = configService.getString("MESSAGE_TEMPLATE", templateKey,
                "Hi {name}! We haven't seen you in {days} days. Come back for 10% off!");

        return template.replace("{name}", name)
                .replace("{days}", days)
                .replace("{product}", product);
    }

    private ChurnAlertDTO toChurnAlert(MemberSummaryDTO m) {
        ChurnAlertDTO alert = new ChurnAlertDTO();
        alert.setMemberId(m.getMemberId() != null ? m.getMemberId().intValue() : null);
        alert.setMemberName(m.getMemberName());
        alert.setPhone(m.getPhone());
        alert.setTotalOrders(m.getTotalOrders());
        alert.setTotalSpent(m.getTotalSpent());
        alert.setLastOrderTime(m.getLastOrderTime());
        alert.setDaysSinceLastOrder(m.getDaysSinceLastOrder());
        alert.setAvgIntervalDays(m.getAvgIntervalDays());
        alert.setChurnLevel(m.getChurnLevel());
        alert.setRiskDescription(getRiskDescription(m));
        alert.setSuggestedAction(getSuggestedAction(m.getChurnLevel()));
        alert.setLastProductName(m.getLastProductName());
        return alert;
    }

    /**
     * 风险收入 = 所有 AT_RISK / HIGH_RISK / CHURNED 客户的月均消费
     * 月均消费 = 客单价 × (30 / 平均购买间隔)
     */
    private BigDecimal calculateRevenueAtRisk(List<MemberSummaryDTO> customers) {
        BigDecimal total = BigDecimal.ZERO;
        for (MemberSummaryDTO c : customers) {
            String level = c.getChurnLevel();
            if ("AT_RISK".equals(level) || "HIGH_RISK".equals(level)
                    || "CHURNED".equals(level) || "NEW_CHURNED".equals(level)) {
                if (c.getAvgIntervalDays() != null && c.getAvgIntervalDays() > 0
                        && c.getAvgOrderValue() != null) {
                    BigDecimal monthlyRate = c.getAvgOrderValue()
                            .multiply(BigDecimal.valueOf(30.0 / c.getAvgIntervalDays()));
                    total = total.add(monthlyRate);
                } else if (c.getAvgOrderValue() != null) {
                    total = total.add(c.getAvgOrderValue());
                }
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
