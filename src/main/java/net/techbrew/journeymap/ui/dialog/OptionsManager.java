/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.dialog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.forgehandler.KeyEventHandler;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.properties.config.Config;
import net.techbrew.journeymap.task.MapPlayerTask;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.*;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import net.techbrew.journeymap.ui.option.CategorySlot;
import net.techbrew.journeymap.ui.option.OptionSlotFactory;
import net.techbrew.journeymap.ui.option.SlotMetadata;
import net.techbrew.journeymap.waypoint.WaypointStore;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Master options UI
 * // TODO: Black image for cave map
 */
public class OptionsManager extends JmUI
{
    protected final int inGameMinimapId;
    protected Config.Category[] initialCategories;
    protected CheckBox minimap1PreviewButton;
    protected CheckBox minimap2PreviewButton;
    protected Button minimap1KeysButton, minimap2KeysButton;
    protected Button fullscreenKeysButton;
    protected Button buttonClose;
    protected Button renderStatsButton;
    protected SlotMetadata renderStatsSlotMetadata;
    protected CategorySlot cartographyCategorySlot;
    protected ScrollListPane optionsListPane;
    protected Map<Config.Category, List<SlotMetadata>> toolbars;
    protected EnumSet<Config.Category> changedCategories = EnumSet.noneOf(Config.Category.class);
    protected boolean forceMinimapUpdate;

    public OptionsManager()
    {
        this(null);
    }

    public OptionsManager(JmUI returnDisplay)
    {
        this(returnDisplay, null, null);
    }

    public OptionsManager(JmUI returnDisplay, Config.Category... initialCategories)
    {
        super(String.format("JourneyMap %s %s", JourneyMap.JM_VERSION, Constants.getString("jm.common.options")), returnDisplay);
        this.initialCategories = initialCategories;
        this.inGameMinimapId = JourneyMap.getActiveMinimapId();
    }

    @Override
    public void initGui()
    {
        try
        {
            buttonList.clear();

            if (minimap1PreviewButton == null)
            {
                String name = String.format("%s %s", Constants.getString("jm.minimap.preview"), "1");
                String tooltip = Constants.getString("jm.minimap.preview.tooltip");
                minimap1PreviewButton = new CheckBox(name, false);
                minimap1PreviewButton.setTooltip(tooltip);
            }

            if (minimap2PreviewButton == null)
            {
                String name = String.format("%s %s", Constants.getString("jm.minimap.preview"), "2");
                String tooltip = Constants.getString("jm.minimap.preview.tooltip");
                minimap2PreviewButton = new CheckBox(name, false);
                minimap2PreviewButton.setTooltip(tooltip);
            }

            if (minimap1KeysButton == null)
            {
                String name = Constants.getString("jm.common.hotkeys");
                String tooltip = Constants.getString("jm.common.hotkeys.tooltip");
                minimap1KeysButton = new Button(name);
                minimap1KeysButton.setTooltip(tooltip);
                minimap1KeysButton.setDrawBackground(false);
            }

            if (minimap2KeysButton == null)
            {
                String name = Constants.getString("jm.common.hotkeys");
                String tooltip = Constants.getString("jm.common.hotkeys.tooltip");
                minimap2KeysButton = new Button(name);
                minimap2KeysButton.setTooltip(tooltip);
                minimap2KeysButton.setDrawBackground(false);
            }

            if (fullscreenKeysButton == null)
            {
                String name = Constants.getString("jm.common.hotkeys");
                String tooltip = Constants.getString("jm.common.hotkeys.tooltip");
                fullscreenKeysButton = new Button(name);
                fullscreenKeysButton.setTooltip(tooltip);
                fullscreenKeysButton.setDrawBackground(false);
            }

            if (renderStatsButton == null)
            {
                renderStatsButton = new LabelButton(150, "jm.common.renderstats", 0, 0, 0);
                renderStatsButton.setEnabled(false);
            }

            if (optionsListPane == null)
            {
                List<ScrollListPane.ISlot> categorySlots = new ArrayList<ScrollListPane.ISlot>();
                optionsListPane = new ScrollListPane(this, mc, this.width, this.height, this.headerHeight, this.height - 30, 20);
                optionsListPane.setSlots(OptionSlotFactory.getSlots(getToolbars()));
                if (initialCategories != null)
                {
                    for (Config.Category initialCategory : initialCategories)
                    {
                        for (ScrollListPane.ISlot slot : optionsListPane.getRootSlots())
                        {
                            if (slot instanceof CategorySlot && ((CategorySlot) slot).getCategory() == initialCategory)
                            {
                                ((CategorySlot) slot).setSelected(true);
                                categorySlots.add(slot);
                            }
                        }
                    }
                }

                // Add Toolbar buttons
                for (ScrollListPane.ISlot rootSlot : optionsListPane.getRootSlots())
                {
                    if (rootSlot instanceof CategorySlot)
                    {
                        CategorySlot categorySlot = (CategorySlot) rootSlot;
                        Config.Category category = categorySlot.getCategory();

                        // Reset button
                        ResetButton resetButton = new ResetButton(category);
                        SlotMetadata resetSlotMetadata = new SlotMetadata(resetButton, 1);

                        switch (category)
                        {
                            case MiniMap1:
                            {
                                categorySlot.getAllChildMetadata().add(new SlotMetadata(minimap1PreviewButton, 3));
                                categorySlot.getAllChildMetadata().add(new SlotMetadata(minimap1KeysButton, 2));
                                categorySlot.getAllChildMetadata().add(resetSlotMetadata);
                                break;
                            }
                            case MiniMap2:
                            {
                                categorySlot.getAllChildMetadata().add(new SlotMetadata(minimap2PreviewButton, 3));
                                categorySlot.getAllChildMetadata().add(new SlotMetadata(minimap2KeysButton, 2));
                                categorySlot.getAllChildMetadata().add(resetSlotMetadata);
                                break;
                            }
                            case FullMap:
                            {
                                categorySlot.getAllChildMetadata().add(new SlotMetadata(fullscreenKeysButton, 2));
                                categorySlot.getAllChildMetadata().add(resetSlotMetadata);
                                break;
                            }
                            case Cartography:
                            {
                                cartographyCategorySlot = categorySlot;
                                renderStatsSlotMetadata = new SlotMetadata(renderStatsButton,
                                        Constants.getString("jm.common.renderstats.title"),
                                        Constants.getString("jm.common.renderstats.tooltip"), 2);
                                categorySlot.getAllChildMetadata().add(renderStatsSlotMetadata);
                                categorySlot.getAllChildMetadata().add(resetSlotMetadata);
                                break;
                            }
                            default:
                            {
                                categorySlot.getAllChildMetadata().add(resetSlotMetadata);
                            }
                        }
                    }
                }


                // Update slots
                optionsListPane.updateSlots();

                if (!categorySlots.isEmpty())
                {
                    // Scroll to first
                    optionsListPane.scrollTo(categorySlots.get(0));
                }
            }
            else
            {
                optionsListPane.func_148122_a(width, height, headerHeight, this.height - 30);
                optionsListPane.updateSlots();
            }

            buttonClose = new Button(Constants.getString("jm.common.close"));
            buttonClose.setWidth(150);

            buttonList.add(buttonClose);

            ButtonList bottomRow = new ButtonList(buttonList);
            //bottomRow.equalizeWidths(getFontRenderer());

            bottomRow.layoutCenteredHorizontal(width / 2, height - 25, true, 4);

        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error in OptionsManager.initGui(): " + t, t);
        }
    }

