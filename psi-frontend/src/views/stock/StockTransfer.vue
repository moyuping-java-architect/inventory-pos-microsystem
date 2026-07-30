<template>
  <div class="stock-transfer">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('stock.transferNo')">
          <el-input v-model="searchForm.transferNo" :placeholder="t('common.input') + t('stock.transferNo')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('stock.addTransfer') }}</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="docNo" :label="t('stock.transferNo')" />
        <el-table-column prop="docName" label="单据名称" />
        <el-table-column prop="partnerName" :label="t('stock.outWarehouse')" />
        <el-table-column :label="t('stock.inWarehouse')">
          <template #default="scope">
            <span>{{ scope.row.toWarehouseName || scope.row.partnerName?.split('→')[1]?.trim() || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" :label="t('stock.totalAmount')" />
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
    
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="900px">
      <el-form :model="formData" label-width="100px">
        <el-form-item :label="t('stock.transferNo')">
          <el-input v-model="formData.transferNo" disabled />
        </el-form-item>
        <el-form-item label="单据名称" required>
          <el-input v-model="formData.docName" placeholder="请输入单据名称" :disabled="isViewing" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="t('stock.outWarehouse')" v-if="isViewing && (formData.fromWarehouseName || formData.warehouseName || formData.partnerName)">
              <el-input :value="formData.fromWarehouseName || formData.warehouseName || formData.partnerName || ''" disabled />
            </el-form-item>
            <el-form-item :label="t('stock.outWarehouse')" v-else required>
              <el-select v-model="formData.fromWarehouseId" :placeholder="t('common.select') + t('stock.outWarehouse')" style="width:100%" :disabled="isViewing">
                <el-option v-for="warehouse in warehouses" :key="warehouse.id" :label="warehouse.name" :value="warehouse.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('stock.inWarehouse')" v-if="isViewing && formData.toWarehouseName">
              <el-input :value="formData.toWarehouseName" disabled />
            </el-form-item>
            <el-form-item :label="t('stock.inWarehouse')" v-else required>
              <el-select v-model="formData.toWarehouseId" :placeholder="t('common.select') + t('stock.inWarehouse')" style="width:100%" :disabled="isViewing" :teleported="false">
                <el-option v-for="warehouse in warehouses" :key="warehouse.id" :label="warehouse.name" :value="warehouse.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('stock.transferDate')">
          <el-date-picker v-model="formData.transferDate" type="date" style="width:100%" :disabled="isViewing" :teleported="false" />
        </el-form-item>
        <el-form-item :label="t('table.items')">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsCode" :label="t('stock.goodsCode')" width="100" />
              <el-table-column prop="goodsName" :label="t('stock.goodsName')" min-width="120" />
              <el-table-column prop="goodsSpec" :label="t('stock.goodsSpec')" width="100" />
              <el-table-column prop="goodsUnit" :label="t('stock.goodsUnit')" width="70" />
              <el-table-column prop="quantity" :label="t('stock.transferQuantity')" width="110">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column prop="unitPrice" :label="t('stock.unitPrice')" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column :label="t('stock.amount')" width="100">
                <template #default="scope">{{ formatAmount(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="taxRate" :label="t('stock.taxRate')" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.taxRate" :min="0" :max="100" :precision="2" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column :label="t('stock.taxAmount')" width="100">
                <template #default="scope">{{ formatAmount(scope.row.taxAmount) }}</template>
              </el-table-column>
              <el-table-column :label="t('common.actions')" width="60" fixed="right">
                <template #default="scope">
                  <el-button v-if="!isViewing" size="small" type="danger" @click="removeItem(scope.$index)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button v-if="!isViewing" size="small" type="primary" @click="addItem" style="margin-top:8px">{{ t('stock.addProduct') }}</el-button>
          </div>
        </el-form-item>
        <el-form-item :label="t('stock.totalAmount')">
          <span style="font-weight:bold;font-size:16px">{{ formatAmount(totalAmount) }}</span>
        </el-form-item>
        <el-form-item :label="t('table.remark')">
          <el-input type="textarea" v-model="formData.remark" rows="3" :disabled="isViewing" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isViewing ? t('common.close') : t('common.cancel') }}</el-button>
        <el-button v-if="!isViewing" type="primary" @click="handleSave" :loading="saving">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="t('stock.selectProduct')" v-model="productDialogVisible" width="700px" destroy-on-close append-to-body modal-class="product-selector-mask">
      <div class="product-search">
        <el-input v-model="productSearch" :placeholder="t('common.input') + t('stock.goodsName')" clearable @input="searchProducts" />
      </div>
      <el-table :data="productList" border stripe v-loading="productLoading" highlight-current-row style="margin-top:12px">
        <el-table-column prop="goodsCode" :label="t('stock.goodsCode')" width="120" />
        <el-table-column prop="goodsName" :label="t('stock.goodsName')" min-width="140" />
        <el-table-column prop="goodsSpec" :label="t('stock.goodsSpec')" width="100" />
        <el-table-column prop="unit" :label="t('stock.goodsUnit')" width="70" />
        <el-table-column :label="t('stock.unitPrice')" width="100">
          <template #default="scope">¥{{ Number(scope.row.avgCostPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" :label="t('table.stock')" width="80" />
        <el-table-column :label="t('common.actions')" width="80" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="onProductSelect(scope.row)">{{ t('common.select') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        :total="productTotal"
        :page-size="productPageSize"
        :current-page="productPage"
        layout="prev, pager, next"
        @current-change="onProductPageChange"
        small
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { docApi, stockApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const searchForm = reactive({ transferNo: '' })

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
  id: '', transferNo: '', docName: '', fromWarehouseId: '', toWarehouseId: '', toWarehouseName: '', transferDate: '', items: [], remark: '', extJson: ''
})

const warehouses = ref([
  { id: 1, code: 'MAIN', name: '主仓库' },
  { id: 2, code: 'BACKUP', name: '备用仓库' }
])

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
})

