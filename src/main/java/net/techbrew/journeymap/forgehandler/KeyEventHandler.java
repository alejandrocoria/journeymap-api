package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
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

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
public class KeyEventHandler extends KeyBindingRegistry.KeyHandler {

    private static boolean[] boolarray;

    static
    {
        boolarray = new boolean[Constants.KEYBINDINGS.length];
        Arrays.fill(boolarray, false);
    }

    public KeyEventHandler()
    {
        super(Constants.KEYBINDINGS, boolarray);
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
    {
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
    {
        if (tickEnd)
        {
            KeyEventHandler.onKeypress(false);
        }
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

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel()
    {
        return getClass().getName();
    }
}

