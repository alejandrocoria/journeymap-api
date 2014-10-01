/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.dialog;

import net.minecraft.client.gui.GuiButton;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.JmUI;
import net.techbrew.journeymap.ui.component.ScrollListPane;
import net.techbrew.journeymap.ui.option.OptionSlotFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Master options UI
 */
public class MasterOptions2 extends JmUI
{
    Button buttonClose;
    ScrollListPane optionsListPane;
    List<ScrollListPane.ISlot> optionSlots;

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
            optionsListPane.setSlots(OptionSlotFactory.getSlots());
        }
        else
        {
            optionsListPane.func_148122_a(width, height, headerHeight, this.height - 30);
            optionsListPane.updateSlots();
        }

        buttonClose = new Button(0, Constants.getString("jm.common.close"));
        buttonClose.setWidth(150);
        buttonList.add(buttonClose);
    }

    @Override
    protected void layoutButtons()
    {
        if (buttonList.isEmpty())
        {
            initGui();
        }

        final int hgap = 4;
        final int vgap = (getFontRenderer().FONT_HEIGHT * 2) + 4;


        // Close
        buttonClose.centerHorizontalOn(width / 2).setY(height - 25);
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
        optionsListPane.mousePressed(mouseX, mouseY, mouseEvent);
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
            JourneyMap.getInstance().softReset();
            UIManager.getInstance().openMap();
            return;
        }
    }

    @Override
    protected void closeAndReturn()
    {
        if (returnClass == null)
        {
            UIManager.getInstance().openMap();
        }
        else
        {
            UIManager.getInstance().open(returnClass);
        }
    }
}
