import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { docApi, workflowApi } from '../api/index.js'

/**
 * 单据草稿管理组合式函数
 * 统一管理所有业务单据的草稿保存、更新、提交审批、查询等操作
 *
 * @param {Object} options - 配置项
 * @param {string} options.docType - 单据类型编码（如 PURCHASE_ORDER, SALE_ORDER 等）
 * @returns {Object} 草稿操作方法
 */
export function useDocDraft(options = {}) {
  const { docType } = options

  // 草稿列表数据
  const draftList = ref([])
  const total = ref(0)
  const pageSize = ref(10)
  const currentPage = ref(1)
  const loading = ref(false)

  // 草稿状态映射
  const draftStatusMap = {
    0: { label: '草稿', type: 'info' },
    1: { label: '已提交', type: 'warning' },
    2: { label: '审批中', type: 'warning' },
    3: { label: '已审批', type: 'primary' },
    4: { label: '执行中', type: 'primary' },
    5: { label: '已完成', type: 'success' },
    '-1': { label: '已取消', type: 'danger' },
    '-2': { label: '已驳回', type: 'danger' }
  }

  /**
   * 获取草稿列表
   */
  const fetchDraftList = async (params = {}) => {
    loading.value = true
    try {
      const queryParams = {
        pageNum: params.pageNum || currentPage.value,
        pageSize: params.pageSize || pageSize.value,
        docType: params.docType || docType,
        status: params.status,
        creatorId: params.creatorId,
        ...params
      }
      const res = await docApi.getPage(queryParams)
      if (res.code === 200) {
        draftList.value = res.data.records || res.data || []
        total.value = res.data.total || 0
      } else {
        // 降级：按类型查询
        const fallbackRes = await docApi.getByType(docType)
        if (fallbackRes.code === 200) {
          draftList.value = fallbackRes.data || []
          total.value = draftList.value.length
        }
      }
    } catch (error) {
      console.error('获取草稿列表失败:', error)
      draftList.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  /**
   * 保存草稿
   * @param {Object} formData - 表单数据（CreateDocRequest 格式）
   * @param {number|null} draftId - 草稿ID（更新时传入）
   * @returns {Object|null} 保存结果
   */
  const saveDraft = async (formData, draftId = null) => {
    try {
      const payload = {
        ...formData,
        docType: docType
      }
      let res
      if (draftId) {
        res = await docApi.updateDraft(draftId, payload)
      } else {
        res = await docApi.createDraft(payload)
      }
      if (res.code === 200) {
        ElMessage.success(draftId ? '草稿更新成功' : '草稿保存成功')
        return res.data
      } else {
        ElMessage.error(res.message || '保存草稿失败')
        return null
      }
    } catch (error) {
      console.error('保存草稿失败:', error)
      ElMessage.error('保存草稿失败: ' + (error.message || '网络错误'))
      return null
    }
  }

  /**
   * 提交审批（草稿→已提交，同时初始化工作流）
   * @param {number} id - 草稿ID
   * @param {Object} extraParams - 额外的提交参数
   * @returns {boolean} 是否成功
   */
  const submitDraft = async (id, extraParams = {}) => {
    try {
      const res = await docApi.submitDraft(id)
      if (res.code === 200) {
        ElMessage.success('单据已提交审批')
        // 尝试初始化工作流
        try {
          await workflowApi.startProcess({
            processCode: getWorkflowProcessCode(docType),
            businessKey: res.data?.docNo || String(id),
            title: getDocTypeLabel(docType) + (res.data?.docNo || ''),
            remark: extraParams.remark || '',
            initiator: localStorage.getItem('userId') || '1'
          })
        } catch (wfError) {
          console.warn('工作流初始化失败（不影响单据提交）:', wfError)
        }
        return true
      } else {
        ElMessage.error(res.message || '提交审批失败')
        return false
      }
    } catch (error) {
      console.error('提交审批失败:', error)
      ElMessage.error('提交审批失败: ' + (error.message || '网络错误'))
      return false
    }
  }

  /**
   * 删除草稿
   * @param {number} id - 草稿ID
   * @returns {boolean} 是否成功
   */
  const deleteDraft = async (id) => {
    try {
      await ElMessageBox.confirm('确定要删除该草稿吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const res = await docApi.deleteDraft(id)
      if (res.code === 200) {
        ElMessage.success('草稿已删除')
        await fetchDraftList()
        return true
      } else {
        ElMessage.error(res.message || '删除失败')
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除草稿失败:', error)
        ElMessage.error('删除失败: ' + (error.message || '网络错误'))
      }
      return false
    }
  }

  /**
   * 取消单据
   * @param {number} id - 单据ID
   * @param {string} reason - 取消原因
   * @returns {boolean} 是否成功
   */
  const cancelDraft = async (id, reason = '') => {
    try {
      await ElMessageBox.confirm('确定要取消该单据吗？', '确认取消', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const res = await docApi.cancel(id, reason)
      if (res.code === 200) {
        ElMessage.success('单据已取消')
        await fetchDraftList()
        return true
      } else {
        ElMessage.error(res.message || '取消失败')
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('取消单据失败:', error)
        ElMessage.error('取消失败: ' + (error.message || '网络错误'))
      }
      return false
    }
  }

  /**
   * 获取状态标签配置
   */
  const getStatusTag = (status) => {
    return draftStatusMap[String(status)] || { label: '未知', type: 'info' }
  }

  /**
   * 获取单据类型对应的流程编码
   */
  const getWorkflowProcessCode = (type) => {
    const map = {
      'PURCHASE_ORDER': 'purchase_approval',
      'PURCHASE_IN': 'purchase_approval',
      'PURCHASE_RETURN': 'purchase_approval',
      'SALE_ORDER': 'sale_approval',
      'SALE_OUT': 'sale_approval',
      'SALE_RETURN': 'sale_approval',
      'STOCK_LOSS': 'stock_adjust',
      'STOCK_OVERFLOW': 'stock_adjust',
      'STOCK_CHECK': 'stock_adjust',
      'STOCK_TRANSFER': 'stock_transfer',
      'INVENTORY_INIT': 'stock_adjust',
      'ADJUST_PRICE': 'stock_adjust'
    }
    return map[type] || 'common_approval'
  }

  /**
   * 获取单据类型中文标签
   */
  const getDocTypeLabel = (type) => {
    const map = {
      'PURCHASE_ORDER': '采购订单',
      'PURCHASE_IN': '采购入库单',
      'PURCHASE_RETURN': '采购退货单',
      'SALE_ORDER': '销售订单',
      'SALE_OUT': '销售出库单',
      'SALE_RETURN': '销售退货单',
      'STOCK_LOSS': '报损单',
      'STOCK_OVERFLOW': '报溢单',
      'STOCK_CHECK': '盘点单',
      'STOCK_TRANSFER': '调拨单',
      'INVENTORY_INIT': '库存初始化单',
      'ADJUST_PRICE': '调价单'
    }
    return map[type] || type
  }

  /**
   * 构建 CreateDocRequest 明细项目
   * 将前端表单的明细行转换为后端需要的格式
   */
  const buildItem = (item) => {
    return {
      goodsId: item.goodsId,
      goodsCode: item.goodsCode || '',
      skuCode: item.skuCode || '',
      skuName: item.skuName || item.goodsName || '',
      barcode: item.barcode || '',
      goodsName: item.goodsName || '',
      goodsSpec: item.spec || item.goodsSpec || '',
      unitCode: item.unitCode || '',
      goodsUnit: item.unit || item.goodsUnit || '件',
      conversionRate: Number(item.conversionRate || 1),
      unitPrice: item.price || item.unitPrice || 0,
      quantity: item.quantity || 1,
      taxRate: item.taxRate || 0,
      discountRate: item.discountRate || 0,
      discountAmount: item.discountAmount || 0,
      costPrice: item.costPrice || 0,
      stockId: item.stockId || null,
      batchNo: item.batchNo || '',
      expiryDate: item.expiryDate || '',
      remark: item.remark || '',
      lineNo: item.lineNo || 0
    }
  }

  return {
    // 响应式数据
    draftList,
    total,
    pageSize,
    currentPage,
    loading,
    draftStatusMap,

    // 方法
    fetchDraftList,
    saveDraft,
    submitDraft,
    deleteDraft,
    cancelDraft,
    getStatusTag,
    getDocTypeLabel,
    getWorkflowProcessCode,
    buildItem
  }
}