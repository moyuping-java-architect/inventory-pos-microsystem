package com.psi.flow.service;

import com.psi.common.feign.DocFeignResponse;
import com.psi.common.feign.DocFeignResponse.DocFeignItemResponse;
import com.psi.order.dto.DocResponse;
import com.psi.order.dto.DocResponse.DocItemResponse;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单据转换服务
 *
 * <p>将内部 {@link DocResponse} 转换为跨服务传输的 {@link DocFeignResponse}</p>
 */
@Service
public class DocConvertService {

    /**
     * 把 DocResponse 转成 DocFeignResponse
     */
    public DocFeignResponse convertToFeignResponse(DocResponse doc) {
        if (doc == null) {
            return null;
        }
        DocFeignResponse response = new DocFeignResponse();
        response.setId(doc.getId());
        response.setDocNo(doc.getDocNo());
        response.setDocType(doc.getDocType());
        response.setDocName(doc.getDocName());
        response.setDocTypeDesc(doc.getDocTypeDesc());
        response.setStatus(doc.getStatus());
        response.setCreatorId(doc.getCreatorId());
        response.setCreatorName(doc.getCreatorName());
        response.setPartnerId(doc.getPartnerId());
        response.setPartnerCode(doc.getPartnerCode());
        response.setPartnerName(doc.getPartnerName());
        response.setWarehouseId(doc.getWarehouseId());
        response.setWarehouseCode(doc.getWarehouseCode());
        response.setWarehouseName(doc.getWarehouseName());
        response.setOrderNo(doc.getOrderNo());
        response.setSaleType(doc.getSaleType());
        response.setPaymentType(doc.getPaymentType());
        response.setCurrencyCode(doc.getCurrencyCode());
        response.setExchangeRate(doc.getExchangeRate());
        response.setTotalAmount(doc.getTotalAmount());
        response.setTaxAmount(doc.getTaxAmount());
        response.setDiscountAmount(doc.getDiscountAmount());
        response.setPayAmount(doc.getPayAmount());
        response.setItemCount(doc.getItemCount());
        response.setRemark(doc.getRemark());
        response.setExtJson(doc.getExtJson());
        if (doc.getDocDate() != null) {
            response.setDocDate(doc.getDocDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (doc.getDeliveryDate() != null) {
            response.setDeliveryDate(doc.getDeliveryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        if (doc.getItems() != null) {
            List<DocFeignItemResponse> items = doc.getItems().stream()
                    .map(this::convertItem)
                    .collect(Collectors.toList());
            response.setItems(items);
        }
        return response;
    }

    private DocFeignItemResponse convertItem(DocItemResponse item) {
        DocFeignItemResponse result = new DocFeignItemResponse();
        result.setId(item.getId());
        result.setGoodsId(item.getGoodsId());
        result.setGoodsCode(item.getGoodsCode());
        result.setSkuCode(item.getSkuCode());
        result.setSkuName(item.getSkuName());
        result.setGoodsName(item.getGoodsName());
        result.setGoodsSpec(item.getGoodsSpec());
        result.setGoodsUnit(item.getGoodsUnit());
        result.setConversionRate(item.getConversionRate());
        result.setUnitPrice(item.getUnitPrice());
        result.setQuantity(item.getQuantity());
        result.setAmount(item.getAmount());
        result.setTaxRate(item.getTaxRate());
        result.setTaxAmount(item.getTaxAmount());
        result.setDiscountRate(item.getDiscountRate());
        result.setDiscountAmount(item.getDiscountAmount());
        result.setBatchNo(item.getBatchNo());
        result.setExpiryDate(item.getExpiryDate());
        result.setRemark(item.getRemark());
        return result;
    }
}
