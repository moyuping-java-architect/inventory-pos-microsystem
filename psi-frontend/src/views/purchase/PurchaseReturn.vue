<template>
  <div class="purchase-return">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="退货单号">
          <el-input v-model="searchForm.returnNo" placeholder="请输入退货单号" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}退货</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="docNo" label="退货单号" />
        <el-table-column prop="docName" label="单据名称" />
        <el-table-column prop="partnerName" label="供应商" />
        <el-table-column prop="warehouseName" label="退货仓库" />
        <el-table-column prop="totalAmount" label="退货金额" />
        <el-table-column prop="itemCount" :label="t('table.itemCount')" />
        <el-table-column prop="status" :label="t('common.status')">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column prop="creatorName" :label="t('table.creator')" />
        <el-table-column :label="t('common.actions')" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">{{ t('common.view') }}</el-button>
            <el-button v-if="activeTab !== 'formal'" size="small" type="primary" @click="handleEdit(scope.row)">{{ t('common.edit') }}</el-button>
            <el-button v-if="activeTab !== 'formal'" size="small" type="danger" @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>
    
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="1200px" destroy-on-close>
      <el-form :model="formData" label-width="100px">
        <el-form-item label="退货单号" :disabled="isEdit">
          <el-input v-model="formData.returnNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="单据名称" required>
          <el-input v-model="formData.docName" placeholder="请输入单据名称" :disabled="isViewing" />
        </el-form-item>
        <el-form-item label="关联采购订单" v-if="isViewing && formData.orderNo">
          <el-input :value="formData.orderNo" disabled />
        </el-form-item>
        <el-form-item label="关联采购订单" v-else required>
          <div style="display:flex;gap:8px">
            <el-date-picker
              v-model="orderDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width:260px"
              :disabled="isViewing"
              @change="onOrderDateChange"
            />
            <el-select 
              v-model="formData.orderId" 
              placeholder="请输入采购单号搜索" 
              :disabled="isViewing" 
              filterable
              remote
              :remote-method="searchOrders"
              :loading="ordersLoading"
              clearable
              style="flex:1"
              @change="onOrderChange"
            >
              <el-option v-for="order in orders" :key="order.id" :label="order.orderNo" :value="order.id">
                <span>{{ order.orderNo }}</span>
                <span style="color:#8492a6;font-size:13px;margin-left:8px">{{ order.supplierName }}</span>
              </el-option>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input :value="isViewing ? (formData.supplierName || formData.partnerName || '') : selectedSupplierName" disabled />
        </el-form-item>
        <el-form-item label="退货仓库" v-if="isViewing && formData.warehouseName">
          <el-input :value="formData.warehouseName" disabled />
        </el-form-item>
        <el-form-item label="退货仓库" v-else>
          <el-select v-model="formData.warehouseId" placeholder="请选择退货仓库" :disabled="isViewing">
            <el-option v-for="warehouse in warehouses" :key="warehouse.id" :label="warehouse.name" :value="warehouse.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="退货明细">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsCode" label="商品编码" width="100" />
              <el-table-column prop="barcode" label="条码" width="100" />
              <el-table-column prop="goodsName" label="商品名称" min-width="120" />
              <el-table-column prop="goodsSpec" label="规格" width="80" />
              <el-table-column prop="goodsUnit" label="单位" width="60" />
              <el-table-column prop="quantity" label="退货数量" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" :max="scope.row.maxQty" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column prop="returnedQty" label="已退数量" width="80">
                <template #default="scope">{{ scope.row.returnedQty || 0 }}</template>
              </el-table-column>
              <el-table-column prop="unitPrice" label="单价" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column label="金额" width="90">
                <template #default="scope">{{ formatAmount(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="reason" label="退货原因" width="120">
                <template #default="scope">
                  <el-input v-model="scope.row.reason" size="small" :disabled="isViewing" />
                </template>
              </el-table-column>
              <el-table-column :label="t('common.actions')" width="55" fixed="right">
                <template #default="scope">
                  <el-button v-if="!isViewing" size="small" type="danger" @click="removeItem(scope.$index)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-textarea v-model="formData.remark" rows="3" :disabled="isViewing" />
        </el-form-item>
        <el-form-item label="退货总金额">
          <span style="font-weight:bold;font-size:16px">{{ formatAmount(totalAmount) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isViewing ? t('common.close') : t('common.cancel') }}</el-button>
        <el-button v-if="!isViewing" type="primary" @click="handleSave" :loading="saving">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { docApi, purchaseApi, goodsApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const searchForm = reactive({
  returnNo: '',
  supplierName: ''
})

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const activeTab = ref('formal')
const isViewing = ref(false)

const statusLabelMap = computed(() => ({ 0: '草稿', 1: '待审批', 2: '已审核', 3: '已完成' }))
const statusTypeMap = computed(() => ({ 0: 'info', 1: 'warning', 2: 'success', 3: '' }))
const getStatusLabel = (s) => statusLabelMap.value[s] || '未知'
const getStatusType = (s) => statusTypeMap.value[s] || 'info'

const formData = reactive({
  id: '',
  returnNo: '',
  docName: '',
  orderId: '',
  warehouseId: '',
  warehouseName: '',
  items: [],
  remark: ''
})

const orders = ref([])
const warehouses = ref([
  { id: 1, code: 'MAIN', name: '主仓库' },
  { id: 2, code: 'BACKUP', name: '备用仓库' }
])

const ordersLoading = ref(false)
const orderDateRange = ref([])

const onOrderDateChange = () => {
  searchOrders('')
}

const searchOrders = async (keyword) => {
  ordersLoading.value = true
  try {
    const params = { pageNum: 1, pageSize: 50 }
    if (keyword) params.orderNo = keyword
    if (orderDateRange.value && orderDateRange.value.length === 2) {
      params.startDate = orderDateRange.value[0]
      params.endDate = orderDateRange.value[1]
    }
    const res = await purchaseApi.getOrderList(params)
    if (res) {
      orders.value = (res.list || []).map(record => ({
        id: record.id,
        orderNo: record.orderNo,
        supplierName: record.supplierName || ''
      }))
    }
  } catch (e) {
    console.error('搜索采购订单失败:', e)
  } finally {
    ordersLoading.value = false
  }
}

const selectedSupplierName = computed(() => {
  const order = orders.value.find(o => o.id === formData.orderId)
  return order?.supplierName || ''
})

const loadList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'formal') {
      const params = { pageNum: currentPage.value, pageSize: pageSize.value }
      if (searchForm.returnNo) params.returnNo = searchForm.returnNo
      const res = await purchaseApi.getReturnList(params)
      if (res.code === 200) {
        tableData.value = (res.list || []).map(item => ({
          ...item,
          docNo: item.returnNo,
          partnerName: item.supplierName,
          warehouseName: item.warehouseName,
          orderNo: item.orderNo || item.inNo,
          remark: item.remark,
          docDate: item.returnDate,
          itemCount: item.items ? item.items.length : 0,
          totalQuantity: item.items ? item.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0) : 0,
          totalAmount: item.items ? item.items.reduce((sum, item) => sum + Number(item.amount || 0), 0) : 0,
          taxAmount: item.items ? item.items.reduce((sum, item) => sum + Number(item.taxAmount || 0), 0) : 0,
          creator: item.creator,
          createTime: item.createTime,
          status: item.returnStatus != null ? item.returnStatus : item.status
        }))
        total.value = res.total || 0
      }
    } else {
      const params = { docType: 'PURCHASE_RETURN', pageNum: currentPage.value, pageSize: pageSize.value }
      if (activeTab.value === 'pending') { params.status = '1' }
      else { params.status = '0' }
      const res = await docApi.getPage(params)
      if (res.code === 200 && res.data) {
        tableData.value = (res.data.records || []).map(item => ({
          ...item,
          totalQuantity: item.items ? item.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0) : 0,
          status: Number(item.status)
        }))
        total.value = res.data.total || 0
      }
    }
  } catch (e) {
    console.error('加载采购退货失败:', e)
  } finally {
    loading.value = false
  }
}

