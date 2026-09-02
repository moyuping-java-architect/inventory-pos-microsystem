<template>
  <div class="sale-out">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="出库单号">
          <el-input v-model="searchForm.outNo" placeholder="请输入出库单号" clearable />
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}出库</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="docNo" label="出库单号" />
        <el-table-column prop="docName" label="单据名称" />
        <el-table-column prop="partnerName" label="客户" />
        <el-table-column prop="warehouseName" label="出库仓库" />
        <el-table-column prop="totalAmount" label="出库金额">
          <template #default="scope">¥{{ scope.row.totalAmount ? scope.row.totalAmount.toFixed(2) : '0.00' }}</template>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" destroy-on-close>
      <el-form :model="formData" label-width="100px">
        <el-form-item label="出库单号">
          <el-input v-model="formData.outNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="单据名称" required>
          <el-input v-model="formData.docName" placeholder="请输入单据名称" :disabled="isViewing" />
        </el-form-item>
        <el-form-item label="出库类型" v-if="!isViewing">
          <el-radio-group v-model="isSelfUse" :disabled="isViewing">
            <el-radio :label="false">关联销售订单</el-radio>
            <el-radio :label="true">自用单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联订单" v-if="isViewing && formData.orderNo">
          <el-input :value="formData.orderNo" disabled />
        </el-form-item>
        <el-form-item label="关联订单" v-else>
          <el-select v-model="formData.orderNo" placeholder="不选择即为自用单" filterable clearable style="width:100%" :disabled="isViewing || isSelfUse" :teleported="false" @change="handleOrderChange">
            <el-option v-for="o in orderList" :key="o.id" :label="o.orderNo + (o.docName ? ' - ' + o.docName : '')" :value="o.orderNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户" v-if="isViewing && (formData.customerName || formData.partnerName)">
          <el-input :value="formData.customerName || formData.partnerName" disabled />
        </el-form-item>
        <el-form-item label="客户" v-else required>
          <el-select v-model="formData.customerId" placeholder="请选择客户" filterable style="width:100%" :disabled="isViewing" :teleported="false">
            <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库仓库" v-if="isViewing && formData.warehouseName">
          <el-input :value="formData.warehouseName" disabled />
        </el-form-item>
        <el-form-item label="出库仓库" v-else>
          <el-select v-model="formData.warehouseId" placeholder="请选择出库仓库" :disabled="isViewing" :teleported="false">
            <el-option v-for="warehouse in warehouses" :key="warehouse.id" :label="warehouse.warehouseName" :value="warehouse.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库明细">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsCode" label="商品编码" width="120" />
              <el-table-column prop="skuCode" label="SKU编码" width="120" />
              <el-table-column prop="goodsName" label="商品名称" />
              <el-table-column prop="goodsSpec" label="规格" />
              <el-table-column prop="unit" label="单位" width="80" />
              <el-table-column prop="quantity" label="出库数量">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" size="small" :disabled="isViewing" />
                </template>
              </el-table-column>
              <el-table-column prop="price" label="单价">
                <template #default="scope">
                  <el-input-number v-model="scope.row.price" :min="0" :precision="2" size="small" :disabled="isViewing" />
                </template>
              </el-table-column>
              <el-table-column :label="t('common.actions')">
                <template #default="scope">
                  <el-button v-if="!isViewing" size="small" type="danger" @click="removeItem(scope.$index)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button v-if="!isViewing" size="small" type="primary" @click="addItem" style="margin-top:8px">添加商品</el-button>
          </div>
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

    <el-dialog :title="t('stock.selectProduct')" v-model="productDialogVisible" width="800px" destroy-on-close append-to-body>
      <div class="product-search">
        <el-input v-model="productSearch" :placeholder="t('common.input') + t('stock.goodsName')" clearable @input="searchProducts" />
      </div>
      <el-table :data="productList" border stripe v-loading="productLoading" highlight-current-row style="margin-top:12px">
        <el-table-column prop="goodsCode" :label="t('stock.goodsCode')" width="120" />
        <el-table-column prop="skuCode" :label="'SKU编码'" width="120" />
        <el-table-column prop="goodsName" :label="t('stock.goodsName')" min-width="140" />
        <el-table-column prop="goodsSpec" :label="t('stock.goodsSpec')" width="100" />
        <el-table-column prop="unit" :label="t('stock.goodsUnit')" width="70" />
        <el-table-column :label="t('table.stock')" width="90">
          <template #default="scope">{{ Number(scope.row.quantity || 0) }}</template>
        </el-table-column>
        <el-table-column :label="t('stock.unitPrice')" width="100">
          <template #default="scope">¥{{ Number(scope.row.avgCostPrice || 0).toFixed(2) }}</template>
        </el-table-column>
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
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { docApi, saleApi, stockApi } from '../../api'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const route = useRoute()
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
const orderList = ref([])
const warehouses = ref([])
const productDialogVisible = ref(false)
const productSearch = ref('')
const productList = ref([])
const productLoading = ref(false)
const productTotal = ref(0)
const productPage = ref(1)
const productPageSize = ref(10)
const activeTab = ref('formal')
const isViewing = ref(false)
const isSelfUse = ref(false)
const selfUseMode = computed(() => route.path === '/sale/self-use-out')
watch(isSelfUse, (val) => {
  if (val) {
    formData.orderNo = ''
    formData.docName = '自用出库单' + new Date().toISOString().slice(0, 10).replace(/-/g, '')
  }
})

