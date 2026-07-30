<template>
  <div class="purchase-in">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="入库单号">
          <el-input v-model="searchForm.inNo" placeholder="请输入入库单号" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待入库" value="PENDING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}入库</el-button>
      <el-button type="success" icon="Check" @click="handleComplete">完成入库</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange" v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="docNo" label="入库单号" />
        <el-table-column prop="docName" label="单据名称" />
        <el-table-column prop="partnerName" label="供应商" />
        <el-table-column prop="warehouseName" label="入库仓库" />
        <el-table-column prop="totalQuantity" label="入库数量" />
        <el-table-column prop="totalAmount" label="入库金额" />
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
        <el-form-item label="入库单号" :disabled="isEdit">
          <el-input v-model="formData.inNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="单据名称" required>
          <el-input v-model="formData.docName" placeholder="请输入单据名称" :disabled="isViewing" />
        </el-form-item>
        <el-form-item label="关联订单号" v-if="isViewing && formData.orderNo">
          <el-input :value="formData.orderNo" disabled />
        </el-form-item>
        <el-form-item label="关联订单" v-else>
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
        <el-form-item label="入库仓库" v-if="isViewing && formData.warehouseName">
          <el-input :value="formData.warehouseName" disabled />
        </el-form-item>
        <el-form-item label="入库仓库" v-else>
          <el-select v-model="formData.warehouseId" placeholder="请选择仓库" :disabled="isViewing">
            <el-option v-for="warehouse in warehouses" :key="warehouse.id" :label="warehouse.name" :value="warehouse.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库明细">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsCode" label="商品编码" width="100" />
              <el-table-column prop="barcode" label="条码" width="100" />
              <el-table-column prop="goodsName" label="商品名称" min-width="120" />
              <el-table-column prop="goodsSpec" label="规格" width="80" />
              <el-table-column prop="goodsUnit" label="单位" width="60" />
              <el-table-column prop="quantity" label="入库数量" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column prop="unitPrice" label="单价" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column label="金额" width="90">
                <template #default="scope">{{ formatAmount(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="batchNo" label="批次号" width="100">
                <template #default="scope">
                  <el-input v-model="scope.row.batchNo" size="small" :disabled="isViewing" />
                </template>
              </el-table-column>
              <el-table-column prop="expiryDate" label="有效期" width="110">
                <template #default="scope">
                  <el-date-picker v-model="scope.row.expiryDate" type="date" size="small" :disabled="isViewing" />
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
        <el-form-item label="入库总金额">
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
import { docApi, purchaseApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const searchForm = reactive({
  inNo: '',
  supplierName: '',
  status: ''
})

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const selectedRows = ref([])
const saving = ref(false)
const activeTab = ref('formal')
const isViewing = ref(false)

const statusLabelMap = computed(() => ({ 0: '草稿', 1: '待审批', 2: '已审核', 3: '已完成' }))
const statusTypeMap = computed(() => ({ 0: 'info', 1: 'warning', 2: 'success', 3: '' }))
const getStatusLabel = (s) => statusLabelMap.value[s] || '未知'
const getStatusType = (s) => statusTypeMap.value[s] || 'info'

const formData = reactive({
  id: '',
  inNo: '',
  docName: '',
  orderId: '',
  orderNo: '',
  warehouseId: '',
  warehouseName: '',
  items: [],
  remark: ''
})

const orders = ref([])
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
        quantity: 1,
        amount: 0,
        batchNo: '',
        expiryDate: ''
      }))
      formData.items.forEach(item => calcItem(item))
    }
  } catch (e) {
    console.error('加载订单明细失败:', e)
    ElMessage.error('加载订单明细失败')
  }
}

const warehouses = ref([
  { id: 1, code: 'MAIN', name: '主仓库' },
  { id: 2, code: 'BACKUP', name: '备用仓库' }
])

