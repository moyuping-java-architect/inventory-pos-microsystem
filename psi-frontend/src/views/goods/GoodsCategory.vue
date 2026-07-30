<template>
  <div class="goods-category">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.name" placeholder="请输入分类名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}分类</el-button>
    </div>
    
    <div class="table-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="code" label="分类编码" />
        <el-table-column prop="parentName" label="上级分类" />
        <el-table-column prop="sortOrder" label="排序" />
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column :label="t('common.actions')" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">{{ t('common.view') }}</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">{{ t('common.edit') }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>
    
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="分类名称" required>
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="分类编码" :disabled="isEdit">
          <el-input v-model="formData.code" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="formData.parentId" placeholder="请选择上级分类">
            <el-option label="无" value="" />
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input type="number" v-model="formData.sortOrder" />
        </el-form-item>
        <el-form-item label="备注">
          <el-textarea v-model="formData.remark" rows="3" />
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
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const searchForm = reactive({
  name: ''
})

const tableData = ref([
  { id: 1, name: '分类A', code: 'CAT001', parentName: '-', sortOrder: 1, createTime: '2024-12-01' },
  { id: 2, name: '分类B', code: 'CAT002', parentName: '-', sortOrder: 2, createTime: '2024-12-02' },
  { id: 3, name: '子分类A1', code: 'CAT003', parentName: '分类A', sortOrder: 3, createTime: '2024-12-03' }
])

const total = ref(20)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')

const formData = reactive({
  id: '',
  name: '',
  code: '',
  parentId: '',
  sortOrder: '',
  remark: ''
})

const categories = ref([
  { id: 1, name: '分类A' },
  { id: 2, name: '分类B' }
])

const handleSearch = () => {}
const handleReset = () => {
  searchForm.name = ''
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增分类'
  formData.id = ''
  formData.name = ''
  formData.code = 'CAT' + Date.now().toString().slice(-3)
  formData.parentId = ''
  formData.sortOrder = ''
  formData.remark = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  dialogTitle.value = '查看分类'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = () => {}
const handleSave = () => {
  dialogVisible.value = false
}

const handlePageChange = (page) => {
  currentPage.value = page
}
</script>

<style scoped>
.goods-category {
  padding: 20px;
}

.search-bar {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.search-form :deep(.el-form-item) {
  margin-right: 16px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
</style>