package com.veloservice.shared.application.util;

import java.util.Locale;

/**
 * Utilities for working with Chilean RUT values.
 */
public final class RutUtils {

    private RutUtils() {
    }

    /**
     * Normalizes a RUT to bare digits plus verifier, e.g. {@code 18.295.090-7 -> 182950907}.
     * Removes dots, dashes and spaces and upper-cases the {@code K} verifier.
     *
     * @param rut raw RUT, may be {@code null}
     * @return normalized RUT, or {@code null} if input was {@code null}
     */
    public static String normalize(String rut) {
        if (rut == null) {
            return null;
        }
        return rut.replace(".", "")
                .replace("-", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);
    }
}
