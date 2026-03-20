package com.voidpen.server.module.system.model.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SystemMonitorOverviewVO {

    private LocalDateTime serverTime;

    private Long uptimeSeconds;

    private String applicationName;

    private String activeProfile;

    private ResourceStats resource = new ResourceStats();

    private JvmStats jvm = new JvmStats();

    private List<ServiceHealth> services = new ArrayList<>();

    @Data
    public static class ResourceStats {

        private Double cpuUsage;

        private Double memoryUsage;

        private Double diskUsage;
    }

    @Data
    public static class JvmStats {

        private Long heapUsedMb;

        private Long heapMaxMb;

        private Integer threadCount;

        private Long gcCount;

        private Long gcTimeMs;
    }

    @Data
    public static class ServiceHealth {

        private String name;

        private String status;

        private String message;

        private LocalDateTime lastCheckedAt;
    }
}
