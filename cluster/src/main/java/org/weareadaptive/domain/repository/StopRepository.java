package org.weareadaptive.domain.repository;

import org.agrona.collections.Long2ObjectHashMap;
import org.weareadaptive.domain.StopOrder;

import java.util.List;
import java.util.stream.Collectors;

public class StopRepository
{
    private final Long2ObjectHashMap<StopOrder> orderById = new Long2ObjectHashMap<>();

    public void add(final StopOrder order)
    {
        if (orderById.containsKey(order.orderId()))
        {
            throw new IllegalArgumentException("Order already exists: " + order.orderId());
        }

        orderById.put(order.orderId(), order);
    }

    public StopOrder getById(final int orderId)
    {
        return orderById.get(orderId);
    }

    public List<StopOrder> getAllOrders()
    {
        return orderById.values().stream().sorted().collect(Collectors.toList());
    }
}
