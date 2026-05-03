package com.bikeshop.manager.infrastructure.security;

import java.util.UUID;

/**
 * Stores the current user identifier for the request thread.
 */
public final class UsuarioContext {

    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();

    private UsuarioContext() {
    }

    public static void setCurrentUser(UUID userId) {
        CURRENT_USER.set(userId);
    }

    public static UUID getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
