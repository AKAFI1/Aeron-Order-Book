package org.weareadaptive.domain;

import com.weareadaptive.sbe.Side;

public record MarketOrder(Side side,
                          long orderId,
                          long userId,
                          long price,
                          int quantity,
                          long timestamp)
{
}
