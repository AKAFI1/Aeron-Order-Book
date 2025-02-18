package org.weareadaptive.service;

import com.weareadaptive.sbe.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.domain.LimitOrder;
import org.weareadaptive.domain.MarketOrder;
import org.weareadaptive.domain.StopOrder;
import org.weareadaptive.domain.repository.LimitRepository;
import org.weareadaptive.domain.repository.MarketRepository;
import org.weareadaptive.domain.repository.StopRepository;
import org.weareadaptive.domain.repository.UserRepository;
import org.weareadaptive.infra.responder.TraderResponder;
import org.weareadaptive.infra.session.ClientSessionServiceImpl;



public class OrderService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private final MarketRepository marketRepository = new MarketRepository();
    private final LimitRepository limitRepository = new LimitRepository();
    private final StopRepository stopRepository = new StopRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ClientSessionServiceImpl clientSessionService = new ClientSessionServiceImpl();
    private final TraderResponder traderResponder;

    public OrderService(final TraderResponder traderResponder)
    {
        this.traderResponder = traderResponder;
    }

    @SuppressWarnings("checkstyle:LeftCurly")
    public void handleMarketOrderRequest(final Side sbeSide,
                                         final long price,
                                         final int quantity,
                                         final long timestampMs,
                                         final String username)
    {
        final long userId = userRepository.getOrCreateUserId(username);
        final long orderId = marketRepository.getOrCreateOrderId();
        final MarketOrder marketOrder = new MarketOrder(
                sbeSide,
                orderId,
                userId,
                price,
                quantity,
                timestampMs
        );

        marketRepository.add(marketOrder);
        orderNotification(marketOrder);

        final Side side = Side.valueOf(sbeSide.toString());

        switch (side)
        {
            case BID -> { placeBuyOrder(marketOrder); }
            case ASK -> { placeSellOrder(marketOrder); }
        }
    }

    @SuppressWarnings("checkstyle:LeftCurly")
    public void handleLimitOrderRequest(final LimitOrder limitOrder) {
        limitRepository.add(limitOrder);
        orderNotification(limitOrder);

        final Side side = Side.valueOf(limitOrder.side().toString());

        switch (side) {
            case BID -> { placeBuyOrder(limitOrder); }
            case ASK -> { placeSellOrder(limitOrder); }
        }
    }

    @SuppressWarnings("checkstyle:LeftCurly")
    public void handleStopOrderRequest(final StopOrder stopOrder)
    {
        stopRepository.add(stopOrder);
        orderNotification(stopOrder);

        final Side side = Side.valueOf(stopOrder.side().toString());

        switch (side)
        {
            case BID -> { placeBuyOrder(stopOrder); }
            case ASK -> { placeSellOrder(stopOrder); }
        }
    }

    private <T> void placeBuyOrder(final T order)
    {
        orderNotification(order);
    }

    private <T> void placeSellOrder(final T order)
    {
        orderNotification(order);
    }

    private <T> void orderNotification(final T order)
    {
        final String response = "Thank you for making an order: \n" + order;

        traderResponder.sendResponseMessage(response);
    }


}
