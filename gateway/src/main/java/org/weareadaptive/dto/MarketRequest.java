package org.weareadaptive.dto;

import com.weareadaptive.sbe.OrderType;
import com.weareadaptive.sbe.Side;

public record MarketRequest(OrderType orderType,
                            Side side,
                            int userId,
                            int orderId,
                            long price,
                            int quantity,
                            long timestamp)
{
}
