import request from '../utils/request'

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
  
  getSupplierList: params => request.get('/psi/purchase/supplier', { params }),
  getSupplierById: id => request.get(`/psi/purchase/supplier/${id}`),
  saveSupplier: data => request.post('/psi/purchase/supplier', data),
  updateSupplier: (id, data) => request.put(`/psi/purchase/supplier/${id}`, data),
  deleteSupplier: id => request.delete(`/psi/purchase/supplier/${id}`)
}

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

export const goodsApi = {
  // 商品
  getGoodsPage: params => request.get('/psi/goods/page', { params }),
  getGoodsList: () => request.get('/psi/goods/list'),
  getGoodsById: id => request.get(`/psi/goods/${id}`),
  saveGoods: data => request.post('/psi/goods', data),
  updateGoods: (id, data) => request.put(`/psi/goods/${id}`, data),
  deleteGoods: id => request.delete(`/psi/goods/${id}`),

  // 商品分类
  getCategoryPage: params => request.get('/psi/goods/category/page', { params }),
  getCategoryList: () => request.get('/psi/goods/category/list'),
  getCategoryById: id => request.get(`/psi/goods/category/${id}`),
  saveCategory: data => request.post('/psi/goods/category', data),
  updateCategory: (id, data) => request.put(`/psi/goods/category/${id}`, data),
  deleteCategory: id => request.delete(`/psi/goods/category/${id}`),

  // 商品品牌
  getBrandPage: params => request.get('/psi/goods/brand/page', { params }),
  getBrandList: () => request.get('/psi/goods/brand/list'),
  getBrandById: id => request.get(`/psi/goods/brand/${id}`),
  saveBrand: data => request.post('/psi/goods/brand', data),
  updateBrand: (id, data) => request.put(`/psi/goods/brand/${id}`, data),
  deleteBrand: id => request.delete(`/psi/goods/brand/${id}`),

  // 商品单位
  getUnitPage: params => request.get('/psi/goods/unit/page', { params }),
  getUnitList: () => request.get('/psi/goods/unit/list'),
  getUnitById: id => request.get(`/psi/goods/unit/${id}`),
  saveUnit: data => request.post('/psi/goods/unit', data),
  updateUnit: (id, data) => request.put(`/psi/goods/unit/${id}`, data),
  deleteUnit: id => request.delete(`/psi/goods/unit/${id}`),

  // SKU
  getSkuPage: params => request.get('/psi/goods/sku/page', { params }),
  getSkuList: params => request.get('/psi/goods/sku/list', { params }),
  getSkuById: id => request.get(`/psi/goods/sku/${id}`),
  getSkuByGoodsId: goodsId => request.get(`/psi/goods/sku/goods/${goodsId}`),
  saveSku: data => request.post('/psi/goods/sku', data),
  updateSku: (id, data) => request.put(`/psi/goods/sku/${id}`, data),
  deleteSku: id => request.delete(`/psi/goods/sku/${id}`),

  // SKU销售单位
  getSkuSaleUnitPage: params => request.get('/psi/goods/sku/sale-unit/page', { params }),
  getSkuSaleUnitBySkuId: skuId => request.get(`/psi/goods/sku/sale-unit/sku/${skuId}`),
  getSkuSaleUnitByUnifyCode: goodsUnifyCode => request.get(`/psi/goods/sku/sale-unit/unify-code/${goodsUnifyCode}`),
  saveSkuSaleUnit: data => request.post('/psi/goods/sku/sale-unit', data),
  updateSkuSaleUnit: data => request.put('/psi/goods/sku/sale-unit', data),
  deleteSkuSaleUnit: id => request.delete(`/psi/goods/sku/sale-unit/${id}`),
  setDefaultSkuSaleUnit: (skuId, saleUnitId) => request.put(`/psi/goods/sku/sale-unit/sku/${skuId}/default/${saleUnitId}`),

  // 商品选择器（按goods_unify_code聚合，用于销售单选择商品）
  getGoodsForSelector: params => request.get('/psi/goods/cashier/goods', { params }),
  // 获取商品销售单位列表
  getSaleUnits: goodsUnifyCode => request.get(`/psi/goods/cashier/sale-units/${goodsUnifyCode}`)
}

