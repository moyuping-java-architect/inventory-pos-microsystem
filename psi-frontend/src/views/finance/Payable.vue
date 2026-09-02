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

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="supplierName" label="供应商名称" />
        <el-table-column prop="totalAmount" label="应付金额" />
        <el-table-column prop="paidAmount" label="已付金额" />
        <el-table-column prop="remainAmount" label="未付金额" />
        <el-table-column prop="billDate" label="单据日期" />
        <el-table-column prop="sourceNo" label="来源单号" />
        <el-table-column prop="sourceType" label="来源类型" />
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
          <el-input v-model="payForm.remainAmount" disabled />
        </el-form-item>
        <el-form-item label="付款金额" required>
          <el-input type="number" v-model="payForm.payAmount" placeholder="请输入付款金额" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="payForm.payMethod" placeholder="请选择付款方式">
            <el-option label="现金" value="CASH" />
            <el-option label="银行转账" value="BANK" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="payForm.remark" rows="3" />
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
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { financeApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const searchForm = reactive({
  supplierName: ''
})

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const loading = ref(false)
const payVisible = ref(false)

const payForm = reactive({
  id: '',
  supplierName: '',
  remainAmount: '',
  payAmount: '',
  payMethod: '',
  remark: ''
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await financeApi.getPayableList({
      supplierName: searchForm.supplierName || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('加载应付款列表失败', e)
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
  searchForm.supplierName = ''
  currentPage.value = 1
  loadList()
}

const handleSinglePay = (row) => {
  payForm.id = row.id
  payForm.supplierName = row.supplierName
  payForm.remainAmount = row.remainAmount
  payForm.payAmount = ''
  payForm.payMethod = ''
  payForm.remark = ''
  payVisible.value = true
}

const handleSavePay = async () => {
  if (!payForm.payAmount) {
    ElMessage.warning('请输入付款金额')
    return
  }
  try {
    await financeApi.payPayable(payForm.id, {
      payAmount: payForm.payAmount,
      payMethod: payForm.payMethod,
      remark: payForm.remark
    })
    ElMessage.success('付款成功')
    payVisible.value = false
    loadList()
  } catch (e) {
    console.error('付款失败', e)
  }
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

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
</style>
