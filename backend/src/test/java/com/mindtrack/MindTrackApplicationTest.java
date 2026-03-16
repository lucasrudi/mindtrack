package com.mindtrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class MindTrackApplicationTest {

    @Test
    void contextLoads() {
        // Smoke test only: the application context loading without errors is the assertion.
    }
}
