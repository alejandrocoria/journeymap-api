package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;

/**
 * Encapsulates all the layout and display specifics for rendering the Minimap
 * given a Shape, Position, screen size, and user preferences.
*/
public class DisplayVars {

    public enum Position {
        TopRight("MiniMap.position_topright"),
        BottomRight("MiniMap.position_bottomright"),
        BottomLeft("MiniMap.position_bottomleft"),
        TopLeft("MiniMap.position_topleft");

        public final String label;
        private Position(String label){
            this.label = label;
        }
    }

    public enum Shape {
        TinySquare("MiniMap.shape_tinysquare"),
        SmallSquare("MiniMap.shape_smallsquare"),
        LargeSquare("MiniMap.shape_largesquare"),
        SmallCircle("MiniMap.shape_smallcircle"),
        LargeCircle("MiniMap.shape_largecircle");
        public final String label;
        private Shape(String label){
            this.label = label;
        }

        public static Shape[] Enabled = {TinySquare, SmallSquare, LargeSquare};
    }

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
            if(forceUnicode)
            {
                fontRenderer =  Minecraft.getMinecraft().fontRenderer;
                isUnicode = fontRenderer.getUnicodeFlag();
                if(!isUnicode)
                {
                    fontRenderer.setUnicodeFlag(true);
                }
            }
            DrawUtil.drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, color, alpha, fontScale, fontShadow);
            if(forceUnicode && !isUnicode)
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
    final double minimapSize,textureX,textureY;
    final double minimapOffset,translateX,translateY;
    final double marginX, marginY, scissorX, scissorY;
    final double viewPortPadX;
    final double viewPortPadY;
    final boolean showFps;
    final LabelVars labelFps, labelLocation, labelBiome;

    boolean forceUnicode;

    DisplayVars(Minecraft mc, Shape shape, Position position, double _fontScale)
    {
        forceUnicode = (PropertyManager.getBooleanProp(PropertyManager.Key.PREF_MINIMAP_FORCEUNICODE));
        showFps = (PropertyManager.getBooleanProp(PropertyManager.Key.PREF_MINIMAP_SHOWFPS));

        final boolean useUnicode = (forceUnicode || mc.fontRenderer.getUnicodeFlag());
        this.shape = shape;
        this.position = position;
        this.fontScale = _fontScale * (useUnicode ? 2 : 1);
        displayWidth = mc.displayWidth;
        displayHeight = mc.displayHeight;
        scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);

        final boolean useFontShadow = false;

        double labelHeight = DrawUtil.getLabelHeight(mc.fontRenderer, useFontShadow) * (useUnicode ? this.fontScale*.7 : this.fontScale);
        double labelFpsYOffset = 4;
        double labelBiomeYOffset =  -3;
        double labelLocationYOffset = labelBiomeYOffset + -labelHeight;
        double bottomTextureYMargin = 0;

        boolean scissorFps = true;
        boolean scissorLocation = false;
        boolean scissorBiome = true;

        boolean labelsOutside = false;

        DrawUtil.VAlign valignFps = DrawUtil.VAlign.Below;
        DrawUtil.VAlign valignLocation = DrawUtil.VAlign.Above;
        DrawUtil.VAlign valignBiome = DrawUtil.VAlign.Above;

        switch(shape){
            case LargeCircle: {
                drawScale = 1f;
                borderTexture = TextureCache.instance().getMinimapLargeCircle();
                maskTexture = TextureCache.instance().getMinimapLargeCircleMask();
                minimapSize = 512;
                marginX=3;
                marginY=3;
                viewPortPadX=5;
                viewPortPadY=5;
                if(fontScale==1){
                    bottomTextureYMargin = 10;
                } else {
                    bottomTextureYMargin = 20;
                }
                break;
            }
            case SmallCircle: {
                drawScale = 0.5f;
                borderTexture = TextureCache.instance().getMinimapSmallCircle();
                maskTexture = TextureCache.instance().getMinimapSmallCircleMask();
                minimapSize = 256;
                marginX=2;
                marginY=2;
                viewPortPadX=5;
                viewPortPadY=5;
                if(fontScale==1){
                    bottomTextureYMargin = 14;
                } else {
                    bottomTextureYMargin = 24;
                }
                break;
            }
            case LargeSquare: {
                drawScale = 1f;
                borderTexture = TextureCache.instance().getMinimapLargeSquare();
                maskTexture = null;
                minimapSize = 512;
                marginX=0;
                marginY=0;
                viewPortPadX=5;
                viewPortPadY=5;
                labelFpsYOffset = 5;
                labelLocationYOffset = -5;
                labelBiomeYOffset = labelLocationYOffset - labelHeight;
                break;
            }
            case SmallSquare:
            {
                drawScale = 0.5f;
                borderTexture = TextureCache.instance().getMinimapSmallSquare();
                maskTexture = null;
                minimapSize = 256;
                marginX=0;
                marginY=0;
                viewPortPadX=4;
                viewPortPadY=5;
                labelFpsYOffset = 5;
                labelLocationYOffset = -5;
                labelBiomeYOffset = labelLocationYOffset - labelHeight;
                break;
            }
            case TinySquare:
            default: {
                drawScale = 0.5f;
                borderTexture = TextureCache.instance().getMinimapTinySquare();
                maskTexture = null;
                minimapSize = 128;
                marginX=0;
                marginY=0;
                viewPortPadX=2;
                viewPortPadY=2;
                valignLocation = DrawUtil.VAlign.Below;
                valignBiome = DrawUtil.VAlign.Below;
                labelFpsYOffset = 3;
                labelLocationYOffset = 1;
                labelBiomeYOffset = labelLocationYOffset + labelHeight;
                scissorLocation = false;
                scissorBiome = false;
                labelsOutside = true;
                break;
            }
        }

        minimapOffset = minimapSize*0.5;
        final int textureOffsetX = 0; //(borderTexture.width-minimapSize)/2;

        switch(position){
            case BottomRight : {
                textureX = mc.displayWidth - borderTexture.width + textureOffsetX - marginX;
                textureY = mc.displayHeight-(borderTexture.height) - marginY - bottomTextureYMargin;
                translateX = (mc.displayWidth/2)-minimapOffset;
                translateY = (mc.displayHeight/2)-minimapOffset - bottomTextureYMargin;
                scissorX = mc.displayWidth-minimapSize-marginX;
                scissorY = marginY + bottomTextureYMargin;
                if(labelsOutside)
                {
                    labelLocationYOffset = -minimapSize;
                    valignLocation = DrawUtil.VAlign.Above;
                    valignBiome = DrawUtil.VAlign.Above;
                    labelBiomeYOffset = labelLocationYOffset - labelHeight;
                }
                break;
            }
            case TopLeft : {
                textureX = -textureOffsetX + marginX;
                textureY =  marginY;
                translateX = -(mc.displayWidth/2)+minimapOffset;
                translateY = -(mc.displayHeight/2)+minimapOffset;
                scissorX = marginX;
                scissorY = mc.displayHeight-minimapSize-marginY;
                break;
            }
            case BottomLeft : {
                textureX = -textureOffsetX + marginX;
                textureY = mc.displayHeight-(borderTexture.height) - marginY - bottomTextureYMargin;
                translateX = -(mc.displayWidth/2)+minimapOffset;
                translateY = (mc.displayHeight/2)-minimapOffset - bottomTextureYMargin;
                scissorX = marginX;
                scissorY = marginY + bottomTextureYMargin;
                if(labelsOutside)
                {
                    labelLocationYOffset = -minimapSize;
                    valignLocation = DrawUtil.VAlign.Above;
                    valignBiome = DrawUtil.VAlign.Above;
                    labelBiomeYOffset = labelLocationYOffset - labelHeight;
                }
                break;
            }
            case TopRight :
            default : {
                textureX = mc.displayWidth - borderTexture.width + textureOffsetX - marginX;
                textureY = marginY;
                translateX = (mc.displayWidth/2)-minimapOffset;
                translateY = -(mc.displayHeight/2)+minimapOffset;
                scissorX = mc.displayWidth-minimapSize-marginX;
                scissorY = mc.displayHeight-minimapSize-marginY;
                break;
            }
        }

        double centerX = textureX + (minimapSize/2);
        double topY = textureY;
        double bottomY = textureY + minimapSize;
        labelFps = new LabelVars(centerX, topY + labelFpsYOffset, DrawUtil.HAlign.Center, valignFps, fontScale, scissorFps, useFontShadow);
        labelLocation = new LabelVars(centerX, bottomY + labelLocationYOffset, DrawUtil.HAlign.Center, valignLocation, fontScale, scissorLocation, useFontShadow);
        labelBiome = new LabelVars(centerX, bottomY + labelBiomeYOffset, DrawUtil.HAlign.Center, valignBiome, fontScale, scissorBiome, useFontShadow);

    }
}