const statusLabelMap = computed(() => ({ '0': '草稿', '1': '待审批', '2': '已审核', '3': '已完成' }))
const statusTypeMap = computed(() => ({ '0': 'info', '1': 'warning', '2': 'success', '3': '' }))
const getStatusLabel = (s) => statusLabelMap.value[String(s)] || '未知'
const getStatusType = (s) => statusTypeMap.value[String(s)] || 'info'

const searchForm = reactive({ outNo: '', customerName: '' })

const formData = reactive({
  id: '', outNo: '', docName: '', orderNo: '', customerId: '', customerName: '', warehouseId: '', warehouseName: '', items: [], remark: ''
})

const loadList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'formal') {
      const params = { pageNum: currentPage.value, pageSize: pageSize.value }
      const res = selfUseMode.value ? await saleApi.getSelfUseOutList(params) : await saleApi.getOutList(params)
      if (res.data) {
        tableData.value = (res.data.list || []).map(item => ({
          ...item,
          id: item.id,
          docNo: item.outNo,
          partnerName: item.customerName,
          warehouseName: item.warehouseName,
          orderNo: item.orderNo,
          remark: item.remark,
          docDate: item.outDate,
          itemCount: item.items ? item.items.length : 0,
          totalQuantity: item.items ? item.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0) : 0,
          totalAmount: selfUseMode.value ? Number(item.totalAmount || 0) : (item.items ? item.items.reduce((sum, item) => sum + Number(item.price * item.quantity || 0), 0) : 0),
          taxAmount: selfUseMode.value ? Number(item.taxAmount || 0) : (item.items ? item.items.reduce((sum, item) => sum + Number(item.taxAmount || 0), 0) : 0),
          creator: item.creator,
          createTime: item.createTime,
          status: item.status
        }))
        total.value = res.data.total || 0
      }
    } else {
      const params = { docType: 'SALE_OUT', pageNum: currentPage.value, pageSize: pageSize.value }
      if (activeTab.value === 'pending') { params.status = '1' }
      else { params.status = '0' }
      if (selfUseMode.value) { params.saleType = 2 }
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
    console.error('加载出库单失败:', e)
  } finally {
    loading.value = false
  }
}

const statusMap = { 1: 'PENDING', 2: 'APPROVED', 3: 'IN_STOCK', 4: 'CANCELLED', 5: 'COMPLETED' }
const statusToString = (int) => statusMap[int] || ''

const handleTabChange = () => { currentPage.value = 1; loadList() }

const handleSearch = () => { currentPage.value = 1; loadList() }
const handleReset = () => { searchForm.outNo = ''; searchForm.customerName = ''; handleSearch() }

const loadCustomers = async () => {
  try {
    const res = await saleApi.getCustomerList({ pageNum: 1, pageSize: 999 })
    customerList.value = res.data?.list || res.data || []
  } catch (e) { console.error('加载客户失败', e) }
}

const loadOrders = async () => {
  try {
    const res = await saleApi.getOrderList({ pageNum: 1, pageSize: 999, orderStatus: 2 })
    orderList.value = res.data?.list || res.data || []
  } catch (e) { console.error('加载订单失败', e) }
}

const loadWarehouses = async () => {
  try {
    const res = await request.get('/psi/admin/warehouse/list', { params: { pageNum: 1, pageSize: 999 } })
    if (res.code === 200) {
      warehouses.value = res.list || []
    }
  } catch (e) { console.error('加载仓库失败', e) }
}

