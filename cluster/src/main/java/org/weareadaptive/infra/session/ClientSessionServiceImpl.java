package org.weareadaptive.infra.session;

import io.aeron.Publication;
import io.aeron.cluster.service.ClientSession;
import org.agrona.DirectBuffer;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.concurrent.IdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ClientSessionServiceImpl implements SessionMessageContext
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSessionServiceImpl.class);

    private static final long RETRY_COUNT = 3;

    private final List<ClientSession> allSessions = new ArrayList<>();
    private final Long2ObjectHashMap<ClientSession> sessionsById = new Long2ObjectHashMap<>();
    private ClientSessionService clientSessions;
    private ClientSession session;
    private long timestamp;
    private IdleStrategy idleStrategy;


    public void setClientSessionServiceImpl(final ClientSessionService clientSessions)
    {
        this.clientSessions = clientSessions;
    }

    public void addSession(final ClientSession session, final long timestamp)
    {
        allSessions.add(session);
        sessionsById.put(session.id(), session);
         if (clientSessions != null)
         {
             clientSessions.onSessionOpen(session, timestamp);
         }
    }

    public void removeSession(final ClientSession session, final long timestamp)
    {
        allSessions.remove(session);
        sessionsById.remove(session.id());

        if (clientSessions != null)
        {
            clientSessions.onSessionClose(session, timestamp);
        }
    }

    public List<ClientSession> getAllSessions()
    {
        return allSessions;
    }

    public void setSessionContext(final ClientSession clientSession, final long timestamp)
    {
        this.timestamp = timestamp;
        this.session = clientSession;
    }

    public void setClusterTime(final long timestamp)
    {
        this.timestamp = timestamp;
    }

    public long getClusterTime()
    {
        return timestamp;
    }

    public void setIdleStrategy(final IdleStrategy idleStrategy)
    {
        this.idleStrategy = idleStrategy;
    }

    @Override
    public void reply(final DirectBuffer buffer, final int offset, final int length)
    {
        offerToClient(session, buffer, offset, length);
    }

    @Override
    public void broadcast(final DirectBuffer buffer, final int offset, final int length)
    {
        getAllSessions().forEach(session -> offerToClient(session, buffer, offset, length));
    }

    private void offerToClient(final ClientSession targetSession, final DirectBuffer buffer, final int offset, final int length)
    {
        int retries = 0;
        do
        {
            final long result = targetSession.offer(buffer, offset, length);
            if (result > 0L)
            {
                return;
            }
            else if (result == Publication.ADMIN_ACTION || result == Publication.BACK_PRESSURED)
            {
                LOGGER.warn("backpressure or admin action on session offer");
            }
            else if (result == Publication.NOT_CONNECTED || result == Publication.MAX_POSITION_EXCEEDED)
            {
                LOGGER.error("unexpected state on session offer: {}", result);
                return;
            }

            idleStrategy.idle();
            retries += 1;
        }
        while (retries < RETRY_COUNT);

        LOGGER.error("failed to offer snapshot within {} retries. Closing client session.", RETRY_COUNT);
        session.close();
    }
}
