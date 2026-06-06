package com.orch.hub.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
    "orch.messaging.rocketmq.endpoint=10.0.0.1:8081",
    "orch.messaging.rocketmq.namespace=test-ns",
    "orch.messaging.rocketmq.access-key=test-ak",
    "orch.messaging.rocketmq.secret-key=test-sk",
    "orch.messaging.rocketmq.work-agent-response-topic=WORK_RESPONSE_TOPIC",
    "orch.messaging.rocketmq.work-agent-response-group-id=WORK_RESPONSE_GID",
    "orch.messaging.rocketmq.agent-topic=AGENT_TOPIC",
    "orch.messaging.rocketmq.agent-url=http://agent.example.com",
    "orch.messaging.rocketmq.use-default-recover-mode=true"
})
class OrchMessagingPropertiesTest {

    @Autowired
    private OrchMessagingProperties properties;

    @Test
    void shouldBindAllNineRocketMQFields() {
        var rmq = properties.getRocketmq();
        assertEquals("10.0.0.1:8081", rmq.getEndpoint(), "endpoint");
        assertEquals("test-ns", rmq.getNamespace(), "namespace");
        assertEquals("test-ak", rmq.getAccessKey(), "accessKey");
        assertEquals("test-sk", rmq.getSecretKey(), "secretKey");
        assertEquals("WORK_RESPONSE_TOPIC", rmq.getWorkAgentResponseTopic(), "workAgentResponseTopic");
        assertEquals("WORK_RESPONSE_GID", rmq.getWorkAgentResponseGroupID(), "workAgentResponseGroupID");
        assertEquals("AGENT_TOPIC", rmq.getAgentTopic(), "agentTopic");
        assertEquals("http://agent.example.com", rmq.getAgentUrl(), "agentUrl");
        assertTrue(rmq.isUseDefaultRecoverMode(), "useDefaultRecoverMode");
    }

    @Test
    void shouldDefaultUseDefaultRecoverModeToFalse() {
        var props = new OrchMessagingProperties();
        assertFalse(props.getRocketmq().isUseDefaultRecoverMode());
    }
}
