/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forge.event;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import net.techbrew.journeymap.ui.minimap.MiniMap;
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
        HashSet<String> keyDescs = new HashSet<String>();
        for (KeyBinding existing : Minecraft.getMinecraft().gameSettings.keyBindings)
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
                JourneyMap.getLogger().error("Unexpected error when checking existing keybinding : " + existing);
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
                    JourneyMap.getLogger().warn("Avoided duplicate keybinding that was already registered: " + kb.getKeyDescription());
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
            else if (Constants.isPressed(Constants.KB_MAP_DAY))
            {
                MiniMap.state().setMapType(Constants.MapType.day);
                return true;
            }
            else if (Constants.isPressed(Constants.KB_MAP_NIGHT))
            {
                MiniMap.state().setMapType(Constants.MapType.night);
                return true;
            }
            else if (Constants.isPressed(Constants.KB_MINIMAP_PRESET))
            {
                UIManager.getInstance().switchMiniMapPreset();
                return true;
            }
            else if (controlDown && Constants.isPressed(Constants.KB_WAYPOINT))
            {
                UIManager.getInstance().openWaypointManager(null, null);
                return true;
            }

            if (!minimapOnly)
            {
                if (Constants.KB_MAP.isPressed())
                {
                    if (FMLClientHandler.instance().getClient().currentScreen == null)
                    {
                        UIManager.getInstance().openFullscreenMap();
                    }
                    else
                    {
                        if (FMLClientHandler.instance().getClient().currentScreen instanceof Fullscreen)
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
                        if (FMLClientHandler.instance().getClient().currentScreen == null)
                        {
                            Minecraft mc = FMLClientHandler.instance().getClient();
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