const formatAmount = (val) => {
  return (Number(val) || 0).toFixed(2)
}

const calcItem = (item) => {
  const qty = Number(item.quantity) || 0
  const price = Number(item.unitPrice) || 0
  item.amount = (qty * price).toFixed(2)
  const taxRate = Number(item.taxRate) || 0
  item.taxAmount = (Number(item.amount) * taxRate / 100).toFixed(2)
}

const loadList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'formal') {
      const params = { pageNum: currentPage.value, pageSize: pageSize.value }
      const res = await stockApi.getTransferList(params)
      if (res.data) {
        tableData.value = (res.data.list || []).map(item => ({
          ...item,
          docNo: item.transferNo,
          warehouseName: item.warehouseName,
          remark: item.remark,
          docDate: item.transferDate,
          itemCount: item.items ? item.items.length : 0,
          totalQuantity: item.items ? item.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0) : 0,
          totalAmount: item.items ? item.items.reduce((sum, item) => sum + Number(item.amount || 0), 0) : 0,
          taxAmount: item.items ? item.items.reduce((sum, item) => sum + Number(item.taxAmount || 0), 0) : 0,
          creator: item.creator,
          createTime: item.createTime,
          status: statusToString(item.orderStatus)
        }))
        total.value = res.data.total || 0
      }
    } else {
      const params = { docType: 'STOCK_TRANSFER', pageNum: currentPage.value, pageSize: pageSize.value }
      if (activeTab.value === 'pending') { params.status = '1' }
      else { params.status = '0' }
      const res = await docApi.getPage(params)
      if (res.code === 200 && res.data) {
        tableData.value = (res.data.records || []).map(item => {
          // 解析 extJson 中的 toWarehouseName
          if (item.extJson) {
            try {
              const ext = JSON.parse(item.extJson)
              if (ext.toWarehouseName) {
                item.toWarehouseName = ext.toWarehouseName
              }
            } catch (e) {
              console.warn('解析 extJson 失败:', item.extJson)
            }
          }
          item.totalQuantity = item.items ? item.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0) : 0
          return item
        })
        total.value = res.data.total || 0
      }
    }
  } catch (e) {
    console.error('加载调拨单失败:', e)
  } finally {
    loading.value = false
  }
}

const statusMap = { 1: 'PENDING', 2: 'APPROVED', 3: 'IN_STOCK', 4: 'CANCELLED', 5: 'COMPLETED' }
const statusToString = (int) => statusMap[int] || ''

const handleTabChange = () => { currentPage.value = 1; loadList() }

const handleSearch = () => { loadList() }
const handleReset = () => { searchForm.transferNo = ''; loadList() }

const handleAdd = () => {
  isEdit.value = false
  isViewing.value = false
  dialogTitle.value = t('stock.addTransfer')
  formData.id = ''
  formData.transferNo = 'TR' + Date.now()
  formData.fromWarehouseId = ''
  formData.toWarehouseId = ''
  formData.toWarehouseName = ''
  formData.transferDate = ''
  formData.items = []
  formData.remark = ''
  formData.extJson = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  isViewing.value = false
  dialogTitle.value = '编辑调拨单'
  Object.assign(formData, row)
  // 解析 extJson 中的 toWarehouseName 和 toWarehouseId
  if (formData.extJson) {
    try {
      const ext = JSON.parse(formData.extJson)
      if (ext.toWarehouseName) formData.toWarehouseName = ext.toWarehouseName
      if (ext.toWarehouseId) formData.toWarehouseId = ext.toWarehouseId
    } catch (e) {
      console.warn('解析 extJson 失败:', formData.extJson)
    }
  }
  dialogVisible.value = true
}

