package com.psi.system.controller;

import com.psi.system.dto.SysUserDTO;
import com.psi.system.dto.SysUserQueryDTO;
import com.psi.system.dto.SysUserSaveDTO;
import com.psi.system.service.SysUserService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/admin/user")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/{id}")
    public CommonResult<SysUserDTO> getById(@PathVariable Long id) {
        return sysUserService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<SysUserDTO> list(SysUserQueryDTO queryDTO) {
        return sysUserService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<SysUserDTO> save(@RequestBody SysUserSaveDTO saveDTO) {
        return sysUserService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<SysUserDTO> update(@PathVariable Long id, @RequestBody SysUserSaveDTO saveDTO) {
        return sysUserService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysUserService.delete(id);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return sysUserService.updateStatus(id, status);
    }
}