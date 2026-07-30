package com.psi.order.service.impl;

import com.psi.order.dto.CreateDocRequest;
import com.psi.order.dto.DocResponse;
import com.psi.order.entity.DocEntity;
import com.psi.order.entity.DocItemEntity;
import com.psi.order.mapper.DocMapper;
import com.psi.order.mapper.DocItemMapper;
import com.psi.order.service.DocService;
import com.psi.order.util.DocNoGenerator;
import com.psi.order.constant.DocTypeConstant.DocType;
import com.psi.order.constant.DocTypeConstant.DocStatus;
import com.psi.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单据服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocServiceImpl implements DocService {

    private final DocMapper docMapper;
    private final DocItemMapper docItemMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public DocResponse create(CreateDocRequest request) {
        validateRequest(request);
        DocType docType = DocType.fromCode(request.getDocType());
        
        DocEntity doc = new DocEntity();
        doc.setDocNo(DocNoGenerator.generate(docType));
        doc.setDocType(docType.getCode());
        doc.setDocName(request.getDocName() != null ? request.getDocName() : generateDefaultDocName(docType));
        doc.setStatus(DocStatus.DRAFT.getValue());
        doc.setShopCode(request.getShopCode());
        doc.setShopName(request.getShopName());
        doc.setCreatorId(request.getCreatorId());
        doc.setCreatorName(request.getCreatorName());
        doc.setDeptId(request.getDeptId());
        doc.setDeptName(request.getDeptName());
        doc.setPartnerId(request.getPartnerId());
        doc.setPartnerCode(request.getPartnerCode());
        doc.setPartnerName(request.getPartnerName());
        doc.setWarehouseId(request.getWarehouseId());
        doc.setWarehouseCode(request.getWarehouseCode());
        doc.setWarehouseName(request.getWarehouseName());
        doc.setOrderNo(request.getOrderNo());
        doc.setSaleType(request.getSaleType());
        doc.setPaymentType(request.getPaymentType());
        doc.setCurrencyCode(request.getCurrencyCode());
        doc.setExchangeRate(request.getExchangeRate());
        doc.setTaxAmount(request.getTaxAmount());
        doc.setDiscountAmount(request.getDiscountAmount());
        doc.setPayAmount(request.getPayAmount());
        doc.setDocDate(request.getDocDate() != null ? request.getDocDate() : LocalDateTime.now());
        doc.setDeliveryDate(request.getDeliveryDate());
        doc.setRemark(request.getRemark());
        doc.setExtJson(request.getExtJson());
        docMapper.insert(doc);

        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;
        
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            int lineNo = 1;
            for (CreateDocRequest.DocItemRequest itemRequest : request.getItems()) {
                DocItemEntity item = new DocItemEntity();
                item.setDocId(doc.getId());
                item.setDocNo(doc.getDocNo());
                item.setShopCode(request.getShopCode());
                item.setShopName(request.getShopName());
                item.setGoodsId(itemRequest.getGoodsId());
                item.setGoodsCode(itemRequest.getGoodsCode());
                item.setSkuCode(itemRequest.getSkuCode());
                item.setSkuName(itemRequest.getSkuName());
                item.setBarcode(itemRequest.getBarcode());
                item.setGoodsName(itemRequest.getGoodsName());
                item.setGoodsSpec(itemRequest.getGoodsSpec());
                item.setUnitCode(itemRequest.getUnitCode());
                item.setGoodsUnit(itemRequest.getGoodsUnit());
                item.setConversionRate(getConversionRate(itemRequest.getSkuCode(), itemRequest.getUnitCode(), itemRequest.getConversionRate()));
                item.setUnitPrice(itemRequest.getUnitPrice());
                item.setQuantity(itemRequest.getQuantity());
                item.setTaxRate(itemRequest.getTaxRate());
                item.setDiscountRate(itemRequest.getDiscountRate());
                item.setDiscountAmount(itemRequest.getDiscountAmount());
                item.setCostPrice(itemRequest.getCostPrice());
                item.setStockId(itemRequest.getStockId());
                item.setBatchNo(itemRequest.getBatchNo());
                item.setExpiryDate(itemRequest.getExpiryDate());
                item.setRemark(itemRequest.getRemark());
                item.setLineNo(itemRequest.getLineNo() != null ? itemRequest.getLineNo() : lineNo++);
                
                item.calculateAll();
                
                docItemMapper.insert(item);
                
                if (item.getAmount() != null) {
                    totalAmount = totalAmount.add(item.getAmount());
                }
                itemCount++;
            }
        }
        
        doc.setTotalAmount(totalAmount);
        doc.setItemCount(itemCount);
        docMapper.updateById(doc);
        
        log.info("创建单据成功: docNo={}, docType={}", doc.getDocNo(), docType.getDescription());
        return findById(doc.getId());
    }

    @Override
    public DocResponse findById(Long id) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        return buildResponse(doc);
    }

    @Override
    public DocResponse findByDocNo(String docNo) {
        DocEntity doc = docMapper.selectByDocNo(docNo);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        return buildResponse(doc);
    }

    @Override
    public IPage<DocResponse> findPage(Page<DocResponse> page, String docType, Integer status, String creatorId) {
        Page<DocEntity> entityPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<DocEntity> result = docMapper.selectPage(entityPage, docType, status, creatorId, null, null, null, null, null, null);
        
        Page<DocResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<DocResponse> records = result.getRecords().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(records);
        
        return responsePage;
    }

    @Override
    public IPage<DocResponse> findPageByStatusList(Page<DocResponse> page, String docType, List<Integer> statusList, String creatorId) {
        Page<DocEntity> entityPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<DocEntity> result = docMapper.selectPageByStatusList(entityPage, docType, statusList, creatorId);
        
        Page<DocResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<DocResponse> records = result.getRecords().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(records);
        
        return responsePage;
    }

    @Override
    public List<DocResponse> findByDocType(String docType) {
        List<DocEntity> docs = docMapper.selectByDocType(docType);
        return docs.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    @Override
    public List<DocResponse> findByStatus(Integer status) {
        List<DocEntity> docs = docMapper.selectByStatus(status);
        return docs.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    @Override
    public List<DocResponse> findPendingApprove() {
        List<DocEntity> docs = docMapper.selectPendingApprove();
        return docs.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocResponse update(Long id, CreateDocRequest request) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        
        validateRequest(request);
        
        DocStatus status = doc.getStatusEnum();
        if (status != DocStatus.DRAFT && status != DocStatus.REJECTED) {
            throw new BusinessException("只能修改草稿或已驳回的单据");
        }
        
        doc.setShopCode(request.getShopCode());
        doc.setShopName(request.getShopName());
        doc.setDeptId(request.getDeptId());
        doc.setDeptName(request.getDeptName());
        doc.setPartnerId(request.getPartnerId());
        doc.setPartnerCode(request.getPartnerCode());
        doc.setPartnerName(request.getPartnerName());
        doc.setWarehouseId(request.getWarehouseId());
        doc.setWarehouseCode(request.getWarehouseCode());
        doc.setWarehouseName(request.getWarehouseName());
        doc.setSaleType(request.getSaleType());
        doc.setPaymentType(request.getPaymentType());
        doc.setCurrencyCode(request.getCurrencyCode());
        doc.setExchangeRate(request.getExchangeRate());
        doc.setTaxAmount(request.getTaxAmount());
        doc.setDiscountAmount(request.getDiscountAmount());
        doc.setPayAmount(request.getPayAmount());
        doc.setDocDate(request.getDocDate() != null ? request.getDocDate() : doc.getDocDate());
        doc.setDeliveryDate(request.getDeliveryDate());
        doc.setDocName(request.getDocName() != null ? request.getDocName() : doc.getDocName());
        doc.setRemark(request.getRemark());
        doc.setExtJson(request.getExtJson());
        docMapper.updateById(doc);
        
        docItemMapper.delete(Wrappers.lambdaQuery(DocItemEntity.class).eq(DocItemEntity::getDocId, id));
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;
        
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            int lineNo = 1;
            for (CreateDocRequest.DocItemRequest itemRequest : request.getItems()) {
                DocItemEntity item = new DocItemEntity();
                item.setDocId(doc.getId());
                item.setDocNo(doc.getDocNo());
                item.setShopCode(request.getShopCode());
                item.setShopName(request.getShopName());
                item.setGoodsId(itemRequest.getGoodsId());
                item.setGoodsCode(itemRequest.getGoodsCode());
                item.setSkuCode(itemRequest.getSkuCode());
                item.setSkuName(itemRequest.getSkuName());
                item.setBarcode(itemRequest.getBarcode());
                item.setGoodsName(itemRequest.getGoodsName());
                item.setGoodsSpec(itemRequest.getGoodsSpec());
                item.setUnitCode(itemRequest.getUnitCode());
                item.setGoodsUnit(itemRequest.getGoodsUnit());
                item.setConversionRate(getConversionRate(itemRequest.getSkuCode(), itemRequest.getUnitCode(), itemRequest.getConversionRate()));
                item.setUnitPrice(itemRequest.getUnitPrice());
                item.setQuantity(itemRequest.getQuantity());
                item.setTaxRate(itemRequest.getTaxRate());
                item.setDiscountRate(itemRequest.getDiscountRate());
                item.setDiscountAmount(itemRequest.getDiscountAmount());
                item.setCostPrice(itemRequest.getCostPrice());
                item.setStockId(itemRequest.getStockId());
                item.setBatchNo(itemRequest.getBatchNo());
                item.setExpiryDate(itemRequest.getExpiryDate());
                item.setRemark(itemRequest.getRemark());
                item.setLineNo(itemRequest.getLineNo() != null ? itemRequest.getLineNo() : lineNo++);
                
                item.calculateAll();
                
                docItemMapper.insert(item);
                
                if (item.getAmount() != null) {
                    totalAmount = totalAmount.add(item.getAmount());
                }
                itemCount++;
            }
        }
        
        doc.setTotalAmount(totalAmount);
        doc.setItemCount(itemCount);
        docMapper.updateById(doc);
        
        log.info("更新单据成功: docNo={}", doc.getDocNo());
        return findById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        
        DocStatus status = doc.getStatusEnum();
        if (status != DocStatus.DRAFT) {
            throw new BusinessException("只能删除草稿状态的单据");
        }
        
        docItemMapper.delete(Wrappers.lambdaQuery(DocItemEntity.class).eq(DocItemEntity::getDocId, id));
        docMapper.deleteById(id);
        
        log.info("删除单据成功: docNo={}", doc.getDocNo());
    }

    @Override
    @Transactional
    public DocResponse submit(Long id) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        
        if (!doc.canSubmit()) {
            throw new BusinessException("当前状态无法提交");
        }
        
        List<DocItemEntity> items = docItemMapper.selectByDocId(id);
        if (items == null || items.isEmpty()) {
            throw new BusinessException("请添加商品明细后再提交");
        }
        for (int i = 0; i < items.size(); i++) {
            DocItemEntity item = items.get(i);
            if (item.getGoodsId() == null) {
                throw new BusinessException("第" + (i + 1) + "行商品未选择");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("第" + (i + 1) + "行数量必须大于0");
            }
        }
        
        doc.setStatus(DocStatus.SUBMITTED.getValue());
        docMapper.updateById(doc);
        
        log.info("提交单据成功: docNo={}", doc.getDocNo());
        return findById(id);
    }

    @Override
    @Transactional
    public DocResponse approve(Long id, String approverId, String approverName) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        
        if (!doc.canApprove()) {
            throw new BusinessException("当前状态无法审批");
        }
        
        doc.setStatus(DocStatus.APPROVED.getValue());
        doc.setApproveTime(LocalDateTime.now());
        docMapper.updateById(doc);
        
        log.info("审批通过单据: docNo={}, approver={}", doc.getDocNo(), approverName);
        return findById(id);
    }

    @Override
    @Transactional
    public DocResponse reject(Long id, String approverId, String remark) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        
        if (!doc.canApprove()) {
            throw new BusinessException("当前状态无法审批");
        }
        
        doc.setStatus(DocStatus.REJECTED.getValue());
        doc.setRemark(remark);
        docMapper.updateById(doc);
        
        log.info("审批驳回单据: docNo={}, approver={}", doc.getDocNo(), approverId);
        return findById(id);
    }

    @Override
    @Transactional
    public DocResponse execute(Long id) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        
        if (!doc.canExecute()) {
            throw new BusinessException("当前状态无法执行");
        }
        
        doc.setStatus(DocStatus.EXECUTING.getValue());
        doc.setExecuteTime(LocalDateTime.now());
        docMapper.updateById(doc);
        
        log.info("执行单据: docNo={}", doc.getDocNo());
        return findById(id);
    }

    @Override
    @Transactional
    public DocResponse complete(Long id) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        
        doc.setStatus(DocStatus.COMPLETED.getValue());
        doc.setCompleteTime(LocalDateTime.now());
        docMapper.updateById(doc);
        
        log.info("完成单据: docNo={}", doc.getDocNo());
        return findById(id);
    }

    @Override
    @Transactional
    public DocResponse cancel(Long id, String remark) {
        DocEntity doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("单据不存在");
        }
        
        if (!doc.canCancel()) {
            throw new BusinessException("当前状态无法取消");
        }
        
        doc.setStatus(DocStatus.CANCELLED.getValue());
        doc.setCancelTime(LocalDateTime.now());
        doc.setRemark(remark);
        docMapper.updateById(doc);
        
        log.info("取消单据: docNo={}", doc.getDocNo());
        return findById(id);
    }

    @Override
    public List<DocType> getAllDocTypes() {
        return Arrays.asList(DocType.values());
    }

    @Override
    public List<DocStatus> getAllDocStatus() {
        return Arrays.asList(DocStatus.values());
    }

    private void validateRequest(CreateDocRequest request) {
        if (request.getDocType() == null || request.getDocType().isBlank()) {
            throw new BusinessException("单据类型不能为空");
        }
        
        String docType = request.getDocType();
        boolean isPurchaseType = docType.startsWith("PURCHASE_");
        boolean isSaleType = docType.startsWith("SALE_");
        boolean isStockType = docType.startsWith("STOCK_");
        
        if (isPurchaseType || isSaleType) {
            if (request.getPartnerId() == null || request.getPartnerId().isBlank()) {
                throw new BusinessException(isPurchaseType ? "请选择供应商" : "请选择客户");
            }
        }
        if (isStockType) {
            if (request.getWarehouseId() == null) {
                throw new BusinessException("请选择仓库");
            }
        }
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("请添加商品明细");
        }
        for (int i = 0; i < request.getItems().size(); i++) {
            CreateDocRequest.DocItemRequest item = request.getItems().get(i);
            if (item.getGoodsId() == null) {
                throw new BusinessException("第" + (i + 1) + "行商品未选择");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("第" + (i + 1) + "行数量必须大于0");
            }
        }
    }

    private DocResponse buildResponse(DocEntity doc) {
        DocResponse response = DocResponse.fromEntity(doc);
        
        List<DocItemEntity> items = docItemMapper.selectByDocId(doc.getId());
        List<DocResponse.DocItemResponse> itemResponses = items.stream()
                .map(DocResponse.DocItemResponse::fromEntity)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        
        return response;
    }

    /**
     * 生成默认单据名称（单据类型+当天日期）
     */
    private String generateDefaultDocName(DocType docType) {
        String typeName = docType.getDescription();
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return typeName + dateStr;
    }

    /**
     * 获取 SKU+业务单位 对应的换算率
     * 优先使用请求传入的值，未传入则查 goods_sku_sale_unit 兜底
     */
    private BigDecimal getConversionRate(String skuCode, String unitCode, BigDecimal requestRate) {
        if (requestRate != null && requestRate.compareTo(BigDecimal.ZERO) > 0) {
            return requestRate;
        }
        if (skuCode == null || unitCode == null || unitCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        try {
            BigDecimal rate = jdbcTemplate.queryForObject(
                    "SELECT conversion_rate FROM psi_goods.goods_sku_sale_unit WHERE sku_code = ? AND symbol = ? LIMIT 1",
                    BigDecimal.class, skuCode, unitCode);
            if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                return rate;
            }
        } catch (Exception e) {
            log.warn("查询SKU[{}]单位[{}]换算率失败: {}", skuCode, unitCode, e.getMessage());
        }
        log.warn("未找到SKU[{}]销售单位[{}]的换算率，按1:1处理", skuCode, unitCode);
        return BigDecimal.ONE;
    }
}