const handleOrderChange = async (orderNo) => {
  if (!orderNo) return
  isSelfUse.value = false
  const selectedOrder = orderList.value.find(o => o.orderNo === orderNo)
  if (!selectedOrder) return
  
  // 自动填充客户
  if (selectedOrder.customerId) {
    formData.customerId = selectedOrder.customerId
    formData.customerName = selectedOrder.customerName || selectedOrder.partnerName || ''
  }
  
  // 自动填充仓库
  if (selectedOrder.warehouseId) {
    formData.warehouseId = selectedOrder.warehouseId
    formData.warehouseName = selectedOrder.warehouseName || ''
  }
  
  // 自动带出订单明细
  if (selectedOrder.items && selectedOrder.items.length > 0) {
    formData.items = selectedOrder.items.map(item => ({
      goodsId: item.goodsId,
      goodsCode: item.goodsCode || '',
      goodsName: item.goodsName || '',
      spec: item.goodsSpec || item.spec || '',
      unit: item.goodsUnit || item.unit || '件',
      quantity: item.quantity || 1,
      price: item.unitPrice || item.price || 0,
      taxRate: item.taxRate || 0,
      taxAmount: item.taxAmount || 0,
      amount: item.amount || 0
    }))
  } else {
    // 如果订单列表中没有明细，尝试查询订单详情
    try {
      const res = await saleApi.getOrderById(selectedOrder.id)
      if (res.data && res.data.items) {
        formData.items = res.data.items.map(item => ({
          goodsId: item.goodsId,
          goodsCode: item.goodsCode || '',
          goodsName: item.goodsName || '',
          spec: item.goodsSpec || item.spec || '',
          unit: item.goodsUnit || item.unit || '件',
          quantity: item.quantity || 1,
          price: item.unitPrice || item.price || 0,
          taxRate: item.taxRate || 0,
          taxAmount: item.taxAmount || 0,
          amount: item.amount || 0
        }))
      }
    } catch (e) { console.error('加载订单明细失败', e) }
  }
}

