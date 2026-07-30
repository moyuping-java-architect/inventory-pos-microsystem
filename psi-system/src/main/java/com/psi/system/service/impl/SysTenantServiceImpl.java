package com.psi.system.service.impl;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.system.dto.SysTenantDTO;
import com.psi.system.dto.SysTenantQueryDTO;
import com.psi.system.dto.SysTenantSaveDTO;
import com.psi.system.entity.SysTenant;
import com.psi.system.mapper.SysTenantMapper;
import com.psi.system.service.SysTenantService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 租户服务实现类
 * 继承 MyBatis-Plus ServiceImpl，提供租户数据访问能力
 */
@Service
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService {

    private final BatchUtils batchUtils;

    public SysTenantServiceImpl(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

    @Override
    public CommonResult<SysTenantDTO> getById(Long id) {
        SysTenant tenant = baseMapper.selectById(id);
        if (tenant == null) {
            return CommonResult.fail("租户不存在");
        }
        return CommonResult.success(BeanUtils.convert(tenant, SysTenantDTO.class));
    }

    @Override
    public PageResult<SysTenantDTO> list(SysTenantQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getTenantName())) {
            wrapper.like(SysTenant::getTenantName, queryDTO.getTenantName());
        }
        if (StringUtils.hasText(queryDTO.getTenantCode())) {
            wrapper.like(SysTenant::getTenantCode, queryDTO.getTenantCode());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysTenant::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(SysTenant::getCreateTime);

        IPage<SysTenant> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.success(
            BeanUtils.convertList(page.getRecords(), SysTenantDTO.class),
            page.getTotal(),
            pageNum,
            pageSize
        );
    }

    @Override
    public CommonResult<SysTenantDTO> save(SysTenantSaveDTO saveDTO) {
        SysTenant tenant = BeanUtils.convert(saveDTO, SysTenant.class);
        super.save(tenant);
        return CommonResult.success(BeanUtils.convert(tenant, SysTenantDTO.class));
    }

    @Override
    public CommonResult<SysTenantDTO> update(Long id, SysTenantSaveDTO saveDTO) {
        SysTenant tenant = super.getById(id);
        if (tenant == null) {
            return CommonResult.fail("租户不存在");
        }
        BeanUtils.copyProperties(saveDTO, tenant);
        super.updateById(tenant);
        return CommonResult.success(BeanUtils.convert(tenant, SysTenantDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        SysTenant tenant = super.getById(id);
        if (tenant == null) {
            return CommonResult.fail("租户不存在");
        }
        super.removeById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SysTenant tenant = super.getById(id);
        if (tenant == null) {
            return CommonResult.fail("租户不存在");
        }
        tenant.setStatus(status);
        super.updateById(tenant);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<List<SysTenantDTO>> batchSave(List<SysTenantSaveDTO> saveDTOList) {
        if (saveDTOList == null || saveDTOList.isEmpty()) {
            return CommonResult.success(new ArrayList<>());
        }

        List<SysTenant> tenants = saveDTOList.stream()
                .map(dto -> BeanUtils.convert(dto, SysTenant.class))
                .toList();

        List<SysTenant> insertList = tenants.stream()
                .filter(tenant -> tenant.getId() == null)
                .toList();

        List<SysTenant> updateList = tenants.stream()
                .filter(tenant -> tenant.getId() != null)
                .toList();

        if (!insertList.isEmpty()) {
            batchUtils.saveBatch(this, insertList);
        }

        if (!updateList.isEmpty()) {
            batchUtils.updateBatchById(this, updateList);
        }

        return CommonResult.success(BeanUtils.convertList(tenants, SysTenantDTO.class));
    }
}