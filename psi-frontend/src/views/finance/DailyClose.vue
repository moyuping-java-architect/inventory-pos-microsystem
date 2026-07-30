<template>
  <div class="daily-close">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="日期">
          <el-date-picker v-model="searchForm.date" type="date" />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="未结" value="OPEN" />
            <el-option label="已结" value="CLOSED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleClose">日结</el-button>
    </div>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="saleAmount" label="当日销售额" />
        <el-table-column prop="purchaseAmount" label="当日采购额" />
        <el-table-column prop="profit" label="当日利润" />
        <el-table-column prop="cashAmount" label="现金收款" />
        <el-table-column prop="bankAmount" label="银行收款" />
        <el-table-column prop="status" :label="t('common.status')">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="操作人" />
        <el-table-column prop="createTime" label="操作时间" />
        <el-table-column :label="t('common.actions')" width="120">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'CLOSED'" size="small" type="danger" @click="handleCancel(scope.row)">{{ t('common.cancel') }}</el-button>
            <el-button v-else-if="scope.row.status === 'OPEN'" size="small" type="primary" @click="handleClose(scope.row)">日结</el-button>
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
    
    <el-dialog :title="'日结 - ' + closeForm.date" v-model="closeVisible" width="500px">
      <el-form :model="closeForm" label-width="120px">
        <el-form-item label="日结日期">
          <el-date-picker v-model="closeForm.date" type="date" />
        </el-form-item>
        <el-form-item label="当日销售额">
          <el-input v-model="closeForm.saleAmount" disabled />
        </el-form-item>
        <el-form-item label="当日采购额">
          <el-input v-model="closeForm.purchaseAmount" disabled />
        </el-form-item>
        <el-form-item label="现金收款">
          <el-input v-model="closeForm.cashAmount" />
        </el-form-item>
        <el-form-item label="银行收款">
          <el-input v-model="closeForm.bankAmount" />
        </el-form-item>
        <el-form-item label="备注">
          <el-textarea v-model="closeForm.remark" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSaveClose">确认日结</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const searchForm = reactive({
  date: '',
  status: ''
})

const tableData = ref([
  { id: 1, date: '2024-12-07', saleAmount: '¥ 23,450.00', purchaseAmount: '¥ 12,580.00', profit: '¥ 8,920.00', cashAmount: '¥ 5,000.00', bankAmount: '¥ 18,450.00', status: 'OPEN', creator: '-', createTime: '-' },
  { id: 2, date: '2024-12-06', saleAmount: '¥ 18,600.00', purchaseAmount: '¥ 9,400.00', profit: '¥ 7,200.00', cashAmount: '¥ 3,000.00', bankAmount: '¥ 15,600.00', status: 'CLOSED', creator: '张三', createTime: '2024-12-06 23:30' },
  { id: 3, date: '2024-12-05', saleAmount: '¥ 15,200.00', purchaseAmount: '¥ 6,800.00', profit: '¥ 7,400.00', cashAmount: '¥ 4,200.00', bankAmount: '¥ 11,000.00', status: 'CLOSED', creator: '李四', createTime: '2024-12-05 22:45' }
])

const total = ref(30)
const pageSize = ref(10)
const currentPage = ref(1)
const closeVisible = ref(false)

const closeForm = reactive({
  date: '',
  saleAmount: '',
  purchaseAmount: '',
  cashAmount: '',
  bankAmount: '',
  remark: ''
})

const getStatusType = (status) => {
  const types = {
    'OPEN': 'warning',
    'CLOSED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    'OPEN': '未结',
    'CLOSED': '已结',
    'CANCELLED': '已取消'
  }
  return labels[status] || status
}

const handleSearch = () => {}
const handleReset = () => {
  searchForm.date = ''
  searchForm.status = ''
}

const handleClose = (row) => {
  closeForm.date = row?.date || new Date().toISOString().split('T')[0]
  closeForm.saleAmount = row?.saleAmount || '¥ 0.00'
  closeForm.purchaseAmount = row?.purchaseAmount || '¥ 0.00'
  closeForm.cashAmount = ''
  closeForm.bankAmount = ''
  closeForm.remark = ''
  closeVisible.value = true
}

const handleCancel = () => {}
const handleSaveClose = () => {
  closeVisible.value = false
}

const handlePageChange = (page) => {
  currentPage.value = page
}
</script>

<style scoped>
.daily-close {
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