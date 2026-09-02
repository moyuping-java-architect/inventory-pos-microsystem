<template>
  <div class="goods-brand">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="品牌名称">
          <el-input v-model="searchForm.brandName" placeholder="请输入品牌名称" clearable />
        </el-form-item>
        <el-form-item label="品牌编码">
          <el-input v-model="searchForm.brandCode" placeholder="请输入品牌编码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}品牌</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="brandName" label="品牌名称" />
        <el-table-column prop="brandCode" label="品牌编码" />
        <el-table-column prop="brandLogo" label="品牌logo" />
        <el-table-column prop="brandDesc" label="品牌描述" />
        <el-table-column prop="createTime" :label="t('table.createTime')" />
        <el-table-column :label="t('common.actions')" width="180">
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
      <el-form :model="formData" label-width="100px" :disabled="isView">
        <el-form-item label="品牌名称" required>
          <el-input v-model="formData.brandName" />
        </el-form-item>
        <el-form-item label="品牌编码" required>
          <el-input v-model="formData.brandCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="品牌logo">
          <el-input v-model="formData.brandLogo" />
        </el-form-item>
        <el-form-item label="品牌描述">
          <el-input v-model="formData.brandDesc" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave" v-if="!isView">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { goodsApi } from '../../api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const loading = ref(false)
const searchForm = reactive({
  brandName: '',
  brandCode: ''
})

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const dialogTitle = ref('')

const formData = reactive({
  id: null,
  brandName: '',
  brandCode: '',
  brandLogo: '',
  brandDesc: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await goodsApi.getBrandPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      brandName: searchForm.brandName,
      brandCode: searchForm.brandCode
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.brandName = ''
  searchForm.brandCode = ''
  currentPage.value = 1
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.brandName = ''
  formData.brandCode = ''
  formData.brandLogo = ''
  formData.brandDesc = ''
}

const handleAdd = () => {
  isEdit.value = false
  isView.value = false
  dialogTitle.value = '新增品牌'
  resetForm()
  formData.brandCode = 'BRAND' + Date.now().toString().slice(-3)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  isView.value = false
  dialogTitle.value = '编辑品牌'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isEdit.value = false
  isView.value = true
  dialogTitle.value = '查看品牌'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await goodsApi.updateBrand(formData.id, formData)
      ElMessage.success('编辑成功')
    } else {
      await goodsApi.saveBrand(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该品牌吗？', '提示', { type: 'warning' })
    .then(async () => {
      await goodsApi.deleteBrand(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.goods-brand {
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

.el-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
