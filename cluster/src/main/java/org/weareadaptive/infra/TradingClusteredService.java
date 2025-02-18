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
import org.weareadaptive.infra.responder.TraderResponder;
import org.weareadaptive.infra.session.ClientSessionServiceImpl;
import org.weareadaptive.service.OrderService;

public class TradingClusteredService implements ClusteredService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TradingClusteredService.class);

    private final ClientSessionServiceImpl clientSession = new ClientSessionServiceImpl();
    private final OrderService orderService = new OrderService(new TraderResponder(clientSession));
    private final IngressAdapter ingressAdapter = new IngressAdapter(orderService);

    @Override
    public void onStart(final Cluster cluster, final Image snapshotImage)
    {
        clientSession.setIdleStrategy(cluster.idleStrategy());
        // TODO check for snapshot
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
        // TODO call snapshot service to store state
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
