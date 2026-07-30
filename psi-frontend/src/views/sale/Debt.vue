<template>
  <div class="debt">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('menu.debtNo')">
          <el-input v-model="searchForm.debtNo" :placeholder="t('common.input') + t('menu.debtNo')" clearable />
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
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('menu.addDebt') }}</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="docNo" :label="t('menu.debtNo')" />
        <el-table-column prop="partnerName" :label="t('sale.customerName')" />
        <el-table-column prop="totalAmount" :label="t('menu.debtAmount')">
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
        <el-form-item :label="t('menu.debtNo')">
          <el-input v-model="formData.debtNo" disabled />
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
            <el-form-item :label="t('menu.debtDate')">
              <el-date-picker v-model="formData.debtDate" type="date" style="width:100%" :disabled="isViewing" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('table.items')">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsCode" :label="t('stock.goodsCode')" width="100" />
              <el-table-column prop="goodsName" :label="t('stock.goodsName')" min-width="120" />
              <el-table-column prop="goodsSpec" :label="t('stock.goodsSpec')" width="100" />
              <el-table-column prop="goodsUnit" :label="t('stock.goodsUnit')" width="70" />
              <el-table-column prop="quantity" :label="t('stock.quantity')" width="100">
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

const searchForm = reactive({ debtNo: '', customerName: '' })

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
  id: '', debtNo: '', customerId: '', debtDate: '', items: [], remark: ''
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
    const params = { docType: 'DEBT_ORDER', pageNum: currentPage.value, pageSize: pageSize.value }
    if (activeTab.value === 'formal') { params.status = '2,3' }
    else if (activeTab.value === 'pending') { params.status = '1' }
    else { params.status = '0' }
    const res = await docApi.getPage(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载欠款单失败:', e)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => { currentPage.value = 1; loadList() }

const handleSearch = () => { currentPage.value = 1; loadList() }
const handleReset = () => { searchForm.debtNo = ''; searchForm.customerName = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  isViewing.value = false
  dialogTitle.value = t('menu.addDebt')
  formData.id = ''
  formData.debtNo = 'DS' + Date.now()
  formData.customerId = ''
  formData.debtDate = ''
  formData.items = []
  formData.remark = ''
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  isViewing.value = false
  dialogTitle.value = '编辑欠款单'
  try {
    const res = await docApi.findById(row.id)
    if (res.data) Object.assign(formData, res.data)
  } catch (e) {
    ElMessage.error('获取欠款单详情失败')
  }
  dialogVisible.value = true
}

const handleView = (row) => {
  isViewing.value = true
  dialogTitle.value = '查看欠款单'
  Object.assign(formData, row)
  formData.customerName = row.customerName || row.partnerName || ''
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.customerId) { ElMessage.warning('请选择客户'); return }
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    const payload = {
      docType: 'DEBT_ORDER',
      partnerId: String(formData.customerId),
      partnerName: customerList.value.find(c => c.id === formData.customerId)?.customerName || '',
      totalAmount: totalAmount.value,
      taxAmount: formData.items.reduce((s, i) => s + Number(i.taxAmount || 0), 0),
      remark: formData.remark,
      docDate: formData.debtDate,
      items: formData.items.map((item, idx) => ({
        goodsId: Number(item.goodsId) || null,
        goodsCode: item.goodsCode || '',
        barcode: item.barcode || '',
        goodsName: item.goodsName || '',
        goodsSpec: item.goodsSpec || '',
        goodsUnit: item.goodsUnit || '件',
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
      await docApi.createAndSubmit(payload)
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
    barcode: row.barcode || '',
    goodsName: row.goodsName,
    goodsSpec: row.packageSpec || '',
    goodsUnit: row.saleUnitName || '件',
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
    if (items[i].goodsId == null) { ElMessage.warning(`第${i + 1}行商品数据异常，请确认关联订单数据完整`); return false }
    if (!items[i].quantity || Number(items[i].quantity) <= 0) { ElMessage.warning(`第${i + 1}行数量必须大于0`); return false }
  }
  return true
}

onMounted(() => { loadList(); loadCustomers() })
</script>

<style scoped>
.debt { padding: 20px; }
.search-bar { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-container { background: #fff; border-radius: 8px; padding: 16px; }
.items-table { overflow-x: auto; }
</style>