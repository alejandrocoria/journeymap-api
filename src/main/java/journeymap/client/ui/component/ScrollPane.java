/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import journeymap.client.render.draw.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Scroll pane.
 */
public class ScrollPane extends GuiSlot
{
    /**
     * The Pane width.
     */
    public int paneWidth = 0;
    /**
     * The Pane height.
     */
    public int paneHeight = 0;
    /**
     * The Origin.
     */
    public Point.Double origin = new Point2D.Double();
    /**
     * The Selected.
     */
    protected Scrollable selected = null;
    private Integer frameColor = new Color(-6250336).getRGB();
    private List<? extends Scrollable> items;
    private Minecraft mc;
    private int _mouseX;
    private int _mouseY;
    private boolean showFrame = true;
    private int firstVisibleIndex;
    private int lastVisibleIndex;

    /**
     * Instantiates a new Scroll pane.
     *
     * @param mc         the mc
     * @param width      the width
     * @param height     the height
     * @param items      the items
     * @param itemHeight the item height
     * @param itemGap    the item gap
     */
    public ScrollPane(Minecraft mc, int width, int height, List<? extends Scrollable> items, int itemHeight, int itemGap)
    {
        super(mc, width, height, 16, height, itemHeight + itemGap);
        this.items = items;
        paneWidth = width;
        paneHeight = height;
        this.mc = mc;
    }

    /**
     * Gets x.
     *
     * @return the x
     */
    public int getX()
    {
        return (int) origin.getX();
    }

    /**
     * Gets y.
     *
     * @return the y
     */
    public int getY()
    {
        return (int) origin.getY();
    }

    public int getSlotHeight()
    {
        return this.slotHeight;
    }

    /**
     * Sets dimensions.
     *
     * @param width        the width
     * @param height       the height
     * @param marginTop    the margin top
     * @param marginBottom the margin bottom
     * @param x            the x
     * @param y            the y
     */
    public void setDimensions(int width, int height, int marginTop, int marginBottom, int x, int y)
    {
        // 1.7
        // super.func_148122_a(width, height, marginTop, height - marginBottom);

        // 1.8
        super.setDimensions(width, height, marginTop, height - marginBottom);
        paneWidth = width;
        paneHeight = height;
        origin.setLocation(x, y);
    }

    @Override
    protected int getSize()
    {
        return items.size();
    }

    @Override
    protected void elementClicked(int i, boolean flag, int p1, int p2)
    {
        selected = items.get(i);
    }

    @Override
    protected boolean isSelected(int i)
    {
        return items.get(i) == selected;
    }

    /**
     * Is selected boolean.
     *
     * @param item the item
     * @return the boolean
     */
    public boolean isSelected(Scrollable item)
    {
        return item == selected;
    }

    /**
     * Select.
     *
     * @param item the item
     */
    public void select(Scrollable item)
    {
        selected = item;
    }

    @Override
    protected void drawBackground()
    {
    }

