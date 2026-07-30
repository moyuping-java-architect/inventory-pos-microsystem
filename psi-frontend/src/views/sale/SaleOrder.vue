<template>
  <div class="sale-order">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('sale.orderNo')">
          <el-input v-model="searchForm.orderNo" :placeholder="t('common.input') + t('sale.orderNo')" clearable />
        </el-form-item>
        <el-form-item :label="t('sale.customer')">
          <el-input v-model="searchForm.customerName" :placeholder="t('common.input') + t('sale.customer')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('sale.addOrder') }}</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="docNo" :label="t('sale.orderNo')" />
        <el-table-column prop="docName" label="单据名称" />
        <el-table-column prop="partnerName" :label="t('sale.customer')" />
        <el-table-column prop="totalAmount" :label="t('table.amount')">
          <template #default="scope">¥{{ scope.row.totalAmount ? scope.row.totalAmount.toFixed(2) : '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="payAmount" label="实付金额">
          <template #default="scope">
            <span v-if="scope.row.payAmount !== undefined && scope.row.payAmount !== null">
              ¥{{ Number(scope.row.payAmount).toFixed(2) }}
            </span>
            <span v-else style="color:#909399">-</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('sale.itemCount')">
          <template #default="scope">{{ scope.row.items ? scope.row.items.length : 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" :label="t('common.status')">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column :label="t('common.actions')" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">{{ t('common.view') }}</el-button>
            <el-button v-if="activeTab === 'draft'" size="small" type="primary" @click="handleEdit(scope.row)">{{ t('common.edit') }}</el-button>
            <el-button v-if="activeTab === 'draft'" size="small" type="danger" @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
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

    <!-- 订单编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" destroy-on-close>
      <el-form :model="formData" label-width="100px">
        <el-form-item :label="t('sale.orderNo')">
          <el-input v-model="formData.orderNo" :disabled="true" :placeholder="'自动生成'" />
        </el-form-item>
        <el-form-item label="单据名称" required>
          <el-input v-model="formData.docName" placeholder="请输入单据名称" :disabled="isViewing" />
        </el-form-item>
        <el-form-item :label="t('sale.customer')" v-if="isViewing && (formData.customerName || formData.partnerName)">
          <el-input :value="formData.customerName || formData.partnerName" disabled />
        </el-form-item>
        <el-form-item :label="t('sale.customer')" v-else required>
          <el-select v-model="formData.customerId" :placeholder="t('common.select') + t('sale.customer')" filterable style="width:100%" :disabled="isViewing">
            <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出货仓库" v-if="isViewing && formData.warehouseName">
          <el-input :value="formData.warehouseName" disabled />
        </el-form-item>
        <el-form-item label="出货仓库" v-else required>
          <el-select v-model="formData.warehouseId" placeholder="请选择出货仓库" filterable style="width:100%" :disabled="isViewing">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('sale.items')">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsName" :label="t('sale.productName')" />
              <el-table-column prop="spec" :label="t('sale.spec')" />
              <el-table-column prop="unit" :label="t('sale.unit')" />
              <el-table-column prop="quantity" :label="t('table.quantity')">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" size="small" :disabled="isViewing" />
                </template>
              </el-table-column>
              <el-table-column prop="price" :label="t('sale.unitPrice')">
                <template #default="scope">
                  <el-input-number v-model="scope.row.price" :min="0" :precision="2" size="small" :disabled="isViewing" />
                </template>
              </el-table-column>
              <el-table-column :label="t('table.amount')">
                <template #default="scope">¥{{ ((scope.row.quantity || 0) * (scope.row.price || 0)).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column :label="t('common.actions')">
                <template #default="scope">
                  <el-button v-if="!isViewing" size="small" type="danger" @click="removeItem(scope.$index)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button v-if="!isViewing" size="small" type="primary" @click="openProductDialog" style="margin-top:8px">{{ t('sale.addProduct') }}</el-button>
          </div>
        </el-form-item>
        <el-form-item label="总金额">
          <span style="font-weight:bold;font-size:16px;color:#409EFF">¥{{ totalAmount.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="实付金额" v-if="isViewing">
          <span v-if="formData.payAmount !== undefined && formData.payAmount !== null" style="font-weight:bold;font-size:16px;color:#67C23A">
            ¥{{ Number(formData.payAmount).toFixed(2) }}
          </span>
          <span v-else style="color:#909399">未录入</span>
        </el-form-item>
        <el-form-item :label="t('table.remark')">
          <el-input v-model="formData.remark" type="textarea" :rows="3" :disabled="isViewing" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isViewing ? t('common.close') : t('common.cancel') }}</el-button>
        <el-button v-if="!isViewing" type="primary" @click="handleSave" :loading="saving">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 商品选择对话框 -->
    <el-dialog :title="t('sale.selectProduct')" v-model="productDialogVisible" width="700px" destroy-on-close>
      <div class="product-search">
        <el-input v-model="productSearch" :placeholder="t('common.input') + t('sale.productName')" clearable @input="searchProducts" />
      </div>
      <el-table :data="productList" border stripe v-loading="productLoading" @row-click="onProductSelect" highlight-current-row style="cursor:pointer;margin-top:12px">
        <el-table-column prop="goodsName" :label="t('sale.productName')" />
        <el-table-column prop="packageSpec" :label="t('sale.spec')" />
        <el-table-column prop="saleUnitSymbol" :label="t('sale.unit')" />
        <el-table-column :label="t('sale.unitPrice')">
          <template #default="scope">¥{{ Number(scope.row.salePrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stockQty" :label="t('table.stock')" />
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
import { saleApi, goodsApi, docApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const customerList = ref([])
const activeTab = ref('formal')
const isViewing = ref(false)

const warehouses = ref([
  { id: 1, code: 'MAIN', name: '主仓库' },
  { id: 2, code: 'BACKUP', name: '备用仓库' }
])

const searchForm = reactive({ orderNo: '', customerName: '' })

const formData = reactive({
  id: '', orderNo: '', docName: '', customerId: '', customerName: '', warehouseId: '', warehouseName: '', items: [], remark: '', payAmount: null
})

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + Number((item.quantity || 0) * (item.price || 0)), 0)
})

