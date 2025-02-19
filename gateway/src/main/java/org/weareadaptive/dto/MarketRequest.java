package org.weareadaptive.dto;

import com.weareadaptive.sbe.OrderType;
import com.weareadaptive.sbe.Side;

public record MarketRequest(String username,
                            OrderType orderType,
                            Side side,
                            long price,
                            int quantity,
                            long timestamp)
{
}
