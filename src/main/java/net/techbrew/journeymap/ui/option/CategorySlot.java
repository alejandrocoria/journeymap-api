/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.ui.option;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.forge.helper.ForgeHelper;
import net.techbrew.journeymap.properties.config.Config;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.ScrollListPane;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 */
public class CategorySlot implements ScrollListPane.ISlot, Comparable<CategorySlot>
{
    final String name;
    Minecraft mc = ForgeHelper.INSTANCE.getClient();
    SlotMetadata metadata;
    Config.Category category;
    int currentSlotIndex;
    Button button;
    int currentListWidth;
    int currentColumns;
    int currentColumnWidth;
    SlotMetadata masterSlot;
    LinkedList<SlotMetadata> childMetadataList = new LinkedList<SlotMetadata>();
    List<ScrollListPane.ISlot> childSlots = new ArrayList<ScrollListPane.ISlot>();
    String glyphClosed = "\u25B6";
    String glyphOpen = "\u25BC";
    private boolean selected;

    public CategorySlot(Config.Category category)
    {
        this.category = category;
        this.name = Constants.getString(category.key);
        String tooltip = Constants.getString(category.key + ".tooltip");
        boolean advanced = category == Config.Category.Advanced;

        button = new Button(name);
//        button.setDefaultStyle(false);
//        button.setDrawLabelShadow(false);
//        button.setLabelColors(new Color(10, 10, 100), new Color(10, 10, 100), null);
//
//        Color smallBgColor = new Color(220, 220, 250);
//        Color smallBgHoverColor = new Color(235, 235, 255);
//        Color smallBgHoverColor2 = new Color(100, 100, 100);
//
//        button.setBackgroundColors(smallBgColor, smallBgHoverColor, smallBgHoverColor2);

        metadata = new SlotMetadata(button, name, tooltip, advanced);
        updateButtonLabel();
    }

    public CategorySlot add(ScrollListPane.ISlot slot)
    {
        childSlots.add(slot);
        childMetadataList.addAll(slot.getMetadata());
        for (SlotMetadata slotMetadata : slot.getMetadata())
        {
            if (slotMetadata.isMasterPropertyForCategory())
            {
                masterSlot = slotMetadata;
            }
        }
        return this;
    }

    public void clear()
    {
        childSlots.clear();
    }

    public int size()
    {
        return childSlots.size();
    }

    public void sort()
    {
        Collections.sort(childMetadataList);
    }

    @Override
    public int getColumnWidth()
    {
        int columnWidth = 100;
        for (ScrollListPane.ISlot slot : childSlots)
        {
            columnWidth = Math.max(columnWidth, slot.getColumnWidth());
        }
        return columnWidth;
    }

    @Override
    public List<ScrollListPane.ISlot> getChildSlots(int listWidth, int columnWidth)
    {
        if (!selected)
        {
            return Collections.EMPTY_LIST;
        }

        int columns = listWidth / (columnWidth + ButtonListSlot.hgap);
        if (columnWidth == currentColumnWidth && columns == currentColumns)
        {
            return childSlots;
        }

        currentListWidth = listWidth;
        currentColumnWidth = columnWidth;
        currentColumns = columns;

        // Rebuild slots
        childSlots.clear();
        sort();

        ArrayList<SlotMetadata> remaining = new ArrayList<SlotMetadata>(childMetadataList);
        while (!remaining.isEmpty())
        {
            ButtonListSlot row = new ButtonListSlot(this);
            SlotMetadata.ValueType lastType = null;
            for (int i = 0; i < columns; i++)
            {
                if (!remaining.isEmpty())
                {
                    SlotMetadata.ValueType thisType = remaining.get(0).valueType;
                    if (lastType == null && thisType == SlotMetadata.ValueType.Toolbar)
                    {
                        row.addAll(remaining);
                        remaining.clear();
                        break;
                    }

                    if (lastType != null && lastType != thisType)
                    {
                        if (thisType == SlotMetadata.ValueType.Toolbar)
                        {
                            break;
                        }
                        if (lastType == SlotMetadata.ValueType.Boolean)
                        {
                            if (remaining.size() > columns - i)
                            {
                                break;
                            }
                        }
                    }
                    SlotMetadata column = remaining.remove(0);
                    lastType = column.valueType;
                    row.add(column);
                }
                else
                {
                    break;
                }
            }
            row.buttons.setWidths(columnWidth);
            childSlots.add(row);
        }

        return childSlots;
    }

    @Override
    public Collection<SlotMetadata> getMetadata()
    {
        return Arrays.asList(metadata);
    }

    public List<SlotMetadata> getAllChildMetadata()
    {
        return childMetadataList;
    }

    public int getCurrentColumns()
    {
        return currentColumns;
    }

    public int getCurrentColumnWidth()
    {
        return currentColumnWidth;
    }

    @Override
    // 1.7 public SlotMetadata drawSlot(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected)
    public SlotMetadata drawSlot(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        currentSlotIndex = slotIndex;
        button.setWidth(listWidth);
        button.setPosition(x, y);
        button.setHeight(slotHeight);
        button.drawButton(mc, mouseX, mouseY);

        DrawUtil.drawRectangle(button.getX() + 4, button.getMiddleY() - 5, 11, 10, Color.black, 50);
        DrawUtil.drawLabel(selected ? glyphOpen : glyphClosed, button.getX() + 12, button.getMiddleY(), DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, Color.black, 0, button.getLabelColor(), 255, 1, true);

        if (masterSlot != null && selected)
        {
            boolean enabled = masterSlot.button.isActive();
            for (ScrollListPane.ISlot slot : childSlots)
            {
                slot.setEnabled(enabled);
            }
        }

        if (button.mouseOver(mouseX, mouseY))
        {
            return metadata;
        }
        return null;
    }

    private void updateButtonLabel()
    {
        this.button.displayString = name;
    }

    public boolean isSelected()
    {
        return this.selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        boolean pressed = button.mousePressed(mc, x, y);
        if (pressed)
        {
            selected = !selected;
            updateButtonLabel();
        }
        return pressed;
    }

    @Override
    public String[] mouseHover(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        if (button.mouseOver(x, y))
        {
            return metadata.getTooltip();
        }
        return new String[0];
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        button.mouseReleased(x, y);
    }

    @Override
    public boolean keyTyped(char c, int i)
    {
        return false;
    }

    @Override
    public int compareTo(CategorySlot other)
    {
        return category.compareTo(other.category);
    }

    @Override
    public void setEnabled(boolean enabled)
    {

    }

    public SlotMetadata getLastPressed()
    {
        return null;
    }

    public boolean contains(SlotMetadata slotMetadata)
    {
        return childMetadataList.contains(slotMetadata);
    }

    public Config.Category getCategory()
    {
        return category;
    }
}
