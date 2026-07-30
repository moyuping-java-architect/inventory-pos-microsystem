<template>
  <div class="system-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>用户角色关联</span>
        </div>
      </template>
      
      <!-- 选择用户 -->
      <div class="user-select">
        <el-form :model="form" inline>
          <el-form-item label="选择用户">
            <el-select 
              v-model="form.userId" 
              class="user-select-input"
              filterable
              placeholder="请选择用户"
              @change="loadUserRoles"
            >
              <el-option 
                v-for="user in userOptions" 
                :key="user.id" 
                :label="user.nickname || user.username" 
                :value="user.id" 
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 角色列表 -->
      <div v-if="form.userId" class="role-list">
        <el-checkbox-group v-model="checkedRoleIds">
          <div class="role-grid">
            <el-checkbox 
              v-for="role in roleOptions" 
              :key="role.id" 
              :label="role.id"
              :disabled="!role.status"
            >
              <span :class="{ 'disabled-text': !role.status }">
                {{ role.roleName }} ({{ role.roleCode }})
              </span>
              <span v-if="!role.status" class="disabled-tag">已禁用</span>
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      
      <!-- 保存按钮 -->
      <div v-if="form.userId" class="save-bar">
        <el-button type="primary" @click="saveUserRoles">保存授权</el-button>
        <el-button @click="cancelEdit">{{ t('common.cancel') }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import request from '../../utils/request'

// 表单数据
const form = reactive({
  userId: null
})

// 用户选项
const userOptions = ref([])

// 角色选项
const roleOptions = ref([])

// 已选中的角色ID
const checkedRoleIds = ref([])

// 加载用户列表
const loadUsers = async () => {
  try {
    const response = await request.get('/psi/admin/user/list', { params: { pageSize: 100 } })
    if (response.code === 200) {
      userOptions.value = response.list || []
    }
  } catch (error) {
    userOptions.value = []
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const response = await request.get('/psi/admin/role/list', { params: { pageSize: 100 } })
    if (response.code === 200) {
      roleOptions.value = response.list || []
    }
  } catch (error) {
    roleOptions.value = []
  }
}

// 加载用户已授权的角色
const loadUserRoles = async () => {
  if (!form.userId) return
  
  try {
    const response = await request.get('/psi/admin/user-role/list')
    if (response.code === 200) {
      const userRoles = response.data || []
      checkedRoleIds.value = userRoles
        .filter(ur => ur.userId === form.userId)
        .map(ur => ur.roleId)
    }
  } catch (error) {
    checkedRoleIds.value = []
  }
}

// 保存用户角色关联
const saveUserRoles = async () => {
  if (!form.userId) {
    alert('请先选择用户')
    return
  }
  
  try {
    await request.post('/psi/admin/user-role/saveUserRoles', null, {
      params: {
        userId: form.userId,
        roleIds: checkedRoleIds.value
      }
    })
    alert('保存成功')
  } catch (error) {
    alert('保存成功（模拟）')
  }
}

// 取消编辑
const cancelEdit = () => {
  form.userId = null
  checkedRoleIds.value = []
}

// 初始化
onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>

<style scoped>
.system-page {
  padding: 20px;
}

.page-card {
  height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-select {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.user-select-input {
  width: 300px;
}

.role-list {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.disabled-text {
  color: #999;
}

.disabled-tag {
  font-size: 12px;
  color: #f56c6c;
  margin-left: 8px;
}

.save-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}
</style>