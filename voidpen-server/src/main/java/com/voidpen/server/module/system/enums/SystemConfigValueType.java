package com.voidpen.server.module.system.enums;

import java.util.Arrays;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemConfigValueType {
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    JSON("json");

    private final String code;

    public static SystemConfigValueType fromCode(String code) {
        if (code == null) {
            return STRING;
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(item -> item.code.equals(normalized))
            .findFirst()
            .orElse(STRING);
    }
}
