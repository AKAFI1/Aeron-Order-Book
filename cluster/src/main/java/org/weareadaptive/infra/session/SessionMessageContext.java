package org.weareadaptive.infra.session;

import org.agrona.DirectBuffer;

public interface SessionMessageContext
{
    long getClusterTime();

    void reply(DirectBuffer buffer, int offset, int length);

    void broadcast(DirectBuffer buffer, int offset, int length);
}
