import request from '../../utils/request'

/**
 * 单据草稿 API（通用）
 * 所有业务单据的草稿保存、提交审批、查询等操作统一使用此API
 */
export const docApi = {
  // 创建草稿
  createDraft: data => request.post('/psi/order/doc', data),
  // 更新草稿
  updateDraft: (id, data) => request.put(`/psi/order/doc/${id}`, data),
  // 根据ID查询
  getById: id => request.get(`/psi/order/doc/${id}`),
  // 根据单据编号查询
  getByDocNo: docNo => request.get(`/psi/order/doc/docNo/${docNo}`),
  // 分页查询
  getPage: params => request.get('/psi/order/doc/page', { params }),
  // 根据单据类型查询列表
  getByType: docType => request.get(`/psi/order/doc/type/${docType}`),
  // 根据状态查询
  getByStatus: status => request.get(`/psi/order/doc/status/${status}`),
  // 查询待审批列表
  getPendingApprove: () => request.get('/psi/order/doc/pending'),
  // 删除草稿
  deleteDraft: id => request.delete(`/psi/order/doc/${id}`),
  // 提交审批（草稿→待审批，同时发起工作流）
  submitDraft: id => request.post(`/psi/order/doc/${id}/submit`),
  // 创建并提交审批（新建单据直接提交审批+启动工作流）
  createAndSubmit: data => request.post('/psi/order/api/doc/submit', data),
  // 各业务类型独立的提交审批接口
  submitPurchaseOrder: data => request.post('/psi/order/api/doc/purchase-order/submit', data),
  submitPurchaseIn: data => request.post('/psi/order/api/doc/purchase-in/submit', data),
  submitPurchaseReturn: data => request.post('/psi/order/api/doc/purchase-return/submit', data),
  submitSaleOrder: data => request.post('/psi/order/api/doc/sale-order/submit', data),
  submitSaleOut: data => request.post('/psi/order/api/doc/sale-out/submit', data),
  submitSaleReturn: data => request.post('/psi/order/api/doc/sale-return/submit', data),
  submitStockCheck: data => request.post('/psi/order/api/doc/stock-check/submit', data),
  submitStockTransfer: data => request.post('/psi/order/api/doc/stock-transfer/submit', data),
  submitStockOverflow: data => request.post('/psi/order/api/doc/stock-overflow/submit', data),
  submitStockLoss: data => request.post('/psi/order/api/doc/stock-loss/submit', data),
  submitInventoryInit: data => request.post('/psi/order/api/doc/inventory-init/submit', data),
  submitAdjustPrice: data => request.post('/psi/order/api/doc/adjust-price/submit', data),
  // 审批通过
  approve: (id, approverId, approverName) => request.post(`/psi/order/doc/${id}/approve`, null, { params: { approverId, approverName } }),
  // 审批驳回
  reject: (id, approverId, remark) => request.post(`/psi/order/doc/${id}/reject`, null, { params: { approverId, remark } }),
  // 取消单据
  cancel: (id, remark) => request.post(`/psi/order/doc/${id}/cancel`, null, { params: { remark } }),
  // 完成单据
  complete: id => request.post(`/psi/order/doc/${id}/complete`),
  // 获取单据类型列表
  getDocTypes: () => request.get('/psi/order/doc/types'),
  // 获取单据状态列表
  getDocStatuses: () => request.get('/psi/order/doc/statuses')
}
