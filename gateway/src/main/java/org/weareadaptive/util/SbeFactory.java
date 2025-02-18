package org.weareadaptive.util;

import com.weareadaptive.sbe.*;

public final class SbeFactory {
    private static SbeFactory instance = new SbeFactory();

    private final MessageHeaderDecoder headerDecoder;
    private final MessageHeaderEncoder headerEncoder;

    private final ActionResultDecoder actionResultDecoder;
    private final ActionResultEncoder actionResultEncoder;

    private final MarketRequestDecoder marketRequestDecoder;
    private final MarketRequestEncoder marketRequestEncoder;

    private final LimitOrderRequestDecoder limitRequestDecoder;
    private final LimitOrderRequestEncoder limitRequestEncoder;

    private final StopOrderRequestDecoder stopRequestDecoder;
    private final StopOrderRequestEncoder stopRequestEncoder;

    private SbeFactory() {
        headerDecoder = new MessageHeaderDecoder();
        headerEncoder = new MessageHeaderEncoder();

        actionResultDecoder = new ActionResultDecoder();
        actionResultEncoder = new ActionResultEncoder();

        marketRequestDecoder = new MarketRequestDecoder();
        marketRequestEncoder = new MarketRequestEncoder();

        limitRequestDecoder = new LimitOrderRequestDecoder();
        limitRequestEncoder = new LimitOrderRequestEncoder();

        stopRequestDecoder = new StopOrderRequestDecoder();
        stopRequestEncoder = new StopOrderRequestEncoder();
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

    public ActionResultDecoder actionResultDecoder()
    {
        return actionResultDecoder;
    }

    public ActionResultEncoder actionResultEncoder()
    {
        return actionResultEncoder;
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

    public StopOrderRequestDecoder stopRequestDecoder()
    {
        return stopRequestDecoder;
    }

    public StopOrderRequestEncoder stopRequestEncoder()
    {
        return stopRequestEncoder;
    }
}