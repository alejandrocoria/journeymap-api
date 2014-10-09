package net.techbrew.journeymap.ui.theme;

import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.minimap.ReticleOrientation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by Mark on 9/7/2014.
 */
public class ThemeMinimapFrame
{
    private final Theme theme;
    private final Theme.Minimap.MinimapSpec minimapSpec;
    private final ReticleOrientation reticleOrientation;

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
    private TextureImpl textureCompassPoint;

    private double x;
    private double y;
    private int width;
    private int height;
    private Color frameColor;
    private float frameAlpha;
    private boolean isSquare;
    private boolean showReticle;
    private int reticleAlpha;
    private int reticleHeadingAlpha;
    private double reticleThickness;
    private double reticleHeadingThickness;
    private double reticleSegmentLength;
    private double reticleOffset;
    private Color reticleColor;
    private Rectangle.Double frameBounds;

    public ThemeMinimapFrame(Theme theme, Theme.Minimap.MinimapSpec minimapSpec, MiniMapProperties miniMapProperties, int width, int height)
    {
        this.theme = theme;
        this.minimapSpec = minimapSpec;
        this.width = width;
        this.height = height;
        this.frameColor = Theme.getColor(minimapSpec.frameColor);
        this.frameAlpha = Math.max(0f, Math.min(1f, miniMapProperties.frameAlpha.get() / 100f));
        this.reticleOrientation = miniMapProperties.reticleOrientation.get();

        if (minimapSpec instanceof Theme.Minimap.MinimapSquare)
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
            int imgSize = width <= 256 ? 256 : 512;
            resourcePattern = "minimap/circle/" + minimapCircle.prefix + "%s.png";

            TextureImpl tempMask = getTexture("mask_" + imgSize, imgSize, imgSize, false, true);
            textureCircleMask = TextureCache.instance().getScaledCopy("scaledCircleMask", tempMask, width, width, 1f);

            TextureImpl tempCircle = getTexture("rim_" + imgSize, imgSize, imgSize, false, true);
            textureCircle = TextureCache.instance().getScaledCopy("scaledCircleRim", tempCircle, width, width, frameAlpha);
        }

        if (minimapSpec.compassPoint != null && minimapSpec.compassPoint.width > 0 && minimapSpec.compassPoint.height > 0)
        {
            textureCompassPoint = getTexture("compass_point", minimapSpec.compassPoint);
        }

