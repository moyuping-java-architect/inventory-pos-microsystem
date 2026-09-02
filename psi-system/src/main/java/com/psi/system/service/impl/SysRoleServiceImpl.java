package com.psi.system.service.impl;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.system.dto.SysRoleDTO;
import com.psi.system.dto.SysRoleQueryDTO;
import com.psi.system.dto.SysRoleSaveDTO;
import com.psi.system.entity.SysRole;
import com.psi.system.mapper.SysRoleMapper;
import com.psi.system.service.SysRoleService;
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
 * 角色服务实现类
 * 继承 MyBatis-Plus ServiceImpl，提供角色数据访问能力
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final BatchUtils batchUtils;

    public SysRoleServiceImpl(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

    @Override
    public CommonResult<SysRoleDTO> getById(Long id) {
        SysRole role = baseMapper.selectById(id);
        if (role == null) {
            return CommonResult.fail("角色不存在");
        }
        return CommonResult.success(BeanUtils.convert(role, SysRoleDTO.class));
    }

    @Override
    public PageResult<SysRoleDTO> list(SysRoleQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getRoleName())) {
            wrapper.like(SysRole::getRoleName, queryDTO.getRoleName());
        }
        if (StringUtils.hasText(queryDTO.getRoleCode())) {
            wrapper.like(SysRole::getRoleCode, queryDTO.getRoleCode());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(SysRole::getCreateTime);

        IPage<SysRole> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.success(
            BeanUtils.convertList(page.getRecords(), SysRoleDTO.class),
            page.getTotal(),
            pageNum,
            pageSize
        );
    }

    @Override
    public CommonResult<SysRoleDTO> save(SysRoleSaveDTO saveDTO) {
        SysRole role = BeanUtils.convert(saveDTO, SysRole.class);
        super.save(role);
        return CommonResult.success(BeanUtils.convert(role, SysRoleDTO.class));
    }

    @Override
    public CommonResult<SysRoleDTO> update(Long id, SysRoleSaveDTO saveDTO) {
        SysRole role = super.getById(id);
        if (role == null) {
            return CommonResult.fail("角色不存在");
        }
        BeanUtils.copyProperties(saveDTO, role);
        super.updateById(role);
        return CommonResult.success(BeanUtils.convert(role, SysRoleDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        SysRole role = super.getById(id);
        if (role == null) {
            return CommonResult.fail("角色不存在");
        }
        super.removeById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SysRole role = super.getById(id);
        if (role == null) {
            return CommonResult.fail("角色不存在");
        }
        role.setStatus(status);
        super.updateById(role);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<List<SysRoleDTO>> batchSave(List<SysRoleSaveDTO> saveDTOList) {
        if (saveDTOList == null || saveDTOList.isEmpty()) {
            return CommonResult.success(new ArrayList<>());
        }

        List<SysRole> roles = saveDTOList.stream()
                .map(dto -> BeanUtils.convert(dto, SysRole.class))
                .toList();

        List<SysRole> insertList = roles.stream()
                .filter(role -> role.getId() == null)
                .toList();

        List<SysRole> updateList = roles.stream()
                .filter(role -> role.getId() != null)
                .toList();

        if (!insertList.isEmpty()) {
            batchUtils.saveBatch(this, insertList);
        }

        if (!updateList.isEmpty()) {
            batchUtils.updateBatchById(this, updateList);
        }

        return CommonResult.success(BeanUtils.convertList(roles, SysRoleDTO.class));
    }
}