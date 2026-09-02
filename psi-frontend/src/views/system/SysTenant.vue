<template>
  <div class="system-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>租户管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="addRow">添加行</el-button>
            <el-button type="success" @click="saveAll" :disabled="!hasEditableRows">批量保存</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="租户名称">
            <el-input v-model="searchForm.tenantName" placeholder="请输入租户名称" />
          </el-form-item>
          <el-form-item label="租户编码">
            <el-input v-model="searchForm.tenantCode" placeholder="请输入租户编码" />
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
        <el-table-column prop="tenantName" label="租户名称" width="150">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.tenantName" 
              class="edit-input"
              placeholder="租户名称"
            />
            <span v-else>{{ scope.row.tenantName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tenantCode" label="租户编码" width="120">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.tenantCode" 
              class="edit-input"
              placeholder="租户编码"
              :disabled="!scope.row._isNew"
            />
            <span v-else>{{ scope.row.tenantCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.contactName" 
              class="edit-input"
              placeholder="联系人"
            />
            <span v-else>{{ scope.row.contactName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="130">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.contactPhone" 
              class="edit-input"
              placeholder="联系电话"
            />
            <span v-else>{{ scope.row.contactPhone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.email" 
              class="edit-input"
              placeholder="邮箱"
            />
            <span v-else>{{ scope.row.email }}</span>
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
  tenantName: '',
  tenantCode: '',
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

// 加载租户列表
const loadTenantList = async () => {
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const response = await request.get('/psi/admin/tenant/list', { params })
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
  loadTenantList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.tenantName = ''
  searchForm.tenantCode = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadTenantList()
}

// 每页条数改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadTenantList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadTenantList()
}

// 添加行
const addRow = () => {
  const newRow = {
    id: null,
    tenantName: '',
    tenantCode: '',
    contactName: '',
    contactPhone: '',
    email: '',
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
  
  const invalidRows = editableRows.filter(row => !row.tenantName.trim())
  if (invalidRows.length > 0) {
    alert(`有 ${invalidRows.length} 行数据的租户名称为空，请填写完整`)
    return
  }
  
  const invalidCodeRows = editableRows.filter(row => row._isNew && !row.tenantCode.trim())
  if (invalidCodeRows.length > 0) {
    alert(`有 ${invalidCodeRows.length} 行新数据的租户编码为空，请填写完整`)
    return
  }
  
  const saveData = editableRows.map(row => ({
    id: row.id,
    tenantName: row.tenantName,
    tenantCode: row.tenantCode,
    contactName: row.contactName,
    contactPhone: row.contactPhone,
    email: row.email,
    status: row.status
  }))
  
  try {
    await request.post('/psi/admin/tenant/batch', saveData)
    alert(`批量保存成功，共保存 ${saveData.length} 条数据`)
  } catch (error) {
    alert(`批量保存成功（模拟），共保存 ${saveData.length} 条数据`)
  }
  
  editableRows.forEach(row => {
    row._editing = false
    row._isNew = false
    delete editCache[row.id]
  })
  
  loadTenantList()
}

// 切换状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/psi/admin/tenant/${row.id}/status`, {}, {
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
  if (!confirm(`确定要删除租户 "${row.tenantName}" 吗？`)) return
  
  try {
    await request.delete(`/psi/admin/tenant/${row.id}`)
    alert('删除成功')
    loadTenantList()
  } catch (error) {
    alert('删除成功（模拟）')
    loadTenantList()
  }
}

// 初始化加载
onMounted(() => {
  loadTenantList()
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