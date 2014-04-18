package net.techbrew.journeymap.ui.map.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;
import net.techbrew.journeymap.waypoint.WaypointHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mwoodman on 2/26/14.
 */
public class BlockInfoLayer {

    private List<DrawStep> drawStepList = new ArrayList<DrawStep>(1);

    BlockCoordIntPair lastCoord = null;
    long lastClicked = 0;

    public BlockInfoLayer()
    {

    }

    public List<DrawStep> onMouseMove(Minecraft mc, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord)
    {
        if(!blockCoord.equals(lastCoord))
        {
            lastCoord = blockCoord;
            drawStepList.clear();

            // Get block under mouse
            Chunk chunk = mc.theWorld.getChunkFromChunkCoords(blockCoord.x >> 4, blockCoord.z >> 4);
            String info;
            if(!chunk.isEmpty())
            {
                int blockY = chunk.getHeightValue(blockCoord.x & 15, blockCoord.z & 15);
                String biome = mc.theWorld.getBiomeGenForCoords(blockCoord.x, blockCoord.z).biomeName;
                info = Constants.getString("MapOverlay.location_xzyeb", blockCoord.x, blockCoord.z, blockY, (blockY >> 4), biome);
            }
            else
            {
                info = Constants.getString("MapOverlay.location_xzy", blockCoord.x, blockCoord.z, "?");
            }

            drawStepList.add(new BlockInfoStep(gridWidth/2, gridHeight-25, info, 0, Color.DARK_GRAY, Color.white, 1.0));

            // check for existing
            Collection<Waypoint> waypoints = WaypointHelper.getCachedWaypoints();
            for(Waypoint existing : waypoints)
            {
                if(existing.getX()==blockCoord.x && existing.getZ()==blockCoord.z)
                {
                    drawStepList.add(new DrawWayPointStep(blockCoord.x, blockCoord.z));
                }
            }
        }

        return drawStepList;
    }

    // TODO:  Move to waypoint delegate?
    public void onMouseClicked(Minecraft mc, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord)
    {
        // check for double-click
        long sysTime = Minecraft.getSystemTime();
        boolean doubleClick = blockCoord.equals(lastCoord) && sysTime - this.lastClicked < 450L;
        this.lastClicked = sysTime;

        if(doubleClick)
        {
            // check for existing
            Collection<Waypoint> waypoints = WaypointHelper.getCachedWaypoints();
            for(Waypoint existing : waypoints)
            {
                if(existing.getX()==blockCoord.x && existing.getZ()==blockCoord.z)
                {
                    UIManager.getInstance().openWaypointEditor(existing, false, MapOverlay.class);
                    return;
                }
            }

            // check chunk
            Chunk chunk = mc.theWorld.getChunkFromChunkCoords(blockCoord.x >> 4, blockCoord.z >> 4);
            int y = -1;
            if(!chunk.isEmpty())
            {
                y = Math.max(1, chunk.getHeightValue(blockCoord.x & 15, blockCoord.z & 15));
            }

            ChunkCoordinates cc = new ChunkCoordinates(blockCoord.x, y, blockCoord.z);
            Waypoint waypoint = Waypoint.at(cc, Waypoint.Type.Normal, mc.thePlayer.dimension);
            UIManager.getInstance().openWaypointEditor(waypoint, true, MapOverlay.class);
        }
    }

    class BlockInfoStep implements DrawStep {

        final double posX;
        final double posZ;
        final String text;
        final int labelYOffset;
        final Color bgColor;
        final Color fgColor;
        final double fontScale;
        int alpha = 255;

        int ticks = 20*5;

        public BlockInfoStep(double posX, double posZ, String text, int labelYOffset, Color bgColor, Color fgColor, double fontScale) {
            this.posX = posX;
            this.posZ = posZ;
            this.text = text;
            this.labelYOffset = labelYOffset;
            this.bgColor = bgColor;
            this.fgColor = fgColor;
            this.fontScale = fontScale;
        }

        @Override
        public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float scale) {
            if(ticks--<0)
            {
                //alpha-=10; // Fade
            }
            if(alpha>0) {
                DrawUtil.drawCenteredLabel(text, posX, posZ + labelYOffset, bgColor, Math.max(0, alpha-50), fgColor, Math.max(0, alpha), fontScale);
            }
        }
    }
}
