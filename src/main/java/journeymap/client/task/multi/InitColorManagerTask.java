package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.color.ColorManager;
import net.minecraft.client.Minecraft;

import java.io.File;

/**
 * Initializes ColorManager
 */
public class InitColorManagerTask implements ITask
{
    @Override
    public int getMaxRuntime()
    {
        return 5000;
    }

    @Override
    public void performTask(Minecraft mc, JourneymapClient jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        ColorManager.INSTANCE.ensureCurrent(false);
    }

    public static class Manager implements ITaskManager
    {
        static boolean enabled = false;

        @Override
        public Class<? extends ITask> getTaskClass()
        {
            return InitColorManagerTask.class;
        }

        @Override
        public boolean enableTask(Minecraft minecraft, Object params)
        {
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
                return new InitColorManagerTask();
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
