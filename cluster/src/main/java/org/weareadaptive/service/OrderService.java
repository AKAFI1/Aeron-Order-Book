package org.weareadaptive.service;

import com.weareadaptive.sbe.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.domain.OrderBook;
import org.weareadaptive.domain.dto.LimitOrder;
import org.weareadaptive.domain.dto.MarketOrder;
import org.weareadaptive.domain.dto.StopOrder;
import org.weareadaptive.domain.repository.LimitRepository;
import org.weareadaptive.domain.repository.MarketRepository;
import org.weareadaptive.domain.repository.StopRepository;
import org.weareadaptive.domain.repository.UserRepository;
import org.weareadaptive.infra.responder.TraderResponder;



public class OrderService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private final MarketRepository marketRepository;
    private final LimitRepository limitRepository;
    private final StopRepository stopRepository;
    private final UserRepository userRepository;
    private final OrderBook orderBook;

    private final TraderResponder traderResponder;

    public OrderService(final MarketRepository marketRepository,
                        final LimitRepository limitRepository,
                        final StopRepository stopRepository,
                        final UserRepository userRepository,
                        final OrderBook orderBook,
                        final TraderResponder traderResponder)
    {
        this.marketRepository = marketRepository;
        this.limitRepository = limitRepository;
        this.stopRepository = stopRepository;
        this.userRepository = userRepository;
        this.orderBook = orderBook;
        this.traderResponder = traderResponder;
    }

    public void handleMarketRequest(final String username,
                                         final String sbeSide,
                                         final int quantity,
                                         final long timestampMs)
    {
        final long userId = userRepository.getOrCreateUserId(username);
        final long orderId = marketRepository.getOrCreateOrderId();

        final MarketOrder marketOrder = new MarketOrder(
                "BTC",
                sbeSide,
                orderId,
                userId,
                quantity,
                timestampMs
        );

        orderBook.placeMarketOrder(marketOrder);

        orderNotification(username, "Market", marketOrder);
        marketRepository.add(marketOrder);
        LOGGER.info("Market order added: {}", marketOrder);
    }

    @SuppressWarnings("checkstyle:LeftCurly")
    public void handleLimitRequest(final String username,
                                        final String sbeSide,
                                        final long limitPrice,
                                        final int quantity,
                                        final long timestamp)
    {
        final long userId = userRepository.getOrCreateUserId(username);
        final long orderId = limitRepository.getOrCreateOrderId();

        final LimitOrder limitOrder = new LimitOrder(
                "BTC",
                sbeSide,
                orderId,
                userId,
                limitPrice,
                quantity,
                timestamp
        );

        orderNotification(username, "Limit", limitOrder);

        limitRepository.add(limitOrder);
        LOGGER.info("Limit order added: {}", limitOrder);

        orderBook.placeLimitOrder(limitOrder);
    }

    @SuppressWarnings("checkstyle:LeftCurly")
    public void handleStopRequest(final String username,
                                       final Side sbeSide,
                                       final long stopPrice,
                                       final int quantity,
                                       final long timestamp)
    {
        final long userId = userRepository.getOrCreateUserId(username);
        final long orderId = marketRepository.getOrCreateOrderId();

        final StopOrder stopOrder = new StopOrder(
                sbeSide,
                orderId,
                userId,
                stopPrice,
                quantity,
                timestamp
        );
        orderNotification(username, "Stop", stopOrder);

        stopRepository.add(stopOrder);

    }

    private <T> void orderNotification(final String username, final String orderType, final T order)
    {
        final String response = "Thank you " + username + " for making a " + orderType + " order: \n" + order;

        traderResponder.sendResponseMessage(response);
    }


}
