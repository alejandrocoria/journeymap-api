package net.techbrew.journeymap.ui.option;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.ui.component.Button;
import net.techbrew.journeymap.ui.component.ScrollListPane;

import java.util.*;

/**
 *
 */
public class CategorySlot implements ScrollListPane.ISlot, Comparable<CategorySlot>
{
    Minecraft mc = FMLClientHandler.instance().getClient();
    SlotMetadata metadata;
    Config.Category category;
    int currentSlotIndex;
    boolean selected;
    Button button;
    int currentWidth;
    int currentColumns;
    int columnWidth;
    LinkedHashSet<SlotMetadata> childMetadataList = new LinkedHashSet<SlotMetadata>();
    List<ScrollListPane.ISlot> childSlots = new ArrayList<ScrollListPane.ISlot>();

    public CategorySlot(Config.Category category)
    {
        this.category = category;
        String name = Constants.getString(category.key);
        String tooltip = Constants.getString(category.key + ".tooltip");
        boolean advanced = category == Config.Category.Advanced;

        button = new Button(0, name);
        button.setDefaultStyle(false);
        metadata = new SlotMetadata(button, name, tooltip, advanced);
    }

    public CategorySlot add(ScrollListPane.ISlot slot)
    {
        childSlots.add(slot);
        childMetadataList.addAll(slot.getMetadata());
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
        Collections.sort(childSlots, new Comparator<ScrollListPane.ISlot>()
        {
            @Override
            public int compare(ScrollListPane.ISlot o1, ScrollListPane.ISlot o2)
            {
                if (o1 instanceof CategorySlot)
                {
                    if (o2 instanceof CategorySlot)
                    {
                        return ((CategorySlot) o1).category.compareTo(((CategorySlot) o2).category);
                    }
                    return -1;
                }

                if (o2 instanceof CategorySlot)
                {
                    return 1;
                }

                if (o1 instanceof ButtonListSlot && o2 instanceof ButtonListSlot)
                {
                    return ((ButtonListSlot) o1).compareTo((ButtonListSlot) o2);
                }

                return 0;
            }
        });
    }

    public List<ScrollListPane.ISlot> getChildSlots()
    {
        if (selected)
        {
            return childSlots;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<ScrollListPane.ISlot> getChildSlots(int listWidth)
    {
        if (!selected)
        {
            return Collections.EMPTY_LIST;
        }

        if (listWidth == currentWidth)
        {
            return childSlots;
        }

        currentWidth = listWidth;

        if (columnWidth == 0)
        {
            columnWidth = 100;
            for (ScrollListPane.ISlot slot : childSlots)
            {
                columnWidth = Math.max(columnWidth, slot.getMinimumWidth());
            }

        }

        int columns = currentWidth / (columnWidth + ButtonListSlot.hgap);
        if (columns == currentColumns)
        {
            return childSlots;
        }
        currentColumns = columns;

        // Rebuild slots
        childSlots.clear();
        Iterator<SlotMetadata> iterator = childMetadataList.iterator();
        while (iterator.hasNext())
        {
            ButtonListSlot row = new ButtonListSlot();
            for (int i = 0; i < columns; i++)
            {
                if (iterator.hasNext())
                {
                    row.add(iterator.next());
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
    public int getMinimumWidth()
    {
        return currentWidth;
    }

    @Override
    public Collection<SlotMetadata> getMetadata()
    {
        return Arrays.asList(metadata);
    }

    @Override
    public void drawSlot(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected)
    {
        currentSlotIndex = slotIndex;
        button.setWidth(listWidth);
        button.setPosition(x, y);
        button.setHeight(slotHeight);
        button.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        boolean pressed = button.mousePressed(mc, x, y);
        if (pressed)
        {
            selected = !selected;
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
    public int compareTo(CategorySlot other)
    {
        return category.compareTo(other.category);
    }
}
