package com.psi.system.controller;

import com.psi.system.dto.SysTenantDTO;
import com.psi.system.dto.SysTenantQueryDTO;
import com.psi.system.dto.SysTenantSaveDTO;
import com.psi.system.service.SysTenantService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理控制器
 * 提供租户的 RESTful API 接口
 */
@RestController
@RequestMapping("/psi/admin/tenant")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SysTenantController {

    private final SysTenantService sysTenantService;

    public SysTenantController(SysTenantService sysTenantService) {
        this.sysTenantService = sysTenantService;
    }

    /**
     * 根据ID查询租户详情
     *
     * @param id 租户ID
     * @return 租户详情
     */
    @GetMapping("/{id}")
    public CommonResult<SysTenantDTO> getById(@PathVariable Long id) {
        return sysTenantService.getById(id);
    }

    /**
     * 分页查询租户列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public PageResult<SysTenantDTO> list(SysTenantQueryDTO queryDTO) {
        return sysTenantService.list(queryDTO);
    }

    /**
     * 新增租户
     *
     * @param saveDTO 租户数据
     * @return 新增后的租户
     */
    @PostMapping
    public CommonResult<SysTenantDTO> save(@RequestBody SysTenantSaveDTO saveDTO) {
        return sysTenantService.save(saveDTO);
    }

    /**
     * 更新租户
     *
     * @param id      租户ID
     * @param saveDTO 租户数据
     * @return 更新后的租户
     */
    @PutMapping("/{id}")
    public CommonResult<SysTenantDTO> update(@PathVariable Long id, @RequestBody SysTenantSaveDTO saveDTO) {
        return sysTenantService.update(id, saveDTO);
    }

    /**
     * 删除租户
     *
     * @param id 租户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return sysTenantService.delete(id);
    }

    /**
     * 更新租户状态
     *
     * @param id     租户ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return sysTenantService.updateStatus(id, status);
    }

    /**
     * 批量保存租户
     * 支持批量新增和批量更新
     *
     * @param saveDTOList 租户数据列表
     * @return 保存后的租户列表
     */
    @PostMapping("/batch")
    public CommonResult<List<SysTenantDTO>> batchSave(@RequestBody List<SysTenantSaveDTO> saveDTOList) {
        return sysTenantService.batchSave(saveDTOList);
    }
}