package com.veloservice.config.security;

import java.util.UUID;

public final class TallerContext {

    private static final ThreadLocal<UUID> CURRENT_TALLER = new ThreadLocal<>();

    private TallerContext() {}

    public static void setCurrentTaller(UUID tallerId) {
        CURRENT_TALLER.set(tallerId);
    }

    public static UUID getCurrentTaller() {
        return CURRENT_TALLER.get();
    }

    public static void clear() {
        CURRENT_TALLER.remove();
    }
}
