package org.weareadaptive.domain.dto;

public record MarketOrder(String instrument,
                          String side,
                          long orderId,
                          long userId,
                          int quantity,
                          long timestamp)
{
}
