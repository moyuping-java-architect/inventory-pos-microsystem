package com.psi.customer.controller;

import com.psi.common.exception.BusinessException;
import com.psi.common.result.CommonResult;
import com.psi.customer.dto.CustomerJourneyConfigGroupDTO;
import com.psi.customer.dto.JourneyTemplateOptionDTO;
import com.psi.customer.entity.CustomerJourneyConfigEntity;
import com.psi.customer.entity.CustomerJourneyStageEntity;
import com.psi.customer.entity.CustomerJourneyTemplateEntity;
import com.psi.customer.service.CustomerJourneyConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Customer journey zero-code configuration controller.
 * Shop owners adjust churn thresholds, message templates, touchpoint types, etc.
 * through these endpoints — no code changes needed.
 */
@RestController
@RequestMapping("/psi/customer/journey/config")
@Tag(name = "客户旅程配置", description = "零代码可配置：流失阈值/消息模板/触点类型/渠道规则/建议动作/看板限制")
public class CustomerJourneyConfigController {

    private final CustomerJourneyConfigService configService;

    public CustomerJourneyConfigController(CustomerJourneyConfigService configService) {
        this.configService = configService;
    }

    /** Group display names for the admin form section headers */
    private static final Map<String, String> GROUP_NAMES = new LinkedHashMap<>();
    static {
        GROUP_NAMES.put("CHURN_MODEL", "Churn model thresholds");
        GROUP_NAMES.put("MESSAGE_TEMPLATE", "WhatsApp message templates");
        GROUP_NAMES.put("TOUCHPOINT_TYPE", "Touchpoint types");
        GROUP_NAMES.put("CHANNEL_RULE", "Channel selection rules");
        GROUP_NAMES.put("SUGGESTED_ACTION", "Suggested actions");
        GROUP_NAMES.put("DASHBOARD", "Dashboard display");
        GROUP_NAMES.put("JOURNEY_STAGES", "Journey stages");
    }

    /**
     * Groups the frontend should NOT render as flat config tabs. They are
     * either engine-internal (JOURNEY_STAGES — read by loadStages() using
     * flat keys) or the dedicated UI editing surface (JOURNEY_STAGE_CONFIG —
     * handled by /journey-templates + /journey-stages endpoints below).
     */
    private static final List<String> EXCLUDED_FROM_FLAT = List.of("JOURNEY_STAGE_CONFIG");

