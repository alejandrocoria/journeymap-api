package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ColorManager;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;

import java.io.File;

/**
 * Initializes block colors on BlockMDs that don't have one
 * already set via ColorPalette.
 */
public class InitBlockColorsTask implements ITask
{
    @Override
    public int getMaxRuntime()
    {
        return 5000;
    }

    @Override
    public void performTask(Minecraft mc, JourneymapClient jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        ColorManager.instance().initBlockColors();
    }

    public static class Manager implements ITaskManager
    {
        static boolean enabled = false;

        @Override
        public Class<? extends ITask> getTaskClass()
        {
            return InitBlockColorsTask.class;
        }

        @Override
        public boolean enableTask(Minecraft minecraft, Object params)
        {
            if (!ForgeHelper.INSTANCE.getColorHelper().hasBlocksTexture())
            {
                Journeymap.getLogger().debug("Can't run InitBlockColorsTask before BlocksTexture initialized");
                return false;
            }

            enabled = true;
            return true;
        }

        @Override
        public boolean isEnabled(Minecraft minecraft)
        {
            return enabled;
        }

        @Override
        public ITask getTask(Minecraft minecraft)
        {
            if (enabled)
            {
                return new InitBlockColorsTask();
            }
            else
            {
                return null;
            }
        }

        @Override
        public void taskAccepted(ITask task, boolean accepted)
        {
            enabled = false;
        }

        @Override
        public void disableTask(Minecraft minecraft)
        {
            enabled = false;
        }
    }
}
