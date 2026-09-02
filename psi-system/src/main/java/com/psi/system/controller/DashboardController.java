package com.psi.system.controller;

import com.psi.common.result.CommonResult;
import com.psi.system.dto.DashboardDTO;
import com.psi.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘数据接口
 */
@RestController
@RequestMapping("/psi/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public CommonResult<DashboardDTO> summary() {
        return CommonResult.success(dashboardService.getDashboardData());
    }
}
