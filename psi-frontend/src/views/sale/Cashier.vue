<template>
  <div class="cashier-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="cashier-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('sale.cashier') }}</span>
              <el-tag :type="isOnline ? 'success' : 'danger'">{{ isOnline ? '在线' : '离线' }}</el-tag>
            </div>
          </template>
          <el-form :inline="true" class="search-form">
            <el-input
              v-model="productCode"
              :placeholder="t('sale.scanOrEnterProductCode')"
              @keyup.enter="addProduct"
              class="product-input"
              :disabled="!isOnline"
            />
            <el-button type="primary" @click="addProduct" :disabled="!isOnline">{{ t('sale.addProduct') }}</el-button>
          </el-form>
          <el-table :data="products" border>
            <el-table-column prop="code" :label="t('sale.productCode')" width="150" />
            <el-table-column prop="name" :label="t('sale.productName')" />
            <el-table-column prop="spec" :label="t('sale.spec')" width="100" />
            <el-table-column prop="unit" :label="t('sale.unit')" width="80" />
            <el-table-column prop="quantity" :label="t('table.quantity')" width="100">
              <template #default="scope">
                <el-input-number v-model="scope.row.quantity" :min="1" size="small" @change="recalcItem(scope.row)" />
              </template>
            </el-table-column>
            <el-table-column prop="price" :label="t('sale.unitPrice')" width="120">
              <template #default="scope">$ {{ scope.row.price.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="taxRate" label="VAT" width="80">
              <template #default="scope">{{ (scope.row.taxRate * 100).toFixed(0) }}%</template>
            </el-table-column>
            <el-table-column :label="t('table.amount')" width="120">
              <template #default="scope">$ {{ scope.row.amount.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column :label="t('common.actions')" width="80">
              <template #default="scope">
                <el-button type="danger" link @click="removeProduct(scope.$index)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="danger" class="clear-btn" @click="clearCashier">{{ t('sale.clearCashier') }}</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('sale.customerInfo') }}</span>
            </div>
          </template>
          <el-form :model="customerForm" label-width="80px">
            <el-form-item :label="t('sale.customerCode')">
              <el-input v-model="customerForm.code" />
            </el-form-item>
            <el-form-item :label="t('sale.customerName')">
              <el-input v-model="customerForm.name" />
            </el-form-item>
          </el-form>
        </el-card>
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('sale.warehouseInfo') }}</span>
            </div>
          </template>
          <el-form :model="warehouseForm" label-width="80px">
            <el-form-item :label="t('sale.warehouseCode')">
              <el-input v-model="warehouseForm.code" />
            </el-form-item>
            <el-form-item :label="t('sale.warehouseName')">
              <el-input v-model="warehouseForm.name" />
            </el-form-item>
          </el-form>
        </el-card>
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('sale.settlement') }}</span>
            </div>
          </template>
          <el-form :model="settlementForm" label-width="100px">
            <el-form-item :label="t('sale.paymentMethod')">
              <el-select v-model="settlementForm.payType" class="payment-select">
                <el-option :label="t('sale.payType.cash')" value="CASH" />
                <el-option :label="t('sale.payType.wechat')" value="WECHAT" />
                <el-option :label="t('sale.payType.alipay')" value="ALIPAY" />
                <el-option :label="t('sale.payType.bank')" value="BANK" />
                <el-option label="Airtel Money" value="AIRTEL" />
                <el-option label="MTN MoMo" value="MTN" />
                <el-option label="Zamtel Kwacha" value="ZAMTEL" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="isMobileMoney" label="手机号">
              <el-input v-model="settlementForm.phoneNumber" placeholder="2609XXXXXXXX" />
            </el-form-item>
            <el-form-item label="Subtotal">
              <el-input v-model="subtotalDisplay" disabled />
            </el-form-item>
            <el-form-item label="VAT">
              <el-input v-model="taxDisplay" disabled />
            </el-form-item>
            <el-form-item label="Total">
              <el-input v-model="totalDisplay" disabled />
            </el-form-item>
            <el-form-item :label="t('sale.paidAmount')">
              <el-input type="number" v-model="settlementForm.paidAmount" />
            </el-form-item>
            <el-form-item :label="t('sale.changeAmount')">
              <el-input v-model="changeDisplay" disabled />
            </el-form-item>
          </el-form>
          <el-button type="primary" class="confirm-btn" @click="confirmReceipt" :loading="submitting">{{ t('sale.confirmReceipt') }}</el-button>
          <el-button type="success" class="upload-btn" @click="uploadData">{{ t('sale.uploadData') }}</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { goodsApi, cashierApi } from '../../api/index.js'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const productCode = ref('')
