package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;

/**
* Created by mwoodman on 12/18/13.
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

    final Position position;
    final Shape shape;
    final TextureImpl borderTexture;
    final TextureImpl maskTexture;

    final float drawScale;
    final double fontScale;
    final int displayWidth;
    final int displayHeight;
    final ScaledResolution scaledResolution;
    final int minimapSize,textureX,textureY;
    final double minimapOffset,translateX,translateY;
    final int marginX,marginY,scissorX,scissorY, labelX, topLabelY, bottomLabelY;
    final int viewPortPadX;
    final int viewPortPadY;

    DisplayVars(Minecraft mc, Shape shape, Position position, double fontScale){
        this.shape = shape;
        this.position = position;
        this.fontScale = fontScale;
        displayWidth = mc.displayWidth;
        displayHeight = mc.displayHeight;
        scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);

        int topLabelYOffset = 0;
        int bottomLabelYOffset = 0;
        int bottomTextureYMargin = 0;

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
                    topLabelYOffset = 6;
                    bottomLabelYOffset = 4;
                    bottomTextureYMargin = 10;
                } else {
                    topLabelYOffset = 9;
                    bottomLabelYOffset = 6;
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
                    topLabelYOffset = 6;
                    bottomLabelYOffset = 6;
                    bottomTextureYMargin = 14;
                } else {
                    topLabelYOffset = 9;
                    bottomLabelYOffset = 10;
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
                if(fontScale==1){
                    topLabelYOffset = 7;
                    bottomLabelYOffset = -6 - mc.fontRenderer.FONT_HEIGHT;
                } else {
                    topLabelYOffset = 9;
                    bottomLabelYOffset = -14 -mc.fontRenderer.FONT_HEIGHT;
                }
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
                if(fontScale==1){
                    topLabelYOffset = 7;
                    bottomLabelYOffset = -6 - mc.fontRenderer.FONT_HEIGHT;
                } else {
                    topLabelYOffset = 9;
                    bottomLabelYOffset = -14 -mc.fontRenderer.FONT_HEIGHT;
                }
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
                if(fontScale==1){
                    topLabelYOffset = fontOffset - 3;
                    bottomLabelYOffset = -fontOffset + 3;
                } else {
                    topLabelYOffset = fontOffset - 6;
                    bottomLabelYOffset = -fontOffset + 6;
                }
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
                labelX = mc.displayWidth-(minimapSize/2);
                topLabelY = mc.displayHeight-minimapSize+topLabelYOffset-bottomTextureYMargin;
                bottomLabelY = mc.displayHeight-marginY-marginY+bottomLabelYOffset-bottomTextureYMargin;
                break;
            }
            case TopLeft : {
                textureX = -textureOffsetX + marginX;
                textureY =  marginY;
                translateX = -(mc.displayWidth/2)+minimapOffset;
                translateY = -(mc.displayHeight/2)+minimapOffset;
                scissorX = 0+marginX;
                scissorY = mc.displayHeight-minimapSize-marginY;
                labelX = minimapSize/2;
                topLabelY = marginY+topLabelYOffset;
                bottomLabelY = minimapSize+bottomLabelYOffset;
                break;
            }
            case BottomLeft : {
                textureX = -textureOffsetX + marginX;
                textureY = mc.displayHeight-(borderTexture.height) - marginY - bottomTextureYMargin;
                translateX = -(mc.displayWidth/2)+minimapOffset;
                translateY = (mc.displayHeight/2)-minimapOffset - bottomTextureYMargin;
                scissorX = marginX;
                scissorY = marginY + bottomTextureYMargin;
                labelX = minimapSize/2;
                topLabelY = mc.displayHeight-minimapSize+topLabelYOffset-bottomTextureYMargin;
                bottomLabelY = mc.displayHeight-marginY-marginY+bottomLabelYOffset-bottomTextureYMargin;
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
                labelX = mc.displayWidth-(minimapSize/2);
                topLabelY = marginY+topLabelYOffset;
                bottomLabelY = minimapSize+bottomLabelYOffset;
                break;
            }
        }

        //JourneyMap.getLogger().info("New DisplayVars: " + shape + " " + position + " : " + displayWidth + "x" + displayHeight);
    }
}
