/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.map;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.MathHelper;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.overlay.OverlayRadarRenderer;
import net.techbrew.journeymap.render.overlay.OverlayWaypointRenderer;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.ui.Button;
import net.techbrew.journeymap.ui.ButtonList;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.adapter.BooleanPropertyAdapter;
import net.techbrew.journeymap.ui.map.layer.LayerDelegate;
import net.techbrew.journeymap.ui.theme.ThemeButton;
import net.techbrew.journeymap.ui.theme.ThemeToolbar;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Displays the map as a full-screen overlay in-game.
 *
 * @author mwoodman
 */
public class MapOverlay extends JmUI
{
    final static MapOverlayState state = new MapOverlayState();
    final static GridRenderer gridRenderer = new GridRenderer(5, JourneyMap.getFullMapProperties());
    final OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
    final OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
    final LayerDelegate layerDelegate = new LayerDelegate();
    FullMapProperties fullMapProperties = JourneyMap.getFullMapProperties();
    Boolean isScrolling = false;
    int msx, msy, mx, my;
    Logger logger = JourneyMap.getLogger();
    MapChat chat;
    ThemeButton buttonFollow, buttonZoomIn, buttonZoomOut, buttonDay, buttonNight, buttonCaves;
    ThemeButton buttonAlert, buttonOptions, buttonActions, buttonClose;
    ThemeButton buttonMode, buttonWaypointManager;
    ButtonList northEastButtons, westButtons;
    ThemeToolbar northWestToolbar;
    Color bgColor = new Color(0x22, 0x22, 0x22);
    Color playerInfoFgColor = Color.lightGray;
    Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);
    StatTimer drawScreenTimer = StatTimer.get("MapOverlay.drawScreen");
    StatTimer drawMapTimer = StatTimer.get("MapOverlay.drawScreen.drawMap");
    StatTimer drawMapTimerWithRefresh = StatTimer.get("MapOverlay.drawMap+refreshState");
    /**
     * Default constructor
     */
    public MapOverlay()
    {
        super(null);
        mc = FMLClientHandler.instance().getClient();
        fullMapProperties = JourneyMap.getFullMapProperties();
        state.refresh(mc, mc.thePlayer, fullMapProperties);
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        gridRenderer.setZoom(fullMapProperties.zoomLevel.get());
    }

    public static synchronized MapOverlayState state()
    {
        return state;
    }

    public static void reset()
    {
        state.requireRefresh();
        gridRenderer.clear();
        gridRenderer.setMapProperties(JourneyMap.getFullMapProperties());
        TileCache.instance().invalidateAll();
        TileCache.instance().cleanUp();
    }

    @Override
    public void initGui()
    {
        fullMapProperties = JourneyMap.getFullMapProperties();
        gridRenderer.setMapProperties(JourneyMap.getFullMapProperties());

        Keyboard.enableRepeatEvents(true);
        initButtons();

        // When switching dimensions, reset grid
        if (state.getDimension() != mc.thePlayer.dimension)
        {
            gridRenderer.clear();
        }
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

            ArrayList<String> tooltip = null;
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

        final ButtonEnum id = ButtonEnum.values()[guibutton.id];
        switch (id)
        {
            case Day:
            {
                if (Constants.MapType.day != state.getCurrentMapType())
                {
                    setMapType(Constants.MapType.day);
                }
                break;
            }

            case Night:
            {
                if (Constants.MapType.night != state.getCurrentMapType())
                {
                    setMapType(Constants.MapType.night);
                }
                break;
            }

            case Follow:
            { // follow
                toggleFollow();
                break;
            }
            case ZoomIn:
            { // zoom in
                zoomIn();
                break;
            }
            case ZoomOut:
            { // zoom out
                zoomOut();
                break;
            }
            case Close:
            { // close
                UIManager.getInstance().closeAll();
                break;
            }
            case Alert:
            { // alert
                VersionCheck.launchWebsite();
                break;
            }
            case Options:
            { // options
                UIManager.getInstance().openMasterOptions();
                break;
            }
            case Actions:
            { // actions
                UIManager.getInstance().openMapActions();
                break;
            }
            case WaypointManager:
            {
                UIManager.getInstance().openWaypointManager(null, getClass());
                break;
            }
            case Caves:
            {
                if (buttonCaves.isEnabled())
                {
                    if (Constants.MapType.underground != state.getCurrentMapType())
                    {
                        setMapType(Constants.MapType.underground);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height)
    {
        super.setWorldAndResolution(minecraft, width, height);

        state.requireRefresh();

        layoutButtons();

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

            FontRenderer fr = getFontRenderer();

            String on = Constants.getString("jm.common.on");
            String off = Constants.getString("jm.common.off");

            Constants.MapType mapType = state.getMapType(fullMapProperties.showCaves.get());
            boolean underground = DataCache.getPlayer().underground;

            buttonAlert = new ThemeButton(ButtonEnum.Alert, Constants.getString("jm.common.update_available"), ThemeButton.Style.Button, "alert");
            buttonAlert.setDrawButton(VersionCheck.getVersionIsChecked() && !VersionCheck.getVersionIsCurrent());

            buttonDay = new ThemeButton(ButtonEnum.Day, Constants.getString("jm.fullscreen.map_day"), ThemeButton.Style.Toggle, "day");
            buttonDay.setEnabled(!mc.theWorld.provider.hasNoSky);
            buttonDay.setToggled(mapType == Constants.MapType.day, false);

            buttonNight = new ThemeButton(ButtonEnum.Night, Constants.getString("jm.fullscreen.map_night"), ThemeButton.Style.Toggle, "night");
            buttonNight.setEnabled(!mc.theWorld.provider.hasNoSky);
            buttonNight.setToggled(mapType == Constants.MapType.night, false);

            buttonCaves = new ThemeButton(ButtonEnum.Caves, ThemeButton.Style.Toggle, "caves");
            buttonCaves.setPropertyAdapter(new BooleanPropertyAdapter(fullMapProperties, fullMapProperties.showCaves), "jm.fullscreen.map_caves");
            buttonCaves.setDrawButton(state.isCaveMappingAllowed());
            buttonCaves.setEnabled(underground && state.isCaveMappingAllowed() && !mc.theWorld.provider.hasNoSky);

            buttonFollow = new ThemeButton(ButtonEnum.Follow, Constants.getString("jm.fullscreen.follow"), ThemeButton.Style.Button, "follow");

            buttonZoomIn = new ThemeButton(ButtonEnum.ZoomIn, Constants.getString("jm.fullscreen.zoom_in"), ThemeButton.Style.Button, "zoomin");
            buttonZoomIn.setEnabled(fullMapProperties.zoomLevel.get() < state.maxZoom);

            buttonZoomOut = new ThemeButton(ButtonEnum.ZoomOut, Constants.getString("jm.fullscreen.zoom_out"), ThemeButton.Style.Button, "zoomout");
            buttonZoomOut.setEnabled(fullMapProperties.zoomLevel.get() > state.minZoom);

            buttonWaypointManager = new ThemeButton(ButtonEnum.WaypointManager, Constants.getString("jm.waypoint.waypoints"), ThemeButton.Style.Button, "waypoints");
            buttonWaypointManager.setDrawButton(WaypointsData.isManagerEnabled());

            buttonOptions = new ThemeButton(ButtonEnum.Options, Constants.getString("jm.common.options"), ThemeButton.Style.Button, "options");

            buttonActions = new ThemeButton(ButtonEnum.Actions, Constants.getString("jm.common.actions"), ThemeButton.Style.Button, "actions");

            buttonClose = new ThemeButton(ButtonEnum.Close, Constants.getString("jm.common.close"), ThemeButton.Style.Button, "close");

            northWestToolbar = new ThemeToolbar(0, buttonDay, buttonNight, buttonCaves);
            northEastButtons = new ButtonList(buttonAlert, buttonWaypointManager, buttonOptions, buttonActions, buttonClose).reverse();
            westButtons = new ButtonList(buttonFollow, buttonZoomIn, buttonZoomOut);

            buttonList.add(northWestToolbar);
            buttonList.addAll(northWestToolbar.getButtonList());
            buttonList.addAll(northEastButtons);
            buttonList.addAll(westButtons);
        }
    }

    /**
     * Center buttons in UI.
     */
    @Override
    protected void layoutButtons()
    {
        // Buttons
        if (buttonList.isEmpty())
        {
            initButtons();
        }

        final int startX = 40;
        final int endX = width - 3;
        final int startY = 10;
        final int hgap = 3;
        final int vgap = 3;

        buttonZoomIn.setPosition(8, 32);
        buttonZoomOut.below(buttonZoomIn, 8).setX(8);

        westButtons.layoutVertical(3, 32, true, vgap);

        northWestToolbar.getButtonList().layoutHorizontal(startX, startY, true, hgap);


        int rightX = northWestToolbar.getButtonList().getRightX() + hgap;
        if (rightX <= width - northEastButtons.getWidth(hgap))
        {
            if (!northEastButtons.isHorizontal())
            {
                northEastButtons.setFitWidths(mc.fontRenderer);
            }
            northEastButtons.layoutHorizontal(endX, startY, false, hgap);
        }
        else
        {
            if (northEastButtons.isHorizontal())
            {
                northEastButtons.equalizeWidths(mc.fontRenderer);
            }
            northEastButtons.layoutVertical(endX, startY, false, vgap);
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
        Boolean guiButtonUsed = false;
        if (mouseButton == 0)
        {
            for (int l = 0; l < buttonList.size(); l++)
            {
                GuiButton guibutton = (GuiButton) buttonList.get(l);
                if (guibutton.mousePressed(mc, mouseX, mouseY))
                {
                    guiButtonUsed = true;
                    break;
                }
            }
        }
        if (!guiButtonUsed)
        {
            BlockCoordIntPair blockCoord = gridRenderer.getBlockUnderMouse(Mouse.getEventX(), Mouse.getEventY(), mc.displayWidth, mc.displayHeight);
            layerDelegate.onMouseClicked(mc, Mouse.getEventX(), Mouse.getEventY(), gridRenderer.getWidth(), gridRenderer.getHeight(), blockCoord, mouseButton);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which)
    {
        super.mouseMovedOrUp(mouseX, mouseY, which);

        if (isMouseOverButton(mouseX, mouseY, which) && which == 0)
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
                int mouseDragX = (mx - msx) * Math.max(1,scaleFactor) / blockSize;
                int mouseDragY = (my - msy) * Math.max(1,scaleFactor) / blockSize;
                msx = mx;
                msy = my;

                try
                {
                    gridRenderer.move(-mouseDragX, -mouseDragY);
                    boolean showCaves = mc.thePlayer.worldObj.provider.hasNoSky || fullMapProperties.showCaves.get();
                    gridRenderer.updateTextures(state.getMapType(showCaves), state.getVSlice(), mc.displayWidth, mc.displayHeight, false, 0, 0);
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

    void setMapType(Constants.MapType mapType)
    {
        buttonDay.setToggled(mapType == Constants.MapType.day);
        buttonNight.setToggled(mapType == Constants.MapType.night);
        buttonCaves.setToggled(mapType == Constants.MapType.underground);

        // TODO: ButtonCaves doesn't update as expected
        state.setMapType(mapType);
        refreshState();
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
        layoutButtons();
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

            int mouseDragX = (mx - msx) * Math.max(1,scaleFactor) / blockSize;
            int mouseDragY = (my - msy) * Math.max(1,scaleFactor) / blockSize;

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

        boolean unicodeForced = DrawUtil.startUnicode(mc.fontRenderer, fullMapProperties.forceUnicode.get());
        float drawScale = fullMapProperties.textureSmall.get() ? 1f : 2f;

        if (state.follow.get())
        {
            gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, fullMapProperties.zoomLevel.get());
        }
        boolean showCaves = mc.thePlayer.worldObj.provider.hasNoSky || fullMapProperties.showCaves.get();
        gridRenderer.updateTextures(state.getMapType(showCaves), state.getVSlice(), mc.displayWidth, mc.displayHeight, false, 0, 0);
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

        DrawUtil.drawLabel(state.playerLastPos, mc.displayWidth / 2, mc.displayHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, playerInfoBgColor, 235, playerInfoFgColor, 255, getMapFontScale(), true);

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

        gridRenderer.setMapProperties(fullMapProperties);
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

        boolean showCaves = mc.thePlayer.worldObj.provider.hasNoSky || fullMapProperties.showCaves.get();
        gridRenderer.updateTextures(state.getMapType(showCaves), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);

        // Build list of drawSteps
        state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, fullMapProperties, 1f, false);

        // Update player pos
        state.playerLastPos = Constants.getString("jm.common.location_xzyeb",
                MathHelper.floor_double(mc.thePlayer.posX),
                MathHelper.floor_double(mc.thePlayer.posZ),
                MathHelper.floor_double(mc.thePlayer.boundingBox.minY),
                mc.thePlayer.chunkCoordY,
                state.getPlayerBiome()); 

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
            return state.shouldRefresh(super.mc, fullMapProperties);
        }
    }

    void moveCanvas(int deltaBlockX, int deltaBlockz)
    {
        refreshState();
        gridRenderer.move(deltaBlockX, deltaBlockz);

        boolean showCaves = mc.thePlayer.worldObj.provider.hasNoSky || fullMapProperties.showCaves.get();
        gridRenderer.updateTextures(state.getMapType(showCaves), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);
        setFollow(false);
    }

    @Override
    protected void drawLogo()
    {
        //sizeDisplay(mc.displayWidth, mc.displayHeight);

        final boolean smallScale = (scaleFactor == 1);
        DrawUtil.drawImage(logo, smallScale ? 2 : 4, 0, false, smallScale ? .5f : 1f, 0);
        //sizeDisplay(width, height);
    }

    @Override
    public final boolean doesGuiPauseGame()
    {
        return false;
    }

    private enum ButtonEnum
    {
        Alert, Day, Night, Follow, ZoomIn, ZoomOut, Options, Actions, Close, Mode, WaypointManager, Caves
    }

}

