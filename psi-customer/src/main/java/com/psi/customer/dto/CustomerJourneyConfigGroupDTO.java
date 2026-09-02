package com.psi.customer.dto;

import com.psi.customer.entity.CustomerJourneyConfigEntity;
import lombok.Data;

import java.util.List;

/**
 * Grouped config DTO for admin form rendering.
 * Each group becomes a section in the configuration page.
 */
@Data
public class CustomerJourneyConfigGroupDTO {

    /** Group code: CHURN_MODEL / MESSAGE_TEMPLATE / TOUCHPOINT_TYPE / etc. */
    private String group;

    /** Human-readable group name for section header */
    private String groupDisplayName;

    /** Config items in this group, sorted by sort_order */
    private List<CustomerJourneyConfigEntity> items;
}
