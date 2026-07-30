<template>
  <div class="payable">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handlePay">付款</el-button>
    </div>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="supplierName" label="供应商名称" />
        <el-table-column prop="debtAmount" label="应付账款" />
        <el-table-column prop="paidAmount" label="已付金额" />
        <el-table-column prop="remainingAmount" label="未付金额" />
        <el-table-column prop="lastTransactionDate" label="最后交易日期" />
        <el-table-column :label="t('common.actions')" width="100">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleSinglePay(scope.row)">付款</el-button>
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
    
    <el-dialog :title="'付款 - ' + payForm.supplierName" v-model="payVisible" width="500px">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="供应商名称">
          <el-input v-model="payForm.supplierName" disabled />
        </el-form-item>
        <el-form-item label="应付余额">
          <el-input v-model="payForm.remainingAmount" disabled />
        </el-form-item>
        <el-form-item label="付款金额" required>
          <el-input type="number" v-model="payForm.amount" placeholder="请输入付款金额" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="payForm.payType" placeholder="请选择付款方式">
            <el-option label="现金" value="CASH" />
            <el-option label="银行转账" value="BANK" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-textarea v-model="payForm.remark" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSavePay">确认付款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const searchForm = reactive({
  supplierName: ''
})

const tableData = ref([
  { id: 1, supplierName: '供应商A', debtAmount: '¥ 12,580', paidAmount: '¥ 5,000', remainingAmount: '¥ 7,580', lastTransactionDate: '2024-12-07' },
  { id: 2, supplierName: '供应商B', debtAmount: '¥ 5,600', paidAmount: '¥ 0', remainingAmount: '¥ 5,600', lastTransactionDate: '2024-12-07' },
  { id: 3, supplierName: '供应商C', debtAmount: '¥ 3,800', paidAmount: '¥ 2,000', remainingAmount: '¥ 1,800', lastTransactionDate: '2024-12-06' }
])

const total = ref(20)
const pageSize = ref(10)
const currentPage = ref(1)
const payVisible = ref(false)

const payForm = reactive({
  supplierId: '',
  supplierName: '',
  remainingAmount: '',
  amount: '',
  payType: '',
  remark: ''
})

const handleSearch = () => {}
const handleReset = () => {
  searchForm.supplierName = ''
}

const handlePay = () => {}
const handleSinglePay = (row) => {
  payForm.supplierId = row.id
  payForm.supplierName = row.supplierName
  payForm.remainingAmount = row.remainingAmount
  payForm.amount = ''
  payForm.payType = ''
  payForm.remark = ''
  payVisible.value = true
}

const handleSavePay = () => {
  payVisible.value = false
}

const handlePageChange = (page) => {
  currentPage.value = page
}
</script>

<style scoped>
.payable {
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