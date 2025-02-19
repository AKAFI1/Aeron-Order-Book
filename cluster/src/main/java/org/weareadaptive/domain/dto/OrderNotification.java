package org.weareadaptive.domain.dto;

import com.weareadaptive.sbe.OrderType;

public record OrderNotification<T>(OrderType orderType, T order)
{
}
