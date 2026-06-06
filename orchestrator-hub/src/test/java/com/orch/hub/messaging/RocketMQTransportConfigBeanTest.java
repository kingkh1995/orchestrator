package com.orch.hub.messaging;

import org.apache.rocketmq.a2a.transport.config.RocketMQTransportConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link RocketMQTransportConfig} is built from
 * {@link OrchMessagingProperties} — the bean bridges Spring config and
 * the rocketmq-a2a transport layer.
 */
@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
    "orch.messaging.rocketmq.endpoint=10.0.0.1:8081",
    "orch.messaging.rocketmq.namespace=test-ns",
    "orch.messaging.rocketmq.access-key=test-ak",
    "orch.messaging.rocketmq.secret-key=test-sk",
    "orch.messaging.rocketmq.work-agent-response-topic=WORK_TOPIC",
    "orch.messaging.rocketmq.work-agent-response-group-id=WORK_GID",
    "orch.messaging.rocketmq.agent-topic=AGENT_TOPIC",
    "orch.messaging.rocketmq.agent-url=http://agent.example.com",
    "orch.messaging.rocketmq.use-default-recover-mode=true"
})
class RocketMQTransportConfigBeanTest {

    @Autowired
    private ApplicationContext ctx;

    @Test
    void shouldExposeRocketMQTransportConfigBean() {
        assertTrue(ctx.containsBean("rocketMQTransportConfig"),
            "rocketMQTransportConfig bean must be present");
    }

    @Test
    void shouldMapAllPropertiesIntoTheLibraryConfig() {
        var cfg = ctx.getBean(RocketMQTransportConfig.class);
        assertNotNull(cfg);
        assertEquals("10.0.0.1:8081", cfg.getEndpoint());
        assertEquals("test-ns", cfg.getNamespace());
        assertEquals("test-ak", cfg.getAccessKey());
        assertEquals("test-sk", cfg.getSecretKey());
        assertEquals("WORK_TOPIC", cfg.getWorkAgentResponseTopic());
        assertEquals("WORK_GID", cfg.getWorkAgentResponseGroupID());
        assertEquals("AGENT_TOPIC", cfg.getAgentTopic());
        assertEquals("http://agent.example.com", cfg.getAgentUrl());
        assertTrue(cfg.isUseDefaultRecoverMode());
    }
}
