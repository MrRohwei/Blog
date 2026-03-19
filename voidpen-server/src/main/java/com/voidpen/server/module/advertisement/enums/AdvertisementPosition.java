package com.voidpen.server.module.advertisement.enums;

import java.util.Arrays;

public enum AdvertisementPosition {
    SIDEBAR,
    HEADER,
    FOOTER,
    DETAIL_BOTTOM;

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(position -> position.name().equals(value));
    }
}
