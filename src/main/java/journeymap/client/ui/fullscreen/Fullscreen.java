/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.fullscreen;

import journeymap.client.Constants;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.Overlay;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.UIState;
import journeymap.client.data.WaypointsData;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.io.ThemeFileHandler;
import journeymap.client.log.ChatLog;
import journeymap.client.log.StatTimer;
import journeymap.client.model.MapState;
import journeymap.client.model.MapType;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.CoreProperties;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.draw.RadarDrawStepFactory;
import journeymap.client.render.draw.WaypointDrawStepFactory;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.map.Tile;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.component.OnOffButton;
import journeymap.client.ui.dialog.FullscreenActions;
import journeymap.client.ui.fullscreen.layer.LayerDelegate;
import journeymap.client.ui.minimap.Shape;
import journeymap.client.ui.option.LocationFormat;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemeButton;
import journeymap.client.ui.theme.ThemeToggle;
import journeymap.client.ui.theme.ThemeToolbar;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.version.VersionCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProviderHell;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Displays the map as a full-screen overlay in-game.
 *
 * @author mwoodman
 */
public class Fullscreen extends JmUI
{
    final static MapState state = new MapState();
    final static GridRenderer gridRenderer = new GridRenderer(Context.UI.Fullscreen, 5);
    final WaypointDrawStepFactory waypointRenderer = new WaypointDrawStepFactory();
    final RadarDrawStepFactory radarRenderer = new RadarDrawStepFactory();
    final LayerDelegate layerDelegate = new LayerDelegate();
    FullMapProperties fullMapProperties = Journeymap.getClient().getFullMapProperties();
    CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
    boolean firstLayoutPass = true;
    boolean hideOptionsToolbar = false;
    Boolean isScrolling = false;
    int msx, msy, mx, my;
    Logger logger = Journeymap.getLogger();
    MapChat chat;
    ThemeButton buttonFollow, buttonZoomIn, buttonZoomOut, buttonDay, buttonNight, buttonTopo, buttonCaves;
    ThemeButton buttonAlert, buttonOptions, buttonActions, buttonClose;
    ThemeButton buttonTheme, buttonWaypointManager;
    ThemeButton buttonMobs, buttonAnimals, buttonPets, buttonVillagers, buttonPlayers, buttonGrid;
    ThemeToolbar mapTypeToolbar, optionsToolbar, menuToolbar, zoomToolbar;//, northEastToolbar;
    int bgColor = 0x222222;
    int statusForegroundColor;
    int statusBackgroundColor;
    float statusForegroundAlpha;
    float statusBackgroundAlpha;
    StatTimer drawScreenTimer = StatTimer.get("Fullscreen.drawScreen");
    StatTimer drawMapTimer = StatTimer.get("Fullscreen.drawScreen.drawMap", 50);
    StatTimer drawMapTimerWithRefresh = StatTimer.get("Fullscreen.drawMap+refreshState", 5);
    LocationFormat locationFormat = new LocationFormat();

    List<Overlay> tempOverlays = new ArrayList<Overlay>();

    /**
     * Default constructor
     */
    public Fullscreen()
    {
        super(null);
        mc = FMLClientHandler.instance().getClient();
        fullMapProperties = Journeymap.getClient().getFullMapProperties();
        state.refresh(mc, mc.player, fullMapProperties);
        boolean showCaves = state.isCaveMappingAllowed() && fullMapProperties.showCaves.get();
        gridRenderer.setContext(state.getWorldDir(), state.getMapType(showCaves));
        gridRenderer.setZoom(fullMapProperties.zoomLevel.get());
    }

    public static synchronized MapState state()
    {
        return state;
    }

    public static synchronized UIState uiState()
    {
        return gridRenderer.getUIState();
    }

    public void reset()
    {
        state.requireRefresh();
        gridRenderer.clear();
        buttonList.clear();
    }

    @Override
    public void initGui()
    {
        fullMapProperties = Journeymap.getClient().getFullMapProperties();
        Keyboard.enableRepeatEvents(true);

        // When switching dimensions, reset grid
        if (state.getCurrentMapType().dimension != mc.player.dimension)
        {
            gridRenderer.clear();
        }

        initButtons();

        // Check for first-time use
        String thisVersion = Journeymap.JM_VERSION.toString();
        String splashViewed = Journeymap.getClient().getCoreProperties().splashViewed.get();

        if (splashViewed == null || !thisVersion.equals(splashViewed))
        {
            UIManager.INSTANCE.openSplash(this);
        }
    }

