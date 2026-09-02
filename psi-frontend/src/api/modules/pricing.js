import request from '../../utils/request'

/**
 * 定价引擎 API
 *
 * 后端接口路径: /psi/cashier/pricing
 * 权限: 配置和计算需要 ADMIN 以上角色，快速试算无限制
 */
export const pricingApi = {
  /** 获取当前定价配置 */
  getConfig: () => request.get('/psi/cashier/pricing/config'),

  /** 保存/更新定价配置（仅管理员） */
  saveConfig: data => request.post('/psi/cashier/pricing/config', data),

  /** ZMW采购价 -> 完整价格链（仅管理员） */
  calcFromZmw: zmwAmount =>
    request.post('/psi/cashier/pricing/calc/zmw', null, { params: { zmwAmount } }),

  /** USD采购价 -> 完整价格链（仅管理员） */
  calcFromUsd: usdAmount =>
    request.post('/psi/cashier/pricing/calc/usd', null, { params: { usdAmount } }),

  /** 快速试算（无权限限制，返回ZMW建议零售价） */
  quickCalc: zmwAmount =>
    request.get('/psi/cashier/pricing/calc/quick', { params: { zmwAmount } }),

  /** 计算汇率利润（仅管理员） */
  calcFxProfit: usdSalePrice =>
    request.get('/psi/cashier/pricing/calc/fx-profit', { params: { usdSalePrice } })
}
