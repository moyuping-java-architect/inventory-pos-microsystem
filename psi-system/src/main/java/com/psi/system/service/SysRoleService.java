package com.psi.system.service;

import com.psi.system.dto.SysRoleDTO;
import com.psi.system.dto.SysRoleQueryDTO;
import com.psi.system.dto.SysRoleSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

import java.util.List;

/**
 * 角色服务接口
 * 提供角色的增删改查及批量操作功能
 */
public interface SysRoleService {

    /**
     * 根据ID查询角色详情
     *
     * @param id 角色ID
     * @return 角色详情DTO
     */
    CommonResult<SysRoleDTO> getById(Long id);

    /**
     * 分页查询角色列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<SysRoleDTO> list(SysRoleQueryDTO queryDTO);

    /**
     * 保存角色（新增）
     *
     * @param saveDTO 角色保存DTO
     * @return 保存后的角色DTO
     */
    CommonResult<SysRoleDTO> save(SysRoleSaveDTO saveDTO);

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param saveDTO 角色保存DTO
     * @return 更新后的角色DTO
     */
    CommonResult<SysRoleDTO> update(Long id, SysRoleSaveDTO saveDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    CommonResult<Void> delete(Long id);

    /**
     * 更新角色状态
     *
     * @param id     角色ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    CommonResult<Void> updateStatus(Long id, Integer status);

    /**
     * 批量保存角色
     * 支持批量新增和批量更新（根据ID是否为空判断）
     *
     * @param saveDTOList 角色保存DTO列表
     * @return 保存后的角色DTO列表
     */
    CommonResult<List<SysRoleDTO>> batchSave(List<SysRoleSaveDTO> saveDTOList);
}