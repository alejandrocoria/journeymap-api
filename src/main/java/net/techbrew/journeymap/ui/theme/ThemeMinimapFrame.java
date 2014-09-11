package net.techbrew.journeymap.ui.theme;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;

/**
 * Created by Mark on 9/7/2014.
 */
public class ThemeMinimapFrame
{
    private Theme theme;

    private final String resourcePattern;
    private final TextureImpl textureTopLeft;
    private final TextureImpl textureTop;
    private final TextureImpl textureTopRight;
    private final TextureImpl textureRight;
    private final TextureImpl textureBottomRight;
    private final TextureImpl textureBottom;
    private final TextureImpl textureBottomLeft;
    private final TextureImpl textureLeft;
    private final TextureImpl textureCircle;
    private final Color circleColor;

    private double x;
    private double y;
    private int width;
    private int height;

    public ThemeMinimapFrame(Theme theme, int size)
    {
        this.theme = theme;
        this.width = size;
        this.height = size;
        resourcePattern = "minimap/" + theme.minimap.prefix + "%s.png";

        textureTopLeft = getTexture("topleft", theme.minimap.topLeft);
        textureTop = getTexture("top", width - (theme.minimap.topLeft.width/2) - (theme.minimap.topRight.width/2), theme.minimap.top.height, true);
        textureTopRight = getTexture("topright", theme.minimap.topRight);
        textureRight = getTexture("right", theme.minimap.right.width, height - (theme.minimap.topRight.height/2) - (theme.minimap.bottomRight.height/2), true);
        textureBottomRight = getTexture("bottomright", theme.minimap.bottomRight);
        textureBottom = getTexture("bottom", width - (theme.minimap.bottomLeft.width/2) - (theme.minimap.bottomRight.width/2), theme.minimap.bottom.height, true);
        textureBottomLeft = getTexture("bottomleft", theme.minimap.bottomLeft);
        textureLeft = getTexture("left", theme.minimap.left.width, height - (theme.minimap.topLeft.height/2) - (theme.minimap.bottomLeft.height/2), true);

        textureCircle = TextureCache.instance().getScaledCopy("scaledMinimap",
                TextureCache.instance().getMinimapLargeCircle(), size, size,
                JourneyMap.getMiniMapProperties().frameAlpha.get()/255f);

        circleColor = Theme.getColor(theme.minimap.circleFrameColor);

    }

    public void drawSquare(final double x, final double y)
    {
        DrawUtil.drawClampedImage(textureTop, x + (textureTopLeft.width  / 2D), y - (textureTop.height  / 2D), 1, 0);
        DrawUtil.drawClampedImage(textureLeft, x - (textureLeft.width  / 2D), y + (textureTopLeft.height  / 2D), 1, 0);
        DrawUtil.drawClampedImage(textureTopLeft, x - (textureTopLeft.width  / 2D), y - (textureTopLeft.height  / 2D), 1, 0);
        DrawUtil.drawClampedImage(textureBottom, x + (textureBottomLeft.width  / 2D), y + height - (textureBottom.height  / 2D), 1, 0);
        DrawUtil.drawClampedImage(textureRight, x + width - (textureRight.width  / 2D), y + (textureTopRight.height  / 2D), 1, 0);
        DrawUtil.drawClampedImage(textureTopLeft, x - (textureTopLeft.width  / 2D), y - (textureTopLeft.height  / 2D), 1, 0);
        DrawUtil.drawClampedImage(textureTopRight, x + width - (textureTopRight.width  / 2D), y - (textureTopRight.height  / 2D), 1, 0);
        DrawUtil.drawClampedImage(textureBottomLeft, x - (textureBottomLeft.width  / 2D), y + height - (textureBottomLeft.height  / 2D), 1, 0);
        DrawUtil.drawClampedImage(textureBottomRight, x + width - (textureBottomRight.width  / 2D), y + height - (textureBottomRight.height  / 2D), 1, 0);
    }

    public void drawCircle(final double x, final double y) {
        DrawUtil.drawColoredImage(textureCircle, 255, circleColor, x, y, 0);
    }

    private TextureImpl getTexture(String suffix, Theme.ImageSpec imageSpec)
    {
        return getTexture(suffix, imageSpec.width, imageSpec.height, false);
    }

    private TextureImpl getTexture(String suffix, int width, int height, boolean resize)
    {
        return TextureCache.instance().getThemeTexture(theme, String.format(resourcePattern, suffix), width, height, resize, JourneyMap.getMiniMapProperties().frameAlpha.get()/255f);
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }
}
