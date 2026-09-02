<template>
  <div class="system-page">
    <!-- ==================== 定价配置区 ==================== -->
    <el-card class="page-card config-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('pricing.configTitle') }}</span>
          <el-tag v-if="!isAdmin" type="info" size="small">{{ t('pricing.readonly') }}</el-tag>
        </div>
      </template>

      <el-alert
        :title="t('pricing.configTip')"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-form :model="configForm" label-width="160px" class="config-form">
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item :label="t('pricing.purchaseRate')">
              <el-input-number
                v-model="configForm.purchaseRate"
                :min="1"
                :precision="4"
                :step="0.5"
                :disabled="!isAdmin || saving"
                controls-position="right"
                style="width: 100%"
              />
              <div class="field-hint">{{ t('pricing.purchaseRateHint') }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item :label="t('pricing.salesRate')">
              <el-input-number
                v-model="configForm.salesRate"
                :min="1"
                :precision="4"
                :step="0.5"
                :disabled="!isAdmin || saving"
                controls-position="right"
                style="width: 100%"
              />
              <div class="field-hint">{{ t('pricing.salesRateHint') }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item :label="t('pricing.markupFactor')">
              <el-input-number
                v-model="configForm.markupFactor"
                :min="1"
                :max="10"
                :precision="4"
                :step="0.01"
                :disabled="!isAdmin || saving"
                controls-position="right"
                style="width: 100%"
              />
              <div class="field-hint">{{ t('pricing.markupHint') }}</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item :label="t('pricing.marketRate')">
              <el-input-number
                v-model="configForm.marketRate"
                :min="0"
                :precision="4"
                :step="0.5"
                :disabled="!isAdmin || saving"
                controls-position="right"
                style="width: 100%"
              />
              <div class="field-hint">{{ t('pricing.marketRateHint') }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item :label="t('pricing.rateSpread')">
              <el-input
                :model-value="rateSpread"
                disabled
                style="width: 100%"
              >
                <template #suffix>ZMW</template>
              </el-input>
              <div class="field-hint">{{ t('pricing.rateSpreadHint') }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item :label="t('pricing.baseCurrency')">
              <el-select v-model="configForm.baseCurrency" :disabled="!isAdmin || saving" style="width: 100%">
                <el-option label="USD" value="USD" />
                <el-option label="CNY" value="CNY" />
                <el-option label="EUR" value="EUR" />
              </el-select>
              <div class="field-hint">{{ t('pricing.baseCurrencyHint') }}</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="isAdmin">
          <el-button type="primary" @click="handleSaveConfig" :loading="saving">
            {{ t('common.save') }}
          </el-button>
          <el-button @click="loadConfig">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ==================== 价格试算器 ==================== -->
    <el-card v-if="isAdmin" class="page-card calc-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('pricing.calculatorTitle') }}</span>
          <el-radio-group v-model="calcMode" size="small">
            <el-radio-button label="zmw">ZMW {{ t('pricing.purchase') }}</el-radio-button>
            <el-radio-button label="usd">USD {{ t('pricing.purchase') }}</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-row :gutter="24">
        <!-- 输入区 -->
        <el-col :span="8">
          <div class="calc-input-section">
            <div class="calc-input-label">
              {{ calcMode === 'zmw' ? 'ZMW ' + t('pricing.purchasePrice') : 'USD ' + t('pricing.purchasePrice') }}
            </div>
            <el-input-number
              v-model="calcInput"
              :min="0"
              :precision="2"
              :step="100"
              controls-position="right"
              style="width: 100%; margin-bottom: 12px"
              :placeholder="t('pricing.inputPlaceholder')"
            />
            <el-button type="primary" @click="handleCalc" :loading="calculating" style="width: 100%">
              {{ t('pricing.calculate') }}
            </el-button>
          </div>
        </el-col>

        <!-- 结果区 -->
        <el-col :span="16">
          <div v-if="calcResult" class="calc-result">
            <div class="price-chain">
              <div class="chain-step" v-if="calcMode === 'zmw'">
                <div class="step-label">{{ t('pricing.step1') }}</div>
                <div class="step-value">ZMW {{ formatNumber(calcInput) }} &divide; {{ configForm.purchaseRate }} = <strong>USD {{ formatNumber(calcResult.usdCostPrice) }}</strong></div>
              </div>
              <div class="chain-step">
                <div class="step-label">{{ t('pricing.step2') }}</div>
                <div class="step-value">USD {{ formatNumber(calcMode === 'zmw' ? calcResult.usdCostPrice : calcInput) }} &times; {{ configForm.markupFactor }} = <strong>USD {{ formatNumber(calcResult.usdSalePrice) }}</strong></div>
              </div>
              <div class="chain-step">
                <div class="step-label">{{ t('pricing.step3') }}</div>
                <div class="step-value">USD {{ formatNumber(calcResult.usdSalePrice) }} &times; {{ configForm.salesRate }} = <strong class="final-price">ZMW {{ formatNumber(calcResult.zmwRetailPrice) }}</strong></div>
              </div>
            </div>

            <el-divider />

            <el-row :gutter="16">
              <el-col :span="6">
                <div class="metric-box">
                  <div class="metric-label">{{ t('pricing.zmwRetail') }}</div>
                  <div class="metric-value">ZMW {{ formatNumber(calcResult.zmwRetailPrice) }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="metric-box">
                  <div class="metric-label">{{ t('pricing.usdSale') }}</div>
                  <div class="metric-value">USD {{ formatNumber(calcResult.usdSalePrice) }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="metric-box">
                  <div class="metric-label">{{ t('pricing.grossProfit') }}</div>
                  <div class="metric-value">ZMW {{ formatNumber(calcResult.grossProfit) }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="metric-box">
                  <div class="metric-label">{{ t('pricing.grossMargin') }}</div>
                  <div class="metric-value">{{ formatNumber(calcResult.grossMargin) }}%</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <el-empty v-else :description="t('pricing.calcEmpty')" />
        </el-col>
      </el-row>
    </el-card>

    <!-- ==================== 利润分析 ==================== -->
    <el-card v-if="isAdmin" class="page-card profit-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('pricing.profitAnalysis') }}</span>
        </div>
      </template>

      <el-row :gutter="24">
        <el-col :span="6">
          <div class="profit-metric">
            <div class="profit-label">{{ t('pricing.marketRate') }}</div>
            <div class="profit-value">{{ formatNumber(configForm.marketRate) }}</div>
            <div class="profit-hint">{{ t('pricing.actualRate') }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="profit-metric">
            <div class="profit-label">{{ t('pricing.purchaseRate') }}</div>
            <div class="profit-value">{{ formatNumber(configForm.purchaseRate) }}</div>
            <div class="profit-hint">{{ t('pricing.purchaseRateDiff') }}: {{ formatNumber(configForm.purchaseRate - configForm.marketRate) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="profit-metric">
            <div class="profit-label">{{ t('pricing.salesRate') }}</div>
            <div class="profit-value">{{ formatNumber(configForm.salesRate) }}</div>
            <div class="profit-hint">{{ t('pricing.salesRateDiff') }}: {{ formatNumber(configForm.salesRate - configForm.marketRate) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="profit-metric highlight">
            <div class="profit-label">{{ t('pricing.totalSpread') }}</div>
            <div class="profit-value">{{ formatNumber(rateSpread) }}</div>
            <div class="profit-hint">{{ t('pricing.perUsdProfit') }}</div>
          </div>
        </el-col>
      </el-row>

      <el-alert
        :title="profitTip"
        type="warning"
        :closable="false"
        show-icon
        style="margin-top: 16px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { pricingApi } from '../../api/index.js'
import { isAdmin } from '../../utils/permission'

const { t } = useI18n()

// ===== 权限 =====
const canEdit = computed(() => isAdmin())

// ===== 定价配置表单 =====
const configForm = reactive({
  purchaseRate: 26.0,
  salesRate: 28.0,
  markupFactor: 1.26,
  marketRate: 27.0,
  baseCurrency: 'USD'
})
const saving = ref(false)

// 汇率差（销售汇率 - 采购汇率）
const rateSpread = computed(() => {
  const v = (configForm.salesRate - configForm.purchaseRate).toFixed(4)
  return parseFloat(v)
})

// 利润提示
const profitTip = computed(() => {
  const spread = rateSpread.value
  if (spread > 3.5) return t('pricing.spreadWarning')
  if (spread > 2.0) return t('pricing.spreadModerate')
  return t('pricing.spreadSafe')
})

// ===== 加载配置 =====
const loadConfig = async () => {
  try {
    const res = await pricingApi.getConfig()
    if (res.code === 200 && res.data) {
      Object.assign(configForm, {
        purchaseRate: res.data.purchaseRate || 26.0,
        salesRate: res.data.salesRate || 28.0,
        markupFactor: res.data.markupFactor || 1.26,
        marketRate: res.data.marketRate || 27.0,
        baseCurrency: res.data.baseCurrency || 'USD'
      })
    }
  } catch (e) {
    // 后端未启动时用默认值
    console.log('Load config fallback to defaults')
  }
}

// ===== 保存配置 =====
const handleSaveConfig = async () => {
  if (configForm.purchaseRate >= configForm.salesRate) {
    ElMessage.error(t('pricing.rateError'))
    return
  }
  saving.value = true
  try {
    await pricingApi.saveConfig({ ...configForm })
    ElMessage.success(t('common.success'))
  } catch (e) {
    ElMessage.error(t('common.failed'))
  } finally {
    saving.value = false
  }
}

// ===== 价格试算器 =====
const calcMode = ref('zmw')
const calcInput = ref(2700)
const calcResult = ref(null)
const calculating = ref(false)

const handleCalc = async () => {
  if (!calcInput.value || calcInput.value <= 0) {
    ElMessage.warning(t('pricing.inputPlaceholder'))
    return
  }
  calculating.value = true
  try {
    const res =
      calcMode.value === 'zmw'
        ? await pricingApi.calcFromZmw(calcInput.value)
        : await pricingApi.calcFromUsd(calcInput.value)
    if (res.code === 200 && res.data) {
      calcResult.value = res.data
    } else {
      // 后端不可用时本地计算
      localCalc()
    }
  } catch (e) {
    localCalc()
  } finally {
    calculating.value = false
  }
}

// 本地计算（后端不可用时的降级方案）
const localCalc = () => {
  let usdCost
  if (calcMode.value === 'zmw') {
    usdCost = calcInput.value / configForm.purchaseRate
  } else {
    usdCost = calcInput.value
  }
  const usdSale = usdCost * configForm.markupFactor
  const zmwRetail = usdSale * configForm.salesRate
  const zmwCost = calcMode.value === 'zmw' ? calcInput.value : usdCost * configForm.marketRate
  const grossProfit = zmwRetail - zmwCost
  const grossMargin = zmwRetail > 0 ? (grossProfit / zmwRetail * 100) : 0

  calcResult.value = {
    usdCostPrice: usdCost,
    usdSalePrice: usdSale,
    zmwRetailPrice: zmwRetail,
    zmwCostPrice: zmwCost,
    grossProfit: grossProfit,
    grossMargin: grossMargin
  }
}

// ===== 工具方法 =====
const formatNumber = (val) => {
  if (val === null || val === undefined) return '0.00'
  return parseFloat(val).toFixed(2)
}

// ===== 初始化 =====
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.system-page {
  padding: 20px;
}

.page-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-form {
  max-width: 1000px;
}

.field-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
}

/* 试算器 */
.calc-card :deep(.el-card__body) {
  padding: 24px;
}

.calc-input-section {
  text-align: center;
}

.calc-input-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.calc-result {
  padding-left: 20px;
}

.price-chain {
  margin-bottom: 20px;
}

.chain-step {
  display: flex;
  align-items: baseline;
  margin-bottom: 16px;
  gap: 16px;
}

.step-label {
  width: 120px;
  font-size: 13px;
  color: #999;
  flex-shrink: 0;
}

.step-value {
  font-size: 15px;
  color: #333;
}

.step-value strong {
  color: #19aa8d;
  font-size: 16px;
}

.final-price {
  color: #f56c6c !important;
  font-size: 18px !important;
}

.metric-box {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.metric-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.metric-value {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

/* 利润分析 */
.profit-metric {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.profit-metric.highlight {
  background: #fdf6ec;
  border: 1px solid #faecd8;
}

.profit-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.profit-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.highlight .profit-value {
  color: #e6a23c;
}

.profit-hint {
  font-size: 11px;
  color: #bbb;
  margin-top: 6px;
}

:deep(.el-table .cell) {
  padding: 4px 8px;
}
</style>
