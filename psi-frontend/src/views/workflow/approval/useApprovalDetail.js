import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { workflowApi } from '../../../api'

export function useApprovalDetail() {
  const route = useRoute()
  const { t } = useI18n()

  const loading = ref(false)
  const approving = ref(false)
  const rejecting = ref(false)
  const detail = ref(null)
  const approveForm = ref({ comment: '' })

  // 当前任务名称
  const currentTaskName = computed(() => {
    if (!detail.value) return null
    const tasks = detail.value.tasks || []
    const pending = tasks.find(t => t.status === 1)
    if (pending) return pending.taskName || pending.nodeName || t('workflow.pending')
    const currentNodeId = detail.value.currentNodeId
    const nodes = detail.value.nodes || []
    const node = nodes.find(n => n.id === currentNodeId)
    return node ? node.nodeName : t('workflow.completed')
  })

  // 是否可以审批
  const canApprove = computed(() => {
    if (!detail.value) return false
    const tasks = detail.value.tasks || []
    return tasks.some(t => t.status === 1)
  })

  // 当前待审批任务
  const currentTask = computed(() => {
    if (!detail.value) return null
    const tasks = detail.value.tasks || []
    return tasks.find(t => t.status === 1) || null
  })

  // 状态颜色
  const statusType = (status) => {
    if (status === 1) return 'warning'
    if (status === 2) return 'success'
    if (status === 3) return 'danger'
    return 'info'
  }

  const statusText = (status) => {
    if (status === 1) return t('workflow.running')
    if (status === 2) return t('workflow.completed')
    if (status === 3) return t('workflow.rejected')
    return '—'
  }

  const logType = (type) => {
    if (type === 'approve') return 'success'
    if (type === 'reject') return 'danger'
    if (type === 'submit') return 'primary'
    return 'info'
  }

  const logText = (type) => {
    if (type === 'approve') return t('workflow.logApprove')
    if (type === 'reject') return t('workflow.logReject')
    if (type === 'submit') return t('workflow.logSubmit')
    return type || t('workflow.logOperation')
  }

  // 金额格式化
  const formatAmount = (val) => {
    if (val == null) return '0.00'
    return Number(val).toFixed(2)
  }

  // 单据状态类型
  const docStatusType = (status) => {
    if (status === 0) return 'info'
    if (status === 1) return 'warning'
    if (status === 2) return 'success'
    if (status === 3) return 'danger'
    if (status === 4) return ''
    if (status === 5) return 'success'
    return 'info'
  }

  // 合计行
  const getSummaries = (param) => {
    const { columns, data } = param
    const sums = []
    columns.forEach((col, index) => {
      if (index === 0) {
        sums[index] = t('workflow.total')
        return
      }
      if (col.property === 'quantity') {
        const total = data.reduce((acc, item) => acc + Number(item.quantity || 0), 0)
        sums[index] = total.toFixed(2)
      } else if (col.property === 'amount') {
        const total = data.reduce((acc, item) => acc + Number(item.amount || 0), 0)
        sums[index] = '\u00a5' + total.toFixed(2)
      } else {
        sums[index] = ''
      }
    })
    return sums
  }

  // ========== 流程图布局计算 ==========
  const NODE_W = 140
  const NODE_H = 56
  const DIAMOND_SIZE = 52
  const H_GAP = 80
  const V_GAP = 60
  const PADDING = 40
  const CURRENT_SCALE = 1.2

  const nodeTypeLabels = computed(() => ({
    1: t('workflow.approveNode'),
    2: t('workflow.conditionNode'),
    3: t('workflow.ccNode'),
    4: t('workflow.endNode')
  }))

  const flowchartData = computed(() => {
    if (!detail.value) return { nodes: [], edges: [], svgWidth: 0, svgHeight: 0 }

    const nodes = detail.value.nodes || []
    const relations = detail.value.relations || []
    const currentNodeId = detail.value.currentNodeId
    const tasks = detail.value.tasks || []

    if (!nodes.length) return { nodes: [], edges: [], svgWidth: 0, svgHeight: 0 }

    const doneNodeIds = new Set(tasks.filter(t => t.status === 2).map(t => t.nodeId))

    const endNodes = nodes.filter(n => n.nodeType === 4)
    const endNodeIds = new Set(endNodes.map(n => n.id))
    const flowNodes = nodes.filter(n => n.nodeType !== 4)

    const outgoing = {}
    const incoming = {}
    nodes.forEach(n => {
      incoming[n.id] = []
      outgoing[n.id] = []
    })
    relations.forEach(r => {
      if (outgoing[r.fromNodeId]) outgoing[r.fromNodeId].push(r)
      if (incoming[r.toNodeId]) incoming[r.toNodeId].push(r)
    })

    const startNodes = flowNodes.filter(n => {
      const ins = incoming[n.id] || []
      if (ins.length === 0) return true
      const fromNodeIds = ins.map(r => r.fromNodeId)
      const fromNodes = fromNodeIds.map(id => nodes.find(nn => nn.id === id)).filter(Boolean)
      return fromNodes.every(fn => fn.nodeType === 2)
    })

    const levels = {}
    const queue = startNodes.map(n => ({ nodeId: n.id, level: 0 }))
    while (queue.length > 0) {
      const { nodeId, level } = queue.shift()
      if (levels[nodeId] !== undefined && levels[nodeId] >= level) continue
      levels[nodeId] = level
      const outs = outgoing[nodeId] || []
      outs.forEach(r => {
        if (!endNodeIds.has(r.toNodeId)) {
          queue.push({ nodeId: r.toNodeId, level: level + 1 })
        }
      })
    }
    flowNodes.forEach(n => {
      if (levels[n.id] === undefined) levels[n.id] = 0
    })

    const levelGroups = {}
    Object.entries(levels).forEach(([nodeId, level]) => {
      if (!levelGroups[level]) levelGroups[level] = []
      levelGroups[level].push(nodeId)
    })

    const maxLevel = Math.max(...Object.keys(levelGroups).map(Number), 0)
    const maxNodesInLevel = Math.max(...Object.values(levelGroups).map(g => g.length), 1)
    const endAreaHeight = endNodes.length > 0 ? NODE_H + V_GAP * 2 : 0
    const svgWidth = (maxLevel + 2) * (NODE_W + H_GAP) + PADDING * 2
    const svgHeight = maxNodesInLevel * (NODE_H + V_GAP) + PADDING * 2 + endAreaHeight

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

    const startNodePos = {
      x: PADDING,
      y: (svgHeight - endAreaHeight) / 2 - NODE_H / 2,
      isDiamond: false
    }

    const vizNodes = [
      {
        id: 'START',
        label: t('workflow.startNode'),
        isStart: true,
        type: 0,
        isCurrent: false,
        isDone: true,
        ...startNodePos,
        width: NODE_W,
        height: NODE_H,
        cx: startNodePos.x + NODE_W / 2,
        cy: startNodePos.y + NODE_H / 2
      }
    ]

    flowNodes.forEach(node => {
      const pos = nodePositions[node.id] || { x: 0, y: 0, isDiamond: false }
      const isDiamond = node.nodeType === 2
      const isCurrent = node.id === currentNodeId
      const baseW = isDiamond ? DIAMOND_SIZE : NODE_W
      const baseH = isDiamond ? DIAMOND_SIZE : NODE_H
      const w = isCurrent ? baseW * CURRENT_SCALE : baseW
      const h = isCurrent ? baseH * CURRENT_SCALE : baseH
      const cx = pos.x + baseW / 2
      const cy = pos.y + baseH / 2
      vizNodes.push({
        id: node.id,
        label: node.nodeName,
        type: node.nodeType,
        typeLabel: nodeTypeLabels.value[node.nodeType] || '',
        isCurrent,
        isDone: doneNodeIds.has(node.id),
        isDiamond,
        isStart: false,
        x: isCurrent ? cx - w / 2 : pos.x,
        y: isCurrent ? cy - h / 2 : pos.y,
        width: w,
        height: h,
        cx,
        cy
      })
    })

    endNodes.forEach(node => {
      const pos = nodePositions[node.id] || { x: 0, y: 0 }
      const isCurrent = node.id === currentNodeId
      const w = isCurrent ? NODE_W * CURRENT_SCALE : NODE_W
      const h = isCurrent ? NODE_H * CURRENT_SCALE : NODE_H
      const cx = pos.x + NODE_W / 2
      const cy = pos.y + NODE_H / 2
      vizNodes.push({
        id: node.id,
        label: node.nodeName,
        type: 4,
        typeLabel: t('workflow.endNode'),
        isCurrent,
        isDone: false,
        isDiamond: false,
        isStart: false,
        x: isCurrent ? cx - w / 2 : pos.x,
        y: isCurrent ? cy - h / 2 : pos.y,
        width: w,
        height: h,
        cx,
        cy
      })
    })

    const vizEdges = []
    startNodes.forEach(sn => {
      const startCx = startNodePos.x + NODE_W
      const startCy = startNodePos.y + NODE_H / 2
      const target = vizNodes.find(vn => vn.id === sn.id)
      if (target) {
        vizEdges.push({
          path: `M ${startCx} ${startCy} L ${target.x} ${target.cy}`,
          label: '',
          active: true
        })
      }
    })

    relations.forEach(r => {
      const from = vizNodes.find(vn => vn.id === r.fromNodeId)
      const to = vizNodes.find(vn => vn.id === r.toNodeId)
      if (!from || !to) return

      const fromX = from.x + from.width
      const fromY = from.cy
      const toX = to.x
      const toY = to.cy
      const midX = (fromX + toX) / 2

      const path = `M ${fromX} ${fromY} C ${midX} ${fromY}, ${midX} ${toY}, ${toX} ${toY}`

      vizEdges.push({
        path,
        label: r.conditionExpr || '',
        labelX: midX,
        labelY: fromY < toY ? fromY + 16 : fromY - 16,
        labelWidth: (r.conditionExpr || '').length * 7,
        isEnd: to.type === 4,
        active: from.isDone || from.isCurrent
      })
    })

    return { nodes: vizNodes, edges: vizEdges, svgWidth, svgHeight }
  })

  const diamondPoints = (node) => {
    const { cx, cy, width, height } = node
    const hw = width / 2
    const hh = height / 2
    return `${cx},${cy - hh} ${cx + hw},${cy} ${cx},${cy + hh} ${cx - hw},${cy}`
  }

  const diamondLegendPoints = (cx, cy) => {
    return `${cx},${cy - 7} ${cx + 7},${cy} ${cx},${cy + 7} ${cx - 7},${cy}`
  }

  // ========== 审批操作 ==========
  const doApprove = async () => {
    const task = currentTask.value
    if (!task) {
      ElMessage.warning(t('workflow.noTask'))
      return
    }
    approving.value = true
    try {
      const response = await workflowApi.approveTask(task.id, {
        comment: approveForm.value.comment,
        operatorId: localStorage.getItem('userId') || '1',
        operatorName: localStorage.getItem('userName') || '当前用户',
        variables: {}
      })
      if (response.code === 200) {
        ElMessage.success(t('workflow.approveSuccess'))
        loadDetail()
      } else {
        ElMessage.error(response.message || t('workflow.approveFailed'))
      }
    } catch (error) {
      console.error('审批失败:', error)
      ElMessage.error(t('workflow.approveFailed'))
    } finally {
      approving.value = false
    }
  }

  const doReject = async () => {
    const task = currentTask.value
    if (!task) {
      ElMessage.warning(t('workflow.noTask'))
      return
    }
    rejecting.value = true
    try {
      const response = await workflowApi.rejectTask(task.id, {
        comment: approveForm.value.comment,
        operatorId: localStorage.getItem('userId') || '1',
        operatorName: localStorage.getItem('userName') || '当前用户',
        variables: {}
      })
      if (response.code === 200) {
        ElMessage.success(t('workflow.rejectSuccess'))
        loadDetail()
      } else {
        ElMessage.error(response.message || t('workflow.rejectFailed'))
      }
    } catch (error) {
      console.error('驳回失败:', error)
      ElMessage.error(t('workflow.rejectFailed'))
    } finally {
      rejecting.value = false
    }
  }

  const loadDetail = async () => {
    const instanceId = route.params.id
    if (!instanceId) {
      ElMessage.error(t('workflow.missingInstanceId'))
      return
    }
    loading.value = true
    try {
      const response = await workflowApi.getApprovalDetail(instanceId)
      if (response.code === 200) {
        detail.value = response.data
      } else {
        ElMessage.error(response.message || t('workflow.loadingFailed'))
      }
    } catch (error) {
      console.error('加载审批详情失败:', error)
      ElMessage.error(t('workflow.loadingFailed'))
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    loadDetail()
  })

  return {
    loading,
    approving,
    rejecting,
    detail,
    approveForm,
    currentTaskName,
    canApprove,
    currentTask,
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
    doReject,
    loadDetail,
    t
  }
}