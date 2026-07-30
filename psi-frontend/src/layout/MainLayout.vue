<template>
  <div class="main-layout">
    <el-container>
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h2>{{ t('login.title') }}</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          mode="vertical"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>{{ t('menu.dashboard') }}</span>
          </el-menu-item>

          <el-sub-menu index="/sale">
            <template #title>
              <el-icon><Shop /></el-icon>
              <span>{{ t('menu.sale') }}</span>
            </template>
            <el-menu-item index="/sale/cashier">{{ t('menu.cashier') }}</el-menu-item>
            <el-menu-item index="/sale/order">{{ t('menu.saleOrder') }}</el-menu-item>
            <el-menu-item index="/sale/out">{{ t('menu.saleOut') }}</el-menu-item>
            <el-menu-item index="/sale/self-use-out">{{ t('menu.saleSelfUseOut') }}</el-menu-item>
            <el-menu-item index="/sale/return">{{ t('menu.saleReturn') }}</el-menu-item>
            <el-menu-item index="/sale/customer">{{ t('menu.customer') }}</el-menu-item>
            <el-menu-item index="/sale/debt">{{ t('menu.debt') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/purchase">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>{{ t('menu.purchase') }}</span>
            </template>
            <el-menu-item index="/purchase/order">{{ t('menu.purchaseOrder') }}</el-menu-item>
            <el-menu-item index="/purchase/in">{{ t('menu.purchaseIn') }}</el-menu-item>
            <el-menu-item index="/purchase/return">{{ t('menu.purchaseReturn') }}</el-menu-item>
            <el-menu-item index="/purchase/supplier">{{ t('menu.supplier') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/stock">
            <template #title>
              <el-icon><ShoppingTrolley /></el-icon>
              <span>{{ t('menu.stock') }}</span>
            </template>
            <el-menu-item index="/stock/check">{{ t('menu.stockCheck') }}</el-menu-item>
            <el-menu-item index="/stock/loss">{{ t('menu.stockLoss') }}</el-menu-item>
            <el-menu-item index="/stock/over">{{ t('menu.stockOver') }}</el-menu-item>
            <el-menu-item index="/stock/transfer">{{ t('menu.stockTransfer') }}</el-menu-item>
            <el-menu-item index="/stock/inventory">{{ t('menu.inventory') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/goods">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>{{ t('menu.goods') }}</span>
            </template>
            <el-menu-item index="/goods/list">{{ t('menu.goodsList') }}</el-menu-item>
            <el-menu-item index="/goods/category">{{ t('menu.goodsCategory') }}</el-menu-item>
            <el-menu-item index="/goods/brand">{{ t('menu.goodsBrand') }}</el-menu-item>
            <el-menu-item index="/goods/unit">{{ t('menu.goodsUnit') }}</el-menu-item>
            <el-menu-item index="/goods/sku">{{ t('menu.goodsSku') }}</el-menu-item>
            <el-menu-item index="/goods/sku-sale-unit">{{ t('menu.goodsSkuSaleUnit') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/finance">
            <template #title>
              <el-icon><Wallet /></el-icon>
              <span>{{ t('menu.finance') }}</span>
            </template>
            <el-menu-item index="/finance/receivable">{{ t('menu.receivable') }}</el-menu-item>
            <el-menu-item index="/finance/payable">{{ t('menu.payable') }}</el-menu-item>
            <el-menu-item index="/finance/account">{{ t('menu.account') }}</el-menu-item>
            <el-menu-item index="/finance/daily">{{ t('menu.dailyClose') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>{{ t('menu.system') }}</span>
            </template>
            <el-menu-item index="/system/users">{{ t('menu.sysUser') }}</el-menu-item>
            <el-menu-item index="/system/roles">{{ t('menu.sysRole') }}</el-menu-item>
            <el-menu-item index="/system/depts">{{ t('menu.sysDept') }}</el-menu-item>
            <el-menu-item index="/system/menus">{{ t('menu.sysMenu') }}</el-menu-item>
            <el-menu-item index="/system/dict-types">{{ t('menu.sysDictType') }}</el-menu-item>
            <el-menu-item index="/system/dict-data">{{ t('menu.sysDictData') }}</el-menu-item>
            <el-menu-item index="/system/tenant">{{ t('menu.sysTenant') }}</el-menu-item>
            <el-menu-item index="/system/shop">{{ t('menu.sysShop') }}</el-menu-item>
            <el-menu-item index="/system/warehouse">{{ t('menu.sysWarehouse') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/report">
            <template #title>
              <el-icon><DocumentCopy /></el-icon>
              <span>{{ t('menu.report') }}</span>
            </template>
            <el-menu-item index="/report/center">{{ t('menu.reportCenter') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/workflow">
            <template #title>
              <el-icon><CirclePlus /></el-icon>
              <span>{{ t('menu.workflow') }}</span>
            </template>
            <el-menu-item index="/workflow/todo">{{ t('menu.workflowTodo') }}</el-menu-item>
            <el-menu-item index="/workflow/start">我的发起</el-menu-item>
            <el-menu-item index="/workflow/history">{{ t('menu.workflowHistory') }}</el-menu-item>
            <el-menu-item index="/workflow/definition">流程定义</el-menu-item>
            <el-menu-item index="/workflow/monitor">流程监控</el-menu-item>
            <el-menu-item index="/workflow/setting">流程设置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-content">
            <div class="header-title">{{ currentTitle }}</div>
            <div class="header-right">
              <el-button type="primary" link @click="toggleLang">{{ currentLangText }}</el-button>
              <el-dropdown>
                <span class="el-dropdown-link">
                  <el-icon><User /></el-icon>
                  <span>{{ userName }}</span>
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleLogout">{{ t('login.logout') }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  ShoppingCart,
  Shop,
  ShoppingTrolley,
  Box,
  Wallet,
  User,
  ArrowDown,
  Setting,
  CirclePlus,
  DocumentCopy
} from '@element-plus/icons-vue'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()

const activeMenu = ref('/dashboard')
const userName = ref(localStorage.getItem('userName') || 'admin')
const currentLangText = computed(() => locale.value === 'zh-CN' ? 'English' : '中文')

const toggleLang = () => {
  const newLocale = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = newLocale
  localStorage.setItem('locale', newLocale)
}

const currentTitle = computed(() => {
  const titles = {
    '/dashboard': t('menu.dashboard'),
    '/sale/cashier': t('menu.cashier'),
    '/sale/order': t('menu.saleOrder'),
    '/sale/out': t('menu.saleOut'),
    '/sale/self-use-out': t('menu.saleSelfUseOut'),
    '/sale/return': t('menu.saleReturn'),
    '/sale/customer': t('menu.customer'),
    '/sale/debt': t('menu.receivable'),
    '/purchase/order': t('menu.purchaseOrder'),
    '/purchase/in': t('menu.purchaseIn'),
    '/purchase/return': t('menu.purchaseReturn'),
    '/purchase/supplier': t('menu.supplier'),
    '/stock/check': t('menu.stockCheck'),
    '/stock/loss': t('menu.stockLoss'),
    '/stock/over': t('menu.stockOver'),
    '/stock/transfer': t('menu.stockTransfer'),
    '/stock/inventory': t('menu.inventory'),
    '/goods/list': t('menu.goodsList'),
    '/goods/category': t('menu.goodsCategory'),
    '/goods/brand': t('menu.goodsBrand'),
    '/goods/unit': t('menu.goodsUnit'),
    '/goods/sku': t('menu.goodsSku'),
    '/goods/sku-sale-unit': t('menu.goodsSkuSaleUnit'),
    '/finance/receivable': t('menu.receivable'),
    '/finance/payable': t('menu.payable'),
    '/finance/account': t('menu.account'),
    '/finance/daily': t('menu.dailyClose'),
    '/system/users': t('menu.sysUser'),
    '/system/roles': t('menu.sysRole'),
    '/system/depts': t('menu.sysDept'),
    '/system/menus': t('menu.sysMenu'),
    '/system/dict-types': t('menu.sysDictType'),
    '/system/dict-data': t('menu.sysDictData'),
    '/system/tenant': t('menu.sysTenant'),
    '/system/shop': t('menu.sysShop'),
    '/system/warehouse': t('menu.sysWarehouse'),
    '/report/center': t('menu.reportCenter'),
    '/workflow/todo': t('menu.workflowTodo'),
            '/workflow/start': '我的发起',
            '/workflow/history': t('menu.workflowHistory'),
            '/workflow/definition': '流程定义',
            '/workflow/monitor': '流程监控',
            '/workflow/setting': '流程设置'
  }
  return titles[route.path] || t('login.title')
})

const handleMenuSelect = (index) => {
  activeMenu.value = index
  router.push(index)
}

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}

watch(
  () => route.path,
  (newPath) => {
    activeMenu.value = newPath
  }
)

onMounted(() => {
  activeMenu.value = route.path
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
}

.sidebar {
  background-color: #2f4050;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #1f2d3d;
}

.logo h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: #a7b1c2;
  height: 45px;
  line-height: 45px;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  color: #fff;
  background-color: #1f2d3d;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background-color: #19aa8d;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right :deep(.el-dropdown-link) {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #666;
}

.main-content {
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>