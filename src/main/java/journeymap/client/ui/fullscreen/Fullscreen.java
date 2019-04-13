/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
import journeymap.client.data.DataCache;
import journeymap.client.data.WaypointsData;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.io.MapSaver;
import journeymap.client.io.ThemeLoader;
import journeymap.client.log.ChatLog;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockMD;
import journeymap.client.model.EntityDTO;
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
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.task.main.EnsureCurrentColorsTask;
import journeymap.client.task.multi.MapRegionTask;
import journeymap.client.task.multi.SaveMapTask;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.IntSliderButton;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.component.OnOffButton;
import journeymap.client.ui.dialog.AutoMapConfirmation;
import journeymap.client.ui.dialog.DeleteMapConfirmation;
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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Displays the map as a full-screen overlay in-game.
 *
 * @author techbrew
 */
public class Fullscreen extends JmUI implements ITabCompleter
{
    /**
     * The constant state.
     */
    final static MapState state = new MapState();
    /**
     * The constant gridRenderer.
     */
    final static GridRenderer gridRenderer = new GridRenderer(Context.UI.Fullscreen, 5);
    /**
     * The Waypoint renderer.
     */
    final WaypointDrawStepFactory waypointRenderer = new WaypointDrawStepFactory();
    /**
     * The Radar renderer.
     */
    final RadarDrawStepFactory radarRenderer = new RadarDrawStepFactory();
    /**
     * The Layer delegate.
     */
    final LayerDelegate layerDelegate;
    /**
     * The Full map properties.
     */
    FullMapProperties fullMapProperties = Journeymap.getClient().getFullMapProperties();
    /**
     * The Core properties.
     */
    CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
    /**
     * The First layout pass.
     */
    boolean firstLayoutPass = true;

    /**
     * The Is scrolling.
     */
    Boolean isScrolling = false;
    /**
     * Mouse scroll x
     */
    int msx;

    /**
     * Mouse scroll y
     */
    int msy;

    /**
     * Mouse x
     */
    int mx;

    /**
     * Mouse y
     */
    int my;
    /**
     * The Logger.
     */
    Logger logger = Journeymap.getLogger();
    /**
     * The Chat.
     */
    MapChat chat;
    /**
     * The Button follow.
     */
    ThemeButton buttonFollow;
    /**
     * The Button zoom in.
     */
    ThemeButton buttonZoomIn;
    /**
     * The ThemeButton button zoom out.
     */
    ThemeButton buttonZoomOut;
    /**
     * The ThemeButton button day.
     */
    ThemeButton buttonDay;
    /**
     * The ThemeButton button night.
     */
    ThemeButton buttonNight;
    /**
     * The ThemeButton button topo.
     */
    ThemeButton buttonTopo;
    /**
     * The ThemeButton button layers.
     */
    ThemeButton buttonLayers;
    /**
     * The ThemeButton button caves.
     */
    ThemeButton buttonCaves;
    /**
     * The ThemeButton button alert.
     */
    ThemeButton buttonAlert;
    /**
     * The ThemeButton button options.
     */
    ThemeButton buttonOptions;

    /**
     * The ThemeButton button close.
     */
    ThemeButton buttonClose;
    /**
     * The Button theme.
     */
    ThemeButton buttonTheme;
    /**
     * The Button waypoint manager.
     */
    ThemeButton buttonWaypointManager;
    /**
     * The Button mobs.
     */
    ThemeButton buttonMobs;
    /**
     * The ThemeButton button animals.
     */
    ThemeButton buttonAnimals;
    /**
     * The ThemeButton button pets.
     */
    ThemeButton buttonPets;
    /**
     * The ThemeButton button villagers.
     */
    ThemeButton buttonVillagers;
    /**
     * The ThemeButton button players.
     */
    ThemeButton buttonPlayers;
    /**
     * The ThemeButton button grid.
     */
    ThemeButton buttonGrid;
    /**
     * The ThemeButton to show keys.
     */
    ThemeButton buttonKeys;

    ThemeButton buttonAutomap;
    ThemeButton buttonSavemap;
    ThemeButton buttonDeletemap;
    ThemeButton buttonDisable;
    ThemeButton buttonResetPalette;
    ThemeButton buttonBrowser;
    ThemeButton buttonAbout;

    /**
     * The Map type toolbar.
     */
    ThemeToolbar mapTypeToolbar;
    /**
     * The Options toolbar.
     */
    ThemeToolbar optionsToolbar;
    /**
     * The Menu toolbar.
     */
    ThemeToolbar menuToolbar;
    /**
     * The Zoom toolbar.
     */
    ThemeToolbar zoomToolbar;
    /**
     * The Bg color.
     */
    int bgColor = 0x222222;

    /**
     * Label spec for status text
     */
    Theme.LabelSpec statusLabelSpec;

    /**
     * The Draw screen timer.
     */
    StatTimer drawScreenTimer = StatTimer.get("Fullscreen.drawScreen");
    /**
     * The Draw map timer.
     */
    StatTimer drawMapTimer = StatTimer.get("Fullscreen.drawScreen.drawMap", 50);
    /**
     * The Draw map timer with refresh.
     */
    StatTimer drawMapTimerWithRefresh = StatTimer.get("Fullscreen.drawMap+refreshState", 5);
    /**
     * The Location format.
     */
    LocationFormat locationFormat = new LocationFormat();

    /**
     * The Temp overlays.
     */
    List<Overlay> tempOverlays = new ArrayList<Overlay>();

    private IntSliderButton sliderCaveLayer;
    private List<String> autoMapOnTooltip;
    private List<String>autoMapOffTooltip;
    private Rectangle2D.Double mapTypeToolbarBounds;
    private Rectangle2D.Double optionsToolbarBounds;
    private Rectangle2D.Double menuToolbarBounds;

