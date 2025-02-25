package org.weareadaptive.domain.repository;

import org.agrona.collections.Long2ObjectHashMap;
import org.weareadaptive.domain.dto.LimitOrder;
import org.weareadaptive.domain.dto.MarketOrder;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class LimitRepository
{
    private final Long2ObjectHashMap<LimitOrder> orderById = new Long2ObjectHashMap<>();
    private final AtomicLong orderIdCounter = new AtomicLong(1);

    public void add(final LimitOrder order)
    {
        if (orderById.containsKey(order.orderId()))
        {
            throw new IllegalArgumentException("Order already exists: " + order.orderId());
        }

        orderById.put(order.orderId(), order);
    }

    public LimitOrder getById(final long orderId)
    {
        return orderById.get(orderId);
    }

    public List<LimitOrder> getAllOrders()
    {
        return orderById.values().stream().toList();
    }

    public long getOrCreateOrderId()
    {
        return orderIdCounter.getAndIncrement();
    }

    public void put(final long orderId, final LimitOrder order)
    {
        orderById.put(orderId, order);
    }

    public void remove(final long orderId)
    {
        orderById.remove(orderId);
    }
}