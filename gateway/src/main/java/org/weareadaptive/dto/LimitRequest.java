package org.weareadaptive.dto;

import com.weareadaptive.sbe.OrderType;
import com.weareadaptive.sbe.Side;

public record LimitRequest(OrderType orderType,
                           Side side,
                           long price,
                           long limitPrice,
                           int quantity,
                           long timestamp,
                           String username)
{
}
