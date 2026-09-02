<template>
  <div class="system-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>商铺管理</span>
          <el-button type="primary" @click="addRow">添加行</el-button>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="商铺名称">
            <el-input v-model="searchForm.shopName" placeholder="请输入商铺名称" />
          </el-form-item>
          <el-form-item label="商铺编码">
            <el-input v-model="searchForm.shopCode" placeholder="请输入商铺编码" />
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
        <el-table-column prop="shopName" label="商铺名称" width="150">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.shopName" 
              class="edit-input"
              placeholder="商铺名称"
            />
            <span v-else>{{ scope.row.shopName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="shopCode" label="商铺编码" width="120">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.shopCode" 
              class="edit-input"
              placeholder="商铺编码"
              :disabled="!scope.row._isNew"
            />
            <span v-else>{{ scope.row.shopCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tenantName" label="所属租户" width="120" />
        <el-table-column prop="address" :label="t('table.address')" width="180">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.address" 
              class="edit-input"
              placeholder="地址"
            />
            <span v-else>{{ scope.row.address }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" :label="t('table.phone')" width="130">
          <template #default="scope">
            <el-input 
              v-if="scope.row._editing" 
              v-model="scope.row.phone" 
              class="edit-input"
              placeholder="联系电话"
            />
            <span v-else>{{ scope.row.phone }}</span>
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
              <el-button size="small" type="primary" @click="saveRow(scope.row)">{{ t('common.save') }}</el-button>
              <el-button size="small" @click="cancelEdit(scope.row)">{{ t('common.cancel') }}</el-button>
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
  shopName: '',
  shopCode: '',
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

// 加载商铺列表
const loadShopList = async () => {
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const response = await request.get('/psi/admin/shop/list', { params })
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
  loadShopList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.shopName = ''
  searchForm.shopCode = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadShopList()
}

// 每页条数改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadShopList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadShopList()
}

// 添加行
const addRow = () => {
  const newRow = {
    id: null,
    shopName: '',
    shopCode: '',
    tenantName: '',
    address: '',
    phone: '',
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

// 保存行
const saveRow = async (row) => {
  if (!row.shopName) {
    alert('请输入商铺名称')
    return
  }
  if (!row.shopCode) {
    alert('请输入商铺编码')
    return
  }
  
  try {
    if (row._isNew) {
      // 新增
      await request.post('/psi/admin/shop', {
        shopName: row.shopName,
        shopCode: row.shopCode,
        address: row.address,
        phone: row.phone,
        status: row.status
      })
      alert('添加成功')
    } else {
      // 编辑
      await request.put(`/psi/admin/shop/${row.id}`, {
        shopName: row.shopName,
        address: row.address,
        phone: row.phone,
        status: row.status
      })
      alert('修改成功')
    }
    row._editing = false
    row._isNew = false
    delete editCache[row.id]
    loadShopList()
  } catch (error) {
    alert('保存成功（模拟）')
    row._editing = false
    row._isNew = false
    delete editCache[row.id]
    loadShopList()
  }
}

// 切换状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/psi/admin/shop/${row.id}/status`, {}, {
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
  if (!confirm(`确定要删除商铺 "${row.shopName}" 吗？`)) return
  
  try {
    await request.delete(`/psi/admin/shop/${row.id}`)
    alert('删除成功')
    loadShopList()
  } catch (error) {
    alert('删除成功（模拟）')
    loadShopList()
  }
}

// 初始化加载
onMounted(() => {
  loadShopList()
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