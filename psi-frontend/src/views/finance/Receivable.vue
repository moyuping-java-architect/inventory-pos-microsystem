<template>
  <div class="receivable">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="customerName" label="客户名称" />
        <el-table-column prop="debtAmount" label="应收账款" />
        <el-table-column prop="paidAmount" label="已收金额" />
        <el-table-column prop="remainingAmount" label="未收金额" />
        <el-table-column prop="lastTransactionDate" label="最后交易日期" />
      </el-table>
      
      <el-pagination
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const searchForm = reactive({
  customerName: ''
})

const tableData = ref([
  { id: 1, customerName: '客户A', debtAmount: '¥ 15,000', paidAmount: '¥ 3,000', remainingAmount: '¥ 12,000', lastTransactionDate: '2024-12-07' },
  { id: 2, customerName: '客户C', debtAmount: '¥ 8,500', paidAmount: '¥ 0', remainingAmount: '¥ 8,500', lastTransactionDate: '2024-12-04' }
])

const total = ref(15)
const pageSize = ref(10)
const currentPage = ref(1)

const handleSearch = () => {}
const handleReset = () => {
  searchForm.customerName = ''
}

const handlePageChange = (page) => {
  currentPage.value = page
}
</script>

<style scoped>
.receivable {
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