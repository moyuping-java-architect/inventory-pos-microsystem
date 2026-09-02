package com.psi.system.controller;

import com.psi.system.dto.WarehouseInfoDTO;
import com.psi.system.dto.WarehouseInfoQueryDTO;
import com.psi.system.dto.WarehouseInfoSaveDTO;
import com.psi.system.service.WarehouseInfoService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/admin/warehouse")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class WarehouseInfoController {

    private final WarehouseInfoService warehouseInfoService;

    public WarehouseInfoController(WarehouseInfoService warehouseInfoService) {
        this.warehouseInfoService = warehouseInfoService;
    }

    @GetMapping("/{id}")
    public CommonResult<WarehouseInfoDTO> getById(@PathVariable Long id) {
        return warehouseInfoService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<WarehouseInfoDTO> list(WarehouseInfoQueryDTO queryDTO) {
        return warehouseInfoService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<WarehouseInfoDTO> save(@RequestBody WarehouseInfoSaveDTO saveDTO) {
        return warehouseInfoService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<WarehouseInfoDTO> update(@PathVariable Long id, @RequestBody WarehouseInfoSaveDTO saveDTO) {
        return warehouseInfoService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return warehouseInfoService.delete(id);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return warehouseInfoService.updateStatus(id, status);
    }
}