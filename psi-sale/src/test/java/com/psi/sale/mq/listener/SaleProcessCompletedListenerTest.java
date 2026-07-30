package com.psi.sale.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.sale.dto.SaleOrderMainDTO;
import com.psi.sale.dto.SaleOutMainDTO;
import com.psi.sale.dto.SaleReturnMainDTO;
import com.psi.sale.service.CustomerPaymentService;
import com.psi.sale.service.SaleOrderMainService;
import com.psi.sale.service.SaleOutMainService;
import com.psi.sale.service.SaleOutSelfUseMainService;
import com.psi.sale.service.SaleReturnMainService;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaleProcessCompletedListenerTest {

    @Mock
    private DocFeignClient docFeignClient;

    @Mock
    private SaleOrderMainService saleOrderMainService;

    @Mock
    private SaleOutMainService saleOutMainService;

    @Mock
    private SaleOutSelfUseMainService saleOutSelfUseMainService;

    @Mock
    private SaleReturnMainService saleReturnMainService;

    @Mock
    private CustomerPaymentService customerPaymentService;

    @Mock
    private MqMessageFacade mqMessageFacade;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper springObjectMapper;

    @Mock
    private MessageIdempotencyService messageIdempotencyService;

    private SaleProcessCompletedListener listener;

    @BeforeEach
    void setUp() {
        when(messageIdempotencyService.execute(any(), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        listener = new SaleProcessCompletedListener(
                docFeignClient,
                saleOrderMainService,
                saleOutMainService,
                saleOutSelfUseMainService,
                saleReturnMainService,
                customerPaymentService,
                mqMessageFacade,
                jdbcTemplate,
                springObjectMapper,
                messageIdempotencyService
        );
    }

    @Test
    void onProcessCompleted_shouldSaveSaleOrderAndPayment() throws Exception {
        DocFeignResponse doc = buildDoc("SALE_ORDER");

        SaleOrderMainDTO saved = new SaleOrderMainDTO();
        saved.setId(1L);
        saved.setOrderNo("SO20240701001");

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(saleOrderMainService.save(any(com.psi.sale.dto.SaleOrderSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(saleOrderMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());
        when(customerPaymentService.save(any(com.psi.sale.dto.CustomerPaymentSaveDTO.class))).thenReturn(CommonResult.success());

        listener.onProcessCompleted(buildMessage(doc));

        verify(saleOrderMainService).save(any(com.psi.sale.dto.SaleOrderSaveDTO.class));
        verify(saleOrderMainService).audit(1L, 1);
        verify(customerPaymentService).save(any(com.psi.sale.dto.CustomerPaymentSaveDTO.class));
    }

    @Test
    void onProcessCompleted_shouldSaveAndAuditSaleOut() throws Exception {
        DocFeignResponse doc = buildDoc("SALE_OUT");

        SaleOutMainDTO saved = new SaleOutMainDTO();
        saved.setId(2L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(saleOutMainService.save(any(com.psi.sale.dto.SaleOutSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(saleOutMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onProcessCompleted(buildMessage(doc));

        verify(saleOutMainService).save(any(com.psi.sale.dto.SaleOutSaveDTO.class));
        verify(saleOutMainService).audit(2L, 2);
    }

    @Test
    void onProcessCompleted_shouldSaveAndAuditSaleReturn() throws Exception {
        DocFeignResponse doc = buildDoc("SALE_RETURN");

        SaleReturnMainDTO saved = new SaleReturnMainDTO();
        saved.setId(3L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(saleReturnMainService.save(any(com.psi.sale.dto.SaleReturnSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(saleReturnMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onProcessCompleted(buildMessage(doc));

        verify(saleReturnMainService).save(any(com.psi.sale.dto.SaleReturnSaveDTO.class));
        verify(saleReturnMainService).audit(3L, 1);
    }

    @Test
    void onProcessCompleted_shouldSendReleaseWhenRejected() throws Exception {
        DocFeignResponse doc = buildDoc("SALE_ORDER");

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);

        MqCommonMessage<Map<String, Object>> message = buildMessage(doc);
        message.getData().put("processStatus", 3);

        listener.onProcessCompleted(message);

        verify(mqMessageFacade).sendAsync(any(MqCommonMessage.class));
        verify(saleOrderMainService, never()).save(any(com.psi.sale.dto.SaleOrderSaveDTO.class));
    }

    @Test
    void onProcessCompleted_shouldFallbackToFeign() throws Exception {
        DocFeignResponse doc = buildDoc("SALE_ORDER");
        SaleOrderMainDTO saved = new SaleOrderMainDTO();
        saved.setId(1L);
        saved.setOrderNo("SO20240701001");

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenThrow(new RuntimeException("parse error"));
        when(docFeignClient.findByDocNo("SO20240701001")).thenReturn(CommonResult.success(doc));
        when(saleOrderMainService.save(any(com.psi.sale.dto.SaleOrderSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(saleOrderMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());
        when(customerPaymentService.save(any(com.psi.sale.dto.CustomerPaymentSaveDTO.class))).thenReturn(CommonResult.success());

        MqCommonMessage<Map<String, Object>> message = new MqCommonMessage<>();
        Map<String, Object> data = new HashMap<>();
        data.put("bizId", "SO20240701001");
        message.setData(data);

        listener.onProcessCompleted(message);

        verify(docFeignClient).findByDocNo("SO20240701001");
        verify(saleOrderMainService).save(any(com.psi.sale.dto.SaleOrderSaveDTO.class));
    }

    private DocFeignResponse buildDoc(String docType) {
        DocFeignResponse doc = new DocFeignResponse();
        doc.setDocType(docType);
        doc.setDocNo("SO20240701001");
        doc.setDocName("测试销售单据");
        doc.setPartnerId("1");
        doc.setPartnerCode("CUS001");
        doc.setPartnerName("客户A");
        doc.setTotalAmount(new BigDecimal("1000.00"));

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