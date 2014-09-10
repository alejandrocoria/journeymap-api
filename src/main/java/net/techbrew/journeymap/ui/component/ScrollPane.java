/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     cpw - implementation
 */

package net.techbrew.journeymap.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.render.draw.DrawUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class ScrollPane extends GuiSlot
{
    public int paneWidth = 0;
    public int paneHeight = 0;
    public Point.Double origin = new Point2D.Double();
    protected Scrollable selected = null;
    private Color frameColor = new Color(-6250336);
    private List<? extends Scrollable> items;
    private Minecraft mc;
    private int _mouseX;
    private int _mouseY;
    private boolean showFrame = true;
    private int firstVisibleIndex;
    private int lastVisibleIndex;

    public ScrollPane(Minecraft mc, int width, int height, List<? extends Scrollable> items, int itemHeight, int itemGap)
    {
        super(mc, width, height, 16, height, itemHeight + itemGap);
        this.items = items;
        paneWidth = width;
        paneHeight = height;
        this.mc = mc;
    }

    public int getX()
    {
        return (int) origin.getX();
    }

    public int getY()
    {
        return (int) origin.getY();
    }

    public int getSlotHeight()
    {
        return this.slotHeight;
    }

    public void position(int width, int height, int marginTop, int marginBottom, int x, int y)
    {
        super.func_148122_a(width, height, marginTop, height - marginBottom);
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

    public boolean isSelected(Scrollable item)
    {
        return item == selected;
    }

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
     */
    public net.techbrew.journeymap.ui.component.Button mouseClicked(int mouseX, int mouseY, int mouseButton)
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
                    if (item instanceof net.techbrew.journeymap.ui.component.Button)
                    {
                        net.techbrew.journeymap.ui.component.Button button = (net.techbrew.journeymap.ui.component.Button) item;
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
    public void drawScreen(int mX, int mY, float f)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(getX(), getY(), 0);

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
        GL11.glPopMatrix();
    }

    @Override
    protected void drawSlot(int index, int xPosition, int yPosition, int l, Tessellator tessellator, int var6, int var7)
    {
        if (firstVisibleIndex == -1)
        {
            firstVisibleIndex = index;
        }
        lastVisibleIndex = Math.max(lastVisibleIndex, index);

        GL11.glPushMatrix();
        GL11.glTranslated(-getX(), -getY(), 0);

        final int margin = 4;
        final int itemX = getX() + (margin / 2);
        final int itemY = yPosition + getY();

        Scrollable item = items.get(index);
        item.setPosition(itemX, itemY);
        item.setWidth(this.paneWidth - margin);

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

        GL11.glPopMatrix();
    }

    public boolean inFullView(Scrollable item)
    {
        return (item.getY() >= this.getY()) && item.getY() + item.getHeight() <= this.getY() + this.paneHeight;
    }

    @Override
    protected int getScrollBarX()
    {
        return this.paneWidth;
    }

    public int getWidth()
    {
        boolean scrollVisible = 0 < this.getAmountScrolled(); // TODO right super?
        return paneWidth + (scrollVisible ? 5 : 0);
    }

    public int getFitWidth(FontRenderer fr)
    {
        int fit = 0;
        for (Scrollable item : items)
        {
            fit = Math.max(fit, item.getFitWidth(fr));
        }
        return fit;
    }

    public void setShowFrame(boolean showFrame)
    {
        this.showFrame = showFrame;
    }

    @Override
    protected void drawContainerBackground(Tessellator tess)
    {
        int width = getWidth();
        int alpha = 100;

        // Tinted scrollbar area
        DrawUtil.drawRectangle(0, top, width, paneHeight, Color.BLACK, alpha);
        DrawUtil.drawRectangle(width - 6, top, 5, paneHeight, Color.BLACK, alpha);

        // Frame
        if (showFrame)
        {
            alpha = 255;
            DrawUtil.drawRectangle(-1, -1, width + 2, 1, frameColor, alpha);
            DrawUtil.drawRectangle(-1, paneHeight, width + 2, 1, frameColor, alpha);

            DrawUtil.drawRectangle(-1, -1, 1, paneHeight + 1, frameColor, alpha);
            DrawUtil.drawRectangle(width + 1, -1, 1, paneHeight + 2, frameColor, alpha);
        }
    }

    public int getFirstVisibleIndex()
    {
        return firstVisibleIndex;
    }

    public int getLastVisibleIndex()
    {
        return lastVisibleIndex;
    }

    public interface Scrollable
    {
        public void setPosition(int x, int y);

        public int getX();

        public int getY();

        public int getWidth();

        public void setWidth(int width);

        public int getFitWidth(FontRenderer fr);

        public int getHeight();

        public void drawScrollable(Minecraft mc, int mouseX, int mouseY);

        public void drawPartialScrollable(Minecraft mc, int x, int y, int width, int height);

        public void clickScrollable(Minecraft mc, int mouseX, int mouseY);
    }
}
