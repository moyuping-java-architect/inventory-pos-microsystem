<template>
  <div class="workflow-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('workflow.setting') }}</span>
        </div>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="t('workflowSetting.basicTab')" name="basic">
          <el-form :model="basicForm" label-width="140px" style="max-width: 600px;">
            <el-form-item :label="t('workflowSetting.timeoutReminder')">
              <el-switch v-model="basicForm.timeoutReminder" />
              <span class="form-tip">{{ t('workflowSetting.timeoutTip') }}</span>
            </el-form-item>
            <el-form-item :label="t('workflowSetting.timeoutHours')">
              <el-input-number v-model="basicForm.timeoutHours" :min="1" :max="72" :disabled="!basicForm.timeoutReminder" />
            </el-form-item>
            <el-form-item :label="t('workflowSetting.autoTransfer')">
              <el-switch v-model="basicForm.autoTransfer" />
              <span class="form-tip">{{ t('workflowSetting.autoTransferTip') }}</span>
            </el-form-item>
            <el-form-item :label="t('workflowSetting.autoTransferHours')">
              <el-input-number v-model="basicForm.autoTransferHours" :min="1" :max="168" :disabled="!basicForm.autoTransfer" />
            </el-form-item>
            <el-form-item :label="t('workflowSetting.ccNotification')">
              <el-switch v-model="basicForm.ccNotification" />
              <span class="form-tip">{{ t('workflowSetting.ccNotificationTip') }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasic" :loading="savingBasic">{{ t('common.save') }}</el-button>
              <el-button @click="resetBasic">{{ t('common.reset') }}</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane :label="t('workflowSetting.approverRuleTab')" name="approver">
          <div style="margin-bottom: 10px;">
            <el-button type="primary" size="small" @click="openRuleDialog(null)">{{ t('workflowSetting.addRule') }}</el-button>
          </div>
          <el-table :data="approverRules" border>
            <el-table-column prop="ruleName" :label="t('workflowSetting.ruleName')" min-width="150" />
            <el-table-column prop="processCode" :label="t('workflowSetting.applyProcess')" width="150" />
            <el-table-column prop="ruleType" :label="t('workflowSetting.ruleType')" width="100">
              <template #default="scope">
                <el-tag size="small">{{ scope.row.ruleType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="config" :label="t('workflowSetting.config')" min-width="150" />
            <el-table-column :label="t('common.actions')" width="150" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="openRuleDialog(scope.row)">{{ t('common.edit') }}</el-button>
                <el-button size="small" type="danger" @click="deleteRule(scope.row)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="approverRules.length === 0" :description="t('workflow.noApproverRules')" />
        </el-tab-pane>
        <el-tab-pane :label="t('workflowSetting.notificationTab')" name="notification">
          <el-form :model="notificationForm" label-width="140px" style="max-width: 600px;">
            <el-form-item :label="t('workflowSetting.todoReminder')">
              <el-switch v-model="notificationForm.todoReminder" />
              <span class="form-tip">{{ t('workflowSetting.todoReminderTip') }}</span>
            </el-form-item>
            <el-form-item :label="t('workflowSetting.remindMethod')">
              <el-checkbox-group v-model="notificationForm.todoRemindMethods" :disabled="!notificationForm.todoReminder">
                <el-checkbox label="站内信">{{ t('workflowSetting.internal') }}</el-checkbox>
                <el-checkbox label="邮件">{{ t('workflowSetting.email') }}</el-checkbox>
                <el-checkbox label="短信">{{ t('workflowSetting.sms') }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item :label="t('workflowSetting.completeNotification')">
              <el-switch v-model="notificationForm.completeNotification" />
              <span class="form-tip">{{ t('workflowSetting.completeNotificationTip') }}</span>
            </el-form-item>
            <el-form-item :label="t('workflowSetting.rejectNotification')">
              <el-switch v-model="notificationForm.rejectNotification" />
              <span class="form-tip">{{ t('workflowSetting.rejectNotificationTip') }}</span>
            </el-form-item>
            <el-form-item :label="t('workflowSetting.ccNotificationSetting')">
              <el-switch v-model="notificationForm.ccNotification" />
              <span class="form-tip">{{ t('workflowSetting.ccNotificationSettingTip') }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveNotification" :loading="savingNotification">{{ t('common.save') }}</el-button>
              <el-button @click="resetNotification">{{ t('common.reset') }}</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 审批人规则编辑对话框 -->
    <el-dialog v-model="ruleDialog" :title="editingRule ? t('workflowSetting.editRule') : t('workflowSetting.addRule')" width="500px" destroy-on-close>
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item :label="t('workflowSetting.ruleName')" required>
          <el-input v-model="ruleForm.ruleName" :placeholder="t('workflowSetting.ruleName')" />
        </el-form-item>
        <el-form-item :label="t('workflowSetting.applyProcess')" required>
          <el-select v-model="ruleForm.processCode" style="width: 100%" :placeholder="t('common.select') + t('workflowSetting.applyProcess')">
            <el-option value="*" :label="t('workflowSetting.allProcess')" />
            <el-option value="purchase_approval" :label="t('workflowSetting.purchaseApproval')" />
            <el-option value="sale_approval" :label="t('workflowSetting.saleApproval')" />
            <el-option value="leave_approval" :label="t('workflowSetting.leaveApproval')" />
            <el-option value="reimbursement_approval" :label="t('workflowSetting.reimbursementApproval')" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('workflowSetting.ruleType')" required>
          <el-select v-model="ruleForm.ruleType" style="width: 100%" :placeholder="t('common.select') + t('workflowSetting.ruleType')">
            <el-option value="角色" :label="t('workflowSetting.role')" />
            <el-option value="部门" :label="t('workflowSetting.department')" />
            <el-option value="职位" :label="t('workflowSetting.position')" />
            <el-option value="指定用户" :label="t('workflowSetting.specifiedUser')" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('workflowSetting.config')" required>
          <el-input v-model="ruleForm.config" :placeholder="ruleForm.ruleType === '指定用户' ? t('workflowSetting.configPlaceholderUser') : t('workflowSetting.configPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveRule" :loading="savingRule">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const activeTab = ref('basic')

// 基本设置
const defaultBasicForm = {
  timeoutReminder: true,
  timeoutHours: 24,
  autoTransfer: false,
  autoTransferHours: 48,
  ccNotification: true
}

const basicForm = reactive({ ...defaultBasicForm })
const savingBasic = ref(false)

const loadBasicSettings = () => {
  try {
    const saved = localStorage.getItem('workflow_basic_settings')
    if (saved) {
      Object.assign(basicForm, JSON.parse(saved))
    }
  } catch (e) {}
}

const saveBasic = async () => {
  savingBasic.value = true
  try {
    localStorage.setItem('workflow_basic_settings', JSON.stringify(basicForm))
    ElMessage.success(t('workflowSetting.saveSettingsSuccess'))
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingBasic.value = false
  }
}

const resetBasic = () => {
  Object.assign(basicForm, defaultBasicForm)
}

// 消息通知设置
const defaultNotificationForm = {
  todoReminder: true,
  todoRemindMethods: ['站内信'],
  completeNotification: true,
  rejectNotification: true,
  ccNotification: true
}

const notificationForm = reactive({ ...defaultNotificationForm })
const savingNotification = ref(false)

const loadNotificationSettings = () => {
  try {
    const saved = localStorage.getItem('workflow_notification_settings')
    if (saved) {
      Object.assign(notificationForm, JSON.parse(saved))
    }
  } catch (e) {}
}

const saveNotification = async () => {
  savingNotification.value = true
  try {
    localStorage.setItem('workflow_notification_settings', JSON.stringify(notificationForm))
    ElMessage.success('消息通知设置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingNotification.value = false
  }
}

const resetNotification = () => {
  Object.assign(notificationForm, defaultNotificationForm)
}

// 审批人规则
const approverRules = ref([])
const ruleDialog = ref(false)
const editingRule = ref(null)
const savingRule = ref(false)
const ruleForm = reactive({
  ruleName: '',
  processCode: '*',
  ruleType: '角色',
  config: ''
})

const loadApproverRules = () => {
  try {
    const saved = localStorage.getItem('workflow_approver_rules')
    if (saved) {
      approverRules.value = JSON.parse(saved)
    }
  } catch (e) {}
}

const saveApproverRules = () => {
  localStorage.setItem('workflow_approver_rules', JSON.stringify(approverRules.value))
}

const openRuleDialog = (row) => {
  if (row) {
    editingRule.value = row
    Object.assign(ruleForm, row)
  } else {
    editingRule.value = null
    ruleForm.ruleName = ''
    ruleForm.processCode = '*'
    ruleForm.ruleType = '角色'
    ruleForm.config = ''
  }
  ruleDialog.value = true
}

const saveRule = async () => {
  if (!ruleForm.ruleName || !ruleForm.config) {
    ElMessage.warning('请填写完整信息')
    return
  }
  savingRule.value = true
  try {
    if (editingRule.value) {
      const index = approverRules.value.findIndex(r => r.id === editingRule.value.id)
      if (index > -1) {
        approverRules.value[index] = { ...editingRule.value, ...ruleForm }
      }
    } else {
      approverRules.value.push({
        id: Date.now(),
        ...ruleForm
      })
    }
    saveApproverRules()
    ElMessage.success(t('common.success'))
    ruleDialog.value = false
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingRule.value = false
  }
}

const deleteRule = (row) => {
  ElMessageBox.confirm(`确定要删除规则 "${row.ruleName}" 吗？`, t('common.confirm'), {
    type: 'warning'
  }).then(() => {
    approverRules.value = approverRules.value.filter(r => r.id !== row.id)
    saveApproverRules()
    ElMessage.success(t('common.success'))
  }).catch(() => {})
}

onMounted(() => {
  loadBasicSettings()
  loadNotificationSettings()
  loadApproverRules()
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
.form-tip {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}
</style>