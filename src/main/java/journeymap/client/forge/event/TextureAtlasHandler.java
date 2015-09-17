package journeymap.client.forge.event;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ColorManager;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.LogFormatter;
import journeymap.client.task.main.IMainThreadTask;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;

/**
 * Handles events related to the TextureAtlas for Blocks.
 */
public class TextureAtlasHandler implements EventHandlerManager.EventHandler
{
    IMainThreadTask task = new Task();

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent()
    public void onTextureStiched(TextureStitchEvent.Post event)
    {
        try
        {
            JourneymapClient.getInstance().queueMainThreadTask(task);
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Error queuing TextureAtlasHandlerTask: " + LogFormatter.toString(e));
        }
    }

    /**
     * Initialize the blocks texture image used for color derivation,
     * ensure the color manager palette is current.
     */
    class Task implements IMainThreadTask
    {
        @Override
        public IMainThreadTask perform(Minecraft mc, JourneymapClient jm)
        {
            ForgeHelper.INSTANCE.getColorHelper().initBlocksTexture();
            ColorManager.instance().ensureCurrent();
            return null;
        }

        @Override
        public String getName()
        {
            return "TextureAtlasHandlerTask";
        }
    }
}
