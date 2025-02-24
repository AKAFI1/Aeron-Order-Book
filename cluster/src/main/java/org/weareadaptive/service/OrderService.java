package org.weareadaptive.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.domain.OrderBook;
import org.weareadaptive.domain.dto.LimitOrder;
import org.weareadaptive.domain.dto.MarketOrder;
import org.weareadaptive.domain.repository.LimitRepository;
import org.weareadaptive.domain.repository.MarketRepository;
import org.weareadaptive.domain.repository.UserRepository;
import org.weareadaptive.infra.responder.TraderResponder;



public class OrderService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private final MarketRepository marketRepository;
    private final LimitRepository limitRepository;
    private final UserRepository userRepository;
    private final OrderBook orderBook;

    private final TraderResponder traderResponder;

    public OrderService(final MarketRepository marketRepository,
                        final LimitRepository limitRepository,
                        final UserRepository userRepository,
                        final OrderBook orderBook,
                        final TraderResponder traderResponder)
    {
        this.marketRepository = marketRepository;
        this.limitRepository = limitRepository;
        this.userRepository = userRepository;
        this.orderBook = orderBook;
        this.traderResponder = traderResponder;
    }

    public void handleMarketRequest(final long correlationId,
                                        final String username,
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

        if (orderBook.placeMarketOrder(marketOrder))
        {
            orderNotification(correlationId, username, "Market", marketOrder, true);
            marketRepository.add(marketOrder);
            LOGGER.info("Market order added: {}", marketOrder);
        }
        else
        {
            orderNotification(correlationId, username, "Market", marketOrder, false);
        }

    }

    @SuppressWarnings("checkstyle:LeftCurly")
    public void handleLimitRequest(final long correlationId,
                                   final String username,
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

        orderNotification(correlationId, username, "Limit", limitOrder, true);

        limitRepository.add(limitOrder);
        LOGGER.info("Limit order added: {}", limitOrder);

        orderBook.placeLimitOrder(limitOrder);
    }

    private <T> void orderNotification(final long correlationId,
                                       final String username,
                                       final String orderType,
                                       final T order,
                                       final boolean success)
    {
        String response = "";
        if (success)
        {
            response = "CorrelationId: " + correlationId +
                    ".\n Thank you " + username +
                    " for making a " + orderType +
                    " order: \n" + order;
        }
        else
        {
            response = "CorrelationId: " + correlationId +
                    ". Market Order could not be matched due to insufficient liquidity.";
        }

        traderResponder.sendResponseMessage(correlationId, response);
    }


}
