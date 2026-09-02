<template>
  <div class="daily-close">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="开始日期">
          <el-date-picker v-model="searchForm.closeDateStart" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="searchForm.closeDateEnd" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-select v-model="searchForm.closeStatus" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option label="未日结" :value="0" />
            <el-option label="已日结" :value="1" />
            <el-option label="已撤销" :value="2" />
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
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="closeDate" label="日期" />
        <el-table-column prop="storeName" label="门店" />
        <el-table-column prop="saleAmount" label="销售额" />
        <el-table-column prop="costAmount" label="成本" />
        <el-table-column prop="profitAmount" label="利润" />
        <el-table-column prop="totalBalance" label="总余额" />
        <el-table-column prop="closeStatusName" :label="t('common.status')">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.closeStatus)">{{ scope.row.closeStatusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="closeBy" label="操作人" />
        <el-table-column prop="closeTime" label="操作时间" />
        <el-table-column :label="t('common.actions')" width="120">
          <template #default="scope">
            <el-button v-if="scope.row.closeStatus === 1" size="small" type="warning" @click="handleReopen(scope.row)">重开</el-button>
            <el-button size="small" @click="handleView(scope.row)">{{ t('common.view') }}</el-button>
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

    <el-dialog title="日结" v-model="closeVisible" width="500px">
      <el-form :model="closeForm" label-width="120px">
        <el-form-item label="门店编码" required>
          <el-input v-model="closeForm.storeCode" placeholder="请输入门店编码" />
        </el-form-item>
        <el-form-item label="日结日期" required>
          <el-date-picker v-model="closeForm.closeDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSaveClose">确认日结</el-button>
      </template>
    </el-dialog>

    <el-dialog title="日结详情" v-model="viewVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日期">{{ viewData.closeDate }}</el-descriptions-item>
        <el-descriptions-item label="门店">{{ viewData.storeName }}</el-descriptions-item>
        <el-descriptions-item label="销售额">{{ viewData.saleAmount }}</el-descriptions-item>
        <el-descriptions-item label="成本">{{ viewData.costAmount }}</el-descriptions-item>
        <el-descriptions-item label="利润">{{ viewData.profitAmount }}</el-descriptions-item>
        <el-descriptions-item label="总余额">{{ viewData.totalBalance }}</el-descriptions-item>
        <el-descriptions-item label="现金余额">{{ viewData.cashBalance }}</el-descriptions-item>
        <el-descriptions-item label="微信余额">{{ viewData.wechatBalance }}</el-descriptions-item>
        <el-descriptions-item label="支付宝余额">{{ viewData.alipayBalance }}</el-descriptions-item>
        <el-descriptions-item label="银行余额">{{ viewData.bankBalance }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ viewData.closeStatusName }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ viewData.closeBy }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ viewData.closeTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
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
  closeDateStart: '',
  closeDateEnd: '',
  closeStatus: undefined
})

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const loading = ref(false)
const closeVisible = ref(false)
const viewVisible = ref(false)

const closeForm = reactive({
  storeCode: localStorage.getItem('shopId') || '1',
  closeDate: new Date().toISOString().split('T')[0]
})

const viewData = reactive({})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await financeApi.getDailyCloseList({
      closeDateStart: searchForm.closeDateStart || undefined,
      closeDateEnd: searchForm.closeDateEnd || undefined,
      closeStatus: searchForm.closeStatus,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('加载日结列表失败', e)
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
  searchForm.closeDateStart = ''
  searchForm.closeDateEnd = ''
  searchForm.closeStatus = undefined
  currentPage.value = 1
  loadList()
}

const handleClose = () => {
  closeForm.storeCode = localStorage.getItem('shopId') || '1'
  closeForm.closeDate = new Date().toISOString().split('T')[0]
  closeVisible.value = true
}

const handleSaveClose = async () => {
  if (!closeForm.storeCode || !closeForm.closeDate) {
    ElMessage.warning('请填写门店编码和日结日期')
    return
  }
  try {
    await financeApi.closeDaily({
      storeCode: closeForm.storeCode,
      closeDate: closeForm.closeDate,
      closeBy: localStorage.getItem('userName') || 'admin'
    })
    ElMessage.success('日结成功')
    closeVisible.value = false
    loadList()
  } catch (e) {
    console.error('日结失败', e)
  }
}

const handleReopen = async (row) => {
  try {
    await financeApi.reopenDaily({
      storeCode: row.storeCode,
      closeDate: row.closeDate
    })
    ElMessage.success('重开成功')
    loadList()
  } catch (e) {
    console.error('重开失败', e)
  }
}

const handleView = (row) => {
  Object.assign(viewData, row)
  viewVisible.value = true
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
