<template>
  <div class="purchase-order">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('table.orderNo')">
          <el-input v-model="searchForm.orderNo" :placeholder="t('common.input') + t('table.orderNo')" clearable />
        </el-form-item>
        <el-form-item :label="t('purchase.supplierName')">
          <el-input v-model="searchForm.supplierName" :placeholder="t('common.input') + t('purchase.supplierName')" clearable />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-select v-model="searchForm.status" :placeholder="t('common.select') + t('common.status')" clearable>
            <el-option :label="t('common.all')" value="" />
            <el-option :label="t('table.statusMap.pending')" value="PENDING" />
            <el-option :label="t('table.statusMap.approved')" value="APPROVED" />
            <el-option :label="t('table.statusMap.inStock')" value="IN_STOCK" />
            <el-option :label="t('table.statusMap.cancelled')" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('purchase.dateRange')">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" :range-separator="t('common.to')" :start-placeholder="t('purchase.startDate')" :end-placeholder="t('purchase.endDate')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('purchase.addPurchaseOrder') }}</el-button>
      <el-button type="success" icon="Check" @click="handleApprove">{{ t('purchase.approveOrder') }}</el-button>
      <el-button type="danger" icon="Delete" @click="handleDelete">{{ t('common.delete') + t('table.orderNo') }}</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>

    <div class="table-container">
      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange" v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="docNo" :label="t('table.orderNo')" />
        <el-table-column prop="docName" label="单据名称" />
        <el-table-column prop="partnerName" :label="t('purchase.supplierName')" />
        <el-table-column prop="totalAmount" :label="t('table.amount')" />
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
            <el-button v-if="activeTab !== 'formal'" size="small" type="danger" @click="handleSingleDelete(scope.row)">{{ t('common.delete') }}</el-button>
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
      <el-form :model="formData" label-width="110px">
        <el-form-item :label="t('table.orderNo')">
          <el-input v-model="formData.orderNo" disabled />
        </el-form-item>
        <el-form-item label="单据名称" required>
          <el-input v-model="formData.docName" placeholder="请输入单据名称" :disabled="isViewing" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="t('purchase.supplierName')" v-if="isViewing && (formData.supplierName || formData.partnerName)">
              <el-input :value="formData.supplierName || formData.partnerName" disabled />
            </el-form-item>
            <el-form-item :label="t('purchase.supplierName')" v-else required>
              <el-select v-model="formData.supplierId" :placeholder="t('common.select') + t('purchase.supplierName')" style="width:100%" :disabled="isViewing">
                <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('purchase.orderDate')">
              <el-date-picker v-model="formData.orderDate" type="date" style="width:100%" :disabled="isViewing" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="t('purchase.deliveryDate')">
              <el-date-picker v-model="formData.deliveryDate" type="date" style="width:100%" :disabled="isViewing" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('purchase.paymentType')">
              <el-select v-model="formData.paymentType" :placeholder="t('common.select') + t('purchase.paymentType')" style="width:100%" :disabled="isViewing">
                <el-option v-for="(label, value) in paymentTypeOptions" :key="value" :label="label" :value="Number(value)" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="t('purchase.currencyCode')">
              <el-select v-model="formData.currencyCode" :placeholder="t('common.select') + t('purchase.currencyCode')" style="width:100%" :disabled="isViewing" @change="onCurrencyChange">
                <el-option v-for="(label, code) in currencyOptions" :key="code" :label="code + ' ' + label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('purchase.exchangeRate')">
              <el-input-number v-model="formData.exchangeRate" :min="0" :precision="4" style="width:100%" :disabled="isViewing || formData.currencyCode === 'CNY'" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('purchase.items')">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsCode" :label="t('purchase.goodsCode')" width="100" />
              <el-table-column prop="goodsName" :label="t('purchase.goodsName')" min-width="120" />
              <el-table-column prop="goodsSpec" :label="t('purchase.goodsSpec')" width="100" />
              <el-table-column prop="goodsUnit" :label="t('purchase.goodsUnit')" width="70" />
              <el-table-column prop="quantity" :label="t('purchase.quantity')" width="110">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column prop="unitPrice" :label="t('purchase.unitPrice')" width="110">
                <template #default="scope">
                  <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column :label="t('purchase.amount')" width="100">
                <template #default="scope">{{ formatAmount(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="taxRate" :label="t('purchase.taxRate')" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.taxRate" :min="0" :max="100" :precision="2" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column :label="t('purchase.taxAmount')" width="100">
                <template #default="scope">{{ formatAmount(scope.row.taxAmount) }}</template>
              </el-table-column>
              <el-table-column prop="discountRate" :label="t('purchase.discountRate')" width="110">
                <template #default="scope">
                  <el-input-number v-model="scope.row.discountRate" :min="0" :max="100" :precision="2" size="small" :disabled="isViewing" @change="calcItem(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column :label="t('purchase.discountAmount')" width="110">
                <template #default="scope">{{ formatAmount(scope.row.discountAmount) }}</template>
              </el-table-column>
              <el-table-column :label="t('purchase.netAmount')" width="100">
                <template #default="scope">{{ formatAmount(scope.row.netAmount) }}</template>
              </el-table-column>
              <el-table-column :label="t('common.actions')" width="60" fixed="right">
                <template #default="scope">
                  <el-button v-if="!isViewing" size="small" type="danger" @click="removeItem(scope.$index)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button v-if="!isViewing" size="small" type="primary" @click="addItem" style="margin-top:8px">{{ t('purchase.addProduct') }}</el-button>
          </div>
        </el-form-item>
        <el-form-item :label="t('purchase.totalAmount')">
          <span style="font-weight:bold;font-size:16px">{{ formatAmount(totalAmount) }}</span>
        </el-form-item>
        <el-form-item :label="t('purchase.payAmount')">
          <span style="font-weight:bold;color:#409eff;font-size:16px">{{ formatAmount(payAmount) }}</span>
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

    <el-dialog :title="t('purchase.selectProduct')" v-model="productDialogVisible" width="700px" destroy-on-close>
      <div class="product-search">
        <el-input v-model="productSearch" :placeholder="t('common.input') + t('purchase.goodsName')" clearable @input="searchProducts" />
      </div>
      <el-table :data="productList" border stripe v-loading="productLoading" @row-click="onProductSelect" highlight-current-row style="cursor:pointer;margin-top:12px">
        <el-table-column prop="goodsCode" :label="t('purchase.goodsCode')" width="120" />
        <el-table-column prop="goodsName" :label="t('purchase.goodsName')" min-width="140" />
        <el-table-column prop="packageSpec" :label="t('purchase.goodsSpec')" width="100" />
        <el-table-column prop="saleUnitSymbol" :label="t('purchase.goodsUnit')" width="70" />
        <el-table-column :label="t('purchase.unitPrice')" width="100">
          <template #default="scope">¥{{ Number(scope.row.salePrice || scope.row.costPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stockQty" :label="t('table.stock')" width="80" />
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
import { docApi, goodsApi, purchaseApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const searchForm = reactive({
  orderNo: '', supplierName: '', status: '', dateRange: []
})

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const selectedRows = ref([])
const saving = ref(false)
const activeTab = ref('formal')
const isViewing = ref(false)
const isEdit = ref(false)

const suppliers = ref([
  { id: 1, name: '供应商A' }, { id: 2, name: '供应商B' }, { id: 3, name: '供应商C' }
])

const paymentTypeOptions = {
  1: t('purchase.paymentTypeMap.prepay'),
  2: t('purchase.paymentTypeMap.cashOnDelivery'),
  3: t('purchase.paymentTypeMap.monthly')
}

const currencyOptions = {
  CNY: t('purchase.currencyMap.CNY'),
  USD: t('purchase.currencyMap.USD'),
  EUR: t('purchase.currencyMap.EUR'),
  JPY: t('purchase.currencyMap.JPY'),
  GBP: t('purchase.currencyMap.GBP'),
  HKD: t('purchase.currencyMap.HKD')
}

const formData = reactive({
  id: '', orderNo: '', docName: '', supplierId: '', orderDate: '', deliveryDate: '',
  paymentType: 2, currencyCode: 'CNY', exchangeRate: 1.0, items: [], remark: ''
})

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
})

const payAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + (Number(item.netAmount) || 0), 0)
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
  const discountRate = Number(item.discountRate) || 0
  item.discountAmount = (Number(item.amount) * discountRate / 100).toFixed(2)
  item.netAmount = (Number(item.amount) + Number(item.taxAmount) - Number(item.discountAmount)).toFixed(2)
}

const onCurrencyChange = (code) => {
  if (code === 'CNY') {
    formData.exchangeRate = 1.0
  }
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

const productDialogVisible = ref(false)
const productSearch = ref('')
const productList = ref([])
const productLoading = ref(false)
const productTotal = ref(0)
const productPage = ref(1)
const productPageSize = ref(10)

const addItem = () => {
  productSearch.value = ''
  productPage.value = 1
  productDialogVisible.value = true
  searchProducts()
}

const searchProducts = async () => {
  productLoading.value = true
  try {
    const res = await goodsApi.getGoodsForSelector({
      goodsName: productSearch.value || undefined,
      pageNum: productPage.value,
      pageSize: productPageSize.value
    })
    if (res) {
      productList.value = res.list || []
      productTotal.value = res.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    productLoading.value = false
  }
}

const onProductSelect = (row) => {
  formData.items.push({
    goodsId: row.goodsId,
    goodsCode: row.goodsCode || '',
    skuCode: row.skuCode || '',
    skuName: row.goodsName || '',
    barcode: row.barcode || '',
    goodsName: row.goodsName,
    goodsSpec: row.packageSpec || '',
    unitCode: row.saleUnitSymbol || '',
    goodsUnit: row.saleUnitName || '件',
    conversionRate: Number(row.conversionRate || 1),
    quantity: 1,
    unitPrice: Number(row.salePrice || row.costPrice || 0).toFixed(2),
    amount: 0,
    taxRate: 0,
    taxAmount: 0,
    discountRate: 0,
    discountAmount: 0,
    netAmount: 0
  })
  calcItem(formData.items[formData.items.length - 1])
  productDialogVisible.value = false
}

const onProductPageChange = (page) => {
  productPage.value = page
  searchProducts()
}

const statusTypeMap = {
  'PENDING': 'warning', 'APPROVED': 'primary', 'IN_STOCK': 'success',
  'CANCELLED': 'danger', 'COMPLETED': 'success'
}

const getStatusType = (status) => statusTypeMap[status] || 'info'

const statusLabelMap = computed(() => ({
  'PENDING': t('table.statusMap.pending'),
  'APPROVED': t('table.statusMap.approved'),
  'IN_STOCK': t('table.statusMap.inStock'),
  'CANCELLED': t('table.statusMap.cancelled'),
  'COMPLETED': t('table.statusMap.completed')
}))

const getStatusLabel = (status) => statusLabelMap.value[status] || status

const loadList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'formal') {
      // 正式数据：查询采购微服务
      const params = { pageNum: currentPage.value, pageSize: pageSize.value }
      if (searchForm.orderNo) params.orderNo = searchForm.orderNo
      if (searchForm.supplierName) params.supplierName = searchForm.supplierName
      if (searchForm.status) params.orderStatus = statusToInt(searchForm.status)
      const res = await purchaseApi.getOrderList(params)
      if (res.code === 200) {
        tableData.value = (res.list || []).map(item => ({
          ...item,
          docNo: item.orderNo,
          partnerName: item.supplierName,
          itemCount: item.items ? item.items.length : 0,
          status: statusToString(item.orderStatus)
        }))
        total.value = res.total || 0
      }
    } else {
      // 草稿/待审批：查询 order-rule 模块
      const params = { docType: 'PURCHASE_ORDER', pageNum: currentPage.value, pageSize: pageSize.value }
      if (activeTab.value === 'pending') { params.status = '1' }
      else { params.status = '0' }
      const res = await docApi.getPage(params)
      if (res.code === 200 && res.data) {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
      }
    }
  } catch (e) {
    console.error('加载采购订单失败:', e)
  } finally {
    loading.value = false
  }
}

