package com.bikeshop.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = com.veloservice.BikeshopManagerApplication.class)
@ActiveProfiles("dev")
class BikeshopManagerApplicationTests {

    @Test
    void contextLoads() {
    }
}
