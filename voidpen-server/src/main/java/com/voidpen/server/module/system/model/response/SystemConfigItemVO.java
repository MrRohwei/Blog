package com.voidpen.server.module.system.model.response;

import lombok.Data;

@Data
public class SystemConfigItemVO {

    private Long id;

    private String configGroup;

    private String configKey;

    private String configValue;

    private String valueType;

    private String description;

    private Integer editable;

    private Boolean sensitive;
}