const statusMap = { 1: 'PENDING', 2: 'APPROVED', 3: 'IN_STOCK', 4: 'CANCELLED', 5: 'COMPLETED' }
const statusToInt = (str) => {
  for (const [k, v] of Object.entries(statusMap)) {
    if (v === str) return parseInt(k)
  }
  return null
}
const statusToString = (int) => statusMap[int] || ''

const handleTabChange = () => { currentPage.value = 1; loadList() }

const handleSearch = () => { loadList() }
const handleReset = () => { searchForm.orderNo = ''; searchForm.supplierName = ''; searchForm.status = ''; searchForm.dateRange = []; loadList() }

const handleAdd = () => {
  isViewing.value = false
  isEdit.value = false
  dialogTitle.value = t('purchase.addPurchaseOrder')
  formData.id = ''
  formData.orderNo = ''
  formData.supplierId = ''
  formData.orderDate = ''
  formData.deliveryDate = ''
  formData.paymentType = 2
  formData.currencyCode = 'CNY'
  formData.exchangeRate = 1.0
  formData.items = []
  formData.remark = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isViewing.value = false
  isEdit.value = true
  dialogTitle.value = t('purchase.editPurchaseOrder')
  Object.assign(formData, row)
  if (!formData.currencyCode) formData.currencyCode = 'CNY'
  if (!formData.exchangeRate) formData.exchangeRate = 1.0
  if (!formData.paymentType) formData.paymentType = 2
  dialogVisible.value = true
}

