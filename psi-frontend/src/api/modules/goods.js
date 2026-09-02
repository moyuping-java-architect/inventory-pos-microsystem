import request from '../../utils/request'

/**
 * 商品模块 API
 */
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
