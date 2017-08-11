/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;


import journeymap.client.Constants;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/**
 * Key even handling + init code for key bindings.
 */
public class KeyEventHandler implements EventHandlerManager.EventHandler
{
    /**
     * On keyboard event.
     *
     * @param event the event
     */
    @SubscribeEvent()
    public void onKeyboardEvent(InputEvent.KeyInputEvent event)
    {
        boolean clearKeys = KeyEventHandler.onKeypress(false);
        if (clearKeys)
        {
            KeyBinding.unPressAllKeys();
        }
    }

    /**
     * Init key bindings.
     */
    public static void initKeyBindings()
    {
        for (KeyBinding kb : Constants.initKeybindings())
        {
            try
            {
                ClientRegistry.registerKeyBinding(kb);
            }
            catch (Throwable t)
            {
                ChatLog.announceError("Unexpected error when registering keybinding : " + kb);
            }
        }
    }

    /**
     * On keypress boolean.
     *
     * @param minimapOnly the minimap only
     * @return the boolean
     */
    public static boolean onKeypress(boolean minimapOnly)
    {
        try
        {
            if (Constants.KB_MINIMAP_TOGGLE.isKeyDown())
            {
                UIManager.INSTANCE.toggleMinimap();
                return true;
            }
            else if (Constants.KB_MINIMAP_ZOOMIN.isKeyDown())
            {
                MiniMap.state().zoomIn();
                return false;
            }
            else if (Constants.KB_MINIMAP_ZOOMOUT.isKeyDown())
            {
                MiniMap.state().zoomOut();
                return false;
            }
            else if (Constants.KB_MINIMAP_TYPE.isKeyDown())
            {
                MiniMap.state().toggleMapType();
                return false;
            }
            else if (Constants.KB_MINIMAP_PRESET.isKeyDown())
            {
                UIManager.INSTANCE.switchMiniMapPreset();
                return true;
            }
            else if (Constants.KB_WAYPOINT_MANAGER.isKeyDown())
            {
                UIManager.INSTANCE.openWaypointManager(null, null);
                return true;
            }

            if (!minimapOnly)
            {
                if (Constants.KB_FULLSCREEN.isPressed())
                {
                    if (FMLClientHandler.instance().getClient().currentScreen == null)
                    {
                        UIManager.INSTANCE.openFullscreenMap();
                    }
                    else
                    {
                        if (FMLClientHandler.instance().getClient().currentScreen instanceof Fullscreen)
                        {
                            UIManager.INSTANCE.closeAll();
                        }
                    }
                    return true;
                }
                else
                {
                    if (Constants.KB_CREATE_WAYPOINT.isPressed())
                    {
                        if (FMLClientHandler.instance().getClient().currentScreen == null)
                        {
                            Minecraft mc = FMLClientHandler.instance().getClient();
                            Waypoint waypoint = Waypoint.of(mc.player);
                            UIManager.INSTANCE.openWaypointEditor(waypoint, true, null);
                        }
                        return true;
                    }
                }
            }
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error during keypress: " + LogFormatter.toPartialString(e));
        }
        return false;
    }
}

