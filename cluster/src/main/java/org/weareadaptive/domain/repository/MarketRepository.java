package org.weareadaptive.domain.repository;

import org.agrona.collections.Long2ObjectHashMap;
import org.weareadaptive.domain.MarketOrder;

import java.util.List;
import java.util.stream.Collectors;

public class MarketRepository
{
    private final Long2ObjectHashMap<MarketOrder> orderById = new Long2ObjectHashMap<>();
    private final int orderId;

    public MarketRepository(final int orderId)
    {
        this.orderId = orderId;
    }

    public void add(final MarketOrder order)
    {
        if (orderById.containsKey(order.orderId))
        {
            throw new IllegalArgumentException("Order already exists: " + orderId());
        }

        orderById.put(orderId, order);
    }

    public MarketOrder getById(final int orderId)
    {
        return orderById.get(orderId);
    }

    public List<MarketOrder> getAllOrders()
    {
        return orderById.values().stream().sorted().collect(Collectors.toList());
    }
}
