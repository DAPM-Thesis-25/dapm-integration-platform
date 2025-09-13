package com.dapm.security_service.models.enums;

public enum Tier {
    FREE(0),
    BASIC(1),
    PREMIUM(2),
    PRIVATE (3);

    private final int level;

    Tier(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}

