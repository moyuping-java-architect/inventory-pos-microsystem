package com.psi.system.service.impl;

import com.psi.system.dto.SysUserDTO;
import com.psi.system.dto.SysUserQueryDTO;
import com.psi.system.dto.SysUserSaveDTO;
import com.psi.system.entity.SysUser;
import com.psi.system.mapper.SysUserMapper;
import com.psi.system.service.SysUserService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    public SysUserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public CommonResult<SysUserDTO> getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return CommonResult.fail("用户不存在");
        }
        return CommonResult.success(BeanUtils.convert(user, SysUserDTO.class));
    }

    @Override
    public PageResult<SysUserDTO> list(SysUserQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        IPage<SysUserDTO> page = sysUserMapper.selectUserPageWithDeptAndShop(
            new Page<>(pageNum, pageSize),
            queryDTO.getUsername(),
            queryDTO.getNickname(),
            queryDTO.getDeptId(),
            queryDTO.getStatus()
        );

        return PageResult.success(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CommonResult<SysUserDTO> save(SysUserSaveDTO saveDTO) {
        SysUser user = BeanUtils.convert(saveDTO, SysUser.class);
        sysUserMapper.insert(user);
        return CommonResult.success(BeanUtils.convert(user, SysUserDTO.class));
    }

    @Override
    public CommonResult<SysUserDTO> update(Long id, SysUserSaveDTO saveDTO) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return CommonResult.fail("用户不存在");
        }
        BeanUtils.copyProperties(saveDTO, user);
        sysUserMapper.updateById(user);
        return CommonResult.success(BeanUtils.convert(user, SysUserDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return CommonResult.fail("用户不存在");
        }
        sysUserMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return CommonResult.fail("用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
        return CommonResult.success();
    }
}