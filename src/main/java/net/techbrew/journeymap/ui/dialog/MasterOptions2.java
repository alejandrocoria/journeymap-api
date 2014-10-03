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
public class MasterOptions2 extends JmUI
{
    Button buttonClose;
    ScrollListPane optionsListPane;
    List<ScrollListPane.ISlot> optionSlots;
    Map<Config.Category, List<SlotMetadata>> toolbars;
    EnumSet<Config.Category> changedCategories = EnumSet.noneOf(Config.Category.class);

    MinimapPreviewButton minimapPreviewButton;

    public MasterOptions2()
    {
        super("JourneyMap " + Constants.getString("jm.common.options"));
    }

    @Override
    public void initGui()
    {
        buttonList.clear();

        if (optionsListPane == null)
        {
            optionsListPane = new ScrollListPane(this, mc, this.width, this.height, this.headerHeight, this.height - 30, 20);
            optionsListPane.setSlots(OptionSlotFactory.getSlots(getToolbars()));
        }
        else
        {
            optionsListPane.func_148122_a(width, height, headerHeight, this.height - 30);
            optionsListPane.updateSlots();
        }

        buttonClose = new Button(0, Constants.getString("jm.common.close"));
        buttonClose.setWidth(150);

        String name = Constants.getString("jm.minimap.preview");
        String tooltip = Constants.getString("jm.minimap.preview.tooltip");
        minimapPreviewButton = new MinimapPreviewButton(name);
        minimapPreviewButton.setTooltip(tooltip);

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

        for (List<SlotMetadata> toolbar : getToolbars().values())
        {
            for (SlotMetadata slotMetadata : toolbar)
            {
                slotMetadata.getButton().secondaryDrawButton();
            }
        }

        minimapPreviewButton.secondaryDrawButton();
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
            CategorySlot categorySlot = (CategorySlot) optionsListPane.getLastPressedParentSlot();
            if (categorySlot != null)
            {
                Config.Category category = categorySlot.getCategory();
                changedCategories.add(category);
            }

            SlotMetadata slotMetadata = optionsListPane.getLastPressed();
            if (slotMetadata != null)
            {
                Button button = slotMetadata.getButton();
//                if(minimapPreviewButton == button)
//                {
//                    minimapPreviewButton.toggle();
//                }
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
        optionsListPane.keyTyped(c, i);
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

    static class MinimapPreviewButton extends CheckBox
    {
        public MinimapPreviewButton(String label)
        {
            super(0, label, false);
        }

        /**
         * Called at the end of drawScreen
         */
        @Override
        public void secondaryDrawButton()
        {
            if (this.toggled)
            {
                RenderHelper.enableStandardItemLighting();
                UIManager.getInstance().getMiniMap().drawMap();
                RenderHelper.enableStandardItemLighting();
            }
        }
    }
}
