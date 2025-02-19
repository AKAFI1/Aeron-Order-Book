package org.weareadaptive.agent;

import io.aeron.Publication;
import io.aeron.cluster.client.AeronCluster;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.SystemEpochClock;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterClientAgent implements Agent
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterClientAgent.class);
    private static final long HEARTBEAT_INTERVAL = 10;
    private final EpochClock epochClock = new SystemEpochClock();
    private final AeronCluster clusterClient;
    private final ManyToOneRingBuffer towardsClusterBuffer;
    private long lastHeartbeatTime = Long.MIN_VALUE;
    private AgentState agentState = AgentState.INITIAL;
    private Publication publication;

    public ClusterClientAgent(final AeronCluster aeronCluster,
                              final ManyToOneRingBuffer ringBuffer)
    {
        this.clusterClient = aeronCluster;
        this.towardsClusterBuffer = ringBuffer;
    }

    @Override
    public void onStart() 
    {
        state(AgentState.STARTING);
        state(AgentState.CONNECTING);
    }

    @Override
    public int doWork() throws InterruptedException
    {
        int workCount = 0;
        switch (agentState)
        {
            case CONNECTING ->
            {
                publication = clusterClient.ingressPublication();
                LOGGER.info("Publisher agent is connecting to cluster");
                if (publication.isConnected())
                {
                    state(AgentState.STEADY);
                }

            }
            case STEADY ->
            {
                if (clusterClient != null)
                {
                    workCount += clusterClient.pollEgress();
                    workCount += towardsClusterBuffer.read(this::processRingBuffer);
                    Thread.sleep(1000);

                    final long now = epochClock.time();
                    if (now >= (lastHeartbeatTime + HEARTBEAT_INTERVAL))
                    {
                        lastHeartbeatTime = now;
                        clusterClient.sendKeepAlive();
                    }
                }
                else
                {
                    onClose();
                }
            }
        }
        return workCount;
    }

    @Override
    public void onClose()
    {
        publication.close();
    }

    @Override
    public String roleName()
    {
        return "EgressAgent";
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

    private void state(final AgentState newState)
    {
        agentState = newState;
        LOGGER.info("State changed to {}", newState);
    }
}
