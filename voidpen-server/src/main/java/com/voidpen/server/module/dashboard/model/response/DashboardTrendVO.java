package com.voidpen.server.module.dashboard.model.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DashboardTrendVO {

    private List<String> dates = new ArrayList<>();

    private List<Long> views = new ArrayList<>();
}
