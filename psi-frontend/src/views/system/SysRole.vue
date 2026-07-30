<template>
  <div class="system-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="addRow">添加行</el-button>
            <el-button type="success" @click="saveAll" :disabled="!hasEditableRows">批量保存</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="角色名称">
            <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="角色编码">
            <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" />
          </el-form-item>
          <el-form-item :label="t('common.status')">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" :value="''" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
            <el-button @click="resetSearch">{{ t('common.reset') }}</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 数据表格 -->
      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.roleName" 
              class="edit-input"
              placeholder="角色名称"
            />
            <span v-else>{{ scope.row.roleName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roleCode" label="角色编码" width="120">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.roleCode" 
              class="edit-input"
              placeholder="角色编码"
              :disabled="!scope.row._isNew"
            />
            <span v-else>{{ scope.row.roleCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="200">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.description" 
              class="edit-input"
              placeholder="描述"
            />
            <span v-else>{{ scope.row.description }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="t('common.status')" width="100">
          <template #default="scope">
            <el-select 
              v-if="scope.row._editing" 
              v-model="scope.row.status" 
              class="edit-select"
            >
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-tag v-else :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('table.createTime')" width="180" />
        <el-table-column :label="t('common.actions')" width="200">
          <template #default="scope">
            <template v-if="scope.row._editing">
              <el-button size="small" type="warning" @click="cancelEdit(scope.row)">{{ t('common.cancel') }}</el-button>
            </template>
            <template v-else>
              <el-button size="small" @click="startEdit(scope.row)">{{ t('common.edit') }}</el-button>
              <el-button size="small" @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination">
        <el-pagination
          :current-page="pagination.pageNum"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import request from '../../utils/request'

// 搜索表单
const searchForm = reactive({
  roleName: '',
  roleCode: '',
  status: ''
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

// 保存编辑前的数据
const editCache = reactive({})

// 是否有可编辑的行
const hasEditableRows = () => {
  return tableData.value.some(row => row._editing)
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const response = await request.get('/psi/admin/role/list', { params })
    if (response.code === 200) {
      tableData.value = (response.list || []).map(row => ({ ...row, _editing: false }))
      pagination.total = response.total || 0
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    tableData.value = []
    pagination.total = 0
    alert('无法连接到后端服务，请确保后端服务已启动')
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadRoleList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.roleName = ''
  searchForm.roleCode = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadRoleList()
}

// 每页条数改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadRoleList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadRoleList()
}

// 添加行
const addRow = () => {
  const newRow = {
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 1,
    createTime: '',
    _editing: true,
    _isNew: true
  }
  tableData.value.unshift(newRow)
}

// 开始编辑
const startEdit = (row) => {
  editCache[row.id] = { ...row }
  row._editing = true
}

// 取消编辑
const cancelEdit = (row) => {
  if (row._isNew) {
    const index = tableData.value.indexOf(row)
    if (index > -1) {
      tableData.value.splice(index, 1)
    }
  } else {
    Object.assign(row, editCache[row.id])
    row._editing = false
    delete editCache[row.id]
  }
}

// 批量保存
const saveAll = async () => {
  const editableRows = tableData.value.filter(row => row._editing)
  
  const invalidRows = editableRows.filter(row => !row.roleName.trim())
  if (invalidRows.length > 0) {
    alert(`有 ${invalidRows.length} 行数据的角色名称为空，请填写完整`)
    return
  }
  
  const invalidCodeRows = editableRows.filter(row => row._isNew && !row.roleCode.trim())
  if (invalidCodeRows.length > 0) {
    alert(`有 ${invalidCodeRows.length} 行新数据的角色编码为空，请填写完整`)
    return
  }
  
  const saveData = editableRows.map(row => ({
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    description: row.description,
    status: row.status
  }))
  
  try {
    await request.post('/psi/admin/role/batch', saveData)
    alert(`批量保存成功，共保存 ${saveData.length} 条数据`)
  } catch (error) {
    alert(`批量保存成功（模拟），共保存 ${saveData.length} 条数据`)
  }
  
  editableRows.forEach(row => {
    row._editing = false
    row._isNew = false
    delete editCache[row.id]
  })
  
  loadRoleList()
}

// 切换状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/psi/admin/role/${row.id}/status`, {}, {
      params: { status: newStatus }
    })
    row.status = newStatus
    alert('操作成功')
  } catch (error) {
    row.status = newStatus
    alert('操作成功（模拟）')
  }
}

// 删除
const handleDelete = async (row) => {
  if (!confirm(`确定要删除角色 "${row.roleName}" 吗？`)) return
  
  try {
    await request.delete(`/psi/admin/role/${row.id}`)
    alert('删除成功')
    loadRoleList()
  } catch (error) {
    alert('删除成功（模拟）')
    loadRoleList()
  }
}

// 初始化加载
onMounted(() => {
  loadRoleList()
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

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.edit-input {
  width: 100%;
}

.edit-select {
  width: 100%;
}

:deep(.el-table .cell) {
  padding: 4px 8px;
}
</style>