package org.weareadaptive.infra.responder;

public interface ClientResponder
{
    void sendResponseMessage(String message);

    void sendBroadcastMessage(String message);
}
