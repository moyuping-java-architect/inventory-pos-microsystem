package com.psi.system.service;

import com.psi.system.dto.DashboardDTO;
import com.psi.system.mapper.DashboardMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 仪表盘服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardMapper dashboardMapper;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardData_shouldAggregateAllStats() {
        when(dashboardMapper.selectTodayPurchaseAmount(anyString())).thenReturn(new BigDecimal("5000.00"));
        when(dashboardMapper.selectTodaySaleAmount(anyString())).thenReturn(new BigDecimal("8000.00"));
        when(dashboardMapper.selectStockSkuCount()).thenReturn(100L);
        when(dashboardMapper.selectMonthPurchaseAmount(anyString())).thenReturn(new BigDecimal("20000.00"));
        when(dashboardMapper.selectMonthSaleAmount(anyString())).thenReturn(new BigDecimal("35000.00"));
        when(dashboardMapper.selectSaleTrend(anyList())).thenReturn(List.of());
        when(dashboardMapper.selectStockAlert()).thenReturn(List.of());
        when(dashboardMapper.selectRecentOrders()).thenReturn(List.of());

        DashboardDTO dto = dashboardService.getDashboardData();

        assertNotNull(dto);
        assertEquals(new BigDecimal("5000.00"), dto.getTodayPurchaseAmount());
        assertEquals(new BigDecimal("8000.00"), dto.getTodaySaleAmount());
        assertEquals(100L, dto.getStockSkuCount());
        assertEquals(new BigDecimal("15000.00"), dto.getMonthProfit());
        assertNotNull(dto.getSaleTrend());
        assertEquals(7, dto.getSaleTrend().size());
    }

    @Test
    void getDashboardData_shouldFillMissingTrendDates() {
        when(dashboardMapper.selectTodayPurchaseAmount(anyString())).thenReturn(BigDecimal.ZERO);
        when(dashboardMapper.selectTodaySaleAmount(anyString())).thenReturn(BigDecimal.ZERO);
        when(dashboardMapper.selectStockSkuCount()).thenReturn(0L);
        when(dashboardMapper.selectMonthPurchaseAmount(anyString())).thenReturn(BigDecimal.ZERO);
        when(dashboardMapper.selectMonthSaleAmount(anyString())).thenReturn(BigDecimal.ZERO);
        when(dashboardMapper.selectSaleTrend(anyList())).thenReturn(List.of());
        when(dashboardMapper.selectStockAlert()).thenReturn(List.of());
        when(dashboardMapper.selectRecentOrders()).thenReturn(List.of());

        DashboardDTO dto = dashboardService.getDashboardData();

        assertEquals(7, dto.getSaleTrend().size());
        assertEquals(BigDecimal.ZERO, dto.getSaleTrend().get(0).getAmount());
        assertEquals(0, dto.getSaleTrend().get(0).getOrderCount());
    }
}
