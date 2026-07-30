package com.psi.system.controller;

import com.psi.system.dto.PosConfigDTO;
import com.psi.system.dto.PosConfigQueryDTO;
import com.psi.system.dto.PosConfigSaveDTO;
import com.psi.system.service.PosConfigService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/admin/pos/config")
public class PosConfigController {

    private final PosConfigService posConfigService;

    public PosConfigController(PosConfigService posConfigService) {
        this.posConfigService = posConfigService;
    }

    @GetMapping("/{id}")
    public CommonResult<PosConfigDTO> getById(@PathVariable Long id) {
        return posConfigService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<PosConfigDTO> list(PosConfigQueryDTO queryDTO) {
        return posConfigService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<PosConfigDTO> save(@RequestBody PosConfigSaveDTO saveDTO) {
        return posConfigService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<PosConfigDTO> update(@PathVariable Long id, @RequestBody PosConfigSaveDTO saveDTO) {
        return posConfigService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return posConfigService.delete(id);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return posConfigService.updateStatus(id, status);
    }
}