package com.orch.hub.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Messaging configuration. All values come from {@code application.yml}.
 * The product is built on RocketMQ-A2A; dev environments bring up a local
 * broker via {@code docker-compose.yml}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "orch.messaging")
public class OrchMessagingProperties {

    @org.springframework.boot.context.properties.NestedConfigurationProperty
    private RocketMQ rocketmq = new RocketMQ();

    /**
     * RocketMQ-A2A transport configuration. Field set mirrors
     * {@code org.apache.rocketmq.a2a.transport.config.RocketMQTransportConfig}
     * 1:1 so values forward to a {@code RocketMQTransportConfig} bean
     * without translation.
     */
    @Getter
    @Setter
    public static class RocketMQ {

        private String endpoint;

        private String namespace;

        private String accessKey;

        private String secretKey;

        private String workAgentResponseTopic;

        private String workAgentResponseGroupID;

        private String agentTopic;

        private String agentUrl;

        private boolean useDefaultRecoverMode = false;
    }
}
