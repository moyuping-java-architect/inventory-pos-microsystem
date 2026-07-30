package com.psi.goods.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.result.CommonResult;
import com.psi.goods.entity.AdjustPriceItemEntity;
import com.psi.goods.entity.AdjustPriceMainEntity;
import com.psi.goods.service.AdjustPriceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商品调价单控制器单元测试（standalone MockMvc）
 */
@ExtendWith(MockitoExtension.class)
class AdjustPriceControllerTest {

    @Mock
    private AdjustPriceService adjustPriceService;

    @InjectMocks
    private AdjustPriceController adjustPriceController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adjustPriceController).build();
    }

    @Test
    void save_shouldReturnSavedAdjustPrice() throws Exception {
        AdjustPriceMainEntity main = new AdjustPriceMainEntity();
        main.setId(1L);
        main.setAdjustNo("TP20240701001");

        AdjustPriceController.AdjustPriceRequest request = new AdjustPriceController.AdjustPriceRequest();
        request.setMain(main);
        request.setItems(List.of());

        when(adjustPriceService.saveAdjustPrice(any(), any())).thenReturn(CommonResult.success(main));

        mockMvc.perform(post("/psi/goods/adjust-price/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.adjustNo").value("TP20240701001"));
    }

    @Test
    void getById_shouldReturnDetail() throws Exception {
        AdjustPriceMainEntity main = new AdjustPriceMainEntity();
        main.setId(1L);
        main.setAdjustNo("TP20240701001");

        when(adjustPriceService.getDetailById(1L)).thenReturn(CommonResult.success(main));

        mockMvc.perform(get("/psi/goods/adjust-price/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.adjustNo").value("TP20240701001"));
    }

    @Test
    void audit_shouldReturnSuccess() throws Exception {
        when(adjustPriceService.audit(1L)).thenReturn(CommonResult.success());

        mockMvc.perform(post("/psi/goods/adjust-price/audit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
