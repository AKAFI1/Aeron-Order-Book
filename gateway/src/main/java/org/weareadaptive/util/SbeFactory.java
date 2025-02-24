package org.weareadaptive.util;

import com.weareadaptive.sbe.*;

public final class SbeFactory
{
    private static SbeFactory instance = new SbeFactory();

    private final MessageHeaderDecoder headerDecoder;
    private final MessageHeaderEncoder headerEncoder;

    private final FixedStringEncodingDecoder fixedStringDecoder;
    private final FixedStringEncodingEncoder fixedStringEncoder;

    private final ActionResultDecoder resultDecoder;
    private final ActionResultEncoder resultEncoder;

    private final MarketRequestDecoder marketRequestDecoder;
    private final MarketRequestEncoder marketRequestEncoder;

    private final LimitOrderRequestDecoder limitRequestDecoder;
    private final LimitOrderRequestEncoder limitRequestEncoder;

    private SbeFactory()
    {
        headerDecoder = new MessageHeaderDecoder();
        headerEncoder = new MessageHeaderEncoder();

        fixedStringDecoder = new FixedStringEncodingDecoder();
        fixedStringEncoder = new FixedStringEncodingEncoder();

        resultDecoder = new ActionResultDecoder();
        resultEncoder = new ActionResultEncoder();

        marketRequestDecoder = new MarketRequestDecoder();
        marketRequestEncoder = new MarketRequestEncoder();

        limitRequestDecoder = new LimitOrderRequestDecoder();
        limitRequestEncoder = new LimitOrderRequestEncoder();

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
