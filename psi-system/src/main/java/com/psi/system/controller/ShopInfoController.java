package com.psi.system.controller;

import com.psi.system.dto.ShopInfoDTO;
import com.psi.system.dto.ShopInfoQueryDTO;
import com.psi.system.dto.ShopInfoSaveDTO;
import com.psi.system.service.ShopInfoService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/admin/shop")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class ShopInfoController {

    private final ShopInfoService shopInfoService;

    public ShopInfoController(ShopInfoService shopInfoService) {
        this.shopInfoService = shopInfoService;
    }

    @GetMapping("/{id}")
    public CommonResult<ShopInfoDTO> getById(@PathVariable Long id) {
        return shopInfoService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<ShopInfoDTO> list(ShopInfoQueryDTO queryDTO) {
        return shopInfoService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<ShopInfoDTO> save(@RequestBody ShopInfoSaveDTO saveDTO) {
        return shopInfoService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<ShopInfoDTO> update(@PathVariable Long id, @RequestBody ShopInfoSaveDTO saveDTO) {
        return shopInfoService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return shopInfoService.delete(id);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return shopInfoService.updateStatus(id, status);
    }
}