const products = ref([])
const submitting = ref(false)
const isOnline = ref(navigator.onLine)

const customerForm = reactive({ code: '', name: '' })
const warehouseForm = reactive({ code: 'W001', name: '主仓库' })

const settlementForm = reactive({
  payType: 'CASH',
  phoneNumber: '',
  paidAmount: 0
})

const isMobileMoney = computed(() => {
  return ['AIRTEL', 'MTN', 'ZAMTEL'].includes(settlementForm.payType)
})

const subtotal = computed(() => {
  return products.value.reduce((sum, item) => sum + item.netAmount, 0)
})
const taxAmount = computed(() => {
  return products.value.reduce((sum, item) => sum + item.taxAmount, 0)
})
const totalAmount = computed(() => subtotal.value + taxAmount.value)

const subtotalDisplay = computed(() => '$ ' + subtotal.value.toFixed(2))
const taxDisplay = computed(() => '$ ' + taxAmount.value.toFixed(2))
const totalDisplay = computed(() => '$ ' + totalAmount.value.toFixed(2))
const changeDisplay = computed(() => '$ ' + Math.max(0, settlementForm.paidAmount - totalAmount.value).toFixed(2))

watch(totalAmount, (val) => {
  // 默认实收等于应收，方便现金收款
  if (settlementForm.payType === 'CASH') {
    settlementForm.paidAmount = Number(val.toFixed(2))
  }
})

watch(() => settlementForm.payType, () => {
  if (settlementForm.payType === 'CASH') {
    settlementForm.paidAmount = Number(totalAmount.value.toFixed(2))
  }
})

const recalcItem = (item) => {
  item.netAmount = item.price * item.quantity
  item.taxAmount = item.netAmount * item.taxRate
  item.amount = item.netAmount + item.taxAmount
}

const addProduct = async () => {
  if (!productCode.value) return
  try {
    const res = await goodsApi.getSkuPage({
      pageNum: 1,
      pageSize: 10,
      keyword: productCode.value
    })
    const list = res.list || (res.data && res.data.list) || []
    if (list.length === 0) {
      ElMessage.warning('未找到商品')
      return
    }
    const sku = list[0]
    const taxRate = sku.taxRate != null ? Number(sku.taxRate) : 0.16
    const price = Number(sku.salePrice || 0)
    const existing = products.value.find(p => p.code === sku.skuCode)
    if (existing) {
      existing.quantity += 1
      recalcItem(existing)
    } else {
      const quantity = 1
      const netAmount = price * quantity
      const taxAmount = netAmount * taxRate
      products.value.push({
        skuId: String(sku.id),
        code: sku.skuCode,
        name: sku.goodsName || sku.skuCode,
        spec: sku.specValues || '',
        unit: sku.saleUnit || sku.baseUnit || '件',
        quantity,
        price,
        taxRate,
        netAmount,
        taxAmount,
        amount: netAmount + taxAmount
      })
    }
    productCode.value = ''
  } catch (e) {
    console.error(e)
    ElMessage.error('查询商品失败')
  }
}

const removeProduct = (index) => { products.value.splice(index, 1) }
const clearCashier = () => {
  products.value = []
  settlementForm.paidAmount = 0
  settlementForm.phoneNumber = ''
}