const handleView = (row) => {
  isViewing.value = true
  dialogTitle.value = '查看调拨单'
  Object.assign(formData, row)
  // 设置出库仓库名称
  formData.fromWarehouseName = row.warehouseName || row.partnerName || ''
  // 解析 extJson 中的 toWarehouseName 和 toWarehouseId
  if (formData.extJson) {
    try {
      const ext = JSON.parse(formData.extJson)
      if (ext.toWarehouseName) formData.toWarehouseName = ext.toWarehouseName
      if (ext.toWarehouseId) formData.toWarehouseId = ext.toWarehouseId
    } catch (e) {
      console.warn('解析 extJson 失败:', formData.extJson)
    }
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

const formatDateTime = (val) => {
  if (!val) return null
  const d = new Date(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const handleSave = async () => {
  if (!formData.fromWarehouseId || !formData.toWarehouseId) { ElMessage.warning('请选择调出和调入仓库'); return }
  if (formData.fromWarehouseId === formData.toWarehouseId) { ElMessage.warning('调出和调入仓库不能相同'); return }
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    const fromWarehouse = warehouses.value.find(w => w.id === formData.fromWarehouseId)
    const toWarehouse = warehouses.value.find(w => w.id === formData.toWarehouseId)
    const userId = localStorage.getItem('userId') || '1'
    const userName = localStorage.getItem('userName') || '当前用户'
    const payload = {
      id: formData.id,
      docType: 'STOCK_TRANSFER',
      docName: formData.docName || '库存调拨' + new Date().toISOString().slice(0,10).replace(/-/g,''),
      creatorId: userId,
      creatorName: userName,
      docNo: formData.transferNo,
      partnerId: String(formData.fromWarehouseId),
      partnerName: (fromWarehouse?.name || '') + ' → ' + (toWarehouse?.name || ''),
      warehouseId: formData.fromWarehouseId,
      warehouseCode: fromWarehouse?.code || '',
      warehouseName: fromWarehouse?.name || '',
      extJson: JSON.stringify({ toWarehouseName: toWarehouse?.name || '', toWarehouseId: formData.toWarehouseId }),
      totalAmount: totalAmount.value,
      totalQuantity: formData.items.reduce((s, i) => s + Number(i.quantity || 0), 0),
      taxAmount: formData.items.reduce((s, i) => s + Number(i.taxAmount || 0), 0),
      remark: formData.remark,
      docDate: formatDateTime(formData.transferDate),
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
        lineNo: idx + 1
      }))
    }
    if (isEdit.value) {
      await docApi.updateDraft(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await docApi.submitStockTransfer(payload)
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

const productDialogVisible = ref(false)
const productSearch = ref('')
const productList = ref([])
const productLoading = ref(false)
const productTotal = ref(0)
const productPage = ref(1)
const productPageSize = ref(10)

const addItem = () => {
  if (!formData.fromWarehouseId) {
    ElMessage.warning('请先选择调出仓库')
    return
  }
  productSearch.value = ''
  productPage.value = 1
  productDialogVisible.value = true
  searchProducts()
}

const searchProducts = async () => {
  productLoading.value = true
  try {
    const selectedWarehouse = warehouses.value.find(w => w.id === formData.fromWarehouseId)
    const res = await stockApi.getStockForSelector({
      warehouseCode: selectedWarehouse?.code,
      goodsName: productSearch.value || undefined,
      pageNum: productPage.value,
      pageSize: productPageSize.value
    })
    if (res && res.data) {
      productList.value = res.data.list || []
      productTotal.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    productLoading.value = false
  }
}

const onProductSelect = (row) => {
  formData.items.push({
    goodsId: null,
    goodsCode: row.goodsCode || '',
    skuCode: row.skuCode || '',
    skuName: row.goodsName || '',
    barcode: '',
    goodsName: row.goodsName,
    goodsSpec: row.goodsSpec || '',
    unitCode: row.unit || '',
    goodsUnit: row.unit || '件',
    conversionRate: 1,
    quantity: 1,
    unitPrice: Number(row.avgCostPrice || 0).toFixed(2),
    amount: 0,
    taxRate: 0,
    taxAmount: 0
  })
  calcItem(formData.items[formData.items.length - 1])
  productDialogVisible.value = false
}

const onProductPageChange = (page) => {
  productPage.value = page
  searchProducts()
}

const removeItem = (index) => { formData.items.splice(index, 1) }

const validateItems = (items) => {
  if (!items || items.length === 0) { ElMessage.warning('请添加商品明细'); return false }
  for (let i = 0; i < items.length; i++) {
    if (!items[i].skuCode && !items[i].goodsCode) { ElMessage.warning(`第${i + 1}行商品数据异常，请确认商品编码或SKU编码存在`); return false }
    if (!items[i].quantity || Number(items[i].quantity) <= 0) { ElMessage.warning(`第${i + 1}行数量必须大于0`); return false }
  }
  return true
}
</script>

<style scoped>
.stock-transfer { padding: 20px; }
.search-bar { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-container { background: #fff; border-radius: 8px; padding: 16px; }
.items-table { overflow-x: auto; }

:global(.product-selector-mask) {
  pointer-events: none !important;
}
:global(.product-selector-mask .el-dialog) {
  pointer-events: auto !important;
}
:global(.product-selector-mask .el-dialog *) {
  pointer-events: auto !important;
}
</style>