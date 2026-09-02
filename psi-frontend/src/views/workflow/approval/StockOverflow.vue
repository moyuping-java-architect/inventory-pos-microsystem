<template>
  <ApprovalContainer :title="t('workflow.stockOverflowTitle')">
    <template #doc-detail="{ detail, formatAmount, docStatusType, getSummaries, t }">
      <div class="doc-section" v-if="detail.doc">
        <div class="section-title">
          <span class="title-icon">&#128196;</span>
          {{ t('workflow.stockOverflowDetail') }}
          <el-tag size="small" type="info" style="margin-left: 8px">{{ detail.doc.docTypeDesc || detail.doc.docType }}</el-tag>
        </div>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item :label="t('workflow.docNo')">{{ detail.doc.docNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="单据名称">{{ detail.doc.docName || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('workflow.docStatus')">
            <el-tag :type="docStatusType(detail.doc.status)" size="small">{{ detail.doc.statusDesc || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('workflow.shop')">{{ detail.doc.shopName || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('workflow.overflowWarehouse')">{{ detail.doc.warehouseName || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('workflow.creator')">{{ detail.doc.creatorName || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('workflow.overflowDate')">{{ detail.doc.docDate || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('workflow.remark')" :span="2">{{ detail.doc.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="sub-title">{{ t('workflow.overflowItems') }}{{ detail.doc.itemCount || 0 }}{{ t('workflow.itemsSuffix') }}</div>
        <el-table :data="detail.doc.items || []" border stripe size="small" max-height="400" show-summary :summary-method="getSummaries">
          <el-table-column type="index" :label="t('workflow.seqNo')" width="55" />
          <el-table-column prop="goodsCode" :label="t('workflow.goodsCode')" min-width="120" />
          <el-table-column prop="goodsName" :label="t('workflow.goodsName')" min-width="150" />
          <el-table-column prop="goodsSpec" :label="t('workflow.goodsSpec')" width="100" />
          <el-table-column prop="goodsUnit" :label="t('workflow.goodsUnit')" width="70" />
          <el-table-column prop="quantity" :label="t('workflow.overflowQty')" width="100" align="right" />
          <el-table-column prop="remark" :label="t('workflow.itemRemark')" min-width="120" />
        </el-table>
      </div>
    </template>
  </ApprovalContainer>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import ApprovalContainer from './ApprovalContainer.vue'

const { t } = useI18n()
</script>

<style scoped>
.doc-section { margin-bottom: 24px; }
.doc-section .sub-title { font-size: 14px; font-weight: 600; color: #303133; margin: 16px 0 10px; padding-left: 8px; border-left: 3px solid #409eff; }
</style>