package com.psi.system.controller;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.result.CommonResult;
import com.psi.system.entity.SysRoleMenu;
import com.psi.system.service.SysRoleMenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 角色菜单关联Controller
 * 
 * @author PSI
 * @version 1.0.0
 */
@RestController
@RequestMapping("/psi/admin/role-menu")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SysRoleMenuController {

    private final SysRoleMenuService sysRoleMenuService;
    private final BatchUtils batchUtils;

    public SysRoleMenuController(SysRoleMenuService sysRoleMenuService, BatchUtils batchUtils) {
        this.sysRoleMenuService = sysRoleMenuService;
        this.batchUtils = batchUtils;
    }

    /**
     * 查询角色菜单关联列表
     */
    @GetMapping("/list")
    public CommonResult<List<SysRoleMenu>> list(SysRoleMenu sysRoleMenu) {
        List<SysRoleMenu> list = sysRoleMenuService.list();
        return CommonResult.success(list);
    }

    /**
     * 查询角色菜单关联详细
     */
    @GetMapping("/{id}")
    public CommonResult<SysRoleMenu> getById(@PathVariable Long id) {
        SysRoleMenu roleMenu = sysRoleMenuService.getById(id);
        return roleMenu != null ? CommonResult.success(roleMenu) : CommonResult.fail("记录不存在");
    }

    /**
     * 新增角色菜单关联
     */
    @PostMapping
    public CommonResult<Void> save(@RequestBody SysRoleMenu sysRoleMenu) {
        return sysRoleMenuService.save(sysRoleMenu) ? CommonResult.success() : CommonResult.fail("保存失败");
    }

    /**
     * 修改角色菜单关联
     */
    @PutMapping("/{id}")
    public CommonResult<Void> update(@PathVariable Long id, @RequestBody SysRoleMenu sysRoleMenu) {
        sysRoleMenu.setId(id);
        return sysRoleMenuService.updateById(sysRoleMenu) ? CommonResult.success() : CommonResult.fail("更新失败");
    }

    /**
     * 删除角色菜单关联
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysRoleMenuService.removeById(id) ? CommonResult.success() : CommonResult.fail("删除失败");
    }

    /**
     * 批量删除角色菜单关联
     */
    @DeleteMapping("/batch")
    public CommonResult<Void> deleteBatch(@RequestBody Long[] ids) {
        return batchUtils.removeByIds(sysRoleMenuService, Arrays.asList(ids)) ? CommonResult.success() : CommonResult.fail("删除失败");
    }

    /**
     * 批量保存角色菜单关联
     */
    @PostMapping("/saveRoleMenus")
    public CommonResult<Void> saveRoleMenus(@RequestParam Long roleId, @RequestParam Long[] menuIds) {
        return sysRoleMenuService.saveRoleMenus(roleId, menuIds) ? CommonResult.success() : CommonResult.fail("保存失败");
    }
}