package org.weareadaptive;

import com.weareadaptive.sbe.FixedStringEncodingEncoder;
import com.weareadaptive.sbe.Side;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.dto.LimitRequest;
import org.weareadaptive.dto.MarketRequest;
import org.weareadaptive.dto.StopRequest;
import org.weareadaptive.util.SbeFactory;

public class ActionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionHandler.class);
    private final SbeFactory sf = SbeFactory.sbeFactory();

    private final ManyToOneRingBuffer towardsClusterBuffer;

    public ActionHandler(final ManyToOneRingBuffer towardsClusterBuffer)
    {
        this.towardsClusterBuffer = towardsClusterBuffer;
    }

    public void onMarketOrder(final MarketRequest request, final long correlationId)
    {
        final int length = sf.headerEncoder().encodedLength() +
                sf.marketRequestEncoder().encodedLength() +
                sf.stringEncoder().encodedLength();

        final int claimIndex = towardsClusterBuffer.tryClaim(1, length);

        if (claimIndex > 0)
        {
            sf.marketRequestEncoder().wrapAndApplyHeader(towardsClusterBuffer.buffer(), claimIndex, sf.headerEncoder());
            sf.headerEncoder().correlationId(correlationId);

            final FixedStringEncodingEncoder usernameEncoder = sf.marketRequestEncoder().username();
            usernameEncoder.string(request.username());

            final Side sideEnum = sf.fromString(request.side());

            sf.marketRequestEncoder()
                    .side(sideEnum)
                    .quantity(request.quantity())
                    .timestamp(request.timestamp());

            towardsClusterBuffer.commit(claimIndex);
            LOGGER.info("Commited market order");
        }
        else
        {
            LOGGER.error("Failed to claim ring buffer space for market order request");
        }
    }

    public void onLimitOrder(final LimitRequest request, final long correlationId)
    {

        final int length = sf.headerEncoder().encodedLength() +
                sf.limitRequestEncoder().encodedLength() +
                sf.stringEncoder().encodedLength();

        final int claimIndex = towardsClusterBuffer.tryClaim(1, length);

        if (claimIndex > 0)
        {
            sf.limitRequestEncoder().wrapAndApplyHeader(towardsClusterBuffer.buffer(), claimIndex, sf.headerEncoder());
            sf.headerEncoder().correlationId(correlationId);

            final FixedStringEncodingEncoder usernameEncoder = sf.limitRequestEncoder().username();
            usernameEncoder.string(request.username());

            final Side sideEnum = sf.fromString(request.side());

            sf.limitRequestEncoder()
                    .side(sideEnum)
                    .limitPrice(request.limitPrice())
                    .quantity(request.quantity())
                    .timestamp(request.timestamp());

            towardsClusterBuffer.commit(claimIndex);
        }
        else
        {
            LOGGER.error("Failed to claim ring buffer space for limit order request");
        }
    }

    public void onStopOrder(final StopRequest request, final long correlationId)
    {
        final int length = sf.headerEncoder().encodedLength() + sf.stopRequestEncoder().encodedLength();
        final int claimIndex = towardsClusterBuffer.tryClaim(1, length);
        if (claimIndex > 0)
        {
            sf.stopRequestEncoder().wrapAndApplyHeader(towardsClusterBuffer.buffer(), claimIndex, sf.headerEncoder());

            sf.stopRequestEncoder().wrapAndApplyHeader(towardsClusterBuffer.buffer(), 0, sf.headerEncoder())
                    .side(request.side())
                    .stopPrice(request.stopPrice())
                    .quantity(request.quantity())
                    .timestamp(request.timestamp());

            towardsClusterBuffer.commit(claimIndex);
        }
        else
        {
            LOGGER.error("Failed to claim ring buffer space for stop order request");
        }
    }
}
