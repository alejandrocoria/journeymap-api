package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mwoodman on 4/2/2014.
 */
public class ButtonList extends ArrayList<Button>
{
    private boolean horizontal = true;

    public ButtonList(List<Button> buttons)
    {
        super(buttons);
    }

    public ButtonList(Button... buttons)
    {
        super(Arrays.asList(buttons));
    }

    public int getWidth(int hgap)
    {
        return getWidth(-1, hgap);
    }

    private int getWidth(int buttonWidth, int hgap)
    {
        if (this.isEmpty())
        {
            return 0;
        }

        int total = 0;
        int visible = 0;
        for (Button button : this)
        {
            if (button.isDrawButton())
            {
                if (buttonWidth > 0)
                {
                    total += buttonWidth;
                }
                else
                {
                    total += button.getWidth();
                }
                visible++;
            }
        }

        if (visible > 1)
        {
            total += (hgap * (visible - 1));
        }
        return total;
    }

    public int getHeight(int vgap)
    {
        if (this.isEmpty())
        {
            return 0;
        }

        int total = 0;
        int visible = 0;
        for (Button button : this)
        {
            if (button.isDrawButton())
            {
                total += button.getHeight();
                visible++;
            }
        }

        if (visible > 1)
        {
            total += (vgap * (visible - 1));
        }
        return total;
    }

    public int getLeftX()
    {
        int left = Integer.MAX_VALUE;
        for (Button button : this)
        {
            if (button.isDrawButton())
            {
                left = Math.min(left, button.getX());
            }
        }
        if (left == Integer.MAX_VALUE)
        {
            left = 0;
        }
        return left;
    }

    public int getTopY()
    {
        int top = Integer.MAX_VALUE;
        for (Button button : this)
        {
            if (button.isDrawButton())
            {
                top = Math.min(top, button.getY());
            }
        }
        if (top == Integer.MAX_VALUE)
        {
            top = 0;
        }
        return top;
    }

    public int getBottomY()
    {
        int bottom = Integer.MIN_VALUE;
        for (Button button : this)
        {
            if (button.isDrawButton())
            {
                bottom = Math.max(bottom, button.getY() + button.getHeight());
            }
        }
        if (bottom == Integer.MIN_VALUE)
        {
            bottom = 0;
        }
        return bottom;
    }

    public int getRightX()
    {
        int right = 0;
        for (Button button : this)
        {
            if (button.isDrawButton())
            {
                right = Math.max(right, button.getX() + button.getWidth());
            }
        }
        return right;
    }

    public Button findButton(int id)
    {
        for (Button button : this)
        {
            if (button.id == id)
            {
                return button;
            }
        }
        return null;
    }

    public ButtonList layoutHorizontal(int startX, final int y, boolean leftToRight, int hgap)
    {
        this.horizontal = true;

        Button last = null;
        for (Button button : this)
        {
            if (last == null)
            {
                if (leftToRight)
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
                if (leftToRight)
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
        for (Button button : this)
        {
            if (last == null)
            {
                if (leftToRight)
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
                if (leftToRight)
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
        layoutHorizontal(centerX - (width / 2), y, leftToRight, hgap);
        return this;
    }

    public ButtonList layoutDistributedHorizontal(final int leftX, final int y, final int rightX, final boolean leftToRight)
    {
        if (this.size() == 0)
        {
            return this;
        }

        this.horizontal = true;
        int width = getWidth(0);
        int filler = (rightX - leftX) - width;
        int gaps = this.size() - 1;
        int hgap = (filler >= gaps) ? filler / gaps : 0;

        if (leftToRight)
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
        if (this.size() == 0)
        {
            return this;
        }

        this.horizontal = true;
        this.setUniformWidths(fr);

        int width = getWidth(hgap);
        int remaining = (rightX - leftX) - width;
        if (remaining > this.size())
        {
            int gaps = hgap * (size());
            int area = (rightX - leftX) - gaps;
            int wider = area / size();
            setWidths(wider);
            layoutDistributedHorizontal(leftX, y, rightX, leftToRight);
        }
        else
        {
            layoutCenteredHorizontal((rightX - leftX) / 2, y, leftToRight, hgap);
        }
        return this;
    }

    public void setUniformWidths(FontRenderer fr)
    {
        int max = 0;
        for (Button button : this)
        {
            if (button.isDrawButton())
            {
                max = Math.max(max, button.getFitWidth(fr));
            }
        }
        setWidths(max);
    }

    public void setFitWidths(FontRenderer fr)
    {
        fitWidths(fr);
    }

    public boolean isHorizontal()
    {
        return horizontal;
    }

    public ButtonList setOptions(boolean enabled, boolean drawBackground, boolean drawFrame)
    {
        for (Button button : this)
        {
            button.setEnabled(enabled);
            button.setDrawFrame(drawFrame);
            button.setDrawBackground(drawBackground);
        }
        return this;
    }

    public ButtonList draw(Minecraft minecraft, int mouseX, int mouseY)
    {
        for (Button button : this)
        {
            button.drawButton(minecraft, mouseX, mouseY);
        }
        return this;
    }

    public void setHeights(int height)
    {
        for (Button button : this)
        {
            button.setHeight(height);
        }
    }

    public void setWidths(int width)
    {
        for (Button button : this)
        {
            button.setWidth(width);
        }
    }

    public void fitWidths(FontRenderer fr)
    {
        for (Button button : this)
        {
            button.fitWidth(fr);
        }
    }

    public void equalizeWidths(FontRenderer fr)
    {
        int maxWidth = 0;
        for (Button button : this)
        {
            button.fitWidth(fr);
            maxWidth = Math.max(maxWidth, button.getWidth());
        }
        setWidths(maxWidth);
    }

    /**
     * Try to equalize all button widths, but set a max on the total horizontal
     * space that can be used.  If the fit widths still exceed maxTotalWidth,
     * they won't be made smaller; you need to provide more room or remove buttons.
     *
     * @param fr            font renderer
     * @param hgap          horizontal gap
     * @param maxTotalWidth max horizontal space allowed
     */
    public void equalizeWidths(FontRenderer fr, int hgap, int maxTotalWidth)
    {
        int maxWidth = 0;
        for (Button button : this)
        {
            button.fitWidth(fr);
            maxWidth = Math.max(maxWidth, button.getWidth());
        }

        int totalWidth = getWidth(maxWidth, hgap);
        if (totalWidth <= maxTotalWidth)
        {
            setWidths(maxWidth); // same result as setUniformWidths
        }
        else
        {
            totalWidth = getWidth(hgap);
        }

        if (totalWidth < maxTotalWidth)
        {
            // Pad the buttons to get up to maxTotalWidth
            int pad = (maxTotalWidth - totalWidth) / this.size();
            if (pad > 0)
            {
                for (Button button : this)
                {
                    button.setWidth(button.getWidth() + pad);
                }
            }
        }
    }
}
