import request from '../../utils/request'

/**
 * 收银模块 API
 */
export const cashierApi = {
  saveOrder: data => request.post('/psi/cashier/save', data),
  upload: () => request.post('/psi/cashier/upload'),
  health: () => request.get('/psi/cashier/health')
}
