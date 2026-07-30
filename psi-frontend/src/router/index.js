import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'MainLayout',
    component: () => import('../layout/MainLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'purchase',
        name: 'Purchase',
        redirect: '/purchase/order',
        children: [
          {
            path: 'order',
            name: 'PurchaseOrder',
            component: () => import('../views/purchase/PurchaseOrder.vue')
          },
          {
            path: 'in',
            name: 'PurchaseIn',
            component: () => import('../views/purchase/PurchaseIn.vue')
          },
          {
            path: 'return',
            name: 'PurchaseReturn',
            component: () => import('../views/purchase/PurchaseReturn.vue')
          },
          {
            path: 'supplier',
            name: 'Supplier',
            component: () => import('../views/purchase/Supplier.vue')
          }
        ]
      },
      {
            path: 'sale',
            name: 'Sale',
            redirect: '/sale/cashier',
            children: [
              {
                path: 'cashier',
                name: 'Cashier',
                component: () => import('../views/sale/Cashier.vue')
              },
              {
                path: 'order',
                name: 'SaleOrder',
                component: () => import('../views/sale/SaleOrder.vue')
              },
              {
                path: 'out',
                name: 'SaleOut',
                component: () => import('../views/sale/SaleOut.vue')
              },
              {
                path: 'self-use-out',
                name: 'SelfUseOut',
                component: () => import('../views/sale/SaleOut.vue')
              },
          {
            path: 'return',
            name: 'SaleReturn',
            component: () => import('../views/sale/SaleReturn.vue')
          },
          {
            path: 'customer',
            name: 'Customer',
            component: () => import('../views/sale/Customer.vue')
          },
          {
            path: 'debt',
            name: 'Debt',
            component: () => import('../views/sale/Debt.vue')
          }
        ]
      },
      {
        path: 'stock',
        name: 'Stock',
        redirect: '/stock/check',
        children: [
          {
            path: 'check',
            name: 'StockCheck',
            component: () => import('../views/stock/StockCheck.vue')
          },
          {
            path: 'loss',
            name: 'StockLoss',
            component: () => import('../views/stock/StockLoss.vue')
          },
          {
            path: 'over',
            name: 'StockOver',
            component: () => import('../views/stock/StockOver.vue')
          },
          {
            path: 'transfer',
            name: 'StockTransfer',
            component: () => import('../views/stock/StockTransfer.vue')
          },
          {
            path: 'inventory',
            name: 'Inventory',
            component: () => import('../views/stock/Inventory.vue')
          },
          {
            path: 'init',
            name: 'InventoryInit',
            component: () => import('../views/stock/InventoryInit.vue')
          }
        ]
      },
      {
        path: 'goods',
        name: 'Goods',
        redirect: '/goods/list',
        children: [
          {
            path: 'list',
            name: 'GoodsList',
            component: () => import('../views/goods/GoodsList.vue')
          },
          {
            path: 'category',
            name: 'GoodsCategory',
            component: () => import('../views/goods/GoodsCategory.vue')
          },
          {
            path: 'brand',
            name: 'GoodsBrand',
            component: () => import('../views/goods/GoodsBrand.vue')
          },
          {
            path: 'unit',
            name: 'GoodsUnit',
            component: () => import('../views/goods/GoodsUnit.vue')
          },
          {
            path: 'sku',
            name: 'GoodsSku',
            component: () => import('../views/goods/GoodsSku.vue')
          },
          {
            path: 'sku-sale-unit',
            name: 'GoodsSkuSaleUnit',
            component: () => import('../views/goods/GoodsSkuSaleUnit.vue')
          },
          {
            path: 'adjust-price',
            name: 'AdjustPrice',
            component: () => import('../views/goods/AdjustPrice.vue')
          }
        ]
      },
      {
        path: 'finance',
        name: 'Finance',
        redirect: '/finance/receivable',
        children: [
          {
            path: 'receivable',
            name: 'Receivable',
            component: () => import('../views/finance/Receivable.vue')
          },
          {
            path: 'payable',
            name: 'Payable',
            component: () => import('../views/finance/Payable.vue')
          },
          {
            path: 'account',
            name: 'Account',
            component: () => import('../views/finance/Account.vue')
          },
          {
            path: 'daily',
            name: 'DailyClose',
            component: () => import('../views/finance/DailyClose.vue')
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/users',
        children: [
          {
            path: 'users',
            name: 'SysUser',
            component: () => import('../views/system/SysUser.vue')
          },
          {
            path: 'roles',
            name: 'SysRole',
            component: () => import('../views/system/SysRole.vue')
          },
          {
            path: 'depts',
            name: 'SysDept',
            component: () => import('../views/system/SysDept.vue')
          },
          {
            path: 'menus',
            name: 'SysMenu',
            component: () => import('../views/system/SysMenu.vue')
          },
          {
            path: 'dict-types',
            name: 'SysDictType',
            component: () => import('../views/system/SysDictType.vue')
          },
          {
            path: 'dict-data',
            name: 'SysDictData',
            component: () => import('../views/system/SysDictData.vue')
          },
          {
            path: 'tenant',
            name: 'SysTenant',
            component: () => import('../views/system/SysTenant.vue')
          },
          {
            path: 'shop',
            name: 'SysShop',
            component: () => import('../views/system/SysShop.vue')
          },
          {
            path: 'warehouse',
            name: 'SysWarehouse',
            component: () => import('../views/system/SysWarehouse.vue')
          },
          {
            path: 'role-menu',
            name: 'SysRoleMenu',
            component: () => import('../views/system/SysRoleMenu.vue')
          },
          {
            path: 'user-role',
            name: 'SysUserRole',
            component: () => import('../views/system/SysUserRole.vue')
          }
        ]
      },
      {
        path: 'report',
        name: 'Report',
        redirect: '/report/center',
        children: [
          {
            path: 'center',
            name: 'ReportCenter',
            component: () => import('../views/report/ReportCenter.vue')
          }
        ]
      },
      {
        path: 'workflow',
        name: 'Workflow',
        redirect: '/workflow/todo',
        children: [
          {
            path: 'todo',
            name: 'WorkflowTodo',
            component: () => import('../views/workflow/WorkflowTodo.vue')
          },
          {
            path: 'approval/:id',
            name: 'WorkflowApproval',
            component: () => import('../views/workflow/WorkflowApproval.vue')
          },
          {
            path: 'approval/purchase-order/:id',
            name: 'ApprovalPurchaseOrder',
            component: () => import('../views/workflow/approval/PurchaseOrder.vue')
          },
          {
            path: 'approval/purchase-in/:id',
            name: 'ApprovalPurchaseIn',
            component: () => import('../views/workflow/approval/PurchaseIn.vue')
          },
          {
            path: 'approval/purchase-return/:id',
            name: 'ApprovalPurchaseReturn',
            component: () => import('../views/workflow/approval/PurchaseReturn.vue')
          },
          {
            path: 'approval/sale-order/:id',
            name: 'ApprovalSaleOrder',
            component: () => import('../views/workflow/approval/SaleOrder.vue')
          },
          {
            path: 'approval/sale-out/:id',
            name: 'ApprovalSaleOut',
            component: () => import('../views/workflow/approval/SaleOut.vue')
          },
          {
            path: 'approval/sale-return/:id',
            name: 'ApprovalSaleReturn',
            component: () => import('../views/workflow/approval/SaleReturn.vue')
          },
          {
            path: 'approval/stock-check/:id',
            name: 'ApprovalStockCheck',
            component: () => import('../views/workflow/approval/StockCheck.vue')
          },
          {
            path: 'approval/stock-transfer/:id',
            name: 'ApprovalStockTransfer',
            component: () => import('../views/workflow/approval/StockTransfer.vue')
          },
          {
            path: 'approval/stock-overflow/:id',
            name: 'ApprovalStockOverflow',
            component: () => import('../views/workflow/approval/StockOverflow.vue')
          },
          {
            path: 'approval/stock-loss/:id',
            name: 'ApprovalStockLoss',
            component: () => import('../views/workflow/approval/StockLoss.vue')
          },
          {
            path: 'approval/inventory-init/:id',
            name: 'ApprovalInventoryInit',
            component: () => import('../views/workflow/approval/InventoryInit.vue')
          },
          {
            path: 'approval/adjust-price/:id',
            name: 'ApprovalAdjustPrice',
            component: () => import('../views/workflow/approval/AdjustPrice.vue')
          },
          {
            path: 'start',
            name: 'WorkflowStart',
            component: () => import('../views/workflow/WorkflowStart.vue')
          },
          {
            path: 'history',
            name: 'WorkflowHistory',
            component: () => import('../views/workflow/WorkflowHistory.vue')
          },
          {
            path: 'definition',
            name: 'WorkflowDefinition',
            component: () => import('../views/workflow/WorkflowDefinition.vue')
          },
          {
            path: 'monitor',
            name: 'WorkflowMonitor',
            component: () => import('../views/workflow/WorkflowMonitor.vue')
          },
          {
            path: 'setting',
            name: 'WorkflowSetting',
            component: () => import('../views/workflow/WorkflowSetting.vue')
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router