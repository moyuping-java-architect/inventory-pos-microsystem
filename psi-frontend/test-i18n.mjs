import { createI18n } from 'vue-i18n'
import zhCN from './src/i18n/zh-CN.js'
import enUS from './src/i18n/en-US.js'

const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'en-US',
  messages: { 'zh-CN': zhCN, 'en-US': enUS }
})

const { t, te } = i18n.global

console.log('=== Test 1: group label ===')
console.log('groups.CHURN_MODEL te():', te('journeyConfig.groups.CHURN_MODEL'))
console.log('groups.CHURN_MODEL t():', t('journeyConfig.groups.CHURN_MODEL'))

console.log('\n=== Test 2: item label ===')
const key = 'journeyConfig.items.CHURN_MODEL.active_multiplier.label'
console.log('te():', te(key))
console.log('t():', t(key))

console.log('\n=== Test 3: CHANNEL_RULE items ===')
console.log('high_value_threshold.label te():', te('journeyConfig.items.CHANNEL_RULE.high_value_threshold.label'))
console.log('high_value_threshold.label t():', t('journeyConfig.items.CHANNEL_RULE.high_value_threshold.label'))
console.log('default_channel.label te():', te('journeyConfig.items.CHANNEL_RULE.default_channel.label'))
console.log('default_channel.label t():', t('journeyConfig.items.CHANNEL_RULE.default_channel.label'))

console.log('\n=== Test 4: silent fallback method ===')
function silent(key, fallback) {
  const translated = t(key, {}, { missingWarn: false, fallbackWarn: false })
  return translated === key ? fallback : translated
}
console.log('high_value_threshold silent():', silent('journeyConfig.items.CHANNEL_RULE.high_value_threshold.label', 'High-value threshold'))
console.log('default_channel silent():', silent('journeyConfig.items.CHANNEL_RULE.default_channel.label', 'Default channel'))
