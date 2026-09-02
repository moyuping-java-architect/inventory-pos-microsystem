<template>
  <div class="workflow-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('workflow.todo') }}</span>
          <el-button @click="loadTodoList" :loading="loading">{{ t('common.search') }}</el-button>
        </div>
      </template>
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column :label="t('document.docType')" width="130">
          <template #default="scope">
            <el-tag :type="getDocTypeTag(scope.row.docType)" effect="dark" size="default">
              {{ getDocTypeLabel(scope.row.docType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="docNo" :label="t('document.docNo')" width="160" />
        <el-table-column prop="docName" label="单据名称" min-width="150" />
        <el-table-column prop="taskName" :label="t('workflow.processName')" min-width="150" />
        <el-table-column prop="processInstanceId" :label="t('workflow.instanceId')" width="180" show-overflow-tooltip />
        <el-table-column prop="nodeName" :label="t('workflow.currentNode')" width="120" />
        <el-table-column prop="handlerUserName" :label="t('workflow.handlerUser')" width="100" />
        <el-table-column prop="creatorName" label="制单人" width="100" />
        <el-table-column prop="createTime" :label="t('workflow.startTime')" width="170" />
        <el-table-column :label="t('common.actions')" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="goApproval(scope.row)">{{ t('common.view') }}</el-button>
            <el-button size="small" type="success" @click="goApproval(scope.row)">{{ t('workflow.approve') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" :description="t('workflow.noTodo')" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { workflowApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const router = useRouter()
const tableData = ref([])
const loading = ref(false)

const DOC_TYPE_LABEL_MAP = {
  PURCHASE_ORDER: 'document.purchaseOrder',
  PURCHASE_IN: 'document.purchaseIn',
  PURCHASE_RETURN: 'document.purchaseReturn',
  SALE_ORDER: 'document.saleOrder',
  SALE_OUT: 'document.saleOut',
  SALE_RETURN: 'document.saleReturn',
  STOCK_LOSS: 'document.stockLoss',
  STOCK_OVERFLOW: 'document.stockOverflow',
  STOCK_CHECK: 'document.stockCheck',
  STOCK_TRANSFER: 'document.stockTransfer',
  INVENTORY_INIT: 'document.inventoryInit',
  ADJUST_PRICE: 'document.adjustPrice'
}

const DOC_TYPE_TAG_MAP = {
  PURCHASE_ORDER: 'warning',
  PURCHASE_IN: 'warning',
  PURCHASE_RETURN: 'warning',
  SALE_ORDER: 'success',
  SALE_OUT: 'success',
  SALE_RETURN: 'success',
  STOCK_LOSS: 'danger',
  STOCK_OVERFLOW: 'info',
  STOCK_CHECK: '',
  STOCK_TRANSFER: '',
  INVENTORY_INIT: '',
  ADJUST_PRICE: 'danger'
}

const getDocTypeLabel = (docType) => {
  const key = DOC_TYPE_LABEL_MAP[docType]
  return key ? t(key) : (docType || '-')
}

const getDocTypeTag = (docType) => {
  return DOC_TYPE_TAG_MAP[docType] || 'info'
}

const loadTodoList = async () => {
  loading.value = true
  try {
    const userId = localStorage.getItem('userId') || '1'
    const response = await workflowApi.getTodoList(userId)
    if (response.code === 200) {
      tableData.value = response.data || []
    }
  } catch (error) {
    console.error('加载待办列表失败:', error)
    ElMessage.error('加载待办列表失败')
  } finally {
    loading.value = false
  }
}

const goApproval = (row) => {
  router.push(`/workflow/approval/${row.processInstanceId}`)
}

onMounted(() => {
  loadTodoList()
})
</script>

<style scoped>
.workflow-page {
  padding: 20px;
}
.page-card {
  min-height: calc(100vh - 160px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.variable-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.variable-key {
  font-weight: bold;
  min-width: 60px;
}
</style>