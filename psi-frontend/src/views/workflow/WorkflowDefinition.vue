<template>
  <div class="workflow-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('workflow.definition') }}</span>
          <el-button type="primary" @click="openAddDialog">{{ t('common.add') }}{{ t('workflow.definition') }}</el-button>
        </div>
      </template>
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="processName" :label="t('workflow.processName')" min-width="150" />
        <el-table-column prop="processKey" :label="t('workflow.processKey')" width="150" />
        <el-table-column prop="remark" :label="t('workflow.remark')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="version" :label="t('workflow.version')" width="80" />
        <el-table-column prop="status" :label="t('common.status')" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? t('workflow.enabled') : t('workflow.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('workflow.startTime')" width="170" />
        <el-table-column :label="t('common.actions')" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openDesign(scope.row)">{{ t('workflow.processDesign') }}</el-button>
            <el-button size="small" @click="openEditDialog(scope.row)">{{ t('common.edit') }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑流程定义对话框 -->
    <el-dialog v-model="formDialog" :title="isEdit ? t('common.edit') + t('workflow.definition') : t('common.add') + t('workflow.definition')" width="500px" destroy-on-close>
      <el-form :model="formData" label-width="100px">
        <el-form-item :label="t('workflow.processName')" required>
          <el-input v-model="formData.processName" />
        </el-form-item>
        <el-form-item :label="t('workflow.processKey')" required>
          <el-input v-model="formData.processKey" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="t('workflow.remark')">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" :active-text="t('workflow.enabled')" :inactive-text="t('workflow.disabled')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveDefinition" :loading="saving">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 流程设计对话框 -->
    <el-dialog v-model="designDialog" :title="t('workflow.processDesign') + ' - ' + designDef.processName" width="1100px" destroy-on-close top="2vh">
      <el-tabs v-model="designTab">
        <el-tab-pane :label="t('workflow.flowChart')" name="chart">
          <div class="flowchart-wrapper" v-if="flowchartData.nodes.length > 0">
            <svg :width="flowchartData.svgWidth" :height="flowchartData.svgHeight" class="flowchart-svg">
              <defs>
                <!-- 箭头 -->
                <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                  <polygon points="0 0, 10 3.5, 0 7" fill="#909399" />
                </marker>
                <!-- 绿色箭头（跳级连线） -->
                <marker id="arrowheadGreen" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                  <polygon points="0 0, 10 3.5, 0 7" fill="#67c23a" />
                </marker>
                <!-- 节点阴影 -->
                <filter id="nodeShadow" x="-10%" y="-10%" width="130%" height="130%">
                  <feDropShadow dx="2" dy="3" stdDeviation="3" flood-opacity="0.15" />
                </filter>
                <!-- 审批节点渐变 -->
                <linearGradient id="gradApprove" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#5b9cf5" />
                  <stop offset="100%" stop-color="#409eff" />
                </linearGradient>
                <!-- 条件节点渐变 -->
                <linearGradient id="gradCondition" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#f0b347" />
                  <stop offset="100%" stop-color="#e6a23c" />
                </linearGradient>
                <!-- 抄送节点渐变 -->
                <linearGradient id="gradCc" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#a8abb2" />
                  <stop offset="100%" stop-color="#909399" />
                </linearGradient>
                <!-- 结束节点渐变 -->
                <linearGradient id="gradEnd" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#7ddb52" />
                  <stop offset="100%" stop-color="#67c23a" />
                </linearGradient>
                <!-- 开始节点渐变 -->
                <linearGradient id="gradStart" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#73d860" />
                  <stop offset="100%" stop-color="#52c41a" />
                </linearGradient>
              </defs>

              <!-- 网格背景 -->
              <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
                <circle cx="1" cy="1" r="1" fill="#e0e0e0" />
              </pattern>
              <rect x="0" y="0" :width="flowchartData.svgWidth" :height="flowchartData.svgHeight" fill="url(#grid)" opacity="0.5" />

              <!-- 连线 -->
              <g v-for="(edge, idx) in flowchartData.edges" :key="'e' + idx">
                <path :d="edge.path" fill="none" :stroke="edge.isEnd ? '#409eff' : '#b0b8c0'" stroke-width="2.5" marker-end="url(#arrowhead)" />
                <rect v-if="edge.label" :x="edge.labelX - edge.labelWidth / 2 - 6" :y="edge.labelY - 10" :width="edge.labelWidth + 12" :height="20" rx="10" :fill="edge.isEnd ? '#ecf5ff' : '#fef0d9'" :stroke="edge.isEnd ? '#409eff' : '#e6a23c'" stroke-width="1" />
                <text v-if="edge.label" :x="edge.labelX" :y="edge.labelY" text-anchor="middle" dominant-baseline="middle" font-size="11" font-weight="600" :fill="edge.isEnd ? '#337ecc' : '#b88230'">{{ edge.label }}</text>
              </g>

              <!-- 节点 -->
              <g v-for="node in flowchartData.nodes" :key="node.id" filter="url(#nodeShadow)">
                <!-- 菱形条件节点 -->
                <polygon
                  v-if="node.isDiamond"
                  :points="diamondPoints(node)"
                  :fill="node.type === 2 ? 'url(#gradCondition)' : node.color"
                  stroke="#fff" stroke-width="2.5"
                />
                <text
                  v-if="node.isDiamond"
                  :x="node.cx" :y="node.cy - 1"
                  text-anchor="middle" dominant-baseline="middle"
                  font-size="12" fill="#fff" font-weight="bold"
                >{{ node.label }}</text>

                <!-- 圆角矩形节点 -->
                <rect
                  v-else
                  :x="node.x" :y="node.y"
                  :width="node.width" :height="node.height"
                  :rx="(node.type === 4 || node.isStart) ? node.height / 2 : 10"
                  :ry="(node.type === 4 || node.isStart) ? node.height / 2 : 10"
                  :fill="node.isStart ? 'url(#gradStart)' : node.type === 1 ? 'url(#gradApprove)' : node.type === 3 ? 'url(#gradCc)' : 'url(#gradEnd)'"
                  stroke="#fff" stroke-width="2.5"
                />
                <template v-if="!node.isDiamond">
                  <text
                    :x="node.cx" :y="node.cy - (node.isStart ? 0 : 4)"
                    text-anchor="middle" dominant-baseline="middle"
                    :font-size="node.isStart ? 14 : 13" fill="#fff" font-weight="bold"
                  >{{ node.label }}</text>
                  <text
                    v-if="!node.isStart"
                    :x="node.cx" :y="node.cy + 14"
                    text-anchor="middle" dominant-baseline="middle"
                    font-size="10" fill="rgba(255,255,255,0.85)"
                  >{{ node.typeLabel }}</text>
                </template>
              </g>

              <!-- 图例 -->
              <g transform="translate(10, 10)">
                <rect x="0" y="0" width="260" height="28" rx="6" fill="rgba(255,255,255,0.9)" stroke="#ddd" stroke-width="1" />
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
              </g>
            </svg>
          </div>
          <el-empty v-else :description="t('workflow.noFlowchartData')" />
        </el-tab-pane>
        <el-tab-pane :label="t('workflow.nodeConfig')" name="nodes">
          <div style="display: flex; gap: 20px; min-height: 400px;">
            <!-- 节点列表 -->
            <div style="width: 350px; flex-shrink: 0;">
              <el-button type="primary" size="small" @click="openNodeDialog(null)" style="margin-bottom: 10px;">{{ t('workflow.addNode') }}</el-button>
              <el-table :data="designNodes" border size="small" max-height="400">
                <el-table-column prop="nodeName" :label="t('workflow.nodeName')" />
                <el-table-column prop="nodeType" :label="t('workflow.nodeType')" width="90">
                  <template #default="scope">
                    <el-tag size="small" :type="nodeTypeTag(scope.row.nodeType)">{{ nodeTypeText(scope.row.nodeType) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="t('common.actions')" width="120">
                  <template #default="scope">
                    <el-button size="small" text @click="openNodeDialog(scope.row)">{{ t('common.edit') }}</el-button>
                    <el-button size="small" text type="danger" @click="handleDeleteNode(scope.row)">{{ t('common.delete') }}</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <!-- 连线列表 -->
            <div style="flex: 1;">
              <el-button type="success" size="small" @click="openRelationDialog(null)" style="margin-bottom: 10px;">{{ t('workflow.addRelation') }}</el-button>
              <el-table :data="designRelations" border size="small" max-height="400">
                <el-table-column :label="t('workflow.fromNode')" width="120">
                  <template #default="scope">
                    {{ getNodeName(scope.row.fromNodeId) }}
                  </template>
                </el-table-column>
                <el-table-column :label="t('workflow.toNode')" width="120">
                  <template #default="scope">
                    {{ getNodeName(scope.row.toNodeId) }}
                  </template>
                </el-table-column>
                <el-table-column prop="conditionExpr" :label="t('workflow.conditionExpr')" min-width="200">
                  <template #default="scope">
                    <el-tag v-if="scope.row.conditionExpr" type="warning" size="small">{{ scope.row.conditionExpr }}</el-tag>
                    <span v-else style="color: #999;">{{ t('workflow.noCondition') }}</span>
                  </template>
                </el-table-column>
                <el-table-column :label="t('common.actions')" width="120">
                  <template #default="scope">
                    <el-button size="small" text @click="openRelationDialog(scope.row)">{{ t('common.edit') }}</el-button>
                    <el-button size="small" text type="danger" @click="handleDeleteRelation(scope.row)">{{ t('common.delete') }}</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 节点编辑对话框 -->
    <el-dialog v-model="nodeDialog" :title="editingNode ? t('workflow.editNode') : t('workflow.addNode')" width="500px" destroy-on-close>
      <el-form :model="nodeForm" label-width="100px">
        <el-form-item :label="t('workflow.nodeName')" required>
          <el-input v-model="nodeForm.nodeName" />
        </el-form-item>
        <el-form-item :label="t('workflow.nodeType')" required>
          <el-select v-model="nodeForm.nodeType" style="width: 100%">
            <el-option :value="1" :label="t('workflow.approveNode')" />
            <el-option :value="2" :label="t('workflow.conditionNode')" />
            <el-option :value="3" :label="t('workflow.ccNode')" />
            <el-option :value="4" :label="t('workflow.endNode')" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('workflow.handlerUser')" v-if="nodeForm.nodeType === 1">
          <el-input v-model="nodeForm.handlerUser" :placeholder="t('workflow.handlerUserPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('workflow.sort')">
          <el-input-number v-model="nodeForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveNode" :loading="saving">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 连线编辑对话框 -->
    <el-dialog v-model="relationDialog" :title="editingRelation ? t('workflow.editRelation') : t('workflow.addRelation')" width="500px" destroy-on-close>
      <el-form :model="relationForm" label-width="100px">
        <el-form-item :label="t('workflow.fromNode')" required>
          <el-select v-model="relationForm.fromNodeId" style="width: 100%" :placeholder="t('workflow.selectFromNode')">
            <el-option v-for="n in designNodes" :key="n.id" :value="n.id" :label="n.nodeName" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('workflow.toNode')" required>
          <el-select v-model="relationForm.toNodeId" style="width: 100%" :placeholder="t('workflow.selectToNode')">
            <el-option v-for="n in designNodes" :key="n.id" :value="n.id" :label="n.nodeName" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('workflow.conditionExpr')">
          <el-input v-model="relationForm.conditionExpr" :placeholder="t('workflow.noCondition')" />
          <div style="color: #999; font-size: 12px; margin-top: 4px;">{{ t('workflow.conditionTip') }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="relationDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveRelation" :loading="saving">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { workflowApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const tableData = ref([])
const loading = ref(false)
const saving = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const response = await workflowApi.getDefinitions()
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

// 新增/编辑流程定义
const formDialog = ref(false)
const isEdit = ref(false)
const formData = ref({ processName: '', processKey: '', remark: '', status: 1, version: '1.0' })

const openAddDialog = () => {
  isEdit.value = false
  formData.value = { processName: '', processKey: '', remark: '', status: 1, version: '1.0' }
  formDialog.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  formData.value = { ...row }
  formDialog.value = true
}

const saveDefinition = async () => {
  if (!formData.value.processName || !formData.value.processKey) {
    ElMessage.warning('请填写流程名称和编码')
    return
  }
  saving.value = true
  try {
    let response
    if (isEdit.value) {
      response = await workflowApi.updateDefinition(formData.value.id, formData.value)
    } else {
      response = await workflowApi.saveDefinition(formData.value)
    }
    if (response.code === 200) {
      ElMessage.success(t('common.success'))
      formDialog.value = false
      loadList()
    } else {
      ElMessage.error(response.message || t('common.failed'))
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除流程 "${row.processName}" 吗？`, t('common.confirm'), { type: 'warning' })
    const response = await workflowApi.deleteDefinition(row.id)
    if (response.code === 200) {
      ElMessage.success(t('common.success'))
      loadList()
    } else {
      ElMessage.error(response.message || t('common.failed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 流程设计
const designDialog = ref(false)
const designTab = ref('nodes')
const designDef = ref({})
const designNodes = ref([])
const designRelations = ref([])

// 流程图布局计算
const NODE_W = 140
const NODE_H = 56
const DIAMOND_SIZE = 52
const H_GAP = 80
const V_GAP = 60
const PADDING = 40

const nodeColors = { 1: '#409eff', 2: '#e6a23c', 3: '#909399', 4: '#67c23a' }
const nodeTypeLabels = { 1: '审批节点', 2: '条件节点', 3: '抄送节点', 4: '结束节点' }

const flowchartData = computed(() => {
  const nodes = designNodes.value
  const relations = designRelations.value
  if (!nodes.length) return { nodes: [], edges: [], svgWidth: 0, svgHeight: 0 }

  // 分离结束节点
  const endNodes = nodes.filter(n => n.nodeType === 4)
  const endNodeIds = new Set(endNodes.map(n => n.id))
  const flowNodes = nodes.filter(n => n.nodeType !== 4)

  // 构建邻接表（只考虑非结束节点间的连线）
  const incoming = {}
  const outgoing = {}
  nodes.forEach(n => {
    incoming[n.id] = []
    outgoing[n.id] = []
  })
  relations.forEach(r => {
    if (outgoing[r.fromNodeId]) outgoing[r.fromNodeId].push(r)
    if (incoming[r.toNodeId]) incoming[r.toNodeId].push(r)
  })

  // 找起始节点（无入边或入边全来自条件节点，排除结束节点）
  const startNodes = flowNodes.filter(n => {
    const ins = incoming[n.id] || []
    if (ins.length === 0) return true
    const fromNodeIds = ins.map(r => r.fromNodeId)
    const fromNodes = fromNodeIds.map(id => nodes.find(nn => nn.id === id)).filter(Boolean)
    return fromNodes.every(fn => fn.nodeType === 2)
  })

  // BFS 分配层级（只对非结束节点）
  const levels = {}
  const queue = startNodes.map(n => ({ nodeId: n.id, level: 0 }))

  while (queue.length > 0) {
    const { nodeId, level } = queue.shift()
    if (levels[nodeId] !== undefined && levels[nodeId] >= level) continue
    levels[nodeId] = level
    const outs = outgoing[nodeId] || []
    outs.forEach(r => {
      // 跳过指向结束节点的连线
      if (!endNodeIds.has(r.toNodeId)) {
        queue.push({ nodeId: r.toNodeId, level: level + 1 })
      }
    })
  }

  flowNodes.forEach(n => {
    if (levels[n.id] === undefined) levels[n.id] = 0
  })

  // 按层级分组
  const levelGroups = {}
  Object.entries(levels).forEach(([nodeId, level]) => {
    if (!levelGroups[level]) levelGroups[level] = []
    levelGroups[level].push(nodeId)
  })

  const maxLevel = Math.max(...Object.keys(levelGroups).map(Number), 0)
  const maxNodesInLevel = Math.max(...Object.values(levelGroups).map(g => g.length), 1)

  // 计算 SVG 画布尺寸（+1 给开始节点，底部留空间给结束节点）
  const endAreaHeight = endNodes.length > 0 ? NODE_H + V_GAP * 2 : 0
  const svgWidth = (maxLevel + 2) * (NODE_W + H_GAP) + PADDING * 2
  const svgHeight = maxNodesInLevel * (NODE_H + V_GAP) + PADDING * 2 + endAreaHeight

  // 计算每个节点的位置（所有节点右移一层，给开始节点留位）
  const nodePositions = {}
  Object.entries(levelGroups).forEach(([level, nodeIds]) => {
    const lv = Number(level) + 1
    const totalHeight = nodeIds.length * (NODE_H + V_GAP) - V_GAP
    const startY = (svgHeight - endAreaHeight - totalHeight) / 2
    nodeIds.forEach((nodeId, index) => {
      const node = nodes.find(n => n.id === nodeId)
      const isDiamond = node && node.nodeType === 2
      nodePositions[nodeId] = {
        x: PADDING + lv * (NODE_W + H_GAP),
        y: startY + index * (NODE_H + V_GAP),
        isDiamond
      }
    })
  })

  // 结束节点放在底部居中
  if (endNodes.length > 0) {
    const endTotalW = endNodes.length * (NODE_W + H_GAP) - H_GAP
    const endStartX = (svgWidth - endTotalW) / 2
    const endY = svgHeight - endAreaHeight + V_GAP
    endNodes.forEach((en, idx) => {
      nodePositions[en.id] = {
        x: endStartX + idx * (NODE_W + H_GAP),
        y: endY,
        isDiamond: false
      }
    })
  }

  // 开始节点位置
  const startNodePos = {
    x: PADDING,
    y: (svgHeight - endAreaHeight) / 2 - NODE_H / 2,
    isDiamond: false
  }

  // 记录起始节点（原始流程中无入边的节点）
  let originalStartNodeIds = startNodes.map(n => n.id)
  // 如果有多个起始节点，只保留第一个，开始节点只连一个起点
  if (originalStartNodeIds.length > 1) {
    // 找出排序最小的节点（一般就是第一个审批节点）
    const minSortStart = originalStartNodeIds
      .map(id => nodes.find(n => n.id === id))
      .filter(Boolean)
      .sort((a, b) => (a.sort || 0) - (b.sort || 0))[0]
    originalStartNodeIds = minSortStart ? [minSortStart.id] : []
  }

  // 生成节点数据（首先添加开始节点）
  const startChartNode = {
    id: '__start__',
    x: startNodePos.x,
    y: startNodePos.y,
    width: NODE_W,
    height: NODE_H,
    cx: startNodePos.x + NODE_W / 2,
    cy: startNodePos.y + NODE_H / 2,
    label: '开始',
    type: 0,
    typeLabel: '',
    color: '#52c41a',
    isDiamond: false,
    isStart: true
  }

  const chartNodes = [startChartNode, ...nodes.map(n => {
    const pos = nodePositions[n.id] || { x: PADDING, y: PADDING }
    const isDiamond = n.nodeType === 2
    const w = isDiamond ? DIAMOND_SIZE : NODE_W
    const h = isDiamond ? DIAMOND_SIZE : NODE_H
    const cx = pos.x + (isDiamond ? 0 : NODE_W / 2)
    const cy = pos.y + (isDiamond ? 0 : NODE_H / 2) + (isDiamond ? DIAMOND_SIZE / 2 : 0)
    return {
      id: n.id,
      x: pos.x,
      y: pos.y,
      width: w,
      height: h,
      cx,
      cy,
      label: n.nodeName || '未命名',
      type: n.nodeType || 1,
      typeLabel: nodeTypeLabels[n.nodeType] || '节点',
      color: nodeColors[n.nodeType] || '#909399',
      isDiamond
    }
  })]

  // 生成连线数据
  const chartEdges = relations.map(r => {
    const from = nodePositions[r.fromNodeId]
    const to = nodePositions[r.toNodeId]
    if (!from || !to) return null

    const fromNode = chartNodes.find(n => n.id === r.fromNodeId)
    const toNode = chartNodes.find(n => n.id === r.toNodeId)
    const toIsEnd = endNodeIds.has(r.toNodeId)

    let x1, y1, x2, y2
    if (fromNode && fromNode.isDiamond) {
      x1 = fromNode.cx + DIAMOND_SIZE / 2 + 2
      y1 = fromNode.cy
    } else {
      x1 = from.x + NODE_W
      y1 = from.y + NODE_H / 2
    }
    if (toNode && toNode.isDiamond) {
      x2 = toNode.cx - DIAMOND_SIZE / 2 - 2
      y2 = toNode.cy
    } else if (toIsEnd) {
      x2 = to.x + NODE_W / 2
      y2 = to.y
    } else {
      x2 = to.x
      y2 = to.y + NODE_H / 2
    }

    const midX = (x1 + x2) / 2
    const midY = (y1 + y2) / 2

    let path
    if (toIsEnd) {
      // 到结束节点：先向右再向下
      const turnX = x1 + (x2 - x1) * 0.5
      path = `M ${x1} ${y1} L ${turnX} ${y1} L ${turnX} ${y2} L ${x2} ${y2}`
    } else if (Math.abs(y1 - y2) < 10) {
      path = `M ${x1} ${y1} L ${x2} ${y2}`
    } else {
      const cpX = x1 + (x2 - x1) * 0.4
      path = `M ${x1} ${y1} C ${cpX} ${y1}, ${cpX} ${y2}, ${x2} ${y2}`
    }

    const label = r.conditionExpr || ''
    const labelWidth = label.length * 7

    return {
      path,
      label,
      labelX: toIsEnd ? midX + 30 : midX,
      labelY: toIsEnd ? y1 + (y2 - y1) * 0.3 : midY - 6,
      labelWidth,
      isJump: false,
      isEnd: toIsEnd
    }
  }).filter(Boolean)

  // 添加开始节点到原始起始节点的连线
  originalStartNodeIds.forEach(toId => {
    const to = nodePositions[toId]
    if (!to) return
    const toNode = chartNodes.find(n => n.id === toId)
    const x1 = startNodePos.x + NODE_W
    const y1 = startNodePos.y + NODE_H / 2
    let x2, y2
    if (toNode && toNode.isDiamond) {
      x2 = toNode.cx - DIAMOND_SIZE / 2 - 2
      y2 = toNode.cy
    } else {
      x2 = to.x
      y2 = to.y + NODE_H / 2
    }
    const midX = (x1 + x2) / 2
    const midY = (y1 + y2) / 2
    let path
    if (Math.abs(y1 - y2) < 10) {
      path = `M ${x1} ${y1} L ${x2} ${y2}`
    } else {
      const cpX = x1 + (x2 - x1) * 0.4
      path = `M ${x1} ${y1} C ${cpX} ${y1}, ${cpX} ${y2}, ${x2} ${y2}`
    }
    chartEdges.unshift({ path, label: '', labelX: midX, labelY: midY - 6, labelWidth: 0 })
  })

  return {
    nodes: chartNodes,
    edges: chartEdges,
    svgWidth,
    svgHeight: Math.max(svgHeight, 200)
  }
})

// 计算菱形顶点坐标
const diamondPoints = (node) => {
  const { cx, cy } = node
  const d = DIAMOND_SIZE / 2
  return `${cx - d} ${cy}, ${cx} ${cy - d}, ${cx + d} ${cy}, ${cx} ${cy + d}`
}

// 计算图例小菱形坐标
const diamondLegendPoints = (x, y) => {
  const d = 7
  return `${x - d} ${y}, ${x} ${y - d}, ${x + d} ${y}, ${x} ${y + d}`
}

const openDesign = async (row) => {
  designDef.value = row
  designDialog.value = true
  designTab.value = 'chart'
  await loadDesignDetail(row.id)
}

const loadDesignDetail = async (defId) => {
  try {
    const response = await workflowApi.getDefinitionDetail(defId)
    if (response.code === 200) {
      designNodes.value = response.data.nodes || []
      designRelations.value = response.data.relations || []
    }
  } catch (error) {
    console.error('加载设计详情失败:', error)
    ElMessage.error('加载设计详情失败')
  }
}

const getNodeName = (nodeId) => {
  const node = designNodes.value.find(n => n.id === nodeId)
  return node ? node.nodeName : `节点${nodeId}`
}

const nodeTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'warning', 3: 'info', 4: 'success' }
  return tags[type] || 'info'
}

const nodeTypeText = (type) => {
  const texts = { 1: t('workflow.approveNode'), 2: t('workflow.conditionNode'), 3: t('workflow.ccNode'), 4: t('workflow.endNode') }
  return texts[type] || type
}

// 节点编辑
const nodeDialog = ref(false)
const editingNode = ref(null)
const nodeForm = ref({ nodeName: '', nodeType: 1, handlerUser: '', sortOrder: 0 })

const openNodeDialog = (node) => {
  if (node) {
    editingNode.value = node
    nodeForm.value = { ...node }
  } else {
    editingNode.value = null
    nodeForm.value = { nodeName: '', nodeType: 1, handlerUser: '', sortOrder: designNodes.value.length }
  }
  nodeDialog.value = true
}

const saveNode = async () => {
  if (!nodeForm.value.nodeName) {
    ElMessage.warning('请输入节点名称')
    return
  }
  saving.value = true
  try {
    let response
    const data = { ...nodeForm.value, processDefId: designDef.value.id }
    if (editingNode.value) {
      response = await workflowApi.updateNode(editingNode.value.id, data)
    } else {
      response = await workflowApi.saveNode(data)
    }
    if (response.code === 200) {
      ElMessage.success(t('common.success'))
      nodeDialog.value = false
      await loadDesignDetail(designDef.value.id)
    } else {
      ElMessage.error(response.message || t('common.failed'))
    }
  } catch (error) {
    console.error('保存节点失败:', error)
    ElMessage.error('保存节点失败')
  } finally {
    saving.value = false
  }
}

const handleDeleteNode = async (node) => {
  try {
    await ElMessageBox.confirm(`确认删除节点 "${node.nodeName}" 吗？关联的连线也会被删除。`, t('common.confirm'), { type: 'warning' })
    const response = await workflowApi.deleteNode(node.id)
    if (response.code === 200) {
      ElMessage.success(t('common.success'))
      await loadDesignDetail(designDef.value.id)
    } else {
      ElMessage.error(response.message || t('common.failed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除节点失败:', error)
      ElMessage.error('删除节点失败')
    }
  }
}

// 连线编辑
const relationDialog = ref(false)
const editingRelation = ref(null)
const relationForm = ref({ fromNodeId: '', toNodeId: '', conditionExpr: '' })

const openRelationDialog = (relation) => {
  if (relation) {
    editingRelation.value = relation
    relationForm.value = { ...relation }
  } else {
    editingRelation.value = null
    relationForm.value = { fromNodeId: '', toNodeId: '', conditionExpr: '' }
  }
  relationDialog.value = true
}

const saveRelation = async () => {
  if (!relationForm.value.fromNodeId || !relationForm.value.toNodeId) {
    ElMessage.warning('请选择来源节点和目标节点')
    return
  }
  saving.value = true
  try {
    let response
    const data = { ...relationForm.value, processDefId: designDef.value.id, status: 1 }
    if (editingRelation.value) {
      response = await workflowApi.updateRelation(editingRelation.value.id, data)
    } else {
      response = await workflowApi.saveRelation(data)
    }
    if (response.code === 200) {
      ElMessage.success(t('common.success'))
      relationDialog.value = false
      await loadDesignDetail(designDef.value.id)
    } else {
      ElMessage.error(response.message || t('common.failed'))
    }
  } catch (error) {
    console.error('保存连线失败:', error)
    ElMessage.error('保存连线失败')
  } finally {
    saving.value = false
  }
}

const handleDeleteRelation = async (relation) => {
  try {
    await ElMessageBox.confirm('确认删除该连线吗？', t('common.confirm'), { type: 'warning' })
    const response = await workflowApi.deleteRelation(relation.id)
    if (response.code === 200) {
      ElMessage.success(t('common.success'))
      await loadDesignDetail(designDef.value.id)
    } else {
      ElMessage.error(response.message || t('common.failed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除连线失败:', error)
      ElMessage.error('删除连线失败')
    }
  }
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

.flowchart-wrapper {
  overflow: auto;
  background: #f8f9fc;
  border-radius: 10px;
  padding: 16px;
  min-height: 300px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  border: 1px solid #ebeef5;
}
.flowchart-svg {
  display: block;
  min-width: 100%;
}
</style>