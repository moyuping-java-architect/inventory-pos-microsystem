<template>
  <div class="supplier">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contactName" placeholder="请输入联系人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}供应商</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="supplierCode" label="供应商编码" width="120" />
        <el-table-column prop="supplierName" label="供应商名称" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="bankName" label="开户银行" width="120" />
        <el-table-column prop="bankAccount" label="银行账号" width="160" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column :label="t('common.actions')" width="200" fixed="right">
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
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="formData" label-width="100px" :disabled="isView">
        <el-form-item label="供应商编码" required>
          <el-input v-model="formData.supplierCode" />
        </el-form-item>
        <el-form-item label="供应商名称" required>
          <el-input v-model="formData.supplierName" />
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="formData.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="formData.contactPhone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="formData.address" />
        </el-form-item>
        <el-form-item label="开户银行">
          <el-input v-model="formData.bankName" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="formData.bankAccount" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave" v-if="!isView">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { purchaseApi } from '../../api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const loading = ref(false)
const searchForm = reactive({
  supplierName: '',
  contactName: ''
})

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const dialogTitle = ref('')

const formData = reactive({
  id: null,
  supplierCode: '',
  supplierName: '',
  contactName: '',
  contactPhone: '',
  address: '',
  bankName: '',
  bankAccount: '',
  remark: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      supplierName: searchForm.supplierName,
      contactName: searchForm.contactName
    }
    const res = await purchaseApi.getSupplierList(params)
    tableData.value = res.list || (res.data && res.data.list) || []
    total.value = res.total || (res.data && res.data.total) || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.supplierName = ''
  searchForm.contactName = ''
  currentPage.value = 1
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.supplierCode = ''
  formData.supplierName = ''
  formData.contactName = ''
  formData.contactPhone = ''
  formData.address = ''
  formData.bankName = ''
  formData.bankAccount = ''
  formData.remark = ''
}

const handleAdd = () => {
  isEdit.value = false
  isView.value = false
  dialogTitle.value = '新增供应商'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  isView.value = false
  dialogTitle.value = '编辑供应商'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isEdit.value = false
  isView.value = true
  dialogTitle.value = '查看供应商'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (!formData.supplierCode || !formData.supplierName || !formData.contactName || !formData.contactPhone) {
      ElMessage.warning('请填写必填项')
      return
    }
    if (isEdit.value) {
      await purchaseApi.updateSupplier(formData.id, formData)
      ElMessage.success('编辑成功')
    } else {
      await purchaseApi.saveSupplier(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该供应商吗？', '提示', { type: 'warning' })
    .then(async () => {
      await purchaseApi.deleteSupplier(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.supplier {
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

.el-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
