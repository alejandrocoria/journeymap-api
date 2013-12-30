package net.techbrew.mcjm.ui.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.render.texture.TextureImpl;

/**
* Created by mwoodman on 12/18/13.
*/
public class DisplayVars {

    public enum Position {
        TopRight(Constants.getString("MiniMap.position_topright")),
        BottomRight(Constants.getString("MiniMap.position_bottomright")),
        BottomLeft(Constants.getString("MiniMap.position_bottomleft")),
        TopLeft(Constants.getString("MiniMap.position_topleft"));

        public final String label;
        private Position(String label){
            this.label = label;
        }
    }

    public enum Shape {
        SmallSquare(Constants.getString("MiniMap.shape_smallsquare")),
        LargeSquare(Constants.getString("MiniMap.shape_largesquare")),
        SmallCircle(Constants.getString("MiniMap.shape_smallcircle")),
        LargeCircle(Constants.getString("MiniMap.shape_largecircle"));
        public final String label;
        private Shape(String label){
            this.label = label;
        }
    }

    final Position position;
    final Shape shape;
    final TextureImpl borderTexture;
    final TextureImpl maskTexture;

    final double fontScale;
    final int displayWidth;
    final int displayHeight;
    final ScaledResolution scaledResolution;
    final int minimapSize,textureX,textureY;
    final double minimapOffset,translateX,translateY;
    final int marginX,marginY,scissorX,scissorY, labelX, topLabelY, bottomLabelY;

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
                borderTexture = TextureCache.instance().getMinimapLargeCircle();
                maskTexture = TextureCache.instance().getMinimapLargeCircleMask();
                minimapSize = 512;
                marginX=3;
                marginY=3;
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
                borderTexture = TextureCache.instance().getMinimapSmallCircle();
                maskTexture = TextureCache.instance().getMinimapSmallCircleMask();
                minimapSize = 256;
                marginX=2;
                marginY=2;
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
                borderTexture = TextureCache.instance().getMinimapLargeSquare();
                maskTexture = null;
                minimapSize = 512;
                marginX=0;
                marginY=0;
                if(fontScale==1){
                    topLabelYOffset = 7;
                    bottomLabelYOffset = -6 - mc.fontRenderer.FONT_HEIGHT;
                } else {
                    topLabelYOffset = 9;
                    bottomLabelYOffset = -14-mc.fontRenderer.FONT_HEIGHT;
                }
                break;
            }
            case SmallSquare:
            default: {
                borderTexture = TextureCache.instance().getMinimapSmallSquare();
                maskTexture = null;
                minimapSize = 256;
                marginX=2;
                marginY=2;
                if(fontScale==1){
                    topLabelYOffset = 5;
                    bottomLabelYOffset = -2 - mc.fontRenderer.FONT_HEIGHT;
                } else {
                    topLabelYOffset = 6;
                    bottomLabelYOffset = -8-mc.fontRenderer.FONT_HEIGHT;
                }
                break;
            }
        }

        minimapOffset = minimapSize*0.5;
        final int textureOffsetX = (borderTexture.width-minimapSize)/2;

        switch(position){
            case BottomRight : {
                textureX = mc.displayWidth - borderTexture.width + textureOffsetX - marginX;
                textureY = mc.displayHeight-(borderTexture.height)+(minimapSize/2) - marginY - bottomTextureYMargin;
                translateX = (mc.displayWidth/2)-minimapOffset;
                translateY = (mc.displayHeight/2)-minimapOffset - bottomTextureYMargin;
                scissorX = mc.displayWidth-minimapSize-marginX;
                scissorY = marginY - bottomTextureYMargin;
                labelX = mc.displayWidth-(minimapSize/2);
                topLabelY = mc.displayHeight-minimapSize+topLabelYOffset-bottomTextureYMargin;
                bottomLabelY = mc.displayHeight-marginY-marginY+bottomLabelYOffset-bottomTextureYMargin;
                break;
            }
            case TopLeft : {
                textureX = -textureOffsetX + marginX;
                textureY = -(borderTexture.height-minimapSize)/2 + marginY;
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
                textureY = mc.displayHeight-(borderTexture.height)+(minimapSize/2) - marginY - bottomTextureYMargin;
                translateX = -(mc.displayWidth/2)+minimapOffset;
                translateY = (mc.displayHeight/2)-minimapOffset - bottomTextureYMargin;
                scissorX = marginX;
                scissorY = marginY - bottomTextureYMargin;
                labelX = minimapSize/2;
                topLabelY = mc.displayHeight-minimapSize+topLabelYOffset-bottomTextureYMargin;
                bottomLabelY = mc.displayHeight-marginY-marginY+bottomLabelYOffset-bottomTextureYMargin;
                break;
            }
            case TopRight :
            default : {
                textureX = mc.displayWidth - borderTexture.width + textureOffsetX - marginX;
                textureY = -(borderTexture.height-minimapSize)/2 + marginY;
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
