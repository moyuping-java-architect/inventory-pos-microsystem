<template>
  <div class="adjust-price">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('goods.adjustNo')">
          <el-input v-model="searchForm.adjustNo" :placeholder="t('common.input') + t('goods.adjustNo')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('goods.addAdjust') }}</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="t('docTabs.formal')" name="formal" />
      <el-tab-pane :label="t('docTabs.pending')" name="pending" />
      <el-tab-pane :label="t('docTabs.draft')" name="draft" />
    </el-tabs>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="docNo" :label="t('goods.adjustNo')" />
        <el-table-column prop="docName" label="单据名称" />
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
        <el-form-item :label="t('goods.adjustNo')">
          <el-input v-model="formData.adjustNo" disabled />
        </el-form-item>
        <el-form-item label="单据名称" required>
          <el-input v-model="formData.docName" placeholder="请输入单据名称" :disabled="isViewing" />
        </el-form-item>
        <el-form-item :label="t('goods.adjustDate')">
          <el-date-picker v-model="formData.adjustDate" type="date" style="width:100%" :disabled="isViewing" />
        </el-form-item>
        <el-form-item :label="t('table.items')">
          <div class="items-table">
            <el-table :data="formData.items" border>
              <el-table-column prop="goodsCode" :label="t('goods.goodsCode')" width="100" />
              <el-table-column prop="goodsName" :label="t('goods.goodsName')" min-width="120" />
              <el-table-column prop="goodsSpec" :label="t('goods.spec')" width="100" />
              <el-table-column prop="goodsUnit" :label="t('goods.unit')" width="70" />
              <el-table-column prop="oldPrice" :label="t('goods.oldPrice')" width="100">
                <template #default="scope">{{ formatAmount(scope.row.oldPrice) }}</template>
              </el-table-column>
              <el-table-column prop="newPrice" :label="t('goods.newPrice')" width="120">
                <template #default="scope">
                  <el-input-number v-model="scope.row.newPrice" :min="0" :precision="2" size="small" :disabled="isViewing" />
                </template>
              </el-table-column>
              <el-table-column :label="t('common.actions')" width="60" fixed="right">
                <template #default="scope">
                  <el-button v-if="!isViewing" size="small" type="danger" @click="removeItem(scope.$index)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button v-if="!isViewing" size="small" type="primary" @click="addItem" style="margin-top:8px">{{ t('goods.selectSku') }}</el-button>
          </div>
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

    <el-dialog :title="t('goods.selectSku')" v-model="skuDialogVisible" width="700px" destroy-on-close>
      <div class="product-search">
        <el-input v-model="skuSearch" :placeholder="t('common.input') + t('goods.goodsName')" clearable @input="searchSkus" />
      </div>
      <el-table :data="skuList" border stripe v-loading="skuLoading" @row-click="onSkuSelect" highlight-current-row style="cursor:pointer;margin-top:12px">
        <el-table-column prop="skuCode" :label="t('goods.goodsCode')" width="120" />
        <el-table-column prop="goodsName" :label="t('goods.goodsName')" min-width="140" />
        <el-table-column prop="specValues" :label="t('goods.spec')" width="100" />
        <el-table-column prop="saleUnit" :label="t('goods.unit')" width="70" />
        <el-table-column :label="t('goods.salePrice')" width="100">
          <template #default="scope">¥{{ Number(scope.row.salePrice || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        :total="skuTotal"
        :page-size="skuPageSize"
        :current-page="skuPage"
        layout="prev, pager, next"
        @current-change="onSkuPageChange"
        small
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { docApi, goodsApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const searchForm = reactive({ adjustNo: '' })

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
  id: '', adjustNo: '', docName: '', adjustDate: '', items: [], remark: ''
})

const formatAmount = (val) => {
  return (Number(val) || 0).toFixed(2)
}

