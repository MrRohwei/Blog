package com.voidpen.server.module.dashboard.service;

import com.voidpen.server.module.dashboard.model.response.DashboardStatsVO;
import com.voidpen.server.module.dashboard.model.response.DashboardTrendVO;

public interface DashboardService {

    DashboardStatsVO getStats();

    DashboardTrendVO getTrend();
}
