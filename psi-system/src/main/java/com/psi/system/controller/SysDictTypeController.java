package com.psi.system.controller;

import com.psi.system.dto.SysDictTypeDTO;
import com.psi.system.dto.SysDictTypeQueryDTO;
import com.psi.system.dto.SysDictTypeSaveDTO;
import com.psi.system.service.SysDictTypeService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/admin/dict-type")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class SysDictTypeController {

    private final SysDictTypeService sysDictTypeService;

    public SysDictTypeController(SysDictTypeService sysDictTypeService) {
        this.sysDictTypeService = sysDictTypeService;
    }

    @GetMapping("/{id}")
    public CommonResult<SysDictTypeDTO> getById(@PathVariable Long id) {
        return sysDictTypeService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<SysDictTypeDTO> list(SysDictTypeQueryDTO queryDTO) {
        return sysDictTypeService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<SysDictTypeDTO> save(@RequestBody SysDictTypeSaveDTO saveDTO) {
        return sysDictTypeService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<SysDictTypeDTO> update(@PathVariable Long id, @RequestBody SysDictTypeSaveDTO saveDTO) {
        return sysDictTypeService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysDictTypeService.delete(id);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return sysDictTypeService.updateStatus(id, status);
    }
}