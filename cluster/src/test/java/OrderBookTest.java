import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.weareadaptive.domain.OrderBook;
import org.weareadaptive.domain.dto.LimitOrder;
import org.weareadaptive.domain.dto.MarketOrder;
import org.weareadaptive.domain.repository.LimitRepository;
import org.weareadaptive.domain.repository.MarketRepository;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class OrderBookTest
{
    private OrderBook orderBook;
    private MarketRepository marketRepository;
    private LimitRepository limitRepository;

    @BeforeEach
    void setUp()
    {
        marketRepository = Mockito.mock(MarketRepository.class);
        limitRepository = Mockito.mock(LimitRepository.class);
        orderBook = new OrderBook(marketRepository, limitRepository);
    }

    @Test
    void testPlaceLimitOrder()
    {
        LimitOrder limitOrder = new LimitOrder("BTC", "BID", 1L, 1001L, 4500L, 2, 1010102901290L);
        orderBook.placeLimitOrder(limitOrder);

        verify(limitRepository).put(eq(1L), any(LimitOrder.class));
    }

    @Test
    void testMatchLimitOrders()
    {
        LimitOrder buyOrder = new LimitOrder("BTC", "BID", 1L, 1001L, 45000L, 2, System.currentTimeMillis());
        LimitOrder sellOrder = new LimitOrder("BTC", "ASK", 2L, 1002L, 45000L, 2, System.currentTimeMillis());

        orderBook.placeLimitOrder(buyOrder);
        orderBook.placeLimitOrder(sellOrder);

        verify(limitRepository).remove(eq(1L));
        verify(limitRepository).remove(eq(2L));
    }

    @Test
    void testMarketOrderExecution()
    {
        LimitOrder sellOrder = new LimitOrder("BTC", "ASK", 1L, 1002L, 45000L, 2, System.currentTimeMillis());
        orderBook.placeLimitOrder(sellOrder);

        MarketOrder marketOrder = new MarketOrder("BTC", "BID", 2L, 1001L, 2, System.currentTimeMillis());

        assertTrue(orderBook.placeMarketOrder(marketOrder));

        verify(limitRepository).remove(eq(1L));
        verify(marketRepository).remove(eq(2L));
    }

    @Test
    void testMarketOrderNoExecution()
    {
        MarketOrder marketOrder = new MarketOrder("BTC", "BID", 1L, 1001L, 2, System.currentTimeMillis());

        assertFalse(orderBook.placeMarketOrder(marketOrder));
        verify(marketRepository).remove(eq(1L));
    }

    @Test
    void testPlaceLimitOrderWithInvalidQuantity() {
        LimitOrder order = new LimitOrder("BTC", "BID", 1L, 1001L, 45000L, -2, System.currentTimeMillis());
        assertThrows(IllegalArgumentException.class, () -> orderBook.placeLimitOrder(order));
    }

    @Test
    void testPlaceMarketOrderNoMatchingLimitOrders() {
        MarketOrder marketOrder = new MarketOrder("BTC", "BID", 1L, 1001L, 5, System.currentTimeMillis());
        assertFalse(orderBook.placeMarketOrder(marketOrder));
    }

    @Test
    void testMarketOrderPartialFill() {
        LimitOrder sellOrder = new LimitOrder("BTC", "ASK", 1L, 1002L, 45000L, 2, System.currentTimeMillis());
        orderBook.placeLimitOrder(sellOrder);
        MarketOrder marketOrder = new MarketOrder("BTC", "BID", 2L, 1001L, 5, System.currentTimeMillis());
        assertTrue(orderBook.placeMarketOrder(marketOrder));
    }

    @Test
    void testPlaceLimitOrderUnsupportedInstrument() {
        LimitOrder order = new LimitOrder("ETH", "BID", 1L, 1001L, 3000L, 2, System.currentTimeMillis());
        assertThrows(IllegalArgumentException.class, () -> orderBook.placeLimitOrder(order));
    }
}
