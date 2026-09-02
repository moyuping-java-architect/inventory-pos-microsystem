<template>
  <div class="redirect-page" v-loading="true" :element-loading-text="t('workflow.loading')" />
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { workflowApi } from '../../api'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

const BIZ_TYPE_ROUTES = {
  'PURCHASE_ORDER': 'ApprovalPurchaseOrder',
  'PURCHASE_IN': 'ApprovalPurchaseIn',
  'PURCHASE_RETURN': 'ApprovalPurchaseReturn',
  'SALE_ORDER': 'ApprovalSaleOrder',
  'SALE_OUT': 'ApprovalSaleOut',
  'SALE_RETURN': 'ApprovalSaleReturn',
  'STOCK_CHECK': 'ApprovalStockCheck',
  'STOCK_TRANSFER': 'ApprovalStockTransfer',
  'STOCK_OVERFLOW': 'ApprovalStockOverflow',
  'STOCK_LOSS': 'ApprovalStockLoss'
}

onMounted(async () => {
  const instanceId = route.params.id
  try {
    const response = await workflowApi.getApprovalDetail(instanceId)
    if (response.code === 200 && response.data) {
      const bizType = response.data.bizType
      const routeName = BIZ_TYPE_ROUTES[bizType]
      if (routeName) {
        router.replace({ name: routeName, params: { id: instanceId } })
      }
    }
  } catch (e) {
    console.error('加载审批详情失败:', e)
  }
})
</script>

<style scoped>
.redirect-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}
</style>