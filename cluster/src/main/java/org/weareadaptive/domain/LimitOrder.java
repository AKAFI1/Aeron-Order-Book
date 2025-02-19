package org.weareadaptive.domain;

import com.weareadaptive.sbe.Side;

public record LimitOrder(Side side,
                         long orderId,
                         long userId,
                         long price,
                         long limitPrice,
                         int quantity,
                         long timestamp)
{
}
