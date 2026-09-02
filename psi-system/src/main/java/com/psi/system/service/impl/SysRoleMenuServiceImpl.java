package com.psi.system.service.impl;

import com.psi.common.mybatis.util.BatchUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.system.entity.SysRoleMenu;
import com.psi.system.mapper.SysRoleMenuMapper;
import com.psi.system.service.SysRoleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色菜单关联Service实现类
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    private final BatchUtils batchUtils;

    public SysRoleMenuServiceImpl(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

    @Override
    public boolean deleteByRoleId(Long roleId) {
        if (roleId == null) {
            return false;
        }
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenu::getRoleId, roleId);
        return remove(queryWrapper);
    }

    @Override
    public boolean deleteByMenuId(Long menuId) {
        if (menuId == null) {
            return false;
        }
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenu::getMenuId, menuId);
        return remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleMenus(Long roleId, Long[] menuIds) {
        if (roleId == null) {
            return false;
        }
        
        // 删除原有关联
        deleteByRoleId(roleId);
        
        // 如果没有菜单ID，直接返回
        if (menuIds == null || menuIds.length == 0) {
            return true;
        }
        
        // 批量插入新关联
        List<SysRoleMenu> roleMenus = Arrays.stream(menuIds)
                .filter(menuId -> menuId != null)
                .map(menuId -> {
                    SysRoleMenu roleMenu = new SysRoleMenu();
                    roleMenu.setRoleId(roleId);
                    roleMenu.setMenuId(menuId);
                    return roleMenu;
                })
                .collect(Collectors.toList());
        
        return batchUtils.saveBatch(this, roleMenus);
    }
}