    @Override
    protected void layoutButtons()
    {
        if (buttonList.isEmpty())
        {
            initGui();
        }
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        try
        {
            if (forceMinimapUpdate)
            {
                // Minimap buttons:  Force minimap toggles
                if (minimap1PreviewButton.isActive())
                {
                    UIManager.getInstance().switchMiniMapPreset(1);
                }
                else if (minimap2PreviewButton.isActive())
                {
                    UIManager.getInstance().switchMiniMapPreset(2);
                }
            }

            renderStatsButton.displayString = Constants.getString("jm.common.renderstats",
                    MapPlayerTask.getLastChunkStats(),
                    MapPlayerTask.getLastChunkStatsTime(),
                    new DecimalFormat("##.#").format(MapPlayerTask.getLastChunkStatsAvg()));
            renderStatsButton.setWidth(cartographyCategorySlot.getCurrentColumnWidth());

            String[] lastTooltip = optionsListPane.lastTooltip;
            long lastTooltipTime = optionsListPane.lastTooltipTime;
            optionsListPane.lastTooltip = null;
            optionsListPane.drawScreen(x, y, par3);

            super.drawScreen(x, y, par3);

            if (previewMiniMap())
            {
                UIManager.getInstance().getMiniMap().drawMap(true);
                RenderHelper.disableStandardItemLighting();
            }

            if (optionsListPane.lastTooltip != null)
            {
                if (Arrays.equals(optionsListPane.lastTooltip, lastTooltip))
                {
                    optionsListPane.lastTooltipTime = lastTooltipTime;
                    if (System.currentTimeMillis() - optionsListPane.lastTooltipTime > optionsListPane.hoverDelay)
                    {
                        Button button = optionsListPane.lastTooltipMetadata.getButton();
                        drawHoveringText(optionsListPane.lastTooltip, x, button.getBottomY() + 15);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error in OptionsManager.drawScreen(): " + t, t);
        }
    }

    @Override
    public void drawBackground(int layer)
    {
        //drawDefaultBackground();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseEvent)
    {
        super.mouseClicked(mouseX, mouseY, mouseEvent);
        boolean pressed = optionsListPane.mousePressed(mouseX, mouseY, mouseEvent);
        if (pressed)
        {
            checkPressedButton();
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseEvent)
    {
        super.mouseMovedOrUp(mouseX, mouseY, mouseEvent);
        optionsListPane.mouseReleased(mouseX, mouseY, mouseEvent);

    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int lastButtonClicked, long timeSinceMouseClick)
    {
        super.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        checkPressedButton();
    }

    /**
     * Check the pressed button in the scroll pane and determine if something needs to be updated or refreshed
     */
    protected void checkPressedButton()
    {
        SlotMetadata slotMetadata = optionsListPane.getLastPressed();
        if (slotMetadata != null)
        {
            // If it's a reset button, reset that action
            if (slotMetadata.getButton() instanceof ResetButton)
            {
                resetOptions(((ResetButton) slotMetadata.getButton()).category);
            }

            // Theme button: Force update
            if (slotMetadata.getName().equals(Constants.getString("jm.common.ui_theme")))
            {
                ThemeFileHandler.getCurrentTheme(true);
                if (previewMiniMap())
                {
                    UIManager.getInstance().getMiniMap().updateDisplayVars(true);
                }
            }

            // Minimap buttons:  Force minimap toggles
            if (slotMetadata.getButton() == minimap1PreviewButton)
            {
                minimap2PreviewButton.setToggled(false);
                UIManager.getInstance().switchMiniMapPreset(1);
                UIManager.getInstance().getMiniMap().resetInitTime();
            }

            if (slotMetadata.getButton() == minimap2PreviewButton)
            {
                minimap1PreviewButton.setToggled(false);
                UIManager.getInstance().switchMiniMapPreset(2);
                UIManager.getInstance().getMiniMap().resetInitTime();
            }

            if (slotMetadata.getButton() == minimap1KeysButton || slotMetadata.getButton() == minimap2KeysButton)
            {
                optionsListPane.resetLastPressed();
                UIManager.getInstance().openMiniMapHotkeyHelp(this);
                return;
            }

            if (slotMetadata.getButton() == fullscreenKeysButton)
            {
                optionsListPane.resetLastPressed();
                UIManager.getInstance().openMapHotkeyHelp(this);
                return;
            }
        }

        CategorySlot categorySlot = (CategorySlot) optionsListPane.getLastPressedParentSlot();
        if (categorySlot != null)
        {
            // Track the category of the button so resets can happen when OptionsManager is closed
            Config.Category category = categorySlot.getCategory();
            changedCategories.add(category);

            // If the button is MiniMap1-related, force it to update
            if (category == Config.Category.MiniMap1 || category == Config.Category.MiniMap2)
            {
                refreshMinimapOptions();
                DataCache.instance().resetRadarCaches();
                UIManager.getInstance().getMiniMap().updateDisplayVars(true);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button == buttonClose)
        {
            closeAndReturn();
            return;
        }

        if (button == minimap1PreviewButton)
        {
            minimap2PreviewButton.setToggled(false);
            UIManager.getInstance().switchMiniMapPreset(1);
        }

        if (button == minimap2PreviewButton)
        {
            minimap1PreviewButton.setToggled(false);
            UIManager.getInstance().switchMiniMapPreset(2);
        }
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        switch (i)
        {
            case Keyboard.KEY_ESCAPE:
            {
                if (previewMiniMap())
                {
                    minimap1PreviewButton.setToggled(false);
                    minimap2PreviewButton.setToggled(false);
                }
                else
                {
                    closeAndReturn();
                }
                break;
            }
        }

        boolean optionUpdated = optionsListPane.keyTyped(c, i);
        if (optionUpdated && previewMiniMap())
        {
            UIManager.getInstance().getMiniMap().updateDisplayVars(true);
        }

        // Check for any minimap-related keypresses
        if (previewMiniMap())
        {
            boolean pressed = KeyEventHandler.onKeypress(true);
            if (pressed)
            {
                refreshMinimapOptions();
            }
        }
    }

    protected void resetOptions(Config.Category category)
    {
        for (ScrollListPane.ISlot slot : optionsListPane.getRootSlots())
        {
            if (slot instanceof CategorySlot)
            {
                if (category.equals(((CategorySlot) slot).getCategory()))
                {
                    for (SlotMetadata slotMetadata : ((CategorySlot) slot).getAllChildMetadata())
                    {
                        slotMetadata.resetToDefaultValue();
                        slotMetadata.getButton().refresh(); // TODO move into reset
                    }
                    break;
                }
            }
        }
    }

    protected boolean previewMiniMap()
    {
        return minimap1PreviewButton.getToggled() || minimap2PreviewButton.getToggled();
    }

    protected void refreshMinimapOptions()
    {
        EnumSet cats = EnumSet.of(Config.Category.MiniMap1, Config.Category.MiniMap2);
        for (ScrollListPane.ISlot slot : optionsListPane.getRootSlots())
        {
            if (slot instanceof CategorySlot)
            {
                if (cats.contains(((CategorySlot) slot).getCategory()))
                {
                    for (SlotMetadata slotMetadata : ((CategorySlot) slot).getAllChildMetadata())
                    {
                        slotMetadata.getButton().refresh();
                    }
                }
            }
        }
    }

    @Override
    protected void closeAndReturn()
    {
        JourneyMap.getCoreProperties().optionsManagerUsed.set(true);

        // Just in case a property changed but wasn't saved.
        JourneyMap.getCoreProperties().save();
        JourneyMap.getWebMapProperties().save();
        JourneyMap.getFullMapProperties().save();
        JourneyMap.getMiniMapProperties1().save();
        JourneyMap.getMiniMapProperties2().save();
        JourneyMap.getWaypointProperties().save();

        // Ensure minimap is back to the one used before this opened
        UIManager.getInstance().getMiniMap().setMiniMapProperties(JourneyMap.getMiniMapProperties(this.inGameMinimapId));

        for (Config.Category category : changedCategories)
        {
            switch (category)
            {
                case MiniMap1:
                {
                    DataCache.instance().resetRadarCaches();
                    UIManager.getInstance().getMiniMap().reset();
                    break;
                }
                case MiniMap2:
                {
                    DataCache.instance().resetRadarCaches();
                    UIManager.getInstance().getMiniMap().reset();
                    break;
                }
                case FullMap:
                {
                    DataCache.instance().resetRadarCaches();
                    Fullscreen.reset();
                    ThemeFileHandler.getCurrentTheme(true);
                    break;
                }
                case WebMap:
                {
                    DataCache.instance().resetRadarCaches();
                    JourneyMap.getInstance().toggleWebserver(JourneyMap.getWebMapProperties().enabled.get(), true);
                    break;
                }
                case Waypoint:
                {
                    WaypointStore.instance().reset();
                }
                case WaypointBeacon:
                {
                    break;
                }
                case Cartography:
                {
                    JourneyMap.getInstance().softReset();
                    MapPlayerTask.forceNearbyRemap();
                    break;
                }
                case Advanced:
                {
                    JourneyMap.getInstance().softReset();
                    JourneyMap.getInstance().toggleWebserver(JourneyMap.getWebMapProperties().enabled.get(), false);
                    break;
                }
            }
        }

        // Ensure MiniMap is reset, regardless
        UIManager.getInstance().getMiniMap().updateDisplayVars(true);

        if (returnDisplay == null)
        {
            UIManager.getInstance().openFullscreenMap();
        }
        else
        {
            UIManager.getInstance().open(returnDisplay);
        }
    }

    Map<Config.Category, List<SlotMetadata>> getToolbars()
    {
        if (toolbars == null)
        {
            this.toolbars = new HashMap<Config.Category, List<SlotMetadata>>();
            for (Config.Category category : Config.Category.values())
            {
//                String name = Constants.getString("jm.config.reset");
//                String tooltip = Constants.getString("jm.config.reset.tooltip");
//                SlotMetadata toolbarSlotMetadata = new SlotMetadata(new ResetButton(), name, tooltip);
//                toolbars.put(category, Arrays.asList(toolbarSlotMetadata));
            }
        }
        return toolbars;
    }

    public static class ResetButton extends Button
    {
        public final Config.Category category;

        public ResetButton(Config.Category category)
        {
            super(Constants.getString("jm.config.reset"));
            this.category = category;
            setTooltip(Constants.getString("jm.config.reset.tooltip"));
            setDrawBackground(false);
            packedFGColour = Color.red.getRGB();
        }
    }

    public static class LabelButton extends Button
    {
        public LabelButton(int width, String key, Object... labelArgs)
        {
            super(Constants.getString(key, labelArgs));
            setTooltip(Constants.getString(key + ".tooltip"));
            setDrawBackground(false);
            setDrawFrame(false);
            setEnabled(false);
            packedFGColour = Color.lightGray.getRGB();
            this.width = width;
        }

        @Override
        public int getFitWidth(FontRenderer fr)
        {
            return width;
        }

        @Override
        public void fitWidth(FontRenderer fr)
        {
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
        {
            int labelWidth = super.getFitWidth(minecraft.fontRenderer);
            this.drawString(minecraft.fontRenderer, this.displayString, this.xPosition + this.width - labelWidth, this.yPosition + (this.height - 8) / 2, packedFGColour);
        }
    }
}
