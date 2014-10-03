/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.dialog;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.forgehandler.KeyEventHandler;
import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.task.MapPlayerTask;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.*;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import net.techbrew.journeymap.ui.option.CategorySlot;
import net.techbrew.journeymap.ui.option.OptionSlotFactory;
import net.techbrew.journeymap.ui.option.SlotMetadata;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.*;

/**
 * Master options UI
 */
public class OptionsManager extends JmUI
{
    protected Config.Category[] initialCategories;
    protected CheckBox minimapPreviewButton;
    protected Button buttonClose;
    protected ScrollListPane optionsListPane;
    protected Map<Config.Category, List<SlotMetadata>> toolbars;
    protected EnumSet<Config.Category> changedCategories = EnumSet.noneOf(Config.Category.class);

    public OptionsManager()
    {
        this(null, null);
    }

    public OptionsManager(Class<? extends JmUI> returnClass)
    {
        this(returnClass, null);
    }

    public OptionsManager(Class<? extends JmUI> returnClass, Config.Category... initialCategories)
    {
        super("JourneyMap " + Constants.getString("jm.common.options"), returnClass);
        this.initialCategories = initialCategories;
    }

    @Override
    public void initGui()
    {
        buttonList.clear();

        if (optionsListPane == null)
        {
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
                        }
                    }
                }
            }
            optionsListPane.updateSlots();
        }
        else
        {
            optionsListPane.func_148122_a(width, height, headerHeight, this.height - 30);
            optionsListPane.updateSlots();
        }

        buttonClose = new Button(0, Constants.getString("jm.common.close"));
        buttonClose.setWidth(150);

        if (minimapPreviewButton == null)
        {
            String name = Constants.getString("jm.minimap.preview");
            String tooltip = Constants.getString("jm.minimap.preview.tooltip");
            minimapPreviewButton = new CheckBox(0, name, false);
            minimapPreviewButton.setTooltip(tooltip);
        }

        buttonList.add(buttonClose);
        buttonList.add(buttonClose);
        buttonList.add(minimapPreviewButton);

        ButtonList bottomRow = new ButtonList(buttonList);
        bottomRow.equalizeWidths(getFontRenderer());

        bottomRow.layoutCenteredHorizontal(width / 2, height - 25, true, 4);
        minimapPreviewButton.setY(minimapPreviewButton.getY() + 4);
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
        String[] lastTooltip = optionsListPane.lastTooltip;
        long lastTooltipTime = optionsListPane.lastTooltipTime;
        optionsListPane.lastTooltip = null;
        optionsListPane.drawScreen(x, y, par3);
        super.drawScreen(x, y, par3);

        for (List<SlotMetadata> toolbar : getToolbars().values())
        {
            for (SlotMetadata slotMetadata : toolbar)
            {
                slotMetadata.getButton().secondaryDrawButton();
            }
        }

        if (minimapPreviewButton.getToggled())
        {
            RenderHelper.enableStandardItemLighting();
            UIManager.getInstance().getMiniMap().drawMap();
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
            SlotMetadata slotMetadata = optionsListPane.getLastPressed();
            if (slotMetadata != null)
            {
                if (slotMetadata.getName().equals(Constants.getString("jm.common.ui_theme")))
                {
                    ThemeFileHandler.getCurrentTheme(true);
                }
            }

            CategorySlot categorySlot = (CategorySlot) optionsListPane.getLastPressedParentSlot();
            if (categorySlot != null)
            {
                Config.Category category = categorySlot.getCategory();
                changedCategories.add(category);

                if (category.equals(Config.Category.MiniMap))
                {
                    UIManager.getInstance().getMiniMap().updateDisplayVars(true);
                }
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseEvent)
    {
        super.mouseMovedOrUp(mouseX, mouseY, mouseEvent);
        optionsListPane.mouseReleased(mouseX, mouseY, mouseEvent);
    }

    @Override
    protected void mouseClickMove(int p_146273_1_, int p_146273_2_, int p_146273_3_, long p_146273_4_)
    {
        super.mouseClickMove(p_146273_1_, p_146273_2_, p_146273_3_, p_146273_4_);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button == buttonClose)
        {
            closeAndReturn();
            return;
        }

        if (button == minimapPreviewButton)
        {
            //minimapPreviewButton.toggle();
        }
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        super.keyTyped(c, i);

        boolean optionUpdated = optionsListPane.keyTyped(c, i);
        if (optionUpdated && minimapPreviewButton.getToggled())
        {
            UIManager.getInstance().getMiniMap().updateDisplayVars(true);
        }

        if (minimapPreviewButton.getToggled())
        {
            boolean pressed = KeyEventHandler.onKeypress(true);
            if (pressed)
            {
                refreshMinimapOptions();
            }
        }
    }

    protected void refreshMinimapOptions()
    {
        for (ScrollListPane.ISlot slot : optionsListPane.getRootSlots())
        {
            if (slot instanceof CategorySlot)
            {
                if (((CategorySlot) slot).getCategory() == Config.Category.MiniMap)
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
        for (Config.Category category : changedCategories)
        {
            switch (category)
            {
                case MiniMap:
                {
                    UIManager.getInstance().getMiniMap().updateDisplayVars(true);
                    break;
                }
                case FullMap:
                {
                    Fullscreen.reset();
                    ThemeFileHandler.getCurrentTheme(true);
                    UIManager.getInstance().getMiniMap().updateDisplayVars(true);
                    break;
                }
                case WebMap:
                {
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
                    MapPlayerTask.forceNearbyRemap();
                    break;
                }
                case Advanced:
                {
                    DataCache.instance().purge();
                    break;
                }
            }
        }

        if (returnClass == null)
        {
            UIManager.getInstance().openMap();
        }
        else
        {
            UIManager.getInstance().open(returnClass);
        }
    }

    Map<Config.Category, List<SlotMetadata>> getToolbars()
    {
        if (toolbars == null)
        {
            toolbars = Collections.EMPTY_MAP;
//            String name = Constants.getString("jm.minimap.preview");
//            String tooltip = Constants.getString("jm.minimap.preview.tooltip");
//            minimapPreviewButton = new MinimapPreviewButton(name);
//            SlotMetadata toolbarSlotMetadata = new SlotMetadata(minimapPreviewButton, name, tooltip);
//
//            this.toolbars = new HashMap<Config.Category, List<SlotMetadata>>();
//            toolbars.put(Config.Category.MiniMap, Arrays.asList(toolbarSlotMetadata));
        }
        return toolbars;
    }
}
