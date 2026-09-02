<template>
  <div class="script-library-page">
    <!-- 左侧：客户列表 -->
    <div class="customer-panel">
      <div class="panel-header">
        <span class="panel-title">{{ $t('scriptLibrary.customerList') }}</span>
        <el-input
          v-model="customerSearch"
          :placeholder="$t('scriptLibrary.searchCustomer')"
          clearable
          size="small"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div v-loading="loadingCustomers" class="customer-list">
        <div
          v-for="c in filteredCustomers"
          :key="c.customerId"
          :class="['customer-card', { active: selectedCustomer?.customerId === c.customerId }]"
          @click="selectCustomer(c)"
        >
          <div class="customer-avatar" :style="{ background: avatarColor(c.avatar) }">
            {{ c.avatar }}
          </div>
          <div class="customer-info">
            <div class="customer-name">{{ c.customerName }}</div>
            <div class="customer-channel">{{ channelLabel(c.channel) }} · {{ c.contact || '-' }}</div>
            <div class="customer-tags">
              <el-tag
                v-for="tag in c.tags?.slice(0, 3)"
                :key="tag.tagCode"
                size="small"
                effect="plain"
                :style="{ borderColor: tag.color, color: tag.color }"
              >
                {{ tag.tagName }}
              </el-tag>
            </div>
          </div>
          <div class="customer-meta">
            <el-tag v-if="c.hasPending" type="success" effect="dark" size="small">
              {{ $t('scriptLibrary.pendingCount', { count: pendingCount(c) }) }}
            </el-tag>
            <el-tag v-else type="info" effect="plain" size="small">
              {{ $t('scriptLibrary.allSent') }}
            </el-tag>
            <div class="last-interact">{{ $t('scriptLibrary.daysInStage') }} {{ c.daysInStage ?? '-' }} {{ $t('scriptLibrary.days') }}</div>
          </div>
        </div>
        <el-empty v-if="!filteredCustomers.length" :description="$t('scriptLibrary.noCustomer')" />
      </div>
    </div>

    <!-- 中间：触达详情 -->
    <div class="match-panel">
      <template v-if="selectedCustomer">
        <div class="customer-header">
          <div class="customer-avatar large" :style="{ background: avatarColor(selectedCustomer.avatar) }">
            {{ selectedCustomer.avatar }}
          </div>
          <div class="customer-header-info">
            <div class="name-row">
              <span class="name">{{ selectedCustomer.customerName }}</span>
              <el-tag type="warning" effect="light" size="small">{{ selectedCustomer.stageName }}</el-tag>
              <el-tag type="primary" effect="light" size="small">
                {{ $t('scriptLibrary.dayInStage', { day: selectedCustomer.daysInStage ?? '-' }) }}
              </el-tag>
            </div>
            <div class="contact-row">
              {{ channelLabel(selectedCustomer.channel) }} · {{ selectedCustomer.contact || '-' }}
            </div>
            <div class="tag-row">
              <el-tag
                v-for="tag in selectedCustomer.tags"
                :key="tag.tagCode"
                size="small"
                effect="light"
                round
                :style="{ backgroundColor: tag.color + '20', borderColor: tag.color, color: tag.color }"
              >
                {{ tag.tagName }}
              </el-tag>
              <el-button type="primary" link size="small" :icon="Edit" @click="openTagEdit">
                {{ $t('scriptLibrary.editTags') }}
              </el-button>
            </div>
          </div>
        </div>

        <div v-loading="loadingMatch" class="match-result-area">
          <div class="result-title">{{ $t('scriptLibrary.todayScripts') }}</div>
          <template v-if="todayScripts.length">
            <div
              v-for="item in todayScripts"
              :key="item.scriptId"
              :class="['script-recommend-card', { sent: item.alreadySent, disabled: !item.canSend }]"
            >
              <div class="recommend-header">
                <span class="script-name">{{ item.scriptName }}</span>
                <div class="recommend-badges">
                  <el-tag size="small" effect="plain">{{ categoryLabel(item.category) }}</el-tag>
                  <el-tag v-if="item.alreadySent" type="info" size="small" effect="dark">
                    {{ $t('scriptLibrary.alreadySent') }}
                  </el-tag>
                  <el-tag v-else-if="item.canSend" type="success" size="small" effect="dark">
                    {{ $t('scriptLibrary.canSend') }}
                  </el-tag>
                  <el-tag v-else type="danger" size="small" effect="plain">
                    {{ item.blockReason }}
                  </el-tag>
                </div>
              </div>
              <div class="script-content-box">
                {{ item.renderedContent || item.content }}
              </div>
              <div v-if="item.matchedTags?.length" class="matched-tags">
                <span class="label">{{ $t('scriptLibrary.matchedTags') }}：</span>
                <el-tag
                  v-for="tag in item.matchedTags"
                  :key="tag.tagCode"
                  size="small"
                  effect="plain"
                  :style="{ borderColor: tag.color, color: tag.color }"
                >
                  {{ tag.tagName }}
                </el-tag>
              </div>
              <div class="recommend-actions">
                <el-button
                  type="primary"
                  size="small"
                  :icon="ChatLineRound"
                  :disabled="item.alreadySent || !item.canSend"
                  :loading="sendingId === item.scriptId"
                  @click="sendScript(item)"
                >
                  {{ item.alreadySent ? $t('scriptLibrary.alreadySent') : $t('scriptLibrary.sendManual') }}
                </el-button>
              </div>
            </div>
          </template>
          <el-empty v-else :description="$t('scriptLibrary.noMatch')" />
        </div>
      </template>
      <el-empty v-else :description="$t('scriptLibrary.selectCustomerHint')" />
    </div>

    <!-- 右侧：话术库（按节点/天数分组） -->
    <div class="script-panel">
      <div class="panel-header">
        <span class="panel-title">{{ $t('scriptLibrary.scriptLibrary') }}</span>
        <el-button type="primary" size="small" :icon="Plus" @click="openScriptDialog()">
          {{ $t('scriptLibrary.newScript') }}
        </el-button>
      </div>
      <div v-loading="loadingScripts" class="script-list">
        <div v-for="group in groupedScripts" :key="group.stageCode" class="script-group">
          <div class="group-title">
            {{ group.stageName }}
            <span class="group-sub">{{ group.stageCode }}</span>
          </div>
          <div
            v-for="s in group.scripts"
            :key="s.id"
            :class="['script-card', { active: selectedScript?.id === s.id }]"
            @click="previewScript(s)"
          >
            <div class="script-day">D{{ s.dayStart }}<span v-if="s.dayStart !== s.dayEnd">~{{ s.dayEnd }}</span></div>
            <div class="script-name">{{ s.scriptName }}</div>
            <div class="script-meta">
              <el-tag size="small" effect="plain">{{ categoryLabel(s.category) }}</el-tag>
              <span class="script-mode">{{ matchModeLabel(s.tagMatchMode) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="panel-footer">
        {{ $t('scriptLibrary.footerTip') }}
      </div>
    </div>

    <!-- 话术编辑弹窗 -->
    <el-dialog
      v-model="scriptDialogVisible"
      :title="scriptForm.id ? $t('scriptLibrary.editScript') : $t('scriptLibrary.newScript')"
      width="640px"
      destroy-on-close
    >
      <el-form :model="scriptForm" label-width="100px">
        <el-form-item :label="$t('scriptLibrary.colScriptName')">
          <el-input v-model="scriptForm.scriptName" />
        </el-form-item>
        <el-form-item :label="$t('scriptLibrary.colScriptCode')">
          <el-input v-model="scriptForm.scriptCode" :placeholder="$t('scriptLibrary.scriptCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('scriptLibrary.colCategory')">
          <el-select v-model="scriptForm.category" style="width: 100%">
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('scriptLibrary.colContent')">
          <el-input v-model="scriptForm.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item :label="$t('scriptLibrary.colStage')">
          <el-input v-model="scriptForm.stageCode" :placeholder="$t('scriptLibrary.stagePlaceholder')" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('scriptLibrary.colDayStart')">
              <el-input-number v-model="scriptForm.dayStart" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('scriptLibrary.colDayEnd')">
              <el-input-number v-model="scriptForm.dayEnd" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('scriptLibrary.colTagMatchMode')">
          <el-radio-group v-model="scriptForm.tagMatchMode">
            <el-radio label="ANY">{{ $t('scriptLibrary.matchAny') }}</el-radio>
            <el-radio label="ALL">{{ $t('scriptLibrary.matchAll') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('scriptLibrary.colTags')">
          <el-select v-model="scriptForm.tags" multiple style="width: 100%">
            <el-option v-for="tag in allTags" :key="tag.tagCode" :label="tag.tagName" :value="tag.tagCode" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('scriptLibrary.colPriority')">
          <el-input-number v-model="scriptForm.priority" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('scriptLibrary.colSendLimit')">
          <el-input-number v-model="scriptForm.sendLimit" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('scriptLibrary.colEnabled')">
          <el-switch v-model="scriptForm.isEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scriptDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="savingScript" @click="saveScript">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 客户标签编辑弹窗 -->
    <el-dialog v-model="tagDialogVisible" :title="$t('scriptLibrary.editTags')" width="480px">
      <el-form label-width="80px">
        <el-form-item :label="$t('scriptLibrary.customerTags')">
          <el-select v-model="selectedTagCodes" multiple style="width: 100%">
            <el-option v-for="tag in allTags" :key="tag.tagCode" :label="tag.tagName" :value="tag.tagCode" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="savingTags" @click="saveCustomerTags">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, ChatLineRound } from '@element-plus/icons-vue'
import { salesScriptApi } from '@/api/modules/customer'

const customers = ref([])
const scripts = ref([])
const allTags = ref([])
const selectedCustomer = ref(null)
const selectedScript = ref(null)
const todayScripts = ref([])
const customerSearch = ref('')
const loadingCustomers = ref(false)
const loadingScripts = ref(false)
const loadingMatch = ref(false)
const sendingId = ref(null)

const scriptDialogVisible = ref(false)
const savingScript = ref(false)
const scriptForm = ref({
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

const tagDialogVisible = ref(false)
const selectedTagCodes = ref([])
const savingTags = ref(false)

const categoryOptions = [
  { label: '破冰', value: 'ICE_BREAK' },
  { label: '方案', value: 'SOLUTION' },
  { label: '成交', value: 'CLOSE' },
  { label: '复购唤醒', value: 'REACTIVATE' },
  { label: '通用', value: 'SALES' }
]

const filteredCustomers = computed(() => {
  if (!customerSearch.value) return customers.value
  const kw = customerSearch.value.toLowerCase()
  return customers.value.filter(
    c =>
      c.customerName?.toLowerCase().includes(kw) ||
      c.contact?.toLowerCase().includes(kw)
  )
})

const groupedScripts = computed(() => {
  const map = {}
  for (const s of scripts.value) {
    if (!map[s.stageCode]) {
      map[s.stageCode] = {
        stageCode: s.stageCode,
        stageName: s.stageName || s.stageCode,
        scripts: []
      }
    }
    map[s.stageCode].scripts.push(s)
  }
  return Object.values(map)
})

onMounted(() => {
  loadCustomers()
  loadScripts()
  loadTags()
})

async function loadCustomers() {
  loadingCustomers.value = true
  try {
    const res = await salesScriptApi.listMatchCustomers(50)
    customers.value = res.data || []
    if (customers.value.length && !selectedCustomer.value) {
      selectCustomer(customers.value[0])
    }
  } finally {
    loadingCustomers.value = false
  }
}

async function loadScripts() {
  loadingScripts.value = true
  try {
    const res = await salesScriptApi.listScripts()
    scripts.value = res.data || []
  } finally {
    loadingScripts.value = false
  }
}

async function loadTags() {
  const res = await salesScriptApi.listTags()
  allTags.value = res.data || []
}

async function selectCustomer(c) {
  selectedCustomer.value = c
  selectedScript.value = null
  loadingMatch.value = true
  try {
    const res = await salesScriptApi.matchScriptsForCustomer(c.customerId)
    todayScripts.value = res.data || []
  } finally {
    loadingMatch.value = false
  }
}

function previewScript(s) {
  selectedScript.value = s
}

function openScriptDialog(s) {
  if (s) {
    scriptForm.value = { ...s, tags: s.tags || [] }
  } else {
    scriptForm.value = {
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
  scriptDialogVisible.value = true
}

async function saveScript() {
  savingScript.value = true
  try {
    const data = { ...scriptForm.value }
    if (data.id) {
      await salesScriptApi.updateScript(data)
    } else {
      await salesScriptApi.saveScript(data)
    }
    ElMessage.success('保存成功')
    scriptDialogVisible.value = false
    await loadScripts()
    if (selectedCustomer.value) {
      await selectCustomer(selectedCustomer.value)
    }
  } finally {
    savingScript.value = false
  }
}

function openTagEdit() {
  selectedTagCodes.value = selectedCustomer.value.tags?.map(t => t.tagCode) || []
  tagDialogVisible.value = true
}

async function saveCustomerTags() {
  savingTags.value = true
  try {
    await salesScriptApi.saveCustomerTags(selectedCustomer.value.customerId, selectedTagCodes.value)
    ElMessage.success('标签保存成功')
    tagDialogVisible.value = false
    await loadCustomers()
    if (selectedCustomer.value) {
      const updated = customers.value.find(c => c.customerId === selectedCustomer.value.customerId)
      if (updated) await selectCustomer(updated)
    }
  } finally {
    savingTags.value = false
  }
}

async function sendScript(item) {
  sendingId.value = item.scriptId
  try {
    await salesScriptApi.sendScript({
      customerId: selectedCustomer.value.customerId,
      scriptId: item.scriptId,
      dayInStage: selectedCustomer.value.daysInStage
    })
    ElMessage.success('发送成功')
    await selectCustomer(selectedCustomer.value)
  } finally {
    sendingId.value = null
  }
}

function pendingCount(c) {
  return c.todayScripts?.filter(i => i.canSend && !i.alreadySent).length || 0
}

function channelLabel(channel) {
  const map = { WHATSAPP: 'WhatsApp', PHONE: '电话', SMS: '短信', IN_STORE: '到店', API: 'API' }
  return map[channel] || channel
}

function categoryLabel(category) {
  const item = categoryOptions.find(i => i.value === category)
  return item?.label || category
}

function matchModeLabel(mode) {
  return mode === 'ALL' ? '全部命中' : '任一命中'
}

function avatarColor(text) {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9254DE']
  let hash = 0
  for (let i = 0; i < (text || '').length; i++) hash = text.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}
</script>

<style scoped>
.script-library-page {
  display: flex;
  gap: 16px;
  height: calc(100vh - 120px);
  padding: 16px;
  background: #f5f7fa;
}

.customer-panel,
.match-panel,
.script-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.customer-panel {
  width: 320px;
  min-width: 280px;
}

.match-panel {
  flex: 1;
}

.script-panel {
  width: 300px;
  min-width: 260px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #ebeef5;
}

.panel-title {
  font-weight: 600;
  font-size: 15px;
}

.search-input {
  width: 160px;
}

.customer-list,
.script-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.customer-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: background 0.2s;
  border: 1px solid transparent;
}

.customer-card:hover,
.customer-card.active {
  background: #f0f9ff;
  border-color: #c6e2ff;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}

.customer-avatar.large {
  width: 64px;
  height: 64px;
  font-size: 22px;
}

.customer-info {
  flex: 1;
  min-width: 0;
}

.customer-name {
  font-weight: 600;
  font-size: 14px;
}

.customer-channel {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.customer-tags {
  display: flex;
  gap: 4px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.customer-meta {
  text-align: right;
  flex-shrink: 0;
}

.last-interact {
  color: #c0c4cc;
  font-size: 11px;
  margin-top: 4px;
}

.customer-header {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  align-items: center;
}

.customer-header-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.name {
  font-size: 18px;
  font-weight: 600;
}

.contact-row {
  color: #606266;
  font-size: 13px;
  margin-bottom: 10px;
}

.tag-row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.match-result-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.result-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 14px;
}

.script-recommend-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 14px;
  background: #fff;
}

.script-recommend-card.sent {
  background: #f5f7fa;
  opacity: 0.8;
}

.script-recommend-card.disabled {
  border-left: 4px solid #f56c6c;
}

.script-recommend-card:not(.disabled):not(.sent) {
  border-left: 4px solid #67c23a;
}

.recommend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.recommend-badges {
  display: flex;
  gap: 8px;
}

.script-name {
  font-weight: 600;
  font-size: 15px;
}

.script-content-box {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
  margin-bottom: 12px;
}

.matched-tags {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.matched-tags .label {
  color: #909399;
  font-size: 13px;
}

.recommend-actions {
  display: flex;
  justify-content: flex-end;
}

.script-group {
  margin-bottom: 16px;
}

.group-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 10px;
  padding: 0 4px;
}

.group-sub {
  color: #909399;
  font-weight: normal;
  font-size: 12px;
  margin-left: 6px;
}

.script-card {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.script-card:hover,
.script-card.active {
  border-color: #409eff;
  background: #f0f9ff;
}

.script-day {
  color: #409eff;
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 4px;
}

.script-card .script-name {
  font-size: 14px;
  margin-bottom: 6px;
}

.script-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #909399;
  font-size: 12px;
}

.panel-footer {
  padding: 12px;
  color: #909399;
  font-size: 12px;
  text-align: center;
  border-top: 1px solid #ebeef5;
}
</style>
