package com.psi.system.controller;

import com.psi.system.dto.SysDictDataDTO;
import com.psi.system.dto.SysDictDataQueryDTO;
import com.psi.system.dto.SysDictDataSaveDTO;
import com.psi.system.service.SysDictDataService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据管理控制器
 * 提供字典数据的 RESTful API 接口
 */
@RestController
@RequestMapping("/psi/admin/dict-data")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SysDictDataController {

    private final SysDictDataService sysDictDataService;

    public SysDictDataController(SysDictDataService sysDictDataService) {
        this.sysDictDataService = sysDictDataService;
    }

    /**
     * 根据ID查询字典数据详情
     *
     * @param id 字典数据ID
     * @return 字典数据详情
     */
    @GetMapping("/{id}")
    public CommonResult<SysDictDataDTO> getById(@PathVariable Long id) {
        return sysDictDataService.getById(id);
    }

    /**
     * 分页查询字典数据列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public PageResult<SysDictDataDTO> list(SysDictDataQueryDTO queryDTO) {
        return sysDictDataService.list(queryDTO);
    }

    /**
     * 新增字典数据
     *
     * @param saveDTO 字典数据
     * @return 新增后的字典数据
     */
    @PostMapping
    public CommonResult<SysDictDataDTO> save(@RequestBody SysDictDataSaveDTO saveDTO) {
        return sysDictDataService.save(saveDTO);
    }

    /**
     * 更新字典数据
     *
     * @param id      字典数据ID
     * @param saveDTO 字典数据
     * @return 更新后的字典数据
     */
    @PutMapping("/{id}")
    public CommonResult<SysDictDataDTO> update(@PathVariable Long id, @RequestBody SysDictDataSaveDTO saveDTO) {
        return sysDictDataService.update(id, saveDTO);
    }

    /**
     * 删除字典数据
     *
     * @param id 字典数据ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysDictDataService.delete(id);
    }

    /**
     * 更新字典数据状态
     *
     * @param id     字典数据ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return sysDictDataService.updateStatus(id, status);
    }

    /**
     * 批量保存字典数据
     * 支持批量新增和批量更新
     *
     * @param saveDTOList 字典数据列表
     * @return 保存后的字典数据列表
     */
    @PostMapping("/batch")
    public CommonResult<List<SysDictDataDTO>> batchSave(@RequestBody List<SysDictDataSaveDTO> saveDTOList) {
        return sysDictDataService.batchSave(saveDTOList);
    }
}