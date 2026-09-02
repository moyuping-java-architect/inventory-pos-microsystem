package com.psi.system.service;

import com.psi.system.dto.SysDictDataDTO;
import com.psi.system.dto.SysDictDataQueryDTO;
import com.psi.system.dto.SysDictDataSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

import java.util.List;

/**
 * 字典数据服务接口
 * 提供字典数据的增删改查及批量操作功能
 */
public interface SysDictDataService {

    /**
     * 根据ID查询字典数据详情
     *
     * @param id 字典数据ID
     * @return 字典数据详情DTO
     */
    CommonResult<SysDictDataDTO> getById(Long id);

    /**
     * 分页查询字典数据列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<SysDictDataDTO> list(SysDictDataQueryDTO queryDTO);

    /**
     * 保存字典数据（新增）
     *
     * @param saveDTO 字典数据保存DTO
     * @return 保存后的字典数据DTO
     */
    CommonResult<SysDictDataDTO> save(SysDictDataSaveDTO saveDTO);

    /**
     * 更新字典数据
     *
     * @param id      字典数据ID
     * @param saveDTO 字典数据保存DTO
     * @return 更新后的字典数据DTO
     */
    CommonResult<SysDictDataDTO> update(Long id, SysDictDataSaveDTO saveDTO);

    /**
     * 删除字典数据
     *
     * @param id 字典数据ID
     * @return 操作结果
     */
    CommonResult<Void> delete(Long id);

    /**
     * 更新字典数据状态
     *
     * @param id     字典数据ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    CommonResult<Void> updateStatus(Long id, Integer status);

    /**
     * 批量保存字典数据
     * 支持批量新增和批量更新（根据ID是否为空判断）
     *
     * @param saveDTOList 字典数据保存DTO列表
     * @return 保存后的字典数据DTO列表
     */
    CommonResult<List<SysDictDataDTO>> batchSave(List<SysDictDataSaveDTO> saveDTOList);
}