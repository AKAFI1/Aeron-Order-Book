package org.weareadaptive.domain.repository;

import org.agrona.collections.Long2ObjectHashMap;
import org.weareadaptive.domain.LimitOrder;
import org.weareadaptive.domain.MarketOrder;

import java.util.List;
import java.util.stream.Collectors;

public class LimitRepository
{
    private final Long2ObjectHashMap<LimitOrder> orderById = new Long2ObjectHashMap<>();

    public void add(final LimitOrder order)
    {
        if (orderById.containsKey(order.orderId()))
        {
            throw new IllegalArgumentException("Order already exists: " + order.orderId());
        }

        orderById.put(order.orderId(), order);
    }

    public LimitOrder getById(final int orderId)
    {
        return orderById.get(orderId);
    }

    public List<LimitOrder> getAllOrders()
    {
        return orderById.values().stream().sorted().collect(Collectors.toList());
    }
}
