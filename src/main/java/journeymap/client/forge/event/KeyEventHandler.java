/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;


import journeymap.client.Constants;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.render.map.Tile;
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
import org.lwjgl.input.Keyboard;

import java.util.HashSet;

/**
 * Key even handling + init code for key bindings.
 */
public class KeyEventHandler implements EventHandlerManager.EventHandler
{
    @SubscribeEvent()
    public void onKeyboardEvent(InputEvent.KeyInputEvent event)
    {
        boolean clearKeys = KeyEventHandler.onKeypress(false);
        if (clearKeys)
        {
            KeyBinding.unPressAllKeys();
        }
    }

    public static void initKeyBindings()
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        HashSet<String> keyDescs = new HashSet<String>();
        for (KeyBinding existing : minecraft.gameSettings.keyBindings)
        {
            try
            {
                if (existing != null && existing.getKeyDescription() != null)
                {
                    keyDescs.add(existing.getKeyDescription());
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error("Unexpected error when checking existing keybinding : " + existing);
            }
        }

        for (KeyBinding kb : Constants.initKeybindings())
        {
            try
            {
                if (!keyDescs.contains(kb.getKeyDescription()))
                {
                    ClientRegistry.registerKeyBinding(kb);
                }
                else
                {
                    Journeymap.getLogger().warn("Avoided duplicate keybinding that was already registered: " + kb.getKeyDescription());
                }
            }
            catch (Throwable t)
            {
                ChatLog.announceError("Unexpected error when registering keybinding : " + kb);
            }
        }
    }

    public static boolean onKeypress(boolean minimapOnly)
    {
        try
        {
            boolean controlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

            if (controlDown && Constants.KB_MAP.isKeyDown())
            {
                UIManager.INSTANCE.toggleMinimap();
                return true;
            }
            else if (controlDown && Constants.KB_MAP_ZOOMIN.isKeyDown())
            {
                Tile.switchTileRenderType();
                return false;
            }
            else if (controlDown && Constants.KB_MAP_ZOOMOUT.isKeyDown())
            {
                Tile.switchTileDisplayQuality();
                return false;
            }
            else if (Constants.KB_MAP_ZOOMIN.isKeyDown())
            {
                MiniMap.state().zoomIn();
                return false;
            }
            else if (Constants.KB_MAP_ZOOMOUT.isKeyDown())
            {
                MiniMap.state().zoomOut();
                return false;
            }
            else if (Constants.KB_MAP_DAY.isKeyDown() || Constants.KB_MAP_NIGHT.isKeyDown())
            {
                MiniMap.state().toggleMapType();
                return false;
            }
            else if (Constants.KB_MINIMAP_PRESET.isKeyDown())
            {
                UIManager.INSTANCE.switchMiniMapPreset();
                return true;
            }
            else if (controlDown && Constants.KB_WAYPOINT.isKeyDown())
            {
                UIManager.INSTANCE.openWaypointManager(null, null);
                return true;
            }

            if (!minimapOnly)
            {
                if (Constants.KB_MAP.isKeyDown())
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
                    if (Constants.KB_WAYPOINT.isKeyDown())
                    {
                        if (FMLClientHandler.instance().getClient().currentScreen == null)
                        {
                            Minecraft mc = FMLClientHandler.instance().getClient();
                            Waypoint waypoint = Waypoint.of(mc.thePlayer);
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

