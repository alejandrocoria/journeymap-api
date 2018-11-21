/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.util.*;

import static journeymap.client.ui.component.ButtonList.Layout.*;

/**
 * The type Button list.
 *
 * @author techbrew 4/2/2014.
 */
public class ButtonList extends ArrayList<Button>
{
    static EnumSet<Layout> VerticalLayouts = EnumSet.of(Vertical, CenteredVertical);
    static EnumSet<Layout> HorizontalLayouts = EnumSet.of(Horizontal, CenteredHorizontal, DistributedHorizontal, FilledHorizontal);

    private Layout layout = Layout.Horizontal;
    private Direction direction = Direction.LeftToRight;
    private String label;

    /**
     * Instantiates a new Button list.
     */
    public ButtonList()
    {

    }

    /**
     * Instantiates a new Button list.
     *
     * @param label the label
     */
    public ButtonList(String label)
    {
        this.label = label;
    }

    /**
     * Instantiates a new Button list.
     *
     * @param buttons the buttons
     */
    public ButtonList(List<GuiButton> buttons)
    {
        for (GuiButton button : buttons)
        {
            if (button instanceof Button)
            {
                add((Button) button);
            }
        }
    }

    /**
     * Instantiates a new Button list.
     *
     * @param buttons the buttons
     */
    public ButtonList(Button... buttons)
    {
        super(Arrays.asList(buttons));
    }

    /**
     * Gets width.
     *
     * @param hgap the hgap
     * @return the width
     */
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

        if (HorizontalLayouts.contains(this.layout))
        {
            int visible = 0;
            for (Button button : this)
            {
                if (button.isVisible())
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
        }
        else
        {
            if (buttonWidth > 0)
            {
                total = buttonWidth;
            }
            for (Button button : this)
            {
                if (button.isVisible())
                {
                    total = Math.max(total, button.getWidth());
                }
            }
        }
        return total;
    }

    /**
     * Gets height based on layout
     *
     * @return the height
     */
    public int getHeight()
    {
        return getHeight(0);
    }

    /**
     * Gets height based on layout, padding with vgap.
     *
     * @param vgap the vgap
     * @return the height
     */
    public int getHeight(int vgap)
    {
        if (this.isEmpty())
        {
            return 0;
        }

        int total = 0;

        if (VerticalLayouts.contains(this.layout))
        {
            int visible = 0;
            for (Button button : this)
            {
                if (button.isVisible())
                {
                    total += button.getHeight();
                    visible++;
                }
            }

            if (visible > 1)
            {
                total += (vgap * (visible - 1));
            }
        }
        else
        {
            for (Button button : this)
            {
                if (button.isVisible())
                {
                    total = Math.max(total, button.getHeight() + vgap);
                }
            }
        }

        return total;
    }

