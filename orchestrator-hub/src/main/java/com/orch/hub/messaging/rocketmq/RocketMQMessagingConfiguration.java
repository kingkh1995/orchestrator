package com.orch.hub.messaging.rocketmq;

import com.orch.hub.messaging.OrchMessagingProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.a2a.transport.config.RocketMQTransportConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires RocketMQ-A2A transport configuration. Exposes a
 * {@link RocketMQTransportConfig} bean built 1:1 from
 * {@link OrchMessagingProperties.RocketMQ}. The bean is the bridge into
 * the {@code org.apache.rocketmq.a2a} library — outbound {@code Client}
 * instances are built from it via
 * {@code Client.builder(card).withTransport(RocketMQTransport.class, config)}.
 */
@Slf4j
@Configuration
public class RocketMQMessagingConfiguration {

    @Bean
    public RocketMQTransportConfig rocketMQTransportConfig(OrchMessagingProperties properties) {
        var src = properties.getRocketmq();
        log.info("Wiring RocketMQTransportConfig: endpoint={} namespace={} agentTopic={}",
                src.getEndpoint(), src.getNamespace(), src.getAgentTopic());
        return RocketMQTransportConfig.builder()
                .endpoint(src.getEndpoint())
                .namespace(src.getNamespace())
                .accessKey(src.getAccessKey())
                .secretKey(src.getSecretKey())
                .workAgentResponseTopic(src.getWorkAgentResponseTopic())
                .workAgentResponseGroupID(src.getWorkAgentResponseGroupID())
                .agentTopic(src.getAgentTopic())
                .agentUrl(src.getAgentUrl())
                .useDefaultRecoverMode(src.isUseDefaultRecoverMode())
                .build();
    }
}
