package com.psi.system.controller;

import com.psi.system.dto.SysDeptDTO;
import com.psi.system.dto.SysDeptQueryDTO;
import com.psi.system.dto.SysDeptSaveDTO;
import com.psi.system.service.SysDeptService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 * 提供部门的 RESTful API 接口
 */
@RestController
@RequestMapping("/psi/admin/dept")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SysDeptController {

    private final SysDeptService sysDeptService;

    public SysDeptController(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    /**
     * 根据ID查询部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @GetMapping("/{id}")
    public CommonResult<SysDeptDTO> getById(@PathVariable Long id) {
        return sysDeptService.getById(id);
    }

    /**
     * 分页查询部门列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public PageResult<SysDeptDTO> list(SysDeptQueryDTO queryDTO) {
        return sysDeptService.list(queryDTO);
    }

    /**
     * 新增部门
     *
     * @param saveDTO 部门数据
     * @return 新增后的部门
     */
    @PostMapping
    public CommonResult<SysDeptDTO> save(@RequestBody SysDeptSaveDTO saveDTO) {
        return sysDeptService.save(saveDTO);
    }

    /**
     * 更新部门
     *
     * @param id      部门ID
     * @param saveDTO 部门数据
     * @return 更新后的部门
     */
    @PutMapping("/{id}")
    public CommonResult<SysDeptDTO> update(@PathVariable Long id, @RequestBody SysDeptSaveDTO saveDTO) {
        return sysDeptService.update(id, saveDTO);
    }

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysDeptService.delete(id);
    }

    /**
     * 更新部门状态
     *
     * @param id     部门ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return sysDeptService.updateStatus(id, status);
    }

    /**
     * 批量保存部门
     * 支持批量新增和批量更新
     *
     * @param saveDTOList 部门数据列表
     * @return 保存后的部门列表
     */
    @PostMapping("/batch")
    public CommonResult<List<SysDeptDTO>> batchSave(@RequestBody List<SysDeptSaveDTO> saveDTOList) {
        return sysDeptService.batchSave(saveDTOList);
    }
}