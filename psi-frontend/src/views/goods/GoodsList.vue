<template>
  <div class="goods-list">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input v-model="searchForm.goodsCode" placeholder="请输入商品编码" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="searchForm.brandId" placeholder="请选择品牌" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option v-for="brand in brands" :key="brand.id" :label="brand.brandName" :value="brand.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}商品</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="goodsCode" label="商品编码" />
        <el-table-column prop="goodsName" label="商品名称" />
        <el-table-column prop="goodsSpec" label="规格" />
        <el-table-column label="分类">
          <template #default="scope">
            {{ getCategoryName(scope.row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column label="品牌">
          <template #default="scope">
            {{ getBrandName(scope.row.brandId) }}
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" />
        <el-table-column prop="stockQty" label="库存" />
        <el-table-column prop="supplierCode" label="供应商编码" />
        <el-table-column :label="t('common.actions')" width="180" fixed="right">
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="formData" label-width="100px" :disabled="isView">
        <el-form-item label="商品编码" required>
          <el-input v-model="formData.goodsCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="商品名称" required>
          <el-input v-model="formData.goodsName" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="formData.goodsSpec" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" required>
          <el-select v-model="formData.brandId" placeholder="请选择品牌" style="width: 100%">
            <el-option v-for="brand in brands" :key="brand.id" :label="brand.brandName" :value="brand.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" required>
          <el-input v-model="formData.unit" />
        </el-form-item>
        <el-form-item label="供应商编码">
          <el-input v-model="formData.supplierCode" />
        </el-form-item>
        <el-form-item label="商品描述">
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
  goodsName: '',
  goodsCode: '',
  categoryId: '',
  brandId: ''
})

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const dialogTitle = ref('')

const categories = ref([])
const brands = ref([])

const formData = reactive({
  id: null,
  goodsCode: '',
  goodsName: '',
  goodsSpec: '',
  categoryId: null,
  brandId: null,
  unit: '',
  supplierCode: '',
  description: ''
})

const getCategoryName = (id) => {
  const cat = categories.value.find(item => item.id === id)
  return cat ? cat.categoryName : id
}

const getBrandName = (id) => {
  const brand = brands.value.find(item => item.id === id)
  return brand ? brand.brandName : id
}

const loadDict = async () => {
  try {
    const [catRes, brandRes] = await Promise.all([
      goodsApi.getCategoryList(),
      goodsApi.getBrandList()
    ])
    categories.value = (catRes.data || []).map(item => ({ ...item, id: Number(item.id) }))
    brands.value = (brandRes.data || []).map(item => ({ ...item, id: Number(item.id) }))
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      goodsName: searchForm.goodsName,
      goodsCode: searchForm.goodsCode
    }
    if (searchForm.categoryId) params.categoryId = searchForm.categoryId
    if (searchForm.brandId) params.brandId = searchForm.brandId
    const res = await goodsApi.getGoodsPage(params)
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
  searchForm.goodsName = ''
  searchForm.goodsCode = ''
  searchForm.categoryId = ''
  searchForm.brandId = ''
  currentPage.value = 1
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.goodsCode = ''
  formData.goodsName = ''
  formData.goodsSpec = ''
  formData.categoryId = null
  formData.brandId = null
  formData.unit = ''
  formData.supplierCode = ''
  formData.description = ''
}

const handleAdd = () => {
  isEdit.value = false
  isView.value = false
  dialogTitle.value = '新增商品'
  resetForm()
  formData.goodsCode = 'SP' + Date.now().toString().slice(-4)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  isView.value = false
  dialogTitle.value = '编辑商品'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isEdit.value = false
  isView.value = true
  dialogTitle.value = '查看商品'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await goodsApi.updateGoods(formData.id, formData)
      ElMessage.success('编辑成功')
    } else {
      await goodsApi.saveGoods(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' })
    .then(async () => {
      await goodsApi.deleteGoods(row.id)
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
  loadDict()
  loadData()
})
</script>

<style scoped>
.goods-list {
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