    @GetMapping
    @Operation(summary = "Get all config grouped by group (for admin form)")
    public CommonResult<List<CustomerJourneyConfigGroupDTO>> getAllGrouped() {
        List<CustomerJourneyConfigEntity> all = configService.getAll();

        // 按 configGroup 聚合（保持数据库返回顺序）
        Map<String, List<CustomerJourneyConfigEntity>> grouped = all.stream()
                .filter(e -> !EXCLUDED_FROM_FLAT.contains(e.getConfigGroup()))
                .collect(Collectors.groupingBy(
                        CustomerJourneyConfigEntity::getConfigGroup,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 按 GROUP_NAMES 定义的顺序返回，组内 items 按 sortOrder 升序
        List<CustomerJourneyConfigGroupDTO> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : GROUP_NAMES.entrySet()) {
            String groupCode = entry.getKey();
            List<CustomerJourneyConfigEntity> items = grouped.get(groupCode);

            // 特殊：JOURNEY_STAGES 在 flat 配置中没有条目（引擎独享），
            // 但前端需要识别这个 tab 来渲染模板选择器，所以保留一个空的占位。
            if ((items == null || items.isEmpty()) && !"JOURNEY_STAGES".equals(groupCode)) continue;

            if (items != null) {
                items.sort(Comparator.comparing(
                        CustomerJourneyConfigEntity::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));
            }

            CustomerJourneyConfigGroupDTO dto = new CustomerJourneyConfigGroupDTO();
            dto.setGroup(groupCode);
            dto.setGroupDisplayName(entry.getValue());
            dto.setItems(items == null ? new ArrayList<>() : items);
            result.add(dto);
        }
        return CommonResult.success(result);
    }

    @GetMapping("/{group}")
    @Operation(summary = "Get config items for a specific group")
    public CommonResult<List<CustomerJourneyConfigEntity>> getByGroup(@PathVariable String group) {
        return CommonResult.success(configService.getByGroup(group));
    }

    @GetMapping("/touchpoint-types")
    @Operation(summary = "Get touchpoint type options (for dropdown)")
    public CommonResult<List<CustomerJourneyConfigEntity>> getTouchpointTypes() {
        return CommonResult.success(configService.getTouchpointTypes());
    }

    @PutMapping
    @Operation(summary = "Batch update config values (admin saves form)")
    public CommonResult<Void> batchUpdate(@RequestBody List<CustomerJourneyConfigEntity> items) {
        configService.batchUpdate(items);
        return CommonResult.success("Config updated", null);
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset all config to seed defaults")
    public CommonResult<Void> resetToDefaults() {
        configService.resetToDefaults();
        return CommonResult.success("Config reset to defaults", null);
    }

    // ========== Per-template journey stage config (UI editing surface) ==========

    @GetMapping("/journey-templates")
    @Operation(summary = "List all journey templates (enabled+disabled) for the template list UI")
    public CommonResult<List<JourneyTemplateOptionDTO>> listJourneyTemplates() {
        return CommonResult.success(configService.listJourneyTemplates());
    }

    @PostMapping("/journey-templates")
    @Operation(summary = "Create a new journey template (basic metadata)")
    public CommonResult<CustomerJourneyTemplateEntity> createJourneyTemplate(
            @RequestBody CustomerJourneyTemplateEntity entity) {
        try {
            return CommonResult.success(configService.createJourneyTemplate(entity));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @PutMapping("/journey-templates/{id}")
    @Operation(summary = "Update an existing journey template (metadata; code is immutable)")
    public CommonResult<Void> updateJourneyTemplate(
            @PathVariable Long id,
            @RequestBody CustomerJourneyTemplateEntity patch) {
        try {
            configService.updateJourneyTemplate(id, patch);
            return CommonResult.success("Template updated", null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @DeleteMapping("/journey-templates/{id}")
    @Operation(summary = "Soft-delete a journey template (refuses if any stage exists)")
    public CommonResult<Void> deleteJourneyTemplate(@PathVariable Long id) {
        try {
            configService.deleteJourneyTemplate(id);
            return CommonResult.success("Template deleted", null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @GetMapping("/journey-stages")
    @Operation(summary = "Get stage config items for a given template (admin edits these)")
    public CommonResult<List<CustomerJourneyConfigEntity>> getJourneyStages(
            @RequestParam String templateCode) {
        return CommonResult.success(configService.getJourneyStageConfigs(templateCode));
    }

    @PutMapping("/journey-stages")
    @Operation(summary = "Upsert stage config values for a given template")
    public CommonResult<Void> saveJourneyStages(
            @RequestParam String templateCode,
            @RequestBody List<CustomerJourneyConfigEntity> items) {
        configService.saveJourneyStageConfigs(templateCode, items);
        return CommonResult.success("Stage config saved", null);
    }

    // ========== Visual stage editor (v2) — edits customer_journey_stage directly ==========

    @GetMapping("/journey-stages-edit")
    @Operation(summary = "List stages for a given template (visual editor)")
    public CommonResult<List<CustomerJourneyStageEntity>> listJourneyStagesEdit(
            @RequestParam String templateCode) {
        return CommonResult.success(configService.listStagesByTemplate(templateCode));
    }

    @PutMapping("/journey-stages-edit")
    @Operation(summary = "Upsert all stages for a given template (diff; missing codes soft-deleted)")
    public CommonResult<Void> saveJourneyStagesEdit(
            @RequestParam String templateCode,
            @RequestBody List<CustomerJourneyStageEntity> stages) {
        try {
            configService.saveStagesByTemplate(templateCode, stages);
            return CommonResult.success("Stages saved", null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
