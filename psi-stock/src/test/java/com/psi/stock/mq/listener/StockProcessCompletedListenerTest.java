package com.psi.stock.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.stock.dto.*;
import com.psi.stock.service.*;
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
class StockProcessCompletedListenerTest {

    @Mock
    private DocFeignClient docFeignClient;

    @Mock
    private StockLossMainService stockLossMainService;

    @Mock
    private StockOverMainService stockOverMainService;

    @Mock
    private StockCheckMainService stockCheckMainService;

    @Mock
    private StockTransferMainService stockTransferMainService;

    @Mock
    private InventoryInitMainService inventoryInitMainService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper springObjectMapper;

    @Mock
    private MessageIdempotencyService messageIdempotencyService;

    private StockProcessCompletedListener listener;

    @BeforeEach
    void setUp() {
        when(messageIdempotencyService.execute(any(), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        listener = new StockProcessCompletedListener(
                docFeignClient,
                stockLossMainService,
                stockOverMainService,
                stockCheckMainService,
                stockTransferMainService,
                inventoryInitMainService,
                jdbcTemplate,
                springObjectMapper,
                messageIdempotencyService
        );
    }

    @Test
    void onLossCompleted_shouldSaveAndAudit() throws Exception {
        DocFeignResponse doc = buildDoc("STOCK_LOSS");
        StockLossMainDTO saved = new StockLossMainDTO();
        saved.setId(1L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(stockLossMainService.save(any(com.psi.stock.dto.StockLossSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(stockLossMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onLossCompleted(buildMessage(doc));

        verify(stockLossMainService).save(any(com.psi.stock.dto.StockLossSaveDTO.class));
        verify(stockLossMainService).audit(1L, 1);
    }

    @Test
    void onOverflowCompleted_shouldSaveAndAudit() throws Exception {
        DocFeignResponse doc = buildDoc("STOCK_OVERFLOW");
        StockOverMainDTO saved = new StockOverMainDTO();
        saved.setId(2L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(stockOverMainService.save(any(com.psi.stock.dto.StockOverSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(stockOverMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onOverflowCompleted(buildMessage(doc));

        verify(stockOverMainService).save(any(com.psi.stock.dto.StockOverSaveDTO.class));
        verify(stockOverMainService).audit(2L, 1);
    }

    @Test
    void onCheckCompleted_shouldSaveAndAudit() throws Exception {
        DocFeignResponse doc = buildDoc("STOCK_CHECK");
        StockCheckMainDTO saved = new StockCheckMainDTO();
        saved.setId(3L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(stockCheckMainService.save(any(com.psi.stock.dto.StockCheckSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(stockCheckMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onCheckCompleted(buildMessage(doc));

        verify(stockCheckMainService).save(any(com.psi.stock.dto.StockCheckSaveDTO.class));
        verify(stockCheckMainService).audit(3L, 1);
    }

    @Test
    void onStockCompleted_shouldHandleTransferAndInventoryInit() throws Exception {
        DocFeignResponse transferDoc = buildDoc("STOCK_TRANSFER");
        transferDoc.setExtJson("{\"toWarehouseCode\":\"WH002\",\"toWarehouseName\":\"仓库B\"}");
        StockTransferMainDTO transferSaved = new StockTransferMainDTO();
        transferSaved.setId(4L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class)))
                .thenReturn(transferDoc)
                .thenReturn(buildDoc("INVENTORY_INIT"));
        when(stockTransferMainService.save(any(com.psi.stock.dto.StockTransferSaveDTO.class))).thenReturn(CommonResult.success(transferSaved));
        when(stockTransferMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onStockCompleted(buildMessage(transferDoc));

        verify(stockTransferMainService).save(any(com.psi.stock.dto.StockTransferSaveDTO.class));
        verify(stockTransferMainService).audit(4L, 1);

        DocFeignResponse initDoc = buildDoc("INVENTORY_INIT");
        when(inventoryInitMainService.save(any(com.psi.stock.dto.InventoryInitSaveDTO.class))).thenReturn(CommonResult.success(5L));
        when(inventoryInitMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        listener.onStockCompleted(buildMessage(initDoc));

        verify(inventoryInitMainService).save(any(com.psi.stock.dto.InventoryInitSaveDTO.class));
        verify(inventoryInitMainService).audit(5L, 1);
    }

    @Test
    void onStockCompleted_shouldFallbackToFeign() throws Exception {
        DocFeignResponse doc = buildDoc("STOCK_LOSS");
        StockLossMainDTO saved = new StockLossMainDTO();
        saved.setId(1L);

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenThrow(new RuntimeException("parse error"));
        when(docFeignClient.findByDocNo("SK20240701001")).thenReturn(CommonResult.success(doc));
        when(stockLossMainService.save(any(com.psi.stock.dto.StockLossSaveDTO.class))).thenReturn(CommonResult.success(saved));
        when(stockLossMainService.audit(anyLong(), anyInt())).thenReturn(CommonResult.success());

        MqCommonMessage<Map<String, Object>> message = new MqCommonMessage<>();
        Map<String, Object> data = new HashMap<>();
        data.put("bizId", "SK20240701001");
        message.setData(data);

        listener.onLossCompleted(message);

        verify(docFeignClient).findByDocNo("SK20240701001");
        verify(stockLossMainService).save(any(com.psi.stock.dto.StockLossSaveDTO.class));
    }

    private DocFeignResponse buildDoc(String docType) {
        DocFeignResponse doc = new DocFeignResponse();
        doc.setDocType(docType);
        doc.setDocNo("SK20240701001");
        doc.setDocName("测试库存单据");
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