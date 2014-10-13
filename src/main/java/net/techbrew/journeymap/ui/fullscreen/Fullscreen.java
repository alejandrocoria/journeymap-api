/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.fullscreen;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.model.MapState;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.draw.RadarDrawStepFactory;
import net.techbrew.journeymap.render.draw.WaypointDrawStepFactory;
import net.techbrew.journeymap.render.map.GridRenderer;
import net.techbrew.journeymap.render.map.TileCache;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.ButtonList;
import net.techbrew.journeymap.ui.component.JmUI;
import net.techbrew.journeymap.ui.component.OnOffButton;
import net.techbrew.journeymap.ui.fullscreen.layer.LayerDelegate;
import net.techbrew.journeymap.ui.option.LocationFormat;
import net.techbrew.journeymap.ui.theme.Theme;
import net.techbrew.journeymap.ui.theme.ThemeButton;
import net.techbrew.journeymap.ui.theme.ThemeToggle;
import net.techbrew.journeymap.ui.theme.ThemeToolbar;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

/**
 * Displays the map as a full-screen overlay in-game.
 *
 * @author mwoodman
 */
public class Fullscreen extends JmUI
{
    final static MapState state = new MapState();
    final static GridRenderer gridRenderer = new GridRenderer(5);
    final WaypointDrawStepFactory waypointRenderer = new WaypointDrawStepFactory();
    final RadarDrawStepFactory radarRenderer = new RadarDrawStepFactory();
    final LayerDelegate layerDelegate = new LayerDelegate();
    FullMapProperties fullMapProperties = JourneyMap.getFullMapProperties();
    boolean firstLayoutPass = true;
    boolean hideOptionsToolbar = false;
    Boolean isScrolling = false;
    int msx, msy, mx, my;
    Logger logger = JourneyMap.getLogger();
    MapChat chat;
    ThemeButton buttonFollow, buttonZoomIn, buttonZoomOut, buttonDay, buttonNight, buttonCaves;
    ThemeButton buttonAlert, buttonOptions, buttonActions, buttonClose;
    ThemeButton buttonTheme, buttonWaypointManager;
    ThemeButton buttonMobs, buttonAnimals, buttonPets, buttonVillagers, buttonPlayers, buttonGrid;
    ThemeToolbar mapTypeToolbar, optionsToolbar, menuToolbar, zoomToolbar;//, northEastToolbar;
    Color bgColor = new Color(0x22, 0x22, 0x22);
    Color statusForegroundColor;
    Color statusBackgroundColor;
    int statusForegroundAlpha;
    int statusBackgroundAlpha;
    StatTimer drawScreenTimer = StatTimer.get("MapOverlay.drawScreen");
    StatTimer drawMapTimer = StatTimer.get("MapOverlay.drawScreen.drawMap");
    StatTimer drawMapTimerWithRefresh = StatTimer.get("MapOverlay.drawMap+refreshState");
    LocationFormat locationFormat = new LocationFormat();

    int lastWidth;

    /**
     * Default constructor
     */
    public Fullscreen()
    {
        super(null);
        mc = FMLClientHandler.instance().getClient();
        fullMapProperties = JourneyMap.getFullMapProperties();
        state.refresh(mc, mc.thePlayer, fullMapProperties);
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        gridRenderer.setZoom(fullMapProperties.zoomLevel.get());
    }

    public static synchronized MapState state()
    {
        return state;
    }

    public static void reset()
    {
        state.requireRefresh();
        gridRenderer.clear();
        TileCache.instance().invalidateAll();
        TileCache.instance().cleanUp();
    }

    @Override
    public void initGui()
    {
        fullMapProperties = JourneyMap.getFullMapProperties();
        Keyboard.enableRepeatEvents(true);

        // When switching dimensions, reset grid
        if (state.getDimension() != mc.thePlayer.dimension)
        {
            gridRenderer.clear();
        }

        initButtons();
    }

