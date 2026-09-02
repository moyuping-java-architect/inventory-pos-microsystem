<template>
  <div class="customer-dashboard">
    <!-- ===== 顶部统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ dashboard.totalMembers || 0 }}</div>
          <div class="stat-label">总客户数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ dashboard.activeMembers || 0 }}</div>
          <div class="stat-label">活跃客户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon risk">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ dashboard.atRiskMembers || 0 }}</div>
          <div class="stat-label">流失风险</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon churned">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ dashboard.churnedMembers || 0 }}</div>
          <div class="stat-label">已流失</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon revenue">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value danger">{{ formatMoney(dashboard.revenueAtRisk) }}</div>
          <div class="stat-label">风险收入</div>
        </div>
      </div>
    </div>

    <!-- ===== 收入对比 + 客单价 ===== -->
    <div class="metrics-row">
      <div class="metric-card">
        <div class="metric-label">本月收入</div>
        <div class="metric-value">{{ formatMoney(dashboard.totalRevenueThisMonth) }}</div>
        <div class="metric-trend">
          <el-icon v-if="growthPositive" color="#67c23a"><CaretTop /></el-icon>
          <el-icon v-else color="#f56c6c"><CaretBottom /></el-icon>
          <span :class="growthPositive ? 'trend-up' : 'trend-down'">
            {{ Math.abs(growthRate).toFixed(1) }}%
          </span>
          <span class="metric-hint">vs 上月 {{ formatMoney(dashboard.totalRevenueLastMonth) }}</span>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-label">平均客单价</div>
        <div class="metric-value">{{ formatMoney(dashboard.avgOrderValue) }}</div>
        <div class="metric-hint-inline">基于历史订单</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">复购率</div>
        <div class="metric-value">{{ repurchaseRate.toFixed(1) }}%</div>
        <div class="metric-hint-inline">消费2次以上占比</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">待跟进触点</div>
        <div class="metric-value warn">{{ dashboard.pendingFollowUps || 0 }}</div>
        <div class="metric-hint-inline">需要回访</div>
      </div>
    </div>

    
    
    <!-- ===== 客户详情弹窗 ===== -->
    <el-dialog
      v-model="timelineVisible"
      :title="`客户旅程 - ${currentCustomerName}`"
      width="880px"
      top="5vh"
      class="journey-detail-dialog"
    >
      <div v-if="journeyDetail" v-loading="loadingDetail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="section-title"><el-icon><User /></el-icon> 基本信息</div>
          <div class="info-grid">
            <div class="info-item"><span class="info-label">姓名</span>{{ journeyDetail.memberName || '-' }}</div>
            <div class="info-item"><span class="info-label">电话</span>{{ journeyDetail.phone || '-' }}</div>
            <div class="info-item"><span class="info-label">累计消费</span>{{ formatMoney(journeyDetail.totalSpent) }}</div>
            <div class="info-item"><span class="info-label">订单数</span>{{ journeyDetail.totalOrders || 0 }}</div>
            <div class="info-item"><span class="info-label">客单价</span>{{ formatMoney(journeyDetail.avgOrderValue) }}</div>
            <div class="info-item"><span class="info-label">流失等级</span>
              <el-tag :type="churnTagType(journeyDetail.churnLevel)" size="small">{{ churnLabel(journeyDetail.churnLevel) }}</el-tag>
            </div>
          </div>
        </div>

        <!-- 当前旅程节点 -->
        <div class="detail-section">
          <div class="section-title"><el-icon><Position /></el-icon> 当前旅程节点</div>
          <div class="stage-bar">
            <template v-if="journeyDetail.currentStageCode">
              <el-tag type="primary" effect="dark" size="large">{{ journeyDetail.currentStageName || journeyDetail.currentStageCode }}</el-tag>
              <span class="stage-day">已停留 <b>{{ journeyDetail.daysInStage }}</b> 天</span>
              <span class="stage-code">{{ journeyDetail.currentStageCode }}</span>
              <span v-if="journeyDetail.customerTags && journeyDetail.customerTags.length" class="tag-list">
                <el-tag v-for="t in journeyDetail.customerTags" :key="t" size="small" type="info">{{ t }}</el-tag>
              </span>
            </template>
            <el-empty v-else description="暂无旅程阶段数据" :image-size="60" />
          </div>
        </div>

        <!-- 目前需要发送的话术 -->
        <div class="detail-section">
          <div class="section-title">
            <el-icon><Promotion /></el-icon> 目前需要发送的话术
            <span class="section-count">{{ (journeyDetail.matchedScripts || []).length }}</span>
          </div>
          <div v-if="journeyDetail.matchedScripts && journeyDetail.matchedScripts.length" class="script-list">
            <div v-for="s in journeyDetail.matchedScripts" :key="s.scriptId" class="script-card">
              <div class="script-head">
                <span class="script-name">{{ s.scriptName }}</span>
                <el-tag size="small" type="warning">第{{ s.dayStart }}天</el-tag>
                <el-tag v-if="s.matchedTags && s.matchedTags.length" size="small" type="info">
                  命中标签：{{ s.matchedTags.map(t => t.tagName).join('、') }}
                </el-tag>
              </div>
              <div class="script-meta">
                <span>渠道：{{ channelLabel(s.channel) }}</span>
                <span v-if="s.category">分类：{{ s.category }}</span>
              </div>
              <div class="script-content">{{ s.renderedContent || s.content }}</div>
              <div class="script-actions">
                <el-button type="primary" size="small" @click="sendScript(journeyDetail.memberId, s)">发送</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="当前没有待发送的话术" :image-size="60" />
        </div>

        <!-- 已发送历史 -->
        <div class="detail-section">
          <div class="section-title">
            <el-icon><Clock /></el-icon> 已发送历史
            <span class="section-count">{{ (journeyDetail.sentScripts || []).length }}</span>
          </div>
          <el-table v-if="journeyDetail.sentScripts && journeyDetail.sentScripts.length" :data="journeyDetail.sentScripts" size="small" border>
            <el-table-column prop="scriptName" label="话术" min-width="160" />
            <el-table-column prop="stageCode" label="节点" width="120" />
            <el-table-column prop="dayInStage" label="停留天数" width="90" />
            <el-table-column label="渠道" width="100">
              <template #default="{ row }">{{ channelLabel(row.channel) }}</template>
            </el-table-column>
            <el-table-column prop="sendTime" label="发送时间" width="160" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'SENT' ? 'success' : 'info'" size="small">
                  {{ row.status === 'SENT' ? '已发送' : row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="还没有发送过话术" :image-size="60" />
        </div>

        <!-- 历史时间线 -->
        <div class="detail-section">
          <div class="section-title"><el-icon><Histogram /></el-icon> 历史时间线</div>
          <el-timeline v-if="timelineList.length">
            <el-timeline-item
              v-for="(item, idx) in timelineList"
              :key="idx"
              :timestamp="item.time"
              :type="item.eventType === 'ORDER' ? 'success' : 'primary'"
              placement="top"
            >
              <div class="timeline-item">
                <el-tag :type="item.eventType === 'ORDER' ? 'success' : 'info'" size="small">
                  {{ item.eventType === 'ORDER' ? '购买' : '触点' }}
                </el-tag>
                <span class="timeline-summary">{{ item.summary }}</span>
                <span v-if="item.amount" class="timeline-amount">{{ formatMoney(item.amount) }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无事件" :image-size="60" />
        </div>
      </div>
    </el-dialog>

    <!-- ===== 客户旅程（按阶段分组） ===== -->
    <div class="chart-card full-width journey-map">
      <div class="chart-header">
        <h3>客户旅程</h3>
        <div class="journey-actions">
          <span class="journey-hint">每个节点 = 一个旅程阶段，点击客户查看其时间线</span>
          <el-select
            v-model="currentJourney"
            size="small"
            class="journey-select"
            placeholder="选择旅程"
            @change="loadStages"
          >
            <el-option
              v-for="tpl in journeyTemplates"
              :key="tpl.journeyCode"
              :label="tpl.journeyName || tpl.journeyCode"
              :value="tpl.journeyCode"
            />
          </el-select>
          <el-button type="primary" size="small" @click="loadStages" :loading="loadingStages">
            刷新旅程
          </el-button>
        </div>
      </div>
      <div class="journey-flow" v-loading="loadingStages">
        <div
          v-for="(stage, idx) in stages"
          :key="stage.stageCode"
          class="journey-stage"
          :class="{ 'stage-empty': stage.customerCount === 0 }"
          :style="{ borderTopColor: stage.color }"
        >
          <div class="stage-head">
            <span class="stage-title">
              <span class="stage-badge" :style="{ background: stage.color }">
                <el-icon v-if="ICON_MAP[stage.icon]" class="stage-badge-icon">
                  <component :is="ICON_MAP[stage.icon]" />
                </el-icon>
                <span>{{ stage.stageDisplayName }}</span>
              </span>
              <span class="stage-count">{{ stage.customerCount }} 人</span>
            </span>
            <span class="stage-revenue">{{ formatMoney(stage.totalRevenue) }}</span>
          </div>
          <div class="stage-list">
            <div
              v-for="cust in stage.customers"
              :key="cust.memberId"
              class="stage-cust"
              @click="handleRowClick(cust)"
            >
              <span class="cust-name">{{ cust.memberName }}</span>
              <span class="cust-spend">{{ formatMoney(cust.totalSpent) }}</span>
            </div>
            <div v-if="stage.customerCount === 0" class="stage-empty-tip">
              暂无
            </div>
          </div>
          <div v-if="idx < stages.length - 1" class="stage-arrow">→</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, VideoCamera, Document, EditPen, Trophy,
  CircleCheck, CircleClose, Warning, Money,
  CaretTop, CaretBottom, Position, Promotion, Clock, Histogram
} from '@element-plus/icons-vue'

// 阶段图标映射（配置里的 icon 名字符串 → Element Plus 图标组件）
const ICON_MAP = {
  User, VideoCamera, Document, Money, EditPen, Trophy,
  Warning, CircleCheck, CircleClose
}
import { customerJourneyApi } from '../../api/index.js'
import { salesScriptApi } from '../../api/modules/customer.js'

const dashboard = ref({})
const timelineVisible = ref(false)
const timelineList = ref([])
const currentCustomerName = ref('')
const journeyDetail = ref(null)
const loadingDetail = ref(false)

// 客户旅程（按阶段分组）
const stages = ref([])
const loadingStages = ref(false)
// 旅程模板下拉：销售漏斗 / 生命周期 / 会员成长，切换即换一套看板
const journeyTemplates = ref([])
const currentJourney = ref('')

const formatMoney = (value) => {
  const num = Number(value || 0)
  return '¥ ' + num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const growthRate = computed(() => {
  const d = dashboard.value
  if (!d || !d.totalRevenueLastMonth) return 0
  return ((d.totalRevenueThisMonth - d.totalRevenueLastMonth) / d.totalRevenueLastMonth * 100)
})

const growthPositive = computed(() => growthRate.value >= 0)

const repurchaseRate = computed(() => {
  const d = dashboard.value
  if (!d || !d.totalMembers) return 0
  const repeat = d.totalMembers - (d.newMembers || 0)
  return (repeat / d.totalMembers * 100)
})

const loadDashboard = async () => {
  try {
    const res = await customerJourneyApi.getDashboard()
    dashboard.value = res.data || {}
  } catch (e) {
    console.error('加载看板失败:', e)
  }
}

const handleRowClick = async (row) => {
  currentCustomerName.value = row.memberName || row.customerName || '未知'
  const id = row.memberId || row.customerId
  loadingDetail.value = true
  timelineVisible.value = true
  journeyDetail.value = null
  try {
    const res = await customerJourneyApi.getCustomerJourney(id)
    const data = res.data || {}
    journeyDetail.value = data
    timelineList.value = data.timeline || []
  } catch (e) {
    ElMessage.error('加载客户旅程失败')
  } finally {
    loadingDetail.value = false
  }
}

const sendScript = async (customerId, script) => {
  try {
    await salesScriptApi.sendScript({ customerId, scriptId: script.scriptId })
    ElMessage.success('发送成功')
    // 刷新弹窗数据
    const res = await customerJourneyApi.getCustomerJourney(customerId)
    journeyDetail.value = res.data || {}
    timelineList.value = journeyDetail.value.timeline || []
  } catch (e) {
    ElMessage.error('发送失败')
  }
}

const churnTagType = (level) => {
  if (level === 'HIGH_RISK' || level === 'CHURNED') return 'danger'
  if (level === 'AT_RISK' || level === 'NEW_CHURNED') return 'warning'
  if (level === 'ACTIVE') return 'success'
  return 'info'
}
const churnLabel = (level) => {
  const map = {
    ACTIVE: '活跃', NEW: '新客', AT_RISK: '有风险',
    HIGH_RISK: '高风险', CHURNED: '已流失', NEW_CHURNED: '新流失', UNKNOWN: '未知'
  }
  return map[level] || level || '未知'
}
const channelLabel = (c) => {
  if (!c) return '-'
  return { SMS: '短信', WHATSAPP: 'WhatsApp', EMAIL: '邮件', WECHAT: '微信', CALL: '电话' }[c] || c
}

const loadStages = async () => {
  loadingStages.value = true
  try {
    const res = await customerJourneyApi.getStages(10, currentJourney.value || undefined)
    stages.value = res.data || []
  } catch (e) {
    console.error('加载客户旅程失败:', e)
    ElMessage.error('加载客户旅程失败')
  } finally {
    loadingStages.value = false
  }
}

// 拉旅程模板列表，默认选中 isDefault 的那套（没有就取第一个）
const loadJourneyTemplates = async () => {
  try {
    const res = await customerJourneyApi.listJourneyTemplates()
    const list = res.data || []
    journeyTemplates.value = list
    if (!currentJourney.value && list.length) {
      const def = list.find(t => t.isDefault === 1) || list[0]
      currentJourney.value = def.journeyCode
    }
  } catch (e) {
    console.error('加载旅程模板失败:', e)
  }
}

onMounted(async () => {
  loadDashboard()
  await loadJourneyTemplates()
  loadStages()
})
</script>

<style scoped>
.customer-dashboard { padding: 20px; }

/* 统计卡片 */
.stats-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 20px;
}
.stat-card {
  display: flex; align-items: center; background: #fff; padding: 20px;
  border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; margin-right: 14px;
  font-size: 22px; color: #fff;
}
.stat-icon.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.active { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.risk { background: linear-gradient(135deg, #f7b733 0%, #fc4a1a 100%); }
.stat-icon.churned { background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%); }
.stat-icon.revenue { background: linear-gradient(135deg, #c31432 0%, #240b36 100%); }
.stat-info { flex: 1; }
.stat-value { font-size: 22px; font-weight: 700; color: #333; }
.stat-value.danger { color: #f56c6c; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }

/* 指标行 */
.metrics-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px;
}
.metric-card {
  background: #fff; border-radius: 8px; padding: 18px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.metric-label { font-size: 13px; color: #999; margin-bottom: 8px; }
.metric-value { font-size: 24px; font-weight: 700; color: #333; }
.metric-value.warn { color: #e6a23c; }
.metric-trend {
  display: flex; align-items: center; gap: 4px; margin-top: 8px; font-size: 13px;
}
.trend-up { color: #67c23a; font-weight: 600; }
.trend-down { color: #f56c6c; font-weight: 600; }
.metric-hint { color: #bbb; font-size: 12px; }
.metric-hint-inline { color: #bbb; font-size: 12px; margin-top: 6px; }

.chart-card {
  background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}
.chart-card.full-width { grid-column: span 2; }
.chart-header {
  padding: 14px 20px; border-bottom: 1px solid #eee;
  display: flex; justify-content: space-between; align-items: center;
}
.chart-header h3 { margin: 0; font-size: 15px; font-weight: 600; color: #333; }
.chart-content { padding: 16px 20px; }


/* 时间线 */
.timeline-item {
  display: flex; align-items: center; gap: 10px;
}
.timeline-summary { font-size: 14px; color: #333; }
.timeline-amount { font-size: 14px; color: #f56c6c; font-weight: 600; }

/* 客户旅程漏斗 */
.journey-map { margin-top: 0; }
.journey-actions {
  display: flex; align-items: center; gap: 12px;
}
.journey-hint { font-size: 12px; color: #999; }
.journey-select { width: 150px; }
.journey-flow {
  display: flex; align-items: stretch; gap: 0; padding: 20px;
  overflow-x: auto;
}
.journey-stage {
  flex: 1; min-width: 140px; max-width: 220px;
  background: #f7f9fc; border-radius: 8px;
  padding: 12px; margin: 0 4px;
  position: relative;
  display: flex; flex-direction: column;
  border-top: 3px solid #ddd;
}
.journey-stage.stage-empty   { opacity: 0.55; }
.stage-badge {
  display: inline-flex; align-items: center; gap: 4px;
  color: #fff; padding: 4px 10px; border-radius: 4px;
  font-size: 12px; font-weight: 600;
}
.stage-badge-icon { font-size: 14px; }
.stage-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 10px;
}
.stage-title { display: flex; align-items: center; gap: 6px; }
.stage-count { font-size: 12px; color: #666; font-weight: 600; }
.stage-revenue { font-size: 12px; color: #f56c6c; font-weight: 600; }
.stage-list { display: flex; flex-direction: column; gap: 4px; min-height: 40px; }
.stage-cust {
  display: flex; justify-content: space-between; align-items: center;
  padding: 5px 8px; font-size: 12px;
  background: #fff; border-radius: 4px;
  cursor: pointer; transition: all 0.15s;
}
.stage-cust:hover { background: #ecf5ff; transform: translateX(2px); }
.cust-name { color: #333; }
.cust-spend { color: #999; font-weight: 600; }
.stage-empty-tip {
  text-align: center; font-size: 12px; color: #bbb; padding: 8px;
}
.stage-arrow {
  position: absolute; top: 50%; right: -14px;
  transform: translateY(-50%);
  font-size: 20px; color: #dcdfe6; z-index: 1;
  background: #fff; padding: 0 2px;
}

/* 客户详情弹窗 */
.journey-detail-dialog .el-dialog__body { padding-top: 10px; }
.detail-section { margin-bottom: 18px; }
.section-title {
  display: flex; align-items: center; gap: 6px;
  font-size: 14px; font-weight: 600; color: #333;
  margin-bottom: 10px; padding-left: 8px; border-left: 3px solid #409eff;
}
.section-title .el-icon { color: #409eff; }
.section-count {
  background: #ecf5ff; color: #409eff; font-size: 12px;
  border-radius: 10px; padding: 0 8px; font-weight: 600;
}
.info-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px 16px;
  background: #f7f9fc; border-radius: 8px; padding: 14px 16px;
}
.info-item { font-size: 13px; color: #333; display: flex; align-items: center; gap: 8px; }
.info-label { color: #999; min-width: 56px; }
.stage-bar {
  display: flex; align-items: center; flex-wrap: wrap; gap: 10px;
  background: #f7f9fc; border-radius: 8px; padding: 14px 16px;
}
.stage-day { font-size: 13px; color: #666; }
.stage-day b { color: #409eff; font-size: 16px; }
.stage-code { font-size: 12px; color: #bbb; }
.tag-list { display: inline-flex; gap: 6px; flex-wrap: wrap; }
.script-list { display: flex; flex-direction: column; gap: 10px; }
.script-card {
  border: 1px solid #ebeef5; border-radius: 8px; padding: 12px 14px;
  background: #fff;
}
.script-head { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.script-name { font-size: 14px; font-weight: 600; color: #333; }
.script-meta { display: flex; gap: 16px; font-size: 12px; color: #999; margin-bottom: 8px; }
.script-content {
  font-size: 13px; color: #555; line-height: 1.6; white-space: pre-wrap;
  background: #fafafa; border-radius: 6px; padding: 10px 12px; margin-bottom: 10px;
}
.script-actions { text-align: right; }
</style>
