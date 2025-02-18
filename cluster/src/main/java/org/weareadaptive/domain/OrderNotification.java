package org.weareadaptive.domain;

import com.weareadaptive.sbe.OrderType;

public record OrderNotification<T>(OrderType orderType, T order)
{
}
