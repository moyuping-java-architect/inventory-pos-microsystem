package com.psi.system.service;

import com.psi.system.dto.DashboardDTO;
import com.psi.system.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 仪表盘数据服务
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardDTO getDashboardData() {
        DashboardDTO dto = new DashboardDTO();

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        dto.setTodayPurchaseAmount(dashboardMapper.selectTodayPurchaseAmount(today));
        dto.setTodaySaleAmount(dashboardMapper.selectTodaySaleAmount(today));
        dto.setStockSkuCount(dashboardMapper.selectStockSkuCount());

        BigDecimal monthSale = dashboardMapper.selectMonthSaleAmount(month);
        BigDecimal monthPurchase = dashboardMapper.selectMonthPurchaseAmount(month);
        dto.setMonthProfit(monthSale.subtract(monthPurchase));

        dto.setSaleTrend(buildSaleTrend());
        dto.setStockAlert(dashboardMapper.selectStockAlert());
        dto.setRecentOrders(dashboardMapper.selectRecentOrders());

        return dto;
    }

    private List<DashboardDTO.SaleTrendDTO> buildSaleTrend() {
        List<String> dates = new ArrayList<>(7);
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            dates.add(today.minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        List<DashboardDTO.SaleTrendDTO> dbResult = dashboardMapper.selectSaleTrend(dates);
        // 补全没有数据的日期
        List<DashboardDTO.SaleTrendDTO> result = new ArrayList<>();
        for (String date : dates) {
            DashboardDTO.SaleTrendDTO trend = dbResult.stream()
                    .filter(r -> date.equals(r.getDate()))
                    .findFirst()
                    .orElse(null);
            if (trend == null) {
                trend = new DashboardDTO.SaleTrendDTO();
                trend.setDate(date.substring(5));
                trend.setAmount(BigDecimal.ZERO);
                trend.setOrderCount(0);
            } else {
                trend.setDate(trend.getDate().substring(5));
            }
            result.add(trend);
        }
        return result;
    }
}
