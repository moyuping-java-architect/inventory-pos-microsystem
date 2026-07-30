package com.psi.system.controller;

import com.psi.system.dto.SysRoleDTO;
import com.psi.system.dto.SysRoleQueryDTO;
import com.psi.system.dto.SysRoleSaveDTO;
import com.psi.system.service.SysRoleService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 提供角色的 RESTful API 接口
 */
@RestController
@RequestMapping("/psi/admin/role")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    /**
     * 根据ID查询角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    public CommonResult<SysRoleDTO> getById(@PathVariable Long id) {
        return sysRoleService.getById(id);
    }

    /**
     * 分页查询角色列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public PageResult<SysRoleDTO> list(SysRoleQueryDTO queryDTO) {
        return sysRoleService.list(queryDTO);
    }

    /**
     * 新增角色
     *
     * @param saveDTO 角色数据
     * @return 新增后的角色
     */
    @PostMapping
    public CommonResult<SysRoleDTO> save(@RequestBody SysRoleSaveDTO saveDTO) {
        return sysRoleService.save(saveDTO);
    }

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param saveDTO 角色数据
     * @return 更新后的角色
     */
    @PutMapping("/{id}")
    public CommonResult<SysRoleDTO> update(@PathVariable Long id, @RequestBody SysRoleSaveDTO saveDTO) {
        return sysRoleService.update(id, saveDTO);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysRoleService.delete(id);
    }

    /**
     * 更新角色状态
     *
     * @param id     角色ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return sysRoleService.updateStatus(id, status);
    }

    /**
     * 批量保存角色
     * 支持批量新增和批量更新
     *
     * @param saveDTOList 角色数据列表
     * @return 保存后的角色列表
     */
    @PostMapping("/batch")
    public CommonResult<List<SysRoleDTO>> batchSave(@RequestBody List<SysRoleSaveDTO> saveDTOList) {
        return sysRoleService.batchSave(saveDTOList);
    }
}