/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.option;

import journeymap.client.cartography.color.RGB;
import journeymap.client.properties.ClientCategory;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ScrollListPane;
import journeymap.common.properties.Category;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.*;

/**
 *
 */
public class CategorySlot implements ScrollListPane.ISlot, Comparable<CategorySlot>
{
    Minecraft mc = FMLClientHandler.instance().getClient();
    SlotMetadata metadata;
    Category category;
    int currentSlotIndex;
    Button button;
    int currentListWidth;
    int currentColumns;
    int currentColumnWidth;
    SlotMetadata masterSlot;
    SlotMetadata currentTooltip;
    LinkedList<SlotMetadata> childMetadataList = new LinkedList<SlotMetadata>();
    List<ScrollListPane.ISlot> childSlots = new ArrayList<ScrollListPane.ISlot>();
    String glyphClosed = "\u25B6";
    String glyphOpen = "\u25BC";
    private boolean selected;

    public CategorySlot(Category category)
    {
        this.category = category;
        boolean advanced = (category == ClientCategory.Advanced);
        button = new Button(category.getLabel());
//        button.setDefaultStyle(false);
//        button.setDrawLabelShadow(false);
//        button.setLabelColors(new Color(10, 10, 100), new Color(10, 10, 100), null);
//
//        Color smallBgColor = new Color(220, 220, 250);
//        Color smallBgHoverColor = new Color(235, 235, 255);
//        Color smallBgHoverColor2 = new Color(100, 100, 100);
//
//        button.setBackgroundColors(smallBgColor, smallBgHoverColor, smallBgHoverColor2);

        metadata = new SlotMetadata(button, category.getLabel(), category.getTooltip(), advanced);
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
    public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
    {

    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
        currentSlotIndex = slotIndex;
        button.setWidth(listWidth);
        button.setPosition(x, y);
        button.setHeight(slotHeight);
        button.drawButton(mc, mouseX, mouseY, 0f);

        DrawUtil.drawRectangle(button.getX() + 4, button.getMiddleY() - 5, 11, 10, RGB.BLACK_RGB, .2f);
        DrawUtil.drawLabel(selected ? glyphOpen : glyphClosed, button.getX() + 12, button.getMiddleY(), DrawUtil.HAlign.Left, DrawUtil.VAlign.Middle, RGB.BLACK_RGB, 0, button.getLabelColor(), 1f, 1, true);

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
            currentTooltip = metadata;
        }
        currentTooltip = null;
    }

    private void updateButtonLabel()
    {
        this.button.displayString = category.getLabel();
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
        if (mouseEvent == 0)
        {
            boolean pressed = button.mousePressed(mc, x, y);
            if (pressed)
            {
                selected = !selected;
                updateButtonLabel();
            }
            return pressed;
        }
        return false;
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

    @Override
    public SlotMetadata getCurrentTooltip()
    {
        return currentTooltip;
    }

    public boolean contains(SlotMetadata slotMetadata)
    {
        return childMetadataList.contains(slotMetadata);
    }

    public Category getCategory()
    {
        return category;
    }
}
