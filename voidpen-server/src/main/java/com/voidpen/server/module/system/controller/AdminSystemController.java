package com.voidpen.server.module.system.controller;

import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.system.model.request.EvictCachePrefixRequest;
import com.voidpen.server.module.system.model.request.UpdateSystemConfigRequest;
import com.voidpen.server.module.system.model.response.CacheOperateResultVO;
import com.voidpen.server.module.system.model.response.SystemCacheStatsVO;
import com.voidpen.server.module.system.model.response.SystemConfigItemVO;
import com.voidpen.server.module.system.model.response.SystemMonitorOverviewVO;
import com.voidpen.server.module.system.service.AdminSystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统配置与监控")
@RestController
@RequestMapping("/admin/v1/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final AdminSystemService adminSystemService;

    @Operation(summary = "获取配置分组")
    @GetMapping("/config/groups")
    public Result<List<String>> groups() {
        return Result.success(adminSystemService.listConfigGroups());
    }

    @Operation(summary = "按分组获取配置列表")
    @GetMapping("/config/group/{group}")
    public Result<List<SystemConfigItemVO>> listConfigByGroup(@PathVariable("group") String group) {
        return Result.success(adminSystemService.listConfigByGroup(group));
    }

    @Operation(summary = "更新系统配置")
    @PutMapping("/config/item")
    public Result<Void> updateConfig(@Valid @RequestBody UpdateSystemConfigRequest request) {
        adminSystemService.updateConfig(request);
        return Result.success();
    }

    @Operation(summary = "获取运行监控概览")
    @GetMapping("/monitor/overview")
    public Result<SystemMonitorOverviewVO> monitorOverview() {
        return Result.success(adminSystemService.getMonitorOverview());
    }

    @Operation(summary = "获取缓存统计信息")
    @GetMapping("/cache/stats")
    public Result<SystemCacheStatsVO> cacheStats() {
        return Result.success(adminSystemService.getCacheStats());
    }

    @Operation(summary = "按前缀清理缓存")
    @PostMapping("/cache/evict-prefix")
    public Result<CacheOperateResultVO> evictPrefix(@Valid @RequestBody EvictCachePrefixRequest request) {
        return Result.success(adminSystemService.evictByPrefix(currentUserId(), request.getPrefix()));
    }

    @Operation(summary = "清理所有业务缓存")
    @PostMapping("/cache/clear-all")
    public Result<CacheOperateResultVO> clearAll() {
        return Result.success(adminSystemService.clearAllCache(currentUserId()));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
