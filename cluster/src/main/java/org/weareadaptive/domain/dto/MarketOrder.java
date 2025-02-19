package org.weareadaptive.domain.dto;

import com.weareadaptive.sbe.Side;

public record MarketOrder(String Instrument,
                          String side,
                          long orderId,
                          long userId,
                          int quantity,
                          long timestamp)
{
}
