package com.psi.system.service;

import com.psi.system.dto.SysDeptDTO;
import com.psi.system.dto.SysDeptQueryDTO;
import com.psi.system.dto.SysDeptSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

import java.util.List;

/**
 * 部门服务接口
 * 提供部门的增删改查及批量操作功能
 */
public interface SysDeptService {

    /**
     * 根据ID查询部门详情
     *
     * @param id 部门ID
     * @return 部门详情DTO
     */
    CommonResult<SysDeptDTO> getById(Long id);

    /**
     * 分页查询部门列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<SysDeptDTO> list(SysDeptQueryDTO queryDTO);

    /**
     * 保存部门（新增）
     *
     * @param saveDTO 部门保存DTO
     * @return 保存后的部门DTO
     */
    CommonResult<SysDeptDTO> save(SysDeptSaveDTO saveDTO);

    /**
     * 更新部门
     *
     * @param id      部门ID
     * @param saveDTO 部门保存DTO
     * @return 更新后的部门DTO
     */
    CommonResult<SysDeptDTO> update(Long id, SysDeptSaveDTO saveDTO);

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 操作结果
     */
    CommonResult<Void> delete(Long id);

    /**
     * 更新部门状态
     *
     * @param id     部门ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    CommonResult<Void> updateStatus(Long id, Integer status);

    /**
     * 批量保存部门
     * 支持批量新增和批量更新（根据ID是否为空判断）
     *
     * @param saveDTOList 部门保存DTO列表
     * @return 保存后的部门DTO列表
     */
    CommonResult<List<SysDeptDTO>> batchSave(List<SysDeptSaveDTO> saveDTOList);
}