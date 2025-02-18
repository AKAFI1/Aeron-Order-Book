package org.weareadaptive.domain;

import com.weareadaptive.sbe.Side;

public record LimitOrder(Side side,
                         int userId,
                         int orderId,
                         long price,
                         long limitPrice,
                         int quantity,
                         long timestamp)
{
}
