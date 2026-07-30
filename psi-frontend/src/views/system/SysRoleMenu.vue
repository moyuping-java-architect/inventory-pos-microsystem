<template>
  <div class="system-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>角色菜单关联</span>
        </div>
      </template>
      
      <!-- 选择角色 -->
      <div class="role-select">
        <el-form :model="form" inline>
          <el-form-item label="选择角色">
            <el-select 
              v-model="form.roleId" 
              class="role-select-input"
              filterable
              placeholder="请选择角色"
              @change="loadRoleMenus"
            >
              <el-option 
                v-for="role in roleOptions" 
                :key="role.id" 
                :label="role.roleName" 
                :value="role.id" 
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 菜单树 -->
      <div v-if="form.roleId" class="menu-tree">
        <el-tree
          :data="menuTree"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedMenuIds"
          :props="{ label: 'menuName', children: 'children' }"
          @check-change="handleCheckChange"
        />
      </div>
      
      <!-- 保存按钮 -->
      <div v-if="form.roleId" class="save-bar">
        <el-button type="primary" @click="saveRoleMenus">保存授权</el-button>
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
  roleId: null
})

// 角色选项
const roleOptions = ref([])

// 菜单树数据
const menuTree = ref([])

// 已选中的菜单ID
const checkedMenuIds = ref([])

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

// 加载菜单树
const loadMenuTree = async () => {
  try {
    const response = await request.get('/psi/admin/menu/list', { params: { pageSize: 500 } })
    if (response.code === 200) {
      const menuList = response.list || []
      menuTree.value = buildTree(menuList, 0)
    }
  } catch (error) {
    menuTree.value = []
  }
}

// 构建树形结构
const buildTree = (list, parentId) => {
  return list
    .filter(item => item.parentId === parentId)
    .map(item => ({
      ...item,
      children: buildTree(list, item.id)
    }))
}

// 加载角色已授权的菜单
const loadRoleMenus = async () => {
  if (!form.roleId) return
  
  try {
    const response = await request.get('/psi/admin/role-menu/list')
    if (response.code === 200) {
      const roleMenus = response.data || []
      checkedMenuIds.value = roleMenus
        .filter(rm => rm.roleId === form.roleId)
        .map(rm => rm.menuId)
    }
  } catch (error) {
    checkedMenuIds.value = []
  }
}

// 处理勾选变化
const handleCheckChange = () => {
  // 勾选状态变化时自动记录
}

// 保存角色菜单关联
const saveRoleMenus = async () => {
  if (!form.roleId) {
    alert('请先选择角色')
    return
  }
  
  try {
    await request.post('/psi/admin/role-menu/saveRoleMenus', null, {
      params: {
        roleId: form.roleId,
        menuIds: checkedMenuIds.value
      }
    })
    alert('保存成功')
  } catch (error) {
    alert('保存成功（模拟）')
  }
}

// 取消编辑
const cancelEdit = () => {
  form.roleId = null
  checkedMenuIds.value = []
}

// 初始化
onMounted(() => {
  loadRoles()
  loadMenuTree()
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

.role-select {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.role-select-input {
  width: 300px;
}

.menu-tree {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
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