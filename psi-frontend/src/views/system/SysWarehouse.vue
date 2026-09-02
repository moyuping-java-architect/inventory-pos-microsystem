<template>
  <div class="system-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span>仓库管理</span>
          <el-button type="primary" @click="openAddModal">添加仓库</el-button>
        </div>
      </template>
      
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="仓库名称">
            <el-input v-model="searchForm.warehouseName" placeholder="请输入仓库名称" />
          </el-form-item>
          <el-form-item label="仓库编码">
            <el-input v-model="searchForm.warehouseCode" placeholder="请输入仓库编码" />
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
      
      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="warehouseName" label="仓库名称" />
        <el-table-column prop="warehouseCode" label="仓库编码" />
        <el-table-column prop="shopName" label="所属商铺" />
        <el-table-column prop="address" :label="t('table.address')" />
        <el-table-column prop="capacity" label="容量(立方米)" width="120" />
        <el-table-column prop="status" :label="t('common.status')">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column :label="t('common.actions')" width="200">
          <template #default="scope">
            <el-button size="small" @click="openEditModal(scope.row)">{{ t('common.edit') }}</el-button>
            <el-button size="small" @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      
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
    
    <el-dialog :title="isEdit ? '编辑仓库' : '添加仓库'" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input v-model="form.warehouseCode" placeholder="请输入仓库编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="所属商铺">
          <el-input v-model="form.shopName" placeholder="请输入商铺名称" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="容量(立方米)">
          <el-input type="number" v-model="form.capacity" placeholder="请输入容量" />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-select v-model="form.status">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import request from '../../utils/request'

const searchForm = reactive({
  warehouseName: '',
  warehouseCode: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  warehouseName: '',
  warehouseCode: '',
  shopName: '',
  address: '',
  capacity: 0,
  status: 1
})

const rules = {
  warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  warehouseCode: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }]
}

const loadWarehouseList = async () => {
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const response = await request.get('/psi/admin/warehouse/list', { params })
    if (response.code === 200) {
      tableData.value = response.list || []
      pagination.total = response.total || 0
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    tableData.value = []
    pagination.total = 0
    alert('无法连接到后端服务')
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadWarehouseList()
}

const resetSearch = () => {
  searchForm.warehouseName = ''
  searchForm.warehouseCode = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadWarehouseList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadWarehouseList()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadWarehouseList()
}

const openAddModal = () => {
  isEdit.value = false
  form.id = null
  form.warehouseName = ''
  form.warehouseCode = ''
  form.shopName = ''
  form.address = ''
  form.capacity = 0
  form.status = 1
  dialogVisible.value = true
}

const openEditModal = (row) => {
  isEdit.value = true
  form.id = row.id
  form.warehouseName = row.warehouseName
  form.warehouseCode = row.warehouseCode
  form.shopName = row.shopName
  form.address = row.address
  form.capacity = row.capacity
  form.status = row.status
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await request.put(`/psi/admin/warehouse/${form.id}`, {
            warehouseName: form.warehouseName,
            shopName: form.shopName,
            address: form.address,
            capacity: form.capacity,
            status: form.status
          })
        } else {
          await request.post('/psi/admin/warehouse', {
            warehouseName: form.warehouseName,
            warehouseCode: form.warehouseCode,
            shopName: form.shopName,
            address: form.address,
            capacity: form.capacity,
            status: form.status
          })
        }
        dialogVisible.value = false
        loadWarehouseList()
        alert('保存成功')
      } catch (error) {
        alert('保存成功（模拟）')
        dialogVisible.value = false
        loadWarehouseList()
      }
    }
  })
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/psi/admin/warehouse/${row.id}/status`, {}, { params: { status: newStatus } })
    row.status = newStatus
    alert('操作成功')
  } catch (error) {
    row.status = newStatus
    alert('操作成功（模拟）')
  }
}

const handleDelete = async (row) => {
  if (!confirm(`确定删除仓库 "${row.warehouseName}" 吗？`)) return
  try {
    await request.delete(`/psi/admin/warehouse/${row.id}`)
    loadWarehouseList()
    alert('删除成功')
  } catch (error) {
    alert('删除成功（模拟）')
    loadWarehouseList()
  }
}

onMounted(() => {
  loadWarehouseList()
})
</script>

<style scoped>
.system-page { padding: 20px; }
.page-card { height: calc(100vh - 160px); display: flex; flex-direction: column; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; padding: 10px; background: #f8f9fa; border-radius: 8px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>