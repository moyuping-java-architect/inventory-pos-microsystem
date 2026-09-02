package com.psi.system.service;

import com.psi.system.dto.SysTenantDTO;
import com.psi.system.dto.SysTenantQueryDTO;
import com.psi.system.dto.SysTenantSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

import java.util.List;

/**
 * 租户服务接口
 * 提供租户的增删改查及批量操作功能
 */
public interface SysTenantService {

    /**
     * 根据ID查询租户详情
     *
     * @param id 租户ID
     * @return 租户详情DTO
     */
    CommonResult<SysTenantDTO> getById(Long id);

    /**
     * 分页查询租户列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<SysTenantDTO> list(SysTenantQueryDTO queryDTO);

    /**
     * 保存租户（新增）
     *
     * @param saveDTO 租户保存DTO
     * @return 保存后的租户DTO
     */
    CommonResult<SysTenantDTO> save(SysTenantSaveDTO saveDTO);

    /**
     * 更新租户
     *
     * @param id      租户ID
     * @param saveDTO 租户保存DTO
     * @return 更新后的租户DTO
     */
    CommonResult<SysTenantDTO> update(Long id, SysTenantSaveDTO saveDTO);

    /**
     * 删除租户
     *
     * @param id 租户ID
     * @return 操作结果
     */
    CommonResult<Void> delete(Long id);

    /**
     * 更新租户状态
     *
     * @param id     租户ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    CommonResult<Void> updateStatus(Long id, Integer status);

    /**
     * 批量保存租户
     * 支持批量新增和批量更新（根据ID是否为空判断）
     *
     * @param saveDTOList 租户保存DTO列表
     * @return 保存后的租户DTO列表
     */
    CommonResult<List<SysTenantDTO>> batchSave(List<SysTenantSaveDTO> saveDTOList);
}