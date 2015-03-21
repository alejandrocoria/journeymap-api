package net.techbrew.journeymap.task.main;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.forgehandler.MiniMapOverlayHandler;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.render.map.TileDrawStepCache;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.apache.logging.log4j.Logger;

/**
 * Checks state to start/stop mapping (code formerly in JourneyMap.java)
 */
public class SoftResetTask implements IMainThreadTask
{
    private static String NAME = "Tick." + SoftResetTask.class.getSimpleName();
    Logger logger = JourneyMap.getLogger();

    private SoftResetTask()
    {
    }

    public static void queue()
    {
        JourneyMap.getInstance().queueMainThreadTask(new SoftResetTask());
    }

    @Override
    public IMainThreadTask perform(Minecraft mc, JourneyMap jm)
    {
        jm.loadConfigProperties();
        JMLogger.setLevelFromProperties();
        DataCache.instance().purge();
        DataCache.instance().resetBlockMetadata();
        TileDrawStepCache.instance().invalidateAll();
        UIManager.getInstance().reset();
        WaypointStore.instance().reset();
        MiniMapOverlayHandler.checkEventConfig();
        ThemeFileHandler.getCurrentTheme(true);
        UIManager.getInstance().getMiniMap().updateDisplayVars(true);
        return null;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