    @Override
    public void drawScreen(int width, int height, float f)
    {
        try
        {
            drawBackground(0); // drawBackground
            drawMap();

            drawScreenTimer.start();

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
                    guibutton.drawButton(this.mc, width, height, 0f);
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
            UIManager.INSTANCE.closeAll();
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

        refreshState();
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
            MapType mapType = state.getCurrentMapType();
            int id = 0;

            // UI Colors
            bgColor = Theme.getColor(theme.fullscreen.mapBackgroundColor);
            statusForegroundColor = Theme.getColor(theme.fullscreen.statusLabel.foregroundColor);
            statusForegroundAlpha = Math.max(0, Math.min(1, theme.fullscreen.statusLabel.foregroundAlpha / 255));
            statusBackgroundColor = Theme.getColor(theme.fullscreen.statusLabel.backgroundColor);
            statusBackgroundAlpha = Math.max(0, Math.min(1, theme.fullscreen.statusLabel.backgroundAlpha / 255));

            // Maptype toggle listener
            OnOffButton.ToggleListener mapTypeToggleListener = new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    buttonDay.setToggled(false, false);
                    buttonNight.setToggled(false, false);
                    buttonTopo.setToggled(false, false);
                    if (state.isUnderground())
                    {
                        buttonCaves.setToggled(false, false);
                    }

                    if (button == buttonDay)
                    {
                        state.setMapType(MapType.Name.day);
                    }
                    else if (button == buttonNight)
                    {
                        state.setMapType(MapType.Name.night);
                    }
                    else if (button == buttonTopo)
                    {
                        state.setMapType(MapType.Name.topo);
                    }

                    button.setToggled(true, false);
                    state.requireRefresh();
                    return true;
                }
            };

            // Day Toggle
            buttonDay = new ThemeToggle(theme, "jm.fullscreen.map_day", "day");
            buttonDay.setToggled(mapType.isDay(), false);
            buttonDay.addToggleListener(mapTypeToggleListener);

            // Night Toggle
            buttonNight = new ThemeToggle(theme, "jm.fullscreen.map_night", "night");
            buttonNight.setToggled(mapType.isNight(), false);
            buttonNight.addToggleListener(mapTypeToggleListener);

            // Topo Toggle
            buttonTopo = new ThemeToggle(theme, "jm.fullscreen.map_topo", "topo");
            buttonTopo.setDrawButton(coreProperties.mapTopography.get());
            buttonTopo.setToggled(mapType.isTopo(), false);
            buttonTopo.addToggleListener(mapTypeToggleListener);

