package com.psi.purchase.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.purchase.dto.PurchaseInMainDTO;
import com.psi.purchase.dto.PurchaseOrderMainDTO;
import com.psi.purchase.dto.PurchaseReturnMainDTO;
import com.psi.purchase.service.PurchaseInMainService;
import com.psi.purchase.service.PurchaseOrderMainService;
import com.psi.purchase.service.PurchaseReturnMainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 采购流程完成监听器单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseProcessCompletedListenerTest {

    @Mock
    private DocFeignClient docFeignClient;

    @Mock
    private PurchaseOrderMainService purchaseOrderMainService;

    @Mock
    private PurchaseInMainService purchaseInMainService;

    @Mock
    private PurchaseReturnMainService purchaseReturnMainService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper springObjectMapper;

    @Mock
    private MessageIdempotencyService messageIdempotencyService;

    private PurchaseProcessCompletedListener listener;

    @BeforeEach
    void setUp() {
        when(messageIdempotencyService.execute(any(), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        listener = new PurchaseProcessCompletedListener(
                docFeignClient,
                purchaseOrderMainService,
                purchaseInMainService,
                purchaseReturnMainService,
                jdbcTemplate,
                springObjectMapper,
                messageIdempotencyService
        );
    }

    @Test
    void onProcessCompleted_shouldSaveAndAuditPurchaseOrder() throws Exception {
        DocFeignResponse doc = buildDoc("PURCHASE_ORDER");

        PurchaseOrderMainDTO saved = new PurchaseOrderMainDTO();
        saved.setId(1L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(purchaseOrderMainService.save(any(com.psi.purchase.dto.PurchaseOrderSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(purchaseOrderMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onProcessCompleted(buildMessage(doc));

        verify(purchaseOrderMainService).save(any(com.psi.purchase.dto.PurchaseOrderSaveDTO.class));
        verify(purchaseOrderMainService).audit(1L, 1);
    }

    @Test
    void onProcessCompleted_shouldSaveAndAuditPurchaseIn() throws Exception {
        DocFeignResponse doc = buildDoc("PURCHASE_IN");

        PurchaseInMainDTO saved = new PurchaseInMainDTO();
        saved.setId(2L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(purchaseInMainService.save(any(com.psi.purchase.dto.PurchaseInSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(purchaseInMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onProcessCompleted(buildMessage(doc));

        verify(purchaseInMainService).save(any(com.psi.purchase.dto.PurchaseInSaveDTO.class));
        verify(purchaseInMainService).audit(2L, 1);
    }

    @Test
    void onProcessCompleted_shouldSaveAndAuditPurchaseReturn() throws Exception {
        DocFeignResponse doc = buildDoc("PURCHASE_RETURN");

        PurchaseReturnMainDTO saved = new PurchaseReturnMainDTO();
        saved.setId(3L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(purchaseReturnMainService.save(any(com.psi.purchase.dto.PurchaseReturnSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(purchaseReturnMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onProcessCompleted(buildMessage(doc));

        verify(purchaseReturnMainService).save(any(com.psi.purchase.dto.PurchaseReturnSaveDTO.class));
        verify(purchaseReturnMainService).audit(3L, 1);
    }

    @Test
    void onProcessCompleted_shouldFallbackToFeign() throws Exception {
        DocFeignResponse doc = buildDoc("PURCHASE_ORDER");
        PurchaseOrderMainDTO saved = new PurchaseOrderMainDTO();
        saved.setId(1L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenThrow(new RuntimeException("parse error"));
        when(docFeignClient.findByDocNo("PO20240701001")).thenReturn(CommonResult.success(doc));
        when(purchaseOrderMainService.save(any(com.psi.purchase.dto.PurchaseOrderSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(purchaseOrderMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        MqCommonMessage<Map<String, Object>> message = new MqCommonMessage<>();
        Map<String, Object> data = new HashMap<>();
        data.put("bizId", "PO20240701001");
        message.setData(data);

        listener.onProcessCompleted(message);

        verify(docFeignClient).findByDocNo("PO20240701001");
        verify(purchaseOrderMainService).save(any(com.psi.purchase.dto.PurchaseOrderSaveDTO.class));
    }

    private DocFeignResponse buildDoc(String docType) {
        DocFeignResponse doc = new DocFeignResponse();
        doc.setDocType(docType);
        doc.setDocNo("PO20240701001");
        doc.setDocName("测试采购单据");
        doc.setPartnerId("1");
        doc.setPartnerCode("SUP001");
        doc.setPartnerName("供应商A");
        doc.setWarehouseCode("WH001");
        doc.setWarehouseName("仓库A");

        DocFeignResponse.DocFeignItemResponse item = new DocFeignResponse.DocFeignItemResponse();
        item.setGoodsCode("SP001");
        item.setGoodsName("商品A");
        item.setSkuCode("SKU001");
        item.setQuantity(new BigDecimal("10"));
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setGoodsUnit("瓶");
        doc.setItems(List.of(item));
        return doc;
    }

    private MqCommonMessage<Map<String, Object>> buildMessage(DocFeignResponse doc) {
        Map<String, Object> data = new HashMap<>();
        data.put("bizId", doc.getDocNo());
        data.put("docData", new ObjectMapper().valueToTree(doc).toString());

        MqCommonMessage<Map<String, Object>> message = new MqCommonMessage<>();
        message.setData(data);
        return message;
    }
}