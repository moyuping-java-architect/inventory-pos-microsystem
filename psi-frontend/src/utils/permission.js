/**
 * 前端权限控制工具
 *
 * 基于 localStorage 中的角色信息判断当前用户权限。
 * 后端 @PreAuthorize 是最终防线，前端控制仅用于 UI 展示。
 *
 * 角色体系：
 * - SUPER_ADMIN  超级管理员（最高权限）
 * - ADMIN        管理员（可配置定价、操作财务）
 * - CASHIER      收银员（仅收银+查询）
 * - STAFF        普通员工
 */

// 角色常量
export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  ADMIN: 'ADMIN',
  CASHIER: 'CASHIER',
  STAFF: 'STAFF'
}

// 需要管理员权限的角色
const ADMIN_ROLES = [ROLES.SUPER_ADMIN, ROLES.ADMIN]

/**
 * 获取当前用户角色编码
 * 优先用 roleCode，没有则从 roleName 推断
 */
export function getRoleCode() {
  const roleCode = localStorage.getItem('roleCode')
  if (roleCode) return roleCode

  const roleName = localStorage.getItem('roleName') || ''
  const lower = roleName.toLowerCase()
  if (lower.includes('super') || roleName.includes('超级')) return ROLES.SUPER_ADMIN
  if (lower.includes('admin') || roleName.includes('管理员')) return ROLES.ADMIN
  if (lower.includes('cashier') || roleName.includes('收银')) return ROLES.CASHIER
  return ROLES.STAFF
}

/**
 * 判断当前用户是否拥有指定角色
 * @param {string} role 角色编码
 */
export function hasRole(role) {
  return getRoleCode() === role
}

/**
 * 判断当前用户是否拥有任意一个指定角色
 * @param {...string} roles 角色编码列表
 */
export function hasAnyRole(...roles) {
  const current = getRoleCode()
  return roles.includes(current)
}

/**
 * 是否是管理员（SUPER_ADMIN 或 ADMIN）
 */
export function isAdmin() {
  return hasAnyRole(...ADMIN_ROLES)
}

/**
 * 是否是超级管理员
 */
export function isSuperAdmin() {
  return hasRole(ROLES.SUPER_ADMIN)
}

/**
 * 检查路由是否允许当前用户访问
 * @param {object} to 路由对象
 * @returns {boolean}
 */
export function checkRoutePermission(to) {
  if (!to.meta || !to.meta.roles || to.meta.roles.length === 0) {
    return true // 没有配置 roles 限制，所有人可访问
  }
  return to.meta.roles.includes(getRoleCode())
}
