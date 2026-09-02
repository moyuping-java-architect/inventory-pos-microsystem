/**
 * v-role 指令 — 基于角色控制元素显示/隐藏
 *
 * 用法：
 *   <el-button v-role="['SUPER_ADMIN', 'ADMIN']">管理员可见</el-button>
 *   <el-button v-role="'SUPER_ADMIN'">仅超管可见</el-button>
 *
 * 不满足条件的元素会被移除（display:none 会被 inspect 看到但无安全风险，
 * 因为后端 @PreAuthorize 才是最终防线）
 */
import { hasAnyRole } from '../utils/permission'

export const roleDirective = {
  mounted(el, binding) {
    const roles = Array.isArray(binding.value) ? binding.value : [binding.value]
    if (!hasAnyRole(...roles)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

export function setupRoleDirective(app) {
  app.directive('role', roleDirective)
}
