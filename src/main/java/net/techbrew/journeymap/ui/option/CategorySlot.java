package net.techbrew.journeymap.ui.option;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.config.Config;
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
    Minecraft mc = FMLClientHandler.instance().getClient();
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
    String glyphDown = "\u25BC";
    String glyphUp = "\u25B2";
    private boolean selected;

    public CategorySlot(Config.Category category)
    {
        this.category = category;
        this.name = Constants.getString(category.key);
        String tooltip = Constants.getString(category.key + ".tooltip");
        boolean advanced = category == Config.Category.Advanced;

        button = new Button(name);
        button.setDefaultStyle(false);
        button.packedFGColour = Color.white.getRGB();
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
    public SlotMetadata drawSlot(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected)
    {
        currentSlotIndex = slotIndex;
        button.setWidth(listWidth);
        button.setPosition(x, y);
        button.setHeight(slotHeight);
        button.drawButton(mc, mouseX, mouseY);

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
        String glyph = selected ? glyphUp : glyphDown;
        this.button.displayString = String.format("%1$s  %2$s  %1$s", glyph, name);
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
