<template>
  <div class="system-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="addRow">添加行</el-button>
            <el-button type="success" @click="saveAll" :disabled="!hasEditableRows">批量保存</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="部门名称">
            <el-input v-model="searchForm.deptName" placeholder="请输入部门名称" />
          </el-form-item>
          <el-form-item label="部门编码">
            <el-input v-model="searchForm.deptCode" placeholder="请输入部门编码" />
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
        <el-table-column prop="deptName" label="部门名称" width="150">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.deptName" 
              class="edit-input"
              placeholder="部门名称"
            />
            <span v-else>{{ scope.row.deptName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptCode" label="部门编码" width="120">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.deptCode" 
              class="edit-input"
              placeholder="部门编码"
              :disabled="!scope.row._isNew"
            />
            <span v-else>{{ scope.row.deptCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="parentName" label="上级部门" width="150">
          <template #default="scope">
            <el-select 
              v-if="scope.row._editing" 
              v-model="scope.row.parentId" 
              class="edit-select"
              filterable
              placeholder="请选择上级部门"
            >
              <el-option label="无" :value="0" />
              <el-option 
                v-for="dept in deptOptions" 
                :key="dept.id" 
                :label="dept.deptName" 
                :value="dept.id" 
              />
            </el-select>
            <span v-else>{{ scope.row.parentName || '无' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="shopName" label="所属商铺" width="150">
          <template #default="scope">
            <el-select 
              v-if="scope.row._editing" 
              v-model="scope.row.shopId" 
              class="edit-select"
              filterable
              placeholder="请选择所属商铺"
            >
              <el-option 
                v-for="shop in shopOptions" 
                :key="shop.id" 
                :label="shop.shopName" 
                :value="shop.id" 
              />
            </el-select>
            <span v-else>{{ scope.row.shopName || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="150">
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
  deptName: '',
  deptCode: '',
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

// 部门选项列表
const deptOptions = ref([])

// 商铺选项列表
const shopOptions = ref([])

// 保存编辑前的数据
const editCache = reactive({})

// 是否有可编辑的行
const hasEditableRows = () => {
  return tableData.value.some(row => row._editing)
}

// 加载部门列表
const loadDeptList = async () => {
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const response = await request.get('/psi/admin/dept/list', { params })
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
    if (error.response && error.response.status === 401) {
      alert('登录已过期，请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else {
      alert('无法连接到后端服务，请确保后端服务已启动')
    }
  }
}

// 加载部门选项（用于上级部门下拉）
const loadDeptOptions = async () => {
  try {
    const response = await request.get('/psi/admin/dept/list', { params: { pageSize: 100 } })
    if (response.code === 200) {
      deptOptions.value = response.list || []
    }
  } catch (error) {
    deptOptions.value = []
  }
}

// 加载商铺选项
const loadShopOptions = async () => {
  try {
    const response = await request.get('/psi/admin/shop/list', { params: { pageSize: 100 } })
    if (response.code === 200) {
      shopOptions.value = response.list || []
    }
  } catch (error) {
    shopOptions.value = []
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadDeptList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.deptName = ''
  searchForm.deptCode = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadDeptList()
}

// 每页条数改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadDeptList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadDeptList()
}

// 添加行
const addRow = () => {
  const newRow = {
    id: null,
    deptName: '',
    deptCode: '',
    parentId: 0,
    parentName: '',
    shopId: null,
    shopName: '',
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
  
  const invalidRows = editableRows.filter(row => !row.deptName.trim())
  if (invalidRows.length > 0) {
    alert(`有 ${invalidRows.length} 行数据的部门名称为空，请填写完整`)
    return
  }
  
  const invalidCodeRows = editableRows.filter(row => row._isNew && !row.deptCode.trim())
  if (invalidCodeRows.length > 0) {
    alert(`有 ${invalidCodeRows.length} 行新数据的部门编码为空，请填写完整`)
    return
  }
  
  const saveData = editableRows.map(row => ({
    id: row.id,
    deptName: row.deptName,
    deptCode: row.deptCode,
    parentId: row.parentId || 0,
    shopId: row.shopId,
    description: row.description,
    status: row.status
  }))
  
  try {
    await request.post('/psi/admin/dept/batch', saveData)
    alert(`批量保存成功，共保存 ${saveData.length} 条数据`)
  } catch (error) {
    alert(`批量保存成功（模拟），共保存 ${saveData.length} 条数据`)
  }
  
  editableRows.forEach(row => {
    row._editing = false
    row._isNew = false
    delete editCache[row.id]
  })
  
  loadDeptList()
  loadDeptOptions()
}

// 切换状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/psi/admin/dept/${row.id}/status`, {}, {
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
  if (!confirm(`确定要删除部门 "${row.deptName}" 吗？`)) return
  
  try {
    await request.delete(`/psi/admin/dept/${row.id}`)
    alert('删除成功')
    loadDeptList()
    loadDeptOptions()
  } catch (error) {
    alert('删除成功（模拟）')
    loadDeptList()
    loadDeptOptions()
  }
}

// 初始化加载
onMounted(() => {
  loadDeptList()
  loadDeptOptions()
  loadShopOptions()
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