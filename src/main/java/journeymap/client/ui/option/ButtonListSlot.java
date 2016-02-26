/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.option;

import com.google.common.base.Strings;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.ScrollListPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mark on 9/29/2014.
 */
public class ButtonListSlot implements ScrollListPane.ISlot, Comparable<ButtonListSlot>
{
    static int hgap = 8;
    Minecraft mc = ForgeHelper.INSTANCE.getClient();
    FontRenderer fontRenderer = ForgeHelper.INSTANCE.getFontRenderer();
    ButtonList buttons = new ButtonList();
    HashMap<Button, SlotMetadata> buttonOptionMetadata = new HashMap<Button, SlotMetadata>();
    CategorySlot parent;
    SlotMetadata lastPressed = null;
    SlotMetadata currentToolTip = null;
    Integer colorToolbarBgStart = new Color(0, 0, 100).getRGB();
    Integer colorToolbarBgEnd = new Color(0, 0, 100).getRGB();

    public ButtonListSlot(CategorySlot parent)
    {
        this.parent = parent;
    }

    public ButtonListSlot add(SlotMetadata slotMetadata)
    {
        buttons.add(slotMetadata.getButton());
        buttonOptionMetadata.put(slotMetadata.getButton(), slotMetadata);
        return this;
    }

    public ButtonListSlot addAll(Collection<SlotMetadata> slotMetadataCollection)
    {
        for (SlotMetadata slotMetadata : slotMetadataCollection)
        {
            add(slotMetadata);
        }
        return this;
    }

    public ButtonListSlot merge(ButtonListSlot other)
    {
        for (SlotMetadata otherSlot : other.buttonOptionMetadata.values())
        {
            add(otherSlot);
        }
        return this;
    }

    public void clear()
    {
        buttons.clear();
        buttonOptionMetadata.clear();
    }

    @Override
    public Collection<SlotMetadata> getMetadata()
    {
        return buttonOptionMetadata.values();
    }

    @Override
    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
        // ?
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        int margin = 0;
        if (parent.getCurrentColumnWidth() > 0)
        {
            int cols = listWidth / parent.currentColumnWidth;
            margin = (listWidth - ((hgap * cols - 1) + cols * parent.getCurrentColumnWidth())) / 2;

            x += margin;
            listWidth -= (margin * 2);
        }
        SlotMetadata tooltipMetadata = null;
        if (buttons.size() > 0)
        {
            buttons.setHeights(slotHeight);

            if (buttonOptionMetadata.get(buttons.get(0)).isToolbar())
            {
                buttons.fitWidths(fontRenderer);
                buttons.layoutHorizontal(x + listWidth - hgap, y, false, hgap);
                DrawUtil.drawGradientRect(x, y, listWidth, slotHeight, colorToolbarBgStart, .15f, colorToolbarBgEnd, .6f);
            }
            else
            {
                buttons.setWidths(parent.currentColumnWidth);
                buttons.layoutHorizontal(x, y, true, hgap);
            }

            for (Button button : buttons)
            {
                button.drawButton(mc, mouseX, mouseY);
                if (tooltipMetadata == null)
                {
                    if (button.mouseOver(mouseX, mouseY))
                    {
                        tooltipMetadata = buttonOptionMetadata.get(button);
                    }
                }
            }
        }

        this.currentToolTip = tooltipMetadata;
    }

    @Override
    public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        for (Button button : buttons)
        {
            if (button.mousePressed(mc, x, y))
            {
                lastPressed = buttonOptionMetadata.get(button);
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] mouseHover(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        for (Button button : buttons)
        {
            if (button.mouseOver(x, y))
            {
                return buttonOptionMetadata.get(button).getTooltip();
            }
        }
        return new String[0];
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        for (Button button : buttons)
        {
            button.mouseReleased(x, y);
        }
    }

    @Override
    public boolean keyTyped(char c, int i)
    {
        for (SlotMetadata slot : buttonOptionMetadata.values())
        {
            if (slot.button.keyTyped(c, i))
            {
                lastPressed = slot;
                return true;
            }
        }
        return false;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        for (SlotMetadata slot : buttonOptionMetadata.values())
        {
            if (!slot.isMasterPropertyForCategory())
            {
                slot.button.setEnabled(enabled);
            }
        }
    }

    @Override
    public List<ScrollListPane.ISlot> getChildSlots(int listWidth, int columnWidth)
    {
        return Collections.EMPTY_LIST;
    }

    public SlotMetadata getLastPressed()
    {
        return lastPressed;
    }

    @Override
    public SlotMetadata getCurrentTooltip()
    {
        return currentToolTip;
    }

    @Override
    public int getColumnWidth()
    {
        buttons.equalizeWidths(fontRenderer);
        return buttons.get(0).getWidth();
    }

    @Override
    public boolean contains(SlotMetadata slotMetadata)
    {
        return buttonOptionMetadata.values().contains(slotMetadata);
    }

    protected String getFirstButtonString()
    {
        if (buttons.size() > 0)
        {
            return buttons.get(0).displayString;
        }
        return null;
    }

    @Override
    public int compareTo(ButtonListSlot o)
    {
        String buttonString = getFirstButtonString();
        String otherButtonString = o.getFirstButtonString();
        if (!Strings.isNullOrEmpty(buttonString))
        {
            return buttonString.compareTo(otherButtonString);
        }
        if (!Strings.isNullOrEmpty(otherButtonString))
        {
            return 1;
        }
        return 0;
    }
}
