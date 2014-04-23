package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.render.draw.DrawUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by mwoodman on 4/2/2014.
 */
public class ButtonList extends ArrayList<Button>
{
    private boolean horizontal = true;

    public ButtonList(Button... buttons)
    {
        super(Arrays.asList(buttons));
    }

    public int getWidth(int hgap)
    {
        if(this.isEmpty()) return 0;

        int total = 0;
        int visible = 0;
        for(Button button : this)
        {
            if(button.drawButton)
            {
                total += button.getWidth();
                visible++;
            }
        }

        if(visible>1) {
            total += (hgap * (visible - 1));
        }
        return total;
    }

    public int getHeight(int vgap)
    {
        if(this.isEmpty()) return 0;

        int total = 0;
        int visible = 0;
        for(Button button : this)
        {
            if(button.drawButton) {
                total += button.getHeight();
                visible++;
            }
        }

        if(visible>1) {
            total += (vgap * (visible - 1));
        }
        return total;
    }

    public int getLeftX()
    {
        int left = Integer.MAX_VALUE;
        for(Button button : this)
        {
            if(button.drawButton) {
                left = Math.min(left, button.getX());
            }
        }
        if(left == Integer.MAX_VALUE)
        {
            left = 0;
        }
        return left;
    }

    public int getTopY()
    {
        int top = Integer.MAX_VALUE;
        for(Button button : this)
        {
            if(button.drawButton) {
                top = Math.min(top, button.getY());
            }
        }
        if(top == Integer.MAX_VALUE)
        {
            top = 0;
        }
        return top;
    }

    public int getBottomY()
    {
        int bottom = Integer.MIN_VALUE;
        for(Button button : this)
        {
            if(button.drawButton) {
                bottom = Math.max(bottom, button.getY() + button.getHeight());
            }
        }
        if(bottom == Integer.MIN_VALUE)
        {
            bottom = 0;
        }
        return bottom;
    }

    public int getRightX()
    {
        int right = 0;
        for(Button button : this)
        {
            if(button.drawButton) {
                right = Math.max(right, button.getX()+button.getWidth());
            }
        }
        return right;
    }

    public ButtonList layoutHorizontal(int startX, final int y, boolean leftToRight, int hgap)
    {
        this.horizontal = true;

        Button last = null;
        for(Button button : this)
        {
            if(last==null)
            {
                if(leftToRight)
                {
                    button.rightOf(startX).setY(y);
                }
                else
                {
                    button.leftOf(startX).setY(y);
                }
            }
            else
            {
                if(leftToRight)
                {
                    button.rightOf(last, hgap).setY(y);
                }
                else
                {
                    button.leftOf(last, hgap).setY(y);
                }
            }
            last = button;
        }
        return this;
    }

    public ButtonList layoutVertical(final int x, int startY, boolean leftToRight, int vgap)
    {
        this.horizontal = false;

        Button last = null;
        for(Button button : this)
        {
            if(last==null)
            {
                if(leftToRight)
                {
                    button.rightOf(x).setY(startY);
                }
                else
                {
                    button.leftOf(x).setY(startY);
                }
            }
            else
            {
                if(leftToRight)
                {
                    button.rightOf(x).below(last, vgap);
                }
                else
                {
                    button.leftOf(x).below(last, vgap);
                }
            }
            last = button;
        }

        return this;
    }

    public ButtonList layoutCenteredHorizontal(final int centerX, final int y, final boolean leftToRight, final int hgap)
    {
        this.horizontal = true;
        int width = getWidth(hgap);
        layoutHorizontal(centerX-(width/2), y, leftToRight, hgap);
        return this;
    }

    public ButtonList layoutDistributedHorizontal(final int leftX, final int y, final int rightX, final boolean leftToRight)
    {
        if(this.size()==0) return this;

        this.horizontal = true;
        int width = getWidth(0);
        int filler = (rightX-leftX)-width;
        int gaps = this.size()-1;
        int hgap = (filler>=gaps) ? filler/gaps : 0;

        if(leftToRight)
        {
            layoutHorizontal(leftX, y, true, hgap);
        }
        else
        {
            layoutHorizontal(rightX, y, false, hgap);
        }
        return this;
    }

    public ButtonList layoutFilledHorizontal(FontRenderer fr, final int leftX, final int y, final int rightX, final int hgap, final boolean leftToRight)
    {
        if(this.size()==0) return this;

        this.horizontal = true;
        this.setUniformWidths(fr);

        int width = getWidth(hgap);
        int remaining = (rightX-leftX)-width;
        if(remaining>this.size())
        {
            int gaps = hgap * (size());
            int area = (rightX-leftX) - gaps;
            int wider = area/size();
            setWidths(wider, this);
            layoutDistributedHorizontal(leftX, y, rightX, leftToRight);
        }
        else
        {
            layoutCenteredHorizontal((rightX-leftX)/2, y, leftToRight, hgap);
        }
        return this;
    }

    public void setUniformWidths(FontRenderer fr)
    {
        int max = 0;
        for(Button button : this)
        {
            if(button.drawButton) {
                max = Math.max(max, button.getFitWidth(fr));
            }
        }
        setWidths(max, this);
    }

    public void setFitWidths(FontRenderer fr)
    {
        fitWidths(fr, this);
    }

    public boolean isHorizontal()
    {
        return horizontal;
    }

    public ButtonList setOptions(boolean enabled, boolean drawBackground, boolean drawFrame)
    {
        for(Button button : this)
        {
            button.enabled = enabled;
            button.drawFrame = drawFrame;
            button.drawBackground = drawBackground;
        }
        return this;
    }

    public ButtonList draw(Minecraft minecraft, int mouseX, int mouseY)
    {
        for(Button button : this)
        {
            button.drawButton(minecraft, mouseX, mouseY);
        }
        return this;
    }

    public static void setHeights(int height, Collection<Button> collection)
    {
        for(Button button : collection)
        {
            button.setHeight(height);
        }
    }

    public static void setWidths(int width, Collection<Button> collection)
    {
        for(Button button : collection)
        {
            button.setWidth(width);
        }
    }

    public static void fitWidths(FontRenderer fr, Collection<Button> collection)
    {
        for(Button button : collection)
        {
            button.fitWidth(fr);
        }
    }

    public static void equalizeWidths(FontRenderer fr, Collection<Button> collection)
    {
        int maxWidth = 0;
        for(Button button : collection)
        {
            button.fitWidth(fr);
            maxWidth = Math.max(maxWidth, button.getWidth());
        }
        setWidths(maxWidth, collection);
    }

    public static void drawOutlines(int thick, Color color, int alpha, Collection<Button> collection)
    {
        for(Button button : collection)
        {
            DrawUtil.drawRectangle(button.getX()-thick, button.getY()-thick, button.getWidth()+(thick*2), button.getHeight()+(thick*2), color, alpha);
        }
    }
}
