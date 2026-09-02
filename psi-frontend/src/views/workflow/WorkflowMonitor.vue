<template>
  <div class="workflow-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('workflow.monitor') }}</span>
          <div>
            <el-select v-model="filterStatus" :placeholder="t('workflow.statusFilter')" clearable style="width: 140px; margin-right: 10px;">
              <el-option :value="1" :label="t('workflow.running')" />
              <el-option :value="2" :label="t('workflow.completed')" />
              <el-option :value="3" :label="t('workflow.rejected')" />
            </el-select>
            <el-button @click="loadList" :loading="loading">{{ t('common.search') }}</el-button>
          </div>
        </div>
      </template>
      <el-table :data="filteredData" border v-loading="loading">
        <el-table-column prop="id" :label="t('workflow.instanceId')" width="180" show-overflow-tooltip />
        <el-table-column prop="title" :label="t('workflow.processName')" min-width="150" />
        <el-table-column prop="startUserName" :label="t('workflow.startUser')" width="100" />
        <el-table-column prop="status" :label="t('common.status')" width="100">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('workflow.currentNode')" width="120">
          <template #default="scope">
            {{ scope.row.currentNodeName || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('workflow.startTime')" width="170" />
        <el-table-column :label="t('common.actions')" width="120" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">{{ t('common.view') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { workflowApi } from '../../api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const router = useRouter()
const tableData = ref([])
const loading = ref(false)
const filterStatus = ref(null)

const filteredData = computed(() => {
  if (filterStatus.value === null || filterStatus.value === '') return tableData.value
  return tableData.value.filter(item => item.status === filterStatus.value)
})

const loadList = async () => {
  loading.value = true
  try {
    const response = await workflowApi.getInstances()
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