package org.weareadaptive.domain.dto;

public record LimitOrder(String instrument,
                         String side,
                         long orderId,
                         long userId,
                         long limitPrice,
                         int quantity,
                         long timestamp)
{
}
