package com.psi.system.controller;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.result.CommonResult;
import com.psi.system.entity.SysUserRole;
import com.psi.system.service.SysUserRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 用户角色关联Controller
 * 
 * @author PSI
 * @version 1.0.0
 */
@RestController
@RequestMapping("/psi/admin/user-role")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SysUserRoleController {

    private final SysUserRoleService sysUserRoleService;
    private final BatchUtils batchUtils;

    public SysUserRoleController(SysUserRoleService sysUserRoleService, BatchUtils batchUtils) {
        this.sysUserRoleService = sysUserRoleService;
        this.batchUtils = batchUtils;
    }

    /**
     * 查询用户角色关联列表
     */
    @GetMapping("/list")
    public CommonResult<List<SysUserRole>> list(SysUserRole sysUserRole) {
        List<SysUserRole> list = sysUserRoleService.list();
        return CommonResult.success(list);
    }

    /**
     * 查询用户角色关联详细
     */
    @GetMapping("/{id}")
    public CommonResult<SysUserRole> getById(@PathVariable Long id) {
        SysUserRole userRole = sysUserRoleService.getById(id);
        return userRole != null ? CommonResult.success(userRole) : CommonResult.fail("记录不存在");
    }

    /**
     * 新增用户角色关联
     */
    @PostMapping
    public CommonResult<Void> save(@RequestBody SysUserRole sysUserRole) {
        return sysUserRoleService.save(sysUserRole) ? CommonResult.success() : CommonResult.fail("保存失败");
    }

    /**
     * 修改用户角色关联
     */
    @PutMapping("/{id}")
    public CommonResult<Void> update(@PathVariable Long id, @RequestBody SysUserRole sysUserRole) {
        sysUserRole.setId(id);
        return sysUserRoleService.updateById(sysUserRole) ? CommonResult.success() : CommonResult.fail("更新失败");
    }

    /**
     * 删除用户角色关联
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysUserRoleService.removeById(id) ? CommonResult.success() : CommonResult.fail("删除失败");
    }

    /**
     * 批量删除用户角色关联
     */
    @DeleteMapping("/batch")
    public CommonResult<Void> deleteBatch(@RequestBody Long[] ids) {
        return batchUtils.removeByIds(sysUserRoleService, Arrays.asList(ids)) ? CommonResult.success() : CommonResult.fail("删除失败");
    }

    /**
     * 批量保存用户角色关联
     */
    @PostMapping("/saveUserRoles")
    public CommonResult<Void> saveUserRoles(@RequestParam Long userId, @RequestParam Long[] roleIds) {
        return sysUserRoleService.saveUserRoles(userId, roleIds) ? CommonResult.success() : CommonResult.fail("保存失败");
    }
}