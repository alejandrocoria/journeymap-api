package journeymap.server.feature;

import journeymap.common.Journeymap;
import journeymap.common.thread.JMThreadFactory;
import journeymap.server.properties.PropertiesManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerMonitor
{
    private static PlayerMonitor monitor;
    private PlayerTrackingThread playerTracker;
    private final Logger logger = Journeymap.getLogger();

    private PlayerMonitor()
    {
    }

    public static void init()
    {
        monitor = new PlayerMonitor();
        monitor.startPlayerTracker();
    }

    private void startPlayerTracker()
    {
        logger.info("Starting player tracking thread.");
        playerTracker = new PlayerTrackingThread(
                PropertiesManager.getInstance().getGlobalProperties().playerTrackingUpdateTime.get(),
                PropertiesManager.getInstance().getGlobalProperties().playerTrackingEnabled.get(),
                PropertiesManager.getInstance().getGlobalProperties().opPlayerTrackingEnabled.get()
        );
        // Init thread factory
        JMThreadFactory pt = new JMThreadFactory("player_tracker");

        // Run server in own thread
        ExecutorService es = Executors.newSingleThreadExecutor(pt);
        es.execute(playerTracker);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(pt.newThread(new Runnable()
        {
            @Override
            public void run()
            {
                stop();
            }
        }));
    }

    private void stop()
    {
        try
        {
            if (playerTracker.isAlive())
            {
                playerTracker.stop();
                logger.info("Stopped player monitor without errors");
            }
        }
        catch (Throwable t)
        {
            logger.info("Stopped player monitor with error: " + t);
        }
    }
}
