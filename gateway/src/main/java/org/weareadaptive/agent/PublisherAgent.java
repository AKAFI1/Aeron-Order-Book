package org.weareadaptive.agent;

import io.aeron.cluster.client.AeronCluster;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublisherAgent implements Agent
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherAgent.class);

    private AgentState agentState = AgentState.INITIAL;
    private final AeronCluster clusterClient;
    private final ManyToOneRingBuffer towardsClusterBuffer;

    public PublisherAgent(final AeronCluster clusterClient, final ManyToOneRingBuffer towardsClusterBuffer)
    {
        this.clusterClient = clusterClient;
        this.towardsClusterBuffer = towardsClusterBuffer;
    }


    @Override
    public int doWork()
    {
        int workCount = 0;
        workCount += towardsClusterBuffer.read(this::processRingBuffer);
        return workCount;
    }

    @Override
    public String roleName()
    {
        return "ClusterPublisherAgent";
    }

    private void state(final AgentState newState)
    {
        agentState = newState;
        LOGGER.info("State changed to {}", newState);
    }

    private void processRingBuffer(final int messageType, final DirectBuffer buffer, final int offset, final int length)
    {
        final long offerResult = clusterClient.offer(buffer, offset, length);

        LOGGER.info("Publisher agent received offer result: {}", offerResult);
        if (offerResult < 0)
        {
            LOGGER.error("Offer failed - Response Code: {}", offerResult);
        }
    }
}
