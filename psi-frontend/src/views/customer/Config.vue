<template>
  <div class="config-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('journeyConfig.title') }}</span>
          <div>
            <el-button type="warning" plain size="small" @click="handleReset" :loading="resetting">
              {{ $t('journeyConfig.reset') }}
            </el-button>
            <el-button type="primary" size="small" @click="handleSave" :loading="saving">
              {{ $t('journeyConfig.save') }}
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        :title="$t('journeyConfig.alert')"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane
          v-for="group in configGroups"
          :key="group.group"
          :label="getGroupLabel(group.group)"
          :name="group.group"
        >
          <!-- 客户旅程阶段：模板列表 + 阶段编辑器 -->
          <template v-if="group.group === 'JOURNEY_STAGES'">
            <div class="stage-list-wrap">
              <div class="list-header">
                <span class="list-title">{{ $t('journeyConfig.journeyTemplateList') }}</span>
                <el-button type="primary" size="small" :icon="Plus" @click="openCreateDialog">
                  {{ $t('journeyConfig.newTemplate') }}
                </el-button>
              </div>

              <el-table
                v-loading="loadingTemplates"
                :data="journeyTemplates"
                border
                stripe
                size="small"
                class="template-table"
                empty-text="尚无客户旅程模板"
              >
                <el-table-column type="index" label="#" width="48" align="center" />
                <el-table-column prop="journeyName" :label="$t('journeyConfig.colName')" min-width="140" />
                <el-table-column prop="journeyCode" :label="$t('journeyConfig.colCode')" min-width="160">
                  <template #default="{ row }">
                    <el-tag size="small">{{ row.journeyCode }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('journeyConfig.colSubject')" width="120" align="center">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.subjectType === 'MEMBER' ? 'warning' : 'success'" effect="plain">
                      {{ row.subjectType }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('journeyConfig.colStages')" width="80" align="center">
                  <template #default="{ row }">
                    <el-badge :value="row.stageCount" class="stage-badge" type="primary" />
                  </template>
                </el-table-column>
                <el-table-column :label="$t('journeyConfig.colEnabled')" width="80" align="center">
                  <template #default="{ row }">
                    <el-switch
                      :model-value="row.enabled === 1"
                      @change="(val) => handleToggleEnabled(row, val)"
                    />
                  </template>
                </el-table-column>
                <el-table-column :label="$t('journeyConfig.colOps')" width="180" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" type="primary" link @click="editTemplateStages(row)">
                      {{ $t('journeyConfig.editStages') }}
                    </el-button>
                    <el-button size="small" type="primary" link @click="openEditDialog(row)">
                      {{ $t('journeyConfig.editMeta') }}
                    </el-button>
                    <el-button size="small" type="danger" link @click="deleteTemplate(row)">
                      {{ $t('journeyConfig.delete') }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 内联阶段编辑器（编辑某模板的阶段配置） -->
              <div v-if="editingCode" class="stage-editor">
                <div class="editor-header">
                  <span class="editor-title">
                    {{ $t('journeyConfig.editingStages') }}：{{ editingName }}
                  </span>
                  <div>
                    <el-button size="small" @click="closeStageEditor">
                      {{ $t('journeyConfig.back') }}
                    </el-button>
                    <el-button size="small" type="primary" :loading="savingStages" @click="saveStageConfig">
                      {{ $t('journeyConfig.saveStages') }}
                    </el-button>
                  </div>
                </div>
                <el-alert
                  :title="$t('journeyConfig.stageJsonHint')"
                  type="success"
                  :closable="false"
                  show-icon
                  style="margin-bottom: 12px"
                />

                <el-button
                  size="small"
                  type="primary"
                  plain
                  :icon="Plus"
                  style="margin-bottom: 12px"
                  @click="addStageRow"
                >
                  {{ $t('journeyConfig.addStage') }}
                </el-button>

                <el-table
                  v-loading="loadingStages"
                  :data="stageRows"
                  border
                  size="small"
                  class="stage-table"
                  empty-text=""
                  row-key="_rowKey"
                >
                  <el-table-column :label="$t('journeyConfig.colCode')" width="170">
                    <template #default="{ row }">
                      <el-input
                        v-model="row.stageCode"
                        :disabled="!!row.id"
                        placeholder="STAGE_NAME"
                        size="small"
                      />
                      <div class="field-hint">
                        {{ row.id ? $t('journeyConfig.colCode') : $t('journeyConfig.stageCodeRequired') }}
                      </div>
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.stageColName')" width="160">
                    <template #default="{ row }">
                      <el-input v-model="row.stageName" size="small" />
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.stageColColor')" width="140">
                    <template #default="{ row }">
                      <el-color-picker v-model="row.color" size="small" />
                      <div class="field-hint">{{ row.color }}</div>
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.stageColIcon')" width="160">
                    <template #default="{ row }">
                      <el-select v-model="row.icon" size="small" clearable filterable>
                        <el-option
                          v-for="iconName in iconOptions"
                          :key="iconName"
                          :value="iconName"
                          :label="iconName"
                        />
                      </el-select>
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.stageColMatchTp')" min-width="200">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.matchTouchpointList"
                        multiple
                        filterable
                        allow-create
                        default-first-option
                        :placeholder="$t('journeyConfig.matchAnyTp')"
                        size="small"
                        style="width: 100%"
                        @change="(vals) => syncCsv('matchTouchpoint', row, vals)"
                      >
                        <el-option
                          v-for="tp in touchpointDict"
                          :key="tp.dictCode"
                          :value="tp.dictCode"
                          :label="dictLabel(tp)"
                        />
                      </el-select>
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.stageColMatchIntent')" min-width="180">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.matchIntentList"
                        multiple
                        filterable
                        allow-create
                        default-first-option
                        :placeholder="$t('journeyConfig.matchAny')"
                        size="small"
                        style="width: 100%"
                        @change="(vals) => syncCsv('matchIntent', row, vals)"
                      >
                        <el-option
                          v-for="it in intentDict"
                          :key="it.dictCode"
                          :value="it.dictCode"
                          :label="dictLabel(it)"
                        />
                      </el-select>
                    </template>
                  </el-table-column>

                  <!-- 触发事件：直接在该阶段上勾选哪些业务事件推进到本阶段（替代原独立关联页） -->
                  <el-table-column label="触发事件" min-width="260">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.eventCodes"
                        multiple
                        filterable
                        :placeholder="'选择触发本阶段的业务事件'"
                        size="small"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="ev in eventOptions"
                          :key="ev.value"
                          :value="ev.value"
                          :label="ev.label"
                        />
                      </el-select>
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.stageColTip')" min-width="200">
                    <template #default="{ row }">
                      <el-input
                        v-model="row.tip"
                        type="textarea"
                        :rows="1"
                        autosize
                        size="small"
                        :placeholder="$t('journeyConfig.stageColTip')"
                      />
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.stageColOrder')" width="90">
                    <template #default="{ row }">
                      <el-input-number v-model="row.sortOrder" :min="0" :step="10" size="small" controls-position="right" />
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.stageColRollback')" width="80" align="center">
                    <template #default="{ row }">
                      <el-switch v-model="row.allowRollback" :active-value="1" :inactive-value="0" />
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('journeyConfig.colOps')" width="60" align="center" fixed="right">
                    <template #default="{ $index }">
                      <el-button type="danger" link size="small" @click="removeStageRow($index)">
                        {{ $t('journeyConfig.delete') }}
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>

                <div v-if="!loadingStages && stageRows.length === 0" class="empty-hint">
                  {{ $t('journeyConfig.noStages') }}
                </div>
              </div>
            </div>
          </template>

          <!-- 其他 flat 配置组（保持原状） -->
          <el-form v-else label-width="240px" class="config-form">
            <el-form-item
              v-for="item in group.items"
              :key="item.configKey"
              :label="getItemLabel(group.group, item)"
            >
              <el-input-number
                v-if="item.valueType === 'NUMBER' || item.valueType === 'DOUBLE'"
                v-model="item.configValue"
                :min="0"
                :precision="2"
                :step="getConfigStep(item.configKey)"
                controls-position="right"
                style="width: 200px"
              />
              <el-input-number
                v-else-if="item.valueType === 'INT'"
                v-model.number="item.configValue"
                :min="0"
                :step="1"
                controls-position="right"
                style="width: 200px"
              />
              <el-input
                v-else-if="item.valueType === 'STRING'"
                v-model="item.configValue"
                style="width: 400px"
              />
              <el-input
                v-else
                v-model="item.configValue"
                type="textarea"
                :rows="3"
                style="width: 500px"
              />
              <div class="field-hint">{{ getItemDesc(group.group, item) }}</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增 / 编辑 模板弹窗 -->
    <el-dialog
      v-model="templateDialogVisible"
      :title="templateDialogMode === 'create' ? $t('journeyConfig.newTemplate') : $t('journeyConfig.editMeta')"
      width="560px"
      @closed="onTemplateDialogClosed"
    >
      <el-form :model="templateForm" :rules="templateRules" ref="templateFormRef" label-width="100px">
        <el-form-item :label="$t('journeyConfig.colCode')" prop="journeyCode">
          <el-input
            v-model="templateForm.journeyCode"
            :disabled="templateDialogMode === 'edit'"
            placeholder="例如 SALES_FUNNEL"
          />
        </el-form-item>
        <el-form-item :label="$t('journeyConfig.colName')" prop="journeyName">
          <el-input v-model="templateForm.journeyName" placeholder="例如 销售漏斗" />
        </el-form-item>
        <el-form-item :label="$t('journeyConfig.colSubject')" prop="subjectType">
          <el-select v-model="templateForm.subjectType" style="width: 100%">
            <el-option label="CUSTOMER (普通客户)" value="CUSTOMER" />
            <el-option label="MEMBER (会员)" value="MEMBER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('journeyConfig.colDescription')">
          <el-input v-model="templateForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item :label="$t('journeyConfig.colIcon')">
          <el-input v-model="templateForm.icon" placeholder="Element Plus icon name" />
        </el-form-item>
        <el-form-item :label="$t('journeyConfig.colSort')">
          <el-input-number v-model="templateForm.sortOrder" :min="0" :step="1" />
        </el-form-item>
        <el-form-item :label="$t('journeyConfig.colEnabled')">
          <el-switch v-model="enabledSwitch" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">
          {{ $t('journeyConfig.cancel') }}
        </el-button>
        <el-button type="primary" :loading="savingTemplate" @click="submitTemplateDialog">
          {{ $t('journeyConfig.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { customerJourneyApi } from '../../api/index.js'

const { t, locale } = useI18n()

const configGroups = ref([])
const activeTab = ref('CHURN_MODEL')
const saving = ref(false)
const resetting = ref(false)

// 模板列表与 CRUD 状态
const journeyTemplates = ref([])
const loadingTemplates = ref(false)
const templateDialogVisible = ref(false)
const templateDialogMode = ref('create')  // 'create' | 'edit'
const templateForm = ref({
  id: null,
  journeyCode: '',
  journeyName: '',
  subjectType: 'CUSTOMER',
  description: '',
  icon: '',
  sortOrder: 0,
  enabled: 1
})
const enabledSwitch = ref(true)
const templateFormRef = ref(null)
const savingTemplate = ref(false)

// 阶段编辑器状态（行内展开，v2 可视化）
const editingCode = ref('')
const editingName = ref('')
const stageRows = ref([])
const loadingStages = ref(false)
const savingStages = ref(false)

// 看板可选图标（Element Plus 常用）
const iconOptions = [
  'User', 'Phone', 'ChatLineRound', 'ChatDotRound', 'Star', 'StarFilled',
  'Trophy', 'Calendar', 'Bell', 'Promotion', 'Heart', 'Money',
  'ShoppingCart', 'Aim', 'Coordinate', 'Connection', 'Document',
  'Edit', 'View', 'Search', 'Notification', 'DataAnalysis', 'Histogram',
  'Box', 'Coin', 'Discount', 'Goods', 'Ticket', 'TrendCharts'
]

// 触点类型 / 意图 一律来自服务端字典（journey_dict）。
// 这里曾经是两组写死的数组，结果和字典各自漂移：页面上能选到 WHATSAPP_INQUIRY，
// 库里却只有 WHATSAPP_MSG，老板照着选完阶段就是不动，页面还不报错。
// 字典是唯一真相源，前端只负责显示。
const touchpointDict = ref([])
const intentDict = ref([])

/** 按当前语言取字典显示名，没有英文名就退回中文名，再退回编码 */
const dictLabel = (item) => {
  const en = locale.value === 'en-US'
  const name = en ? (item.dictNameEn || item.dictName) : item.dictName
  return name ? `${name} (${item.dictCode})` : item.dictCode
}

// 业务事件下拉选项（供阶段编辑器「触发事件」列使用，数据源取自 engineOptions.events）
const events = ref([])
const eventOptions = computed(() => {
  const en = locale.value === 'en-US'
  return events.value.map(ev => ({
    value: ev.eventCode,
    label: (en && ev.eventNameEn ? ev.eventNameEn : ev.eventName) + ' (' + ev.eventCode + ')'
  }))
})

// 同步多选数组与 CSV 字符串（接口要字符串）
const syncCsv = (field, row, valArr) => {
  const csv = Array.isArray(valArr) ? valArr.join(',') : ''
  if (field === 'matchTouchpoint') row.matchTouchpoint = csv
  else if (field === 'matchIntent') row.matchIntent = csv
}

const toCsvArr = (csv) => {
  if (!csv) return []
  return csv.split(',').map(s => s.trim()).filter(Boolean)
}

const newStageRowKey = () => `row-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`

const defaultStageRow = (sortOrder) => ({
  id: null,
  _rowKey: newStageRowKey(),
  stageCode: '',
  stageName: '',
  color: '#909399',
  icon: 'User',
  matchTouchpoint: '',
  matchTouchpointList: [],
  matchIntent: '',
  matchIntentList: [],
  eventCodes: [],
  tip: '',
  sortOrder: sortOrder ?? 10,
  allowRollback: 0
})

const templateRules = {
  journeyCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  journeyName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  subjectType: [{ required: true, message: '请选择主体类型', trigger: 'change' }]
}

const getGroupLabel = (groupCode) => {
  const key = `journeyConfig.groups.${groupCode}`
  const translated = t(key, {}, { missingWarn: false, fallbackWarn: false })
  return translated === key ? groupCode : translated
}
const getItemLabel = (groupCode, item) => {
  const key = `journeyConfig.items.${groupCode}.${item.configKey}.label`
  const translated = t(key, {}, { missingWarn: false, fallbackWarn: false })
  return translated === key ? item.displayName : translated
}
const getItemDesc = (groupCode, item) => {
  const key = `journeyConfig.items.${groupCode}.${item.configKey}.desc`
  const translated = t(key, {}, { missingWarn: false, fallbackWarn: false })
  return translated === key ? item.description : translated
}
const getConfigStep = (key) => {
  const upper = (key || '').toUpperCase()
  if (upper.includes('FACTOR') || upper.includes('MULTIPLIER')) return 0.1
  if (upper.includes('RATE') || upper.includes('THRESHOLD')) return 0.5
  return 1
}

const loadConfig = async () => {
  try {
    const res = await customerJourneyApi.getConfig()
    configGroups.value = res.data || []
    if (configGroups.value.length > 0 && !configGroups.value.some(g => g.group === activeTab.value)) {
      activeTab.value = configGroups.value[0].group
    }
  } catch (e) {
    console.error('Failed to load config:', e)
    ElMessage.error(t('journeyConfig.loadFailed'))
  }
}

const loadJourneyTemplates = async () => {
  loadingTemplates.value = true
  try {
    const res = await customerJourneyApi.listJourneyTemplates()
    journeyTemplates.value = res.data || []
  } catch (e) {
    console.error('Failed to load journey templates:', e)
    ElMessage.error('加载模板列表失败')
  } finally {
    loadingTemplates.value = false
  }
}

const loadJourneyStages = async () => {
  if (!editingCode.value) {
    stageRows.value = []
    return
  }
  loadingStages.value = true
  try {
    const res = await customerJourneyApi.listJourneyStagesEdit(editingCode.value)
    const list = res.data || []
    stageRows.value = list.map((s, i) => ({
      ...s,
      _rowKey: newStageRowKey(),
      matchTouchpointList: toCsvArr(s.matchTouchpoint),
      matchIntentList: toCsvArr(s.matchIntent),
      matchTouchpoint: s.matchTouchpoint || '',
      matchIntent: s.matchIntent || '',
      eventCodes: s.eventCodes || []
    }))
  } catch (e) {
    console.error('Failed to load journey stages:', e)
    stageRows.value = []
    ElMessage.error(t('journeyConfig.loadStagesFailed'))
  } finally {
    loadingStages.value = false
  }
}

const openCreateDialog = () => {
  templateDialogMode.value = 'create'
  templateForm.value = {
    id: null,
    journeyCode: '',
    journeyName: '',
    subjectType: 'CUSTOMER',
    description: '',
    icon: '',
    sortOrder: 0,
    enabled: 1
  }
  enabledSwitch.value = true
  templateDialogVisible.value = true
}

const openEditDialog = (row) => {
  templateDialogMode.value = 'edit'
  templateForm.value = { ...row, enabled: row.enabled }
  enabledSwitch.value = row.enabled === 1
  templateDialogVisible.value = true
}

const onTemplateDialogClosed = () => {
  templateFormRef.value?.resetFields()
}

const submitTemplateDialog = async () => {
  try {
    await templateFormRef.value.validate()
  } catch {
    return
  }
  savingTemplate.value = true
  templateForm.value.enabled = enabledSwitch.value ? 1 : 0
  try {
    if (templateDialogMode.value === 'create') {
      await customerJourneyApi.createJourneyTemplate(templateForm.value)
      ElMessage.success('模板已创建')
    } else {
      await customerJourneyApi.updateJourneyTemplate(templateForm.value.id, templateForm.value)
      ElMessage.success('模板已更新')
    }
    templateDialogVisible.value = false
    await loadJourneyTemplates()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '操作失败'
    ElMessage.error(msg)
  } finally {
    savingTemplate.value = false
  }
}

const handleToggleEnabled = async (row, val) => {
  try {
    await customerJourneyApi.updateJourneyTemplate(row.id, { enabled: val ? 1 : 0 })
    row.enabled = val ? 1 : 0
    ElMessage.success(val ? '已启用' : '已停用')
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '更新失败'
    ElMessage.error(msg)
    await loadJourneyTemplates()
  }
}

const deleteTemplate = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除「${row.journeyName}」(编码 ${row.journeyCode})？\n如有阶段，请先删除所有阶段。`,
      '删除模板',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await customerJourneyApi.deleteJourneyTemplate(row.id)
    ElMessage.success('已删除')
    if (editingCode.value === row.journeyCode) {
      closeStageEditor()
    }
    await loadJourneyTemplates()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '删除失败'
    ElMessage.error(msg)
  }
}

const editTemplateStages = async (row) => {
  editingCode.value = row.journeyCode
  editingName.value = row.journeyName
  await loadJourneyStages()
  await nextTick()
  document.querySelector('.stage-editor')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const closeStageEditor = () => {
  editingCode.value = ''
  editingName.value = ''
  stageRows.value = []
}

const addStageRow = () => {
  const next = defaultStageRow((stageRows.value.length + 1) * 10)
  stageRows.value = [...stageRows.value, next]
}

const removeStageRow = (idx) => {
  if (idx < 0 || idx >= stageRows.value.length) return
  stageRows.value = stageRows.value.filter((_, i) => i !== idx)
}

const validateStages = () => {
  const codeRe = /^[A-Za-z0-9_]+$/
  const codes = new Set()
  for (let i = 0; i < stageRows.value.length; i++) {
    const r = stageRows.value[i]
    const code = (r.stageCode || '').trim()
    if (!code) {
      ElMessage.warning(`第 ${i + 1} 行阶段编码不能为空`)
      return false
    }
    if (!codeRe.test(code)) {
      ElMessage.warning(`第 ${i + 1} 行编码「${code}」只能含字母/数字/下划线`)
      return false
    }
    if (codes.has(code)) {
      ElMessage.warning(`阶段编码重复: ${code}`)
      return false
    }
    codes.add(code)
    r.stageCode = code
    if (!r.stageName) r.stageName = code
  }
  return true
}

const saveStageConfig = async () => {
  if (!editingCode.value) return
  if (!validateStages()) return

  savingStages.value = true
  try {
    const payload = stageRows.value.map(r => ({
      id: r.id || null,
      tenantId: r.tenantId || null,
      stageCode: r.stageCode,
      stageName: r.stageName || r.stageCode,
      color: r.color || '#909399',
      icon: r.icon || 'User',
      matchTouchpoint: r.matchTouchpoint || '',
      matchIntent: r.matchIntent || '',
      tip: r.tip || '',
      sortOrder: r.sortOrder || 0,
      allowRollback: r.allowRollback || 0,
      eventCodes: r.eventCodes || []
    }))
    await customerJourneyApi.saveJourneyStagesEdit(editingCode.value, payload)
    ElMessage.success(t('journeyConfig.stageSaved'))
    await loadJourneyStages()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || t('journeyConfig.stageSaveFailed')
    ElMessage.error(msg)
  } finally {
    savingStages.value = false
  }
}

/**
 * 拉一次触点/意图字典就够了，切来切去不用反复请求。
 * 阶段编辑器和事件关联页都要用，所以单独拎出来。
 */
const loadDicts = async () => {
  if (touchpointDict.value.length > 0) return
  try {
    const res = await customerJourneyApi.engineOptions()
    const opt = res.data || {}
    touchpointDict.value = opt.touchpointTypes || []
    intentDict.value = opt.intents || []
    events.value = opt.events || []
  } catch (e) {
    console.error('Failed to load journey dicts:', e)
  }
}

watch(activeTab, async (newTab) => {
  if (newTab === 'JOURNEY_STAGES') {
    await Promise.all([loadJourneyTemplates(), loadDicts()])
  }
})

const handleSave = async () => {
  saving.value = true
  try {
    if (activeTab.value === 'JOURNEY_STAGES') {
      ElMessage.info('请在阶段编辑器中点保存，或在模板列表行进行编辑。')
      return
    }
    const allItems = []
    configGroups.value.forEach(g => {
      if (g.group === 'JOURNEY_STAGES') return
      g.items.forEach(item => {
        allItems.push({
          id: item.id,
          configKey: item.configKey,
          configValue: String(item.configValue),
          configGroup: item.configGroup,
          valueType: item.valueType
        })
      })
    })
    await customerJourneyApi.saveConfig(allItems)
    ElMessage.success(t('journeyConfig.saveSuccess'))
  } catch (e) {
    ElMessage.error(t('journeyConfig.saveFailed'))
  } finally {
    saving.value = false
  }
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm(
      t('journeyConfig.resetConfirm'),
      t('journeyConfig.resetTitle'),
      { type: 'warning' }
    )
  } catch {
    return
  }
  resetting.value = true
  try {
    await customerJourneyApi.resetConfig()
    ElMessage.success(t('journeyConfig.resetSuccess'))
    await loadConfig()
    if (activeTab.value === 'JOURNEY_STAGES') {
      await loadJourneyTemplates()
    }
  } catch (e) {
    ElMessage.error(t('journeyConfig.resetFailed'))
  } finally {
    resetting.value = false
  }
}

onMounted(async () => {
  await loadConfig()
  // 提前加载模板与字典，切换 tab 时无需等待
  loadJourneyTemplates()
  loadDicts()
})
</script>

<style scoped>
.config-page { padding: 20px; }
.page-card { margin-bottom: 20px; }
.card-header {
  display: flex; justify-content: space-between; align-items: center;
}
.config-form { max-width: 800px; }
.field-hint {
  font-size: 12px; color: #999; margin-top: 4px; line-height: 1.4;
}
.empty-hint {
  font-size: 13px; color: #909399; padding: 12px 0;
}

.stage-list-wrap { margin: 0 4px; }
.list-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
}
.list-title {
  font-size: 15px; font-weight: 500; color: var(--el-text-color-primary);
}
.template-table { margin-bottom: 20px; }
.stage-badge :deep(.el-badge__content) { font-size: 11px; }

.stage-editor {
  margin-top: 16px;
  padding: 18px;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow-x: auto;
}
.stage-editor :deep(.stage-table) {
  min-width: 1080px;
}
.stage-editor :deep(.stage-table .el-table__cell) {
  padding: 8px 6px;
  vertical-align: top;
}
.editor-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 1px dashed #dcdfe6;
}
.editor-title {
  font-size: 14px; font-weight: 600; color: var(--el-color-primary);
}

:deep(.el-form-item__label) {
  font-size: 13px;
}
:deep(.el-tabs--card > .el-tabs__header .el-tabs__item) {
  color: var(--el-text-color-primary);
  background-color: var(--el-bg-color);
  white-space: nowrap;
}
:deep(.el-tabs--card > .el-tabs__header .el-tabs__item.is-active) {
  color: var(--el-color-primary);
  background-color: var(--el-bg-color);
}
</style>