            // Caves Toggle
            buttonCaves = new ThemeToggle(theme, "jm.fullscreen.map_caves", "caves", fullMapProperties.showCaves);
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
                    UIManager.INSTANCE.openWaypointManager(null, Fullscreen.this);
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
                    UIManager.INSTANCE.getMiniMap().reset();
                    buttonList.clear();
                    return false;
                }
            });

            String[] tooltips = new String[]{
                    TextFormatting.ITALIC + Constants.getString("jm.common.ui_theme_name", theme.name),
                    TextFormatting.ITALIC + Constants.getString("jm.common.ui_theme_author", theme.author)
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
                        UIManager.INSTANCE.openOptionsManager();
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
                    UIManager.INSTANCE.openMapActions();
                    return true;
                }
            });

            // Version Check Alert
            String versionAvailable = Constants.getString("jm.common.new_version_available", VersionCheck.getVersionAvailable());
            buttonAlert = new ThemeToggle(theme, versionAvailable, versionAvailable, "alert");
            buttonAlert.setDrawButton(VersionCheck.getVersionIsChecked() && !VersionCheck.getVersionIsCurrent());
            buttonAlert.setToggled(true);
            buttonAlert.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    FullscreenActions.launchDownloadWebsite();
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
                    UIManager.INSTANCE.closeAll();
                    return true;
                }
            });

            buttonMobs = new ThemeToggle(theme, "jm.common.show_mobs", "monsters", fullMapProperties.showMobs);
            buttonMobs.setDrawButton(FeatureManager.isAllowed(Feature.RadarMobs));

            buttonAnimals = new ThemeToggle(theme, "jm.common.show_animals", "animals", fullMapProperties.showAnimals);
            buttonAnimals.setDrawButton(FeatureManager.isAllowed(Feature.RadarAnimals));

            buttonPets = new ThemeToggle(theme, "jm.common.show_pets", "pets", fullMapProperties.showPets);
            buttonPets.setDrawButton(FeatureManager.isAllowed(Feature.RadarAnimals));

            buttonVillagers = new ThemeToggle(theme, "jm.common.show_villagers", "villagers", fullMapProperties.showVillagers);
            buttonVillagers.setDrawButton(FeatureManager.isAllowed(Feature.RadarVillagers));

            buttonPlayers = new ThemeToggle(theme, "jm.common.show_players", "players", fullMapProperties.showPlayers);
            buttonPlayers.setDrawButton(!mc.isSingleplayer() && FeatureManager.isAllowed(Feature.RadarPlayers));

            buttonGrid = new ThemeToggle(theme, "jm.common.show_grid", "grid", fullMapProperties.showGrid);
            buttonGrid.setTooltip(TextFormatting.GRAY.toString() + Constants.getString("jm.common.show_grid_shift.tooltip"));
            buttonGrid.addToggleListener(new OnOffButton.ToggleListener()
            {
                @Override
                public boolean onToggle(OnOffButton button, boolean toggled)
                {
                    boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                    if (shiftDown)
                    {
                        UIManager.INSTANCE.openGridEditor(Fullscreen.this);
                        buttonGrid.setValue(true);
                        return false;
                    }
                    return true;
                }
            });

            // Toolbars
            mapTypeToolbar = new ThemeToolbar(theme, buttonCaves, buttonTopo, buttonNight, buttonDay);
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
        // Check to see whether theme button texture ids still valid
        if (!buttonDay.hasValidTextures())
        {
            buttonList.clear();
        }

        if (buttonList.isEmpty())
        {
            initButtons();
        }

        // Update toggles
        boolean notNether = !(mc.world.provider instanceof WorldProviderHell);
        buttonDay.setEnabled(notNether);
        buttonNight.setEnabled(notNether);
        buttonTopo.setEnabled(notNether);
        buttonCaves.setEnabled(notNether && state.isUnderground() && state.isCaveMappingAllowed());
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
    public void handleMouseInput() throws IOException
    {
        try
        {
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
                        mouseReleased(mx, my, Mouse.getEventButton());
                    }
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        try
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
            Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), gridRenderer.getHeight() - Mouse.getEventY());
            layerDelegate.onMouseClicked(mc, gridRenderer, mousePosition, mouseButton, getMapFontScale());
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int which)
    {
        try
        {
            super.mouseReleased(mouseX, mouseY, which);

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
                        gridRenderer.updateTiles(state.getCurrentMapType(), state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, false, 0, 0);
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
                Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), gridRenderer.getHeight() - Mouse.getEventY());
                layerDelegate.onMouseMove(mc, gridRenderer, mousePosition, getMapFontScale());
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
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
    public void keyTyped(char c, int i) throws IOException
    {
        if (chat != null && !chat.isHidden())
        {
            chat.keyTyped(c, i);
            return;
        }

        if (i == Keyboard.KEY_O)
        {
            UIManager.INSTANCE.openOptionsManager();
            return;
        }

        if (i == Keyboard.KEY_ESCAPE || i == Constants.KB_MAP.getKeyCode())
        {
            UIManager.INSTANCE.closeAll();
            return;
        }

        boolean controlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

        if (i == Constants.KB_MAP_ZOOMIN.getKeyCode())
        {
            if (controlDown)
            {
                Tile.switchTileRenderType();
            }
            else
            {
                zoomIn();
            }
            return;
        }

        if (i == Constants.KB_MAP_ZOOMOUT.getKeyCode())
        {
            if (controlDown)
            {
                Tile.switchTileDisplayQuality();
            }
            else
            {
                zoomOut();
            }
            return;
        }

        if (i == Constants.KB_MAP_DAY.getKeyCode() || i == Constants.KB_MAP_NIGHT.getKeyCode())
        {
            state.toggleMapType();
            KeyBinding.unPressAllKeys();
            return;
        }

        if (i == Constants.KB_MAP_NIGHT.getKeyCode())
        {
            state.setMapType(MapType.Name.night);
            return;
        }

        if (i == Constants.KB_WAYPOINT.getKeyCode())
        {
            if (controlDown)
            {
                UIManager.INSTANCE.openWaypointManager(null, this);
            }
            else
            {
                Waypoint waypoint = Waypoint.of(mc.player);
                UIManager.INSTANCE.openWaypointEditor(waypoint, true, null);
            }
            return;
        }

        // North
        if (i == mc.gameSettings.keyBindForward.getKeyCode())
        {
            moveCanvas(0, -16);
            return;
        }

        // West
        if (i == mc.gameSettings.keyBindLeft.getKeyCode())
        {
            moveCanvas(-16, 0);
            return;
        }

        // South
        if (i == mc.gameSettings.keyBindBack.getKeyCode())
        {
            moveCanvas(0, 16);
            return;
        }

        // East
        if (i == mc.gameSettings.keyBindRight.getKeyCode())
        {
            moveCanvas(16, 0);
            return;
        }

        // Open inventory
        if (i == mc.gameSettings.keyBindInventory.getKeyCode())
        {
            UIManager.INSTANCE.openInventory();
            return;
        }

        // Open chat
        if (i == mc.gameSettings.keyBindChat.getKeyCode())
        {
            openChat("");
            return;
        }

        // Open chat with command prefix (Minecraft.java does this in runTick() )
        if (i == mc.gameSettings.keyBindCommand.getKeyCode())
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
        DrawUtil.drawRectangle(0, 0, width, height, bgColor, 1f);
    }

    void drawMap()
    {
        final boolean refreshReady = isRefreshReady();
        final StatTimer timer = refreshReady ? drawMapTimerWithRefresh : drawMapTimer;
        timer.start();

        try
        {
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
                    gridRenderer.setContext(state.getWorldDir(), state.getCurrentMapType());
                }
            }

            // Clear GL error queue of anything that happened before JM starts drawing, don't report them
            gridRenderer.clearGlErrors(false);

            gridRenderer.updateRotation(0);

            if (state.follow.get())
            {
                gridRenderer.center(state.getWorldDir(), state.getCurrentMapType(), mc.player.posX, mc.player.posZ, fullMapProperties.zoomLevel.get());
            }
            gridRenderer.updateTiles(state.getCurrentMapType(), state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, false, 0, 0);
            gridRenderer.draw(1f, xOffset, yOffset, fullMapProperties.showGrid.get());
            gridRenderer.draw(state.getDrawSteps(), xOffset, yOffset, getMapFontScale(), 0);
            gridRenderer.draw(state.getDrawWaypointSteps(), xOffset, yOffset, getMapFontScale(), 0);

            if (fullMapProperties.showSelf.get())
            {
                Point2D playerPixel = gridRenderer.getPixel(mc.player.posX, mc.player.posZ);
                if (playerPixel != null)
                {
                    boolean large = fullMapProperties.playerDisplay.get().isLarge();
                    TextureImpl bgTex = large ? TextureCache.getTexture(TextureCache.PlayerArrowBG_Large) : TextureCache.getTexture(TextureCache.PlayerArrowBG);
                    TextureImpl fgTex = large ? TextureCache.getTexture(TextureCache.PlayerArrow_Large) : TextureCache.getTexture(TextureCache.PlayerArrow);
                    DrawUtil.drawColoredEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, bgTex, 0xffffff, 1f, 1f, mc.player.rotationYawHead);

                    int playerColor = coreProperties.getColor(coreProperties.colorSelf);
                    DrawUtil.drawColoredEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, fgTex, playerColor, 1f, 1f, mc.player.rotationYawHead);
                }
            }

            gridRenderer.draw(layerDelegate.getDrawSteps(), xOffset, yOffset, getMapFontScale(), 0);

            DrawUtil.drawLabel(state.playerLastPos, mc.displayWidth / 2, mc.displayHeight, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above,
                    statusBackgroundColor, statusBackgroundAlpha, statusForegroundColor, statusForegroundAlpha, getMapFontScale(), true);

            drawLogo();

            sizeDisplay(true);
        }
        finally
        {
            timer.stop();

            // Clear GL error queue of anything that happened during JM drawing and report them
            gridRenderer.clearGlErrors(true);
        }


        //GridRenderer.addDebugMessage(timer.getName(), timer.getSimpleReportString());
        //GridRenderer.addDebugMessage(StatTimer.getTexture("GridRenderer.center").getName(), StatTimer.getTexture("GridRenderer.center").getSimpleReportString());

    }

    private int getMapFontScale()
    {
        return (fullMapProperties.fontScale.get());
    }

    public void centerOn(Waypoint waypoint)
    {
        if (waypoint.getDimensions().contains(mc.player.dimension))
        {
            state.follow.set(false);
            state.requireRefresh();
            int x = waypoint.getX();
            int z = waypoint.getZ();

            gridRenderer.center(state.getWorldDir(), state.getCurrentMapType(), x, z, fullMapProperties.zoomLevel.get());

            if (!waypoint.isPersistent())
            {
                addTempMarker(waypoint);
            }

            refreshState();
            updateScreen();
        }
    }

    public void addTempMarker(Waypoint waypoint)
    {
        try
        {
            BlockPos pos = waypoint.getBlockPos();

            PolygonOverlay polygonOverlay = new PolygonOverlay(Journeymap.MOD_ID, waypoint.getName(), mc.player.dimension,
                    new ShapeProperties().setStrokeColor(0x0000ff).setStrokeOpacity(1f).setStrokeWidth(1.5f),
                    new MapPolygon(pos.add(-1, 0, 2), pos.add(2, 0, 2), pos.add(2, 0, -1), pos.add(-1, 0, -1)));

            polygonOverlay.setActiveMapTypes(EnumSet.allOf(Context.MapType.class));
            polygonOverlay.setActiveUIs(EnumSet.of(Context.UI.Fullscreen));
            polygonOverlay.setLabel(waypoint.getName());
            tempOverlays.add(polygonOverlay);
            ClientAPI.INSTANCE.show(polygonOverlay);
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Error showing temp location marker: " + LogFormatter.toPartialString(t));
        }
    }

    /**
     * Get a snapshot of the player's biome, effective map state, etc.
     */
    void refreshState()
    {
        // Check player status
        EntityPlayer player = mc.player;
        if (player == null)
        {
            logger.warn("Could not get player");
            return;
        }

        StatTimer timer = StatTimer.get("Fullscreen.refreshState");
        timer.start();

        // Update the state first
        fullMapProperties = Journeymap.getClient().getFullMapProperties();
        state.refresh(mc, player, fullMapProperties);

        if (state.getCurrentMapType().dimension != mc.player.dimension)
        {
            setFollow(true);
        }

        gridRenderer.setContext(state.getWorldDir(), state.getCurrentMapType());

        // Center core renderer
        if (state.follow.get())
        {
            gridRenderer.center(state.getWorldDir(), state.getCurrentMapType(), mc.player.posX, mc.player.posZ, fullMapProperties.zoomLevel.get());
        }
        else
        {
            gridRenderer.setZoom(fullMapProperties.zoomLevel.get());
        }

        // Update tiles
        gridRenderer.updateTiles(state.getCurrentMapType(), state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, true, 0, 0);

        // Build list of drawSteps
        state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, fullMapProperties, false);

        // Update player pos
        LocationFormat.LocationFormatKeys locationFormatKeys = locationFormat.getFormatKeys(fullMapProperties.locationFormat.get());
        state.playerLastPos = locationFormatKeys.format(fullMapProperties.locationFormatVerbose.get(),
                MathHelper.floor(mc.player.posX),
                MathHelper.floor(mc.player.posZ),
                MathHelper.floor(mc.player.getEntityBoundingBox().minY),
                mc.player.chunkCoordY) + " " + state.getPlayerBiome();

        // Reset timer
        state.updateLastRefresh();
        timer.stop();

        // Trigger a mouse move event in the layer delegate so draw steps can update if needed
        Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), gridRenderer.getHeight() - Mouse.getEventY());
        layerDelegate.onMouseMove(mc, gridRenderer, mousePosition, getMapFontScale());

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
        for (Overlay temp : tempOverlays)
        {
            ClientAPI.INSTANCE.remove(temp);
        }

        gridRenderer.updateUIState(false);

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
        gridRenderer.updateTiles(state.getCurrentMapType(), state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, true, 0, 0);
        ClientAPI.INSTANCE.flagOverlaysForRerender();
        setFollow(false);
    }

    @Override
    protected void drawLogo()
    {
        if (logo.isDefunct())
        {
            logo = TextureCache.getTexture(TextureCache.Logo);
        }
        DrawUtil.sizeDisplay(mc.displayWidth, mc.displayHeight);
        DrawUtil.drawImage(logo, 8, 8, false, 1, 0);
        DrawUtil.sizeDisplay(width, height);
    }

    @Override
    public final boolean doesGuiPauseGame()
    {
        return false;
    }

    /**
     * Set theme by name
     */
    public void setTheme(String name)
    {
        try
        {
            MiniMapProperties mmp = Journeymap.getClient().getMiniMapProperties(Journeymap.getClient().getActiveMinimapId());
            mmp.shape.set(Shape.Rectangle);
            mmp.showBiome.set(false);
            mmp.sizePercent.set(20);
            mmp.save();
            Theme theme = ThemeFileHandler.getThemeByName(name);
            ThemeFileHandler.setCurrentTheme(theme);
            UIManager.INSTANCE.getMiniMap().reset();
            ChatLog.announceI18N("jm.common.ui_theme_applied");
            UIManager.INSTANCE.closeAll();
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Could not load Theme: " + LogFormatter.toString(e));
        }
    }
}

