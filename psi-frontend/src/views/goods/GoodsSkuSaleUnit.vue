<template>
  <div class="goods-sku-sale-unit">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="SKU ID">
          <el-input-number v-model="searchForm.skuId" :min="1" controls-position="right" style="width: 160px" />
        </el-form-item>
        <el-form-item label="统一编码">
          <el-input v-model="searchForm.goodsUnifyCode" placeholder="请输入统一编码" clearable />
        </el-form-item>
        <el-form-item label="销售单位">
          <el-input v-model="searchForm.saleUnitName" placeholder="请输入销售单位" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" icon="Plus" @click="handleAdd">{{ t('common.add') }}SKU销售单位</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="skuId" label="SKU ID" width="100" />
        <el-table-column prop="skuCode" label="SKU编码" width="140" />
        <el-table-column prop="goodsUnifyCode" label="统一编码" width="140" />
        <el-table-column prop="saleUnitName" label="销售单位" width="120" />
        <el-table-column prop="saleUnitSymbol" label="单位符号" width="100" />
        <el-table-column prop="conversionRate" label="换算比例" width="100" />
        <el-table-column prop="salePrice" label="销售价" width="100" />
        <el-table-column prop="costPrice" label="成本价" width="100" />
        <el-table-column prop="isDefault" label="是否默认" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isDefault === 1 ? 'success' : 'info'">{{ scope.row.isDefault === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="220" fixed="right">
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
      <el-form :model="formData" label-width="110px" :disabled="isView">
        <el-form-item label="SKU" required>
          <el-input
            v-model="formData.skuCode"
            placeholder="点击右侧按钮选择SKU"
            disabled
            style="width: calc(100% - 80px)"
          />
          <el-button
            type="primary"
            style="width: 76px; margin-left: 4px"
            @click="openSkuSelector"
            :disabled="isEdit || isView"
          >选择</el-button>
        </el-form-item>
        <el-form-item label="统一编码">
          <el-input v-model="formData.goodsUnifyCode" disabled />
        </el-form-item>
        <el-form-item label="销售单位名称" required>
          <el-input v-model="formData.saleUnitName" />
        </el-form-item>
        <el-form-item label="单位符号">
          <el-input v-model="formData.saleUnitSymbol" />
        </el-form-item>
        <el-form-item label="换算比例">
          <el-input-number v-model="formData.conversionRate" :min="0" :precision="4" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="销售价">
          <el-input-number v-model="formData.salePrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成本价">
          <el-input-number v-model="formData.costPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否默认">
          <el-radio-group v-model="formData.isDefault">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave" v-if="!isView">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog title="选择SKU" v-model="skuSelectorVisible" width="800px" append-to-body>
      <div class="sku-selector-search">
        <el-input
          v-model="skuSelectorKeyword"
          placeholder="请输入SKU编码/统一编码/条码搜索"
          clearable
          style="width: 300px"
          @keyup.enter="searchSkuList"
        />
        <el-button type="primary" @click="searchSkuList" style="margin-left: 8px">查询</el-button>
        <el-button @click="skuSelectorKeyword = ''; searchSkuList()">重置</el-button>
      </div>
      <el-table :data="skuSelectorData" border stripe v-loading="skuSelectorLoading" highlight-current-row @row-click="selectSku">
        <el-table-column prop="skuCode" label="SKU编码" width="140" />
        <el-table-column prop="goodsUnifyCode" label="统一编码" width="140" />
        <el-table-column prop="barcode" label="条码" width="160" />
        <el-table-column prop="specValues" label="规格" />
        <el-table-column prop="salePrice" label="销售价" width="100" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click.stop="selectSku(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        :total="skuSelectorTotal"
        :page-size="skuSelectorPageSize"
        :current-page="skuSelectorPageNum"
        layout="total, prev, pager, next, jumper"
        @current-change="handleSkuSelectorPageChange"
        @size-change="handleSkuSelectorSizeChange"
        style="margin-top: 12px; justify-content: flex-end"
      />
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
  skuId: null,
  goodsUnifyCode: '',
  saleUnitName: ''
})

const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const dialogTitle = ref('')

const skuSelectorVisible = ref(false)
const skuSelectorKeyword = ref('')
const skuSelectorData = ref([])
const skuSelectorTotal = ref(0)
const skuSelectorPageNum = ref(1)
const skuSelectorPageSize = ref(10)
const skuSelectorLoading = ref(false)

const formData = reactive({
  id: null,
  skuId: null,
  skuCode: '',
  goodsUnifyCode: '',
  saleUnitName: '',
  saleUnitSymbol: '',
  conversionRate: 1,
  salePrice: 0,
  costPrice: 0,
  isDefault: 0,
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      goodsUnifyCode: searchForm.goodsUnifyCode,
      saleUnitName: searchForm.saleUnitName
    }
    if (searchForm.skuId) {
      params.skuId = searchForm.skuId
    }
    const res = await goodsApi.getSkuSaleUnitPage(params)
    console.log('sku sale unit res:', res)
    tableData.value = res.list || (res.data && res.data.list) || []
    total.value = res.total || (res.data && res.data.total) || 0
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
  searchForm.skuId = null
  searchForm.goodsUnifyCode = ''
  searchForm.saleUnitName = ''
  currentPage.value = 1
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.skuId = null
  formData.skuCode = ''
  formData.goodsUnifyCode = ''
  formData.saleUnitName = ''
  formData.saleUnitSymbol = ''
  formData.conversionRate = 1
  formData.salePrice = 0
  formData.costPrice = 0
  formData.isDefault = 0
  formData.status = 1
}

const handleAdd = () => {
  isEdit.value = false
  isView.value = false
  dialogTitle.value = '新增SKU销售单位'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  isView.value = false
  dialogTitle.value = '编辑SKU销售单位'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  isEdit.value = false
  isView.value = true
  dialogTitle.value = '查看SKU销售单位'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const openSkuSelector = () => {
  skuSelectorVisible.value = true
  skuSelectorKeyword.value = ''
  skuSelectorPageNum.value = 1
  skuSelectorPageSize.value = 10
  searchSkuList()
}

const searchSkuList = async () => {
  skuSelectorLoading.value = true
  try {
    const res = await goodsApi.getSkuPage({
      pageNum: skuSelectorPageNum.value,
      pageSize: skuSelectorPageSize.value,
      keyword: skuSelectorKeyword.value
    })
    console.log('sku selector res:', res)
    skuSelectorData.value = res.list || (res.data && res.data.list) || []
    skuSelectorTotal.value = res.total || (res.data && res.data.total) || 0
  } catch (e) {
    console.error(e)
    skuSelectorData.value = []
    skuSelectorTotal.value = 0
  } finally {
    skuSelectorLoading.value = false
  }
}

const selectSku = (row) => {
  formData.skuId = row.id
  formData.skuCode = row.skuCode || ''
  formData.goodsUnifyCode = row.goodsUnifyCode || ''
  skuSelectorVisible.value = false
}

const handleSkuSelectorPageChange = (page) => {
  skuSelectorPageNum.value = page
  searchSkuList()
}

const handleSkuSelectorSizeChange = (size) => {
  skuSelectorPageSize.value = size
  skuSelectorPageNum.value = 1
  searchSkuList()
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await goodsApi.updateSkuSaleUnit(formData)
      ElMessage.success('编辑成功')
    } else {
      await goodsApi.saveSkuSaleUnit(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该SKU销售单位吗？', '提示', { type: 'warning' })
    .then(async () => {
      await goodsApi.deleteSkuSaleUnit(row.id)
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
.goods-sku-sale-unit {
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

.sku-selector-search {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
</style>
