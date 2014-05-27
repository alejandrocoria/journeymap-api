package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
public class KeyEventHandler implements EventHandlerManager.EventHandler {

    public KeyEventHandler()
    {
        for(KeyBinding kb : Constants.KEYBINDINGS)
        {
            ClientRegistry.registerKeyBinding(kb);
        }
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
        KeyEventHandler.onKeypress(false);
    }

    public static void onKeypress(boolean minimapOnly)
    {
        final int i = Keyboard.getEventKey();
        MapOverlayState mapOverlayState = MapOverlay.state();

        if (GuiScreen.isCtrlKeyDown() && JourneyMap.getInstance().miniMapProperties.enableHotkeys.get())
        {

            if (Constants.isPressed(Constants.KB_MAP))
            {
                UIManager.getInstance().toggleMinimap();
                return;
            }
            else
            {
                if (Constants.isPressed(Constants.KB_MAP_ZOOMIN))
                {
                    mapOverlayState.zoomIn();
                    return;
                }
                else
                {
                    if (Constants.isPressed(Constants.KB_MAP_ZOOMOUT))
                    {
                        mapOverlayState.zoomOut();
                        return;
                    }
                    else
                    {
                        if (Constants.isPressed(Constants.KB_MAP_DAY))
                        {
                            mapOverlayState.overrideMapType(Constants.MapType.day);
                            return;
                        }
                        else
                        {
                            if (Constants.isPressed(Constants.KB_MAP_NIGHT))
                            {
                                mapOverlayState.overrideMapType(Constants.MapType.night);
                                return;
                            }
                            else
                            {
                                if (Constants.isPressed(Constants.KB_MINIMAP_POS))
                                {
                                    UIManager.getInstance().getMiniMap().nextPosition();
                                    return;
                                }
                                else
                                {
                                    if (Constants.isPressed(Constants.KB_WAYPOINT))
                                    {
                                        UIManager.getInstance().openWaypointManager(null);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            if (!minimapOnly)
            {
                if (Constants.KB_MAP.isPressed())
                {
                    if (Minecraft.getMinecraft().currentScreen == null)
                    {
                        UIManager.getInstance().openMap();
                    }
                    else
                    {
                        if (Minecraft.getMinecraft().currentScreen instanceof MapOverlay)
                        {
                            UIManager.getInstance().closeAll();
                        }
                    }
                    return;
                }
                else
                {
                    if (Constants.KB_WAYPOINT.isPressed())
                    {
                        if (Minecraft.getMinecraft().currentScreen == null)
                        {
                            Waypoint waypoint = Waypoint.of(Minecraft.getMinecraft().thePlayer);
                            UIManager.getInstance().openWaypointEditor(waypoint, true, null);
                        }
                        return;
                    }
                }
            }
        }
    }
}

