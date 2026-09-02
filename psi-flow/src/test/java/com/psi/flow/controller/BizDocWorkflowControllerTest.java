package com.psi.flow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.exception.GlobalExceptionHandler;
import com.psi.flow.service.DocWorkflowService;
import com.psi.order.dto.CreateDocRequest;
import com.psi.order.dto.DocResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 业务单据工作流控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class BizDocWorkflowControllerTest {

    @Mock
    private DocWorkflowService docWorkflowService;

    @InjectMocks
    private BizDocWorkflowController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void submitPurchaseOrder_shouldReturnDto() throws Exception {
        CreateDocRequest request = buildRequest("SUP001", null);
        DocResponse response = buildResponse("PO20240701001", "PURCHASE_ORDER");

        when(docWorkflowService.createAndSubmit(any())).thenReturn(response);

        mockMvc.perform(post("/api/doc/purchase-order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.docNo").value("PO20240701001"));
    }

    @Test
    void submitPurchaseIn_shouldRequireWarehouse() throws Exception {
        CreateDocRequest request = buildRequest("SUP001", null);

        mockMvc.perform(post("/api/doc/purchase-in/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("请选择入库仓库"));
    }

    @Test
    void submitSaleOrder_shouldReturnDto() throws Exception {
        CreateDocRequest request = buildRequest("CUS001", null);
        DocResponse response = buildResponse("SO20240701001", "SALE_ORDER");

        when(docWorkflowService.createAndSubmit(any())).thenReturn(response);

        mockMvc.perform(post("/api/doc/sale-order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.docNo").value("SO20240701001"));
    }

    @Test
    void submitSaleOut_shouldRequireWarehouse() throws Exception {
        CreateDocRequest request = buildRequest("CUS001", null);

        mockMvc.perform(post("/api/doc/sale-out/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("请选择出库仓库"));
    }

    @Test
    void submitStockCheck_shouldRequireWarehouse() throws Exception {
        CreateDocRequest request = buildRequest(null, null);

        mockMvc.perform(post("/api/doc/stock-check/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("请选择盘点仓库"));
    }

    @Test
    void submitStockOverflow_shouldReturnDto() throws Exception {
        CreateDocRequest request = buildRequest(null, 1L);
        DocResponse response = buildResponse("SOV20240701001", "STOCK_OVERFLOW");

        when(docWorkflowService.createAndSubmit(any())).thenReturn(response);

        mockMvc.perform(post("/api/doc/stock-overflow/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.docNo").value("SOV20240701001"));
    }

    @Test
    void submitAdjustPrice_shouldReturnDto() throws Exception {
        CreateDocRequest request = buildRequest(null, null);
        DocResponse response = buildResponse("TP20240701001", "ADJUST_PRICE");

        when(docWorkflowService.createAndSubmit(any())).thenReturn(response);

        mockMvc.perform(post("/api/doc/adjust-price/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.docNo").value("TP20240701001"));
    }

    @Test
    void submit_shouldRejectEmptyItems() throws Exception {
        CreateDocRequest request = new CreateDocRequest();
        request.setPartnerId("SUP001");
        request.setItems(List.of());

        mockMvc.perform(post("/api/doc/purchase-order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("请添加商品明细"));
    }

    @Test
    void submit_shouldRejectZeroQuantity() throws Exception {
        CreateDocRequest request = buildRequest("SUP001", null);
        request.getItems().get(0).setQuantity(BigDecimal.ZERO);

        mockMvc.perform(post("/api/doc/purchase-order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("第1行数量必须大于0"));
    }

    private CreateDocRequest buildRequest(String partnerId, Long warehouseId) {
        CreateDocRequest request = new CreateDocRequest();
        request.setPartnerId(partnerId);
        request.setWarehouseId(warehouseId);

        CreateDocRequest.DocItemRequest item = new CreateDocRequest.DocItemRequest();
        item.setGoodsCode("SP001");
        item.setQuantity(new BigDecimal("10"));
        item.setUnitPrice(new BigDecimal("100.00"));
        request.setItems(List.of(item));
        return request;
    }

    private DocResponse buildResponse(String docNo, String docType) {
        DocResponse response = new DocResponse();
        response.setDocNo(docNo);
        response.setDocType(docType);
        return response;
    }
}
