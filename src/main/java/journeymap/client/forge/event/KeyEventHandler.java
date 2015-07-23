/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;


import journeymap.client.Constants;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.render.map.Tile;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;
import java.util.HashSet;

/**
 * Created by mwoodman on 1/29/14.
 */
public class KeyEventHandler implements EventHandlerManager.EventHandler
{

    public KeyEventHandler()
    {
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
        Minecraft minecraft = Minecraft.getMinecraft();
        final int i = Keyboard.getEventKey();

        try
        {
            // This seems to prevent the keycode from "staying"
            boolean controlDown = Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
            //GuiScreen.isCtrlKeyDown();

            if (controlDown && Constants.isPressed(Constants.KB_MAP))
            {
                UIManager.getInstance().toggleMinimap();
                return true;
            }
            else if (controlDown && Constants.isPressed(Constants.KB_MAP_ZOOMIN))
            {
                Tile.switchTileRenderType();
                return true;
            }
            else if (controlDown && Constants.isPressed(Constants.KB_MAP_ZOOMOUT))
            {
                Tile.switchTileDisplayQuality();
                return true;
            }
            else if (Constants.isPressed(Constants.KB_MAP_ZOOMIN))
            {
                MiniMap.state().zoomIn();
                return true;
            }
            else if (Constants.isPressed(Constants.KB_MAP_ZOOMOUT))
            {
                MiniMap.state().zoomOut();
                return true;
            }
            else if (Constants.isPressed(Constants.KB_MAP_DAY) || Constants.isPressed(Constants.KB_MAP_NIGHT))
            {
                MiniMap.state().toggleMapType();
                KeyBinding.unPressAllKeys();
                return true;
            }
            else if (Constants.isPressed(Constants.KB_MINIMAP_PRESET))
            {
                UIManager.getInstance().switchMiniMapPreset();
                return true;
            }
            else if (controlDown && Constants.isPressed(Constants.KB_WAYPOINT))
            {
                KeyBinding.unPressAllKeys();
                UIManager.getInstance().openWaypointManager(null, null);
                return true;
            }

            if (!minimapOnly)
            {
                if (Constants.KB_MAP.isPressed())
                {
                    if (ForgeHelper.INSTANCE.getClient().currentScreen == null)
                    {
                        UIManager.getInstance().openFullscreenMap();
                    }
                    else
                    {
                        if (ForgeHelper.INSTANCE.getClient().currentScreen instanceof Fullscreen)
                        {
                            UIManager.getInstance().closeAll();
                        }
                    }
                    return true;
                }
                else
                {
                    if (Constants.KB_WAYPOINT.isPressed())
                    {
                        if (ForgeHelper.INSTANCE.getClient().currentScreen == null)
                        {
                            Minecraft mc = ForgeHelper.INSTANCE.getClient();
                            Waypoint waypoint = Waypoint.of(mc.thePlayer);
                            UIManager.getInstance().openWaypointEditor(waypoint, true, null);
                        }
                        return true;
                    }
                }
            }
        }
        finally
        {

        }
        return false;
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    @SubscribeEvent()
    public void onKeyboardEvent(InputEvent.KeyInputEvent event)
    {
        KeyEventHandler.onKeypress(false);
    }
}
