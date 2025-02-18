package org.weareadaptive;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;

import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;
import io.aeron.samples.cluster.ClusterConfig;
import io.vertx.core.Vertx;
import org.agrona.CloseHelper;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.*;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.agent.ClusterClientAgent;
import org.weareadaptive.util.CorrelationRepository;
import org.weareadaptive.util.SbeFactory;
import org.weareadaptive.vertx.VerticleMain;

import java.nio.ByteBuffer;
import java.util.List;

public class TraderClient implements EgressListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TraderClient.class);
    private static final int PORT_BASE = 9000;
    private static final int CONFIGURED_PORT = 8080;

    private final SbeFactory sf = SbeFactory.sbeFactory();

    private final ManyToOneRingBuffer towardsClientBuffer;
    private AeronCluster clusterClient;

    public TraderClient(final ManyToOneRingBuffer towardsClientBuffer)
    {
        this.towardsClientBuffer = towardsClientBuffer;
    }

    public static void main(final String[] args)
    {
        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        final IdleStrategy idleStrategy = new BackoffIdleStrategy();

        final int bufferLength = 32768 + RingBufferDescriptor.TRAILER_LENGTH;
        final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(bufferLength));
        final ManyToOneRingBuffer towardsClusterBuffer = new ManyToOneRingBuffer(buffer);

        final int bufferLength2 = 32768 + RingBufferDescriptor.TRAILER_LENGTH;
        final UnsafeBuffer buffer2 = new UnsafeBuffer(ByteBuffer.allocateDirect(bufferLength2));
        final ManyToOneRingBuffer towardsClientBuffer = new ManyToOneRingBuffer(buffer2);

        final CorrelationRepository correlationRepository = new CorrelationRepository(0);

        final ActionHandler actionHandler = new ActionHandler(towardsClusterBuffer);

        final String ingressEndpoints = ingressEndpoints(List.of("localhost", "localhost", "localhost"));
        final TraderClient traderClient = new TraderClient(towardsClientBuffer);

        try (final MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
                .dirDeleteOnShutdown(true)
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnStart(true));
            final AeronCluster aeronCluster = AeronCluster.connect(new AeronCluster.Context()
                    .egressListener(traderClient)
                    .egressChannel("aeron:udp?endpoint=localhost:0")
                    .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                    .ingressChannel("aeron:udp")
                    .ingressEndpoints(ingressEndpoints)
                    .messageTimeoutNs(1000000000L)))
        {
            final ClusterClientAgent clusterClientAgent = new ClusterClientAgent(aeronCluster, towardsClusterBuffer);
            final AgentRunner clusterClientAgentRunner
                    = new AgentRunner(
                                idleStrategy,
                                Throwable::printStackTrace,
                                null,
                                clusterClientAgent);
            AgentRunner.startOnThread(clusterClientAgentRunner);

            traderClient.setAeronCluster(aeronCluster);

            final Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(new VerticleMain(
                    vertx,
                    CONFIGURED_PORT,
                    towardsClusterBuffer,
                    towardsClientBuffer,
                    correlationRepository,
                    actionHandler));
            LOGGER.info("Deployed web verticle");

            barrier.await();

            CloseHelper.close(vertx::close);
            CloseHelper.quietClose(clusterClientAgentRunner);
        }
        catch (final Exception e)
        {
            LOGGER.error("Error starting Chat Cluster Client", e);
        }
    }

    @Override
    public void onMessage(final long clusterSessionId,
                          final long timestamp,
                          final DirectBuffer buffer,
                          final int offset,
                          final int length,
                          final Header header)
    {
        sf.actionResultDecoder().wrapAndApplyHeader(buffer, offset, sf.headerDecoder());
        final String result = sf.actionResultDecoder().resultMessage();

        forwardResultToClient(buffer, offset, length);
        LOGGER.info("Received result: {}", result);
    }

    private void forwardResultToClient(final DirectBuffer buffer, final int offset, final int length)
    {
        final int claimIndex = towardsClientBuffer.tryClaim(1, length);
        if (claimIndex > 0)
        {
            buffer.getBytes(offset, towardsClientBuffer.buffer(), claimIndex, length);
            towardsClientBuffer.commit(claimIndex);
        }
        else
        {
            LOGGER.warn("No message received - failed to claim inbound ring buffer space");
        }
    }

    public void setAeronCluster(final AeronCluster clusterClient)
    {
        this.clusterClient = clusterClient;
    }

    public static String ingressEndpoints(final List<String> hostnames)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hostnames.size(); i++)
        {
            sb.append(i).append('=');
            sb.append(hostnames.get(i)).append(':').append(calculatePort(i, 2));
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    static int calculatePort(final int nodeId, final int offset)
    {
        return PORT_BASE + (nodeId * ClusterConfig.PORTS_PER_NODE) + offset;
    }
}
