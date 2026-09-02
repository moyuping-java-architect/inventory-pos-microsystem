import request from '../../utils/request'

/**
 * 库存模块 API
 */
export const stockApi = {
  getCheckList: params => request.post('/psi/stock/check/list', params),
  getCheckById: id => request.get(`/psi/stock/check/${id}`),
  saveCheck: data => request.post('/psi/stock/check', data),
  updateCheck: (id, data) => request.put(`/psi/stock/check/${id}`, data),
  deleteCheck: id => request.delete(`/psi/stock/check/${id}`),

  getLossList: params => request.post('/psi/stock/loss/list', params),
  getLossById: id => request.get(`/psi/stock/loss/${id}`),
  saveLoss: data => request.post('/psi/stock/loss', data),
  updateLoss: (id, data) => request.put(`/psi/stock/loss/${id}`, data),
  deleteLoss: id => request.delete(`/psi/stock/loss/${id}`),

  getOverList: params => request.post('/psi/stock/over/list', params),
  getOverById: id => request.get(`/psi/stock/over/${id}`),
  saveOver: data => request.post('/psi/stock/over', data),
  updateOver: (id, data) => request.put(`/psi/stock/over/${id}`, data),
  deleteOver: id => request.delete(`/psi/stock/over/${id}`),

  getTransferList: params => request.post('/psi/stock/transfer/list', params),
  getTransferById: id => request.get(`/psi/stock/transfer/${id}`),
  saveTransfer: data => request.post('/psi/stock/transfer', data),
  updateTransfer: (id, data) => request.put(`/psi/stock/transfer/${id}`, data),
  deleteTransfer: id => request.delete(`/psi/stock/transfer/${id}`),

  getInventoryList: params => request.get('/psi/stock/inventory', { params }),
  getInventoryById: id => request.get(`/psi/stock/inventory/${id}`),
  // 按仓库查询库存商品（用于盘点单等需要带仓库过滤的商品选择器）
  getStockForSelector: params => request.post('/psi/stock/list', params)
}
