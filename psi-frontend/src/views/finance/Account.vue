<template>
  <div class="account">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="账户名称">
          <el-input v-model="searchForm.accountName" placeholder="请输入账户名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <el-table :data="filteredData" border stripe v-loading="loading">
        <el-table-column prop="accountName" label="账户名称" />
        <el-table-column prop="accountType" label="账户类型">
          <template #default="scope">{{ getTypeLabel(scope.row.accountType) }}</template>
        </el-table-column>
        <el-table-column prop="accountNo" label="账号" />
        <el-table-column prop="balance" label="账户余额" />
        <el-table-column prop="storeName" label="所属门店" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column :label="t('common.actions')" width="120">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">{{ t('common.view') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="账户名称">
          <el-input v-model="formData.accountName" disabled />
        </el-form-item>
        <el-form-item label="账户类型">
          <el-input :value="getTypeLabel(formData.accountType)" disabled />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="formData.accountNo" disabled />
        </el-form-item>
        <el-form-item label="账户余额">
          <el-input v-model="formData.balance" disabled />
        </el-form-item>
        <el-form-item label="所属门店">
          <el-input v-model="formData.storeName" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" rows="3" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { financeApi } from '../../api'

const { t } = useI18n()

const searchForm = reactive({
  accountName: ''
})

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const formData = reactive({
  id: '',
  accountName: '',
  accountType: '',
  accountNo: '',
  balance: '',
  storeName: '',
  remark: ''
})

const filteredData = computed(() => {
  if (!searchForm.accountName) return tableData.value
  return tableData.value.filter(item => item.accountName && item.accountName.includes(searchForm.accountName))
})

const getTypeLabel = (type) => {
  const labels = {
    'CASH': '现金',
    'BANK': '对公卡',
    'WECHAT': '微信',
    'ALIPAY': '支付宝'
  }
  return labels[type] || type || '-'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await financeApi.getAccountList()
    tableData.value = res.list || []
  } catch (e) {
    console.error('加载账户列表失败', e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {}
const handleReset = () => {
  searchForm.accountName = ''
}

const handleView = (row) => {
  dialogTitle.value = '查看账户'
  Object.assign(formData, row)
  dialogVisible.value = true
}

onMounted(() => {
  loadList()
})
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

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
</style>