        this.showReticle = miniMapProperties.showReticle.get();
        this.reticleColor = Theme.getColor(minimapSpec.reticleColor);
        this.reticleAlpha = minimapSpec.reticleAlpha;
        this.reticleHeadingAlpha = minimapSpec.reticleHeadingAlpha;
        this.reticleThickness = minimapSpec.reticleThickness;
        this.reticleHeadingThickness = minimapSpec.reticleHeadingThickness;
        this.reticleOffset = minimapSpec.reticleOffset;
        if (isSquare)
        {
            reticleSegmentLength += (width * .75) + reticleOffset;
        }
        else
        {
            reticleSegmentLength += (height * .5) + reticleOffset;
        }

    }

    public void setPosition(final double x, final double y)
    {
        this.x = x;
        this.y = y;
        this.frameBounds = new Rectangle2D.Double(x, y, width, height);
    }

    public void drawMask()
    {
        if (isSquare)
        {
            DrawUtil.drawRectangle(x, y, this.width, this.height, Color.white, 255);
        }
        else
        {
            DrawUtil.drawQuad(textureCircleMask, x, y, this.width, this.height, 0, null, 1f, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, true);
        }
    }

    public void drawReticle()
    {
        reticleHeadingAlpha = 255;
        if (showReticle && reticleAlpha > 0)
        {
            double centerX = x + (width / 2);
            double centerY = y + (height / 2);

            double thick = reticleThickness;
            int alpha = reticleAlpha;
            if (reticleOrientation == ReticleOrientation.Compass)
            {
                thick = reticleHeadingThickness;
                alpha = reticleHeadingAlpha;
            }

            // North
            if (thick > 0 && alpha > 0)
            {
                DrawUtil.drawRectangle(centerX - (thick / 2), centerY - reticleSegmentLength - 16, thick, reticleSegmentLength, reticleColor, alpha);
            }

            if (reticleOrientation == ReticleOrientation.PlayerHeading)
            {
                thick = reticleHeadingThickness;
                alpha = reticleHeadingAlpha;
            }
            else
            {
                thick = reticleThickness;
                alpha = reticleAlpha;
            }

            // South
            if (thick > 0 && alpha > 0)
            {
                DrawUtil.drawRectangle(centerX - (thick / 2), centerY + 16, thick, reticleSegmentLength, reticleColor, alpha);
            }
            thick = reticleThickness;
            alpha = reticleAlpha;

            // West
            if (thick > 0 && alpha > 0)
            {
                DrawUtil.drawRectangle(centerX - reticleSegmentLength - 16, centerY - (thick / 2), reticleSegmentLength, reticleThickness, reticleColor, alpha);
            }

            // East
            if (thick > 0 && alpha > 0)
            {
                DrawUtil.drawRectangle(centerX + 16, centerY - (thick / 2), reticleSegmentLength, reticleThickness, reticleColor, alpha);
            }
        }
    }

    public void drawFrame()
    {
        if (frameAlpha > 0)
        {
            if (isSquare)
            {
                DrawUtil.drawClampedImage(textureTop, frameColor, x + (textureTopLeft.width / 2D), y - (textureTop.height / 2D), 1, frameAlpha, 0);
                DrawUtil.drawClampedImage(textureLeft, frameColor, x - (textureLeft.width / 2D), y + (textureTopLeft.height / 2D), 1, frameAlpha, 0);
                DrawUtil.drawClampedImage(textureTopLeft, frameColor, x - (textureTopLeft.width / 2D), y - (textureTopLeft.height / 2D), 1, frameAlpha, 0);
                DrawUtil.drawClampedImage(textureBottom, frameColor, x + (textureBottomLeft.width / 2D), y + height - (textureBottom.height / 2D), 1, frameAlpha, 0);
                DrawUtil.drawClampedImage(textureRight, frameColor, x + width - (textureRight.width / 2D), y + (textureTopRight.height / 2D), 1, frameAlpha, 0);
                DrawUtil.drawClampedImage(textureTopLeft, frameColor, x - (textureTopLeft.width / 2D), y - (textureTopLeft.height / 2D), 1, frameAlpha, 0);
                DrawUtil.drawClampedImage(textureTopRight, frameColor, x + width - (textureTopRight.width / 2D), y - (textureTopRight.height / 2D), 1, frameAlpha, 0);
                DrawUtil.drawClampedImage(textureBottomLeft, frameColor, x - (textureBottomLeft.width / 2D), y + height - (textureBottomLeft.height / 2D), 1, frameAlpha, 0);
                DrawUtil.drawClampedImage(textureBottomRight, frameColor, x + width - (textureBottomRight.width / 2D), y + height - (textureBottomRight.height / 2D), 1, frameAlpha, 0);
            }
            else
            {
                DrawUtil.drawQuad(textureCircle, x, y, this.width, this.height, 0, frameColor, 1f, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, true);
            }
        }
    }

    public TextureImpl getCompassPoint()
    {
        return textureCompassPoint;
    }

    private TextureImpl getTexture(String suffix, Theme.ImageSpec imageSpec)
    {
        return getTexture(suffix, imageSpec.width, imageSpec.height, false, false);
    }

    private TextureImpl getTexture(String suffix, int width, int height, boolean resize, boolean retain)
    {
        return TextureCache.instance().getThemeTexture(theme, String.format(resourcePattern, suffix), width, height, resize, 1f, retain);
    }

    public Rectangle.Double getFrameBounds()
    {
        return frameBounds;
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

    public ReticleOrientation getReticleOrientation()
    {
        return reticleOrientation;
    }
}