    @Override
    public void drawScreen(int width, int height, float f)
    {
        try
        {
            drawScreenTimer.start();
            drawBackground(0); // drawBackground
            drawMap();

            layoutButtons();

            List<String> tooltip = null;

            if (firstLayoutPass)
            {
                layoutButtons();
                firstLayoutPass = false;
            }
            else
            {
                for (int k = 0; k < this.buttonList.size(); ++k)
                {
                    GuiButton guibutton = (GuiButton) this.buttonList.get(k);
                    guibutton.drawButton(this.mc, width, height);
                    if (tooltip == null)
                    {
                        if (guibutton instanceof Button)
                        {
                            Button button = (Button) guibutton;
                            if (button.mouseOver(mx, my))
                            {
                                tooltip = button.getTooltip();
                            }
                        }
                    }
                }
            }

            if (chat != null)
            {
                chat.drawScreen(width, height, f);
            }

            if (tooltip != null && !tooltip.isEmpty())
            {
                drawHoveringText(tooltip, mx, my, getFontRenderer());
                RenderHelper.disableStandardItemLighting();
            }

        }
        catch (Throwable e)
        {
            logger.log(Level.ERROR, "Unexpected exception in jm.fullscreen.drawScreen(): " + LogFormatter.toString(e));
            UIManager.getInstance().closeAll();
        }
        finally
        {
            drawScreenTimer.stop();
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        if (guibutton instanceof ThemeToolbar)
        {
            return;
        }

        if (guibutton instanceof OnOffButton)
        {
            ((OnOffButton) guibutton).toggle();
        }

        if (optionsToolbar.contains(guibutton))
        {
            refreshState();
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height)
    {
        super.setWorldAndResolution(minecraft, width, height);
        state.requireRefresh();

        if (chat == null)
        {
            chat = new MapChat("", true);
        }
        if (chat != null)
        {
            chat.setWorldAndResolution(minecraft, width, height);
        }

        initGui();

        drawMap();
    }

    /**
     * Set up UI buttons.
     */
    void initButtons()
    {
        if (buttonList.isEmpty())
        {
            firstLayoutPass = true;
            hideOptionsToolbar = false;
            Theme theme = ThemeFileHandler.getCurrentTheme();
            Constants.MapType mapType = state.getCurrentMapType();
            int id = 0;

            // UI Colors
            bgColor = Theme.getColor(theme.fullscreen.mapBackgroundColor);
            statusForegroundColor = Theme.getColor(theme.fullscreen.statusLabel.foregroundColor);
            statusForegroundAlpha = theme.fullscreen.statusLabel.foregroundAlpha;
            statusBackgroundColor = Theme.getColor(theme.fullscreen.statusLabel.backgroundColor);
            statusBackgroundAlpha = theme.fullscreen.statusLabel.backgroundAlpha;

            // Day Toggle
            buttonDay = new ThemeToggle(theme, "jm.fullscreen.map_day", "day", null, null);
            buttonDay.setToggled(mapType == Constants.MapType.day, false);
            buttonDay.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    if (toggled)
                    {
                        state.setMapType(Constants.MapType.day);
                        buttonNight.setToggled(false);
                        if (state.isUnderground())
                        {
                            buttonCaves.setToggled(false);
                        }
                        state.requireRefresh();
                    }
                    else if (state.getCurrentMapType() == Constants.MapType.day)
                    {
                        return false;
                    }
                    return true;
                }
            });

            // Night Toggle
            buttonNight = new ThemeToggle(theme, "jm.fullscreen.map_night", "night");
            buttonNight.setToggled(mapType == Constants.MapType.night, false);
            buttonNight.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    if (toggled)
                    {
                        state.setMapType(Constants.MapType.night);
                        buttonDay.setToggled(false);
                        if (state.isUnderground())
                        {
                            buttonCaves.setToggled(false);
                        }
                        state.requireRefresh();
                    }
                    else if (state.getCurrentMapType() == Constants.MapType.night)
                    {
                        return false;
                    }
                    return true;
                }
            });

            // Caves Toggle
            buttonCaves = new ThemeToggle(theme, "jm.fullscreen.map_caves", "caves", fullMapProperties, fullMapProperties.showCaves);
            buttonCaves.setDrawButton(state.isCaveMappingAllowed());
            buttonCaves.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    state.requireRefresh();
                    return true;
                }
            });

            // Follow
            buttonFollow = new ThemeButton(theme, "jm.fullscreen.follow", "follow");
            buttonFollow.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    toggleFollow();
                    return true;
                }
            });

            // Zoom In
            buttonZoomIn = new ThemeButton(theme, "jm.fullscreen.zoom_in", "zoomin");
            buttonZoomIn.setEnabled(fullMapProperties.zoomLevel.get() < state.maxZoom);
            buttonZoomIn.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    zoomIn();
                    return true;
                }
            });

            // Zoom Out
            buttonZoomOut = new ThemeButton(theme, "jm.fullscreen.zoom_out", "zoomout");
            buttonZoomOut.setEnabled(fullMapProperties.zoomLevel.get() > state.minZoom);
            buttonZoomOut.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    zoomOut();
                    return true;
                }
            });

            // Waypoints
            buttonWaypointManager = new ThemeButton(theme, "jm.waypoint.waypoints", "waypoints");
            buttonWaypointManager.setDrawButton(WaypointsData.isManagerEnabled());
            buttonWaypointManager.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    UIManager.getInstance().openWaypointManager(null, Fullscreen.this);
                    return true;
                }
            });

            // Waypoints
            buttonTheme = new ThemeButton(theme, "jm.common.ui_theme", "theme");
            buttonTheme.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    ThemeFileHandler.loadNextTheme();
                    UIManager.getInstance().getMiniMap().reset();
                    buttonList.clear();
                    return false;
                }
            });

            String[] tooltips = new String[]{
                    EnumChatFormatting.ITALIC + Constants.getString("jm.common.ui_theme_name", theme.name),
                    EnumChatFormatting.ITALIC + Constants.getString("jm.common.ui_theme_author", theme.author)
            };
            buttonTheme.setAdditionalTooltips(Arrays.asList(tooltips));

            // Options
            buttonOptions = new ThemeButton(theme, "jm.common.options", "options");
            buttonOptions.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    try
                    {
                        UIManager.getInstance().openOptionsManager();
                        return true;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        return false;
                    }
                }
            });

            // Actions
            buttonActions = new ThemeButton(theme, "jm.common.actions", "actions");
            buttonActions.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    UIManager.getInstance().openMapActions();
                    return true;
                }
            });

            // Alert
            buttonAlert = new ThemeToggle(theme, "jm.common.update_available", "alert");
            buttonAlert.setDrawButton(VersionCheck.getVersionIsChecked() && !VersionCheck.getVersionIsCurrent());
            buttonAlert.setToggled(true);
            buttonAlert.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    VersionCheck.launchWebsite();
                    buttonAlert.setDrawButton(false);
                    return true;
                }
            });

            // Close
            buttonClose = new ThemeButton(theme, "jm.common.close", "close");
            buttonClose.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    UIManager.getInstance().closeAll();
                    return true;
                }
            });

            buttonMobs = new ThemeToggle(theme, "jm.common.show_mobs", "monsters", fullMapProperties, fullMapProperties.showMobs);
            buttonMobs.setDrawButton(FeatureManager.isAllowed(Feature.RadarMobs));

            buttonAnimals = new ThemeToggle(theme, "jm.common.show_animals", "animals", fullMapProperties, fullMapProperties.showAnimals);
            buttonAnimals.setDrawButton(FeatureManager.isAllowed(Feature.RadarAnimals));

            buttonPets = new ThemeToggle(theme, "jm.common.show_pets", "pets", fullMapProperties, fullMapProperties.showPets);
            buttonPets.setDrawButton(FeatureManager.isAllowed(Feature.RadarAnimals));

            buttonVillagers = new ThemeToggle(theme, "jm.common.show_villagers", "villagers", fullMapProperties, fullMapProperties.showVillagers);
            buttonVillagers.setDrawButton(FeatureManager.isAllowed(Feature.RadarVillagers));

            buttonPlayers = new ThemeToggle(theme, "jm.common.show_players", "players", fullMapProperties, fullMapProperties.showPlayers);
            buttonPlayers.setDrawButton(!mc.isSingleplayer() && FeatureManager.isAllowed(Feature.RadarPlayers));

            buttonGrid = new ThemeToggle(theme, "jm.common.show_grid", "grid", fullMapProperties, fullMapProperties.showGrid);

            // Toolbars
            mapTypeToolbar = new ThemeToolbar(theme, buttonCaves, buttonNight, buttonDay);
            mapTypeToolbar.addAllButtons(this);

            optionsToolbar = new ThemeToolbar(theme, buttonMobs, buttonAnimals, buttonPets, buttonVillagers, buttonPlayers, buttonGrid);
            optionsToolbar.addAllButtons(this);

            menuToolbar = new ThemeToolbar(theme, buttonWaypointManager, buttonTheme, buttonOptions, buttonActions);
            menuToolbar.addAllButtons(this);

            zoomToolbar = new ThemeToolbar(theme, buttonFollow, buttonZoomIn, buttonZoomOut);
            zoomToolbar.setLayout(ButtonList.Layout.Vertical, ButtonList.Direction.LeftToRight);
            zoomToolbar.addAllButtons(this);

            // Buttons not in toolbars
            buttonList.add(buttonAlert);
            buttonList.add(buttonClose);
        }
    }

    /**
     * Center buttons in UI.
     */
    @Override
    protected void layoutButtons()
    {
        if (buttonList.isEmpty())
        {
            initButtons();
        }

        // Update toggles
        boolean isSky = !mc.theWorld.provider.hasNoSky;
        buttonDay.setEnabled(isSky);
        buttonNight.setEnabled(isSky);
        buttonCaves.setEnabled(isSky && state.isUnderground() && state.isCaveMappingAllowed());
        buttonFollow.setEnabled(!state.follow.get());

        int padding = mapTypeToolbar.getToolbarSpec().padding;

        zoomToolbar.layoutCenteredVertical(zoomToolbar.getHMargin(), height / 2, true, padding);

        int topY = mapTypeToolbar.getVMargin();

        int margin = mapTypeToolbar.getHMargin();

        layoutToolbars(margin, topY, padding, hideOptionsToolbar);
        buttonClose.leftOf(width - zoomToolbar.getHMargin()).below(mapTypeToolbar.getVMargin());
        buttonAlert.leftOf(width - zoomToolbar.getHMargin()).below(buttonClose, padding);


        if (!hideOptionsToolbar)
        {
            if (menuToolbar.getRightX() + margin >= buttonClose.getX())
            {
                //optionsToolbar.setDrawToolbar(false);
                hideOptionsToolbar = true;
                layoutToolbars(margin, topY, padding, hideOptionsToolbar);
            }
        }

        if (hideOptionsToolbar)
        {
            buttonAlert.setX(buttonOptions.getX());
            buttonClose.setX(buttonOptions.getX());
        }

    }

    protected void layoutToolbars(int margin, int topY, int padding, boolean hideOptionsToolbar)
    {
        if (hideOptionsToolbar)
        {

            int toolbarsWidth = mapTypeToolbar.getWidth() + optionsToolbar.getWidth() + margin + padding;
            int startX = (width - toolbarsWidth) / 2;

            mapTypeToolbar.layoutHorizontal(startX + mapTypeToolbar.getWidth(), topY, false, padding);
            optionsToolbar.layoutHorizontal(mapTypeToolbar.getRightX() + margin, topY, true, padding);

            menuToolbar.layoutCenteredVertical(width - menuToolbar.getWidth(), height / 2, true, padding);

        }
        else
        {
            optionsToolbar.layoutCenteredHorizontal((width / 2), topY, true, padding);
            mapTypeToolbar.layoutHorizontal(optionsToolbar.getX() - margin, topY, false, padding);
            menuToolbar.layoutHorizontal(optionsToolbar.getRightX() + margin, topY, true, padding);
        }
    }

    @Override
    public void handleMouseInput()
    { // handleMouseInput

        if (chat != null && !chat.isHidden())
        {
            chat.handleMouseInput();
            //return;
        }

        // Scale mouse position to Gui Scale coords
        mx = (Mouse.getEventX() * width) / mc.displayWidth;
        my = height - (Mouse.getEventY() * height) / mc.displayHeight - 1;

        if (Mouse.getEventButtonState())
        {
            mouseClicked(mx, my, Mouse.getEventButton());
        }
        else
        {
            int wheel = Mouse.getEventDWheel();
            if (wheel > 0)
            {
                zoomIn();
            }
            else
            {
                if (wheel < 0)
                {
                    zoomOut();
                }
                else
                {
                    mouseMovedOrUp(mx, my, Mouse.getEventButton());
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (chat != null && !chat.isHidden())
        {
            chat.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Bail if over a button
        if (isMouseOverButton(mouseX, mouseY))
        {
            return;
        }

        // Invoke layer delegate
        BlockCoordIntPair blockCoord = gridRenderer.getBlockUnderMouse(Mouse.getEventX(), Mouse.getEventY(), mc.displayWidth, mc.displayHeight);
        layerDelegate.onMouseClicked(mc, Mouse.getEventX(), Mouse.getEventY(), gridRenderer.getWidth(), gridRenderer.getHeight(), blockCoord, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which)
    {
        super.mouseMovedOrUp(mouseX, mouseY, which);

        if (isMouseOverButton(mouseX, mouseY))
        {
            return;
        }

        int blockSize = (int) Math.pow(2, fullMapProperties.zoomLevel.get());

        if (Mouse.isButtonDown(0) && !isScrolling)
        {
            isScrolling = true;
            msx = mx;
            msy = my;
        }
        else
        {
            if (!Mouse.isButtonDown(0) && isScrolling)
            {
                isScrolling = false;
                int mouseDragX = (mx - msx) * Math.max(1, scaleFactor) / blockSize;
                int mouseDragY = (my - msy) * Math.max(1, scaleFactor) / blockSize;
                msx = mx;
                msy = my;

                try
                {
                    gridRenderer.move(-mouseDragX, -mouseDragY);
                    gridRenderer.updateTextures(state.getCurrentMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, false, 0, 0);
                    gridRenderer.setZoom(fullMapProperties.zoomLevel.get());
                }
                catch (Exception e)
                {
                    logger.error("Error moving grid: " + e);
                }

                setFollow(false);
                refreshState();
            }
        }

        if (!isScrolling && which == -1)
        {
            BlockCoordIntPair blockCoord = gridRenderer.getBlockUnderMouse(Mouse.getEventX(), Mouse.getEventY(), mc.displayWidth, mc.displayHeight);
            layerDelegate.onMouseMove(mc, Mouse.getEventX(), Mouse.getEventY(), gridRenderer.getWidth(), gridRenderer.getHeight(), blockCoord);
        }
    }

    void zoomIn()
    {
        if (fullMapProperties.zoomLevel.get() < state.maxZoom)
        {
            setZoom(fullMapProperties.zoomLevel.get() + 1);
        }
    }

    void zoomOut()
    {
        if (fullMapProperties.zoomLevel.get() > state.minZoom)
        {
            setZoom(fullMapProperties.zoomLevel.get() - 1);
        }
    }

    private void setZoom(int zoom)
    {
        if (state.setZoom(zoom))
        {
            buttonZoomOut.setEnabled(fullMapProperties.zoomLevel.get() > state.minZoom);
            buttonZoomIn.setEnabled(fullMapProperties.zoomLevel.get() < state.maxZoom);
            refreshState();
        }
    }

    void toggleFollow()
    {
        setFollow(!state.follow.get());
    }

    void setFollow(Boolean follow)
    {
        state.follow.set(follow);
        if (state.follow.get())
        {
            refreshState();
        }
    }

    @Override
    public void keyTyped(char c, int i)
    {
        if (chat != null && !chat.isHidden())
        {
            chat.keyTyped(c, i);
            return;
        }

        if (i == Keyboard.KEY_O)
        {
            UIManager.getInstance().openOptionsManager();
            return;
        }

        if (i == Keyboard.KEY_ESCAPE || Constants.isPressed(Constants.KB_MAP))
        {
            UIManager.getInstance().closeAll();
            return;
        }
        else
        {
            if (Constants.isPressed(Constants.KB_MAP_ZOOMIN))
            {
                zoomIn();
                return;
            }
            else
            {
                if (Constants.isPressed(Constants.KB_MAP_ZOOMOUT))
                {
                    zoomOut();
                    return;
                }
                else
                {
                    if (Constants.isPressed(Constants.KB_MAP_DAY))
                    {
                        state.setMapType(Constants.MapType.day);
                        return;
                    }
                    else
                    {
                        if (Constants.isPressed(Constants.KB_MAP_NIGHT))
                        {
                            state.setMapType(Constants.MapType.night);
                            return;
                        }
                        else
                        {
                            if (Constants.isPressed(Constants.KB_WAYPOINT))
                            {
                                Waypoint waypoint = Waypoint.of(mc.thePlayer);
                                UIManager.getInstance().openWaypointEditor(waypoint, true, null);
                                return;
                            }
                        }
                    }
                }
            }
        }

        // North
        if (Constants.isPressed(mc.gameSettings.keyBindForward))
        {
            moveCanvas(0, -16);
            return;
        }

        // West
        if (Constants.isPressed(mc.gameSettings.keyBindLeft))
        {
            moveCanvas(-16, 0);
            return;
        }

        // South
        if (Constants.isPressed(mc.gameSettings.keyBindBack))
        {
            moveCanvas(0, 16);
            return;
        }

        // East
        if (Constants.isPressed(mc.gameSettings.keyBindRight))
        {
            moveCanvas(16, 0);
            return;
        }

        // Open inventory
        if (Constants.isPressed(mc.gameSettings.keyBindInventory))
        {
            UIManager.getInstance().openInventory();
            return;
        }

        // Open chat
        if (Constants.isPressed(mc.gameSettings.keyBindChat))
        {
            openChat("");
            return;
        }

        // Open chat with command prefix (Minecraft.java does this in runTick() )
        if (Constants.isPressed(mc.gameSettings.keyBindCommand))
        {
            openChat("/");
            return;
        }

    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        if (chat != null)
        {
            chat.updateScreen();
        }
        //layoutButtons();
    }

    @Override
    public void drawBackground(int layer)
    {
        DrawUtil.drawRectangle(0, 0, width, height, bgColor, 255);
    }

    void drawMap()
    {

        final boolean refreshReady = isRefreshReady();
        final StatTimer timer = refreshReady ? drawMapTimerWithRefresh : drawMapTimer;
        timer.start();

        sizeDisplay(false);

        int xOffset = 0;
        int yOffset = 0;

        if (isScrolling)
        {
            int blockSize = (int) Math.pow(2, fullMapProperties.zoomLevel.get());

            int mouseDragX = (mx - msx) * Math.max(1, scaleFactor) / blockSize;
            int mouseDragY = (my - msy) * Math.max(1, scaleFactor) / blockSize;

            xOffset = (mouseDragX * blockSize);
            yOffset = (mouseDragY * blockSize);

        }
        else
        {
            if (refreshReady)
            {
                refreshState();
            }
            else
            {
                gridRenderer.setContext(state.getWorldDir(), state.getDimension());
            }
        }

        gridRenderer.updateGL(0);
        boolean unicodeForced = DrawUtil.startUnicode(mc.fontRenderer, fullMapProperties.forceUnicode.get());
        float drawScale = fullMapProperties.textureSmall.get() ? 1f : 2f;

        if (state.follow.get())
        {
            gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, fullMapProperties.zoomLevel.get());
        }
        gridRenderer.updateTextures(state.getCurrentMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, false, 0, 0);
        gridRenderer.draw(1f, xOffset, yOffset);
        gridRenderer.draw(state.getDrawSteps(), xOffset, yOffset, drawScale, getMapFontScale(), 0);
        gridRenderer.draw(state.getDrawWaypointSteps(), xOffset, yOffset, drawScale, getMapFontScale(), 0);

        if (fullMapProperties.showSelf.get())
        {
            Point2D playerPixel = gridRenderer.getPixel(mc.thePlayer.posX, mc.thePlayer.posZ);
            if (playerPixel != null)
            {
                DrawUtil.drawEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, mc.thePlayer.rotationYawHead, false, TextureCache.instance().getPlayerLocatorSmall(), drawScale, 0);
            }
        }

        gridRenderer.draw(layerDelegate.getDrawSteps(), xOffset, yOffset, drawScale, getMapFontScale(), 0);

        DrawUtil.drawLabel(state.playerLastPos, mc.displayWidth / 2, mc.displayHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above,
                statusBackgroundColor, statusBackgroundAlpha, statusForegroundColor, statusForegroundAlpha, getMapFontScale(), true);

        if (unicodeForced)
        {
            DrawUtil.stopUnicode(mc.fontRenderer);
        }

        drawLogo();

        sizeDisplay(true);

        timer.stop();
    }

    private int getMapFontScale()
    {
        return (fullMapProperties.fontSmall.get() ? 1 : 2) * (fullMapProperties.forceUnicode.get() ? 2 : 1);
    }

    public void centerOn(Waypoint waypoint)
    {
        if (waypoint.getDimensions().contains(mc.thePlayer.dimension))
        {
            state.follow.set(false);
            state.requireRefresh();
            int x = waypoint.getX();
            int z = waypoint.getZ();

            gridRenderer.center(x, z, fullMapProperties.zoomLevel.get());
            refreshState();
            updateScreen();
        }
    }

    /**
     * Get a snapshot of the player's biome, effective map state, etc.
     */
    void refreshState()
    {
        // Check player status
        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null)
        {
            logger.warn("Could not get player");
            return;
        }

        // Update the state first
        fullMapProperties = JourneyMap.getFullMapProperties();
        state.refresh(mc, player, fullMapProperties);

        if (state.getDimension() != gridRenderer.getDimension())
        {
            setFollow(true);
        }

        gridRenderer.setContext(state.getWorldDir(), state.getDimension());

        // Center core renderer
        if (state.follow.get())
        {
            gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, fullMapProperties.zoomLevel.get());
        }
        else
        {
            gridRenderer.setZoom(fullMapProperties.zoomLevel.get());
        }

        gridRenderer.updateTextures(state.getCurrentMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);

        // Build list of drawSteps
        state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, fullMapProperties, 1f, false);

        // Update player pos
        LocationFormat.LocationFormatKeys locationFormatKeys = locationFormat.getFormatKeys(fullMapProperties.locationFormat.get());
        state.playerLastPos = locationFormatKeys.format(fullMapProperties.locationFormatVerbose.get(),
                MathHelper.floor_double(mc.thePlayer.posX),
                MathHelper.floor_double(mc.thePlayer.posZ),
                MathHelper.floor_double(mc.thePlayer.boundingBox.minY),
                mc.thePlayer.chunkCoordY) + " " + state.getPlayerBiome();

        // Reset timer
        state.updateLastRefresh();

        // Clean up expired tiles
        TileCache.instance().cleanUp();
    }

    void openChat(String defaultText)
    {
        if (chat != null)
        {
            chat.setText(defaultText);
            chat.setHidden(false);
        }
        else
        {
            chat = new MapChat(defaultText, false);
            chat.setWorldAndResolution(mc, width, height);
        }
    }

    @Override
    public void close()
    {
        if (chat != null)
        {
            chat.close();
        }
    }

    // @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    boolean isRefreshReady()
    {
        if (isScrolling)
        {
            return false;
        }
        else
        {
            return state.shouldRefresh(super.mc, fullMapProperties) || gridRenderer.hasUnloadedTile();
        }
    }

    void moveCanvas(int deltaBlockX, int deltaBlockz)
    {
        refreshState();
        gridRenderer.move(deltaBlockX, deltaBlockz);
        gridRenderer.updateTextures(state.getCurrentMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);
        setFollow(false);
    }

    @Override
    protected void drawLogo()
    {
        DrawUtil.sizeDisplay(mc.displayWidth, mc.displayHeight);
        DrawUtil.drawImage(logo, 8, 8, false, 1, 0);
        DrawUtil.sizeDisplay(width, height);
    }

    @Override
    public final boolean doesGuiPauseGame()
    {
        return false;
    }

}

