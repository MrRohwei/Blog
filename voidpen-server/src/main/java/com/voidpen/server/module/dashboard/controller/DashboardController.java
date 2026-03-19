package com.voidpen.server.module.dashboard.controller;

import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.dashboard.model.response.DashboardStatsVO;
import com.voidpen.server.module.dashboard.model.response.DashboardTrendVO;
import com.voidpen.server.module.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据看板")
@RestController
@RequestMapping("/admin/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取概览统计")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.success(dashboardService.getStats());
    }

    @Operation(summary = "获取近30天访问趋势")
    @GetMapping("/trend")
    public Result<DashboardTrendVO> trend() {
        return Result.success(dashboardService.getTrend());
    }
}
