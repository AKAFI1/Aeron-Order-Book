package org.weareadaptive.domain;

import com.weareadaptive.sbe.Side;

public record StopOrder(Side side,
                        int userId,
                        int orderId,
                        long price,
                        long stopPrice,
                        int quantity,
                        long timestamp)
{
}
