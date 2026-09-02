<template>
  <div class="report-center">
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('report.title') }}</span>
        </div>
      </template>

      <el-form :model="form" label-width="100px" class="report-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item :label="t('report.reportType')">
              <el-select v-model="form.reportType" :placeholder="t('report.selectType')" style="width: 100%">
                <el-option
                  v-for="item in reportTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('report.format')">
              <el-radio-group v-model="form.format">
                <el-radio label="excel">Excel</el-radio>
                <el-radio label="pdf">PDF</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('report.dateRange')">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                :start-placeholder="t('report.startDate')"
                :end-placeholder="t('report.endDate')"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item :label="t('report.warehouse')">
              <el-input v-model="form.params.warehouseCode" :placeholder="t('report.warehousePlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('report.goodsCode')">
              <el-input v-model="form.params.goodsCode" :placeholder="t('report.goodsCodePlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleGenerate">
                {{ t('report.generate') }}
              </el-button>
              <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="preview-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('report.typeDescription') }}</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="currentType.label">
          {{ currentType.description }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const { t } = useI18n()

const loading = ref(false)
const dateRange = ref([])

const form = reactive({
  reportType: 'inventory',
  format: 'excel',
  params: {
    warehouseCode: '',
    goodsCode: ''
  }
})

const reportTypes = computed(() => [
  { value: 'inventory', label: t('report.typeInventory'), description: t('report.descInventory') },
  { value: 'purchase_order', label: t('report.typePurchaseOrder'), description: t('report.descPurchaseOrder') },
  { value: 'sale_order', label: t('report.typeSaleOrder'), description: t('report.descSaleOrder') },
  { value: 'stock_flow', label: t('report.typeStockFlow'), description: t('report.descStockFlow') },
  { value: 'psi_summary', label: t('report.typePsiSummary'), description: t('report.descPsiSummary') }
])

const currentType = computed(() => {
  return reportTypes.value.find(item => item.value === form.reportType) || reportTypes.value[0]
})

const getHeaders = () => {
  const locale = localStorage.getItem('locale') || 'zh-CN'
  return {
    'Accept-Language': locale,
    'Authorization': `Bearer ${localStorage.getItem('token') || ''}`,
    'X-Tenant-Id': localStorage.getItem('tenantId') || '1',
    'X-Shop-Id': localStorage.getItem('shopId') || '1',
    'X-Warehouse-Id': localStorage.getItem('warehouseId') || '1',
    'X-Update-User-Id': localStorage.getItem('userId') || '1',
    'X-Update-User-Name': localStorage.getItem('userName') || 'admin',
    'X-Role-Id': localStorage.getItem('roleId') || '1',
    'X-Role-Name': localStorage.getItem('roleName') || '管理员',
    'X-Permissions': localStorage.getItem('permissions') || '*'
  }
}

const handleGenerate = async () => {
  loading.value = true
  try {
    const params = { ...form.params }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const response = await axios.post('/psi/report/generate', {
      reportType: form.reportType,
      format: form.format,
      params,
      data: []
    }, {
      headers: getHeaders(),
      responseType: 'blob'
    })

    const contentType = response.headers['content-type'] || 'application/octet-stream'
    const extension = form.format === 'excel' ? 'xlsx' : 'pdf'
    const filename = `${form.reportType}_${formatDate(new Date())}.${extension}`

    const blob = new Blob([response.data], { type: contentType })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success(t('report.generateSuccess'))
  } catch (error) {
    console.error('Generate report failed:', error)
    ElMessage.error(t('report.generateFailed'))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  form.reportType = 'inventory'
  form.format = 'excel'
  form.params = {
    warehouseCode: '',
    goodsCode: ''
  }
  dateRange.value = []
}

const formatDate = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}${m}${d}${h}${min}${s}`
}
</script>

<style scoped>
.report-center {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
}

.report-form {
  margin-top: 10px;
}

.preview-card {
  margin-top: 20px;
}
</style>
