<template>
  <div class="customer">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('table.name')">
          <el-input v-model="searchForm.customerName" :placeholder="t('common.input') + t('table.name')" clearable />
        </el-form-item>
        <el-form-item :label="t('table.phone')">
          <el-input v-model="searchForm.contactPhone" :placeholder="t('common.input') + t('table.phone')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') + t('sale.customer') }}</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="customerCode" :label="t('sale.customerCode')" />
        <el-table-column prop="customerName" :label="t('table.name')" />
        <el-table-column prop="contactName" :label="t('table.contact')" />
        <el-table-column prop="contactPhone" :label="t('table.phone')" />
        <el-table-column prop="address" :label="t('table.address')" />
        <el-table-column prop="creditLimit" :label="t('table.creditLimit')">
          <template #default="scope">¥{{ scope.row.creditLimit ? scope.row.creditLimit.toFixed(2) : '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column :label="t('common.actions')" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">{{ t('common.view') }}</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">{{ t('common.edit') }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" destroy-on-close>
      <el-form :model="formData" label-width="100px">
        <el-form-item :label="t('sale.customerCode')">
          <el-input v-model="formData.customerCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="t('table.name')" required>
          <el-input v-model="formData.customerName" />
        </el-form-item>
        <el-form-item :label="t('table.contact')" required>
          <el-input v-model="formData.contactName" />
        </el-form-item>
        <el-form-item :label="t('table.phone')" required>
          <el-input v-model="formData.contactPhone" />
        </el-form-item>
        <el-form-item :label="t('table.address')">
          <el-input v-model="formData.address" />
        </el-form-item>
        <el-form-item :label="t('table.creditLimit')">
          <el-input-number v-model="formData.creditLimit" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('table.remark')">
          <el-input v-model="formData.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { saleApi } from '../../api'
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

const searchForm = reactive({ customerName: '', contactPhone: '' })

const formData = reactive({
  id: '', customerCode: '', customerName: '', contactName: '', contactPhone: '', address: '', creditLimit: '', remark: ''
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await saleApi.getCustomerList({
      customerName: searchForm.customerName || undefined,
      contactPhone: searchForm.contactPhone || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.data) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { currentPage.value = 1; loadList() }
const handleReset = () => { searchForm.customerName = ''; searchForm.contactPhone = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = t('common.add') + t('sale.customer')
  formData.id = ''
  formData.customerCode = 'CUS' + Date.now()
  formData.customerName = ''
  formData.contactName = ''
  formData.contactPhone = ''
  formData.address = ''
  formData.creditLimit = ''
  formData.remark = ''
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = t('common.edit') + t('sale.customer')
  try {
    const res = await saleApi.getCustomerById(row.id)
    if (res.data) Object.assign(formData, res.data)
  } catch (e) {
    ElMessage.error('获取客户详情失败')
  }
  dialogVisible.value = true
}

const handleView = async (row) => {
  dialogTitle.value = t('common.view') + t('sale.customer')
  try {
    const res = await saleApi.getCustomerById(row.id)
    if (res.data) Object.assign(formData, res.data)
  } catch (e) {
    ElMessage.error('获取客户详情失败')
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.customerName) { ElMessage.warning('请输入客户名称'); return }
  saving.value = true
  try {
    const payload = {
      customerCode: formData.customerCode,
      customerName: formData.customerName,
      contactName: formData.contactName,
      contactPhone: formData.contactPhone,
      address: formData.address,
      creditLimit: formData.creditLimit,
      remark: formData.remark
    }
    if (isEdit.value) {
      await saleApi.updateCustomer(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await saleApi.saveCustomer(payload)
      ElMessage.success('新增成功')
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
  ElMessageBox.confirm('确定删除该客户吗？', '提示', { type: 'warning' }).then(async () => {
    await saleApi.deleteCustomer(row.id)
    ElMessage.success('删除成功')
    loadList()
  }).catch(() => {})
}

const handlePageChange = (page) => { currentPage.value = page; loadList() }

onMounted(() => { loadList() })
</script>

<style scoped>
.customer { padding: 20px; }
.search-bar { background: #f5f7fa; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-container { background: #fff; border-radius: 8px; padding: 16px; }
</style>