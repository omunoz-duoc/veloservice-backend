package com.bikeshop.manager.infrastructure.security;

import java.util.UUID;

/**
 * Stores the current sucursal identifier for the request thread.
 */
public final class SucursalContext {

    private static final ThreadLocal<UUID> CURRENT_SUCURSAL = new ThreadLocal<>();

    private SucursalContext() {
    }

    public static void setCurrentSucursal(UUID sucursalId) {
        CURRENT_SUCURSAL.set(sucursalId);
    }

    public static UUID getCurrentSucursal() {
        return CURRENT_SUCURSAL.get();
    }

    public static void clear() {
        CURRENT_SUCURSAL.remove();
    }
}