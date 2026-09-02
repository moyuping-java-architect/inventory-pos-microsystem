package com.psi.system.service;

import com.psi.system.dto.SysMenuDTO;
import com.psi.system.dto.SysMenuQueryDTO;
import com.psi.system.dto.SysMenuSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

import java.util.List;

/**
 * 菜单服务接口
 * 提供菜单的增删改查及批量操作功能
 */
public interface SysMenuService {

    /**
     * 根据ID查询菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情DTO
     */
    CommonResult<SysMenuDTO> getById(Long id);

    /**
     * 分页查询菜单列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<SysMenuDTO> list(SysMenuQueryDTO queryDTO);

    /**
     * 保存菜单（新增）
     *
     * @param saveDTO 菜单保存DTO
     * @return 保存后的菜单DTO
     */
    CommonResult<SysMenuDTO> save(SysMenuSaveDTO saveDTO);

    /**
     * 更新菜单
     *
     * @param id      菜单ID
     * @param saveDTO 菜单保存DTO
     * @return 更新后的菜单DTO
     */
    CommonResult<SysMenuDTO> update(Long id, SysMenuSaveDTO saveDTO);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    CommonResult<Void> delete(Long id);

    /**
     * 更新菜单状态
     *
     * @param id     菜单ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    CommonResult<Void> updateStatus(Long id, Integer status);

    /**
     * 批量保存菜单
     * 支持批量新增和批量更新（根据ID是否为空判断）
     *
     * @param saveDTOList 菜单保存DTO列表
     * @return 保存后的菜单DTO列表
     */
    CommonResult<List<SysMenuDTO>> batchSave(List<SysMenuSaveDTO> saveDTOList);
}