package com.orch.hub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class OrchestratorHubApplicationTest {

    @Test
    void shouldLoadApplicationContext(ApplicationContext context) {
        assertNotNull(context, "Spring application context should load successfully");
        assertNotNull(context.getBean(OrchestratorHubApplication.class),
                "OrchestratorHubApplication bean should be registered");
    }
}