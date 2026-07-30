<template>
  <div class="supplier">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.name" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="searchForm.phone" placeholder="请输入联系电话" clearable />
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
      <el-table :data="tableData" border stripe>
        <el-table-column prop="name" label="供应商名称" />
        <el-table-column prop="contact" :label="t('table.contact')" />
        <el-table-column prop="phone" :label="t('table.phone')" />
        <el-table-column prop="address" :label="t('table.address')" />
        <el-table-column prop="bankName" label="开户银行" />
        <el-table-column prop="bankAccount" label="银行账号" />
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column :label="t('common.actions')" width="150">
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
    
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="供应商名称" required>
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="formData.contact" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="formData.phone" />
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
          <el-textarea v-model="formData.remark" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const searchForm = reactive({
  name: '',
  phone: ''
})

const tableData = ref([
  { id: 1, name: '供应商A', contact: '王经理', phone: '13800138001', address: '北京市朝阳区', bankName: '工商银行', bankAccount: '622202********1234', createTime: '2024-12-01' },
  { id: 2, name: '供应商B', contact: '李总', phone: '13900139002', address: '上海市浦东新区', bankName: '建设银行', bankAccount: '622700********5678', createTime: '2024-12-03' },
  { id: 3, name: '供应商C', contact: '张经理', phone: '13700137003', address: '广州市天河区', bankName: '农业银行', bankAccount: '622848********9012', createTime: '2024-12-05' }
])

const total = ref(20)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')

const formData = reactive({
  id: '',
  name: '',
  contact: '',
  phone: '',
  address: '',
  bankName: '',
  bankAccount: '',
  remark: ''
})

const handleSearch = () => {}
const handleReset = () => {
  searchForm.name = ''
  searchForm.phone = ''
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增供应商'
  formData.id = ''
  formData.name = ''
  formData.contact = ''
  formData.phone = ''
  formData.address = ''
  formData.bankName = ''
  formData.bankAccount = ''
  formData.remark = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑供应商'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  dialogTitle.value = '查看供应商'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = () => {}
const handleSave = () => {
  dialogVisible.value = false
}

const handlePageChange = (page) => {
  currentPage.value = page
}
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
</style>