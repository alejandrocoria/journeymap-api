/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui;

import journeymap.client.data.WaypointsData;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.dialog.*;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.client.ui.minimap.MiniMapHotkeysHelp;
import journeymap.client.ui.waypoint.WaypointEditor;
import journeymap.client.ui.waypoint.WaypointManager;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Singleton manager for invoking UIs.
 */
public enum UIManager
{
    INSTANCE;

    private final Logger logger = Journeymap.getLogger();
    private final MiniMap miniMap;
    Minecraft minecraft = FMLClientHandler.instance().getClient();

    private UIManager()
    {
        MiniMap tmp;
        try
        {
            int preset = Journeymap.getClient().getMiniMapProperties1().isActive() ? 1 : 2;
            tmp = new MiniMap(Journeymap.getClient().getMiniMapProperties(preset));
        }
        catch (Throwable e)
        {
            logger.error("Unexpected error: " + LogFormatter.toString(e));
            if (e instanceof LinkageError)
            {
                ChatLog.announceError(e.getMessage() + " : JourneyMap is not compatible with this build of Forge!");
            }
            tmp = new MiniMap(new MiniMapProperties(1));
        }
        this.miniMap = tmp;
    }

    public static void handleLinkageError(LinkageError error)
    {
        Journeymap.getLogger().error(LogFormatter.toString(error));
        try
        {
            ChatLog.announceError(error.getMessage() + " : JourneyMap is not compatible with this build of Forge!");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    public void closeAll()
    {
        try
        {
            closeCurrent();
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable e)
        {
            logger.error("Unexpected error: " + LogFormatter.toString(e));
        }
        minecraft.displayGuiScreen(null);
        minecraft.setIngameFocus();
    }

    public void closeCurrent()
    {
        try
        {
            if (minecraft.currentScreen != null && minecraft.currentScreen instanceof JmUI)
            {
                logger.debug("Closing " + minecraft.currentScreen.getClass());
                ((JmUI) minecraft.currentScreen).close();
            }
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable e)
        {
            logger.error("Unexpected error: " + LogFormatter.toString(e));
        }
        KeyBinding.unPressAllKeys();
    }

    public void openInventory()
    {
        logger.debug("Opening inventory");
        closeAll();
        minecraft.displayGuiScreen(new GuiInventory(minecraft.player)); // displayGuiScreen
    }

    public <T extends JmUI> T open(Class<T> uiClass, JmUI returnDisplay)
    {
        try
        {
            // Try constructor with return display
            return open(uiClass.getConstructor(JmUI.class).newInstance(returnDisplay));
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
            return null;
        }
        catch (Throwable e)
        {
            try
            {
                // Try constructor without return display
                return open(uiClass.getConstructor().newInstance());
            }
            catch (Throwable e2)
            {
                logger.log(Level.ERROR, "1st unexpected exception creating UI: " + LogFormatter.toString(e));
                logger.log(Level.ERROR, "2nd unexpected exception creating UI: " + LogFormatter.toString(e2));
                closeCurrent();
                return null;
            }
        }
    }

    public <T extends JmUI> T open(Class<T> uiClass)
    {
        try
        {
            if (MiniMap.uiState().active)
            {
                MiniMap.updateUIState(false);
            }

            T ui = uiClass.newInstance();
            return open(ui);
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
            return null;
        }
        catch (Throwable e)
        {
            logger.log(Level.ERROR, "Unexpected exception creating UI: " + LogFormatter.toString(e)); //$NON-NLS-1$
            closeCurrent();
            return null;
        }
    }

    public <T extends GuiScreen> T open(T ui)
    {
        closeCurrent();
        logger.debug("Opening UI " + ui.getClass().getSimpleName());
        try
        {
            minecraft.displayGuiScreen(ui);
            //miniMap.setVisible(false);
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
            return null;
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected exception opening UI %s: %s", ui.getClass(), LogFormatter.toString(t)));
        }
        return ui;
    }

    public void toggleMinimap()
    {
        try
        {
            setMiniMapEnabled(!isMiniMapEnabled());
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected exception in toggleMinimap: %s", LogFormatter.toString(t)));
        }
    }

    public boolean isMiniMapEnabled()
    {
        try
        {
            return miniMap.getCurrentMinimapProperties().enabled.get();
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected exception in isMiniMapEnabled: %s", LogFormatter.toString(t)));
        }
        return false;
    }

    public void setMiniMapEnabled(boolean enable)
    {
        try
        {
            miniMap.getCurrentMinimapProperties().enabled.set(enable);
            miniMap.getCurrentMinimapProperties().save();
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable t)
        {
            logger.error(String.format("Unexpected exception in setMiniMapEnabled: %s", LogFormatter.toString(t)));
        }
    }

    public void drawMiniMap()
    {
        minecraft.mcProfiler.startSection("journeymap");
        try
        {
            boolean doDraw = false;
            if (miniMap.getCurrentMinimapProperties().enabled.get())
            {
                final GuiScreen currentScreen = minecraft.currentScreen;
                doDraw = currentScreen == null || currentScreen instanceof GuiChat;
                if (doDraw)
                {
                    if (!MiniMap.uiState().active)
                    {
                        if (MiniMap.state().getLastMapTypeChange() == 0)
                        {
                            miniMap.reset();
                        }
                        else
                        {
                            MiniMap.state().requireRefresh();
                        }
                    }
                    miniMap.drawMap();
                }
            }
            if (doDraw && !MiniMap.uiState().active)
            {
                MiniMap.updateUIState(true);
            }
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error drawing minimap: " + LogFormatter.toString(e));
        }
        finally
        {
            minecraft.mcProfiler.endSection(); // journeymap
        }
    }

    public MiniMap getMiniMap()
    {
        return miniMap;
    }

    public void openFullscreenMap()
    {
        KeyBinding.unPressAllKeys();
        open(Fullscreen.class);
    }

    public void openFullscreenMap(Waypoint waypoint)
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
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error opening map on waypoint: " + LogFormatter.toString(e));
        }
    }

