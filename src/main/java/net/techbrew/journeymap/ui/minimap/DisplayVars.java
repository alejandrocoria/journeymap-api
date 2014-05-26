package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.util.Arrays;

/**
 * Display variables for the Minimap.
 * <p/>
 * Encapsulates all the layout and display specifics for rendering the Minimap
 * given a Shape, Position, screen size, and user preferences.  All of the values
 * only need to be calculated once after a change of shape/position/screen size,
 * so it's done here rather than during the minimap renderloop.
 */
public class DisplayVars
{
    static final MiniMapProperties miniMapProperties = JourneyMap.getInstance().miniMapProperties;

    /**
     * Position of minimap on screen
     */
    public enum Position
    {
        TopRight("MiniMap.position_topright"),
        BottomRight("MiniMap.position_bottomright"),
        BottomLeft("MiniMap.position_bottomleft"),
        TopLeft("MiniMap.position_topleft");

        public final String label;

        public static Position getPreferred()
        {
            DisplayVars.Position position = null;
            try
            {
                position = miniMapProperties.position.get();
            }
            catch (IllegalArgumentException e)
            {
                JourneyMap.getLogger().warning("Not a valid minimap position in : " + miniMapProperties.getFile());
            }

            if (position == null)
            {
                position = Position.TopRight;
                miniMapProperties.position.set(position);
                miniMapProperties.save();
            }
            return position;
        }

        public static Position safeValueOf(String name)
        {
            Position value = null;
            try
            {
                value = Position.valueOf(name);
            }
            catch (IllegalArgumentException e)
            {
                JourneyMap.getLogger().warning("Not a valid minimap position: " + name);
            }

            if (value == null)
            {
                value = Position.TopRight;
            }
            return value;
        }

        private Position(String label)
        {
            this.label = label;
        }
    }

    /**
     * Shape (and size) of minimap
     */
    public enum Shape
    {
        SmallSquare("MiniMap.shape_smallsquare"),
        MediumSquare("MiniMap.shape_mediumsquare"),
        LargeSquare("MiniMap.shape_largesquare"),
        SmallCircle("MiniMap.shape_smallcircle"),
        LargeCircle("MiniMap.shape_largecircle");
        public final String label;

        private Shape(String label)
        {
            this.label = label;
        }

        public static Shape getPreferred()
        {
            DisplayVars.Shape shape = null;
            try
            {
                shape = miniMapProperties.shape.get();
            }
            catch (IllegalArgumentException e)
            {
                JourneyMap.getLogger().warning("Not a valid minimap shape in : " + miniMapProperties.getFile());
            }

            if (shape == null)
            {
                shape = Shape.MediumSquare;
                miniMapProperties.shape.set(shape);
                miniMapProperties.save();
            }
            return shape;
        }

        public static Shape safeValueOf(String name)
        {
            Shape value = null;
            try
            {
                value = Shape.valueOf(name);
            }
            catch (IllegalArgumentException e)
            {
                JourneyMap.getLogger().warning("Not a valid minimap shape: " + name);
            }

            if (value == null || !value.isEnabled())
            {
                value = Shape.MediumSquare;
            }
            return value;
        }

        public boolean isEnabled()
        {
            return Arrays.binarySearch(DisplayVars.Shape.Enabled, this) >= 0;
        }

        public static Shape[] Enabled = {SmallSquare, MediumSquare, LargeSquare};
    }

    /**
     * Encapsulation of label attributes.
     */
    class LabelVars
    {
        final double x;
        final double y;
        DrawUtil.HAlign hAlign;
        DrawUtil.VAlign vAlign;
        final double fontScale;
        final boolean scissor;
        final boolean fontShadow;

        private LabelVars(double x, double y, DrawUtil.HAlign hAlign, DrawUtil.VAlign vAlign, double fontScale, boolean scissor, boolean fontShadow)
        {
            this.x = x;
            this.y = y;
            this.hAlign = hAlign;
            this.vAlign = vAlign;
            this.fontScale = fontScale;
            this.scissor = scissor;
            this.fontShadow = fontShadow;
        }

