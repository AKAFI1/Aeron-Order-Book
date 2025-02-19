package org.weareadaptive.vertx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.ActionHandler;
import org.weareadaptive.dto.*;
import org.weareadaptive.util.CorrelationRepository;
import org.weareadaptive.util.SbeFactory;

import java.util.ArrayList;
import java.util.List;

public class VerticleMain extends AbstractVerticle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VerticleMain.class);

    private final Vertx vertx;
    private final List<ServerWebSocket> webSockets = new ArrayList<>();
    private final int webSocketPort;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ManyToOneRingBuffer towardsClusterBuffer;
    private final ManyToOneRingBuffer towardsClientBuffer;

    private final CorrelationRepository correlationIdRepository;

    private final ActionHandler actionHandler;
    private final SbeFactory sf = SbeFactory.sbeFactory();

    public VerticleMain(final Vertx vertx,
                        final int webSocketPort,
                        final ManyToOneRingBuffer towardsClusterBuffer,
                        final ManyToOneRingBuffer towardsClientBuffer,
                        final CorrelationRepository correlationIdRepository,
                        final ActionHandler actionHandler)
    {
        this.vertx = vertx;
        this.webSocketPort = webSocketPort;
        this.towardsClusterBuffer = towardsClusterBuffer;
        this.towardsClientBuffer = towardsClientBuffer;
        this.correlationIdRepository = correlationIdRepository;
        this.actionHandler = actionHandler;
    }

    @Override
    public void start(final Promise<Void> startPromise)
    {
        final HttpServer httpServer = vertx.createHttpServer();
        final Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        httpServer.webSocketHandler(ws ->
        {
           webSockets.add(ws);
           LOGGER.info("Websocket connected on port {}", webSocketPort);
           ws.closeHandler(v ->
           {
               LOGGER.info("Websocket closed on port {}", webSocketPort);
               webSockets.remove(ws);
           });
           ws.frameHandler(frame ->
           {
              if (!frame.isClose())
              {
                  final String json = frame.textData();
                  forwardToJsonHandler(json, ws);
              }
           });
        })
        .listen(webSocketPort, result ->
        {
            if (result.succeeded())
            {
                LOGGER.info("WebSocket server is running on port " + webSocketPort);
            }
            else
            {
                LOGGER.info("Failed to start WebSocket server");
            }
        });

        vertx.setPeriodic(1, id ->
        {
            checkTowardsClientRingBuffer();
        });
    }

    private void checkTowardsClientRingBuffer()
    {
        while (towardsClientBuffer.read((msgTypeId, buffer, index, length) ->
        {
            sf.resultDecoder().wrapAndApplyHeader(buffer, index, sf.headerDecoder());
            final String message = sf.resultDecoder().resultMessage();

            onReceiveMessageFromCluster(message);
        }) > 0)
        {
            //continue reading buffer while there are  still messages
        }
    }

    private void forwardToJsonHandler(final String json, final ServerWebSocket webSocket)
    {
        try
        {
            final JsonNode jsonNode = objectMapper.readTree(json);
            final Action action = Action.valueOf(jsonNode.get("action").asText());
            final JsonNode payload = jsonNode.get("payload");
            final long correlationId = correlationIdRepository.generateAndStoreCorrelationId(webSocket);

            switch (action)
            {
                case MARKET_ORDER ->
                {
                    LOGGER.info("MARKET ORDER action received");
                    final MarketRequest marketRequest = objectMapper.treeToValue(payload, MarketRequest.class);
                    actionHandler.onMarketOrder(marketRequest, correlationId);
                }
                case LIMIT_ORDER ->
                {
                    LOGGER.info("LIMIT ORDER action received");
                    final LimitRequest limitRequest = objectMapper.treeToValue(payload, LimitRequest.class);
                    actionHandler.onLimitOrder(limitRequest, correlationId);
                }
                case STOP_ORDER ->
                {
                    LOGGER.info("STOP ORDER action received");
                    final StopRequest stopRequest = objectMapper.treeToValue(payload, StopRequest.class);
                    actionHandler.onStopOrder(stopRequest, correlationId);

                }
                default -> LOGGER.error("Unknown action {}", action);
            }
        }
        catch (final JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void onReceiveMessageFromCluster(final String msg)
    {
        for (final ServerWebSocket webSocket : webSockets)
        {
            if (!webSocket.isClosed())
            {
                webSocket.writeTextMessage(msg);
            }
        }
    }

}
