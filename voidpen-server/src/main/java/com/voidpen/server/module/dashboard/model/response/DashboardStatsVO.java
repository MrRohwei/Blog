package com.voidpen.server.module.dashboard.model.response;

import lombok.Data;

@Data
public class DashboardStatsVO {

    private Long blogTotal;

    private Long todayViews;

    private Long commentTotal;

    private Long userTotal;
}
