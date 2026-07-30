<template>
  <div class="account">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="账户名称">
          <el-input v-model="searchForm.name" placeholder="请输入账户名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}账户</el-button>
    </div>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="name" label="账户名称" />
        <el-table-column prop="type" label="账户类型">
          <template #default="scope">{{ getTypeLabel(scope.row.type) }}</template>
        </el-table-column>
        <el-table-column prop="bankName" label="开户银行" />
        <el-table-column prop="accountNo" label="银行账号" />
        <el-table-column prop="balance" label="账户余额" />
        <el-table-column prop="status" :label="t('common.status')">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
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
    
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="账户名称" required>
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="账户类型" required>
          <el-select v-model="formData.type" placeholder="请选择账户类型">
            <el-option label="现金" value="CASH" />
            <el-option label="银行存款" value="BANK" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="开户银行">
          <el-input v-model="formData.bankName" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="formData.accountNo" />
        </el-form-item>
        <el-form-item label="初始余额">
          <el-input type="number" v-model="formData.balance" />
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
  name: ''
})

const tableData = ref([
  { id: 1, name: '现金账户', type: 'CASH', bankName: '-', accountNo: '-', balance: '¥ 10,000.00', status: 'ACTIVE', createTime: '2024-12-01' },
  { id: 2, name: '工商银行', type: 'BANK', bankName: '工商银行', accountNo: '622202********1234', balance: '¥ 50,000.00', status: 'ACTIVE', createTime: '2024-12-02' },
  { id: 3, name: '微信钱包', type: 'WECHAT', bankName: '-', accountNo: '-', balance: '¥ 5,000.00', status: 'ACTIVE', createTime: '2024-12-03' },
  { id: 4, name: '支付宝', type: 'ALIPAY', bankName: '-', accountNo: '-', balance: '¥ 8,000.00', status: 'ACTIVE', createTime: '2024-12-04' }
])

const total = ref(10)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')

const formData = reactive({
  id: '',
  name: '',
  type: '',
  bankName: '',
  accountNo: '',
  balance: '',
  remark: ''
})

const getTypeLabel = (type) => {
  const labels = {
    'CASH': '现金',
    'BANK': '银行存款',
    'WECHAT': '微信',
    'ALIPAY': '支付宝'
  }
  return labels[type] || type
}

const handleSearch = () => {}
const handleReset = () => {
  searchForm.name = ''
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增账户'
  formData.id = ''
  formData.name = ''
  formData.type = ''
  formData.bankName = ''
  formData.accountNo = ''
  formData.balance = ''
  formData.remark = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑账户'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  dialogTitle.value = '查看账户'
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
.account {
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