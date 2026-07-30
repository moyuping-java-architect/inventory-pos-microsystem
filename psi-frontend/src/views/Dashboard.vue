<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon purchase">
          <el-icon><ShoppingCart /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.purchaseAmount }}</div>
          <div class="stat-label">{{ t('dashboard.todayPurchase') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon sale">
          <el-icon><Shop /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.saleAmount }}</div>
          <div class="stat-label">{{ t('dashboard.todaySale') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stock">
          <el-icon><ShoppingTrolley /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.stockCount }}</div>
          <div class="stat-label">{{ t('dashboard.stockItems') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon profit">
          <el-icon><Wallet /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.profit }}</div>
          <div class="stat-label">{{ t('dashboard.monthProfit') }}</div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <h3>{{ t('dashboard.saleTrend') }}</h3>
        </div>
        <div class="chart-content">
          <el-table :data="saleTrend" border>
            <el-table-column prop="date" :label="t('dashboard.date')" />
            <el-table-column prop="amount" :label="t('dashboard.amount')" />
            <el-table-column prop="count" :label="t('dashboard.orderCount')" />
          </el-table>
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>{{ t('dashboard.stockAlert') }}</h3>
        </div>
        <div class="chart-content">
          <el-table :data="stockAlert" border>
            <el-table-column prop="name" :label="t('dashboard.productName')" />
            <el-table-column prop="code" :label="t('dashboard.productCode')" />
            <el-table-column prop="stock" :label="t('dashboard.currentStock')" />
            <el-table-column prop="minStock" :label="t('dashboard.minStock')" />
          </el-table>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card full-width">
        <div class="chart-header">
          <h3>{{ t('dashboard.recentOrders') }}</h3>
        </div>
        <div class="chart-content">
          <el-table :data="recentOrders" border>
            <el-table-column prop="orderNo" :label="t('dashboard.orderNo')" />
            <el-table-column prop="type" :label="t('dashboard.orderType')">
              <template #default="scope">
                <el-tag :type="scope.row.type === '采购' || scope.row.type === 'Purchase' ? 'primary' : 'success'">
                  {{ t('dashboard.' + scope.row.type) || scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="customer" :label="t('dashboard.customer')" />
            <el-table-column prop="amount" :label="t('dashboard.amount')" />
            <el-table-column prop="status" :label="t('dashboard.status')">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ t('dashboard.statusMap.' + statusKey(scope.row.status)) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" :label="t('dashboard.createTime')" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ShoppingCart, Shop, ShoppingTrolley, Wallet } from '@element-plus/icons-vue'
import request from '@/utils/request'

const { t } = useI18n()

const stats = ref({
  purchaseAmount: '¥ 0.00',
  saleAmount: '¥ 0.00',
  stockCount: '0',
  profit: '¥ 0.00'
})

const saleTrend = ref([])
const stockAlert = ref([])
const recentOrders = ref([])

const formatMoney = (value) => {
  const num = Number(value || 0)
  return '¥ ' + num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatNumber = (value) => {
  return Number(value || 0).toLocaleString('zh-CN')
}

const loadDashboard = async () => {
  try {
    const res = await request.get('/psi/admin/dashboard/summary')
    const data = res.data || {}
    stats.value = {
      purchaseAmount: formatMoney(data.todayPurchaseAmount),
      saleAmount: formatMoney(data.todaySaleAmount),
      stockCount: formatNumber(data.stockSkuCount),
      profit: formatMoney(data.monthProfit)
    }
    saleTrend.value = (data.saleTrend || []).map(item => ({
      date: item.date,
      amount: formatMoney(item.amount),
      count: item.orderCount || 0
    }))
    stockAlert.value = (data.stockAlert || []).map(item => ({
      name: item.name,
      code: item.code,
      stock: Number(item.stock || 0),
      minStock: Number(item.minStock || 0)
    }))
    recentOrders.value = (data.recentOrders || []).map(item => ({
      orderNo: item.orderNo,
      type: item.type,
      customer: item.customer,
      amount: formatMoney(item.amount),
      status: item.status,
      createTime: item.createTime
    }))
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

const statusKeyMap = {
    '已完成': 'completed', '已入库': 'inStock', '已出库': 'outStock',
    '待出库': 'pendingOut', '待入库': 'pendingIn', '待审核': 'pending', '已取消': 'cancelled',
    'Completed': 'completed', 'In Stock': 'inStock', 'Out of Stock': 'outStock',
    'Pending Out': 'pendingOut', 'Pending In': 'pendingIn', 'Pending': 'pending', 'Cancelled': 'cancelled'
  }
  
  const statusKey = (status) => statusKeyMap[status] || 'pending'
  
  const getStatusType = (status) => {
    const types = {
      '已完成': 'success', '已入库': 'success', '已出库': 'success',
      '待出库': 'warning', '待入库': 'warning', '待审核': 'info', '已取消': 'danger',
      'Completed': 'success', 'In Stock': 'success', 'Out of Stock': 'success',
      'Pending Out': 'warning', 'Pending In': 'warning', 'Pending': 'info', 'Cancelled': 'danger'
    }
    return types[status] || 'info'
  }

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.dashboard { padding: 20px; }
.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 20px;
}
.stat-card {
  display: flex; align-items: center; background: #fff; padding: 20px;
  border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.stat-icon {
  width: 50px; height: 50px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; margin-right: 16px;
  font-size: 24px; color: #fff;
}
.stat-icon.purchase { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.sale { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.stock { background: linear-gradient(135deg, #fc4a1a 0%, #f7b733 100%); }
.stat-icon.profit { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: 600; color: #333; }
.stat-label { font-size: 14px; color: #999; margin-top: 4px; }
.charts-row {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-bottom: 20px;
}
.chart-card {
  background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}
.chart-card.full-width { grid-column: span 2; }
.chart-header { padding: 16px 20px; border-bottom: 1px solid #eee; }
.chart-header h3 { margin: 0; font-size: 16px; font-weight: 600; color: #333; }
.chart-content { padding: 16px 20px; }
</style>