    /**
     * Gets left x.
     *
     * @return the left x
     */
    public int getLeftX()
    {
        int left = Integer.MAX_VALUE;
        for (Button button : this)
        {
            if (button.isVisible())
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

    /**
     * Gets top y.
     *
     * @return the top y
     */
    public int getTopY()
    {
        int top = Integer.MAX_VALUE;
        for (Button button : this)
        {
            if (button.isVisible())
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

    /**
     * Gets bottom y.
     *
     * @return the bottom y
     */
    public int getBottomY()
    {
        int bottom = Integer.MIN_VALUE;
        for (Button button : this)
        {
            if (button.isVisible())
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

    /**
     * Gets right x.
     *
     * @return the right x
     */
    public int getRightX()
    {
        int right = 0;
        for (Button button : this)
        {
            if (button.isVisible())
            {
                right = Math.max(right, button.getX() + button.getWidth());
            }
        }
        return right;
    }

    /**
     * Find button button.
     *
     * @param id the id
     * @return the button
     */
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

    /**
     * Sets layout.
     *
     * @param layout    the layout
     * @param direction the direction
     */
    public void setLayout(Layout layout, Direction direction)
    {
        this.layout = layout;
        this.direction = direction;
    }

    /**
     * Layout horizontal button list.
     *
     * @param startX      the start x
     * @param y           the y
     * @param leftToRight the left to right
     * @param hgap        the hgap
     * @return the button list
     */
    public ButtonList layoutHorizontal(int startX, final int y, boolean leftToRight, int hgap)
    {
        this.layout = Layout.Horizontal;
        this.direction = leftToRight ? Direction.LeftToRight : Direction.RightToLeft;

        Button last = null;
        for (Button button : this)
        {
            if (!button.visible)
            {
                continue;
            }

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
        this.layout = Layout.Horizontal;
        return this;
    }

    /**
     * Layout vertical button list.
     *
     * @param x           the x
     * @param startY      the start y
     * @param leftToRight the left to right
     * @param vgap        the vgap
     * @return the button list
     */
    public ButtonList layoutVertical(final int x, int startY, boolean leftToRight, int vgap)
    {
        this.layout = Vertical;
        this.direction = leftToRight ? Direction.LeftToRight : Direction.RightToLeft;

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
        this.layout = Vertical;

        return this;
    }

    /**
     * Layout centered vertical button list.
     *
     * @param x           the x
     * @param centerY     the center y
     * @param leftToRight the left to right
     * @param vgap        the vgap
     * @return the button list
     */
    public ButtonList layoutCenteredVertical(final int x, final int centerY, final boolean leftToRight, final int vgap)
    {
        this.layout = CenteredVertical;
        int height = getHeight(vgap);
        layoutVertical(x, centerY - (height / 2), leftToRight, vgap);
        return this;
    }

    /**
     * Layout centered horizontal button list.
     *
     * @param centerX     the center x
     * @param y           the y
     * @param leftToRight the left to right
     * @param hgap        the hgap
     * @return the button list
     */
    public ButtonList layoutCenteredHorizontal(final int centerX, final int y, final boolean leftToRight, final int hgap)
    {
        this.layout = Layout.CenteredHorizontal;
        int width = getWidth(hgap);
        layoutHorizontal(centerX - (width / 2), y, leftToRight, hgap);

        return this;
    }

    /**
     * Layout distributed horizontal button list.
     *
     * @param leftX       the left x
     * @param y           the y
     * @param rightX      the right x
     * @param leftToRight the left to right
     * @return the button list
     */
    public ButtonList layoutDistributedHorizontal(final int leftX, final int y, final int rightX, final boolean leftToRight)
    {
        this.layout = Layout.DistributedHorizontal;
        if (this.size() == 0)
        {
            return this;
        }

        int width = getWidth(0);
        int filler = (rightX - leftX) - width;
        int gaps = this.size() - 1;
        int hgap = gaps == 0 ? 0 : (filler >= gaps) ? filler / gaps : 0;

        if (leftToRight)
        {
            layoutHorizontal(leftX, y, true, hgap);
        }
        else
        {
            layoutHorizontal(rightX, y, false, hgap);
        }
        this.layout = Layout.DistributedHorizontal;
        return this;
    }

    /**
     * Layout filled horizontal button list.
     *
     * @param fr          the fr
     * @param leftX       the left x
     * @param y           the y
     * @param rightX      the right x
     * @param hgap        the hgap
     * @param leftToRight the left to right
     * @return the button list
     */
    public ButtonList layoutFilledHorizontal(FontRenderer fr, final int leftX, final int y, final int rightX, final int hgap, final boolean leftToRight)
    {
        this.layout = Layout.FilledHorizontal;
        if (this.size() == 0)
        {
            return this;
        }

        this.equalizeWidths(fr);

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
        this.layout = Layout.FilledHorizontal;
        return this;
    }

    /**
     * Sets fit widths.
     *
     * @param fr the fr
     */
    public void setFitWidths(FontRenderer fr)
    {
        fitWidths(fr);
    }

    /**
     * Is horizontal boolean.
     *
     * @return the boolean
     */
    public boolean isHorizontal()
    {
        return layout != Vertical && layout != CenteredVertical;
    }

    /**
     * Sets enabled.
     *
     * @param enabled the enabled
     * @return the enabled
     */
    public ButtonList setEnabled(boolean enabled)
    {
        for (Button button : this)
        {
            button.setEnabled(enabled);
        }
        return this;
    }

    /**
     * Sets options.
     *
     * @param enabled        the enabled
     * @param drawBackground the draw background
     * @param drawFrame      the draw frame
     * @return the options
     */
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

    /**
     * Sets default style.
     *
     * @param defaultStyle the default style
     * @return the default style
     */
    public ButtonList setDefaultStyle(boolean defaultStyle)
    {
        for (Button button : this)
        {
            button.setDefaultStyle(defaultStyle);
        }
        return this;
    }

    /**
     * Draw button list.
     *
     * @param minecraft the minecraft
     * @param mouseX    the mouse x
     * @param mouseY    the mouse y
     * @return the button list
     */
    public ButtonList draw(Minecraft minecraft, int mouseX, int mouseY)
    {
        for (Button button : this)
        {
            button.drawButton(minecraft, mouseX, mouseY, 0f);
        }
        return this;
    }

    /**
     * Sets heights.
     *
     * @param height the height
     */
    public void setHeights(int height)
    {
        for (Button button : this)
        {
            button.setHeight(height);
        }
    }

    /**
     * Sets widths.
     *
     * @param width the width
     */
    public void setWidths(int width)
    {
        for (Button button : this)
        {
            button.setWidth(width);
        }
    }

    /**
     * Fit widths.
     *
     * @param fr the fr
     */
    public void fitWidths(FontRenderer fr)
    {
        for (Button button : this)
        {
            button.fitWidth(fr);
        }
    }

    /**
     * Sets draw buttons.
     *
     * @param draw the draw
     */
    public void setDrawButtons(boolean draw)
    {
        for (Button button : this)
        {
            button.setDrawButton(draw);
        }
    }

    /**
     * Equalize widths.
     *
     * @param fr the fr
     */
    public void equalizeWidths(FontRenderer fr)
    {
        int maxWidth = 0;
        for (Button button : this)
        {
            if (button.isVisible())
            {
                button.fitWidth(fr);
                maxWidth = Math.max(maxWidth, button.getWidth());
            }
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
            setWidths(maxWidth); // same result as equalizeWidths
        }
        else
        {
            totalWidth = getWidth(hgap);
        }

        if (totalWidth < maxTotalWidth)
        {
            // Pad the buttons to getTexture up to maxTotalWidth
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

    /**
     * Gets visible button count.
     *
     * @return the visible button count
     */
    public int getVisibleButtonCount()
    {
        int count = 0;
        for (Button button : this)
        {
            if (button.visible)
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Sets label.
     *
     * @param label the label
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * Reverse button list.
     *
     * @return the button list
     */
    public ButtonList reverse()
    {
        Collections.reverse(this);
        return this;
    }

    /**
     * The enum Layout.
     */
    public enum Layout
    {
        /**
         * Horizontal layout.
         */
        Horizontal, /**
     * Vertical layout.
     */
    Vertical, /**
     * Centered horizontal layout.
     */
    CenteredHorizontal, /**
     * Centered vertical layout.
     */
    CenteredVertical, /**
     * Distributed horizontal layout.
     */
    DistributedHorizontal, /**
     * Filled horizontal layout.
     */
    FilledHorizontal;
    }

    /**
     * The enum Direction.
     */
    public enum Direction
    {
        /**
         * Left to right direction.
         */
        LeftToRight, /**
     * Right to left direction.
     */
    RightToLeft
    }
}