const loadList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'formal') {
      const params = { pageNum: currentPage.value, pageSize: pageSize.value }
      if (searchForm.inNo) params.inNo = searchForm.inNo
      const res = await purchaseApi.getInList(params)
      if (res.code === 200) {
        tableData.value = (res.list || []).map(item => ({
          ...item,
          docNo: item.inNo,
          partnerName: item.supplierName,
          warehouseName: item.warehouseName,
          orderNo: item.orderNo,
          remark: item.remark,
          docDate: item.inDate,
          itemCount: item.items ? item.items.length : 0,
          totalQuantity: item.items ? item.items.reduce((sum, item) => sum + Number(item.inQuantity || 0), 0) : 0,
          totalAmount: item.items ? item.items.reduce((sum, item) => sum + Number(item.amount || 0), 0) : 0,
          taxAmount: item.items ? item.items.reduce((sum, item) => sum + Number(item.taxAmount || 0), 0) : 0,
          creator: item.creator,
          createTime: item.createTime,
          status: item.inStatus
        }))
        total.value = res.total || 0
      }
    } else {
      const params = { docType: 'PURCHASE_IN', pageNum: currentPage.value, pageSize: pageSize.value }
      if (activeTab.value === 'pending') { params.status = '1' }
      else { params.status = '0' }
      const res = await docApi.getPage(params)
      if (res.code === 200 && res.data) {
        tableData.value = (res.data.records || []).map(item => ({
          ...item,
          totalQuantity: item.items ? item.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0) : 0
        }))
        total.value = res.data.total || 0
      }
    }
  } catch (e) {
    console.error('加载采购入库失败:', e)
  } finally {
    loading.value = false
  }
}

const statusMap = { 1: 'PENDING', 2: 'APPROVED', 3: 'IN_STOCK', 4: 'CANCELLED', 5: 'COMPLETED' }
const statusToString = (int) => statusMap[int] || ''

const handleTabChange = () => { currentPage.value = 1; loadList() }

const handleSearch = () => { loadList() }
const handleReset = () => {
  searchForm.inNo = ''
  searchForm.supplierName = ''
  searchForm.status = ''
  loadList()
}

const handleAdd = () => {
  isEdit.value = false
  isViewing.value = false
  dialogTitle.value = '新增采购入库'
  formData.id = ''
  formData.inNo = 'PI' + Date.now()
  formData.orderId = ''
  formData.orderNo = ''
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
  dialogTitle.value = '编辑采购入库'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isViewing.value = true
  dialogTitle.value = '查看采购入库'
  Object.assign(formData, row)
  formData.orderNo = row.orderNo || row.docNo || ''
  formData.supplierName = row.supplierName || row.partnerName || ''
  formData.warehouseName = row.warehouseName || ''
  if (formData.items) {
    formData.items = formData.items.map(item => ({
      ...item,
      quantity: item.inQuantity || item.quantity || 0,
      goodsUnit: item.goodsUnit || item.unitCode || '',
      batchNo: item.batchNo || '',
      expiryDate: item.expiryDate || item.expireDate || ''
    }))
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.orderId) { ElMessage.warning('请选择关联订单'); return }
  if (!formData.warehouseId) { ElMessage.warning('请选择仓库'); return }
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    const selectedWarehouse = warehouses.value.find(w => w.id === formData.warehouseId)
    const userId = localStorage.getItem('userId') || '1'
    const userName = localStorage.getItem('userName') || '当前用户'
    const payload = {
      id: formData.id,
      docType: 'PURCHASE_IN',
      docName: formData.docName || '采购入库' + new Date().toISOString().slice(0,10).replace(/-/g,''),
      creatorId: userId,
      creatorName: userName,
      docNo: formData.inNo,
      partnerId: String(formData.orderId || ''),
      partnerName: orders.value.find(o => o.id === formData.orderId)?.supplierName || '',
      orderNo: orders.value.find(o => o.id === formData.orderId)?.orderNo || '',
      warehouseId: formData.warehouseId,
      warehouseCode: selectedWarehouse?.code || '',
      warehouseName: selectedWarehouse?.name || '',
      totalAmount: formData.items.reduce((s, i) => s + Number(i.amount || 0), 0),
      totalQuantity: formData.items.reduce((s, i) => s + Number(i.quantity || 0), 0),
      taxAmount: formData.items.reduce((s, i) => s + Number(i.taxAmount || 0), 0),
      remark: formData.remark,
      docDate: formData.inDate,
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
        lineNo: idx + 1
      }))
    }
    if (isEdit.value) {
      await docApi.updateDraft(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await docApi.submitPurchaseIn(payload)
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

const handleComplete = () => {}
const handlePageChange = (page) => { currentPage.value = page; loadList() }

onMounted(() => { loadList() })

const handleSelectionChange = (val) => { selectedRows.value = val }

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
    if (!items[i].goodsId) { ElMessage.warning(`第${i + 1}行商品未选择`); return false }
    if (!items[i].quantity || Number(items[i].quantity) <= 0) { ElMessage.warning(`第${i + 1}行数量必须大于0`); return false }
  }
  return true
}
</script>

<style scoped>
.purchase-in {
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