package org.weareadaptive.domain.dto;

public record LimitOrder(String Instrument,
                         String side,
                         long orderId,
                         long userId,
                         long limitPrice,
                         int quantity,
                         long timestamp)
{
}
