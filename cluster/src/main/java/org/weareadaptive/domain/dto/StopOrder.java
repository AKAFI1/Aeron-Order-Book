package org.weareadaptive.domain.dto;

import com.weareadaptive.sbe.Side;

public record StopOrder(Side side,
                        long orderId,
                        long userId,
                        long stopPrice,
                        int quantity,
                        long timestamp)
{
}