const statusMap = { 1: 'PENDING', 2: 'APPROVED', 3: 'IN_STOCK', 4: 'CANCELLED', 5: 'COMPLETED' }
const statusToString = (int) => statusMap[int] || ''

const handleTabChange = () => { currentPage.value = 1; loadList() }

const handleSearch = () => { loadList() }
const handleReset = () => { searchForm.returnNo = ''; searchForm.supplierName = ''; loadList() }

const handleAdd = () => {
  isEdit.value = false
  isViewing.value = false
  dialogTitle.value = '新增采购退货'
  formData.id = ''
  formData.returnNo = 'PR' + Date.now()
  formData.orderId = ''
  formData.warehouseId = ''
  formData.warehouseName = ''
  formData.items = []
  formData.remark = ''
  dialogVisible.value = true
  orderDateRange.value = []
  searchOrders('')
}

const handleEdit = (row) => {
  isEdit.value = true
  isViewing.value = false
  dialogTitle.value = '编辑采购退货'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isViewing.value = true
  dialogTitle.value = '查看采购退货'
  Object.assign(formData, row)
  formData.supplierName = row.supplierName || row.partnerName || ''
  formData.warehouseName = row.warehouseName || ''
  formData.orderNo = row.orderNo || row.inNo || row.docNo || ''
  if (formData.items) {
    formData.items = formData.items.map(item => ({
      ...item,
      goodsUnit: item.goodsUnit || item.unitCode || '',
      quantity: item.quantity || item.returnQuantity || 0,
      unitPrice: item.unitPrice || item.price || 0,
      amount: item.amount || item.totalAmount || 0,
      reason: item.reason || item.remark || ''
    }))
  }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await docApi.deleteDraft(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleSave = async () => {
  if (!formData.orderId) { ElMessage.warning('请选择采购订单'); return }
  if (!formData.warehouseId) { ElMessage.warning('请选择退货仓库'); return }
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    const selectedOrder = orders.value.find(o => o.id === formData.orderId)
    const selectedWarehouse = warehouses.value.find(w => w.id === formData.warehouseId)
    const userId = localStorage.getItem('userId') || '1'
    const userName = localStorage.getItem('userName') || '当前用户'
    const payload = {
      id: formData.id,
      docType: 'PURCHASE_RETURN',
      docName: formData.docName || '采购退货' + new Date().toISOString().slice(0,10).replace(/-/g,''),
      creatorId: userId,
      creatorName: userName,
      docNo: formData.returnNo,
      partnerId: String(formData.orderId),
      partnerName: selectedOrder?.supplierName || '',
      orderNo: selectedOrder?.orderNo || '',
      warehouseId: formData.warehouseId,
      warehouseCode: selectedWarehouse?.code || '',
      warehouseName: selectedWarehouse?.name || '',
      totalAmount: formData.items.reduce((s, i) => s + Number(i.amount || 0), 0),
      totalQuantity: formData.items.reduce((s, i) => s + Number(i.quantity || 0), 0),
      taxAmount: formData.items.reduce((s, i) => s + Number(i.taxAmount || 0), 0),
      remark: formData.remark,
      docDate: formData.returnDate,
      itemCount: formData.items.length,
      status: '0',
      creator: '当前用户',
      createTime: new Date().toISOString(),
      items: formData.items.map((item, idx) => ({
        goodsId: Number(item.goodsId) || null,
        goodsCode: item.goodsCode || '',
        skuCode: item.skuCode || '',
        skuName: item.skuName || '',
        barcode: item.barcode || '',
        goodsName: item.goodsName || '',
        goodsSpec: item.goodsSpec || '',
        unitCode: item.unitCode || '',
        goodsUnit: item.goodsUnit || '件',
        conversionRate: Number(item.conversionRate || 1),
        unitPrice: Number(item.unitPrice || 0),
        quantity: Number(item.quantity || 1),
        taxRate: Number(item.taxRate || 0),
        taxAmount: Number(item.taxAmount || 0),
        amount: Number(item.amount || 0),
        batchNo: item.batchNo || '',
        expiryDate: item.expiryDate || '',
        remark: item.reason || item.remark || '',
        lineNo: idx + 1
      }))
    }
    if (isEdit.value) {
      await docApi.updateDraft(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await docApi.submitPurchaseReturn(payload)
      ElMessage.success('提交审批成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handlePageChange = (page) => { currentPage.value = page; loadList() }

onMounted(() => { loadList() })

const formatAmount = (val) => {
  return '¥' + (Number(val || 0).toFixed(2))
}

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + Number(item.amount || 0), 0)
})

const calcItem = (item) => {
  item.amount = Number(item.quantity || 0) * Number(item.unitPrice || 0)
}

const removeItem = (index) => {
  formData.items.splice(index, 1)
}

const validateItems = (items) => {
  if (!items || items.length === 0) { ElMessage.warning('请添加商品明细'); return false }
  for (let i = 0; i < items.length; i++) {
    if (!items[i].quantity || Number(items[i].quantity) <= 0) { ElMessage.warning(`第${i + 1}行数量必须大于0`); return false }
  }
  return true
}

const onOrderChange = async (orderId) => {
  if (!orderId) {
    formData.items = []
    return
  }
  try {
    const res = await purchaseApi.getOrderById(orderId)
    if (res && res.code === 200 && res.data) {
      const order = res.data
      formData.items = (order.items || []).map(item => ({
        goodsId: Number(item.goodsId) || null,
        goodsCode: item.goodsCode || '',
        skuCode: item.skuCode || '',
        skuName: item.skuName || item.goodsName || '',
        barcode: item.barcode || '',
        goodsName: item.goodsName || '',
        goodsSpec: item.goodsSpec || '',
        unitCode: item.unitCode || '',
        goodsUnit: item.unitCode || item.goodsUnit || '件',
        conversionRate: Number(item.conversionRate || 1),
        unitPrice: Number(item.unitPrice || 0),
        quantity: Number(item.quantity || 0),
        returnedQty: 0,
        maxQty: Number(item.quantity || 0),
        amount: 0,
        reason: ''
      }))

      const itemsWithoutGoodsId = formData.items.filter(item => !item.goodsId && item.goodsCode)
      if (itemsWithoutGoodsId.length > 0) {
        const goodsCodes = [...new Set(itemsWithoutGoodsId.map(item => item.goodsCode))].filter(Boolean)
        try {
          const goodsRes = await goodsApi.getGoodsList({ pageNum: 1, pageSize: 999 })
          if (goodsRes && goodsRes.code === 200 && goodsRes.data && goodsRes.data.records) {
            const goodsList = goodsRes.data.records
            formData.items.forEach(item => {
              if (!item.goodsId && item.goodsCode) {
                const matched = goodsList.find(g => g.code === item.goodsCode || g.goodsCode === item.goodsCode)
                if (matched) {
                  item.goodsId = matched.id
                }
              }
            })
          }
        } catch (ge) {
          console.error('查询商品补全goodsId失败:', ge)
        }
      }

      formData.items.forEach(item => calcItem(item))
    }
  } catch (e) {
    console.error('加载订单明细失败:', e)
    ElMessage.error('加载订单明细失败')
  }
}
</script>

<style scoped>
.purchase-return {
  padding: 20px;
}

.search-bar {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.search-form :deep(.el-form-item) {
  margin-right: 16px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.items-table {
  margin-top: 8px;
}
</style>