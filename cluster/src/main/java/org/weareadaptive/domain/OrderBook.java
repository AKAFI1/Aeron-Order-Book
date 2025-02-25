package org.weareadaptive.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.domain.dto.LimitOrder;
import org.weareadaptive.domain.dto.MarketOrder;
import org.weareadaptive.domain.repository.LimitRepository;
import org.weareadaptive.domain.repository.MarketRepository;

import java.util.*;

public class OrderBook
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBook.class);
    private final TreeMap<Long, List<LimitOrder>> buySide = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<Long, List<LimitOrder>> sellSide = new TreeMap<>();
    private final Map<Long, List<String>> tradeHistory = new HashMap<>();

    private final MarketRepository marketRepository;
    private final LimitRepository limitRepository;
    private static final String SUPPORTED_INSTRUMENT = "BTC";

    public OrderBook(final MarketRepository marketRepository, final LimitRepository limitRepository)
    {
        this.marketRepository = marketRepository;
        this.limitRepository = limitRepository;
    }

    public void placeLimitOrder(final LimitOrder order)
    {
        final TreeMap<Long, List<LimitOrder>> book = order.side().equals("BID") ? buySide : sellSide;
        book.computeIfAbsent(order.limitPrice(), k -> new ArrayList<>()).add(order);
        matchOrders();
    }

    public boolean placeMarketOrder(final MarketOrder order)
    {
        final TreeMap<Long, List<LimitOrder>> book = order.side().equals("BID") ? sellSide : buySide;

        if (book.isEmpty())
        {
            LOGGER.warn("Market Order couldn't be executed: {}", order);
            marketRepository.remove(order.orderId());
            return false;
        }
        executeMarketOrder(order, book);
        return true;
    }

    private void executeMarketOrder(final MarketOrder marketOrder, final TreeMap<Long, List<LimitOrder>> book)
    {
        int remainingQuantity = marketOrder.quantity();

        while (!book.isEmpty() && remainingQuantity > 0)
        {
            final Long bestPrice = book.firstKey();
            final List<LimitOrder> limitOrders = book.get(bestPrice);

            final Iterator<LimitOrder> iterator = limitOrders.iterator();
            while (iterator.hasNext() && remainingQuantity > 0)
            {
                final LimitOrder limitOrder = iterator.next();
                final int matchedQuantity = Math.min(limitOrder.quantity(), remainingQuantity);

                final String tradeDetails = "Trade executed: " + matchedQuantity + " BTC @ $" + bestPrice;
                LOGGER.info(tradeDetails);
                tradeHistory.computeIfAbsent(marketOrder.orderId(), k -> new ArrayList<>()).add(tradeDetails);

                if (limitOrder.quantity() > matchedQuantity)
                {
                    final LimitOrder updatedOrder = new LimitOrder(
                            "BTC",
                            limitOrder.side(),
                            limitOrder.orderId(),
                            limitOrder.userId(),
                            limitOrder.limitPrice(),
                            limitOrder.quantity() - matchedQuantity,
                            limitOrder.timestamp()
                    );
                    LOGGER.info("updated limit order: {}", updatedOrder);
                    limitOrders.set(limitOrders.indexOf(limitOrder), updatedOrder);
                    limitRepository.put(limitOrder.orderId(), updatedOrder);
                }
                else
                {
                    LOGGER.info("limit order fulfilled: {}", limitOrder);
                    limitRepository.remove(limitOrder.orderId());
                    iterator.remove();
                }

                remainingQuantity -= matchedQuantity;
            }

            if (limitOrders.isEmpty())
            {
                book.remove(bestPrice);
            }
        }
    }

    @SuppressWarnings("checkstyle:NeedBraces")
    private void matchOrders()
    {
        LOGGER.info("Matching orders");
        while (!buySide.isEmpty() && !sellSide.isEmpty() && buySide.firstKey() >= sellSide.firstKey())
        {
            final Long bidPrice = buySide.firstKey();
            final Long askPrice = sellSide.firstKey();

            final List<LimitOrder> buyList = buySide.get(bidPrice);
            final List<LimitOrder> sellList = sellSide.get(askPrice);

            final LimitOrder bid = buyList.get(0);
            final LimitOrder ask = sellList.get(0);

            final int matchedQuantity = Math.min(bid.quantity(), ask.quantity());

            final String tradeDetails = "Limit Order Executed " + matchedQuantity + " BTC @ $" + askPrice;
            LOGGER.info(tradeDetails);
            tradeHistory.computeIfAbsent(bid.orderId(), k -> new ArrayList<>()).add(tradeDetails);

           if (bid.quantity() > matchedQuantity)
           {
               buyList.set(0, new LimitOrder(
                       SUPPORTED_INSTRUMENT,
                       bid.side(),
                       bid.orderId(),
                       bid.userId(),
                       bid.limitPrice(),
                       bid.quantity() - matchedQuantity,
                       bid.timestamp()));
           }
           else
           {
               buyList.remove(0);
           }

            if (ask.quantity() > matchedQuantity)
            {
                final LimitOrder updatedOrder = new LimitOrder(SUPPORTED_INSTRUMENT,
                        ask.side(),
                        ask.orderId(),
                        ask.userId(),
                        ask.limitPrice(),
                        ask.quantity() - matchedQuantity,
                        ask.timestamp());

                sellList.set(0, updatedOrder);
                limitRepository.put(ask.orderId(), updatedOrder);
            }
            else
            {
                sellList.remove(0);
                limitRepository.remove(ask.orderId());
            }

            if (buyList.isEmpty())
            {
                buySide.remove(bidPrice);
            }
            if (sellList.isEmpty())
            {
                sellSide.remove(askPrice);
            }
        }
    }
}
