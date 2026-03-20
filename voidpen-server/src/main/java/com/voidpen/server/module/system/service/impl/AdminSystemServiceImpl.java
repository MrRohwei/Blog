package com.voidpen.server.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voidpen.server.common.constant.RedisKeys;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.module.system.entity.TSystemConfig;
import com.voidpen.server.module.system.entity.TSystemOperationLog;
import com.voidpen.server.module.system.enums.SystemConfigValueType;
import com.voidpen.server.module.system.mapper.SystemConfigMapper;
import com.voidpen.server.module.system.mapper.SystemOperationLogMapper;
import com.voidpen.server.module.system.model.request.UpdateSystemConfigRequest;
import com.voidpen.server.module.system.model.response.CacheOperateResultVO;
import com.voidpen.server.module.system.model.response.SystemCacheStatsVO;
import com.voidpen.server.module.system.model.response.SystemConfigItemVO;
import com.voidpen.server.module.system.model.response.SystemMonitorOverviewVO;
import com.voidpen.server.module.system.service.AdminSystemService;
import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSystemServiceImpl implements AdminSystemService {

    private static final String STATUS_UP = "UP";

    private static final String STATUS_DOWN = "DOWN";

    private static final long CLEAR_ALL_COOLDOWN_SECONDS = 30L;

    private static final String CACHE_PATTERN_ALL = "voidpen:*";

    private static final Pattern SENSITIVE_KEY_PATTERN =
        Pattern.compile("(?i)(secret|password|token|credential)");

    private static final List<String> CONFIG_GROUPS = List.of("site", "seo", "comment", "upload", "feature");

    private static final List<String> CACHE_PREFIXES = List.of(
        RedisKeys.BLOG_DETAIL,
        RedisKeys.BLOG_VIEWS,
        RedisKeys.BLOG_LIKE,
        RedisKeys.TOKEN_BLACKLIST,
        RedisKeys.SYSTEM_CONFIG,
        "voidpen:category:"
    );

    private final SystemConfigMapper systemConfigMapper;

    private final SystemOperationLogMapper systemOperationLogMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper;

    private final Environment environment;

    @Override
    public List<String> listConfigGroups() {
        LinkedHashSet<String> groups = new LinkedHashSet<>(CONFIG_GROUPS);
        systemConfigMapper.selectList(
            new LambdaQueryWrapper<TSystemConfig>()
                .select(TSystemConfig::getConfigGroup)
                .groupBy(TSystemConfig::getConfigGroup)
                .orderByAsc(TSystemConfig::getConfigGroup)
        ).stream()
            .map(TSystemConfig::getConfigGroup)
            .filter(StringUtils::hasText)
            .forEach(groups::add);
        return new ArrayList<>(groups);
    }

    @Override
    public List<SystemConfigItemVO> listConfigByGroup(String configGroup) {
        String normalizedGroup = normalizeGroup(configGroup);
        return systemConfigMapper.selectList(
            new LambdaQueryWrapper<TSystemConfig>()
                .eq(TSystemConfig::getConfigGroup, normalizedGroup)
                .orderByAsc(TSystemConfig::getId)
        ).stream().map(this::toConfigItemVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(UpdateSystemConfigRequest request) {
        String normalizedGroup = normalizeGroup(request.getConfigGroup());
        String normalizedKey = normalizeKey(request.getConfigKey());

        TSystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<TSystemConfig>()
                .eq(TSystemConfig::getConfigGroup, normalizedGroup)
                .eq(TSystemConfig::getConfigKey, normalizedKey)
                .last("LIMIT 1")
        );

        if (config == null) {
            throw new BusinessException(ErrorCode.SYSTEM_CONFIG_NOT_FOUND);
        }
        if (!Objects.equals(config.getEditable(), 1)) {
            throw new BusinessException(ErrorCode.SYSTEM_CONFIG_NOT_EDITABLE);
        }

        String normalizedValue = normalizeValue(request.getConfigValue(), config.getValueType());
        config.setConfigValue(normalizedValue);
        systemConfigMapper.updateById(config);

        redisTemplate.opsForValue().set(buildConfigCacheKey(normalizedGroup, normalizedKey), normalizedValue);
    }

    @Override
    public SystemMonitorOverviewVO getMonitorOverview() {
        SystemMonitorOverviewVO overview = new SystemMonitorOverviewVO();
        LocalDateTime now = LocalDateTime.now();

        overview.setServerTime(now);
        overview.setApplicationName(resolveApplicationName());
        overview.setActiveProfile(resolveActiveProfile());
        overview.setUptimeSeconds(resolveUptimeSeconds());

        overview.setResource(resolveResourceStats());
        overview.setJvm(resolveJvmStats());
        overview.getServices().add(resolveServerService(now));
        overview.getServices().add(resolveMysqlService(now));
        overview.getServices().add(resolveRedisService(now));
        return overview;
    }

    @Override
    public SystemCacheStatsVO getCacheStats() {
        SystemCacheStatsVO stats = new SystemCacheStatsVO();
        Properties serverInfo = getRedisInfo("server");
        Properties clientsInfo = getRedisInfo("clients");
        Properties memoryInfo = getRedisInfo("memory");
        Properties statsInfo = getRedisInfo("stats");

        stats.setRedisVersion(getString(serverInfo, "redis_version"));
        stats.setConnectedClients(toInt(clientsInfo.get("connected_clients")));
        stats.setUsedMemoryBytes(toLong(memoryInfo.get("used_memory")));
        stats.setUsedMemoryHuman(getString(memoryInfo, "used_memory_human"));
        stats.setKeyspaceHits(toLong(statsInfo.get("keyspace_hits")));
        stats.setKeyspaceMisses(toLong(statsInfo.get("keyspace_misses")));

        long totalHitAndMiss = stats.getKeyspaceHits() + stats.getKeyspaceMisses();
        stats.setHitRate(
            totalHitAndMiss <= 0 ? 0D : roundPercent((double) stats.getKeyspaceHits() * 100D / totalHitAndMiss)
        );

        CACHE_PREFIXES.forEach(prefix -> {
            SystemCacheStatsVO.CachePrefixStats prefixStats = new SystemCacheStatsVO.CachePrefixStats();
            prefixStats.setPrefix(prefix);
            prefixStats.setKeyCount(countKeys(prefix + "*"));
            stats.getPrefixes().add(prefixStats);
        });
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CacheOperateResultVO evictByPrefix(Long operatorId, String prefix) {
        String normalizedPrefix = normalizeCachePrefix(prefix);
        long deletedCount = deleteByPattern(normalizedPrefix + "*");
        saveOperationLog(operatorId, "CACHE_EVICT_PREFIX", normalizedPrefix + "*", "deleted=" + deletedCount);
        return buildCacheResult(normalizedPrefix + "*", deletedCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CacheOperateResultVO clearAllCache(Long operatorId) {
        long now = Instant.now().getEpochSecond();
        long lastAction = toLong(redisTemplate.opsForValue().get(RedisKeys.SYSTEM_CACHE_CLEAR_ALL_COOLDOWN));
        if (now - lastAction < CLEAR_ALL_COOLDOWN_SECONDS) {
            throw new BusinessException(ErrorCode.SYSTEM_CACHE_CLEAR_TOO_FREQUENT);
        }

        long deletedCount = deleteByPattern(CACHE_PATTERN_ALL);
        redisTemplate
            .opsForValue()
            .set(
                RedisKeys.SYSTEM_CACHE_CLEAR_ALL_COOLDOWN,
                now,
                Duration.ofSeconds(CLEAR_ALL_COOLDOWN_SECONDS)
            );
        saveOperationLog(operatorId, "CACHE_CLEAR_ALL", CACHE_PATTERN_ALL, "deleted=" + deletedCount);
        return buildCacheResult(CACHE_PATTERN_ALL, deletedCount);
    }

    private SystemConfigItemVO toConfigItemVO(TSystemConfig config) {
        boolean sensitive = isSensitiveKey(config.getConfigKey());
        SystemConfigItemVO vo = new SystemConfigItemVO();
        vo.setId(config.getId());
        vo.setConfigGroup(config.getConfigGroup());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(sensitive ? maskValue(config.getConfigValue()) : config.getConfigValue());
        vo.setValueType(SystemConfigValueType.fromCode(config.getValueType()).getCode());
        vo.setDescription(config.getDescription());
        vo.setEditable(config.getEditable());
        vo.setSensitive(sensitive);
        return vo;
    }

    private String resolveApplicationName() {
        return environment.getProperty("spring.application.name", "voidpen-server");
    }

    private String resolveActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return activeProfiles[0];
    }

    private Long resolveUptimeSeconds() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        return runtime.getUptime() / 1000;
    }

    private SystemMonitorOverviewVO.ResourceStats resolveResourceStats() {
        SystemMonitorOverviewVO.ResourceStats stats = new SystemMonitorOverviewVO.ResourceStats();
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpuLoad = osBean == null ? 0D : osBean.getSystemCpuLoad();
        stats.setCpuUsage(roundPercent(Math.max(cpuLoad, 0D) * 100D));

        long totalMemory = osBean == null ? 0L : osBean.getTotalMemorySize();
        long freeMemory = osBean == null ? 0L : osBean.getFreeMemorySize();
        stats.setMemoryUsage(calculateUsage(totalMemory - freeMemory, totalMemory));

        File root = resolveRootPath();
        long totalSpace = root.getTotalSpace();
        long usedSpace = totalSpace - root.getUsableSpace();
        stats.setDiskUsage(calculateUsage(usedSpace, totalSpace));
        return stats;
    }

    private SystemMonitorOverviewVO.JvmStats resolveJvmStats() {
        SystemMonitorOverviewVO.JvmStats stats = new SystemMonitorOverviewVO.JvmStats();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        stats.setHeapUsedMb(heapUsed / 1024 / 1024);
        stats.setHeapMaxMb(heapMax <= 0 ? 0L : heapMax / 1024 / 1024);
        stats.setThreadCount(threadMXBean.getThreadCount());

        long gcCount = 0L;
        long gcTimeMs = 0L;
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (gcBean.getCollectionCount() > 0) {
                gcCount += gcBean.getCollectionCount();
            }
            if (gcBean.getCollectionTime() > 0) {
                gcTimeMs += gcBean.getCollectionTime();
            }
        }
        stats.setGcCount(gcCount);
        stats.setGcTimeMs(gcTimeMs);
        return stats;
    }

    private SystemMonitorOverviewVO.ServiceHealth resolveServerService(LocalDateTime now) {
        SystemMonitorOverviewVO.ServiceHealth health = new SystemMonitorOverviewVO.ServiceHealth();
        health.setName("server");
        health.setStatus(STATUS_UP);
        health.setMessage("running");
        health.setLastCheckedAt(now);
        return health;
    }

    private SystemMonitorOverviewVO.ServiceHealth resolveMysqlService(LocalDateTime now) {
        SystemMonitorOverviewVO.ServiceHealth health = new SystemMonitorOverviewVO.ServiceHealth();
        health.setName("mysql");
        health.setLastCheckedAt(now);
        try {
            Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (value != null && value == 1) {
                health.setStatus(STATUS_UP);
                health.setMessage("ok");
            } else {
                health.setStatus(STATUS_DOWN);
                health.setMessage("response unexpected");
            }
        } catch (Exception ex) {
            health.setStatus(STATUS_DOWN);
            health.setMessage(firstLine(ex.getMessage()));
        }
        return health;
    }

    private SystemMonitorOverviewVO.ServiceHealth resolveRedisService(LocalDateTime now) {
        SystemMonitorOverviewVO.ServiceHealth health = new SystemMonitorOverviewVO.ServiceHealth();
        health.setName("redis");
        health.setLastCheckedAt(now);
        try {
            String pong = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
            if ("PONG".equalsIgnoreCase(pong)) {
                health.setStatus(STATUS_UP);
                health.setMessage("ok");
            } else {
                health.setStatus(STATUS_DOWN);
                health.setMessage("response unexpected");
            }
        } catch (Exception ex) {
            health.setStatus(STATUS_DOWN);
            health.setMessage(firstLine(ex.getMessage()));
        }
        return health;
    }

    private CacheOperateResultVO buildCacheResult(String scope, long deletedCount) {
        CacheOperateResultVO result = new CacheOperateResultVO();
        result.setScope(scope);
        result.setDeletedCount(deletedCount);
        return result;
    }

    private void saveOperationLog(Long operatorId, String operationType, String targetScope, String detail) {
        TSystemOperationLog logEntity = new TSystemOperationLog();
        logEntity.setOperatorId(operatorId);
        logEntity.setOperationType(operationType);
        logEntity.setTargetScope(targetScope);
        logEntity.setDetail(detail);
        logEntity.setCreatedAt(LocalDateTime.now());
        systemOperationLogMapper.insert(logEntity);
    }

    private String normalizeGroup(String value) {
        String normalized = StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "配置分组不能为空");
        }
        return normalized;
    }

    private String normalizeKey(String value) {
        String normalized = value == null ? "" : value.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "配置键不能为空");
        }
        return normalized;
    }

    private String normalizeValue(String value, String valueTypeCode) {
        String normalized = value == null ? "" : value.trim();
        SystemConfigValueType valueType = SystemConfigValueType.fromCode(valueTypeCode);

        try {
            return switch (valueType) {
                case STRING -> normalized;
                case NUMBER -> new BigDecimal(normalized).stripTrailingZeros().toPlainString();
                case BOOLEAN -> normalizeBoolean(normalized);
                case JSON -> objectMapper.writeValueAsString(objectMapper.readTree(normalized));
            };
        } catch (Exception ex) {
            throw new BusinessException(
                ErrorCode.SYSTEM_CONFIG_VALUE_INVALID.getCode(),
                "配置值格式错误，期望类型: " + valueType.getCode()
            );
        }
    }

    private String normalizeBoolean(String value) {
        if ("1".equals(value) || "true".equalsIgnoreCase(value)) {
            return "true";
        }
        if ("0".equals(value) || "false".equalsIgnoreCase(value)) {
            return "false";
        }
        throw new BusinessException(ErrorCode.SYSTEM_CONFIG_VALUE_INVALID);
    }

    private Properties getRedisInfo(String section) {
        try {
            Properties info = redisTemplate.execute((RedisCallback<Properties>) connection -> {
                if (connection.serverCommands() == null) {
                    return new Properties();
                }
                return connection.serverCommands().info(section);
            });
            return info == null ? new Properties() : info;
        } catch (Exception ex) {
            log.warn("Failed to read redis info section: {}", section, ex);
            return new Properties();
        }
    }

    private long deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        Long deleted = redisTemplate.delete(keys);
        return deleted == null ? 0L : deleted;
    }

    private long countKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys == null ? 0L : keys.size();
    }

    private String normalizeCachePrefix(String prefix) {
        String normalized = prefix == null ? "" : prefix.trim();
        if (!StringUtils.hasText(normalized) || normalized.contains("*") || normalized.contains("?")) {
            throw new BusinessException(ErrorCode.SYSTEM_CACHE_PREFIX_INVALID);
        }

        if (!normalized.startsWith("voidpen:")) {
            normalized = "voidpen:" + normalized;
        }
        if (!normalized.endsWith(":")) {
            normalized += ":";
        }
        if ("voidpen:".equals(normalized)) {
            throw new BusinessException(ErrorCode.SYSTEM_CACHE_PREFIX_INVALID);
        }
        return normalized;
    }

    private String buildConfigCacheKey(String group, String key) {
        return RedisKeys.SYSTEM_CONFIG + group + ":" + key;
    }

    private boolean isSensitiveKey(String configKey) {
        return StringUtils.hasText(configKey) && SENSITIVE_KEY_PATTERN.matcher(configKey).find();
    }

    private String maskValue(String value) {
        return "******";
    }

    private String getString(Properties properties, String key) {
        Object value = properties.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String firstLine(String message) {
        if (!StringUtils.hasText(message)) {
            return "unknown error";
        }
        int index = message.indexOf('\n');
        return index < 0 ? message : message.substring(0, index);
    }

    private Double calculateUsage(long used, long total) {
        if (total <= 0) {
            return 0D;
        }
        double value = (double) Math.max(used, 0L) * 100D / total;
        return roundPercent(value);
    }

    private double roundPercent(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private File resolveRootPath() {
        File[] roots = File.listRoots();
        if (roots != null && roots.length > 0) {
            return roots[0];
        }
        return new File(".");
    }
}
