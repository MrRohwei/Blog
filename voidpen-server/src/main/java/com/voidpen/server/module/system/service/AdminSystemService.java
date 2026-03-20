package com.voidpen.server.module.system.service;

import com.voidpen.server.module.system.model.request.UpdateSystemConfigRequest;
import com.voidpen.server.module.system.model.response.CacheOperateResultVO;
import com.voidpen.server.module.system.model.response.SystemCacheStatsVO;
import com.voidpen.server.module.system.model.response.SystemConfigItemVO;
import com.voidpen.server.module.system.model.response.SystemMonitorOverviewVO;
import java.util.List;

public interface AdminSystemService {

    List<String> listConfigGroups();

    List<SystemConfigItemVO> listConfigByGroup(String configGroup);

    void updateConfig(UpdateSystemConfigRequest request);

    SystemMonitorOverviewVO getMonitorOverview();

    SystemCacheStatsVO getCacheStats();

    CacheOperateResultVO evictByPrefix(Long operatorId, String prefix);

    CacheOperateResultVO clearAllCache(Long operatorId);
}
