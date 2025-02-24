package org.weareadaptive.infra.responder;

public interface ClientResponder
{
    void sendResponseMessage(long correlationId, String message);

    void sendBroadcastMessage(String message);
}
