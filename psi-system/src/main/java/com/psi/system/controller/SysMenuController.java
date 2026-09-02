package com.psi.system.controller;

import com.psi.system.dto.SysMenuDTO;
import com.psi.system.dto.SysMenuQueryDTO;
import com.psi.system.dto.SysMenuSaveDTO;
import com.psi.system.service.SysMenuService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 * 提供菜单的 RESTful API 接口
 */
@RestController
@RequestMapping("/psi/admin/menu")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    public SysMenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    /**
     * 根据ID查询菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{id}")
    public CommonResult<SysMenuDTO> getById(@PathVariable Long id) {
        return sysMenuService.getById(id);
    }

    /**
     * 分页查询菜单列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public PageResult<SysMenuDTO> list(SysMenuQueryDTO queryDTO) {
        return sysMenuService.list(queryDTO);
    }

    /**
     * 新增菜单
     *
     * @param saveDTO 菜单数据
     * @return 新增后的菜单
     */
    @PostMapping
    public CommonResult<SysMenuDTO> save(@RequestBody SysMenuSaveDTO saveDTO) {
        return sysMenuService.save(saveDTO);
    }

    /**
     * 更新菜单
     *
     * @param id      菜单ID
     * @param saveDTO 菜单数据
     * @return 更新后的菜单
     */
    @PutMapping("/{id}")
    public CommonResult<SysMenuDTO> update(@PathVariable Long id, @RequestBody SysMenuSaveDTO saveDTO) {
        return sysMenuService.update(id, saveDTO);
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysMenuService.delete(id);
    }

    /**
     * 更新菜单状态
     *
     * @param id     菜单ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return sysMenuService.updateStatus(id, status);
    }

    /**
     * 批量保存菜单
     * 支持批量新增和批量更新
     *
     * @param saveDTOList 菜单数据列表
     * @return 保存后的菜单列表
     */
    @PostMapping("/batch")
    public CommonResult<List<SysMenuDTO>> batchSave(@RequestBody List<SysMenuSaveDTO> saveDTOList) {
        return sysMenuService.batchSave(saveDTOList);
    }
}