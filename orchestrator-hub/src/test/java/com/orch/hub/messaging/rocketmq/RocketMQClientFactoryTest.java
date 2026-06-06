package com.orch.hub.messaging.rocketmq;

import io.a2a.client.Client;
import io.a2a.spec.A2AClientException;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentInterface;
import io.a2a.spec.AgentProvider;
import org.apache.rocketmq.a2a.common.constant.RocketMQA2AConstant;
import org.apache.rocketmq.a2a.transport.config.RocketMQTransportConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies {@link RocketMQClientFactory} is wired correctly with the
 * {@code RocketMQTransportConfig} bean. The actual {@code createClient}
 * call requires a live broker (the RocketMQ transport fetches the
 * AgentCard via gRPC during {@code ClientBuilder.build()}), so the
 * smoke test lives in {@code RocketMQIntegrationTest} against the
 * docker-compose broker.
 */
@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
    "orch.messaging.rocketmq.endpoint=localhost:8081",
    "orch.messaging.rocketmq.namespace=test",
    "orch.messaging.rocketmq.access-key=ak",
    "orch.messaging.rocketmq.secret-key=sk",
    "orch.messaging.rocketmq.work-agent-response-topic=WORK_TOPIC",
    "orch.messaging.rocketmq.work-agent-response-group-id=WORK_GID",
    "orch.messaging.rocketmq.agent-topic=AGENT_TOPIC",
    "orch.messaging.rocketmq.agent-url=http://agent.example.com",
    "orch.messaging.rocketmq.use-default-recover-mode=false"
})
class RocketMQClientFactoryTest {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private RocketMQClientFactory factory;

    @Autowired
    private RocketMQTransportConfig transportConfig;

    @Test
    void shouldExposeFactoryBean() {
        assertNotNull(ctx.getBean(RocketMQClientFactory.class));
    }

    @Test
    void shouldInjectTransportConfigFromProperties() {
        assertNotNull(transportConfig);
        assertEquals("localhost:8081", transportConfig.getEndpoint());
    }

    @Test
    void shouldDeclareCreateClientMethodWithCorrectSignature() throws NoSuchMethodException {
        var method = RocketMQClientFactory.class.getMethod("createClient", AgentCard.class);
        assertEquals(Client.class, method.getReturnType());
    }

    @Test
    void sampleAgentCardWithRocketMQProtocolReference() {
        AgentCard card = new AgentCard.Builder()
                .name("sub-agent")
                .description("sub agent")
                .url("http://localhost:8081/orchestrator-dev/AGENT_TOPIC")
                .version("0.0.1")
                .provider(new AgentProvider("test", "http://test"))
                .capabilities(new AgentCapabilities(false, false, false, null))
                .defaultInputModes(List.of())
                .defaultOutputModes(List.of())
                .skills(List.of())
                .preferredTransport(RocketMQA2AConstant.ROCKETMQ_PROTOCOL)
                .additionalInterfaces(List.of(new AgentInterface(
                        RocketMQA2AConstant.ROCKETMQ_PROTOCOL,
                        "http://localhost:8081/orchestrator-dev/AGENT_TOPIC")))
                .build();

        assertEquals("RocketMQ", card.preferredTransport());
    }
}
