package org.weareadaptive.infra;

import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.infra.session.ClientSessionServiceImpl;
import org.weareadaptive.service.OrderService;
import org.weareadaptive.infra.snapshot.SnapshotService;

public class TradingClusteredService implements ClusteredService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TradingClusteredService.class);

    private final ClientSessionServiceImpl clientSession;
    private final OrderService orderService;
    private final IngressAdapter ingressAdapter;
    private final SnapshotService snapshotService;

    private Cluster cluster;

    public TradingClusteredService(final ClientSessionServiceImpl clientSession,
                                   final OrderService orderService,
                                   final IngressAdapter ingressAdapter,
                                   final SnapshotService snapshotService)
    {
        this.clientSession = clientSession;
        this.orderService = orderService;
        this.ingressAdapter = ingressAdapter;
        this.snapshotService = snapshotService;
    }

    @Override
    public void onStart(final Cluster cluster, final Image snapshotImage)
    {
        this.cluster = cluster;
        clientSession.setIdleStrategy(cluster.idleStrategy());
        snapshotService.setIdleStrategy(cluster.idleStrategy());

        if (snapshotImage != null)
        {
            snapshotService.loadSnapshot(snapshotImage);
        }
    }

    @Override
    public void onSessionOpen(final ClientSession session, final long timestamp)
    {
        clientSession.addSession(session, timestamp);
    }

    @Override
    public void onSessionClose(final ClientSession session, final long timestamp, final CloseReason closeReason)
    {
        clientSession.removeSession(session, timestamp);

    }

    @Override
    public void onSessionMessage(final ClientSession session,
                     final long timestamp,
                     final DirectBuffer buffer,
                     final int offset,
                     final int length,
                     final Header header)
    {
        // set session
        clientSession.setSessionContext(session, timestamp);
        // send message to ingress adapter
        LOGGER.info("Session message received");
        ingressAdapter.onFragment(buffer, offset, length, header);
    }

    @Override
    public void onTimerEvent(final long correlationId, final long timestamp)
    {
        clientSession.setClusterTime(timestamp);
    }

    @Override
    public void onTakeSnapshot(final ExclusivePublication snapshotPublication)
    {
        LOGGER.info("Taking snapshot");
        snapshotService.handleTakeSnapshot(snapshotPublication);
    }

    @Override
    public void onRoleChange(final Cluster.Role newRole)
    {
        LOGGER.info("Cluster Node is in role {}", newRole.name());
    }

    @Override
    public void onTerminate(final Cluster cluster)
    {
        LOGGER.info("Cluster Node is terminating");
    }
}