    /**
     * Default constructor
     */
    public Fullscreen()
    {
        super(null);
        mc = FMLClientHandler.instance().getClient();
        layerDelegate = new LayerDelegate(this);
        if (Journeymap.getClient().getFullMapProperties().showCaves.get() && DataCache.getPlayer().underground && state.follow.get())
        {
            state.setMapType(MapType.underground(DataCache.getPlayer()));
        }
    }

    /**
     * State map state.
     *
     * @return the map state
     */
    public static synchronized MapState state()
    {
        return state;
    }

    /**
     * Ui state ui state.
     *
     * @return the ui state
     */
    public static synchronized UIState uiState()
    {
        return gridRenderer.getUIState();
    }

    /**
     * Reset.
     */
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

        state.requireRefresh();
        state.refresh(mc, mc.player, fullMapProperties);
        MapType mapType = state.getMapType();

        Keyboard.enableRepeatEvents(true);

        // When switching dimensions, reset grid
        if (mapType.dimension != mc.player.dimension) {
            gridRenderer.clear();
        }

        initButtons();

        // Check for first-time use
        String thisVersion = Journeymap.JM_VERSION.toString();
        String splashViewed = Journeymap.getClient().getCoreProperties().splashViewed.get();

        if (splashViewed == null || !thisVersion.equals(splashViewed)) {
            UIManager.INSTANCE.openSplash(this);
        }
    }

    @Override
    public void drawScreen(int width, int height, float f)
    {
        try {
            drawBackground(0); // drawBackground
            drawMap();

            drawScreenTimer.start();

            layoutButtons();

            List<String> tooltip = null;

            if (firstLayoutPass) {
                layoutButtons();
                updateMapType(state.getMapType());
                firstLayoutPass = false;
            } else {
                for (int k = 0; k < this.buttonList.size(); ++k) {
                    GuiButton guibutton = this.buttonList.get(k);
                    guibutton.drawButton(this.mc, width, height, f);
                    if (tooltip == null) {
                        if (guibutton instanceof Button) {
                            Button button = (Button) guibutton;
                            if (button.mouseOver(mx, my)) {
                                tooltip = button.getTooltip();
                            }
                        }
                    }
                }
            }

            if (chat != null) {
                chat.drawScreen(width, height, f);
            }

            if (tooltip != null && !tooltip.isEmpty()) {
                drawHoveringText(tooltip, mx, my, getFontRenderer());
                RenderHelper.disableStandardItemLighting();
            }

        } catch (Throwable e) {
            logger.log(Level.ERROR, "Unexpected exception in jm.fullscreen.drawScreen(): " + LogFormatter.toString(e));
            UIManager.INSTANCE.closeAll();
        } finally {
            drawScreenTimer.stop();
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    { // actionPerformed

        if (guibutton instanceof ThemeToolbar) {
            return;
        }

        if (guibutton instanceof OnOffButton) {
            ((OnOffButton) guibutton).toggle();
        }

        if (optionsToolbar.contains(guibutton)) {
            refreshState();
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height)
    {
        super.setWorldAndResolution(minecraft, width, height);
        state.requireRefresh();

        if (chat == null) {
            chat = new MapChat("", true);
        }
        if (chat != null) {
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
        if (buttonList.isEmpty()) {
            firstLayoutPass = true;
            Theme theme = ThemeLoader.getCurrentTheme();
            MapType mapType = state.getMapType();

            // UI Colors
            bgColor = theme.fullscreen.background.getColor();
            statusLabelSpec = theme.fullscreen.statusLabel;

            // Maptype buttons
            buttonDay = new ThemeToggle(theme, "jm.fullscreen.map_day", "day");
            buttonNight = new ThemeToggle(theme, "jm.fullscreen.map_night", "night");
            buttonTopo = new ThemeToggle(theme, "jm.fullscreen.map_topo", "topo");
            buttonLayers = new ThemeToggle(theme, "jm.fullscreen.map_cave_layers", "layers");

            // Day Toggle
            buttonDay.setToggled(mapType.isDay(), false);
            buttonDay.setStaysOn(true);
            buttonDay.addToggleListener((button, toggled) -> {
                if (button.enabled)
                {
                    updateMapType(MapType.day(state.getDimension()));
                }
                return button.enabled;
            });

            // Night Toggle
            buttonNight.setToggled(mapType.isNight(), false);

            buttonNight.setStaysOn(true);
            buttonNight.addToggleListener((button, toggled) -> {
                if (button.enabled)
                {
                    updateMapType(MapType.night(state.getDimension()));
                }
                return button.enabled;
            });

            // Topo Toggle
            buttonTopo.setDrawButton(coreProperties.mapTopography.get());

            buttonTopo.setToggled(mapType.isTopo(), false);
            buttonTopo.setStaysOn(true);
            buttonTopo.addToggleListener((button, toggled) -> {
                if (button.enabled)
                {
                    updateMapType(MapType.topo(state.getDimension()));
                }
                return button.enabled;
            });

            // Cave Layers Toggle
            buttonLayers.setEnabled(FeatureManager.isAllowed(Feature.MapCaves));
            buttonLayers.setToggled(mapType.isUnderground(), false);
            buttonLayers.setStaysOn(true);
            buttonLayers.addToggleListener((button, toggled) -> {
                if (button.enabled)
                {
                    updateMapType(MapType.underground(DataCache.getPlayer()));
                }
                return button.enabled;
            });

            FontRenderer fontRenderer = getFontRenderer();

            // Cave Layers Slider
            sliderCaveLayer = new IntSliderButton(state.getLastSlice(), Constants.getString("jm.fullscreen.map_cave_layers.button") + " ", "");
            sliderCaveLayer.setWidth(sliderCaveLayer.getFitWidth(fontRenderer) + fontRenderer.getStringWidth("0"));
            sliderCaveLayer.setDefaultStyle(false);
            sliderCaveLayer.setDrawBackground(true);
            Theme.Control.ButtonSpec buttonSpec = buttonLayers.getButtonSpec();
            sliderCaveLayer.setBackgroundColors(buttonSpec.buttonDisabled.getColor(), buttonSpec.buttonOff.getColor(), buttonSpec.buttonOff.getColor());
            sliderCaveLayer.setLabelColors(buttonSpec.iconHoverOff.getColor(), buttonSpec.iconHoverOn.getColor(), buttonSpec.iconDisabled.getColor());
            sliderCaveLayer.addClickListener(button -> {
                state.setMapType(MapType.underground(sliderCaveLayer.getValue(), state.getDimension()));
                refreshState();
                return true;
            });

            buttonList.add(sliderCaveLayer);


            // Follow
            buttonFollow = new ThemeButton(theme, "jm.fullscreen.follow", "follow");
            buttonFollow.addToggleListener((button, toggled) -> {
                toggleFollow();
                return true;
            });

            // Zoom In
            buttonZoomIn = new ThemeButton(theme, "jm.fullscreen.zoom_in", "zoomin");
            buttonZoomIn.setEnabled(fullMapProperties.zoomLevel.get() < state.maxZoom);
            buttonZoomIn.addToggleListener((button, toggled) -> {
                zoomIn();
                return true;
            });

            // Zoom Out
            buttonZoomOut = new ThemeButton(theme, "jm.fullscreen.zoom_out", "zoomout");
            buttonZoomOut.setEnabled(fullMapProperties.zoomLevel.get() > state.minZoom);
            buttonZoomOut.addToggleListener((button, toggled) -> {
                zoomOut();
                return true;
            });

            // Waypoints
            buttonWaypointManager = new ThemeButton(theme, "jm.waypoint.waypoints_button", "waypoints");
            buttonWaypointManager.setDrawButton(WaypointsData.isManagerEnabled());
            buttonWaypointManager.addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.openWaypointManager(null, Fullscreen.this);
                return true;
            });

            // Waypoints
            buttonTheme = new ThemeButton(theme, "jm.common.ui_theme", "theme");
            buttonTheme.addToggleListener((button, toggled) -> {
                ThemeLoader.loadNextTheme();
                UIManager.INSTANCE.getMiniMap().reset();
                buttonList.clear();
                return false;
            });

            String[] tooltips = new String[]{
                    TextFormatting.ITALIC + Constants.getString("jm.common.ui_theme_name", theme.name),
                    TextFormatting.ITALIC + Constants.getString("jm.common.ui_theme_author", theme.author)
            };
            buttonTheme.setAdditionalTooltips(Arrays.asList(tooltips));

            // Options
            buttonOptions = new ThemeButton(theme, "jm.common.options_button", "options");
            buttonOptions.addToggleListener((button, toggled) -> {
                try {
                    UIManager.INSTANCE.openOptionsManager();
                    buttonList.clear();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });

            // Version Check Alert
            String versionAvailable = Constants.getString("jm.common.new_version_available", VersionCheck.getVersionAvailable());
            buttonAlert = new ThemeButton(theme, versionAvailable, versionAvailable, false, "alert");
            buttonAlert.setDrawButton(VersionCheck.getVersionIsChecked() && !VersionCheck.getVersionIsCurrent());
            buttonAlert.setToggled(true);
            buttonAlert.addToggleListener((button, toggled) -> {
                FullscreenActions.launchDownloadWebsite();
                buttonAlert.setDrawButton(false);
                return true;
            });

            // Close
            buttonClose = new ThemeButton(theme, "jm.common.close", "close");
            buttonClose.addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.closeAll();
                return true;
            });

            // Display buttons
            buttonCaves = new ThemeToggle(theme, "jm.common.show_caves", "caves", fullMapProperties.showCaves);
            buttonCaves.setTooltip(Constants.getString("jm.common.show_caves.tooltip"));
            buttonCaves.setDrawButton(state.isCaveMappingAllowed());
            buttonCaves.addToggleListener((button, toggled) -> {
                EntityDTO player = DataCache.getPlayer();
                if(toggled && player.underground)
                {
                    updateMapType(MapType.underground(player));
                }
                return true;
            });

            buttonMobs = new ThemeToggle(theme, "jm.common.show_mobs", "monsters", fullMapProperties.showMobs);
            buttonMobs.setTooltip(Constants.getString("jm.common.show_mobs.tooltip"));
            buttonMobs.setDrawButton(FeatureManager.isAllowed(Feature.RadarMobs));

            buttonAnimals = new ThemeToggle(theme, "jm.common.show_animals", "animals", fullMapProperties.showAnimals);
            buttonAnimals.setTooltip(Constants.getString("jm.common.show_animals.tooltip"));
            buttonAnimals.setDrawButton(FeatureManager.isAllowed(Feature.RadarAnimals));

            buttonPets = new ThemeToggle(theme, "jm.common.show_pets", "pets", fullMapProperties.showPets);
            buttonPets.setTooltip(Constants.getString("jm.common.show_pets.tooltip"));
            buttonPets.setDrawButton(FeatureManager.isAllowed(Feature.RadarAnimals));

            buttonVillagers = new ThemeToggle(theme, "jm.common.show_villagers", "villagers", fullMapProperties.showVillagers);
            buttonVillagers.setTooltip(Constants.getString("jm.common.show_villagers.tooltip"));
            buttonVillagers.setDrawButton(FeatureManager.isAllowed(Feature.RadarVillagers));

            buttonPlayers = new ThemeToggle(theme, "jm.common.show_players", "players", fullMapProperties.showPlayers);
            buttonPlayers.setTooltip(Constants.getString("jm.common.show_players.tooltip"));
            buttonPlayers.setDrawButton(!mc.isSingleplayer() && FeatureManager.isAllowed(Feature.RadarPlayers));

            buttonGrid = new ThemeToggle(theme, "jm.common.show_grid", "grid", fullMapProperties.showGrid);
            buttonGrid.setTooltip(Constants.getString("jm.common.show_grid_shift.tooltip"));
            buttonGrid.setTooltip(Constants.getString("jm.common.show_grid_shift.tooltip"));
            buttonGrid.addToggleListener((button, toggled) -> {
                boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                if (shiftDown) {
                    UIManager.INSTANCE.openGridEditor(Fullscreen.this);
                    buttonGrid.setValue(true);
                    return false;
                }
                return true;
            });



            buttonKeys = new ThemeToggle(theme, "jm.common.show_keys", "keys", fullMapProperties.showKeys);
            buttonKeys.setTooltip(Constants.getString("jm.common.show_keys.tooltip"));

            // New actions buttons
            buttonAbout = new ThemeButton(theme, "jm.common.splash_about", "about");
            buttonAbout.addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.openSplash(Fullscreen.this);
                return true;
            });

            buttonSavemap = new ThemeButton(theme, "jm.common.save_map", "savemap");
            buttonSavemap.addToggleListener((button, toggled) -> {
                buttonSavemap.setEnabled(false);
                try
                {
                    final MapSaver mapSaver = new MapSaver(state.getWorldDir(), state.getMapType());
                    if (mapSaver.isValid())
                    {
                        Journeymap.getClient().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
                        ChatLog.announceI18N("jm.common.save_filename", mapSaver.getSaveFileName());
                    }
                } finally {
                    buttonSavemap.setToggled(false);
                    buttonSavemap.setEnabled(true);
                }
                return true;
            });

            buttonBrowser = new ThemeButton(theme, "jm.common.use_browser", "browser");
            boolean webMapEnabled = Journeymap.getClient().getWebMapProperties().enabled.get();
            buttonBrowser.setEnabled(webMapEnabled);
            buttonBrowser.setDrawButton(webMapEnabled);
            buttonBrowser.addToggleListener((button, toggled) -> {
                FullscreenActions.launchLocalhost();
                return true;
            });


            boolean automapRunning = Journeymap.getClient().isTaskManagerEnabled(MapRegionTask.Manager.class);
            String autoMapOn = Constants.getString("jm.common.automap_stop_title");
            String autoMapOff = Constants.getString("jm.common.automap_title");
            autoMapOnTooltip = fontRenderer.listFormattedStringToWidth(Constants.getString("jm.common.automap_stop_text"), 200);
            autoMapOffTooltip = fontRenderer.listFormattedStringToWidth(Constants.getString("jm.common.automap_text"), 200);
            buttonAutomap = new ThemeToggle(theme, autoMapOn, autoMapOff,"automap");
            buttonAutomap.setEnabled(FMLClientHandler.instance().getClient().isSingleplayer() && Journeymap.getClient().getCoreProperties().mappingEnabled.get());
            buttonAutomap.setToggled(automapRunning, false);
            buttonAutomap.addToggleListener((button, toggled) -> {
                if (toggled)
                {
                    UIManager.INSTANCE.open(AutoMapConfirmation.class, this);
                }
                else
                {
                    Journeymap.getClient().toggleTask(MapRegionTask.Manager.class, false, null);
                    buttonAutomap.setToggled(false, false);
                    buttonList.clear();
                }
                return true;
            });

            buttonDeletemap = new ThemeButton(theme, "jm.common.deletemap_title", "delete");
            buttonDeletemap.setAdditionalTooltips(fontRenderer.listFormattedStringToWidth((Constants.getString("jm.common.deletemap_text")), 200));
            buttonDeletemap.addToggleListener((button, toggled) -> {
                UIManager.INSTANCE.open(DeleteMapConfirmation.class, this);
                return false;
            });

            buttonDisable = new ThemeToggle(theme, "jm.common.enable_mapping_false", "disable");
            buttonDisable.addToggleListener((button, toggled) -> {
                Journeymap.getClient().getCoreProperties().mappingEnabled.set(!toggled);
                if (Journeymap.getClient().getCoreProperties().mappingEnabled.get())
                {
                    DataCache.INSTANCE.invalidateChunkMDCache();
                    ChatLog.announceI18N("jm.common.enable_mapping_true_text");
                }
                else
                {
                    Journeymap.getClient().stopMapping();
                    BlockMD.reset();
                    ChatLog.announceI18N("jm.common.enable_mapping_false_text");
                }
                return true;
            });

            buttonResetPalette = new ThemeButton(theme, "jm.common.colorreset_title", "reset");
            buttonResetPalette.setAdditionalTooltips(fontRenderer.listFormattedStringToWidth(Constants.getString("jm.common.colorreset_text"), 200));
            buttonResetPalette.addToggleListener((button, toggled) -> {
                Journeymap.getClient().queueMainThreadTask(new EnsureCurrentColorsTask(true, true));
                return false;
            });

            // Toolbars
            mapTypeToolbar = new ThemeToolbar(theme, buttonLayers, buttonTopo, buttonNight, buttonDay);
            mapTypeToolbar.addAllButtons(this);

            optionsToolbar = new ThemeToolbar(theme, buttonCaves, buttonMobs, buttonAnimals, buttonPets, buttonVillagers, buttonPlayers, buttonGrid, buttonKeys);
            optionsToolbar.addAllButtons(this);
            optionsToolbar.visible = false; // Hide until laid out

            menuToolbar = new ThemeToolbar(theme, buttonWaypointManager, buttonOptions, buttonAbout, buttonBrowser, buttonTheme, buttonResetPalette, buttonDeletemap, buttonSavemap, buttonAutomap, buttonDisable);
            menuToolbar.addAllButtons(this);
            menuToolbar.visible = false; // Hide until laid out

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
        if (buttonDay!=null && !buttonDay.hasValidTextures()) {
            buttonList.clear();
        }

        if (buttonList.isEmpty()) {
            initButtons();
        }

        menuToolbar.setDrawToolbar(!isChatOpen());

        // Update toggles
        MapType mapType = state.getMapType();

        buttonDay.setEnabled(state.isSurfaceMappingAllowed());
        buttonDay.setToggled(buttonDay.enabled && mapType.isDay());

        buttonNight.setEnabled(state.isSurfaceMappingAllowed());
        buttonNight.setToggled(buttonNight.enabled && mapType.isNight());

        buttonTopo.setEnabled(state.isTopoMappingAllowed());
        buttonTopo.setToggled(buttonTopo.enabled && mapType.isTopo());

        buttonCaves.setEnabled(state.isCaveMappingAllowed());
        buttonCaves.setToggled(buttonCaves.enabled && mapType.isUnderground());

        buttonFollow.setEnabled(!state.follow.get());

        boolean automapRunning = Journeymap.getClient().isTaskManagerEnabled(MapRegionTask.Manager.class);
        boolean mappingEnabled = Journeymap.getClient().getCoreProperties().mappingEnabled.get();
        buttonDisable.setToggled(!mappingEnabled, false);

        buttonAutomap.setToggled(automapRunning, false);
        buttonAutomap.setEnabled(mappingEnabled);

        buttonAutomap.setAdditionalTooltips(automapRunning ? autoMapOnTooltip : autoMapOffTooltip);

        boolean webMapEnabled = Journeymap.getClient().getWebMapProperties().enabled.get();
        buttonBrowser.setEnabled(webMapEnabled && mappingEnabled);
        buttonBrowser.setDrawButton(webMapEnabled);

        boolean mainThreadActive = Journeymap.getClient().isMainThreadTaskActive();
        buttonResetPalette.setEnabled(!mainThreadActive && mappingEnabled);
        buttonDeletemap.setEnabled(!mainThreadActive);
        buttonDisable.setEnabled(!mainThreadActive);


        // Update toolbar layouts
        int padding = mapTypeToolbar.getToolbarSpec().padding;
        zoomToolbar.layoutCenteredVertical(zoomToolbar.getHMargin(), height / 2, true, padding);

        int topY = mapTypeToolbar.getVMargin();

        int margin = mapTypeToolbar.getHMargin();

        buttonClose.leftOf(width - zoomToolbar.getHMargin()).below(mapTypeToolbar.getVMargin());
        buttonAlert.leftOf(width - zoomToolbar.getHMargin()).below(buttonClose, padding);

        int toolbarsWidth = mapTypeToolbar.getWidth() + optionsToolbar.getWidth() + margin + padding;
        int startX = (width - toolbarsWidth) / 2;

        Rectangle2D.Double oldBounds = mapTypeToolbar.getBounds();
        mapTypeToolbar.layoutHorizontal(startX + mapTypeToolbar.getWidth(), topY, false, padding);
        if(!mapTypeToolbar.getBounds().equals(oldBounds))
        {
            mapTypeToolbarBounds = null;
        }

        oldBounds = optionsToolbar.getBounds();
        optionsToolbar.layoutHorizontal(mapTypeToolbar.getRightX() + margin, topY, true, padding);
        optionsToolbar.visible = true;
        if(!optionsToolbar.getBounds().equals(oldBounds))
        {
            optionsToolbarBounds = null;
        }

        oldBounds = menuToolbar.getBounds();
        menuToolbar.layoutCenteredHorizontal((width/2), height - menuToolbar.height - menuToolbar.getVMargin(), true, padding);
        if(!menuToolbar.getBounds().equals(oldBounds))
        {
            menuToolbarBounds = null;
        }

        boolean showCaveLayers = buttonLayers.getToggled();
        if(showCaveLayers)
        {
            Rectangle2D.Double bounds = getMapTypeToolbarBounds();
            if(bounds!=null)
            {
                boolean alreadyVisible = sliderCaveLayer.isVisible() && Mouse.isButtonDown(0);
                sliderCaveLayer.setDrawButton(alreadyVisible || bounds.contains(mx, my));
            }
        }
        else
        {
            sliderCaveLayer.setDrawButton(false);
        }

        if(sliderCaveLayer.isVisible())
        {
            sliderCaveLayer.below(buttonLayers, 1).centerHorizontalOn(buttonLayers.getCenterX());
            final int slice = sliderCaveLayer.getValue();
            final int minY = Math.max((slice << 4), 0);
            final int maxY = ((slice + 1) << 4) - 1;
            sliderCaveLayer.setTooltip(Constants.getString("jm.fullscreen.map_cave_layers.button.tooltip", minY, maxY));
        }
    }

    @Nullable
    public Rectangle2D.Double getOptionsToolbarBounds()
    {
        if (optionsToolbar != null && optionsToolbar.isVisible())
        {
            Rectangle2D.Double unscaled = optionsToolbar.getBounds();
            optionsToolbarBounds = new Rectangle2D.Double(unscaled.x * scaleFactor,unscaled.y * scaleFactor,unscaled.width * scaleFactor,unscaled.height * scaleFactor);
        }
        return optionsToolbarBounds;
    }

    @Nullable
    public Rectangle2D.Double getMenuToolbarBounds()
    {
        if (menuToolbar != null && menuToolbar.isVisible())
        {
            Rectangle2D.Double unscaled = menuToolbar.getBounds();
            menuToolbarBounds = new Rectangle2D.Double(unscaled.x * scaleFactor,unscaled.y * scaleFactor,unscaled.width * scaleFactor,unscaled.height * scaleFactor);
        }
        return menuToolbarBounds;
    }

    @Nullable
    public Rectangle2D.Double getMapTypeToolbarBounds()
    {
        if (mapTypeToolbar != null && mapTypeToolbar.isVisible())
        {
            Rectangle2D.Double unscaled = mapTypeToolbar.getBounds();
            mapTypeToolbarBounds = new Rectangle2D.Double(unscaled.x * scaleFactor,unscaled.y * scaleFactor,unscaled.width * scaleFactor,unscaled.height * scaleFactor);
            mapTypeToolbarBounds.add(sliderCaveLayer.getBounds());
        }
        return mapTypeToolbarBounds;
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        try {
            if (chat != null && !chat.isHidden()) {
                chat.handleMouseInput();
                //return;
            }

            // Scale mouse position to Gui Scale coords
            mx = (Mouse.getEventX() * width) / mc.displayWidth;
            my = height - (Mouse.getEventY() * height) / mc.displayHeight - 1;

            if (Mouse.getEventButtonState()) {
                mouseClicked(mx, my, Mouse.getEventButton());
            } else {
                int wheel = Mouse.getEventDWheel();
                if (wheel > 0) {
                    zoomIn();
                } else {
                    if (wheel < 0) {
                        zoomOut();
                    } else {
                        mouseReleased(mx, my, Mouse.getEventButton());
                    }
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        try {
            if (chat != null && !chat.isHidden()) {
                chat.mouseClicked(mouseX, mouseY, mouseButton);
            }

            super.mouseClicked(mouseX, mouseY, mouseButton);

            // Invoke layer delegate
            Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), gridRenderer.getHeight() - Mouse.getEventY());
            layerDelegate.onMouseClicked(mc, gridRenderer, mousePosition, mouseButton, getMapFontScale());
        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int which)
    {
        try {
            super.mouseReleased(mouseX, mouseY, which);

            if (isMouseOverButton(mouseX, mouseY) || sliderCaveLayer.isVisible()) {
                return;
            }

            int blockSize = (int) Math.pow(2, fullMapProperties.zoomLevel.get());

            if (Mouse.isButtonDown(0) && !isScrolling) {
                isScrolling = true;
                msx = mx;
                msy = my;
            } else {
                if (!Mouse.isButtonDown(0) && isScrolling && !isMouseOverButton(msx, msy)) {
                    isScrolling = false;
                    int mouseDragX = (mx - msx) * Math.max(1, scaleFactor) / blockSize;
                    int mouseDragY = (my - msy) * Math.max(1, scaleFactor) / blockSize;
                    msx = mx;
                    msy = my;

                    try {
                        gridRenderer.move(-mouseDragX, -mouseDragY);
                        gridRenderer.updateTiles(state.getMapType(), state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, false, 0, 0);
                        gridRenderer.setZoom(fullMapProperties.zoomLevel.get());
                    } catch (Exception e) {
                        logger.error("Error moving grid: " + e);
                    }

                    setFollow(false);
                    refreshState();
                }
            }

            Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), gridRenderer.getHeight() - Mouse.getEventY());
            layerDelegate.onMouseMove(mc, gridRenderer, mousePosition, getMapFontScale(), isScrolling);

        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }

    public void toggleMapType()
    {
        updateMapType(state.toggleMapType());
    }

    /**
     * Set a new MapType and ensure buttons correctly show the state.
     * @param newType
     */
    private void updateMapType(MapType newType)
    {
        if(!newType.isAllowed())
        {
            newType = state.getMapType();
        }

        state.setMapType(newType);
        buttonDay.setToggled(newType.isDay(), false);
        buttonNight.setToggled(newType.isNight(), false);
        buttonTopo.setToggled(newType.isTopo(), false);
        buttonLayers.setToggled(newType.isUnderground(), false);
        if (newType.isUnderground())
        {
            sliderCaveLayer.setValue(newType.vSlice);
        }

        state.requireRefresh();
    }

    /**
     * Zoom in.
     */
    public void zoomIn()
    {
        if (fullMapProperties.zoomLevel.get() < state.maxZoom) {
            setZoom(fullMapProperties.zoomLevel.get() + 1);
        }
    }

    /**
     * Zoom out.
     */
    public void zoomOut()
    {
        if (fullMapProperties.zoomLevel.get() > state.minZoom) {
            setZoom(fullMapProperties.zoomLevel.get() - 1);
        }
    }

    private void setZoom(int zoom)
    {
        if (state.setZoom(zoom)) {
            buttonZoomOut.setEnabled(fullMapProperties.zoomLevel.get() > state.minZoom);
            buttonZoomIn.setEnabled(fullMapProperties.zoomLevel.get() < state.maxZoom);
            refreshState();
        }
    }

    /**
     * Toggle follow.
     */
    void toggleFollow()
    {
        boolean isFollow = !state.follow.get();
        setFollow(isFollow);
        if(isFollow)
        {
            if(mc.player!=null)
            {
                sliderCaveLayer.setValue(mc.player.chunkCoordY);
                if (state.getMapType().isUnderground())
                {
                    sliderCaveLayer.checkClickListeners();
                }
            }
        }
    }

    /**
     * Sets follow.
     *
     * @param follow the follow
     */
    void setFollow(Boolean follow)
    {
        state.follow.set(follow);
        if (follow)
        {
            state.resetMapType();
            refreshState();
        }
    }

    public void createWaypointAtMouse()
    {
        Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), gridRenderer.getHeight() - Mouse.getEventY());
        BlockPos blockPos = layerDelegate.getBlockPos(mc, gridRenderer, mousePosition);
        Waypoint waypoint = Waypoint.at(blockPos, Waypoint.Type.Normal, mc.player.dimension);
        UIManager.INSTANCE.openWaypointEditor(waypoint, true, this);
    }

    public void chatPositionAtMouse()
    {
        Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), gridRenderer.getHeight() - Mouse.getEventY());
        BlockPos blockPos = layerDelegate.getBlockPos(mc, gridRenderer, mousePosition);
        Waypoint waypoint = Waypoint.at(blockPos, Waypoint.Type.Normal, state.getDimension());
        openChat(waypoint.toChatString());
    }

    public boolean isChatOpen()
    {
        return chat != null && !chat.isHidden();
    }

    /**
     * Note: All the keybindings defined by JM are in KeyEventHandler.  This
     * method is just for mimicing "native" functionality in Minecraft.
     * @param c
     * @param key
     * @throws IOException
     */
    @Override
    public void keyTyped(char c, int key) throws IOException
    {
        if (isChatOpen())
        {
            chat.keyTyped(c, key);
            return;
        }

        if(mc.gameSettings.keyBindChat.getKeyCode()==key)
        {
            openChat("");
            return;
        }

        if(mc.gameSettings.keyBindCommand.getKeyCode()==key)
        {
            openChat("/");
            return;
        }

        // Escape
        if(Keyboard.KEY_ESCAPE == key)
        {
            UIManager.INSTANCE.closeAll();
            return;
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        if (chat != null) {
            chat.updateScreen();
        }
    }

    @Override
    public void drawBackground(int layer)
    {
        DrawUtil.drawRectangle(0, 0, width, height, bgColor, 1f);
    }

    /**
     * Draw map.
     */
    void drawMap()
    {
        final boolean refreshReady = isRefreshReady();
        final StatTimer timer = refreshReady ? drawMapTimerWithRefresh : drawMapTimer;
        final MapType mapType = state.getMapType();
        timer.start();

        try {
            sizeDisplay(false);

            int xOffset = 0;
            int yOffset = 0;

            if (isScrolling) {
                int blockSize = (int) Math.pow(2, fullMapProperties.zoomLevel.get());

                int mouseDragX = (mx - msx) * Math.max(1, scaleFactor) / blockSize;
                int mouseDragY = (my - msy) * Math.max(1, scaleFactor) / blockSize;

                xOffset = (mouseDragX * blockSize);
                yOffset = (mouseDragY * blockSize);

            } else {
                if (refreshReady) {
                    refreshState();
                } else {
                    gridRenderer.setContext(state.getWorldDir(), mapType);
                }
            }

            // Clear GL error queue of anything that happened before JM starts drawing, don't report them
            gridRenderer.clearGlErrors(false);

            gridRenderer.updateRotation(0);

            if (state.follow.get()) {
                gridRenderer.center(state.getWorldDir(), mapType, mc.player.posX, mc.player.posZ, fullMapProperties.zoomLevel.get());
            }
            gridRenderer.updateTiles(mapType, state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, false, 0, 0);
            gridRenderer.draw(1f, xOffset, yOffset, fullMapProperties.showGrid.get());
            gridRenderer.draw(state.getDrawSteps(), xOffset, yOffset, getMapFontScale(), 0);
            gridRenderer.draw(state.getDrawWaypointSteps(), xOffset, yOffset, getMapFontScale(), 0);

            if (fullMapProperties.showSelf.get()) {
                Point2D playerPixel = gridRenderer.getPixel(mc.player.posX, mc.player.posZ);
                if (playerPixel != null) {
                    boolean large = fullMapProperties.playerDisplay.get().isLarge();
                    TextureImpl bgTex = large ? TextureCache.getTexture(TextureCache.PlayerArrowBG_Large) : TextureCache.getTexture(TextureCache.PlayerArrowBG);
                    TextureImpl fgTex = large ? TextureCache.getTexture(TextureCache.PlayerArrow_Large) : TextureCache.getTexture(TextureCache.PlayerArrow);
                    DrawUtil.drawColoredEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, bgTex, 0xffffff, 1f, 1f, mc.player.rotationYawHead);

                    int playerColor = coreProperties.getColor(coreProperties.colorSelf);
                    DrawUtil.drawColoredEntity(playerPixel.getX() + xOffset, playerPixel.getY() + yOffset, fgTex, playerColor, 1f, 1f, mc.player.rotationYawHead);
                }
            }

            gridRenderer.draw(layerDelegate.getDrawSteps(), xOffset, yOffset, getMapFontScale(), 0);

            drawLogo();

            sizeDisplay(true);
        } finally {
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

    /**
     * Center on.
     *
     * @param waypoint the waypoint
     */
    public void centerOn(Waypoint waypoint)
    {
        if (waypoint.getDimensions().contains(mc.player.dimension)) {
            state.follow.set(false);
            state.requireRefresh();
            int x = waypoint.getX();
            int z = waypoint.getZ();

            gridRenderer.center(state.getWorldDir(), state.getMapType(), x, z, fullMapProperties.zoomLevel.get());

            if (!waypoint.isPersistent()) {
                addTempMarker(waypoint);
            }

            refreshState();
            updateScreen();
        }
    }

    /**
     * Add temp marker.
     *
     * @param waypoint the waypoint
     */
    public void addTempMarker(Waypoint waypoint)
    {
        try {
            BlockPos pos = waypoint.getBlockPos();

            PolygonOverlay polygonOverlay = new PolygonOverlay(Journeymap.MOD_ID, waypoint.getName(), mc.player.dimension,
                    new ShapeProperties().setStrokeColor(0x0000ff).setStrokeOpacity(1f).setStrokeWidth(1.5f),
                    new MapPolygon(pos.add(-1, 0, 2), pos.add(2, 0, 2), pos.add(2, 0, -1), pos.add(-1, 0, -1)));

            polygonOverlay.setActiveMapTypes(EnumSet.allOf(Context.MapType.class));
            polygonOverlay.setActiveUIs(EnumSet.of(Context.UI.Fullscreen));
            polygonOverlay.setLabel(waypoint.getName());
            tempOverlays.add(polygonOverlay);
            ClientAPI.INSTANCE.show(polygonOverlay);
        } catch (Throwable t) {
            Journeymap.getLogger().error("Error showing temp location marker: " + LogFormatter.toPartialString(t));
        }
    }

    /**
     * Get a snapshot of the player's biome, effective map state, etc.
     */
    void refreshState()
    {
        // Check player status
        EntityPlayerSP player = mc.player;
        if (player == null) {
            logger.warn("Could not get player");
            return;
        }

        StatTimer timer = StatTimer.get("Fullscreen.refreshState");
        timer.start();
        try
        {

            // Clear toolbar bounds
            menuToolbarBounds = null;
            optionsToolbarBounds = null;

            // Update the state first
            fullMapProperties = Journeymap.getClient().getFullMapProperties();
            state.refresh(mc, player, fullMapProperties);
            MapType mapType = state.getMapType();

            gridRenderer.setContext(state.getWorldDir(), mapType);

            // Center core renderer
            if (state.follow.get())
            {
                gridRenderer.center(state.getWorldDir(), mapType, mc.player.posX, mc.player.posZ, fullMapProperties.zoomLevel.get());
            }
            else
            {
                gridRenderer.setZoom(fullMapProperties.zoomLevel.get());
            }

            // Update tiles
            gridRenderer.updateTiles(mapType, state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, true, 0, 0);

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
        }
        finally
        {
            timer.stop();
        }

        // Trigger a mouse move event in the layer delegate so draw steps can update if needed
        Point2D.Double mousePosition = new Point2D.Double(Mouse.getEventX(), gridRenderer.getHeight() - Mouse.getEventY());
        layerDelegate.onMouseMove(mc, gridRenderer, mousePosition, getMapFontScale(), isScrolling);
    }

    /**
     * Open chat.
     *
     * @param defaultText the default text
     */
    public void openChat(String defaultText)
    {
        if (chat != null) {
            chat.setText(defaultText);
            chat.setHidden(false);
        } else {
            chat = new MapChat(defaultText, false);
            chat.setWorldAndResolution(mc, width, height);
        }
    }

    @Override
    public void close()
    {
        for (Overlay temp : tempOverlays) {
            ClientAPI.INSTANCE.remove(temp);
        }

        gridRenderer.updateUIState(false);

        if (chat != null) {
            chat.close();
        }
    }

    // @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Is refresh ready boolean.
     *
     * @return the boolean
     */
    boolean isRefreshReady()
    {
        if (isScrolling) {
            return false;
        } else {
            return state.shouldRefresh(super.mc, fullMapProperties) || gridRenderer.hasUnloadedTile();
        }
    }

    public int getScreenScaleFactor()
    {
        return scaleFactor;
    }

    /**
     * Move canvas.
     *
     * @param deltaBlockX the delta block x
     * @param deltaBlockz the delta blockz
     */
    public void moveCanvas(int deltaBlockX, int deltaBlockz)
    {
        refreshState();
        gridRenderer.move(deltaBlockX, deltaBlockz);
        gridRenderer.updateTiles(state.getMapType(), state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, true, 0, 0);
        ClientAPI.INSTANCE.flagOverlaysForRerender();
        setFollow(false);
    }

    public void showCaveLayers()
    {
        if(!state.isUnderground())
        {
            updateMapType(MapType.underground(3, state.getDimension()));
        }
    }

    @Override
    protected void drawLogo()
    {
        if (logo.isDefunct()) {
            logo = TextureCache.getTexture(TextureCache.Logo);
        }
        DrawUtil.sizeDisplay(mc.displayWidth, mc.displayHeight);
        Theme.Container.Toolbar toolbar = ThemeLoader.getCurrentTheme().container.toolbar;
        float scale = scaleFactor*2;

        DrawUtil.sizeDisplay(width, height);
        DrawUtil.drawImage(logo, toolbar.horizontal.margin, toolbar.vertical.margin, false, 1f/scale, 0);
    }

    @Override
    public final boolean doesGuiPauseGame()
    {
        return false;
    }

    /**
     * Set theme by name
     *
     * @param name the name
     */
    public void setTheme(String name)
    {
        try {
            // TODO: Why force these properties?
            MiniMapProperties mmp = Journeymap.getClient().getMiniMapProperties(Journeymap.getClient().getActiveMinimapId());
            mmp.shape.set(Shape.Rectangle);
            mmp.sizePercent.set(20);
            mmp.save();
            Theme theme = ThemeLoader.getThemeByName(name);
            ThemeLoader.setCurrentTheme(theme);
            UIManager.INSTANCE.getMiniMap().reset();
            ChatLog.announceI18N("jm.common.ui_theme_applied");
            UIManager.INSTANCE.closeAll();
        } catch (Exception e) {
            Journeymap.getLogger().error("Could not load Theme: " + LogFormatter.toString(e));
        }
    }

    @Override
    public void setCompletions(String... newCompletions)
    {
        chat.setCompletions(newCompletions);
    }
}

