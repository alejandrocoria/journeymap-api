/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;

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
        for (KeyBinding kb : Constants.initKeybindings())
        {
            ClientRegistry.registerKeyBinding(kb);
        }
    }

    public static boolean onKeypress(boolean minimapOnly)
    {
        final int i = Keyboard.getEventKey();
        MapOverlayState mapOverlayState = Fullscreen.state();

        try
        {
            if (JourneyMap.getMiniMapProperties().enableHotkeys.get())
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
                    mapOverlayState.zoomIn();
                    return true;
                }
                else if (Constants.isPressed(Constants.KB_MAP_ZOOMOUT))
                {
                    mapOverlayState.zoomOut();
                    return true;
                }
                else if (Constants.isPressed(Constants.KB_MAP_DAY))
                {
                    mapOverlayState.setMapType(Constants.MapType.day);
                    return true;
                }
                else if (Constants.isPressed(Constants.KB_MAP_NIGHT))
                {
                    mapOverlayState.setMapType(Constants.MapType.night);
                    return true;
                }
                else if (Constants.isPressed(Constants.KB_MINIMAP_POS))
                {
                    JourneyMap.toggleMiniMapPreset();
                    UIManager.getInstance().resetMinimap();
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
                            UIManager.getInstance().openMap();
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

