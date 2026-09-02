import request from '../../utils/request'

/**
 * 财务模块 API
 */
export const financeApi = {
  getReceivableList: data => request.post('/psi/finance/receivable/list', data),
  getReceivableById: id => request.get(`/psi/finance/receivable/${id}`),
  payReceivable: (id, data) => request.post(`/psi/finance/receivable/${id}/pay`, data),

  getPayableList: data => request.post('/psi/finance/payable/list', data),
  getPayableById: id => request.get(`/psi/finance/payable/${id}`),
  payPayable: (id, data) => request.post(`/psi/finance/payable/${id}/pay`, data),

  getAccountList: () => request.get('/psi/finance/account/list'),
  getAccountById: id => request.get(`/psi/finance/account/${id}`),

  getDailyCloseList: data => request.post('/psi/finance/close/list', data),
  getDailyCloseById: id => request.get(`/psi/finance/close/${id}`),
  getDailyCloseByDate: params => request.get('/psi/finance/close/query', { params }),
  closeDaily: params => request.post('/psi/finance/close', null, { params }),
  reopenDaily: params => request.post('/psi/finance/close/reopen', null, { params })
}
