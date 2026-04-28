package com.bikeshop.manager.infrastructure.security;

import java.util.UUID;

/**
 * Stores tenant and user identifiers for the current request thread.
 */
public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();

    /**
     * Sets the current tenant identifier.
     *
     * @param tenantId tenant identifier
     */
    public static void setCurrentTenant(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Returns the current tenant identifier.
     *
     * @return tenant identifier
     */
    public static UUID getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Sets the current user identifier.
     *
     * @param userId user identifier
     */
    public static void setCurrentUser(UUID userId) {
        CURRENT_USER.set(userId);
    }

    /**
     * Returns the current user identifier.
     *
     * @return user identifier
     */
    public static UUID getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * Clears the current thread context.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
    }
}
