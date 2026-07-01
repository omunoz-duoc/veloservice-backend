package com.veloservice.shared.application.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RutUtilsTest {

    @Test
    void normalizeStripsDotsDashesAndSpaces() {
        assertThat(RutUtils.normalize("18.295.090-7")).isEqualTo("182950907");
        assertThat(RutUtils.normalize(" 18 295 090 7 ")).isEqualTo("182950907");
    }

    @Test
    void normalizeUpperCasesKVerifier() {
        assertThat(RutUtils.normalize("12.345.678-k")).isEqualTo("12345678K");
    }

    @Test
    void normalizeReturnsNullForNull() {
        assertThat(RutUtils.normalize(null)).isNull();
    }
}
