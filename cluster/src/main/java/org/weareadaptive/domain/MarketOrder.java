package org.weareadaptive.domain;

import com.weareadaptive.sbe.Side;

public record MarketOrder(Side side,
                          int orderId,
                          long price,
                          int quantity,
                          long timestampMs,
                          String username)
{
}