const statusTypes = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger', 'PENDING': 'warning', 'APPROVED': 'primary', 'IN_STOCK': 'success', 'CANCELLED': 'danger', 'COMPLETED': 'success' }
const statusLabels = { 0: '草稿', 1: '待审核', 2: '已审核', 3: '已完成', 4: '已取消', 'PENDING': '待审核', 'APPROVED': '已审核', 'IN_STOCK': '已入库', 'CANCELLED': '已取消', 'COMPLETED': '已完成' }

const getStatusType = (s) => statusTypes[s] || 'info'
const getStatusLabel = (s) => statusLabels[s] || '未知'

const loadList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'formal') {
      const params = { pageNum: currentPage.value, pageSize: pageSize.value }
      const res = await saleApi.getOrderList(params)
      if (res.data) {
        const records = (res.data.list || res.data).map(item => ({
          ...item,
          docNo: item.orderNo,
          partnerName: item.customerName,
          itemCount: item.items ? item.items.length : 0,
          status: statusToString(item.orderStatus)
        }))
        tableData.value = records
        total.value = res.data.total || 0
      }
    } else {
      const params = {
        docType: 'SALE_ORDER',
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
      if (activeTab.value === 'pending') { params.status = '1' }
      else { params.status = '0' }
      const res = await docApi.getPage(params)
      if (res.data) {
        const records = res.data.records || res.data || []
        tableData.value = records
        total.value = res.data.total || 0
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const statusMap = { 1: 'PENDING', 2: 'APPROVED', 3: 'IN_STOCK', 4: 'CANCELLED', 5: 'COMPLETED' }
const statusToString = (int) => statusMap[int] || ''

const handleTabChange = () => {
  currentPage.value = 1
  loadList()
}

const loadCustomers = async () => {
  try {
    const res = await saleApi.getCustomerList({ pageNum: 1, pageSize: 999 })
    if (res.data) {
      customerList.value = res.data.list || []
    }
  } catch (e) { /* ignore */ }
}

const handleSearch = () => { currentPage.value = 1; loadList() }
const handleReset = () => { searchForm.orderNo = ''; searchForm.customerName = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  isViewing.value = false
  dialogTitle.value = t('sale.addOrder')
  formData.id = ''
  formData.orderNo = ''
  formData.customerId = ''
  formData.customerName = ''
  formData.warehouseId = ''
  formData.warehouseName = ''
  formData.items = []
  formData.remark = ''
  formData.payAmount = null
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  isViewing.value = false
  dialogTitle.value = t('sale.editOrder')
  try {
    const res = await docApi.getById(row.id)
    if (res.data) {
      const d = res.data
      formData.id = d.id
      formData.orderNo = d.docNo
      formData.customerId = d.partnerId
      formData.customerName = d.partnerName || ''
      formData.warehouseId = d.warehouseId || ''
      formData.warehouseName = d.warehouseName || ''
      formData.items = (d.items || []).map(item => ({
        goodsId: Number(item.goodsId) || null,
        goodsName: item.goodsName,
        spec: item.goodsSpec || '',
        unit: item.goodsUnit || '件',
        quantity: item.quantity,
        price: item.unitPrice || 0
      }))
      formData.payAmount = d.payAmount
      formData.remark = d.remark || ''
    }
  } catch (e) {
    ElMessage.error('获取订单详情失败')
  }
  dialogVisible.value = true
}

const handleView = async (row) => {
  isViewing.value = true
  dialogTitle.value = t('sale.viewOrder')
  Object.assign(formData, row)
  formData.customerName = row.customerName || row.partnerName || ''
  formData.orderNo = row.orderNo || row.docNo || ''
  formData.warehouseName = row.warehouseName || ''
  formData.payAmount = row.payAmount
  formData.items = (row.items || []).map(item => ({
    goodsId: Number(item.goodsId) || null,
    goodsName: item.goodsName,
    spec: item.goodsSpec || item.spec || '',
    unit: item.goodsUnit || item.unit || '件',
    quantity: item.quantity,
    price: item.unitPrice || item.price || 0
  }))
  formData.remark = row.remark || ''
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
  if (!formData.warehouseId) {
    ElMessage.warning('请选择出货仓库')
    return
  }
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    const selectedCustomer = customerList.value.find(c => c.id === formData.customerId)
    const selectedWarehouse = warehouses.value.find(w => w.id === formData.warehouseId)
    const userId = localStorage.getItem('userId') || '1'
    const userName = localStorage.getItem('userName') || '当前用户'
    const payload = {
      docType: 'SALE_ORDER',
      docName: formData.docName || '销售订单' + new Date().toISOString().slice(0,10).replace(/-/g,''),
      creatorId: userId,
      creatorName: userName,
      partnerId: String(formData.customerId),
      partnerName: selectedCustomer?.customerName || '',
      warehouseId: Number(formData.warehouseId) || null,
      warehouseCode: selectedWarehouse?.code || '',
      warehouseName: selectedWarehouse?.name || '',
      totalAmount: totalAmount.value,
      remark: formData.remark,
      items: formData.items.map((item, idx) => ({
        goodsId: Number(item.goodsId) || null,
        goodsCode: item.goodsCode || '',
        skuCode: item.skuCode || '',
        skuName: item.skuName || '',
        barcode: item.barcode || '',
        goodsName: item.goodsName || '',
        goodsSpec: item.spec || '',
        unitCode: item.unitCode || '',
        goodsUnit: item.unit || '件',
        conversionRate: Number(item.conversionRate || 1),
        unitPrice: Number(item.price || 0),
        quantity: Number(item.quantity || 1),
        amount: Number((item.quantity || 0) * (item.price || 0)),
        lineNo: idx + 1
      }))
    }
    if (isEdit.value) {
      await docApi.updateDraft(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await docApi.submitSaleOrder(payload)
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

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该订单吗？', '提示', { type: 'warning' }).then(async () => {
    await docApi.deleteDraft(row.id)
    ElMessage.success('删除成功')
    loadList()
  }).catch(() => {})
}

const handlePageChange = (page) => { currentPage.value = page; loadList() }

const removeItem = (index) => { formData.items.splice(index, 1) }

const validateItems = (items) => {
  if (!items || items.length === 0) { ElMessage.warning('请添加商品明细'); return false }
  for (let i = 0; i < items.length; i++) {
    if (!items[i].quantity || Number(items[i].quantity) <= 0) { ElMessage.warning(`第${i + 1}行数量必须大于0`); return false }
  }
  return true
}

// 商品选择相关
const productDialogVisible = ref(false)
const productSearch = ref('')
const productList = ref([])
const productLoading = ref(false)
const productTotal = ref(0)
const productPage = ref(1)
const productPageSize = ref(10)

const openProductDialog = () => {
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
    spec: row.packageSpec || '',
    unitCode: row.saleUnitSymbol || '',
    unit: row.saleUnitName || '件',
    conversionRate: Number(row.conversionRate || 1),
    quantity: 1,
    price: Number(row.salePrice || 0).toFixed(2),
    amount: 0,
    taxRate: 0,
    taxAmount: 0
  })
  productDialogVisible.value = false
  ElMessage.success('已添加商品')
}

const onProductPageChange = (page) => {
  productPage.value = page
  searchProducts()
}

onMounted(() => {
  loadList()
  loadCustomers()
})
</script>

<style scoped>
.sale-order { padding: 20px; }
.search-bar { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.search-form :deep(.el-form-item) { margin-right: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-container { background: #fff; border-radius: 8px; padding: 16px; }
.items-table { margin-top: 8px; }
.product-search { margin-bottom: 0; }
</style>