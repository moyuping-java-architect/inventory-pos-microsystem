<template>
  <div class="workflow-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('workflow.start') }}</span>
          <el-button @click="loadList" :loading="loading">{{ t('common.search') }}</el-button>
        </div>
      </template>
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="title" :label="t('workflow.processName')" min-width="150" />
        <el-table-column prop="id" :label="t('workflow.instanceId')" width="180" show-overflow-tooltip />
        <el-table-column prop="startUserName" :label="t('workflow.startUser')" width="100" />
        <el-table-column prop="status" :label="t('common.status')" width="100">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('workflow.currentNode')" width="120">
          <template #default="scope">
            {{ scope.row.currentNodeName || (scope.row.status === 1 ? t('workflow.running') : scope.row.status === 2 ? t('workflow.completed') : t('workflow.rejected')) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('workflow.startTime')" width="170" />
        <el-table-column :label="t('common.actions')" width="120" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">{{ t('common.view') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" :description="t('workflow.noStarted')" />
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

const loadList = async () => {
  loading.value = true
  try {
    const userId = localStorage.getItem('userId') || '1'
    const response = await workflowApi.getInstances({ userId })
    if (response.code === 200) {
      tableData.value = response.data || []
    }
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error(t('workflow.loadFailed'))
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  router.push(`/workflow/approval/${row.id}`)
}

const statusType = (status) => {
  const types = { 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const statusText = (status) => {
  const texts = { 1: t('workflow.running'), 2: t('workflow.completed'), 3: t('workflow.rejected') }
  return texts[status] || status
}

onMounted(() => {
  loadList()
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
</style>