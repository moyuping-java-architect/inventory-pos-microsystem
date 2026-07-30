import axios from 'axios'

const request = axios.create({
  timeout: 5000
})

request.interceptors.request.use(
  config => {
    if (!config.headers) {
      config.headers = {}
    }
    const locale = localStorage.getItem('locale') || 'zh-CN'
    config.headers['Accept-Language'] = locale
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    config.headers['X-Tenant-Id'] = localStorage.getItem('tenantId') || '1'
    config.headers['X-Shop-Id'] = localStorage.getItem('shopId') || '1'
    config.headers['X-Warehouse-Id'] = localStorage.getItem('warehouseId') || '1'
    config.headers['X-Update-User-Id'] = localStorage.getItem('userId') || '1'
    config.headers['X-Update-User-Name'] = localStorage.getItem('userName') || 'admin'
    config.headers['X-Role-Id'] = localStorage.getItem('roleId') || '1'
    config.headers['X-Role-Name'] = localStorage.getItem('roleName') || '管理员'
    config.headers['X-Permissions'] = localStorage.getItem('permissions') || '*'
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      console.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default request