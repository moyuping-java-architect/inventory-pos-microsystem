import request from '../../utils/request'

/**
 * 系统模块 API（仓库等基础数据）
 */
export const warehouseApi = {
  // 获取启用的仓库列表（已归一化为 { id, code, name, warehouseCode, warehouseName, ... }）
  getEnabledList: async () => {
    const res = await request.get('/psi/admin/warehouse/list', { params: { pageNum: 1, pageSize: 999, status: 1 } })
    return (res.list || []).map(w => ({
      ...w,
      code: w.warehouseCode,
      name: w.warehouseName
    }))
  }
}
