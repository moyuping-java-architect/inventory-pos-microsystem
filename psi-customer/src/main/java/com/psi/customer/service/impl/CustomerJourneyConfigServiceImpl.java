package com.psi.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.psi.customer.dto.JourneyTemplateOptionDTO;
import com.psi.customer.entity.CustomerJourneyConfigEntity;
import com.psi.customer.entity.CustomerJourneyStageEntity;
import com.psi.customer.entity.TouchpointGenerateRuleEntity;
import com.psi.customer.entity.CustomerJourneyTemplateEntity;
import com.psi.customer.mapper.CustomerJourneyConfigMapper;
import com.psi.customer.mapper.CustomerJourneyStageMapper;
import com.psi.customer.mapper.TouchpointGenerateRuleMapper;
import com.psi.customer.mapper.CustomerJourneyTemplateMapper;
import com.psi.customer.service.CustomerJourneyConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Zero-code config service implementation.
 * Caches all config in a ConcurrentHashMap; refreshes on update.
 * Default values are kept in a static map for resetToDefaults().
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerJourneyConfigServiceImpl implements CustomerJourneyConfigService {

    private final CustomerJourneyConfigMapper configMapper;
    private final CustomerJourneyTemplateMapper templateMapper;
    private final CustomerJourneyStageMapper stageMapper;
    private final TouchpointGenerateRuleMapper ruleMapper;

    private static final ObjectMapper M = new ObjectMapper();

    /** Separate group for the UI editing surface (Config page "客户旅程阶段" tab). */
    static final String STAGE_CFG_GROUP = "JOURNEY_STAGE_CONFIG";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Cache: key = "GROUP:KEY" -> value string */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /** Whether cache has been loaded */
    private volatile boolean loaded = false;

    /** Default seed values (28 items, mirrors customer_journey_config.sql) */
    private static final Map<String, String> DEFAULTS = new HashMap<>();
    static {
        // CHURN_MODEL
        DEFAULTS.put("CHURN_MODEL:active_multiplier",     "1.5");
        DEFAULTS.put("CHURN_MODEL:at_risk_multiplier",    "2.5");
        DEFAULTS.put("CHURN_MODEL:high_risk_multiplier",  "4.0");
        DEFAULTS.put("CHURN_MODEL:max_days_churned",      "90");
        DEFAULTS.put("CHURN_MODEL:new_customer_days",     "30");
        DEFAULTS.put("CHURN_MODEL:fallback_active_days", "30");
        DEFAULTS.put("CHURN_MODEL:fallback_at_risk_days", "60");
        // MESSAGE_TEMPLATE
        DEFAULTS.put("MESSAGE_TEMPLATE:template_at_risk",
                "Hi {name}! We haven't seen you in {days} days. Last time you bought {product}. Come back this week for 10% off! Reply STOP to opt out.");
        DEFAULTS.put("MESSAGE_TEMPLATE:template_high_risk",
                "Hi {name}! It's been {days} days since your last visit. Is everything OK? We'd love to see you again. Special offer inside!");
        DEFAULTS.put("MESSAGE_TEMPLATE:template_churned",
                "Hi {name}! We miss you! It's been {days} days. Here's a special 15% off just for you. Valid this week only.");
        DEFAULTS.put("MESSAGE_TEMPLATE:template_new_churned",
                "Hi {name}! How was your first purchase of {product}? We'd love your feedback. Come back for 10% off your next order!");
        // TOUCHPOINT_TYPE
        DEFAULTS.put("TOUCHPOINT_TYPE:WHATSAPP_INQUIRY",   "WhatsApp price inquiry");
        DEFAULTS.put("TOUCHPOINT_TYPE:STORE_VISIT",        "Store visit");
        DEFAULTS.put("TOUCHPOINT_TYPE:PHONE_CALL",         "Phone call");
        DEFAULTS.put("TOUCHPOINT_TYPE:DELIVERY_FEEDBACK",  "Delivery feedback");
        DEFAULTS.put("TOUCHPOINT_TYPE:COMPLAINT",          "Complaint");
        DEFAULTS.put("TOUCHPOINT_TYPE:REFERRAL",           "Referral");
        // CHANNEL_RULE
        DEFAULTS.put("CHANNEL_RULE:high_value_threshold", "5000");
        DEFAULTS.put("CHANNEL_RULE:default_channel",     "WHATSAPP");
        // SUGGESTED_ACTION
        DEFAULTS.put("SUGGESTED_ACTION:action_at_risk",     "Send WhatsApp: We miss you! 10% off this week");
        DEFAULTS.put("SUGGESTED_ACTION:action_high_risk",   "Call directly: Haven't seen you, everything OK?");
        DEFAULTS.put("SUGGESTED_ACTION:action_churned",     "Last attempt: Special 15% off just for you");
        DEFAULTS.put("SUGGESTED_ACTION:action_new_churned", "Follow up: How was your first purchase?");
        // DASHBOARD
        DEFAULTS.put("DASHBOARD:top_customers_limit",   "5");
        DEFAULTS.put("DASHBOARD:top_churn_alerts_limit", "10");
        DEFAULTS.put("DASHBOARD:timeline_limit",        "20");
        DEFAULTS.put("DASHBOARD:winback_limit",         "20");
    }

    // ========== Typed getters ==========

    @Override
    public double getDouble(String group, String key, double defaultValue) {
        String val = getCached(group, key);
        if (val == null) return defaultValue;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            log.warn("Config {}:{} is not a number: {}", group, key, val);
            return defaultValue;
        }
    }

    @Override
    public int getInt(String group, String key, int defaultValue) {
        String val = getCached(group, key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            log.warn("Config {}:{} is not an integer: {}", group, key, val);
            return defaultValue;
        }
    }

    @Override
    public String getString(String group, String key, String defaultValue) {
        String val = getCached(group, key);
        return val != null ? val : defaultValue;
    }

    // ========== Admin operations ==========

    @Override
    public List<CustomerJourneyConfigEntity> getAll() {
        return configMapper.selectAllActive();
    }

    @Override
    public List<CustomerJourneyConfigEntity> getByGroup(String group) {
        return configMapper.selectByGroup(group);
    }

    @Override
    public List<CustomerJourneyConfigEntity> getTouchpointTypes() {
        return configMapper.selectByGroup("TOUCHPOINT_TYPE");
    }

    @Override
    public void batchUpdate(List<CustomerJourneyConfigEntity> items) {
        String now = LocalDateTime.now().format(FMT);
        for (CustomerJourneyConfigEntity item : items) {
            if (item.getId() == null) continue;
            item.setUpdateTime(now);
            configMapper.updateById(item);
            log.info("Config updated: {}:{} = {}",
                    item.getConfigGroup(), item.getConfigKey(), item.getConfigValue());
        }
        refreshCache();
    }

    @Override
    public void resetToDefaults() {
        List<CustomerJourneyConfigEntity> all = configMapper.selectAllActive();
        String now = LocalDateTime.now().format(FMT);
        for (CustomerJourneyConfigEntity e : all) {
            String cacheKey = e.getConfigGroup() + ":" + e.getConfigKey();
            String defaultVal = DEFAULTS.get(cacheKey);
            if (defaultVal != null) {
                e.setConfigValue(defaultVal);
                e.setUpdateTime(now);
                configMapper.updateById(e);
            }
        }
        refreshCache();
        log.info("Customer journey config reset to {} defaults", all.size());
    }

    // ========== Cache management ==========

    private String getCached(String group, String key) {
        ensureLoaded();
        return cache.get(group + ":" + key);
    }

    private synchronized void ensureLoaded() {
        if (loaded) return;
        refreshCache();
    }

    private void refreshCache() {
        cache.clear();
        List<CustomerJourneyConfigEntity> all = configMapper.selectAllActive();
        for (CustomerJourneyConfigEntity e : all) {
            cache.put(e.getConfigGroup() + ":" + e.getConfigKey(), e.getConfigValue());
        }
        loaded = true;
        log.debug("Config cache loaded: {} entries", cache.size());
    }

    // ========== Per-template journey stage config (UI editing surface) ==========

    @Override
    public List<JourneyTemplateOptionDTO> listJourneyTemplates() {
        List<CustomerJourneyTemplateEntity> rows = templateMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyTemplateEntity>()
                        .eq(CustomerJourneyTemplateEntity::getDelFlag, 0)
                        .orderByAsc(CustomerJourneyTemplateEntity::getSortOrder)
                        .orderByAsc(CustomerJourneyTemplateEntity::getJourneyCode)
        );
        Map<String, Long> stageCount = countStagesByTemplate();
        return rows.stream().map(t -> {
            JourneyTemplateOptionDTO dto = new JourneyTemplateOptionDTO();
            dto.setId(t.getId());
            dto.setJourneyCode(t.getJourneyCode());
            dto.setJourneyName(t.getJourneyName());
            dto.setSubjectType(t.getSubjectType());
            dto.setDescription(t.getDescription());
            dto.setIcon(t.getIcon());
            dto.setEnabled(t.getEnabled());
            dto.setSortOrder(t.getSortOrder());
            dto.setStageCount(stageCount.getOrDefault(t.getJourneyCode(), 0L));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerJourneyTemplateEntity createJourneyTemplate(CustomerJourneyTemplateEntity entity) {
        if (entity.getJourneyCode() == null || entity.getJourneyCode().isBlank()) {
            throw new IllegalArgumentException("journeyCode 不能为空");
        }
        if (entity.getJourneyCode().length() > 32) {
            throw new IllegalArgumentException("journeyCode 长度不能超过 32");
        }
        if (entity.getJourneyName() == null || entity.getJourneyName().isBlank()) {
            throw new IllegalArgumentException("journeyName 不能为空");
        }
        // 检查唯一性（同 code 不能重复）
        Long dup = templateMapper.selectCount(new LambdaQueryWrapper<CustomerJourneyTemplateEntity>()
                .eq(CustomerJourneyTemplateEntity::getJourneyCode, entity.getJourneyCode())
                .eq(CustomerJourneyTemplateEntity::getDelFlag, 0));
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("旅程编码已存在：" + entity.getJourneyCode());
        }
        entity.setId(null);  // ensure insert
        if (entity.getSubjectType() == null) entity.setSubjectType("CUSTOMER");
        if (entity.getEnabled() == null) entity.setEnabled(1);
        if (entity.getSortOrder() == null) entity.setSortOrder(0);
        if (entity.getIsDefault() == null) entity.setIsDefault(0);
        if (entity.getDelFlag() == null) entity.setDelFlag(0);
        String now = LocalDateTime.now().format(FMT);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        templateMapper.insert(entity);
        log.info("Created journey template: id={} code={}", entity.getId(), entity.getJourneyCode());
        return entity;
    }

    @Override
    @Transactional
    public void updateJourneyTemplate(Long id, CustomerJourneyTemplateEntity patch) {
        CustomerJourneyTemplateEntity existing = templateMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("旅程模板不存在：id=" + id);
        }
        // journeyCode 不可改
        if (patch.getJourneyName() != null) existing.setJourneyName(patch.getJourneyName());
        if (patch.getSubjectType() != null) existing.setSubjectType(patch.getSubjectType());
        if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
        if (patch.getIcon() != null) existing.setIcon(patch.getIcon());
        if (patch.getEnabled() != null) existing.setEnabled(patch.getEnabled());
        if (patch.getSortOrder() != null) existing.setSortOrder(patch.getSortOrder());
        existing.setUpdateTime(LocalDateTime.now().format(FMT));
        templateMapper.updateById(existing);
        log.info("Updated journey template: id={} code={}", id, existing.getJourneyCode());
    }

    @Override
    @Transactional
    public void deleteJourneyTemplate(Long id) {
        CustomerJourneyTemplateEntity existing = templateMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("旅程模板不存在：id=" + id);
        }
        // 保护：若仍有活跃 stages，拒绝删除（引导用户先清 stages）
        Long stageCount = stageMapper.selectCount(new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                .eq(CustomerJourneyStageEntity::getJourneyCode, existing.getJourneyCode())
                .eq(CustomerJourneyStageEntity::getDelFlag, 0));
        if (stageCount != null && stageCount > 0) {
            throw new IllegalStateException(
                    "模板仍有 " + stageCount + " 个阶段，请先删除所有阶段再删模板");
        }
        // 软删除
        existing.setDelFlag(1);
        existing.setUpdateTime(LocalDateTime.now().format(FMT));
        templateMapper.updateById(existing);
        log.info("Soft-deleted journey template: id={} code={}", id, existing.getJourneyCode());
    }

    @Override
    public Map<String, Long> countStagesByTemplate() {
        Map<String, Long> result = new HashMap<>();
        try {
            // 简单实现：拉所有 active stages，分组统计（量不大）
            List<CustomerJourneyStageEntity> stages = stageMapper.selectList(
                    new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                            .eq(CustomerJourneyStageEntity::getDelFlag, 0)
                            .select(CustomerJourneyStageEntity::getJourneyCode)
            );
            for (CustomerJourneyStageEntity s : stages) {
                result.merge(s.getJourneyCode(), 1L, Long::sum);
            }
        } catch (Exception e) {
            log.warn("countStagesByTemplate failed: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public List<CustomerJourneyConfigEntity> getJourneyStageConfigs(String templateCode) {
        if (templateCode == null || templateCode.isBlank()) return new ArrayList<>();

        // 1) Pull stages for this template from customer_journey_stage
        List<CustomerJourneyStageEntity> stages = stageMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getJourneyCode, templateCode)
                        .eq(CustomerJourneyStageEntity::getDelFlag, 0)
                        .orderByAsc(CustomerJourneyStageEntity::getSortOrder)
        );
        if (stages.isEmpty()) return new ArrayList<>();

        // 2) Pull any saved overrides from JOURNEY_STAGE_CONFIG, keyed "{template}::{stage}"
        List<CustomerJourneyConfigEntity> overrides = configMapper.selectByGroup(STAGE_CFG_GROUP);
        Map<String, String> overrideMap = new HashMap<>();
        String prefix = templateCode + "::";
        for (CustomerJourneyConfigEntity o : overrides) {
            String key = o.getConfigKey();
            if (key != null && key.startsWith(prefix)) {
                overrideMap.put(key.substring(prefix.length()), o.getConfigValue());
            }
        }

        // 3) Build response items: configKey = stageCode (clean for the UI)
        List<CustomerJourneyConfigEntity> items = new ArrayList<>();
        for (CustomerJourneyStageEntity s : stages) {
            CustomerJourneyConfigEntity item = new CustomerJourneyConfigEntity();
            item.setConfigGroup(STAGE_CFG_GROUP);
            item.setConfigKey(s.getStageCode());
            item.setValueType("OPTION");
            item.setDisplayName(s.getStageName());
            item.setDescription(buildStageDescription(s));
            item.setSortOrder(s.getSortOrder() != null ? s.getSortOrder() : 0);
            item.setConfigValue(overrideMap.getOrDefault(s.getStageCode(), synthesizeStageJson(s)));
            items.add(item);
        }
        return items;
    }

    @Override
    public void saveJourneyStageConfigs(String templateCode, List<CustomerJourneyConfigEntity> items) {
        if (templateCode == null || templateCode.isBlank() || items == null || items.isEmpty()) return;
        String now = LocalDateTime.now().format(FMT);

        // Load existing overrides once
        Map<String, CustomerJourneyConfigEntity> existing = new HashMap<>();
        for (CustomerJourneyConfigEntity e : configMapper.selectByGroup(STAGE_CFG_GROUP)) {
            if (e.getConfigKey() != null && e.getConfigKey().startsWith(templateCode + "::")) {
                existing.put(e.getConfigKey(), e);
            }
        }

        for (CustomerJourneyConfigEntity item : items) {
            if (item.getConfigKey() == null) continue;
            String namespacedKey = templateCode + "::" + item.getConfigKey();
            CustomerJourneyConfigEntity row = existing.get(namespacedKey);
            if (row == null) {
                row = new CustomerJourneyConfigEntity();
                row.setConfigGroup(STAGE_CFG_GROUP);
                row.setConfigKey(namespacedKey);
                row.setValueType("OPTION");
                row.setDisplayName(item.getDisplayName() != null ? item.getDisplayName() : item.getConfigKey());
                row.setDescription(item.getDescription());
                row.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0);
                row.setCreateTime(now);
                row.setConfigValue(item.getConfigValue());
                row.setUpdateTime(now);
                configMapper.insert(row);
            } else {
                row.setConfigValue(item.getConfigValue());
                row.setUpdateTime(now);
                configMapper.updateById(row);
            }
            log.info("Stage config upserted: {}:{}", STAGE_CFG_GROUP, namespacedKey);
        }
    }

    /** Build the human-readable description shown under each stage field. */
    private String buildStageDescription(CustomerJourneyStageEntity s) {
        StringBuilder sb = new StringBuilder();
        sb.append("匹配触点：").append(s.getMatchTouchpoint() != null ? s.getMatchTouchpoint() : "（不限）");
        if (s.getMatchIntent() != null && !s.getMatchIntent().isBlank()) {
            sb.append("；意图：").append(s.getMatchIntent());
        }
        if (s.getTip() != null && !s.getTip().isBlank()) {
            sb.append("；建议：").append(s.getTip());
        }
        if (s.getAllowRollback() != null && s.getAllowRollback() == 1) {
            sb.append("（允许回退）");
        }
        return sb.toString();
    }

    /** Build default JSON config_value from the stage entity columns. */
    private String synthesizeStageJson(CustomerJourneyStageEntity s) {
        try {
            ObjectNode root = M.createObjectNode();
            root.put("color", s.getColor() != null ? s.getColor() : "#909399");
            root.put("icon", s.getIcon() != null ? s.getIcon() : "User");
            if (s.getMatchTouchpoint() != null && !s.getMatchTouchpoint().isBlank()) {
                ArrayNode arr = M.createArrayNode();
                for (String t : s.getMatchTouchpoint().split(",")) {
                    String trimmed = t.trim();
                    if (!trimmed.isEmpty()) arr.add(trimmed);
                }
                root.set("matchTouchpoint", arr);
            }
            if (s.getMatchIntent() != null && !s.getMatchIntent().isBlank()) {
                ArrayNode arr = M.createArrayNode();
                for (String t : s.getMatchIntent().split(",")) {
                    String trimmed = t.trim();
                    if (!trimmed.isEmpty()) arr.add(trimmed);
                }
                root.set("matchIntent", arr);
            }
            root.put("tip", s.getTip() != null ? s.getTip() : "");
            return M.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.warn("synthesizeStageJson failed for {}: {}", s.getStageCode(), e.getMessage());
            return "{}";
        }
    }

    // ========== Visual stage editor (v2) ==========

    @Override
    public List<CustomerJourneyStageEntity> listStagesByTemplate(String templateCode) {
        if (templateCode == null || templateCode.isBlank()) return new ArrayList<>();
        List<CustomerJourneyStageEntity> stages = stageMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getJourneyCode, templateCode)
                        .eq(CustomerJourneyStageEntity::getDelFlag, 0)
                        .orderByAsc(CustomerJourneyStageEntity::getSortOrder)
        );
        // 透传每阶段绑定的触发事件（驱动前端「触发事件」多选列）
        for (CustomerJourneyStageEntity stage : stages) {
            List<String> evs = ruleMapper.selectList(
                    new LambdaQueryWrapper<TouchpointGenerateRuleEntity>()
                            .eq(TouchpointGenerateRuleEntity::getStageCode, stage.getStageCode())
                            .eq(TouchpointGenerateRuleEntity::getDelFlag, 0)
                            .eq(TouchpointGenerateRuleEntity::getEnabled, 1)
            ).stream()
                    .map(TouchpointGenerateRuleEntity::getEventCode)
                    .distinct()
                    .collect(Collectors.toList());
            stage.setEventCodes(evs);
        }
        return stages;
    }

    @Override
    @Transactional
    public void saveStagesByTemplate(String templateCode, List<CustomerJourneyStageEntity> stages) {
        if (templateCode == null || templateCode.isBlank()) {
            throw new IllegalArgumentException("模板编码不能为空");
        }
        if (stages == null) stages = new ArrayList<>();
        String now = LocalDateTime.now().format(FMT);

        // 1) Validate: each stage must have a code, and codes must be unique within the batch.
        java.util.Set<String> codes = new java.util.HashSet<>();
        for (int i = 0; i < stages.size(); i++) {
            CustomerJourneyStageEntity s = stages.get(i);
            if (s == null) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 行为空");
            }
            if (s.getStageCode() == null || s.getStageCode().isBlank()) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 行阶段编码不能为空");
            }
            String code = s.getStageCode().trim();
            s.setStageCode(code);
            if (!codes.add(code)) {
                throw new IllegalArgumentException("阶段编码重复: " + code);
            }
            // Default sensible values so the UI never has to set them.
            if (s.getColor() == null || s.getColor().isBlank()) s.setColor("#909399");
            if (s.getIcon() == null || s.getIcon().isBlank()) s.setIcon("User");
            if (s.getSortOrder() == null) s.setSortOrder((i + 1) * 10);
            if (s.getAllowRollback() == null) s.setAllowRollback(0);
        }

        // 2) Load existing rows for this template.
        List<CustomerJourneyStageEntity> existing = stageMapper.selectList(
                new LambdaQueryWrapper<CustomerJourneyStageEntity>()
                        .eq(CustomerJourneyStageEntity::getJourneyCode, templateCode)
                        .eq(CustomerJourneyStageEntity::getDelFlag, 0)
        );
        Map<String, CustomerJourneyStageEntity> existingByCode = existing.stream()
                .collect(Collectors.toMap(CustomerJourneyStageEntity::getStageCode, s -> s, (a, b) -> a));

        // 3) Soft-delete rows whose codes are no longer present.
        for (CustomerJourneyStageEntity e : existing) {
            if (!codes.contains(e.getStageCode())) {
                e.setDelFlag(1);
                e.setUpdateTime(now);
                stageMapper.updateById(e);
                // 同步软删该阶段下的所有触发事件规则，避免孤儿规则
                softDeleteRulesByStage(e.getStageCode(), now);
                log.info("Stage soft-deleted (no longer in template): {}", e.getStageCode());
            }
        }

        // 4) Upsert: insert new, update existing in place (preserve id + createTime).
        for (CustomerJourneyStageEntity s : stages) {
            s.setJourneyCode(templateCode);
            s.setDelFlag(0);
            s.setUpdateTime(now);

            // matchTouchpoint / matchIntent are stored as comma-separated strings.
            // Trim and normalize empty arrays to null (so engine "unlimited" path triggers).
            if (s.getMatchTouchpoint() != null) {
                String normalized = java.util.Arrays.stream(s.getMatchTouchpoint().split(","))
                        .map(String::trim).filter(x -> !x.isEmpty())
                        .collect(Collectors.joining(","));
                s.setMatchTouchpoint(normalized.isEmpty() ? null : normalized);
            }
            if (s.getMatchIntent() != null) {
                String normalized = java.util.Arrays.stream(s.getMatchIntent().split(","))
                        .map(String::trim).filter(x -> !x.isEmpty())
                        .collect(Collectors.joining(","));
                s.setMatchIntent(normalized.isEmpty() ? null : normalized);
            }

            CustomerJourneyStageEntity old = existingByCode.get(s.getStageCode());
            if (old == null) {
                s.setId(null);
                if (s.getCreateTime() == null || s.getCreateTime().isBlank()) {
                    s.setCreateTime(now);
                }
                stageMapper.insert(s);
                log.info("Stage inserted: {} / {}", templateCode, s.getStageCode());
            } else {
                s.setId(old.getId());
                s.setCreateTime(old.getCreateTime());
                stageMapper.updateById(s);
                log.info("Stage updated: {} / {}", templateCode, s.getStageCode());
            }
            // 同步「触发事件」规则（业务事件 -> 该阶段），配置即生效
            syncStageEventRules(s, now);
        }
    }

    /**
     * 按阶段的 eventCodes 同步 touchpoint_generate_rule：
     * - 新增的事件 -> 建规则
     * - 保留的事件 -> 刷新 touchpoint/intent/启用
     * - 已移除的事件 -> 软删规则
     */
    private void syncStageEventRules(CustomerJourneyStageEntity stage, String now) {
        List<String> want = stage.getEventCodes() == null ? new ArrayList<>()
                : new ArrayList<>(new LinkedHashSet<>(stage.getEventCodes()));
        List<TouchpointGenerateRuleEntity> cur = ruleMapper.selectList(
                new LambdaQueryWrapper<TouchpointGenerateRuleEntity>()
                        .eq(TouchpointGenerateRuleEntity::getStageCode, stage.getStageCode())
                        .eq(TouchpointGenerateRuleEntity::getDelFlag, 0));
        Map<String, TouchpointGenerateRuleEntity> byEvent = cur.stream()
                .collect(Collectors.toMap(TouchpointGenerateRuleEntity::getEventCode, r -> r, (a, b) -> a));

        String tp = firstToken(stage.getMatchTouchpoint());
        String intent = firstToken(stage.getMatchIntent());

        for (String ev : want) {
            TouchpointGenerateRuleEntity ex = byEvent.get(ev);
            if (ex == null) {
                TouchpointGenerateRuleEntity r = new TouchpointGenerateRuleEntity();
                r.setEventCode(ev);
                r.setTouchpointType(tp);
                r.setIntent(intent);
                r.setChannel("SYSTEM");
                r.setStageCode(stage.getStageCode());
                r.setRuleName(ev + " → " + stage.getJourneyCode() + "/" + stage.getStageName());
                r.setConditionJson(null);
                r.setOnceOnly(0);
                r.setEnabled(1);
                r.setSortOrder(100);
                r.setDelFlag(0);
                r.setTenantId(null);
                r.setCreateTime(now);
                r.setUpdateTime(now);
                ruleMapper.insert(r);
                log.info("Trigger rule created: {} -> {}", ev, stage.getStageCode());
            } else {
                ex.setTouchpointType(tp);
                ex.setIntent(intent);
                ex.setStageCode(stage.getStageCode());
                ex.setEnabled(1);
                ex.setDelFlag(0);
                ex.setUpdateTime(now);
                ruleMapper.updateById(ex);
                byEvent.remove(ev);
            }
        }
        // 其余现存规则（want 中已无该事件）-> 软删
        for (TouchpointGenerateRuleEntity leftover : byEvent.values()) {
            leftover.setDelFlag(1);
            leftover.setUpdateTime(now);
            ruleMapper.updateById(leftover);
            log.info("Trigger rule soft-deleted: {} -> {}", leftover.getEventCode(), stage.getStageCode());
        }
    }

    /** 软删某阶段下的所有触发事件规则（阶段被移除时调用） */
    private void softDeleteRulesByStage(String stageCode, String now) {
        List<TouchpointGenerateRuleEntity> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<TouchpointGenerateRuleEntity>()
                        .eq(TouchpointGenerateRuleEntity::getStageCode, stageCode)
                        .eq(TouchpointGenerateRuleEntity::getDelFlag, 0));
        for (TouchpointGenerateRuleEntity r : rules) {
            r.setDelFlag(1);
            r.setUpdateTime(now);
            ruleMapper.updateById(r);
        }
    }

    /** 取逗号分隔串的第一个 token（与事件关联弹窗逻辑一致） */
    private String firstToken(String csv) {
        if (csv == null || csv.isBlank()) return null;
        String[] parts = csv.split(",");
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) return t;
        }
        return null;
    }
}
