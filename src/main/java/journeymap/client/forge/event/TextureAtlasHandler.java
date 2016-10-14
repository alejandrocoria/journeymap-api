package journeymap.client.forge.event;

import journeymap.client.render.texture.TextureCache;
import journeymap.client.task.main.EnsureCurrentColorsTask;
import journeymap.client.task.main.IMainThreadTask;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
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
    IMainThreadTask task = new EnsureCurrentColorsTask();

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
            TextureCache.INSTANCE.reset();
            UIManager.INSTANCE.getMiniMap().reset();
            Fullscreen.state().requireRefresh();
            MiniMap.state().requireRefresh();
            Journeymap.getClient().queueMainThreadTask(task);
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Error queuing TextureAtlasHandlerTask: " + LogFormatter.toString(e));
        }
    }
}
