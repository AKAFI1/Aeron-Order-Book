package org.weareadaptive.domain;

import com.weareadaptive.sbe.Side;

public record StopOrder(Side side,
                        long orderId,
                        long userId,
                        long price,
                        long stopPrice,
                        int quantity,
                        long timestamp)
{
}
