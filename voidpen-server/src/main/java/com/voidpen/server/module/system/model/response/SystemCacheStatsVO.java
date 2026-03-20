package com.voidpen.server.module.system.model.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SystemCacheStatsVO {

    private String redisVersion;

    private Integer connectedClients;

    private Long usedMemoryBytes;

    private String usedMemoryHuman;

    private Long keyspaceHits;

    private Long keyspaceMisses;

    private Double hitRate;

    private List<CachePrefixStats> prefixes = new ArrayList<>();

    @Data
    public static class CachePrefixStats {

        private String prefix;

        private Long keyCount;
    }
}