    public void openMapHotkeyHelp(JmUI returnDisplay)
    {
        open(FullscreenHotkeysHelp.class, returnDisplay);
    }

    public void openMiniMapHotkeyHelp(JmUI returnDisplay)
    {
        open(MiniMapHotkeysHelp.class, returnDisplay);
    }

    public void openOptionsManager()
    {
        open(OptionsManager.class);
    }

    public void openOptionsManager(JmUI returnDisplay, Category... initialCategories)
    {
        try
        {
            open(new OptionsManager(returnDisplay, initialCategories));
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
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

    public void openSplash(JmUI returnDisplay)
    {
        open(Splash.class, returnDisplay);
    }

    public void openWaypointManager(Waypoint waypoint, JmUI returnDisplay)
    {
        if (WaypointsData.isManagerEnabled())
        {
            try
            {
                WaypointManager manager = new WaypointManager(waypoint, returnDisplay);
                open(manager);
            }
            catch (LinkageError e)
            {
                handleLinkageError(e);
            }
            catch (Throwable e)
            {
                Journeymap.getLogger().error("Error opening waypoint manager: " + LogFormatter.toString(e));
            }
        }
    }

    public void openWaypointEditor(Waypoint waypoint, boolean isNew, JmUI returnDisplay)
    {
        if (WaypointsData.isManagerEnabled())
        {
            try
            {
                WaypointEditor editor = new WaypointEditor(waypoint, isNew, returnDisplay);
                open(editor);
            }
            catch (LinkageError e)
            {
                handleLinkageError(e);
            }
            catch (Throwable e)
            {
                Journeymap.getLogger().error("Error opening waypoint editor: " + LogFormatter.toString(e));
            }
        }
    }

    public void openGridEditor(JmUI returnDisplay)
    {
        try
        {
            GridEditor editor = new GridEditor(returnDisplay);
            open(editor);
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error opening grid editor: " + LogFormatter.toString(e));
        }
    }

    public void reset()
    {
        try
        {
            Fullscreen.state().requireRefresh();
            miniMap.reset();
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error during reset: " + LogFormatter.toString(e));
        }
    }

    public void switchMiniMapPreset()
    {
        try
        {
            int currentPreset = miniMap.getCurrentMinimapProperties().getId();
            switchMiniMapPreset(currentPreset == 1 ? 2 : 1);
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error during switchMiniMapPreset: " + LogFormatter.toString(e));
        }
    }

    public void switchMiniMapPreset(int which)
    {
        try
        {
            miniMap.setMiniMapProperties(Journeymap.getClient().getMiniMapProperties(which));
            MiniMap.state().requireRefresh();
        }
        catch (LinkageError e)
        {
            handleLinkageError(e);
        }
        catch (Throwable e)
        {
            Journeymap.getLogger().error("Error during switchMiniMapPreset: " + LogFormatter.toString(e));
        }

    }
}
