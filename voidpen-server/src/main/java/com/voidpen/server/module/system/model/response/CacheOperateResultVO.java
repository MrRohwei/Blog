package com.voidpen.server.module.system.model.response;

import lombok.Data;

@Data
public class CacheOperateResultVO {

    private String scope;

    private Long deletedCount;
}
