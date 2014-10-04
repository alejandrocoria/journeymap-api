/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.render.map.TileCache;
import net.techbrew.journeymap.ui.component.JmUI;
import net.techbrew.journeymap.ui.dialog.FullscreenActions;
import net.techbrew.journeymap.ui.dialog.FullscreenHotkeysHelp;
import net.techbrew.journeymap.ui.dialog.OptionsManager;
import net.techbrew.journeymap.ui.dialog.StyleOptions;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import net.techbrew.journeymap.ui.minimap.MiniMap;
import net.techbrew.journeymap.ui.minimap.MiniMapHotkeysHelp;
import net.techbrew.journeymap.ui.waypoint.WaypointEditor;
import net.techbrew.journeymap.ui.waypoint.WaypointHelp;
import net.techbrew.journeymap.ui.waypoint.WaypointManager;
import net.techbrew.journeymap.ui.waypoint.WaypointOptions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class UIManager
{
    private final Logger logger = JourneyMap.getLogger();
    Minecraft minecraft = FMLClientHandler.instance().getClient();
    private MiniMap miniMap;

    private UIManager()
    {
        miniMap = new MiniMap();
    }

    public static UIManager getInstance()
    {
        return Holder.INSTANCE;
    }

    public void closeAll()
    {
        closeCurrent();
        minecraft.displayGuiScreen(null);
        minecraft.setIngameFocus();
        //miniMap.setVisible(true);
        TileCache.instance().cleanUp();
    }

    public void closeCurrent()
    {
        if (minecraft.currentScreen != null && minecraft.currentScreen instanceof JmUI)
        {
            logger.debug("Closing " + minecraft.currentScreen.getClass());
            ((JmUI) minecraft.currentScreen).close();
        }
        KeyBinding.unPressAllKeys();
    }

    public void openInventory()
    {
        logger.debug("Opening inventory");
        closeAll();
        minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer)); // displayGuiScreen
    }

    public <T extends JmUI> T open(Class<T> uiClass, Class<? extends JmUI> returnClass)
    {
        try
        {
            T ui = uiClass.getConstructor(Class.class).newInstance(returnClass);
            return open(ui);
        }
        catch (Throwable e)
        {
            logger.log(Level.ERROR, "Unexpected exception creating UI with return class: " + LogFormatter.toString(e)); //$NON-NLS-1$
            closeCurrent();
            return null;
        }
    }

    public <T extends JmUI> T open(Class<T> uiClass)
    {
        try
        {
            T ui = uiClass.newInstance();
            return open(ui);
        }
        catch (Throwable e)
        {
            logger.log(Level.ERROR, "Unexpected exception creating UI: " + LogFormatter.toString(e)); //$NON-NLS-1$
            closeCurrent();
            return null;
        }
    }

    public <T extends JmUI> T open(T ui)
    {
        closeCurrent();
        logger.debug("Opening UI " + ui.getClass().getSimpleName());
        try
        {
            minecraft.displayGuiScreen(ui);
            //miniMap.setVisible(false);
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected exception opening UI %s: %s", ui.getClass(), LogFormatter.toString(t)));
        }
        return ui;
    }

    public void toggleMinimap()
    {
        setMiniMapEnabled(!isMiniMapEnabled());
    }

    public boolean isMiniMapEnabled()
    {
        return JourneyMap.getMiniMapProperties().enabled.get();
    }

    public void setMiniMapEnabled(boolean enable)
    {
        JourneyMap.getMiniMapProperties().enabled.set(enable);
        JourneyMap.getMiniMapProperties().save();
    }

    public void drawMiniMap()
    {
        try
        {
            if (JourneyMap.getMiniMapProperties().enabled.get())
            {
                final GuiScreen currentScreen = minecraft.currentScreen;
                final boolean doDraw = currentScreen == null || currentScreen instanceof GuiChat;
                if (doDraw)
                {
                    miniMap.drawMap();
                }
            }
        }
        catch (Throwable e)
        {
            JourneyMap.getLogger().error("Error drawing minimap: " + LogFormatter.toString(e));
        }
    }

    public MiniMap getMiniMap()
    {
        return miniMap;
    }

    public void openMap()
    {
        KeyBinding.unPressAllKeys();
        open(Fullscreen.class);
    }

    public void openMap(Waypoint waypoint)
    {
        try
        {
            if (waypoint.isInPlayerDimension())
            {
                KeyBinding.unPressAllKeys();
                Fullscreen map = open(Fullscreen.class);
                map.centerOn(waypoint);
            }
        }
        catch (Throwable e)
        {
            JourneyMap.getLogger().error("Error opening map on waypoint: " + LogFormatter.toString(e));
        }
    }

    public void openMapHotkeyHelp(Class<? extends JmUI> returnClass)
    {
        open(FullscreenHotkeysHelp.class, returnClass);
    }

    public void openMiniMapHotkeyHelp(Class<? extends JmUI> returnClass)
    {
        open(MiniMapHotkeysHelp.class, returnClass);
    }

    public void openWaypointOptions(Class<? extends JmUI> returnClass)
    {
        open(WaypointOptions.class, returnClass);
    }

    public void openStyleOptions(Class<? extends JmUI> returnClass)
    {
        open(StyleOptions.class, returnClass);
    }

    public void openMasterOptions()
    {
        open(OptionsManager.class);
    }

    public void openMasterOptions(Class<? extends JmUI> returnClass, Config.Category... initialCategories)
    {
        try
        {
            open(new OptionsManager(returnClass, initialCategories));
        }
        catch (Throwable e)
        {
            logger.log(Level.ERROR, "Unexpected exception creating MasterOptions with return class: " + LogFormatter.toString(e));
        }
    }

    public void openMapActions()
    {
        open(FullscreenActions.class);
    }

    public void openWaypointHelp(Class<? extends JmUI> returnClass)
    {
        open(WaypointHelp.class, returnClass);
    }

    public void openWaypointManager(Waypoint waypoint, Class<? extends JmUI> returnClass)
    {
        if (WaypointsData.isManagerEnabled())
        {
            try
            {
                WaypointManager manager = new WaypointManager(waypoint, returnClass);
                open(manager);
            }
            catch (Throwable e)
            {
                JourneyMap.getLogger().error("Error opening waypoint manager: " + LogFormatter.toString(e));
            }
        }
    }

    public void openWaypointEditor(Waypoint waypoint, boolean isNew, Class<? extends JmUI> returnClass)
    {
        if (WaypointsData.isManagerEnabled())
        {
            try
            {
                WaypointEditor editor = new WaypointEditor(waypoint, isNew, returnClass);
                open(editor);
            }
            catch (Throwable e)
            {
                JourneyMap.getLogger().error("Error opening waypoint editor: " + LogFormatter.toString(e));
            }
        }
    }

    public void reset()
    {
        Fullscreen.reset();
        TileCache.instance().invalidateAll();
        TileCache.instance().cleanUp();
        resetMinimap();
    }

    public void resetMinimap()
    {
        if (this.miniMap != null)
        {
            this.miniMap.reset();
        }
        this.miniMap = new MiniMap();
    }

    private static class Holder
    {
        private static final UIManager INSTANCE = new UIManager();
    }
}