const loadList = async () => {
  loading.value = true
  try {
    const params = { docType: 'ADJUST_PRICE', pageNum: currentPage.value, pageSize: pageSize.value }
    if (activeTab.value === 'formal') { params.status = '2,3' }
    else if (activeTab.value === 'pending') { params.status = '1' }
    else { params.status = '0' }
    const res = await docApi.getPage(params)
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载调价单失败:', e)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => { currentPage.value = 1; loadList() }

const handleSearch = () => { loadList() }
const handleReset = () => { searchForm.adjustNo = ''; loadList() }

const handleAdd = () => {
  isEdit.value = false
  isViewing.value = false
  dialogTitle.value = t('goods.addAdjust')
  formData.id = ''
  formData.adjustNo = 'ADJ' + Date.now()
  formData.adjustDate = ''
  formData.items = []
  formData.remark = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  isViewing.value = false
  dialogTitle.value = t('goods.editAdjust')
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isViewing.value = true
  dialogTitle.value = t('goods.viewAdjust')
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await docApi.deleteDraft(row.id)
    ElMessage.success(t('common.success'))
    loadList()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

const handleSave = async () => {
  if (!validateItems(formData.items)) return
  saving.value = true
  try {
    const payload = {
      docType: 'ADJUST_PRICE',
      docName: formData.docName || '调价单' + new Date().toISOString().slice(0,10).replace(/-/g,''),
      totalAmount: 0,
      remark: formData.remark,
      docDate: formData.adjustDate,
      items: formData.items.map((item, idx) => ({
        goodsId: Number(item.goodsId) || null,
        goodsCode: item.goodsCode || '',
        skuCode: item.skuCode || '',
        skuName: item.skuName || item.goodsName || '',
        barcode: item.barcode || '',
        goodsName: item.goodsName || '',
        goodsSpec: item.specValues || '',
        unitCode: item.unitCode || '',
        goodsUnit: item.saleUnit || '',
        conversionRate: Number(item.conversionRate || 1),
        unitPrice: Number(item.newPrice || 0),
        quantity: 1,
        amount: 0,
        lineNo: idx + 1
      }))
    }
    if (isEdit.value) {
      await docApi.updateDraft(formData.id, payload)
      ElMessage.success(t('common.success'))
    } else {
      await docApi.submitAdjustPrice(payload)
      ElMessage.success(t('document.submitSuccess'))
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  } finally {
    saving.value = false
  }
}

const handlePageChange = (page) => { currentPage.value = page; loadList() }

onMounted(() => { loadList() })

const skuDialogVisible = ref(false)
const skuSearch = ref('')
const skuList = ref([])
const skuLoading = ref(false)
const skuTotal = ref(0)
const skuPage = ref(1)
const skuPageSize = ref(10)

const addItem = () => {
  skuSearch.value = ''
  skuPage.value = 1
  skuDialogVisible.value = true
  searchSkus()
}

const searchSkus = async () => {
  skuLoading.value = true
  try {
    const res = await goodsApi.getGoodsForSelector({
      goodsName: skuSearch.value || undefined,
      pageNum: skuPage.value,
      pageSize: skuPageSize.value
    })
    if (res) {
      skuList.value = res.list || []
      skuTotal.value = res.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    skuLoading.value = false
  }
}

const onSkuSelect = (row) => {
  const exists = formData.items.find(i => i.skuCode === row.skuCode)
  if (exists) {
    ElMessage.warning(t('goods.skuExists'))
    return
  }
  formData.items.push({
    goodsId: row.goodsId || row.id,
    goodsCode: row.goodsCode || '',
    skuCode: row.skuCode || row.goodsCode || '',
    skuName: row.goodsName || '',
    barcode: row.barcode || '',
    goodsName: row.goodsName || '',
    specValues: row.packageSpec || '',
    unitCode: row.saleUnitSymbol || '',
    saleUnit: row.saleUnitName || '',
    conversionRate: Number(row.conversionRate || 1),
    oldPrice: Number(row.salePrice || 0).toFixed(2),
    newPrice: Number(row.salePrice || 0).toFixed(2)
  })
  skuDialogVisible.value = false
}

const onSkuPageChange = (page) => {
  skuPage.value = page
  searchSkus()
}

const removeItem = (index) => { formData.items.splice(index, 1) }

const validateItems = (items) => {
  if (!items || items.length === 0) { ElMessage.warning(t('goods.noItems')); return false }
  for (let i = 0; i < items.length; i++) {
    if (!items[i].skuCode) { ElMessage.warning(t('goods.selectSkuTip') + (i + 1)); return false }
    if (!items[i].newPrice || Number(items[i].newPrice) <= 0) { ElMessage.warning(t('goods.priceTip') + (i + 1)); return false }
  }
  return true
}
</script>

<style scoped>
.adjust-price { padding: 20px; }
.search-bar { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-container { background: #fff; border-radius: 8px; padding: 16px; }
.items-table { overflow-x: auto; }
</style>