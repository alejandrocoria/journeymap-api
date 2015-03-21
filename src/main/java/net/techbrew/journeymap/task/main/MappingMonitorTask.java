package net.techbrew.journeymap.task.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import org.apache.logging.log4j.Logger;

/**
 * Checks state to start/stop mapping (code formerly in JourneyMap.java)
 */
public class MappingMonitorTask implements IMainThreadTask
{
    private static String NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
    Logger logger = JourneyMap.getLogger();

    @Override
    public IMainThreadTask perform(Minecraft mc, JourneyMap jm)
    {
        StatTimer timer = StatTimer.getDisposable("JourneyMap.performMainThreadTasks", 200).start();
        //long start = System.nanoTime();
        try
        {
            if (!jm.isInitialized())
            {
                return this;
            }

            final boolean isDead = mc.currentScreen != null && mc.currentScreen instanceof GuiGameOver;

            if (mc.theWorld == null)
            {
                if (jm.isMapping())
                {
                    jm.stopMapping();
                }

                GuiScreen guiScreen = mc.currentScreen;
                if (guiScreen instanceof GuiMainMenu ||
                        guiScreen instanceof GuiSelectWorld ||
                        guiScreen instanceof GuiMultiplayer)
                {
                    if (jm.getCurrentWorldId() != null)
                    {
                        logger.info("World ID has been reset.");
                        jm.setCurrentWorldId(null);
                    }
                }

                return this;
            }
            else
            {
                if (!jm.isMapping() && !isDead && JourneyMap.getCoreProperties().mappingEnabled.get())
                {
                    jm.startMapping();
                }
            }

            final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof Fullscreen);
            if (isGamePaused)
            {
                if (!jm.isMapping())
                {
                    return this;
                }
            }

            // Show announcements
            if (!isGamePaused)
            {
                ChatLog.showChatAnnouncements(mc);
            }

            // Start Mapping
            if (!jm.isMapping() && JourneyMap.getCoreProperties().mappingEnabled.get())
            {
                jm.startMapping();
            }
        }
        catch (Throwable t)
        {
            logger.error("Error in JourneyMap.performMainThreadTasks(): " + LogFormatter.toString(t));
        }
        finally
        {
            timer.stop();
//            final double elapsedMs = (System.nanoTime() - start) / StatTimer.NS;
//            if (elapsedMs > 10)
//            {
//                // TODO remove
//                ChatLog.announceError(String.format("[%s] JourneyMap.performMainThreadTasks() too slow: %sms",
//                        new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()), elapsedMs));
//            }
        }
        return this;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
