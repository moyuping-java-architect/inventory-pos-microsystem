import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN.js'
import enUS from './en-US.js'

const getBrowserLanguage = () => {
  const lang = navigator.language || 'zh-CN'
  if (lang.toLowerCase().startsWith('zh')) return 'zh-CN'
  return 'en-US'
}

const savedLocale = localStorage.getItem('locale')
const defaultLocale = savedLocale || getBrowserLanguage()

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: defaultLocale,
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

export const setLocale = (locale) => {
  i18n.global.locale.value = locale
  localStorage.setItem('locale', locale)
  document.documentElement.setAttribute('lang', locale)
}

export const getLocale = () => {
  return i18n.global.locale.value
}

export default i18n