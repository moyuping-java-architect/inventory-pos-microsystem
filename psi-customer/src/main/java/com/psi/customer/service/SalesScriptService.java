package com.psi.customer.service;

import com.psi.common.result.CommonResult;
import com.psi.customer.dto.*;

import java.util.List;

/**
 * 话术库 * 销冠 服务接口（旅程触达计划版）
 * 核心：客户在某旅程节点停留第 N 天 + 命中标签 + 未发送过 -> 触达对应话术
 */
public interface SalesScriptService {

    // ========== 话术库 CRUD ==========

    /** 按节点分组查询话术列表 */
    CommonResult<List<SalesScriptLibraryDTO>> listScripts(String stageCode);

    CommonResult<SalesScriptLibraryDTO> getScript(Long id);

    CommonResult<Long> saveScript(SalesScriptLibraryDTO dto);

    CommonResult<Void> updateScript(SalesScriptLibraryDTO dto);

    CommonResult<Void> deleteScript(Long id);

    // ========== 客户触达 ==========

    /** 查询今日应触达的客户列表（按节点+天数+标签+未发送） */
    CommonResult<List<ScriptMatchCustomerDTO>> listMatchCustomers(Integer limit);

    /** 查询某客户当前可触达的话术列表 */
    CommonResult<List<ScriptMatchResultDTO>> matchScriptsForCustomer(Long customerId);

    /** 发送话术（记录发送日志，实现去重） */
    CommonResult<Void> sendScript(Long customerId, Long scriptId, Integer dayInStage);

    /** 查询某客户的发送记录 */
    CommonResult<List<CustomerScriptSendLogDTO>> listSendLogs(Long customerId);

    // ========== 标签 ==========

    CommonResult<List<CustomerTagDTO>> listTags();

    CommonResult<Void> saveCustomerTags(Long customerId, List<String> tagCodes);
}
