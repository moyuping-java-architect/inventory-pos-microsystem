<template>
  <div class="goods-sku">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="SKU编码">
          <el-input v-model="searchForm.skuCode" placeholder="请输入SKU编码" clearable />
        </el-form-item>
        <el-form-item label="商品统一编码">
          <el-input v-model="searchForm.goodsUnifyCode" placeholder="请输入统一编码" clearable />
        </el-form-item>
        <el-form-item label="条形码">
          <el-input v-model="searchForm.barcode" placeholder="请输入条形码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}SKU</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="skuCode" label="SKU编码" width="140" />
        <el-table-column prop="goodsUnifyCode" label="统一编码" width="140" />
        <el-table-column prop="specValues" label="规格值" />
        <el-table-column prop="barcode" label="条形码" width="140" />
        <el-table-column prop="baseUnit" label="基础单位" width="100" />
        <el-table-column prop="saleUnit" label="销售单位" width="100" />
        <el-table-column prop="costPrice" label="成本价" width="100" />
        <el-table-column prop="salePrice" label="销售价" width="100" />
        <el-table-column prop="taxRate" label="VAT税率" width="100">
          <template #default="scope">{{ scope.row.taxRate != null ? (scope.row.taxRate * 100).toFixed(0) + '%' : '16%' }}</template>
        </el-table-column>
        <el-table-column prop="stockQty" label="库存" width="80" />
        <el-table-column :label="t('common.actions')" width="200" fixed="right">
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
        <el-form-item label="SKU编码" required>
          <el-input v-model="formData.skuCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="商品ID" required>
          <el-input-number v-model="formData.goodsId" :min="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="统一编码">
          <el-input v-model="formData.goodsUnifyCode" />
        </el-form-item>
        <el-form-item label="规格值">
          <el-input v-model="formData.specValues" placeholder="如：红色,XL" />
        </el-form-item>
        <el-form-item label="规格JSON">
          <el-input v-model="formData.specJson" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="条形码">
          <el-input v-model="formData.barcode" />
        </el-form-item>
        <el-form-item label="基础单位">
          <el-input v-model="formData.baseUnit" />
        </el-form-item>
        <el-form-item label="销售单位">
          <el-input v-model="formData.saleUnit" />
        </el-form-item>
        <el-form-item label="成本价">
          <el-input-number v-model="formData.costPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="销售价">
          <el-input-number v-model="formData.salePrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="VAT税率">
          <el-input-number v-model="formData.taxRate" :min="0" :max="1" :precision="4" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="市场价">
          <el-input-number v-model="formData.marketPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="formData.stockQty" :min="0" controls-position="right" style="width: 100%" />
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
  skuCode: '',
  goodsUnifyCode: '',
  barcode: ''
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
  skuCode: '',
  goodsId: null,
  goodsUnifyCode: '',
  specValues: '',
  specJson: '',
  barcode: '',
  baseUnit: '',
  saleUnit: '',
  costPrice: 0,
  salePrice: 0,
  taxRate: 0.16,
  marketPrice: 0,
  stockQty: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await goodsApi.getSkuPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      skuCode: searchForm.skuCode,
      goodsUnifyCode: searchForm.goodsUnifyCode,
      barcode: searchForm.barcode
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
  searchForm.skuCode = ''
  searchForm.goodsUnifyCode = ''
  searchForm.barcode = ''
  currentPage.value = 1
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.skuCode = ''
  formData.goodsId = null
  formData.goodsUnifyCode = ''
  formData.specValues = ''
  formData.specJson = ''
  formData.barcode = ''
  formData.baseUnit = ''
  formData.saleUnit = ''
  formData.costPrice = 0
  formData.salePrice = 0
  formData.taxRate = 0.16
  formData.marketPrice = 0
  formData.stockQty = 0
}

const handleAdd = () => {
  isEdit.value = false
  isView.value = false
  dialogTitle.value = '新增SKU'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  isView.value = false
  dialogTitle.value = '编辑SKU'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isEdit.value = false
  isView.value = true
  dialogTitle.value = '查看SKU'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await goodsApi.updateSku(formData.id, formData)
      ElMessage.success('编辑成功')
    } else {
      await goodsApi.saveSku(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该SKU吗？', '提示', { type: 'warning' })
    .then(async () => {
      await goodsApi.deleteSku(row.id)
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
.goods-sku {
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
