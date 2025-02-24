package org.weareadaptive.util;

import io.vertx.core.http.ServerWebSocket;
import org.agrona.collections.Long2ObjectHashMap;

import java.util.UUID;

public class CorrelationRepository
{
    private long correlationId;
    private final Long2ObjectHashMap<ServerWebSocket> webSocketByCorrId = new Long2ObjectHashMap<>();

    public CorrelationRepository(final long correlationId)
    {
        this.correlationId = correlationId;
    }

    public long generateAndStoreCorrelationId(final ServerWebSocket serverWebSocket)
    {
        correlationId = UUID.randomUUID().getMostSignificantBits();
        webSocketByCorrId.put(correlationId, serverWebSocket);
        return correlationId;
    }

    public ServerWebSocket getWebSocketByCorrelationId(final long correlationId)
    {
        return webSocketByCorrId.get(correlationId);
    }

    public void remove(final long correlationId)
    {
        webSocketByCorrId.remove(correlationId);
    }

}
