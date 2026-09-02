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
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="customerName" label="客户名称" />
        <el-table-column prop="totalAmount" label="应收金额" />
        <el-table-column prop="paidAmount" label="已收金额" />
        <el-table-column prop="remainAmount" label="未收金额" />
        <el-table-column prop="billDate" label="单据日期" />
        <el-table-column prop="sourceNo" label="来源单号" />
        <el-table-column prop="sourceType" label="来源类型" />
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
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { financeApi } from '../../api'

const { t } = useI18n()

const searchForm = reactive({
  customerName: ''
})

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const loading = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await financeApi.getReceivableList({
      customerName: searchForm.customerName || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('加载应收款列表失败', e)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadList()
}
const handleReset = () => {
  searchForm.customerName = ''
  currentPage.value = 1
  loadList()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadList()
}

onMounted(() => {
  loadList()
})
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