const handleAdd = () => {
  isEdit.value = false
  isViewing.value = false
  isSelfUse.value = selfUseMode.value
  dialogTitle.value = selfUseMode.value ? '新增自用出库' : '新增销售出库'
  formData.id = ''
  formData.outNo = 'SOUT' + Date.now()
  formData.orderNo = ''
  formData.docName = ''
  formData.customerId = ''
  formData.customerName = ''
  formData.warehouseName = ''
  formData.items = []
  formData.remark = ''
  loadCustomers()
  loadOrders()
  loadWarehouses()
  if (!formData.warehouseId && warehouses.value.length > 0) {
    formData.warehouseId = warehouses.value[0].id
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  isViewing.value = false
  dialogTitle.value = selfUseMode.value ? '编辑自用出库' : '编辑销售出库'
  loadCustomers()
  loadOrders()
  loadWarehouses()
  try {
    const res = await docApi.getById(row.id)
    if (res.data) Object.assign(formData, res.data)
    isSelfUse.value = !formData.orderNo
  } catch (e) {
    ElMessage.error('获取出库单详情失败')
  }
  dialogVisible.value = true
}

const handleView = async (row) => {
  isViewing.value = true
  dialogTitle.value = selfUseMode.value ? '查看自用出库' : '查看销售出库'
  Object.assign(formData, row)
  isSelfUse.value = !row.orderNo
  formData.customerName = row.customerName || row.partnerName || ''
  formData.warehouseName = row.warehouseName || ''
  formData.orderNo = row.orderNo || ''
  loadWarehouses()
  if (activeTab.value === 'formal') {
    try {
      const res = selfUseMode.value ? await saleApi.getSelfUseOutById(row.id) : await saleApi.getOutById(row.id)
      if (res.data) {
        Object.assign(formData, res.data)
        formData.items = (res.data.items || []).map(item => ({
          ...item,
          goodsCode: item.goodsCode || '',
          skuCode: item.skuCode || '',
          goodsSpec: item.goodsSpec || item.spec || '',
          unit: item.goodsUnit || item.unit || '件',
          quantity: item.outQuantity || item.quantity || 0,
          price: item.unitPrice || item.price || 0
        }))
      }
    } catch (e) { console.error('加载正式单据明细失败', e) }
  } else if (formData.items) {
    formData.items = formData.items.map(item => ({
      ...item,
      goodsCode: item.goodsCode || '',
      skuCode: item.skuCode || '',
      goodsSpec: item.goodsSpec || item.spec || '',
      unit: item.goodsUnit || item.unit || '件',
      quantity: item.outQuantity || item.quantity || 0,
      price: item.unitPrice || item.price || 0
    }))
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!isSelfUse.value && !formData.customerId) { ElMessage.warning('请选择客户'); return }
  if (!formData.warehouseId) { ElMessage.warning('请选择出库仓库'); return }
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    if (isSelfUse.value) {
      formData.orderNo = ''
    }
    const selectedWarehouse = warehouses.value.find(w => w.id === formData.warehouseId)
    const userId = localStorage.getItem('userId') || '1'
    const userName = localStorage.getItem('userName') || '当前用户'
    const customer = customerList.value.find(c => c.id === formData.customerId)
    const payload = {
      id: formData.id,
      docType: 'SALE_OUT',
      docName: formData.docName || (selfUseMode.value ? '自用出库' : '销售出库') + new Date().toISOString().slice(0,10).replace(/-/g,''),
      saleType: isSelfUse.value ? 2 : 1,
      creatorId: userId,
      creatorName: userName,
      docNo: formData.outNo,
      partnerId: isSelfUse.value ? (formData.customerId ? String(formData.customerId) : '0') : String(formData.customerId),
      partnerName: isSelfUse.value ? (customer?.customerName || '自用') : (customer?.customerName || ''),
      orderNo: formData.orderNo,
      warehouseId: formData.warehouseId,
      warehouseCode: selectedWarehouse?.warehouseCode || '',
      warehouseName: selectedWarehouse?.warehouseName || '',
      totalAmount: formData.items.reduce((s, i) => s + Number(i.price * i.quantity || 0), 0),
      totalQuantity: formData.items.reduce((s, i) => s + Number(i.quantity || 0), 0),
      taxAmount: formData.items.reduce((s, i) => s + Number(i.taxAmount || 0), 0),
      remark: formData.remark,
      docDate: formData.outDate,
      itemCount: formData.items.length,
      status: '0',
      creator: userName,
      createTime: new Date().toISOString(),
      items: formData.items.map((item, idx) => {
        const amount = Number(item.price || 0) * Number(item.quantity || 0)
        const taxRate = Number(item.taxRate || 0)
        const taxAmount = amount * taxRate
        return {
          goodsId: Number(item.goodsId) || null,
          goodsCode: item.goodsCode || '',
          skuCode: item.skuCode || '',
          skuName: item.skuName || '',
          barcode: item.barcode || '',
          goodsName: item.goodsName || '',
          goodsSpec: item.goodsSpec || '',
          unitCode: item.unitCode || item.unit || '',
          goodsUnit: item.unit || '件',
          conversionRate: Number(item.conversionRate || 1),
          unitPrice: Number(item.price || 0),
          quantity: Number(item.quantity || 1),
          taxRate: taxRate,
          taxAmount: taxAmount,
          amount: amount,
          lineNo: idx + 1
        }
      })
    }
    if (isEdit.value) {
      await docApi.updateDraft(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await docApi.submitSaleOut(payload)
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
  ElMessageBox.confirm('确定删除该出库单吗？', '提示', { type: 'warning' }).then(async () => {
    await docApi.deleteDraft(row.id)
    ElMessage.success('删除成功')
    loadList()
  }).catch(() => {})
}

const handlePageChange = (page) => { currentPage.value = page; loadList() }

const addItem = () => {
  if (!formData.warehouseId) {
    ElMessage.warning('请先选择出库仓库')
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
    const selectedWarehouse = warehouses.value.find(w => w.id === formData.warehouseId)
    const res = await stockApi.getStockForSelector({
      warehouseCode: selectedWarehouse?.warehouseCode,
      goodsName: productSearch.value || undefined,
      pageNum: productPage.value,
      pageSize: productPageSize.value
    })
    if (res && res.data) {
      productList.value = res.data.list || []
      productTotal.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载库存商品失败', e)
  } finally {
    productLoading.value = false
  }
}

const onProductSelect = (row) => {
  formData.items.push({
    goodsId: row.goodsId || null,
    goodsCode: row.goodsCode || '',
    skuCode: row.skuCode || '',
    skuName: row.skuName || row.goodsName || '',
    barcode: '',
    goodsName: row.goodsName || '',
    goodsSpec: row.goodsSpec || '',
    unitCode: row.unit || '',
    unit: row.unit || '件',
    conversionRate: 1,
    quantity: 1,
    price: Number(row.avgCostPrice || 0),
    taxRate: 0,
    taxAmount: 0,
    amount: 0
  })
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
    if (!items[i].skuCode && !items[i].goodsCode) { ElMessage.warning(`第${i + 1}行商品数据异常，请选择有效商品`); return false }
    if (!items[i].quantity || Number(items[i].quantity) <= 0) { ElMessage.warning(`第${i + 1}行出库数量必须大于0`); return false }
  }
  return true
}

onMounted(() => { loadList(); loadWarehouses() })
</script>

<style scoped>
.sale-out { padding: 20px; }
.search-bar { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.search-form :deep(.el-form-item) { margin-right: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-container { background: #fff; border-radius: 8px; padding: 16px; }
.items-table { margin-top: 8px; }
.product-search { margin-bottom: 12px; }
</style>