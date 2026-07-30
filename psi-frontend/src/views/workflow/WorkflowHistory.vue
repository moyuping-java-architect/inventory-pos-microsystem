<template>
  <div class="workflow-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('workflow.history') }}</span>
          <div>
            <el-input
              v-model="searchKey"
              :placeholder="t('workflow.instanceId')"
              clearable
              style="width: 240px; margin-right: 10px"
            />
            <el-button @click="loadHistory" :loading="loading">{{ t('common.search') }}</el-button>
          </div>
        </div>
      </template>
      <el-table :data="filteredData" border v-loading="loading">
        <el-table-column prop="processInstanceId" :label="t('workflow.instanceId')" width="180" show-overflow-tooltip />
        <el-table-column prop="operatorName" :label="t('workflow.startUser')" width="100" />
        <el-table-column :label="t('workflow.approveResult')" width="100">
          <template #default="scope">
            <el-tag :type="operateTypeTag(scope.row.operateType)">
              {{ operateTypeText(scope.row.operateType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operateContent" :label="t('workflow.approveComment')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" :label="t('workflow.startTime')" width="170" />
        <el-table-column :label="t('common.actions')" width="120" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">{{ t('common.view') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && filteredData.length === 0" :description="t('workflow.noHistory')" />
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
const searchKey = ref('')

const filteredData = computed(() => {
  if (!searchKey.value) return tableData.value
  const key = searchKey.value.toLowerCase()
  return tableData.value.filter(item =>
    (item.processInstanceId || '').toLowerCase().includes(key)
  )
})

const loadHistory = async () => {
  loading.value = true
  try {
    const userId = localStorage.getItem('userId') || '1'
    const response = await workflowApi.getHistory({ userId })
    if (response.code === 200) {
      tableData.value = response.data || []
    }
  } catch (error) {
    console.error('加载审批历史失败:', error)
    ElMessage.error('加载审批历史失败')
  } finally {
    loading.value = false
  }
}

const operateTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }
  return tags[type] || 'info'
}

const operateTypeText = (type) => {
  const texts = { 1: t('workflow.operateStart'), 2: t('workflow.operateApprove'), 3: t('workflow.operateReject'), 4: t('workflow.operateCancel') }
  return texts[type] || t('workflow.operateUnknown')
}

const viewDetail = (row) => {
  router.push(`/workflow/approval/${row.processInstanceId}`)
}

onMounted(() => {
  loadHistory()
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