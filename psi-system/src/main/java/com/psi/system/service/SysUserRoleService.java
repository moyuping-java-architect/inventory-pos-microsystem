package com.psi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psi.system.entity.SysUserRole;

/**
 * 用户角色关联Service接口
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface SysUserRoleService extends IService<SysUserRole> {
    
    /**
     * 根据用户ID删除关联记录
     * 
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteByUserId(Long userId);
    
    /**
     * 根据角色ID删除关联记录
     * 
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    boolean deleteByRoleId(Long roleId);
    
    /**
     * 批量保存用户角色关联
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否保存成功
     */
    boolean saveUserRoles(Long userId, Long[] roleIds);
}