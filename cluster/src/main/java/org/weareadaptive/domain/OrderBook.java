package org.weareadaptive.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.domain.dto.LimitOrder;
import org.weareadaptive.domain.dto.MarketOrder;

import java.util.*;

public class OrderBook
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBook.class);
    private final PriorityQueue<LimitOrder> buySide = new PriorityQueue<>(Comparator.comparingLong(o -> -o.limitPrice()));
    private final PriorityQueue<LimitOrder> sellSide = new PriorityQueue<>(Comparator.comparingLong(LimitOrder::limitPrice));
    private final Map<Long, List<String>> tradeHistory = new HashMap<>();
    private static final String SUPPORTED_INSTRUMENT = "BTC";

    public void placeLimitOrder(final LimitOrder order)
    {
        if (Objects.equals(order.side(), "BID"))
        {
            buySide.add(order);
        }
        else
        {
            sellSide.add(order);
        }
        matchOrders();
    }

    public void placeMarketOrder(final MarketOrder order)
    {
        if (Objects.equals(order.side(), "BID"))
        {
            executeMarketOrder(order, sellSide);
        }
        else
        {
            executeMarketOrder(order, buySide);
        }
    }

    private void executeMarketOrder(MarketOrder marketOrder, final PriorityQueue<LimitOrder> book)
    {
        int remainingQuantity = marketOrder.quantity();

        while (!book.isEmpty() && remainingQuantity > 0)
        {
            final LimitOrder limitOrder = book.poll();
            final int matchedQuantity = Math.min(limitOrder.quantity(), remainingQuantity);

            if (matchedQuantity > 0)
            {
                final String tradeDetails = "Market Order Executed " + matchedQuantity + " BTC @ $" + limitOrder.limitPrice();
                LOGGER.info(tradeDetails);
                tradeHistory.computeIfAbsent(marketOrder.orderId(), k -> new ArrayList<>()).add(tradeDetails);
            }

            if (limitOrder.quantity() > matchedQuantity)
            {
                book.add(new LimitOrder(SUPPORTED_INSTRUMENT,
                        limitOrder.side(),
                        limitOrder.orderId(),
                        limitOrder.userId(),
                        limitOrder.limitPrice(),
                        limitOrder.quantity() - matchedQuantity,
                        limitOrder.timestamp()));

            }
            remainingQuantity -= matchedQuantity;
        }
    }

    @SuppressWarnings("checkstyle:NeedBraces")
    private void matchOrders()
    {
        LOGGER.info("Matching orders");
        while (!buySide.isEmpty() && !sellSide.isEmpty() && buySide.peek().limitPrice() >= sellSide.peek().limitPrice())
        {
            final LimitOrder bid = buySide.poll();
            final LimitOrder ask = sellSide.poll();

            final int matchedQuantity = Math.min(bid.quantity(), ask.quantity());

            final String tradeDetails = "Limit Order Executed " + matchedQuantity + " BTC @ $" + ask.limitPrice();
            tradeHistory.computeIfAbsent(bid.orderId(), k -> new ArrayList<>()).add(tradeDetails);
            LOGGER.info(tradeDetails);

           if (bid.quantity() > matchedQuantity)
           {
               buySide.add(new LimitOrder(
                       SUPPORTED_INSTRUMENT,
                       bid.side(),
                       bid.orderId(),
                       bid.userId(),
                       bid.limitPrice(),
                       bid.quantity() - matchedQuantity,
                       bid.timestamp()));
           }

            if (ask.quantity() > matchedQuantity)
            {
                sellSide.add(new LimitOrder(
                        SUPPORTED_INSTRUMENT,
                        ask.side(),
                        ask.orderId(),
                        ask.userId(),
                        ask.limitPrice(),
                        ask.quantity() - matchedQuantity,
                        ask.timestamp()));
            }
        }
    }
}
