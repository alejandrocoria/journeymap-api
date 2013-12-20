package net.techbrew.mcjm.ui;

import net.minecraft.src.Minecraft;
import net.minecraft.src.ScaledResolution;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.render.texture.TextureImpl;

/**
* Created by mwoodman on 12/18/13.
*/
public class DisplayVars {

    public enum Position {
        TopRight(Constants.getString("MiniMapOverlay.position_topright")),
        BottomRight(Constants.getString("MiniMapOverlay.position_bottomright")),
        BottomLeft(Constants.getString("MiniMapOverlay.position_bottomleft")),
        TopLeft(Constants.getString("MiniMapOverlay.position_topleft"));

        public final String label;
        private Position(String label){
            this.label = label;
        }
    }

    public enum Shape {
        SmallSquare(Constants.getString("MiniMapOverlay.shape_smallsquare")),
        LargeSquare(Constants.getString("MiniMapOverlay.shape_largesquare")),
        SmallCircle(Constants.getString("MiniMapOverlay.shape_smallcircle")),
        LargeCircle(Constants.getString("MiniMapOverlay.shape_largecircle"));
        public final String label;
        private Shape(String label){
            this.label = label;
        }
    }

    final Position position;
    final Shape shape;
    final TextureImpl minimapTexture;
    final int displayWidth;
    final int displayHeight;
    final ScaledResolution scaledResolution;
    final int minimapSize,textureX,textureY;
    final double minimapOffset,translateX,translateY;
    final int marginX,marginY,scissorX,scissorY, labelX, topLabelY, bottomLabelY, topLabelYOffset, bottomLabelYOffset;

    DisplayVars(Minecraft mc, Shape shape, Position position){
        this.shape = shape;
        this.position = position;
        displayWidth = mc.displayWidth;
        displayHeight = mc.displayHeight;
        scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);

        switch(shape){
            case LargeCircle: {
                minimapTexture = TextureCache.instance().getMinimapLargeCircle();
                minimapSize = 512;
                marginX=0;
                marginY=0;
                topLabelYOffset = 10;
                bottomLabelYOffset = -10;
                break;
            }
            case SmallCircle: {
                minimapTexture = TextureCache.instance().getMinimapSmallCircle();
                minimapSize = 256;
                marginX=2;
                marginY=2;
                topLabelYOffset = 8;
                bottomLabelYOffset = -8;
                break;
            }
            case LargeSquare: {
                minimapTexture = TextureCache.instance().getMinimapLargeSquare();
                minimapSize = 512;
                marginX=0;
                marginY=0;
                topLabelYOffset = 8;
                bottomLabelYOffset = -8;
                break;
            }
            case SmallSquare:
            default: {
                minimapTexture = TextureCache.instance().getMinimapSmallSquare();
                minimapSize = 256;
                marginX=2;
                marginY=2;
                topLabelYOffset = 7;
                bottomLabelYOffset = -7;
                break;
            }
        }

        minimapOffset = minimapSize*0.5;
        final int textureOffsetX = (minimapTexture.width-minimapSize)/2;

        switch(position){
            case BottomRight : {
                textureX = mc.displayWidth - minimapTexture.width + textureOffsetX - marginX;
                textureY = mc.displayHeight-(minimapTexture.height)+(minimapSize/2) - marginY;
                translateX = (mc.displayWidth/2)-minimapOffset;
                translateY = (mc.displayHeight/2)-minimapOffset;
                scissorX = mc.displayWidth-minimapSize-marginX;
                scissorY = marginY;
                labelX = mc.displayWidth-(minimapSize/2);
                topLabelY = mc.displayHeight-minimapSize+3;
                bottomLabelY = mc.displayHeight-marginY;
                break;
            }
            case TopLeft : {
                textureX = -textureOffsetX + marginX;
                textureY = -(minimapTexture.height-minimapSize)/2 + marginY;
                translateX = -(mc.displayWidth/2)+minimapOffset;
                translateY = -(mc.displayHeight/2)+minimapOffset;
                scissorX = 0+marginX;
                scissorY = mc.displayHeight-minimapSize-marginY;
                labelX = minimapSize/2;
                topLabelY = marginY+3;
                bottomLabelY = minimapSize;
                break;
            }
            case BottomLeft : {
                textureX = -textureOffsetX + marginX;
                textureY = mc.displayHeight-(minimapTexture.height)+(minimapSize/2) - marginY;
                translateX = -(mc.displayWidth/2)+minimapOffset;
                translateY = (mc.displayHeight/2)-minimapOffset;
                scissorX = marginX;
                scissorY = marginY;
                labelX = minimapSize/2;
                topLabelY = mc.displayHeight-minimapSize+3;
                bottomLabelY = mc.displayHeight-marginY;
                break;
            }
            case TopRight :
            default : {
                textureX = mc.displayWidth - minimapTexture.width + textureOffsetX - marginX;
                textureY = -(minimapTexture.height-minimapSize)/2 + marginY;
                translateX = (mc.displayWidth/2)-minimapOffset;
                translateY = -(mc.displayHeight/2)+minimapOffset;
                scissorX = mc.displayWidth-minimapSize-marginX;
                scissorY = mc.displayHeight-minimapSize-marginY;
                labelX = mc.displayWidth-(minimapSize/2);
                topLabelY = marginY+3;
                bottomLabelY = minimapSize;
                break;
            }
        }

        JourneyMap.getLogger().info("New DisplayVars: " + shape + " " + position + " : " + displayWidth + "x" + displayHeight); // TODO: Fine
    }
}
