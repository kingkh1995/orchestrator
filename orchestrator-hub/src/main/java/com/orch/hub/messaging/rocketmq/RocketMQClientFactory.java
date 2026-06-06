package com.orch.hub.messaging.rocketmq;

import io.a2a.client.Client;
import io.a2a.spec.A2AClientException;
import io.a2a.spec.AgentCard;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.a2a.transport.config.RocketMQTransportConfig;
import org.apache.rocketmq.a2a.transport.impl.RocketMQTransport;
import org.springframework.stereotype.Component;

/**
 * Builds a RocketMQ-transport-backed A2A {@link Client} for a given
 * target {@link AgentCard}. The returned client is per-card — call
 * {@link Client#close()} when done.
 *
 * <p>Construction is lazy: the underlying RocketMQ connection opens on
 * first {@code sendMessage}/{@code getTask} call, so
 * {@link #createClient(AgentCard)} does not block on broker reachability.</p>
 */
@Component
@RequiredArgsConstructor
public class RocketMQClientFactory {

    private final RocketMQTransportConfig transportConfig;

    /**
     * Build a {@link Client} that talks to the target sub-agent through
     * RocketMQ-A2A. Caller owns the returned client and must {@code close()}
     * it to release the underlying HTTP client and transport state.
     */
    public Client createClient(AgentCard agentCard) throws A2AClientException {
        return Client.builder(agentCard)
                .withTransport(RocketMQTransport.class, transportConfig)
                .build();
    }
}
