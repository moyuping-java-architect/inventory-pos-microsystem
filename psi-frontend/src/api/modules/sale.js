import request from '../../utils/request'

/**
 * 销售模块 API
 */
export const saleApi = {
  getOrderList: params => request.post('/psi/sale/order/list', params),
  getOrderById: id => request.get(`/psi/sale/order/${id}`),
  saveOrder: data => request.post('/psi/sale/order', data),
  updateOrder: (id, data) => request.put(`/psi/sale/order/${id}`, data),
  deleteOrder: id => request.delete(`/psi/sale/order/${id}`),
  updateOrderStatus: (id, status) => request.put(`/psi/sale/order/${id}/status/${status}`),

  getOutList: params => request.post('/psi/sale/out/list', params),
  getSelfUseOutList: params => request.post('/psi/sale/self-use-out/list', params),
  getSelfUseOutById: id => request.get(`/psi/sale/self-use-out/${id}`),
  getOutById: id => request.get(`/psi/sale/out/${id}`),
  saveOut: data => request.post('/psi/sale/out', data),
  updateOut: (id, data) => request.put(`/psi/sale/out/${id}`, data),
  deleteOut: id => request.delete(`/psi/sale/out/${id}`),

  getReturnList: params => request.post('/psi/sale/return/list', params),
  getReturnById: id => request.get(`/psi/sale/return/${id}`),
  saveReturn: data => request.post('/psi/sale/return', data),
  updateReturn: (id, data) => request.put(`/psi/sale/return/${id}`, data),
  deleteReturn: id => request.delete(`/psi/sale/return/${id}`),

  getCustomerList: params => request.post('/psi/sale/customer/list', params),
  getCustomerById: id => request.get(`/psi/sale/customer/${id}`),
  saveCustomer: data => request.post('/psi/sale/customer', data),
  updateCustomer: (id, data) => request.put(`/psi/sale/customer/${id}`, data),
  deleteCustomer: id => request.delete(`/psi/sale/customer/${id}`),

  getDebtList: params => request.post('/psi/sale/debt/list', params),
  getDebtById: id => request.get(`/psi/sale/debt/${id}`),
  getDebtByCustomerId: customerId => request.get(`/psi/sale/debt/customer/${customerId}`),
  savePayment: data => request.post('/psi/sale/payment', data),
  getPaymentList: params => request.post('/psi/sale/payment/list', params)
}
