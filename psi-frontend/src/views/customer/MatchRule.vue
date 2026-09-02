<template>
  <div class="match-rule-page">
    <el-alert
      :title="$t('matchRule.alert')"
      type="info"
      :closable="false"
      show-icon
      class="page-alert"
    />

    <div class="plan-header">
      <span class="title">{{ $t('matchRule.title') }}</span>
      <el-button type="primary" :icon="Plus" @click="openDialog()">
        {{ $t('matchRule.addPlan') }}
      </el-button>
    </div>

    <div v-loading="loading" class="plan-body">
      <el-empty v-if="!groupedPlans.length" :description="$t('matchRule.noData')" />
      <el-collapse v-else v-model="activeStages">
        <el-collapse-item
          v-for="group in groupedPlans"
          :key="group.stageCode"
          :title="`${group.stageName} (${group.stageCode})`"
          :name="group.stageCode"
        >
          <el-timeline>
            <el-timeline-item
              v-for="item in group.plans"
              :key="item.id"
              :type="item.isEnabled ? 'primary' : 'info'"
              :icon="item.isEnabled ? Check : Close"
            >
              <el-card shadow="never" :body-style="{ padding: '12px 16px' }">
                <div class="plan-row">
                  <div class="plan-info">
                    <div class="plan-title">
                      <span class="day-badge">D{{ item.dayStart }}<span v-if="item.dayStart !== item.dayEnd">~{{ item.dayEnd }}</span></span>
                      <span class="name">{{ item.scriptName }}</span>
                      <el-tag v-if="!item.isEnabled" type="info" size="small">{{ $t('matchRule.disabled') }}</el-tag>
                    </div>
                    <div class="plan-content">{{ item.content }}</div>
                    <div class="plan-tags">
                      <el-tag size="small" effect="plain">{{ categoryLabel(item.category) }}</el-tag>
                      <el-tag size="small" effect="plain">{{ matchModeLabel(item.tagMatchMode) }}</el-tag>
                      <el-tag
                        v-for="tag in item.tagList"
                        :key="tag.tagCode"
                        size="small"
                        effect="plain"
                        :style="{ borderColor: tag.color, color: tag.color }"
                      >
                        {{ tag.tagName }}
                      </el-tag>
                    </div>
                  </div>
                  <div class="plan-actions">
                    <el-button type="primary" link :icon="Edit" @click="openDialog(item)">
                      {{ $t('common.edit') }}
                    </el-button>
                    <el-popconfirm
                      :title="$t('common.confirmDelete')"
                      @confirm="deletePlan(item.id)"
                    >
                      <template #reference>
                        <el-button type="danger" link :icon="Delete">
                          {{ $t('common.delete') }}
                        </el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 新增/编辑触达计划 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? $t('matchRule.editPlan') : $t('matchRule.addPlan')"
      width="640px"
      destroy-on-close
    >
      <el-form :model="form" label-width="100px">
        <el-form-item :label="$t('matchRule.colScriptName')">
          <el-input v-model="form.scriptName" />
        </el-form-item>
        <el-form-item :label="$t('matchRule.colScriptCode')">
          <el-input v-model="form.scriptCode" :placeholder="$t('matchRule.scriptCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('matchRule.colCategory')">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('matchRule.colStage')">
          <el-input v-model="form.stageCode" :placeholder="$t('matchRule.stagePlaceholder')" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('matchRule.colDayStart')">
              <el-input-number v-model="form.dayStart" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('matchRule.colDayEnd')">
              <el-input-number v-model="form.dayEnd" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('matchRule.colTagMatchMode')">
          <el-radio-group v-model="form.tagMatchMode">
            <el-radio label="ANY">{{ $t('matchRule.matchAny') }}</el-radio>
            <el-radio label="ALL">{{ $t('matchRule.matchAll') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('matchRule.colTags')">
          <el-select v-model="form.tags" multiple style="width: 100%">
            <el-option v-for="tag in allTags" :key="tag.tagCode" :label="tag.tagName" :value="tag.tagCode" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('matchRule.colContent')">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item :label="$t('matchRule.colPriority')">
          <el-input-number v-model="form.priority" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('matchRule.colEnabled')">
          <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="savePlan">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Check, Close } from '@element-plus/icons-vue'
import { salesScriptApi } from '@/api/modules/customer'

const plans = ref([])
const allTags = ref([])
const loading = ref(false)
const saving = ref(false)
const activeStages = ref([])
const dialogVisible = ref(false)
const form = ref({
  scriptCode: '',
  scriptName: '',
  category: 'SALES',
  content: '',
  channel: 'WHATSAPP',
  stageCode: '',
  tags: [],
  tagMatchMode: 'ANY',
  dayStart: 1,
  dayEnd: 1,
  priority: 0,
  sendLimit: 1,
  isEnabled: 1
})

const categoryOptions = [
  { label: '破冰', value: 'ICE_BREAK' },
  { label: '方案', value: 'SOLUTION' },
  { label: '成交', value: 'CLOSE' },
  { label: '复购唤醒', value: 'REACTIVATE' },
  { label: '通用', value: 'SALES' }
]

const groupedPlans = computed(() => {
  const map = {}
  for (const p of plans.value) {
    if (!map[p.stageCode]) {
      map[p.stageCode] = {
        stageCode: p.stageCode,
        stageName: p.stageName || p.stageCode,
        plans: []
      }
    }
    map[p.stageCode].plans.push(p)
  }
  return Object.values(map)
})

onMounted(() => {
  loadPlans()
  loadTags()
})

async function loadPlans() {
  loading.value = true
  try {
    const res = await salesScriptApi.listScripts()
    plans.value = res.data || []
    activeStages.value = [...new Set(plans.value.map(p => p.stageCode))]
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  const res = await salesScriptApi.listTags()
  allTags.value = res.data || []
}

function openDialog(item) {
  if (item) {
    form.value = { ...item, tags: item.tags || [] }
  } else {
    form.value = {
      scriptCode: '',
      scriptName: '',
      category: 'SALES',
      content: '',
      channel: 'WHATSAPP',
      stageCode: '',
      tags: [],
      tagMatchMode: 'ANY',
      dayStart: 1,
      dayEnd: 1,
      priority: 0,
      sendLimit: 1,
      isEnabled: 1
    }
  }
  dialogVisible.value = true
}

async function savePlan() {
  saving.value = true
  try {
    const data = { ...form.value }
    if (data.id) {
      await salesScriptApi.updateScript(data)
    } else {
      await salesScriptApi.saveScript(data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadPlans()
  } finally {
    saving.value = false
  }
}

async function deletePlan(id) {
  await salesScriptApi.deleteScript(id)
  ElMessage.success('删除成功')
  await loadPlans()
}

function categoryLabel(category) {
  const item = categoryOptions.find(i => i.value === category)
  return item?.label || category
}

function matchModeLabel(mode) {
  return mode === 'ALL' ? '全部命中' : '任一命中'
}
</script>

<style scoped>
.match-rule-page {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-alert {
  margin-bottom: 16px;
}

.plan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.title {
  font-weight: 600;
  font-size: 16px;
}

.plan-body {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.plan-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.plan-info {
  flex: 1;
}

.plan-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.day-badge {
  background: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.name {
  font-weight: 600;
}

.plan-content {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 10px;
}

.plan-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.plan-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}
</style>
