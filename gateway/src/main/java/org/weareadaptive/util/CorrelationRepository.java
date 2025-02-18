package org.weareadaptive.util;

import io.vertx.core.http.ServerWebSocket;
import org.agrona.collections.Long2ObjectHashMap;

import java.util.UUID;

public class CorrelationRepository
{
    private long correlationId;
    private final Long2ObjectHashMap<ServerWebSocket> orderById = new Long2ObjectHashMap<>();

    public CorrelationRepository(final long correlationId)
    {
        this.correlationId = correlationId;
    }

    public long generateAndStoreCorrelationId(final ServerWebSocket serverWebSocket)
    {
        correlationId = UUID.randomUUID().getMostSignificantBits();
        orderById.put(correlationId, serverWebSocket);
        return correlationId;
    }

    public Object getRequestByCorrelationId()
    {
        return orderById.get(correlationId);
    }

    public void removeCorrelationId(final long correlationId)
    {
        orderById.remove(correlationId);
    }

}