const handleView = (row) => {
  isViewing.value = true
  dialogTitle.value = t('purchase.viewPurchaseOrder')
  Object.assign(formData, row)
  formData.supplierName = row.supplierName || row.partnerName || ''
  if (!formData.currencyCode) formData.currencyCode = 'CNY'
  if (!formData.exchangeRate) formData.exchangeRate = 1.0
  if (!formData.paymentType) formData.paymentType = 2
  dialogVisible.value = true
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }

const handleSingleDelete = async (row) => {
  try {
    await docApi.deleteDraft(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleDelete = async () => {
  if (selectedRows.value.length === 0) { ElMessage.warning('请先选择订单'); return }
  try {
    for (const row of selectedRows.value) {
      await docApi.deleteDraft(row.id)
    }
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleApprove = () => {
  ElMessage.info('审核功能开发中')
}

const handleSave = async () => {
  if (!formData.supplierId) { ElMessage.warning('请选择供应商'); return }
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    const selectedSupplier = suppliers.value.find(s => s.id === formData.supplierId)
    const userId = localStorage.getItem('userId') || '1'
    const userName = localStorage.getItem('userName') || '当前用户'
    const payload = {
      docType: 'PURCHASE_ORDER',
      docName: formData.docName || '采购订单' + new Date().toISOString().slice(0,10).replace(/-/g,''),
      creatorId: userId,
      creatorName: userName,
      partnerId: String(formData.supplierId),
      partnerName: selectedSupplier?.name || '',
      totalAmount: totalAmount.value,
      taxAmount: formData.items.reduce((s, i) => s + Number(i.taxAmount || 0), 0),
      discountAmount: formData.items.reduce((s, i) => s + Number(i.discountAmount || 0), 0),
      payAmount: payAmount.value,
      remark: formData.remark,
      docDate: formData.orderDate,
      deliveryDate: formData.deliveryDate,
      paymentType: formData.paymentType,
      currencyCode: formData.currencyCode,
      exchangeRate: formData.exchangeRate,
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
        discountRate: Number(item.discountRate || 0),
        discountAmount: Number(item.discountAmount || 0),
        netAmount: Number(item.netAmount || 0),
        amount: Number(item.amount || 0),
        lineNo: idx + 1
      }))
    }
    if (isEdit.value) {
      await docApi.updateDraft(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await docApi.submitPurchaseOrder(payload)
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
</script>

<style scoped>
.purchase-order { padding: 20px; }
.search-bar { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-container { background: #fff; border-radius: 8px; padding: 16px; }
.items-table { overflow-x: auto; }
</style>