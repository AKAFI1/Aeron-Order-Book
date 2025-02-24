package org.weareadaptive.dto;


public record MarketRequest(String username,
                            String side,
                            long price,
                            int quantity,
                            long timestamp)
{
}
