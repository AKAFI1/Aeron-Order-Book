package org.weareadaptive.util;

import com.weareadaptive.sbe.*;

public final class SbeFactory
{
    private static SbeFactory instance = new SbeFactory();

    private final MessageHeaderDecoder headerDecoder;
    private final MessageHeaderEncoder headerEncoder;

    private final FixedStringEncodingDecoder fixedStringDecoder;
    private final FixedStringEncodingEncoder fixedStringEncoder;

    private final FixedSideDecoder fixedSideDecoder;
    private final FixedSideEncoder fixedSideEncoder;

    private final ActionResultDecoder resultDecoder;
    private final ActionResultEncoder resultEncoder;

    private final MarketRequestDecoder marketRequestDecoder;
    private final MarketRequestEncoder marketRequestEncoder;

    private final LimitOrderRequestDecoder limitRequestDecoder;
    private final LimitOrderRequestEncoder limitRequestEncoder;

    private final MarketOrderDecoder marketOrderDecoder;
    private final MarketOrderEncoder marketOrderEncoder;

    private final LimitOrderDecoder limitOrderDecoder;
    private final LimitOrderEncoder limitOrderEncoder;

    private final UserDecoder userDecoder;
    private final UserEncoder userEncoder;

    private final SnapshotDecoder snapshotDecoder;
    private final SnapshotEncoder snapshotEncoder;


    private SbeFactory()
    {
        headerDecoder = new MessageHeaderDecoder();
        headerEncoder = new MessageHeaderEncoder();

        fixedStringDecoder = new FixedStringEncodingDecoder();
        fixedStringEncoder = new FixedStringEncodingEncoder();

        fixedSideDecoder = new FixedSideDecoder();
        fixedSideEncoder = new FixedSideEncoder();

        resultDecoder = new ActionResultDecoder();
        resultEncoder = new ActionResultEncoder();

        marketRequestDecoder = new MarketRequestDecoder();
        marketRequestEncoder = new MarketRequestEncoder();

        limitRequestDecoder = new LimitOrderRequestDecoder();
        limitRequestEncoder = new LimitOrderRequestEncoder();

        marketOrderDecoder = new MarketOrderDecoder();
        marketOrderEncoder = new MarketOrderEncoder();

        limitOrderDecoder = new LimitOrderDecoder();
        limitOrderEncoder = new LimitOrderEncoder();

        userDecoder = new UserDecoder();
        userEncoder = new UserEncoder();

        snapshotDecoder = new SnapshotDecoder();
        snapshotEncoder = new SnapshotEncoder();
    }

    public static SbeFactory sbeFactory()
    {
        return instance;
    }

    public MessageHeaderDecoder headerDecoder()
    {
        return headerDecoder;
    }

    public MessageHeaderEncoder headerEncoder()
    {
        return headerEncoder;
    }

    public FixedSideDecoder fixedSideDecoder()
    {
        return fixedSideDecoder;
    }

    public FixedSideEncoder fixedSideEncoder()
    {
        return fixedSideEncoder;
    }

    public FixedStringEncodingDecoder stringDecoder()
    {
        return fixedStringDecoder;
    }

    public FixedStringEncodingEncoder stringEncoder()
    {
        return fixedStringEncoder;
    }

    public ActionResultDecoder resultDecoder()
    {
        return resultDecoder;
    }

    public ActionResultEncoder resultEncoder()
    {
        return resultEncoder;
    }

    public MarketRequestDecoder marketRequestDecoder()
    {
        return marketRequestDecoder;
    }

    public MarketRequestEncoder marketRequestEncoder()
    {
        return marketRequestEncoder;
    }

    public LimitOrderRequestDecoder limitRequestDecoder()
    {
        return limitRequestDecoder;
    }

    public LimitOrderRequestEncoder limitRequestEncoder()
    {
        return limitRequestEncoder;
    }

    public MarketOrderDecoder marketOrderDecoder()
    {
        return marketOrderDecoder;
    }

    public MarketOrderEncoder marketOrderEncoder()
    {
        return marketOrderEncoder;
    }

    public LimitOrderDecoder limitOrderDecoder()
    {
        return limitOrderDecoder;
    }

    public LimitOrderEncoder limitOrderEncoder()
    {
        return limitOrderEncoder;
    }

    public UserDecoder userDecoder()
    {
        return userDecoder;
    }

    public UserEncoder userEncoder()
    {
        return userEncoder;
    }

    public SnapshotDecoder snapshotDecoder()
    {
        return snapshotDecoder;
    }

    public SnapshotEncoder snapshotEncoder()
    {
        return snapshotEncoder;
    }

    public String toString(final Side side)
    {
        switch (side)
        {
            case BID:
                return "BID";
            case ASK:
                return "ASK";
            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }
    }

    public Side fromString(final String side)
    {
        return switch (side.toUpperCase())
        {
            case "BID", "BUY" -> Side.BID;
            case "ASK", "SELL" -> Side.ASK;
            default -> throw new IllegalArgumentException("Unknown side: " + side);
        };
    }
}
