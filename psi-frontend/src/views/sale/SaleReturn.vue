<template>
  <div class="sale-return">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('sale.returnNo')">
          <el-input v-model="searchForm.returnNo" :placeholder="t('common.input') + t('sale.returnNo')" clearable />
        </el-form-item>
        <el-form-item :label="t('sale.customerName')">
          <el-input v-model="searchForm.customerName" :placeholder="t('common.input') + t('sale.customerName')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('sale.addReturn') }}</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="docNo" :label="t('sale.returnNo')" />
        <el-table-column prop="docName" label="单据名称" />
        <el-table-column prop="partnerName" :label="t('sale.customerName')" />
        <el-table-column prop="warehouseName" label="退货仓库" />
        <el-table-column prop="totalAmount" :label="t('stock.totalAmount')">
          <template #default="scope">{{ formatAmount(scope.row.totalAmount) }}</template>
        </el-table-column>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="900px" destroy-on-close>
      <el-form :model="formData" label-width="100px">
        <el-form-item :label="t('sale.returnNo')">
          <el-input v-model="formData.returnNo" disabled />
        </el-form-item>
        <el-form-item label="单据名称" required>
          <el-input v-model="formData.docName" placeholder="请输入单据名称" :disabled="isViewing" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="t('sale.customerName')" v-if="isViewing && (formData.customerName || formData.partnerName)">
              <el-input :value="formData.customerName || formData.partnerName" disabled />
            </el-form-item>
            <el-form-item :label="t('sale.customerName')" v-else required>
              <el-select v-model="formData.customerId" :placeholder="t('common.select') + t('sale.customerName')" filterable style="width:100%" :disabled="isViewing">
                <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('sale.returnDate')">
              <el-date-picker v-model="formData.returnDate" type="date" style="width:100%" :disabled="isViewing" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('sale.returnReason')">
          <el-input v-model="formData.returnReason" :placeholder="t('common.input') + t('sale.returnReason')" :disabled="isViewing" />
        </el-form-item>
        <el-form-item label="退货仓库" v-if="isViewing && formData.warehouseName">
          <el-input :value="formData.warehouseName" disabled />
        </el-form-item>
        <el-form-item label="退货仓库" v-else>
          <el-select v-model="formData.warehouseId" placeholder="请选择退货仓库" :disabled="isViewing">
            <el-option v-for="warehouse in warehouses" :key="warehouse.id" :label="warehouse.name" :value="warehouse.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('table.items')">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsCode" :label="t('stock.goodsCode')" width="100" />
              <el-table-column prop="goodsName" :label="t('stock.goodsName')" min-width="120" />
              <el-table-column prop="goodsSpec" :label="t('stock.goodsSpec')" width="100" />
              <el-table-column prop="goodsUnit" :label="t('stock.goodsUnit')" width="70" />
              <el-table-column prop="quantity" :label="t('sale.returnQuantity')" width="110">
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

    <el-dialog :title="t('stock.selectProduct')" v-model="productDialogVisible" width="700px" destroy-on-close>
      <div class="product-search">
        <el-input v-model="productSearch" :placeholder="t('common.input') + t('stock.goodsName')" clearable @input="searchProducts" />
      </div>
      <el-table :data="productList" border stripe v-loading="productLoading" @row-click="onProductSelect" highlight-current-row style="cursor:pointer;margin-top:12px">
        <el-table-column prop="goodsCode" :label="t('stock.goodsCode')" width="120" />
        <el-table-column prop="goodsName" :label="t('stock.goodsName')" min-width="140" />
        <el-table-column prop="packageSpec" :label="t('stock.goodsSpec')" width="100" />
        <el-table-column prop="saleUnitSymbol" :label="t('stock.goodsUnit')" width="70" />
        <el-table-column :label="t('stock.unitPrice')" width="100">
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
import { docApi, goodsApi, saleApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const searchForm = reactive({ returnNo: '', customerName: '' })

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

const warehouses = ref([
  { id: 1, code: 'MAIN', name: '主仓库' },
  { id: 2, code: 'BACKUP', name: '备用仓库' }
])

const formData = reactive({
  id: '', returnNo: '', docName: '', orderNo: '', customerId: '', warehouseId: '', warehouseName: '', returnDate: '', returnReason: '', items: [], remark: ''
})

const customerList = ref([])

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

const loadCustomers = async () => {
  try {
    const res = await saleApi.getCustomerList({ pageNum: 1, pageSize: 999 })
    if (res.data) {
      customerList.value = res.data.list || []
    }
  } catch (e) { console.error(e) }
}

const loadList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'formal') {
      const params = { pageNum: currentPage.value, pageSize: pageSize.value }
      const res = await saleApi.getReturnList(params)
      if (res.data) {
        tableData.value = (res.data.list || []).map(item => ({
          ...item,
          docNo: item.returnNo,
          partnerName: item.customerName,
          warehouseName: item.warehouseName,
          orderNo: item.orderNo,
          remark: item.remark,
          docDate: item.returnDate,
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
      const params = { docType: 'SALE_RETURN', pageNum: currentPage.value, pageSize: pageSize.value }
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
    console.error('加载退货单失败:', e)
  } finally {
    loading.value = false
  }
}

const statusMap = { 1: 'PENDING', 2: 'APPROVED', 3: 'IN_STOCK', 4: 'CANCELLED', 5: 'COMPLETED' }
const statusToString = (int) => statusMap[int] || ''

const handleTabChange = () => { currentPage.value = 1; loadList() }

const handleSearch = () => { currentPage.value = 1; loadList() }
const handleReset = () => { searchForm.returnNo = ''; searchForm.customerName = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  isViewing.value = false
  dialogTitle.value = t('sale.addReturn')
  formData.id = ''
  formData.returnNo = 'SR' + Date.now()
  formData.customerId = ''
  formData.warehouseId = ''
  formData.warehouseName = ''
  formData.returnDate = ''
  formData.returnReason = ''
  formData.items = []
  formData.remark = ''
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  isViewing.value = false
  dialogTitle.value = '编辑销售退货'
  try {
    const res = await docApi.findById(row.id)
    if (res.data) Object.assign(formData, res.data)
  } catch (e) {
    ElMessage.error('获取退货单详情失败')
  }
  dialogVisible.value = true
}

const handleView = (row) => {
  isViewing.value = true
  dialogTitle.value = '查看销售退货'
  Object.assign(formData, row)
  formData.customerName = row.customerName || row.partnerName || ''
  formData.warehouseName = row.warehouseName || ''
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.customerId) { ElMessage.warning('请选择客户'); return }
  if (!formData.warehouseId) { ElMessage.warning('请选择退货仓库'); return }
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    const selectedWarehouse = warehouses.value.find(w => w.id === formData.warehouseId)
    const userId = localStorage.getItem('userId') || '1'
    const userName = localStorage.getItem('userName') || '当前用户'
    const payload = {
      id: formData.id,
      docType: 'SALE_RETURN',
      docName: formData.docName || '销售退货' + new Date().toISOString().slice(0,10).replace(/-/g,''),
      creatorId: userId,
      creatorName: userName,
      docNo: formData.returnNo,
      partnerId: String(formData.customerId),
      partnerName: customerList.value.find(c => c.id === formData.customerId)?.customerName || '',
      warehouseId: formData.warehouseId,
      warehouseCode: selectedWarehouse?.code || '',
      warehouseName: selectedWarehouse?.name || '',
      orderNo: formData.orderNo || '',
      totalAmount: totalAmount.value,
      totalQuantity: formData.items.reduce((s, i) => s + Number(i.quantity || 0), 0),
      taxAmount: formData.items.reduce((s, i) => s + Number(i.taxAmount || 0), 0),
      remark: formData.remark,
      docDate: formData.returnDate,
      returnReason: formData.returnReason,
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
      await docApi.submitSaleReturn(payload)
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

const handleDelete = async (row) => {
  try {
    await docApi.deleteDraft(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handlePageChange = (page) => { currentPage.value = page; loadList() }

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
    if (!items[i].quantity || Number(items[i].quantity) <= 0) { ElMessage.warning(`第${i + 1}行数量必须大于0`); return false }
  }
  return true
}

onMounted(() => { loadList(); loadCustomers() })
</script>

<style scoped>
.sale-return { padding: 20px; }
.search-bar { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-container { background: #fff; border-radius: 8px; padding: 16px; }
.items-table { overflow-x: auto; }
</style>