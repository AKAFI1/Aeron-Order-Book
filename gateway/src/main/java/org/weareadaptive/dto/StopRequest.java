package org.weareadaptive.dto;

import com.weareadaptive.sbe.OrderType;
import com.weareadaptive.sbe.Side;

public record StopRequest(OrderType orderType,
                          Side side,
                          int userId,
                          int orderId,
                          long price,
                          long stopPrice,
                          int quantity,
                          long timestamp)
{
}
