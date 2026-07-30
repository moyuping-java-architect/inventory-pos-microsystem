<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon class="logo-icon"><ShoppingCart /></el-icon>
          <h1>{{ t('login.title') }}</h1>
        </div>
        <p class="subtitle">{{ t('login.subtitle') }}</p>
      </div>

      <el-form :model="loginForm" ref="loginFormRef" class="login-form">
        <el-form-item :label="t('login.username')" prop="username" label-width="80px">
          <el-input
            v-model="loginForm.username"
            :placeholder="t('login.placeholder.username')"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item :label="t('login.password')" prop="password" label-width="80px">
          <el-input
            v-model="loginForm.password"
            type="password"
            :placeholder="t('login.placeholder.password')"
            prefix-icon="Lock"
            :show-password="showPassword"
            @blur="validatePassword"
          />
        </el-form-item>

        <el-form-item>
          <div class="form-footer">
            <el-checkbox v-model="rememberMe">{{ t('login.rememberMe') }}</el-checkbox>
            <span class="lang-switch">
              <el-button type="primary" link @click="toggleLang">{{ currentLangText }}</el-button>
            </span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            @click="handleLogin"
            :loading="loading"
          >
            {{ t('login.login') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>{{ t('login.copyright') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ShoppingCart } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const { t, locale } = useI18n()
const router = useRouter()

const currentLangText = computed(() => locale.value === 'zh-CN' ? 'English' : '中文')
const toggleLang = () => {
  const newLocale = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = newLocale
  localStorage.setItem('locale', newLocale)
}

const loginForm = reactive({
  username: '',
  password: ''
})

const rememberMe = ref(false)
const showPassword = ref(false)
const loading = ref(false)
const validatePassword = () => {}

const handleLogin = async () => {
  loading.value = true
  try {
    const response = await axios.post('/psi/admin/login', loginForm)
    const authHeader = response.headers['authorization'] || response.headers['Authorization']

    let token = null
    if (authHeader && authHeader.startsWith('Bearer ')) {
      token = authHeader.substring(7)
    } else if (response.data.data && response.data.data.token) {
      token = response.data.data.token
    }

    if (token) {
      localStorage.setItem('token', token)

      let userInfo = null
      if (response.data.data?.userInfo) {
        userInfo = response.data.data.userInfo
      } else if (response.data.userInfo) {
        userInfo = response.data.userInfo
      } else if (response.data.data) {
        userInfo = response.data.data
      }

      const tenantId = userInfo?.tenantId || '1'
      const shopId = userInfo?.shopId || '1'
      const warehouseId = userInfo?.warehouseId || '1'
      const userId = userInfo?.id || userInfo?.userId || '1'
      const userName = userInfo?.nickname || userInfo?.username || 'admin'
      const roleId = userInfo?.roleId || '1'
      const roleName = userInfo?.roleName || (locale.value === 'zh-CN' ? '管理员' : 'Administrator')
      const permissions = userInfo?.permissions || '*'

      localStorage.setItem('tenantId', String(tenantId))
      localStorage.setItem('shopId', String(shopId))
      localStorage.setItem('warehouseId', String(warehouseId))
      localStorage.setItem('userId', String(userId))
      localStorage.setItem('userName', userName)
      localStorage.setItem('roleId', String(roleId))
      localStorage.setItem('roleName', roleName)
      localStorage.setItem('permissions', permissions)

      router.push('/dashboard')
    } else {
      alert(t('login.loginFailed'))
    }
  } catch (error) {
    console.error('Login failed:', error)
    alert(t('login.loginFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.logo-icon {
  font-size: 32px;
  color: #667eea;
}

.logo h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.subtitle {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lang-switch {
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
}

.login-footer {
  text-align: center;
}

.login-footer p {
  margin: 0;
  color: #999;
  font-size: 12px;
}
</style>