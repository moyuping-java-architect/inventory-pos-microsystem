package com.psi.system.controller;

import com.psi.system.dto.PosOperatorDTO;
import com.psi.system.dto.PosOperatorQueryDTO;
import com.psi.system.dto.PosOperatorSaveDTO;
import com.psi.system.service.PosOperatorService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/admin/pos/operator")
public class PosOperatorController {

    private final PosOperatorService posOperatorService;

    public PosOperatorController(PosOperatorService posOperatorService) {
        this.posOperatorService = posOperatorService;
    }

    @GetMapping("/{id}")
    public CommonResult<PosOperatorDTO> getById(@PathVariable Long id) {
        return posOperatorService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<PosOperatorDTO> list(PosOperatorQueryDTO queryDTO) {
        return posOperatorService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<PosOperatorDTO> save(@RequestBody PosOperatorSaveDTO saveDTO) {
        return posOperatorService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<PosOperatorDTO> update(@PathVariable Long id, @RequestBody PosOperatorSaveDTO saveDTO) {
        return posOperatorService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return posOperatorService.delete(id);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return posOperatorService.updateStatus(id, status);
    }
}