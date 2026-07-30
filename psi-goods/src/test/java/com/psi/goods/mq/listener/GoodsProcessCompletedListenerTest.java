package com.psi.goods.mq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.feign.DocFeignClient;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.goods.entity.AdjustPriceMainEntity;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.service.AdjustPriceService;
import com.psi.goods.service.GoodsService;
import com.psi.goods.service.GoodsSkuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsProcessCompletedListenerTest {

    @Mock
    private DocFeignClient docFeignClient;

    @Mock
    private GoodsService goodsService;

    @Mock
    private GoodsSkuService goodsSkuService;

    @Mock
    private AdjustPriceService adjustPriceService;

    @Mock
    private ObjectMapper springObjectMapper;

    @Mock
    private MessageIdempotencyService messageIdempotencyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GoodsProcessCompletedListener listener;

    @BeforeEach
    void setUp() {
        when(messageIdempotencyService.execute(any(), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        listener = new GoodsProcessCompletedListener(
                docFeignClient,
                goodsService,
                goodsSkuService,
                adjustPriceService,
                springObjectMapper,
                messageIdempotencyService
        );
    }

    @Test
    void onProcessCompleted_shouldCreateGoodsAndSku() throws Exception {
        DocFeignResponse doc = buildGoodsCreateDoc();

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);

        listener.onProcessCompleted(buildMessage("SP001", doc));

        verify(goodsService).save(any());
        verify(goodsSkuService).save(any(GoodsSku.class));
        verify(docFeignClient, never()).findByDocNo(anyString());
    }

    @Test
    void onProcessCompleted_shouldHandleAdjustPrice() throws Exception {
        DocFeignResponse doc = buildAdjustPriceDoc();

        GoodsSku sku = new GoodsSku();
        sku.setSkuCode("SKU001");
        sku.setSalePrice(new BigDecimal("10.00"));

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(adjustPriceService.getByAdjustNo("TP20240701001")).thenReturn(null);
        when(goodsSkuService.getOne(any())).thenReturn(sku);
        when(adjustPriceService.saveAdjustPrice(any(AdjustPriceMainEntity.class), anyList()))
                .thenReturn(CommonResult.success(new AdjustPriceMainEntity()));

        listener.onProcessCompleted(buildMessage("TP20240701001", doc));

        verify(goodsSkuService).updateById(sku);
        verify(adjustPriceService).saveAdjustPrice(any(AdjustPriceMainEntity.class), anyList());
    }

    @Test
    void onProcessCompleted_shouldSkipExistingAdjustPrice() throws Exception {
        DocFeignResponse doc = buildAdjustPriceDoc();

        when(springObjectMapper.readValue(anyString(), eq(DocFeignResponse.class))).thenReturn(doc);
        when(adjustPriceService.getByAdjustNo("TP20240701001")).thenReturn(new AdjustPriceMainEntity());

        listener.onProcessCompleted(buildMessage("TP20240701001", doc));

        verify(adjustPriceService, never()).saveAdjustPrice(any(), anyList());
        verify(goodsSkuService, never()).updateById(any());
    }

    @Test
    void onProcessCompleted_shouldFallbackToFeignWhenDocDataMissing() {
        DocFeignResponse doc = buildGoodsCreateDoc();

        Map<String, Object> data = new HashMap<>();
        data.put("bizId", "SP001");

        MqCommonMessage<Map<String, Object>> message = new MqCommonMessage<>();
        message.setData(data);

        when(docFeignClient.findByDocNo("SP001")).thenReturn(CommonResult.success(doc));

        listener.onProcessCompleted(message);

        verify(docFeignClient).findByDocNo("SP001");
        verify(goodsService).save(any());
    }

    @Test
    void onProcessCompleted_shouldDoNothingWhenDocNotFound() {
        Map<String, Object> data = new HashMap<>();
        data.put("bizId", "SP001");

        MqCommonMessage<Map<String, Object>> message = new MqCommonMessage<>();
        message.setData(data);

        when(docFeignClient.findByDocNo("SP001")).thenReturn(CommonResult.fail("单据不存在"));

        assertDoesNotThrow(() -> listener.onProcessCompleted(message));
        verify(goodsService, never()).save(any());
    }

    private DocFeignResponse buildGoodsCreateDoc() {
        DocFeignResponse doc = new DocFeignResponse();
        doc.setDocType("GOODS_CREATE");
        doc.setPartnerCode("SP001");
        doc.setPartnerName("商品A");

        DocFeignResponse.DocFeignItemResponse item = new DocFeignResponse.DocFeignItemResponse();
        item.setGoodsSpec("规格1");
        item.setGoodsUnit("瓶");
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setQuantity(new BigDecimal("100"));
        doc.setItems(List.of(item));
        return doc;
    }

    private DocFeignResponse buildAdjustPriceDoc() {
        DocFeignResponse doc = new DocFeignResponse();
        doc.setDocType("ADJUST_PRICE");
        doc.setDocNo("TP20240701001");
        doc.setDocName("调价单");
        doc.setDocDate("2024-07-01");

        DocFeignResponse.DocFeignItemResponse item = new DocFeignResponse.DocFeignItemResponse();
        item.setGoodsCode("SP001");
        item.setSkuCode("SKU001");
        item.setGoodsName("商品A");
        item.setUnitPrice(new BigDecimal("15.00"));
        item.setQuantity(new BigDecimal("10"));
        doc.setItems(List.of(item));
        return doc;
    }

    private MqCommonMessage<Map<String, Object>> buildMessage(String bizId, DocFeignResponse doc) {
        Map<String, Object> data = new HashMap<>();
        data.put("bizId", bizId);
        data.put("docData", objectMapper.valueToTree(doc).toString());

        MqCommonMessage<Map<String, Object>> message = new MqCommonMessage<>();
        message.setData(data);
        return message;
    }
}