<template>
  <div class="goods-unit">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="单位名称">
          <el-input v-model="searchForm.unitName" placeholder="请输入单位名称" clearable />
        </el-form-item>
        <el-form-item label="单位编码">
          <el-input v-model="searchForm.unitCode" placeholder="请输入单位编码" clearable />
        </el-form-item>
        <el-form-item label="单位类型">
          <el-select v-model="searchForm.unitType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="重量" value="WEIGHT" />
            <el-option label="体积" value="VOLUME" />
            <el-option label="计数" value="COUNT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}单位</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="unitName" label="单位名称" />
        <el-table-column prop="unitCode" label="单位编码" />
        <el-table-column prop="unitSymbol" label="单位符号" />
        <el-table-column prop="unitType" label="单位类型" />
        <el-table-column prop="conversionRate" label="换算比例" />
        <el-table-column prop="description" label="描述" />
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
        <el-form-item label="单位名称" required>
          <el-input v-model="formData.unitName" />
        </el-form-item>
        <el-form-item label="单位编码" required>
          <el-input v-model="formData.unitCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="单位符号">
          <el-input v-model="formData.unitSymbol" />
        </el-form-item>
        <el-form-item label="单位类型">
          <el-select v-model="formData.unitType" placeholder="请选择" style="width: 100%">
            <el-option label="重量" value="WEIGHT" />
            <el-option label="体积" value="VOLUME" />
            <el-option label="计数" value="COUNT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="换算比例">
          <el-input-number v-model="formData.conversionRate" :min="0" :precision="4" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" rows="3" />
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
  unitName: '',
  unitCode: '',
  unitType: ''
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
  unitName: '',
  unitCode: '',
  unitSymbol: '',
  unitType: 'COUNT',
  conversionRate: 1,
  description: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await goodsApi.getUnitPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      unitName: searchForm.unitName,
      unitCode: searchForm.unitCode,
      unitType: searchForm.unitType
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
  searchForm.unitName = ''
  searchForm.unitCode = ''
  searchForm.unitType = ''
  currentPage.value = 1
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.unitName = ''
  formData.unitCode = ''
  formData.unitSymbol = ''
  formData.unitType = 'COUNT'
  formData.conversionRate = 1
  formData.description = ''
}

const handleAdd = () => {
  isEdit.value = false
  isView.value = false
  dialogTitle.value = '新增单位'
  resetForm()
  formData.unitCode = 'UNIT' + Date.now().toString().slice(-3)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  isView.value = false
  dialogTitle.value = '编辑单位'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isEdit.value = false
  isView.value = true
  dialogTitle.value = '查看单位'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await goodsApi.updateUnit(formData.id, formData)
      ElMessage.success('编辑成功')
    } else {
      await goodsApi.saveUnit(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该单位吗？', '提示', { type: 'warning' })
    .then(async () => {
      await goodsApi.deleteUnit(row.id)
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
.goods-unit {
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