export const cashierApi = {
  saveOrder: data => request.post('/psi/cashier/save', data),
  upload: () => request.post('/psi/cashier/upload'),
  health: () => request.get('/psi/cashier/health')
}

export const financeApi = {
  getReceivableList: params => request.get('/psi/finance/receivable', { params }),
  getPayableList: params => request.get('/psi/finance/payable', { params }),
  
  getAccountList: params => request.get('/psi/finance/account', { params }),
  getAccountById: id => request.get(`/psi/finance/account/${id}`),
  saveAccount: data => request.post('/psi/finance/account', data),
  updateAccount: (id, data) => request.put(`/psi/finance/account/${id}`, data),
  deleteAccount: id => request.delete(`/psi/finance/account/${id}`),
  
  getDailyCloseList: params => request.get('/psi/finance/daily', { params }),
  getDailyCloseById: id => request.get(`/psi/finance/daily/${id}`),
  saveDailyClose: data => request.post('/psi/finance/daily', data),
  closeDaily: data => request.post('/psi/finance/daily/close', data),
  cancelDaily: id => request.post(`/psi/finance/daily/${id}/cancel`)
}

/**
 * 单据草稿API（通用）
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

/**
 * 工作流API
 */
export const workflowApi = {
  // 发起流程
  startProcess: data => request.post('/psi/workflow/start', data),
  // 查询待办任务
  getTodoList: userId => request.get(`/psi/workflow/todo?userId=${userId}`),
  // 审批任务
  approveTask: (taskId, data) => request.post(`/psi/workflow/task/${taskId}/approve`, data),
  // 驳回任务
  rejectTask: (taskId, data) => request.post(`/psi/workflow/task/${taskId}/reject`, data),
  // 查询流程实例
  getInstances: params => request.get('/psi/workflow/instance', { params }),
  // 查询流程实例详情
  getInstanceDetail: id => request.get(`/psi/workflow/instance/${id}/detail`),
  // 查询流程历史
  getHistory: params => request.get('/psi/workflow/history', { params }),
  // 查询流程定义
  getDefinitions: params => request.get('/psi/workflow/definition', { params }),
  // 查询流程定义详情（含节点、条件、关系）
  getDefinitionDetail: id => request.get(`/psi/workflow/definition/${id}/detail`),
  // 保存流程定义
  saveDefinition: data => request.post('/psi/workflow/definition', data),
  // 更新流程定义
  updateDefinition: (id, data) => request.put(`/psi/workflow/definition/${id}`, data),
  // 删除流程定义
  deleteDefinition: id => request.delete(`/psi/workflow/definition/${id}`),
  // 保存流程节点
  saveNode: data => request.post('/psi/workflow/node', data),
  // 更新流程节点
  updateNode: (id, data) => request.put(`/psi/workflow/node/${id}`, data),
  // 删除流程节点
  deleteNode: id => request.delete(`/psi/workflow/node/${id}`),
  // 保存节点关系
  saveRelation: data => request.post('/psi/workflow/relation', data),
  // 更新节点关系
  updateRelation: (id, data) => request.put(`/psi/workflow/relation/${id}`, data),
  // 删除节点关系
  deleteRelation: id => request.delete(`/psi/workflow/relation/${id}`),
  // 查询流程操作日志
  getOperationLogs: instanceId => request.get(`/psi/workflow/instance/${instanceId}/logs`),
  // 查询流程实例审批详情（含流程图、节点、任务、日志）
  getApprovalDetail: instanceId => request.get(`/psi/workflow/instance/${instanceId}/approval-detail`)
}