        void draw(String text, Color bgColor, int bgAlpha, Color color, int alpha)
        {
            boolean isUnicode = false;
            FontRenderer fontRenderer = null;
            if (forceUnicode)
            {
                fontRenderer = Minecraft.getMinecraft().fontRenderer;
                isUnicode = fontRenderer.getUnicodeFlag();
                if (!isUnicode)
                {
                    fontRenderer.setUnicodeFlag(true);
                }
            }
            DrawUtil.drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, color, alpha, fontScale, fontShadow);
            if (forceUnicode && !isUnicode)
            {
                fontRenderer.setUnicodeFlag(false);
            }
        }
    }

    final Position position;
    final Shape shape;
    final TextureImpl borderTexture;
    final TextureImpl maskTexture;

    final float drawScale;
    final double fontScale;
    final int displayWidth;
    final int displayHeight;
    final ScaledResolution scaledResolution;
    final double minimapSize, textureX, textureY;
    final double minimapOffset, translateX, translateY;
    final double marginX, marginY, scissorX, scissorY;
    final double viewPortPadX;
    final double viewPortPadY;
    final boolean showFps;
    final LabelVars labelFps, labelLocation, labelBiome;

    boolean forceUnicode;

    /**
     * Constructor.
     *
     * @param mc             Minecraft
     * @param shape          Desired shape
     * @param position       Desired position
     * @param labelFontScale Font scale for labels
     */
    DisplayVars(Minecraft mc, Shape shape, Position position, double labelFontScale)
    {
        // Immutable member and local vars
        this.scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        this.forceUnicode = JourneyMap.getInstance().miniMapProperties.forceUnicode.get();
        this.showFps = JourneyMap.getInstance().miniMapProperties.showFps.get();
        this.shape = shape;
        this.position = position;
        this.displayWidth = mc.displayWidth;
        this.displayHeight = mc.displayHeight;
        final boolean useFontShadow = false;
        final boolean wasUnicode = mc.fontRenderer.getUnicodeFlag();
        final boolean useUnicode = (forceUnicode || wasUnicode);
        this.fontScale = labelFontScale * (useUnicode ? 2 : 1);
        final double labelHeight = DrawUtil.getLabelHeight(mc.fontRenderer, useFontShadow) * (wasUnicode ? 1 : .7) * this.fontScale;

        // Mutable local vars
        double bottomTextureYMargin = 0;
        boolean labelsOutside = false;
        boolean scissorFps = true;
        boolean scissorLocation = false;
        boolean scissorBiome = true;
        double yOffsetFps = 4;
        double yOffsetBiome = -3;
        double yOffsetLocation = yOffsetBiome + -labelHeight;
        DrawUtil.VAlign valignFps = DrawUtil.VAlign.Below;
        DrawUtil.VAlign valignLocation = DrawUtil.VAlign.Above;
        DrawUtil.VAlign valignBiome = DrawUtil.VAlign.Above;

        // Assign shape
        switch (shape)
        {
            case LargeCircle:
            {
                drawScale = 1f;
                borderTexture = TextureCache.instance().getMinimapLargeCircle();
                maskTexture = TextureCache.instance().getMinimapLargeCircleMask();
                minimapSize = 512;
                marginX = 3;
                marginY = 3;
                viewPortPadX = 5;
                viewPortPadY = 5;
                if (fontScale == 1)
                {
                    bottomTextureYMargin = 10;
                }
                else
                {
                    bottomTextureYMargin = 20;
                }
                break;
            }
            case SmallCircle:
            {
                drawScale = 0.5f;
                borderTexture = TextureCache.instance().getMinimapSmallCircle();
                maskTexture = TextureCache.instance().getMinimapSmallCircleMask();
                minimapSize = 256;
                marginX = 2;
                marginY = 2;
                viewPortPadX = 5;
                viewPortPadY = 5;
                if (fontScale == 1)
                {
                    bottomTextureYMargin = 14;
                }
                else
                {
                    bottomTextureYMargin = 24;
                }
                break;
            }
            case LargeSquare:
            {
                drawScale = 1f;
                borderTexture = TextureCache.instance().getMinimapLargeSquare();
                maskTexture = null;
                minimapSize = 512;
                marginX = 0;
                marginY = 0;
                viewPortPadX = 5;
                viewPortPadY = 5;
                yOffsetFps = 5;
                yOffsetLocation = -5;
                yOffsetBiome = yOffsetLocation - labelHeight;
                break;
            }
            case SmallSquare:
            {
                drawScale = 0.5f;
                borderTexture = TextureCache.instance().getMinimapSmallSquare();
                maskTexture = null;
                minimapSize = 128;
                marginX = 0;
                marginY = 0;
                viewPortPadX = 2;
                viewPortPadY = 2;
                valignLocation = DrawUtil.VAlign.Below;
                valignBiome = DrawUtil.VAlign.Below;
                yOffsetFps = 3;
                yOffsetLocation = 1;
                yOffsetBiome = yOffsetLocation + labelHeight;
                scissorLocation = false;
                scissorBiome = false;
                labelsOutside = true;
                break;
            }
            case MediumSquare:
            default:
            {
                drawScale = 0.5f;
                borderTexture = TextureCache.instance().getMinimapMediumSquare();
                maskTexture = null;
                minimapSize = 256;
                marginX = 0;
                marginY = 0;
                viewPortPadX = 4;
                viewPortPadY = 5;
                yOffsetFps = 5;
                yOffsetLocation = -5;
                yOffsetBiome = yOffsetLocation - labelHeight;
                break;
            }
        }

        minimapOffset = minimapSize * 0.5;

        // Assign position
        switch (position)
        {
            case BottomRight:
            {
                textureX = mc.displayWidth - borderTexture.width - marginX;
                textureY = mc.displayHeight - (borderTexture.height) - marginY - bottomTextureYMargin;
                translateX = (mc.displayWidth / 2) - minimapOffset;
                translateY = (mc.displayHeight / 2) - minimapOffset - bottomTextureYMargin;
                scissorX = mc.displayWidth - minimapSize - marginX;
                scissorY = marginY + bottomTextureYMargin;
                if (labelsOutside)
                {
                    yOffsetLocation = -minimapSize;
                    valignLocation = DrawUtil.VAlign.Above;
                    valignBiome = DrawUtil.VAlign.Above;
                    yOffsetBiome = yOffsetLocation - labelHeight;
                }
                break;
            }
            case TopLeft:
            {
                textureX = marginX;
                textureY = marginY;
                translateX = -(mc.displayWidth / 2) + minimapOffset;
                translateY = -(mc.displayHeight / 2) + minimapOffset;
                scissorX = marginX;
                scissorY = mc.displayHeight - minimapSize - marginY;
                break;
            }
            case BottomLeft:
            {
                textureX = marginX;
                textureY = mc.displayHeight - (borderTexture.height) - marginY - bottomTextureYMargin;
                translateX = -(mc.displayWidth / 2) + minimapOffset;
                translateY = (mc.displayHeight / 2) - minimapOffset - bottomTextureYMargin;
                scissorX = marginX;
                scissorY = marginY + bottomTextureYMargin;
                if (labelsOutside)
                {
                    yOffsetLocation = -minimapSize;
                    valignLocation = DrawUtil.VAlign.Above;
                    valignBiome = DrawUtil.VAlign.Above;
                    yOffsetBiome = yOffsetLocation - labelHeight;
                }
                break;
            }
            case TopRight:
            default:
            {
                textureX = mc.displayWidth - borderTexture.width + marginX;
                textureY = marginY;
                translateX = (mc.displayWidth / 2) - minimapOffset;
                translateY = -(mc.displayHeight / 2) + minimapOffset;
                scissorX = mc.displayWidth - minimapSize - marginX;
                scissorY = mc.displayHeight - minimapSize - marginY;
                break;
            }
        }

        double centerX = textureX + (minimapSize / 2);
        double topY = textureY;
        double bottomY = textureY + minimapSize;
        labelFps = new LabelVars(centerX, topY + yOffsetFps, DrawUtil.HAlign.Center, valignFps, fontScale, scissorFps, useFontShadow);
        labelLocation = new LabelVars(centerX, bottomY + yOffsetLocation, DrawUtil.HAlign.Center, valignLocation, fontScale, scissorLocation, useFontShadow);
        labelBiome = new LabelVars(centerX, bottomY + yOffsetBiome, DrawUtil.HAlign.Center, valignBiome, fontScale, scissorBiome, useFontShadow);
    }
}
