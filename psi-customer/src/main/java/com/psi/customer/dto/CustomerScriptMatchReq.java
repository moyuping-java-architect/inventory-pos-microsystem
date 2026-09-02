package com.psi.customer.dto;

import lombok.Data;

/**
 * 客户话术匹配请求
 */
@Data
public class CustomerScriptMatchReq {

    /** 客户ID */
    private Long customerId;

    /** 限制返回条数 */
    private Integer limit = 10;

    /** 是否只返回已匹配（>=阈值） */
    private Boolean onlyMatched = false;
}
