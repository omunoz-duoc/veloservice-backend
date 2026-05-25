package com.veloservice.config.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class TallerContextTest {

    @AfterEach
    void cleanup() {
        TallerContext.clear();
    }

    @Test
    void returnsNullWhenNotSet() {
        assertThat(TallerContext.getCurrentTaller()).isNull();
    }

    @Test
    void storesAndRetrievesTallerId() {
        UUID id = UUID.randomUUID();
        TallerContext.setCurrentTaller(id);
        assertThat(TallerContext.getCurrentTaller()).isEqualTo(id);
    }

    @Test
    void clearRemovesValue() {
        TallerContext.setCurrentTaller(UUID.randomUUID());
        TallerContext.clear();
        assertThat(TallerContext.getCurrentTaller()).isNull();
    }
}
