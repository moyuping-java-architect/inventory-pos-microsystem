<template>
  <div class="goods-category">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.categoryName" placeholder="请输入分类名称" clearable />
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
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="categoryCode" label="分类编码" />
        <el-table-column prop="parentName" label="上级分类" />
        <el-table-column prop="sortOrder" label="排序" />
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column :label="t('common.actions')" width="200">
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
        @size-change="handleSizeChange"
      />
    </div>
    
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="formData" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="分类名称" prop="categoryName" required>
          <el-input v-model="formData.categoryName" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode" required>
          <el-input v-model="formData.categoryCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="formData.parentId" placeholder="请选择上级分类" clearable>
            <el-option label="无" :value="null" />
            <el-option v-for="cat in parentCategories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.description" type="textarea" rows="3" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { goodsApi } from '../../api/modules/goods'

const { t } = useI18n()

const loading = ref(false)
const searchForm = reactive({
  categoryName: ''
})

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const formData = reactive({
  id: null,
  categoryName: '',
  categoryCode: '',
  parentId: null,
  sortOrder: 0,
  description: ''
})

const formRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
}

const parentCategories = ref([])

// 查询分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      categoryName: searchForm.categoryName
    }
    const res = await goodsApi.getCategoryPage(params)
    if (res.code === 200) {
      // 处理数据，添加上级分类名称
      // PageResult 直接返回 list 和 total，不是嵌套在 data 中
      const list = res.list || []
      tableData.value = list.map(item => ({
        ...item,
        parentName: item.parentId ? getParentName(item.parentId, list) : '-'
      }))
      total.value = res.total || 0
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询分类列表失败:', error)
    ElMessage.error('查询分类列表失败')
  } finally {
    loading.value = false
  }
}

// 获取上级分类名称
const getParentName = (parentId, list) => {
  const parent = list.find(item => item.id === parentId)
  return parent ? parent.categoryName : '-'
}

// 查询所有分类（用于上级分类选择）
const fetchAllCategories = async () => {
  try {
    const res = await goodsApi.getCategoryList()
    if (res.code === 200) {
      parentCategories.value = res.data || []
    }
  } catch (error) {
    console.error('查询分类列表失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchCategoryList()
}

const handleReset = () => {
  searchForm.categoryName = ''
  currentPage.value = 1
  fetchCategoryList()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增分类'
  formData.id = null
  formData.categoryName = ''
  formData.categoryCode = ''
  formData.parentId = null
  formData.sortOrder = 0
  formData.description = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  formData.id = row.id
  formData.categoryName = row.categoryName
  formData.categoryCode = row.categoryCode
  formData.parentId = row.parentId
  formData.sortOrder = row.sortOrder
  formData.description = row.description
  dialogVisible.value = true
}

const handleView = (row) => {
  dialogTitle.value = '查看分类'
  formData.id = row.id
  formData.categoryName = row.categoryName
  formData.categoryCode = row.categoryCode
  formData.parentId = row.parentId
  formData.sortOrder = row.sortOrder
  formData.description = row.description
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await goodsApi.deleteCategory(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCategoryList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let res
        if (isEdit.value) {
          res = await goodsApi.updateCategory(formData.id, formData)
        } else {
          res = await goodsApi.saveCategory(formData)
        }
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
          dialogVisible.value = false
          fetchCategoryList()
        } else {
          ElMessage.error(res.message || (isEdit.value ? '更新失败' : '新增失败'))
        }
      } catch (error) {
        console.error(isEdit.value ? '更新分类失败:' : '新增分类失败:', error)
        ElMessage.error(isEdit.value ? '更新分类失败' : '新增分类失败')
      }
    }
  })
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchCategoryList()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchCategoryList()
}

onMounted(() => {
  fetchCategoryList()
  fetchAllCategories()
})
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