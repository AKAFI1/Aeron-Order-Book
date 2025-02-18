package org.weareadaptive.infra.responder;

import org.agrona.ExpandableDirectByteBuffer;
import org.weareadaptive.infra.session.ClientSessionServiceImpl;
import org.weareadaptive.util.SbeFactory;

public class TraderResponder implements ClientResponder
{
    private final ExpandableDirectByteBuffer buffer = new ExpandableDirectByteBuffer(1024);

    private final SbeFactory sf = SbeFactory.sbeFactory();

    private final ClientSessionServiceImpl clientSession;

    public TraderResponder(final ClientSessionServiceImpl clientSession)
    {
        this.clientSession = clientSession;
    }

    @Override
    public void sendResponseMessage(final String message)
    {
        sf.resultEncoder().wrapAndApplyHeader(buffer, 0, sf.headerEncoder());
        sf.resultEncoder().resultMessage(message);

        clientSession.reply(buffer, 0, sf.headerEncoder().encodedLength() + sf.resultEncoder().encodedLength());
    }

    @Override
    public void sendBroadcastMessage(final String message)
    {
        sf.resultEncoder().wrapAndApplyHeader(buffer, 0, sf.headerEncoder());
        sf.resultEncoder().resultMessage(message);

        clientSession.broadcast(buffer, 0, sf.headerEncoder().encodedLength() + sf.resultEncoder().encodedLength());
    }


}
