package org.weareadaptive.infra.session;

import io.aeron.cluster.service.ClientSession;
import org.agrona.DirectBuffer;

public interface ClientSessionService
{
    void onSessionOpen(ClientSession session, long timestampMs);

    void onSessionClose(ClientSession session, long timestampMs);
}