const buildSaveDto = () => {
  const payIdMap = { CASH: 1, WECHAT: 2, ALIPAY: 3, BANK: 5, AIRTEL: 6, MTN: 7, ZAMTEL: 8 }
  return {
    posId: 'POS001',
    bizType: 20,
    operatorId: 1,
    operatorName: '收银员',
    totalAmount: Number(totalAmount.value.toFixed(2)),
    payAmount: Number(Number(settlementForm.paidAmount).toFixed(2)),
    netAmount: Number(subtotal.value.toFixed(2)),
    taxAmount: Number(taxAmount.value.toFixed(2)),
    changeAmount: Number(Math.max(0, settlementForm.paidAmount - totalAmount.value).toFixed(2)),
    items: products.value.map(p => ({
      skuId: p.skuId,
      goodsCode: p.code,
      goodsName: p.name,
      goodsSpec: p.spec,
      unit: p.unit,
      quantity: p.quantity,
      unitPrice: p.price,
      amount: Number(p.netAmount.toFixed(2)),
      taxRate: p.taxRate,
      batchNo: ''
    })),
    pays: [{
      payId: payIdMap[settlementForm.payType] || 1,
      payName: settlementForm.payType,
      payAmount: Number(totalAmount.value.toFixed(2)),
      payChannel: settlementForm.payType,
      phoneNumber: isMobileMoney.value ? settlementForm.phoneNumber : undefined,
      providerCode: isMobileMoney.value ? settlementForm.payType : undefined
    }]
  }
}

const confirmReceipt = async () => {
  if (products.value.length === 0) {
    ElMessage.warning('请添加商品')
    return
  }
  if (isMobileMoney.value && !settlementForm.phoneNumber) {
    ElMessage.warning('请输入 Mobile Money 手机号')
    return
  }
  submitting.value = true
  try {
    const dto = buildSaveDto()
    if (!isOnline.value) {
      saveOfflineOrder(dto)
      ElMessage.success('离线订单已缓存')
      clearCashier()
      return
    }
    await cashierApi.saveOrder(dto)
    ElMessage.success('收款成功')
    clearCashier()
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '收款失败')
  } finally {
    submitting.value = false
  }
}

const OFFLINE_KEY = 'psi_offline_orders'

const saveOfflineOrder = (dto) => {
  const list = JSON.parse(localStorage.getItem(OFFLINE_KEY) || '[]')
  dto.cashierNo = 'OFFLINE_POS001_' + Date.now()
  list.push(dto)
  localStorage.setItem(OFFLINE_KEY, JSON.stringify(list))
}

const syncOfflineOrders = async () => {
  const list = JSON.parse(localStorage.getItem(OFFLINE_KEY) || '[]')
  if (list.length === 0) return
  let remaining = []
  for (const dto of list) {
    try {
      await cashierApi.saveOrder(dto)
    } catch (e) {
      remaining.push(dto)
    }
  }
  localStorage.setItem(OFFLINE_KEY, JSON.stringify(remaining))
  if (remaining.length === 0) {
    ElMessage.success('离线订单同步完成')
  } else {
    ElMessage.warning(`同步完成，${remaining.length} 笔失败`)
  }
}

const uploadData = async () => {
  await syncOfflineOrders()
  try {
    await cashierApi.upload()
    ElMessage.success('数据上传已触发')
  } catch (e) {
    ElMessage.error('数据上传触发失败')
  }
}

const checkOnline = async () => {
  try {
    await cashierApi.health()
    isOnline.value = true
  } catch (e) {
    isOnline.value = false
  }
}

let onlineHandler, offlineHandler, healthTimer

onMounted(() => {
  checkOnline()
  onlineHandler = () => { isOnline.value = true; syncOfflineOrders() }
  offlineHandler = () => { isOnline.value = false }
  window.addEventListener('online', onlineHandler)
  window.addEventListener('offline', offlineHandler)
  healthTimer = setInterval(checkOnline, 30000)
})

onUnmounted(() => {
  window.removeEventListener('online', onlineHandler)
  window.removeEventListener('offline', offlineHandler)
  clearInterval(healthTimer)
})
</script>

<style scoped>
.cashier-container { padding: 20px; }
.card-header { font-weight: 600; font-size: 16px; display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.product-input { width: 300px; margin-right: 10px; }
.info-card { margin-top: 20px; }
.payment-select { width: 100%; }
.clear-btn { margin-top: 16px; }
.confirm-btn { width: 100%; margin-top: 16px; }
.upload-btn { width: 100%; margin-top: 10px; }
</style>
