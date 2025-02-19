package org.weareadaptive.infra;

import com.weareadaptive.sbe.*;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.domain.LimitOrder;
import org.weareadaptive.domain.MarketOrder;
import org.weareadaptive.domain.StopOrder;
import org.weareadaptive.service.OrderService;
import org.weareadaptive.util.SbeFactory;

public class IngressAdapter implements FragmentHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IngressAdapter.class);
    private final SbeFactory sf = SbeFactory.sbeFactory();
    private final OrderService orderService;

    public IngressAdapter(final OrderService orderService)
    {
        this.orderService = orderService;
    }

    @Override
    public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header)
    {
        sf.headerDecoder().wrap(buffer, offset);
        final int templateId = sf.headerDecoder().templateId();
        final int version = sf.headerDecoder().version();
        final long correlationId = sf.headerDecoder().correlationId();

        switch (templateId)
        {
            case MarketRequestDecoder.TEMPLATE_ID ->
            {
                sf.marketRequestDecoder().wrap(
                        buffer,
                        offset + sf.headerDecoder().encodedLength(),
                        sf.headerDecoder().blockLength(),
                        version);

                final FixedStringEncodingDecoder usernameDecoder = sf.marketRequestDecoder().username();
                final String username = usernameDecoder.string();

                orderService.handleMarketOrderRequest(
                                    username,
                                    sf.marketRequestDecoder().side(),
                                    sf.marketRequestDecoder().price(),
                                    sf.marketRequestDecoder().quantity(),
                                    sf.marketRequestDecoder().timestamp());
            }
            case LimitOrderRequestDecoder.TEMPLATE_ID ->
            {
                sf.limitRequestDecoder().wrapAndApplyHeader(buffer, 0, sf.headerDecoder());

                orderService.handleLimitOrderRequest(
                        sf.limitRequestDecoder().username().toString(),
                        sf.limitRequestDecoder().side(),
                        sf.limitRequestDecoder().price(),
                        sf.limitRequestDecoder().limitPrice(),
                        sf.limitRequestDecoder().quantity(),
                        sf.limitRequestDecoder().timestamp());
            }
            case StopOrderRequestDecoder.TEMPLATE_ID ->
            {
                sf.stopRequestDecoder().wrapAndApplyHeader(buffer, 0, sf.headerDecoder());

                orderService.handleStopOrderRequest(
                        sf.stopRequestDecoder().username().toString(),
                        sf.stopRequestDecoder().side(),
                        sf.stopRequestDecoder().price(),
                        sf.stopRequestDecoder().stopPrice(),
                        sf.stopRequestDecoder().quantity(),
                        sf.stopRequestDecoder().timestamp());
            }

        }
    }
}