    /**
     * Call when the mouse is clicked.  Returns a Button if one was clicked.
     *
     * @param mouseX      the mouse x
     * @param mouseY      the mouse y
     * @param mouseButton the mouse button
     * @return the journeymap . client . ui . component . button
     */
    public journeymap.client.ui.component.Button mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0)
        {
            ArrayList<Scrollable> itemsCopy = new ArrayList<Scrollable>(items);
            for (Scrollable item : itemsCopy)
            {
                if (item == null)
                {
                    continue;
                }

                if (inFullView(item))
                {
                    if (item instanceof journeymap.client.ui.component.Button)
                    {
                        journeymap.client.ui.component.Button button = (journeymap.client.ui.component.Button) item;
                        if (button.mousePressed(this.mc, mouseX, mouseY))
                        {
                            this.actionPerformed(button);
                            return button;
                        }
                    }
                    else
                    {
                        item.clickScrollable(this.mc, mouseX, mouseY);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void drawScreen(int mX, int mY, float f) // func_148128_a
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(getX(), getY(), 0);

        _mouseX = mX;
        _mouseY = mY;

        if (selected != null && !Mouse.isButtonDown(0) && Mouse.getDWheel() == 0)
        {
            if (Mouse.next() && Mouse.getEventButtonState())
            {
                // TODO draw selected?
            }
        }

        firstVisibleIndex = -1;
        lastVisibleIndex = -1;

        super.drawScreen(mX - getX(), mY - getY(), f);
        GlStateManager.popMatrix();
    }

    /**
     * Draw slot.
     *
     * @param index       the index
     * @param xPosition   the x position
     * @param yPosition   the y position
     * @param l           the l
     * @param tessellator the tessellator
     * @param var6        the var 6
     * @param var7        the var 7
     */
// 1.7
    // @Override
    protected void drawSlot(int index, int xPosition, int yPosition, int l, Tessellator tessellator, int var6, int var7)
    {
        drawSlot(index, xPosition, yPosition, l, null, var6, var7);
    }

    // 1.8
    @Override
    protected void drawSlot(int index, int xPosition, int yPosition, int l, int var6, int var7)
    {
        if (firstVisibleIndex == -1)
        {
            firstVisibleIndex = index;
        }
        lastVisibleIndex = Math.max(lastVisibleIndex, index);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-getX(), -getY(), 0);

        final int margin = 4;
        final int itemX = getX() + (margin / 2);
        final int itemY = yPosition + getY();

        Scrollable item = items.get(index);
        item.setPosition(itemX, itemY);
        item.setScrollableWidth(this.paneWidth - margin); // func_180791_a

        //System.out.println(String.format("Item %s = %s", index, itemY));

        if (inFullView(item))
        {
            item.drawScrollable(mc, _mouseX, _mouseY);
        }
        else
        {
            final int paneBottomY = this.getY() + this.paneHeight;
            final int itemBottomY = itemY + item.getHeight();

            Integer drawY = null;
            int yDiff = 0;
            if (itemY < this.getY() && itemBottomY > this.getY())
            {
                drawY = this.getY();
                yDiff = drawY - itemY;
            }
            else if (itemY < paneBottomY && itemBottomY > paneBottomY)
            {
                drawY = itemY;
                yDiff = itemBottomY - paneBottomY;
            }

            if (drawY != null)
            {
                item.drawPartialScrollable(mc, itemX, drawY, item.getWidth(), item.getHeight() - yDiff);
            }
        }

        GlStateManager.popMatrix();
    }

    /**
     * In full view boolean.
     *
     * @param item the item
     * @return the boolean
     */
    public boolean inFullView(Scrollable item)
    {
        return (item.getY() >= this.getY()) && item.getY() + item.getHeight() <= this.getY() + this.paneHeight;
    }

    @Override
    protected int getScrollBarX()
    {
        return this.paneWidth;
    }

    /**
     * Gets width.
     *
     * @return the width
     */
    public int getWidth()
    {
        boolean scrollVisible = 0 < this.getAmountScrolled(); // TODO right super?
        return paneWidth + (scrollVisible ? 5 : 0);
    }

    /**
     * Gets fit width.
     *
     * @param fr the fr
     * @return the fit width
     */
    public int getFitWidth(FontRenderer fr)
    {
        int fit = 0;
        for (Scrollable item : items)
        {
            fit = Math.max(fit, item.getFitWidth(fr));
        }
        return fit;
    }

    /**
     * Sets show frame.
     *
     * @param showFrame the show frame
     */
    public void setShowFrame(boolean showFrame)
    {
        this.showFrame = showFrame;
    }

    @Override
    protected void drawContainerBackground(Tessellator tess)
    {
        int width = getWidth();
        float alpha = .4f;

        // Tinted scrollbar area
        DrawUtil.drawRectangle(0, top, width, paneHeight, Color.BLACK.getRGB(), alpha);
        DrawUtil.drawRectangle(width - 6, top, 5, paneHeight, Color.BLACK.getRGB(), alpha);

        // Frame
        if (showFrame)
        {
            alpha = 1f;
            DrawUtil.drawRectangle(-1, -1, width + 2, 1, frameColor, alpha);
            DrawUtil.drawRectangle(-1, paneHeight, width + 2, 1, frameColor, alpha);

            DrawUtil.drawRectangle(-1, -1, 1, paneHeight + 1, frameColor, alpha);
            DrawUtil.drawRectangle(width + 1, -1, 1, paneHeight + 2, frameColor, alpha);
        }
    }

    /**
     * Gets first visible index.
     *
     * @return the first visible index
     */
    public int getFirstVisibleIndex()
    {
        return firstVisibleIndex;
    }

    /**
     * Gets last visible index.
     *
     * @return the last visible index
     */
    public int getLastVisibleIndex()
    {
        return lastVisibleIndex;
    }

    /**
     * The interface Scrollable.
     */
    public interface Scrollable
    {
        /**
         * Sets position.
         *
         * @param x the x
         * @param y the y
         */
        public void setPosition(int x, int y);

        /**
         * Gets x.
         *
         * @return the x
         */
        public int getX();

        /**
         * Gets y.
         *
         * @return the y
         */
        public int getY();

        /**
         * Gets width.
         *
         * @return the width
         */
        public int getWidth();

        /**
         * Sets scrollable width.
         *
         * @param width the width
         */
        public void setScrollableWidth(int width);

        /**
         * Gets fit width.
         *
         * @param fr the fr
         * @return the fit width
         */
        public int getFitWidth(FontRenderer fr);

        /**
         * Gets height.
         *
         * @return the height
         */
        public int getHeight();

        /**
         * Draw scrollable.
         *
         * @param mc     the mc
         * @param mouseX the mouse x
         * @param mouseY the mouse y
         */
        public void drawScrollable(Minecraft mc, int mouseX, int mouseY);

        /**
         * Draw partial scrollable.
         *
         * @param mc     the mc
         * @param x      the x
         * @param y      the y
         * @param width  the width
         * @param height the height
         */
        public void drawPartialScrollable(Minecraft mc, int x, int y, int width, int height);

        /**
         * Click scrollable.
         *
         * @param mc     the mc
         * @param mouseX the mouse x
         * @param mouseY the mouse y
         */
        public void clickScrollable(Minecraft mc, int mouseX, int mouseY);
    }
}
