package org.weareadaptive.infra;

import com.weareadaptive.sbe.*;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

                final Side sideEnum = sf.marketRequestDecoder().side();

                orderService.handleMarketRequest(
                                    username,
                                    sf.toString(sideEnum),
                                    sf.marketRequestDecoder().quantity(),
                                    sf.marketRequestDecoder().timestamp());
            }
            case LimitOrderRequestDecoder.TEMPLATE_ID ->
            {
                sf.limitRequestDecoder().wrap(
                        buffer,
                        offset + sf.headerDecoder().encodedLength(),
                        sf.headerDecoder().blockLength(),
                        version);

                final FixedStringEncodingDecoder usernameDecoder = sf.limitRequestDecoder().username();
                final String username = usernameDecoder.string();

                final Side sideEnum = sf.limitRequestDecoder().side();

                orderService.handleLimitRequest(
                        username,
                        sf.toString(sideEnum),
                        sf.limitRequestDecoder().limitPrice(),
                        sf.limitRequestDecoder().quantity(),
                        sf.limitRequestDecoder().timestamp());
            }
            case StopOrderRequestDecoder.TEMPLATE_ID ->
            {
                sf.stopRequestDecoder().wrapAndApplyHeader(buffer, 0, sf.headerDecoder());

                orderService.handleStopRequest(
                        sf.stopRequestDecoder().username().toString(),
                        sf.stopRequestDecoder().side(),
                        sf.stopRequestDecoder().stopPrice(),
                        sf.stopRequestDecoder().quantity(),
                        sf.stopRequestDecoder().timestamp());
            }

        }
    }
}
