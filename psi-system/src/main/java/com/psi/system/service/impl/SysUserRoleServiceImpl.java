package com.psi.system.service.impl;

import com.psi.common.mybatis.util.BatchUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.system.entity.SysUserRole;
import com.psi.system.mapper.SysUserRoleMapper;
import com.psi.system.service.SysUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关联Service实现类
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    private final BatchUtils batchUtils;

    public SysUserRoleServiceImpl(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

    @Override
    public boolean deleteByUserId(Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        return remove(queryWrapper);
    }

    @Override
    public boolean deleteByRoleId(Long roleId) {
        if (roleId == null) {
            return false;
        }
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getRoleId, roleId);
        return remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserRoles(Long userId, Long[] roleIds) {
        if (userId == null) {
            return false;
        }
        
        // 删除原有关联
        deleteByUserId(userId);
        
        // 如果没有角色ID，直接返回
        if (roleIds == null || roleIds.length == 0) {
            return true;
        }
        
        // 批量插入新关联
        List<SysUserRole> userRoles = Arrays.stream(roleIds)
                .filter(roleId -> roleId != null)
                .map(roleId -> {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    return userRole;
                })
                .collect(Collectors.toList());
        
        return batchUtils.saveBatch(this, userRoles);
    }
}