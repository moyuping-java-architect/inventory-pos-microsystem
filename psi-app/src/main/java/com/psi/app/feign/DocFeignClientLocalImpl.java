package com.psi.app.feign;

import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.result.CommonResult;
import com.psi.order.dto.DocResponse;
import com.psi.order.service.DocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class DocFeignClientLocalImpl implements DocFeignClient {

    private final DocService docService;

    @Override
    public CommonResult<DocFeignResponse> findByDocNo(String docNo) {
        log.info("本地调用: findByDocNo, docNo={}", docNo);
        DocResponse doc = docService.findByDocNo(docNo);
        if (doc == null) {
            log.warn("本地调用 findByDocNo: 单据不存在, docNo={}", docNo);
            return CommonResult.fail("单据不存在");
        }
        DocFeignResponse response = convertToFeignResponse(doc);
        log.info("本地调用 findByDocNo 完成: docNo={}, id={}", docNo, response.getId());
        return CommonResult.success(response);
    }

    @Override
    public CommonResult<Void> complete(Long id) {
        log.info("本地调用: complete, id={}", id);
        DocResponse doc = docService.complete(id);
        if (doc == null) {
            log.warn("本地调用 complete: 单据不存在, id={}", id);
            return CommonResult.fail("单据不存在");
        }
        log.info("本地调用 complete 完成: id={}", id);
        return CommonResult.success();
    }

    private DocFeignResponse convertToFeignResponse(DocResponse doc) {
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
        response.setDocDate(formatDateTime(doc.getDocDate()));
        response.setDeliveryDate(formatDateTime(doc.getDeliveryDate()));
        return response;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}