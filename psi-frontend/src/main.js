import { createApp, watch } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import App from './App.vue'
import router from './router'
import i18n from './i18n'

const app = createApp(App)

const getElementPlusLocale = (locale) => (locale === 'en-US' ? en : zhCn)

app.use(ElementPlus, { locale: getElementPlusLocale(i18n.global.locale.value) })
app.use(router)
app.use(i18n)

watch(
  () => i18n.global.locale.value,
  (newLocale) => {
    ElementPlus.locale(getElementPlusLocale(newLocale))
  }
)

app.mount('#app')