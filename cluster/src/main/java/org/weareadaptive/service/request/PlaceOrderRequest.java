package org.weareadaptive.service.request;

import com.weareadaptive.sbe.MessageHeaderDecoder;
import com.weareadaptive.sbe.Side;

public interface PlaceOrderRequest
{
    void onNewAuction(MessageHeaderDecoder header,
                      int userId,
                      long price,
                      int quantity,
                      long timestamp,
                      Side type);
}
