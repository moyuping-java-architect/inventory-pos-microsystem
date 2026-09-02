package com.psi.system.controller;

import com.psi.system.dto.DashboardDTO;
import com.psi.system.service.DashboardService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 仪表盘控制器单元测试（standalone MockMvc）
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    void summary_shouldReturnDashboardData() throws Exception {
        DashboardDTO dto = new DashboardDTO();
        dto.setTodayPurchaseAmount(new BigDecimal("5000.00"));
        dto.setTodaySaleAmount(new BigDecimal("8000.00"));
        dto.setStockSkuCount(100L);
        dto.setMonthProfit(new BigDecimal("15000.00"));
        dto.setSaleTrend(List.of());
        dto.setStockAlert(List.of());
        dto.setRecentOrders(List.of());

        when(dashboardService.getDashboardData()).thenReturn(dto);

        mockMvc.perform(get("/psi/admin/dashboard/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.todayPurchaseAmount").value(5000.00))
                .andExpect(jsonPath("$.data.todaySaleAmount").value(8000.00))
                .andExpect(jsonPath("$.data.stockSkuCount").value(100))
                .andExpect(jsonPath("$.data.monthProfit").value(15000.00));
    }
}
