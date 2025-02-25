package org.weareadaptive.infra.snapshot;

import com.weareadaptive.sbe.*;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.Publication;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.domain.dto.LimitOrder;
import org.weareadaptive.domain.dto.MarketOrder;
import org.weareadaptive.domain.dto.User;
import org.weareadaptive.domain.repository.LimitRepository;
import org.weareadaptive.domain.repository.MarketRepository;
import org.weareadaptive.domain.repository.UserRepository;
import org.weareadaptive.util.SbeFactory;

import java.nio.ByteBuffer;

public class SnapshotService implements FragmentHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotService.class);
    private static final int RETRY_COUNT = 3;
    private IdleStrategy idleStrategy;

    private final MarketRepository marketRepository;
    private final LimitRepository limitRepository;
    private final UserRepository userRepository;
    private final SbeFactory sf = SbeFactory.sbeFactory();

    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    final UnsafeBuffer buffer = new UnsafeBuffer(byteBuffer);

    private boolean snapshotFullyLoaded = false;

    public SnapshotService(final MarketRepository marketRepository,
                           final LimitRepository limitRepository,
                           final UserRepository userRepository)
    {
        this.marketRepository = marketRepository;
        this.limitRepository = limitRepository;
        this.userRepository = userRepository;
    }

    public void handleTakeSnapshot(final ExclusivePublication snapshotPublication)
    {

        marketRepository.getAllOrders().forEach(order ->
        {
            sf.headerEncoder().wrap(buffer, 0)
                            .blockLength(sf.marketOrderEncoder().sbeBlockLength())
                                    .templateId(sf.marketOrderEncoder().sbeTemplateId())
                                            .version(sf.marketOrderEncoder().sbeSchemaVersion());

            sf.marketOrderEncoder().wrap(buffer, sf.headerEncoder().encodedLength());

            final FixedStringEncodingEncoder instrumentEncoder = sf.marketOrderEncoder().instrument();
            instrumentEncoder.string(order.instrument());

            final Side side = sf.fromString(order.side());

            sf.marketOrderEncoder()
                    .side(side)
                    .orderId(order.orderId())
                    .userId(order.userId())
                    .quantity(order.quantity())
                    .timestamp(order.timestamp());


            final int length = sf.marketOrderEncoder().encodedLength() + sf.headerEncoder().encodedLength();
            retryingOffer(snapshotPublication, buffer, length);
        });



        limitRepository.getAllOrders().forEach(order ->
        {
            sf.headerEncoder().wrap(buffer, 0)
                    .blockLength(sf.limitOrderEncoder().sbeBlockLength())
                    .templateId(sf.limitOrderEncoder().sbeTemplateId())
                    .version(sf.limitOrderEncoder().sbeSchemaVersion());

            sf.limitOrderEncoder().wrap(buffer, sf.headerEncoder().encodedLength());

            final FixedStringEncodingEncoder instrumentEncoder = sf.limitOrderEncoder().instrument();
            instrumentEncoder.string(order.instrument());

            final Side side = sf.fromString(order.side());

            sf.limitOrderEncoder()
                    .side(side)
                    .orderId(order.orderId())
                    .userId(order.userId())
                    .limitPrice(order.limitPrice())
                    .quantity(order.quantity())
                    .timestamp(order.timestamp());

            final int length = sf.snapshotEncoder().encodedLength() + sf.headerEncoder().encodedLength();
            retryingOffer(snapshotPublication, buffer, length);
        });

        userRepository.getAllUsers().forEach(user ->
        {
            sf.headerEncoder().wrap(buffer, 0)
                    .blockLength(sf.userEncoder().sbeBlockLength())
                    .templateId(sf.userEncoder().sbeTemplateId())
                    .version(sf.userEncoder().sbeSchemaVersion());

            sf.userEncoder().wrap(buffer, sf.headerEncoder().encodedLength());

            sf.userEncoder().userId(user.userId());

            final FixedStringEncodingEncoder usernameEncoder = sf.userEncoder().username();
            usernameEncoder.string(user.username());

            final int length = sf.snapshotEncoder().encodedLength() + sf.headerEncoder().encodedLength();
            retryingOffer(snapshotPublication, buffer, length);
        });

        LOGGER.info("Took snapshot of order book repositories. market: {}, limit: {}", marketRepository.getAllOrders(), limitRepository.getAllOrders());
    }

    public void loadSnapshot(final Image snapshotImage)
    {
        LOGGER.info("Loading snapshot");
        snapshotFullyLoaded = false;

        idleStrategy.reset();
        LOGGER.info("Snapshot pos: {}", snapshotImage.position());
        while (!snapshotImage.isEndOfStream())
        {
            idleStrategy.idle(snapshotImage.poll(this, 20));
        }

        if (!snapshotFullyLoaded)
        {
            LOGGER.warn("Snapshot load not completed - waiting for end of snapshot marker");
        }
        snapshotFullyLoaded = true;

        LOGGER.info("Snapshot pos: {}", snapshotImage.position());
        LOGGER.info("snapshotFullyLoaded: {}", snapshotFullyLoaded);
    }

    public void setIdleStrategy(final IdleStrategy idleStrategy)
    {
        this.idleStrategy = idleStrategy;
    }

    @Override
    public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header)
    {
        sf.headerDecoder().wrap(buffer, offset);
        final int blockLength = sf.headerDecoder().blockLength();
        final int templateId = sf.headerDecoder().templateId();
        final int version = sf.headerDecoder().version();

        switch (templateId)
        {
            case MarketOrderDecoder.TEMPLATE_ID ->
            {
                sf.marketOrderDecoder().wrap(buffer, offset + sf.headerDecoder().encodedLength(), blockLength, version);

                final FixedStringEncodingDecoder instrumentDecoder = sf.marketOrderDecoder().instrument();
                final String instrument = instrumentDecoder.string();
                final String side = sf.toString(sf.marketOrderDecoder().side());

                final MarketOrder marketOrder = new MarketOrder(
                        instrument,
                        side,
                        sf.marketOrderDecoder().orderId(),
                        sf.marketOrderDecoder().userId(),
                        sf.marketOrderDecoder().quantity(),
                        sf.marketOrderDecoder().timestamp()
                );

                marketRepository.put(marketOrder.orderId(), marketOrder);
                LOGGER.info("marketRepository: {}", marketRepository.getAllOrders());
            }
            case LimitOrderDecoder.TEMPLATE_ID ->
            {
                sf.limitOrderDecoder().wrap(buffer, offset + sf.headerDecoder().encodedLength(), blockLength, version);

                final FixedStringEncodingDecoder instrumentDecoder = sf.limitOrderDecoder().instrument();
                final String instrument = instrumentDecoder.string();
                final String side = sf.toString(sf.limitOrderDecoder().side());

                final LimitOrder limitOrder = new LimitOrder(
                        instrument,
                        side,
                        sf.limitOrderDecoder().orderId(),
                        sf.limitOrderDecoder().userId(),
                        sf.limitOrderDecoder().limitPrice(),
                        sf.limitOrderDecoder().quantity(),
                        sf.limitOrderDecoder().timestamp()
                );

                limitRepository.put(limitOrder.orderId(), limitOrder);
                LOGGER.info("limitRepository: {}", limitRepository.getAllOrders());
            }
            case UserDecoder.TEMPLATE_ID ->
            {
                sf.userDecoder().wrap(buffer, offset + sf.headerDecoder().encodedLength(), blockLength, version);

                final long userId = sf.userDecoder().userId();

                final FixedStringEncodingDecoder usernameDecoder = sf.userDecoder().username();
                final String username = usernameDecoder.string();

                final User user = new User(userId, username);

                userRepository.put(userId, user);
                LOGGER.info("userRepository: {}", userRepository.getAllUsers());
            }
        }
    }

    private void retryingOffer(final ExclusivePublication publication, final DirectBuffer buffer, final int length)
    {
        final int offset = 0;
        int retries = 0;
        do
        {
            final long result = publication.offer(buffer, offset, length);
            if (result > 0L)
            {
                return;
            }
            else if (result == Publication.ADMIN_ACTION || result == Publication.BACK_PRESSURED)
            {
                LOGGER.warn("backpressure or admin action on snapshot");
            }
            else if (result == Publication.NOT_CONNECTED || result == Publication.MAX_POSITION_EXCEEDED)
            {
                LOGGER.error("unexpected publication state on snapshot: {}", result);
                return;
            }
            idleStrategy.idle();
            retries += 1;
        }
        while (retries < RETRY_COUNT);

        LOGGER.error("failed to offer snapshot within {} retries", RETRY_COUNT);
    }
}
