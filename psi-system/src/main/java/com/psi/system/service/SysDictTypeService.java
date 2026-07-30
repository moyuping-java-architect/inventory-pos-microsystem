package com.psi.system.service;

import com.psi.system.dto.SysDictTypeDTO;
import com.psi.system.dto.SysDictTypeQueryDTO;
import com.psi.system.dto.SysDictTypeSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

import java.util.List;

/**
 * 字典类型服务接口
 * 提供字典类型的增删改查及批量操作功能
 */
public interface SysDictTypeService {

    /**
     * 根据ID查询字典类型详情
     *
     * @param id 字典类型ID
     * @return 字典类型详情DTO
     */
    CommonResult<SysDictTypeDTO> getById(Long id);

    /**
     * 分页查询字典类型列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<SysDictTypeDTO> list(SysDictTypeQueryDTO queryDTO);

    /**
     * 保存字典类型（新增）
     *
     * @param saveDTO 字典类型保存DTO
     * @return 保存后的字典类型DTO
     */
    CommonResult<SysDictTypeDTO> save(SysDictTypeSaveDTO saveDTO);

    /**
     * 更新字典类型
     *
     * @param id      字典类型ID
     * @param saveDTO 字典类型保存DTO
     * @return 更新后的字典类型DTO
     */
    CommonResult<SysDictTypeDTO> update(Long id, SysDictTypeSaveDTO saveDTO);

    /**
     * 删除字典类型
     *
     * @param id 字典类型ID
     * @return 操作结果
     */
    CommonResult<Void> delete(Long id);

    /**
     * 更新字典类型状态
     *
     * @param id     字典类型ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    CommonResult<Void> updateStatus(Long id, Integer status);

    /**
     * 批量保存字典类型
     * 支持批量新增和批量更新（根据ID是否为空判断）
     *
     * @param saveDTOList 字典类型保存DTO列表
     * @return 保存后的字典类型DTO列表
     */
    CommonResult<List<SysDictTypeDTO>> batchSave(List<SysDictTypeSaveDTO> saveDTOList);
}