<template>
  <div class="inventory">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.goodsName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input v-model="searchForm.goodsCode" placeholder="请输入商品编码" clearable />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable>
            <el-option label="全部" value="" />
            <el-option v-for="warehouse in warehouses" :key="warehouse.id" :label="warehouse.name" :value="warehouse.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="code" label="商品编码" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="spec" label="规格" />
        <el-table-column prop="unit" label="单位" />
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="quantity" label="库存数量" />
        <el-table-column prop="avgCost" label="平均成本" />
        <el-table-column prop="totalValue" label="库存金额" />
      </el-table>

      <el-pagination
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { stockApi } from '../../api'

const { t } = useI18n()

const searchForm = reactive({
  goodsName: '',
  goodsCode: '',
  warehouseId: ''
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)

const warehouses = ref([
  { id: 1, code: 'MAIN', name: '主仓库' },
  { id: 2, code: 'BACKUP', name: '备用仓库' }
])

const fetchData = async () => {
  loading.value = true
  try {
    const selectedWarehouse = warehouses.value.find(w => w.id === searchForm.warehouseId)
    const res = await stockApi.getStockForSelector({
      warehouseCode: selectedWarehouse?.code,
      goodsName: searchForm.goodsName || undefined,
      goodsCode: searchForm.goodsCode || undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res && res.data) {
      tableData.value = (res.data.list || []).map(item => ({
        id: item.id,
        code: item.goodsCode,
        name: item.goodsName,
        spec: item.goodsSpec,
        unit: item.unit,
        warehouseName: item.warehouseName,
        quantity: item.quantity,
        avgCost: '¥ ' + Number(item.avgCostPrice || 0).toFixed(2),
        totalValue: '¥ ' + Number(item.totalAmount || 0).toFixed(2)
      }))
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.goodsName = ''
  searchForm.goodsCode = ''
  searchForm.warehouseId = ''
  currentPage.value = 1
  fetchData()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.inventory {
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

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
</style>