package net.techbrew.journeymap.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.ui.option.SlotMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Adapted from GuiListExtended
 */
public class ScrollListPane extends GuiSlot
{
    final JmUI parent;
    public SlotMetadata lastTooltipMetadata;
    public String[] lastTooltip;
    public long lastTooltipTime;
    public long hoverDelay = 800;
    int hpad = 12;
    List<ISlot> rootSlots;
    List<ISlot> currentSlots = new ArrayList<ISlot>(0);
    SlotMetadata lastPressed;
    int lastClickedIndex;
    int scrollbarX;
    int listWidth;

    public ScrollListPane(JmUI parent, Minecraft mc, int width, int height, int top, int bottom, int slotHeight)
    {
        super(mc, width, height, top, bottom, slotHeight);
        this.parent = parent;
        func_148122_a(width, height, top, bottom);
    }

    public void func_148122_a(int width, int height, int top, int bottom)
    {
        super.func_148122_a(width, height, top, bottom);
        scrollbarX = this.width - (hpad);
        listWidth = this.width - (hpad * 4);
    }

    @Override
    protected int getSize()
    {
        return this.currentSlots == null ? 0 : currentSlots.size();
    }

    public void setSlots(List<ISlot> slots)
    {
        this.rootSlots = slots;
        updateSlots();
    }

    public List<ISlot> getRootSlots()
    {
        return rootSlots;
    }

    public void updateSlots()
    {
        int sizeBefore = currentSlots.size();
        this.currentSlots.clear();

        int columnWidth = 0;
        for (ISlot slot : rootSlots)
        {
            columnWidth = Math.max(columnWidth, slot.getColumnWidth());
        }

        for (ISlot slot : rootSlots)
        {
            currentSlots.add(slot);
            List<ISlot> children = slot.getChildSlots(listWidth, columnWidth);
            if (children != null && !children.isEmpty())
            {
                currentSlots.addAll(children);
            }
        }
        int sizeAfter = currentSlots.size();

        if (sizeBefore < sizeAfter)
        {
            scrollBy(-(sizeAfter * slotHeight));
            scrollBy(lastClickedIndex * slotHeight);
        }
    }

    /**
     * The element in the slot that was clicked, boolean for whether it was double clicked or not
     */
    protected void elementClicked(int index, boolean doubleClick, int mouseX, int mouseY)
    {

    }

    @Override
    protected boolean isSelected(int p_148131_1_)
    {
        return false;
    }

    protected void drawBackground()
    {

    }

    protected void drawSlot(int slotIndex, int x, int y, int slotHeight, Tessellator tessellator, int mouseX, int mouseY)
    {
        SlotMetadata tooltipMetadata = this.getSlot(slotIndex).drawSlot(slotIndex, x, y, this.getListWidth(), slotHeight, tessellator, mouseX, mouseY,
                this.func_148124_c(mouseX, mouseY) == slotIndex); // getSlotIndexFromScreenCoords

        if (tooltipMetadata != null && !Arrays.equals(tooltipMetadata.getTooltip(), lastTooltip))
        {
            lastTooltipMetadata = tooltipMetadata;
            lastTooltip = tooltipMetadata.getTooltip();
            lastTooltipTime = System.currentTimeMillis();
        }
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return listWidth;
    }

    public boolean mousePressed(int mouseX, int mouseY, int mouseEvent)
    {
        if (this.func_148141_e(mouseY)) // isMouseYWithinSlotBounds
        {
            int slotIndex = this.func_148124_c(mouseX, mouseY); // getSlotIndexFromScreenCoords

            if (slotIndex >= 0)
            {
                int i1 = this.left + hpad + this.width / 2 - this.getListWidth() / 2 + 2;
                int j1 = this.top + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.headerPadding;
                int relativeX = mouseX - i1;
                int relativeY = mouseY - j1;
                lastClickedIndex = -1;
                if (this.getSlot(slotIndex).mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY))
                {
                    this.func_148143_b(false); // setEnabled
                    lastClickedIndex = slotIndex;
                    lastPressed = this.getSlot(slotIndex).getLastPressed();
                    updateSlots();
                    return true;
                }
            }
        }

        return false;
    }


    public boolean mouseReleased(int x, int y, int mouseEvent)
    {
        for (int slotIndex = 0; slotIndex < this.getSize(); ++slotIndex)
        {
            int i1 = this.left + hpad + this.width / 2 - this.getListWidth() / 2 + 2;
            int j1 = this.top + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.headerPadding;
            int relativeX = x - i1;
            int relativeY = y - j1;
            this.getSlot(slotIndex).mouseReleased(slotIndex, x, y, mouseEvent, relativeX, relativeY);
        }

        this.func_148143_b(true); // setEnabled
        lastPressed = null;
        return false;
    }

    /**
     * Gets the ISlot object for the given index
     */
    public ISlot getSlot(int index)
    {
        return currentSlots.get(index);
    }

    public SlotMetadata getLastPressed()
    {
        return lastPressed;
    }

    public ISlot getLastPressedParentSlot()
    {
        if (lastPressed != null)
        {
            for (ISlot slot : rootSlots)
            {
                if (slot.contains(lastPressed))
                {
                    return slot;
                }
            }
        }
        return null;
    }

    public boolean keyTyped(char c, int i)
    {
        for (int slotIndex = 0; slotIndex < this.getSize(); ++slotIndex)
        {
            if (this.getSlot(slotIndex).keyTyped(c, i))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getScrollBarX()
    {
        return scrollbarX;
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator)
    {
        parent.drawGradientRect(0, top, this.width, this.height - top, -1072689136, -804253680);
    }

    public interface ISlot
    {
        Collection<SlotMetadata> getMetadata();

        SlotMetadata drawSlot(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected);

        /**
         * Returns true if the mouse has been pressed on a control in this slot.
         */
        boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);

        /**
         * Returns array of strings to display in a hover if the mouse is over the slot
         */
        String[] mouseHover(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);

        /**
         * Called when a key is pressed. Return true to prevent event propagation further.
         */
        boolean keyTyped(char c, int i);

        List<ISlot> getChildSlots(int listWidth, int columnWidth);

        SlotMetadata getLastPressed();

        void setEnabled(boolean enabled);

        int getColumnWidth();

        boolean contains(SlotMetadata slotMetadata);
    }
}
