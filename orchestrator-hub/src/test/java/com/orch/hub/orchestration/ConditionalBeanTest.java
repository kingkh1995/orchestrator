package com.orch.hub.orchestration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableAutoConfiguration
class ConditionalBeanTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldRegisterMockOrchestrationEngineWhenNoOtherBean() {
        assertNotNull(context.getBean(OrchestrationEngine.class),
                "OrchestrationEngine bean should be registered");
        assertTrue(context.getBean(OrchestrationEngine.class) instanceof MockOrchestrationEngine,
                "Default bean should be MockOrchestrationEngine");
    }
}
