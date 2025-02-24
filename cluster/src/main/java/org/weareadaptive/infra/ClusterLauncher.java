package org.weareadaptive.infra;

import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.samples.cluster.ClusterConfig;
import org.agrona.ErrorHandler;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weareadaptive.domain.OrderBook;
import org.weareadaptive.domain.repository.LimitRepository;
import org.weareadaptive.domain.repository.MarketRepository;
import org.weareadaptive.domain.repository.UserRepository;
import org.weareadaptive.infra.responder.TraderResponder;
import org.weareadaptive.infra.session.ClientSessionServiceImpl;
import org.weareadaptive.service.OrderService;

import java.util.List;

public class ClusterLauncher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterLauncher.class);

    public static void main(final String[] args)
    {
        if (args.length < 2)
        {
            System.err.println("Usage: NodeLauncher <nodeId> <ingressChannel>");
            System.exit(1);
        }
        else if (args.length > 2)
        {
            System.err.println("Too many args");
            System.exit(1);
        }

        final int nodeId = Integer.parseInt(args[0]);
        final String ingressChannel = args[1];


        final MarketRepository marketRepository = new MarketRepository();
        final LimitRepository limitRepository = new LimitRepository();
        final UserRepository userRepository = new UserRepository();

        final OrderBook orderBook = new OrderBook(marketRepository, limitRepository);

        final ClientSessionServiceImpl clientSessionService = new ClientSessionServiceImpl();
        final TraderResponder traderResponder = new TraderResponder(clientSessionService);
        final OrderService orderService = new OrderService(marketRepository, limitRepository, userRepository, orderBook, traderResponder);
        final IngressAdapter ingressAdapter = new IngressAdapter(orderService);
        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

        final ClusterConfig clusterConfig = ClusterConfig.create(
                nodeId,
                List.of("localhost", "localhost", "localhost"),
                List.of("localhost", "localhost", "localhost"),
                9000,
                new TradingClusteredService(clientSessionService, orderService, ingressAdapter));

        clusterConfig.mediaDriverContext().errorHandler(errorHandler("Media Driver"));
        clusterConfig.archiveContext().errorHandler(errorHandler("Archive"));
        clusterConfig.aeronArchiveContext().errorHandler(errorHandler("Aeron Archive"));
        clusterConfig.consensusModuleContext().errorHandler(errorHandler("Consensus Module"));
        clusterConfig.clusteredServiceContext().errorHandler(errorHandler("Clustered Service"));
        clusterConfig.consensusModuleContext().ingressChannel(ingressChannel);
        clusterConfig.consensusModuleContext().deleteDirOnStart(true);
        //TODO change to false when snapshotting

        try (ClusteredMediaDriver ignore = ClusteredMediaDriver.launch(
                clusterConfig.mediaDriverContext(),
                clusterConfig.archiveContext(),
                clusterConfig.consensusModuleContext());
             ClusteredServiceContainer ignore1 = ClusteredServiceContainer.launch(
                     clusterConfig.clusteredServiceContext()))
        {

            LOGGER.info("Started Cluster Node...");
            barrier.await();
            LOGGER.info("Exiting");
        }
    }

    private static ErrorHandler errorHandler(final String context)
    {
        return (Throwable throwable) ->
        {
            System.err.println(context);
            throwable.printStackTrace(System.err);
        };
    }
}
