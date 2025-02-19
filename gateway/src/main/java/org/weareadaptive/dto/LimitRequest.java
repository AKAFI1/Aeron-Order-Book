package org.weareadaptive.dto;

public record LimitRequest(String username,
                           String side,
                           long limitPrice,
                           int quantity,
                           long timestamp)
{
}
