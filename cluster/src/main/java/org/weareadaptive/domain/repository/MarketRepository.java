package org.weareadaptive.domain.repository;

import org.agrona.collections.Long2ObjectHashMap;
import org.weareadaptive.domain.dto.MarketOrder;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MarketRepository
{
    private final Long2ObjectHashMap<MarketOrder> orderById = new Long2ObjectHashMap<>();
    private final AtomicLong orderIdCounter = new AtomicLong(1);

    public void add(final MarketOrder order)
    {
        if (orderById.containsKey(order.orderId()))
        {
            throw new IllegalArgumentException("Order already exists: " + order.orderId());
        }

        orderById.put(order.orderId(), order);
    }

    public MarketOrder getById(final long orderId)
    {
        return orderById.get(orderId);
    }

    public List<MarketOrder> getAllOrders()
    {
        return orderById.values().stream().sorted().collect(Collectors.toList());
    }

    public long getOrCreateOrderId()
    {
        return orderIdCounter.getAndIncrement();
    }
}