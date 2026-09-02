<template>
  <div class="workflow-approval-page">
    <el-card class="page-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ title }}</span>
          <div>
            <el-button @click="$router.back()">{{ t('workflow.back') }}</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!loading && !detail" :description="t('workflow.instanceNotFound')" />

      <template v-if="detail">
        <!-- 流程图 -->
        <div class="flowchart-section">
          <div class="section-title">
            <span class="title-icon">&#128256;</span>
            {{ t('workflow.flowChart') }}
            <span class="current-node-hint">
              — {{ t('workflow.currentNode') }}：
              <el-tag type="warning" size="small">{{ currentTaskName || '—' }}</el-tag>
            </span>
          </div>
          <div class="flowchart-wrapper" v-if="flowchartData.nodes.length > 0">
            <svg :width="flowchartData.svgWidth" :height="flowchartData.svgHeight" class="flowchart-svg">
              <defs>
                <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                  <polygon points="0 0, 10 3.5, 0 7" fill="#909399" />
                </marker>
                <marker id="arrowheadActive" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                  <polygon points="0 0, 10 3.5, 0 7" fill="#409eff" />
                </marker>
                <filter id="nodeShadow" x="-10%" y="-10%" width="130%" height="130%">
                  <feDropShadow dx="2" dy="3" stdDeviation="3" flood-opacity="0.15" />
                </filter>
                <filter id="activeGlow" x="-20%" y="-20%" width="140%" height="140%">
                  <feDropShadow dx="0" dy="0" stdDeviation="5" flood-color="#e6a23c" flood-opacity="0.8" />
                </filter>
                <filter id="doneGlow" x="-20%" y="-20%" width="140%" height="140%">
                  <feDropShadow dx="0" dy="0" stdDeviation="3" flood-color="#67c23a" flood-opacity="0.5" />
                </filter>
                <linearGradient id="gradApprove" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#5b9cf5" />
                  <stop offset="100%" stop-color="#409eff" />
                </linearGradient>
                <linearGradient id="gradCondition" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#f0b347" />
                  <stop offset="100%" stop-color="#e6a23c" />
                </linearGradient>
                <linearGradient id="gradCc" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#a8abb2" />
                  <stop offset="100%" stop-color="#909399" />
                </linearGradient>
                <linearGradient id="gradEnd" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#7ddb52" />
                  <stop offset="100%" stop-color="#67c23a" />
                </linearGradient>
                <linearGradient id="gradStart" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#73d860" />
                  <stop offset="100%" stop-color="#52c41a" />
                </linearGradient>
                <linearGradient id="gradActive" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#f5a623" />
                  <stop offset="100%" stop-color="#e6a23c" />
                </linearGradient>
                <linearGradient id="gradDone" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#7ddb52" />
                  <stop offset="100%" stop-color="#52c41a" />
                </linearGradient>
              </defs>

              <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
                <circle cx="1" cy="1" r="1" fill="#e0e0e0" />
              </pattern>
              <rect x="0" y="0" :width="flowchartData.svgWidth" :height="flowchartData.svgHeight" fill="url(#grid)" opacity="0.5" />

              <!-- 连线 -->
              <g v-for="(edge, idx) in flowchartData.edges" :key="'e' + idx">
                <path :d="edge.path" fill="none" :stroke="edge.active ? '#409eff' : '#b0b8c0'" stroke-width="2.5" :marker-end="edge.active ? 'url(#arrowheadActive)' : 'url(#arrowhead)'" />
                <rect v-if="edge.label" :x="edge.labelX - edge.labelWidth / 2 - 6" :y="edge.labelY - 10" :width="edge.labelWidth + 12" :height="20" rx="10" :fill="edge.isEnd ? '#ecf5ff' : '#fef0d9'" :stroke="edge.isEnd ? '#409eff' : '#e6a23c'" stroke-width="1" />
                <text v-if="edge.label" :x="edge.labelX" :y="edge.labelY" text-anchor="middle" dominant-baseline="middle" font-size="11" font-weight="600" :fill="edge.isEnd ? '#337ecc' : '#b88230'">{{ edge.label }}</text>
              </g>

              <!-- 节点 -->
              <g v-for="node in flowchartData.nodes" :key="node.id" :opacity="node.isCurrent || node.isDone ? 1 : 0.45">
                <defs>
                  <filter :id="'glow' + node.id" x="-50%" y="-50%" width="200%" height="200%">
                    <feDropShadow dx="0" dy="0" :stdDeviation="node.isCurrent ? 8 : 4" :flood-color="node.isCurrent ? '#ff6600' : '#e6a23c'" flood-opacity="0.9" />
                  </filter>
                </defs>

                <g :filter="node.isCurrent ? 'url(#glow' + node.id + ')' : 'none'">
                  <polygon
                    v-if="node.isDiamond"
                    :points="diamondPoints(node)"
                    :fill="node.isCurrent ? 'url(#gradActive)' : node.isDone ? 'url(#gradDone)' : 'url(#gradCondition)'"
                    :stroke="node.isCurrent ? '#ff6600' : '#fff'"
                    :stroke-width="node.isCurrent ? 4 : 2.5"
                    :style="node.isCurrent ? { animation: 'pulse 1.2s infinite' } : {}"
                  />
                  <text
                    v-if="node.isDiamond"
                    :x="node.cx" :y="node.cy - 1"
                    text-anchor="middle" dominant-baseline="middle"
                    :font-size="node.isCurrent ? 14 : 12" fill="#fff" font-weight="bold"
                  >{{ node.label }}</text>

                  <rect
                    v-else
                    :x="node.x" :y="node.y"
                    :width="node.width" :height="node.height"
                    :rx="(node.type === 4 || node.isStart) ? node.height / 2 : 10"
                    :ry="(node.type === 4 || node.isStart) ? node.height / 2 : 10"
                    :fill="node.isCurrent ? 'url(#gradActive)' : node.isDone ? 'url(#gradDone)' : node.isStart ? 'url(#gradStart)' : node.type === 1 ? 'url(#gradApprove)' : node.type === 3 ? 'url(#gradCc)' : 'url(#gradEnd)'"
                    :stroke="node.isCurrent ? '#ff6600' : '#fff'"
                    :stroke-width="node.isCurrent ? 4 : 2.5"
                    :style="node.isCurrent ? { animation: 'pulse 1.2s infinite' } : {}"
                  />
                  <template v-if="!node.isDiamond">
                    <text
                      :x="node.cx" :y="node.cy - (node.isStart ? 0 : 4)"
                      text-anchor="middle" dominant-baseline="middle"
                      :font-size="node.isCurrent ? 15 : (node.isStart ? 14 : 13)" fill="#fff" font-weight="bold"
                    >{{ node.label }}</text>
                    <text
                      v-if="!node.isStart"
                      :x="node.cx" :y="node.cy + 14"
                      text-anchor="middle" dominant-baseline="middle"
                      :font-size="node.isCurrent ? 11 : 10" fill="rgba(255,255,255,0.85)"
                    >{{ node.typeLabel }}</text>
                  </template>
                  <text v-if="node.isDone && !node.isStart" :x="node.cx + node.width / 2 - 8" :y="node.y + 16" font-size="16" fill="#fff">&#10003;</text>
                </g>

                <g v-if="node.isCurrent">
                  <rect
                    :x="node.x - 6" :y="node.y - 6"
                    :width="node.width + 12" :height="node.height + 12"
                    :rx="(node.type === 4 || node.isStart) ? (node.height + 12) / 2 : 12"
                    :ry="(node.type === 4 || node.isStart) ? (node.height + 12) / 2 : 12"
                    fill="none" stroke="#ff6600" stroke-width="2"
                    :style="{ animation: 'ringPulse 1.5s infinite' }"
                  />
                  <rect
                    :x="node.x - 14" :y="node.y - 14"
                    :width="node.width + 28" :height="node.height + 28"
                    :rx="(node.type === 4 || node.isStart) ? (node.height + 28) / 2 : 16"
                    :ry="(node.type === 4 || node.isStart) ? (node.height + 28) / 2 : 16"
                    fill="none" stroke="#ff6600" stroke-width="1.5"
                    :style="{ animation: 'ringPulse 1.5s infinite 0.3s' }"
                  />
                  <rect
                    :x="node.x - 22" :y="node.y - 22"
                    :width="node.width + 44" :height="node.height + 44"
                    :rx="(node.type === 4 || node.isStart) ? (node.height + 44) / 2 : 20"
                    :ry="(node.type === 4 || node.isStart) ? (node.height + 44) / 2 : 20"
                    fill="none" stroke="#ff6600" stroke-width="1"
                    :style="{ animation: 'ringPulse 1.5s infinite 0.6s' }"
                  />
                </g>

                <g v-if="node.isCurrent" :transform="'translate(' + (node.cx) + ', ' + (node.y + node.height + 20) + ')'">
                  <rect x="-40" y="-11" width="80" height="22" rx="11" fill="#ff6600" />
                  <text x="0" y="4" text-anchor="middle" dominant-baseline="middle" font-size="12" fill="#fff" font-weight="bold">{{ t('workflow.currentNode') }}</text>
                </g>
              </g>

              <!-- 图例 -->
              <g transform="translate(10, 10)">
                <rect x="0" y="0" width="340" height="28" rx="6" fill="rgba(255,255,255,0.9)" stroke="#ddd" stroke-width="1" />
                <rect x="8" y="6" width="16" height="16" rx="8" fill="url(#gradStart)" />
                <text x="28" y="18" font-size="11" fill="#606266">{{ t('workflow.startNode') }}</text>
                <rect x="60" y="7" width="14" height="14" rx="4" fill="url(#gradApprove)" />
                <text x="78" y="18" font-size="11" fill="#606266">{{ t('workflow.approve') }}</text>
                <polygon :points="diamondLegendPoints(128, 14)" fill="url(#gradCondition)" />
                <text x="146" y="18" font-size="11" fill="#606266">{{ t('workflow.conditionNode') }}</text>
                <rect x="170" y="7" width="14" height="14" rx="4" fill="url(#gradCc)" />
                <text x="188" y="18" font-size="11" fill="#606266">{{ t('workflow.ccNode') }}</text>
                <rect x="216" y="6" width="16" height="16" rx="8" fill="url(#gradEnd)" />
                <text x="236" y="18" font-size="11" fill="#606266">{{ t('workflow.endNode') }}</text>
                <rect x="260" y="7" width="14" height="14" rx="4" fill="url(#gradActive)" />
                <text x="278" y="18" font-size="11" fill="#606266">{{ t('workflow.currentNode') }}</text>
                <rect x="308" y="7" width="14" height="14" rx="4" fill="url(#gradDone)" />
                <text x="326" y="18" font-size="11" fill="#606266">{{ t('workflow.completed') }}</text>
              </g>
            </svg>
          </div>
          <el-empty v-else :description="t('workflow.noFlowchart')" />
        </div>

        <!-- 业务单据详情（插槽，由各页面自定义） -->
        <slot name="doc-detail" :detail="detail" :formatAmount="formatAmount" :docStatusType="docStatusType" :getSummaries="getSummaries" :t="t" />

        <!-- 基本信息 -->
        <div class="info-section">
          <div class="section-title">
            <span class="title-icon">&#128203;</span>
            {{ t('workflow.basicInfo') }}
          </div>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item :label="t('workflow.processName')">{{ detail.title || '—' }}</el-descriptions-item>
            <el-descriptions-item label="单据名称">{{ detail.docName || '—' }}</el-descriptions-item>
            <el-descriptions-item :label="t('workflow.instanceId')">{{ detail.instance?.id || '—' }}</el-descriptions-item>
            <el-descriptions-item :label="t('workflow.status')">
              <el-tag :type="statusType(detail.instance?.status)">
                {{ statusText(detail.instance?.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="t('workflow.startUser')">{{ detail.instance?.startUserName || '—' }}</el-descriptions-item>
            <el-descriptions-item :label="t('workflow.startTime')">{{ detail.instance?.createTime || '—' }}</el-descriptions-item>
            <el-descriptions-item :label="t('workflow.endTime')">{{ detail.instance?.endTime || '—' }}</el-descriptions-item>
            <el-descriptions-item :label="t('workflow.currentNode')" :span="3">
              <el-tag type="warning">{{ currentTaskName || '—' }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 操作日志 -->
        <div class="logs-section">
          <div class="section-title">
            <span class="title-icon">&#128221;</span>
            {{ t('workflow.operationLog') }}
          </div>
          <el-timeline v-if="detail.logs && detail.logs.length > 0">
            <el-timeline-item
              v-for="log in detail.logs"
              :key="log.id"
              :timestamp="log.createTime"
              :type="logType(log.operationType)"
              placement="top"
            >
              <div class="log-item">
                <span class="log-operator">{{ log.operatorName || log.operatorId }}</span>
                <span class="log-action">{{ logText(log.operationType) }}</span>
                <span v-if="log.comment" class="log-comment">：{{ log.comment }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else :description="t('workflow.noLogs')" />
        </div>

        <!-- 审批操作 -->
        <div class="action-section" v-if="canApprove">
          <div class="section-title">
            <span class="title-icon">&#9989;</span>
            {{ t('workflow.approvalOperation') }}
          </div>
          <el-form :model="approveForm" label-width="100px" style="max-width: 600px;">
            <el-form-item :label="t('workflow.approveComment')">
              <el-input v-model="approveForm.comment" type="textarea" :rows="3" :placeholder="t('workflow.inputComment')" />
            </el-form-item>
          </el-form>
          <div class="action-buttons">
            <el-button type="success" size="large" @click="doApprove" :loading="approving">
              {{ t('workflow.approveSuccess') }}
            </el-button>
            <el-button type="danger" size="large" @click="doReject" :loading="rejecting">
              {{ t('workflow.rejectTask') }}
            </el-button>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { useApprovalDetail } from './useApprovalDetail'

const { t } = useI18n()

defineProps({
  title: {
    type: String,
    default: '审批详情'
  }
})

const {
  loading,
  approving,
  rejecting,
  detail,
  approveForm,
  currentTaskName,
  canApprove,
  statusType,
  statusText,
  logType,
  logText,
  formatAmount,
  docStatusType,
  getSummaries,
  flowchartData,
  diamondPoints,
  diamondLegendPoints,
  doApprove,
  doReject
} = useApprovalDetail()
</script>

<style scoped>
.workflow-approval-page {
  padding: 0;
}

.page-card {
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flowchart-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-icon {
  font-size: 18px;
}

.current-node-hint {
  margin-left: 8px;
  font-size: 13px;
  font-weight: normal;
  color: #909399;
}

.flowchart-wrapper {
  background: #fafbfc;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  overflow-y: auto;
  max-height: 500px;
}

.flowchart-svg {
  display: block;
  min-width: 100%;
  overflow: visible;
}

.info-section {
  margin-bottom: 24px;
}

.logs-section {
  margin-bottom: 24px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.log-operator {
  font-weight: 600;
  color: #303133;
}

.log-action {
  color: #606266;
}

.log-comment {
  color: #909399;
}

.action-section {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 8px;
  padding: 20px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  padding-left: 100px;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.75; transform: scale(1.03); }
}

@keyframes ringPulse {
  0% { opacity: 0.9; }
  100% { opacity: 0; }
}
</style>