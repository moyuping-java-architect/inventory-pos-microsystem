import request from '../../utils/request'

/**
 * 采购模块 API
 */
export const purchaseApi = {
  getOrderList: params => request.get('/psi/purchase/order/list', { params }),
  getOrderById: id => request.get(`/psi/purchase/order/${id}`),
  saveOrder: data => request.post('/psi/purchase/order', data),
  updateOrder: (id, data) => request.put(`/psi/purchase/order/${id}`, data),
  deleteOrder: id => request.delete(`/psi/purchase/order/${id}`),

  getInList: params => request.get('/psi/purchase/in/list', { params }),
  getInById: id => request.get(`/psi/purchase/in/${id}`),
  saveIn: data => request.post('/psi/purchase/in', data),
  updateIn: (id, data) => request.put(`/psi/purchase/in/${id}`, data),
  deleteIn: id => request.delete(`/psi/purchase/in/${id}`),

  getReturnList: params => request.get('/psi/purchase/return/list', { params }),
  getReturnById: id => request.get(`/psi/purchase/return/${id}`),
  saveReturn: data => request.post('/psi/purchase/return', data),
  updateReturn: (id, data) => request.put(`/psi/purchase/return/${id}`, data),
  deleteReturn: id => request.delete(`/psi/purchase/return/${id}`),

  getSupplierList: params => request.get('/psi/purchase/supplier/list', { params }),
  getSupplierById: id => request.get(`/psi/purchase/supplier/${id}`),
  saveSupplier: data => request.post('/psi/purchase/supplier', data),
  updateSupplier: (id, data) => request.put(`/psi/purchase/supplier/${id}`, data),
  deleteSupplier: id => request.delete(`/psi/purchase/supplier/${id}`)
}
