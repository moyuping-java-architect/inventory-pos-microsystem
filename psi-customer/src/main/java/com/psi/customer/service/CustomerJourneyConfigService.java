package com.psi.customer.service;

import com.psi.customer.dto.JourneyTemplateOptionDTO;
import com.psi.customer.entity.CustomerJourneyConfigEntity;
import com.psi.customer.entity.CustomerJourneyStageEntity;
import com.psi.customer.entity.CustomerJourneyTemplateEntity;

import java.util.List;

/**
 * Zero-code configuration service for customer journey.
 * All business rules (churn thresholds, templates, channel rules) read from here.
 * The ServiceImpl calls typed getters instead of using hardcoded values.
 */
public interface CustomerJourneyConfigService {

    /**
     * Get a config value as double, falling back to default if missing/invalid.
     */
    double getDouble(String group, String key, double defaultValue);

    /**
     * Get a config value as int, falling back to default if missing/invalid.
     */
    int getInt(String group, String key, int defaultValue);

    /**
     * Get a config value as string, falling back to default if missing.
     */
    String getString(String group, String key, String defaultValue);

    /**
     * Get all config rows (for admin form rendering).
     */
    List<CustomerJourneyConfigEntity> getAll();

    /**
     * Get all config rows in a specific group.
     */
    List<CustomerJourneyConfigEntity> getByGroup(String group);

    /**
     * Get touchpoint type options (for dropdown rendering).
     */
    List<CustomerJourneyConfigEntity> getTouchpointTypes();

    /**
     * Batch update config values (admin saves the form).
     */
    void batchUpdate(List<CustomerJourneyConfigEntity> items);

    /**
     * Reset all config to seed defaults.
     */
    void resetToDefaults();

    // ========== Per-template journey stage config (UI editing surface) ==========
    // Stored under a separate config group "JOURNEY_STAGE_CONFIG" with namespaced
    // keys like "SALES_FUNNEL::STAGE_INTENTION" so they don't collide with the
    // engine's flat "JOURNEY_STAGES" group (which is read by loadStages() in
    // CustomerJourneyServiceImpl and would break if its key format changed).

    /**
     * List ALL journey templates (enabled + disabled) for the Config page list view.
     * Sorted by sortOrder then journeyCode.
     */
    List<JourneyTemplateOptionDTO> listJourneyTemplates();

    /**
     * Create a new journey template (basic metadata only; stages added later).
     * Returns the persisted entity including the generated id.
     */
    CustomerJourneyTemplateEntity createJourneyTemplate(CustomerJourneyTemplateEntity entity);

    /**
     * Update an existing journey template's metadata (name/subject/desc/icon/enabled).
     * Code is immutable.
     */
    void updateJourneyTemplate(Long id, CustomerJourneyTemplateEntity entity);

    /**
     * Delete a journey template. Refuses if any stage still references it.
     */
    void deleteJourneyTemplate(Long id);

    /**
     * Count stages per template, in a single query. Returns journey_code -> count.
     */
    java.util.Map<String, Long> countStagesByTemplate();

    /**
     * Dynamically build the stage config items for a given template.
     * Reads customer_journey_stage for the template, merges config_value from
     * JOURNEY_STAGE_CONFIG (key = "{templateCode}::{stageCode}"), and falls
     * back to a JSON synthesized from the stage entity columns.
     */
    List<CustomerJourneyConfigEntity> getJourneyStageConfigs(String templateCode);

    /**
     * Upsert each item's configValue into JOURNEY_STAGE_CONFIG under the
     * namespaced key "{templateCode}::{stageCode}".
     */
    void saveJourneyStageConfigs(String templateCode, List<CustomerJourneyConfigEntity> items);

    /**
     * List stages for a given template directly from customer_journey_stage.
     * Used by the visual stage editor (rows of fields instead of JSON blobs).
     */
    List<CustomerJourneyStageEntity> listStagesByTemplate(String templateCode);

    /**
     * Save a batch of stages for a given template.
     * Diff against existing rows: missing stage codes are soft-deleted;
     * new stage codes are inserted; existing stage codes are updated in place
     * (preserving id and createTime so customer_journey_state links survive).
     */
    void saveStagesByTemplate(String templateCode, List<CustomerJourneyStageEntity> stages);
}
