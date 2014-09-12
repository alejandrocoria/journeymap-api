package net.techbrew.journeymap.ui.theme;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Created by Mark on 9/7/2014.
 */
public class ThemeMinimapFrame
{
    private final Theme theme;
    private final Theme.Minimap.MinimapSpec minimapSpec;

    private final String resourcePattern;
    private TextureImpl textureTopLeft;
    private TextureImpl textureTop;
    private TextureImpl textureTopRight;
    private TextureImpl textureRight;
    private TextureImpl textureBottomRight;
    private TextureImpl textureBottom;
    private TextureImpl textureBottomLeft;
    private TextureImpl textureLeft;

    private TextureImpl textureCircle;
    private TextureImpl textureCircleMask;

    private double x;
    private double y;
    private int width;
    private int height;
    private Color frameColor;
    private float frameAlpha;
    private boolean isSquare;

    public ThemeMinimapFrame(Theme theme, Theme.Minimap.MinimapSpec minimapSpec, int size)
    {
        this.theme = theme;
        this.minimapSpec = minimapSpec;
        this.width = size;
        this.height = size;
        this.frameColor = Theme.getColor(minimapSpec.frameColor);
        this.frameAlpha = JourneyMap.getMiniMapProperties().frameAlpha.get()/255f;

        if(minimapSpec instanceof Theme.Minimap.MinimapSquare)
        {
            isSquare = true;
            Theme.Minimap.MinimapSquare minimapSquare = (Theme.Minimap.MinimapSquare) minimapSpec;
            resourcePattern = "minimap/square/" + minimapSquare.prefix + "%s.png";

            textureTopLeft = getTexture("topleft", minimapSquare.topLeft);
            textureTop = getTexture("top", width - (minimapSquare.topLeft.width / 2) - (minimapSquare.topRight.width / 2), minimapSquare.top.height, true, false);
            textureTopRight = getTexture("topright", minimapSquare.topRight);
            textureRight = getTexture("right", minimapSquare.right.width, height - (minimapSquare.topRight.height / 2) - (minimapSquare.bottomRight.height / 2), true, false);
            textureBottomRight = getTexture("bottomright", minimapSquare.bottomRight);
            textureBottom = getTexture("bottom", width - (minimapSquare.bottomLeft.width / 2) - (minimapSquare.bottomRight.width / 2), minimapSquare.bottom.height, true, false);
            textureBottomLeft = getTexture("bottomleft", minimapSquare.bottomLeft);
            textureLeft = getTexture("left", minimapSquare.left.width, height - (minimapSquare.topLeft.height / 2) - (minimapSquare.bottomLeft.height / 2), true, false);
        }
        else
        {
            Theme.Minimap.MinimapCircle minimapCircle = (Theme.Minimap.MinimapCircle) minimapSpec;
            int imgSize = size<=256 ? 256 : 512;
            resourcePattern = "minimap/circle/" + minimapCircle.prefix + "%s.png";

            TextureImpl tempMask = getTexture("mask_" + imgSize, imgSize, imgSize, false, true);
            textureCircleMask = TextureCache.instance().getScaledCopy("scaledCircleMask", tempMask, size, size, 1f);

            TextureImpl tempCircle = getTexture("rim_" + imgSize, imgSize, imgSize, false, true);
            textureCircle = TextureCache.instance().getScaledCopy("scaledCircleRim", tempCircle, size, size, frameAlpha);
        }
    }

    public void setPosition(final double x, final double y)
    {
        this.x = x;
        this.y = y;
    }

    public void drawMask()
    {
        if(isSquare)
        {
            DrawUtil.drawRectangle(x, y, this.width, this.height, Color.white, 255);
        }
        else
        {
            DrawUtil.drawQuad(textureCircleMask, x, y, this.width, this.height, 0, null, 1f, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, true);
        }
    }

    public void drawFrame()
    {
        if(isSquare)
        {
            drawSquare(x,y);
        }
        else
        {
            DrawUtil.drawQuad(textureCircle, x, y, this.width, this.height, 0, null, 1f, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, true);
        }
    }

    protected void drawSquare(final double x, final double y)
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
        DrawUtil.drawColoredImage(textureCircle, 255, frameColor, x, y, 0);
    }

    private TextureImpl getTexture(String suffix, Theme.ImageSpec imageSpec)
    {
        return getTexture(suffix, imageSpec.width, imageSpec.height, false, false);
    }

    private TextureImpl getTexture(String suffix, int width, int height, boolean resize, boolean retain)
    {
        return TextureCache.instance().getThemeTexture(theme, String.format(resourcePattern, suffix), width, height, resize, frameAlpha, retain);
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
