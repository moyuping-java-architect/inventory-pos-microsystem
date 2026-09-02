package com.psi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psi.system.entity.SysRoleMenu;

/**
 * 角色菜单关联Service接口
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {
    
    /**
     * 根据角色ID删除关联记录
     * 
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    boolean deleteByRoleId(Long roleId);
    
    /**
     * 根据菜单ID删除关联记录
     * 
     * @param menuId 菜单ID
     * @return 是否删除成功
     */
    boolean deleteByMenuId(Long menuId);
    
    /**
     * 批量保存角色菜单关联
     * 
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否保存成功
     */
    boolean saveRoleMenus(Long roleId, Long[] menuIds);
}