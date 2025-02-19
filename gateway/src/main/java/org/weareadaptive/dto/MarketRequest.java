package org.weareadaptive.dto;

import com.weareadaptive.sbe.OrderType;

public record MarketRequest(String username,
                            String side,
                            long price,
                            int quantity,
                            long timestamp)
{
}
