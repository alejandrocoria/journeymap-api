package net.techbrew.journeymap.cartography;

import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.model.RGB;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ChunkStandardRenderer extends BaseRenderer implements IChunkRenderer
{
    protected boolean caveGreySurface = JourneyMap.getInstance().coreProperties.caveGreySurface.get();
    protected boolean advancedSurfaceCheck = JourneyMap.getInstance().coreProperties.advancedSurfaceCheck.get();

    protected StatTimer renderSurfaceTimer = StatTimer.get("ChunkStandardRenderer.renderSurface");
    protected StatTimer renderSurfacePrepassTimer = StatTimer.get("ChunkStandardRenderer.renderSurface-prepass");
//    protected StatTimer advancedSurfaceCheckTimer = StatTimer.get("ChunkStandardRenderer.renderSurface.advancedSurfaceCheck");
//    protected StatTimer advancedSurfaceRenderTimer = StatTimer.get("ChunkStandardRenderer.renderSurface.advancedSurfaceRender");

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground,
                          final Integer vSlice, final ChunkMD.Set neighbors)
    {

        // Initialize ChunkSub slopes if needed
        if (chunkMd.surfaceSlopes == null)
        {
            initSurfaceSlopes(chunkMd, neighbors);
        }

        // Render the chunk image
        if (underground)
        {

            boolean ok = renderSurface(g2D, chunkMd, vSlice, neighbors, true);
            if (!ok)
            {
                JourneyMap.getLogger().fine("The surface chunk didn't paint: " + chunkMd.toString());
            }

            final int sliceMinY = Math.max((vSlice << 4), 0);
            final int hardSliceMaxY = ((vSlice + 1) << 4) - 1;
            int sliceMaxY = Math.min(hardSliceMaxY, chunkMd.worldObj.getActualHeight());
            if (sliceMinY >= sliceMaxY)
            {
                sliceMaxY = sliceMinY + 2;
            }

            if (chunkMd.sliceSlopes == null)
            {
                initSliceSlopes(chunkMd, sliceMinY, sliceMaxY, neighbors);
            }

            ok = renderUnderground(g2D, chunkMd, vSlice, sliceMinY, sliceMaxY, neighbors);

            if (!ok)
            {
                JourneyMap.getLogger().fine("The underground chunk didn't paint: " + chunkMd.toString());
            }
            return ok;
        }
        else
        {
            return renderSurface(g2D, chunkMd, vSlice, neighbors, false);
        }

    }

    /**
     * Initialize surface slopes in chunk if needed.
     *
     * @param chunkMd
     * @param neighbors
     */
    protected void initSurfaceSlopes(final ChunkMD chunkMd, final ChunkMD.Set neighbors)
    {
//        StatTimer timer = StatTimer.get("ChunkStandardRenderer.initSurfaceSlopes");
//        timer.start();
        float slope, h, hN, hW, hNW;
        chunkMd.surfaceSlopes = new float[16][16];
        for (int y = 0; y < 16; y++)
        {
            for (int x = 0; x < 16; x++)
            {
                h = chunkMd.getSlopeHeightValue(x, y);
                hN = (y == 0) ? getBlockHeight(x, y, 0, -1, chunkMd, neighbors, h) : chunkMd.getSlopeHeightValue(x, y - 1);
                hW = (x == 0) ? getBlockHeight(x, y, -1, 0, chunkMd, neighbors, h) : chunkMd.getSlopeHeightValue(x - 1, y);
                hNW = getBlockHeight(x, y, -1, -1, chunkMd, neighbors, h);
                slope = ((h / hN) + (h / hW) + (h / hNW)) / 3f;
                chunkMd.surfaceSlopes[x][y] = slope;
            }
        }
//        timer.stop();
    }

    /**
     * Initialize slice slopes in chunk if needed.
     *
     * @param chunkMd
     * @param neighbors
     */
    protected void initSliceSlopes(final ChunkMD chunkMd, int sliceMinY, int sliceMaxY, final ChunkMD.Set neighbors)
    {
//        StatTimer timer = StatTimer.get("ChunkStandardRenderer.initSliceSlopes");
//        timer.start();

        chunkMd.sliceSlopes = new float[16][16];
        float slope, h, hN, hW;

        for (int z = 0; z < 16; z++)
        {
            for (int x = 0; x < 16; x++)
            {
                h = getHeightInSlice(chunkMd, x, z, sliceMinY, sliceMaxY);
                hN = (z == 0) ? getBlockHeight(x, z, 0, -1, chunkMd, neighbors, h, sliceMinY, sliceMaxY) : getHeightInSlice(chunkMd, x, z - 1, sliceMinY, sliceMaxY);
                hW = (x == 0) ? getBlockHeight(x, z, -1, 0, chunkMd, neighbors, h, sliceMinY, sliceMaxY) : getHeightInSlice(chunkMd, x - 1, z, sliceMinY, sliceMaxY);
                slope = ((h / hN) + (h / hW)) / 2f;
                chunkMd.sliceSlopes[x][z] = slope;
            }
        }
//        timer.stop();
    }

    /**
     * Get the color for a block based on its location, neighbor slopes.
     */
    protected RGB getBaseBlockColor(final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int y, int z)
    {
        return blockMD.getColor(chunkMd, x, y, z);
    }

    /**
     * Render blocks in the chunk for the surface.
     */
    protected boolean renderSurface(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice, final ChunkMD.Set neighbors, final boolean cavePrePass)
    {
        StatTimer timer = cavePrePass ? renderSurfacePrepassTimer : renderSurfaceTimer;
        timer.start();

        g2D.setComposite(BlockUtils.OPAQUE);

        boolean chunkOk = false;
        for (int x = 0; x < 16; x++)
        {
            blockLoop:
            for (int z = 0; z < 16; z++)
            {

                BlockMD blockMD = null;
                boolean isUnderRoof = false;
                int standardY = Math.max(1, chunkMd.getHeightValue(x, z));
                int roofY = 0;
                int y = standardY;

                if (advancedSurfaceCheck)
                {
                    //advancedSurfaceCheckTimer.start();
                    roofY = Math.max(1, chunkMd.getAbsoluteHeightValue(x, z));

                    if (standardY < roofY)
                    {
                        // Is transparent roof above standard height?
                        int checkY = roofY;
                        while (checkY > standardY)
                        {
                            blockMD = BlockMD.getBlockMD(chunkMd, x, checkY, z);
                            if (blockMD != null && blockMD.isTransparentRoof())
                            {
                                isUnderRoof = true;
                                y = Math.max(standardY, checkY);
                                break;
                            }
                            else
                            {
                                checkY--;
                            }
                        }
                        //advancedSurfaceCheckTimer.stop();
                    }
                    else
                    {
                        //advancedSurfaceCheckTimer.cancel();
                    }
                }

                // Get blockinfo for coords
                do
                {
                    blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);

                    // Null check
                    if (blockMD == null)
                    {
                        paintBadBlock(x, y, z, g2D);
                        continue blockLoop;
                    }

                    if (blockMD.isTransparentRoof())
                    {
                        y--;
                        continue;
                    }

                    if (blockMD.isAir())
                    {
                        y--;
                    }
                    else
                    {
                        break;
                    }
                } while (y >= 0);

                if (blockMD == null)
                {
                    paintBadBlock(x, y, z, g2D);
                    continue blockLoop;
                }

                // Get base color for block
                RGB color = getBaseBlockColor(chunkMd, blockMD, neighbors, x, y, z);
                if (color == null)
                {
                    paintBadBlock(x, y, z, g2D);
                    continue blockLoop;
                }

                // Paint deeper blocks if alpha used, but not if this pass is just for underground layer
                boolean useAlpha = blockMD.hasFlag(BlockUtils.Flag.Transparency) || blockMD.isWater();
                if (useAlpha)
                {

                    color = renderSurfaceAlpha(g2D, chunkMd, blockMD, neighbors, x, y, z);
                    chunkOk = true;

                }
                else
                {

                    // Bevel color according to slope
                    if (!blockMD.hasFlag(BlockUtils.Flag.NoShadow))
                    {
                        surfaceSlopeColor(color, chunkMd, blockMD, neighbors, x, y, z);
                    }

                    // Grey out if used to show outside in underground layer
                    if (cavePrePass && caveGreySurface)
                    {
                        color.ghostSurface();
                    }

                    // Draw daytime map block
                    g2D.setPaint(color.toColor());
                    g2D.fillRect(x, z, 1, 1);
                    chunkOk = true;
                }

                // If under glass, color accordingly
                if (isUnderRoof)
                {
                    //advancedSurfaceRenderTimer.start();
                    while (roofY > y)
                    {
                        blockMD = BlockMD.getBlockMD(chunkMd, x, roofY, z);
                        if (blockMD != null && !blockMD.isAir())
                        {
                            if (blockMD.isTransparentRoof())
                            {
                                g2D.setComposite(blockMD.getAlphaComposite());
                                g2D.setPaint(getBaseBlockColor(chunkMd, blockMD, neighbors, x, roofY, z).toColor());
                                g2D.fillRect(x, z, 1, 1);
                                g2D.setComposite(BlockUtils.OPAQUE);
                            }
                        }
                        roofY--;
                    }
                    //advancedSurfaceRenderTimer.stop();
                }

                // Night color
                if (!cavePrePass)
                {
                    int lightLevel = Math.max(1, chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, (y + 1), z));
                    if (lightLevel < 15)
                    {
                        float diff = Math.min(1F, (lightLevel / 15F) + .1f);
                        if (diff != 1.0)
                        {
                            color.moonlight(diff);
                        }
                    }

                    // Draw nighttime map block
                    g2D.setPaint(color.toColor());
                    g2D.fillRect(x + 16, z, 1, 1);

                    chunkOk = true;
                }
            }
        }
        timer.stop();
        return chunkOk;
    }

    protected RGB renderSurfaceAlpha(final Graphics2D g2D, final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int y, int z)
    {

        RGB color = getBaseBlockColor(chunkMd, blockMD, neighbors, x, y, z);

        // Check for surrounding water
        if (blockMD.isWater())
        {
            BlockMD bw = getBlock(x, y, z, -1, 0, chunkMd, neighbors, blockMD);
            BlockMD be = getBlock(x, y, z, +1, 0, chunkMd, neighbors, blockMD);
            BlockMD bn = getBlock(x, y, z, 0, -1, chunkMd, neighbors, blockMD);
            BlockMD bs = getBlock(x, y, z, 0, +1, chunkMd, neighbors, blockMD);
            Set<RGB> colors = new HashSet<RGB>(6);
            colors.add(color);

            if (bw.isWater())
            {
                colors.add(getBaseBlockColor(chunkMd, bw, neighbors, x, y, z));
            }
            if (be.isWater())
            {
                colors.add(getBaseBlockColor(chunkMd, be, neighbors, x, y, z));
            }
            if (bn.isWater())
            {
                colors.add(getBaseBlockColor(chunkMd, bn, neighbors, x, y, z));
            }
            if (bs.isWater())
            {
                colors.add(getBaseBlockColor(chunkMd, bs, neighbors, x, y, z));
            }

            if (!colors.isEmpty())
            {
                color = RGB.average(colors);
            }
        }

        // Paint depth layers
        paintDepth(chunkMd, blockMD, x, y, z, g2D, false);

        return color;
    }

    protected void surfaceSlopeColor(final RGB color, final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int ignored, int z)
    {

        float slope, bevel, sN, sNW, sW, sS, sE, sSE, sAvg;
        sS = sE = sSE = sNW = 1f;
        slope = chunkMd.surfaceSlopes[x][z];

        if (!blockMD.isFoliage())
        {
            // Trees look more distinct if just beveled on upper left corners
            sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, false);
            sNW = getBlockSlope(x, z, -1, -1, chunkMd, neighbors, slope, false);
            sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, false);
            sAvg = (sN + sNW + sW) / 3f;
        }
        else
        {
            // Everything else gets beveled based on average slope across n,s,e,w
            sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, false);
            sS = getBlockSlope(x, z, 0, 1, chunkMd, neighbors, slope, false);
            sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, false);
            sE = getBlockSlope(x, z, 1, 0, chunkMd, neighbors, slope, false);
            sAvg = (sN + sS + sW + sE) / 4f;
        }

        bevel = 1f;
        if (slope < 1)
        {
            if (slope <= sAvg)
            {
                slope = slope * .6f;
            }
            else if (slope > sAvg)
            {
                if (!blockMD.isFoliage())
                {
                    slope = (slope + sAvg) / 2f;
                }
            }
            bevel = Math.max(slope * .8f, .1f);
        }
        else if (slope > 1)
        {
            if (sAvg > 1)
            {
                if (slope >= sAvg)
                {
                    if (!blockMD.isFoliage())
                    {
                        slope = slope * 1.2f;
                    }
                }
            }
            bevel = Math.min(slope * 1.2f, 1.4f);
        }

        if (bevel != 1f)
        {
            color.bevelSlope(bevel);
        }
    }

    /**
     * Render blocks in the chunk for underground.
     */
    protected boolean renderUnderground(final Graphics2D g2D, final ChunkMD chunkMd, final int vSlice, final int sliceMinY, final int sliceMaxY, final ChunkMD.Set neighbors)
    {

        StatTimer timer = StatTimer.get("ChunkStandardRenderer.renderUnderground");
        timer.start();

        boolean hasSolid;
        boolean hasAir;
        BlockMD blockMD;

        int paintY;
        int lightLevel;

        boolean chunkOk = false;
        for (int z = 0; z < 16; z++)
        {

            blockLoop:
            for (int x = 0; x < 16; x++)
            {
                // reset vars
                lightLevel = 0;

                try
                {

                    int ceiling = BlockUtils.ceiling(chunkMd, x, sliceMaxY, z);

                    // Hole in the world?  Skip.
                    if (ceiling < 0)
                    {
                        chunkOk = true;
                        continue blockLoop;
                    }

                    // Skip if top block is open to the sky
                    if (BlockUtils.skyAbove(chunkMd, x, Math.min(ceiling, sliceMinY), z))
                    {
                        chunkOk = true;
                        paintDimSurface(x, z, g2D);
                        continue blockLoop;
                    }

                    if (ceiling <= sliceMinY)
                    {
                        chunkOk = true;
                        paintDimSurface(x, z, g2D);
                        continue blockLoop;
                    }

                    // Init variables for airloop
                    hasAir = BlockMD.getBlockMD(chunkMd, x, ceiling + 1, z).isAir();
                    hasSolid = false;
                    paintY = ceiling;

                    // Step downward to find air
                    airloop:
                    for (int y = ceiling; y >= 0; y--)
                    {
                        blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);

                        // Water handling
                        if (blockMD.isWater())
                        {
                            paintY = y;
                            paintDepth(chunkMd, BlockMD.getBlockMD(chunkMd, x, y, z), x, paintY, z, g2D, caveLighting);
                            chunkOk = true;
                            continue blockLoop;
                        }

                        // Lava shortcut
                        if (blockMD.isLava())
                        {
                            if (!hasAir)
                            {
                                paintBlock(x, z, Color.black, g2D);
                                chunkOk = true;
                                continue blockLoop;
                            }
                            else if (!hasSolid || y >= sliceMinY)
                            {
                                lightLevel = 15;
                                paintY = y;
                                break airloop;
                            }
                        }

                        // Torch
                        if (blockMD.isTorch())
                        {
                            hasAir = true;
                            paintY = y - 1;
                            lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y, z);

                            // Check whether torch is mounted on the block below it
                            if (chunkMd.stub.getBlockMetadata(x, y, z) != 5)
                            { // standing on block below=5
                                continue airloop;
                            }
                            else
                            {
                                break airloop;
                            }
                        }

                        // Found air or something treated like air
                        if (blockMD.isAir())
                        {
                            hasAir = true;
                            continue airloop;
                        }

                        // Found transparent
                        if (blockMD.hasFlag(BlockUtils.Flag.Transparency))
                        {
                            paintY = y;
                            hasAir = true;
                            continue airloop;
                        }

                        // Arrived at a solid block with air above it
                        if (hasAir)
                        {
                            paintY = y;
                            lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z);
                            if (lightLevel > 0 || !caveLighting)
                            {
                                break airloop;
                            }
                            else
                            {
                                hasSolid = true;
                            }
                        }

                        if (y <= sliceMinY)
                        {
                            // Solid blocks in column
                            break airloop;
                        }

                    } // end airloop

                    // No lit air blocks in column at all
                    if (paintY < 0 || (!hasAir) || (lightLevel < 1 && caveLighting))
                    {
                        chunkOk = true;

                        //paintDimSurface(x, z, g2D);
                        paintBlock(x, z, Color.black, g2D);
                        continue blockLoop;
                    }

                    // Get block color
                    blockMD = BlockMD.getBlockMD(chunkMd, x, paintY, z);
                    RGB color = getBaseBlockColor(chunkMd, blockMD, neighbors, x, paintY, z);

                    boolean keepflat = blockMD.hasFlag(BlockUtils.Flag.NoShadow);
                    if (!keepflat)
                    {
                        // Contour shading
                        // Get slope of block and prepare to bevelSlope
                        float slope, s, sN, sNW, sW, sAvg, shaded;
                        slope = chunkMd.sliceSlopes[x][z];

                        sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, true);
                        sNW = getBlockSlope(x, z, -1, -1, chunkMd, neighbors, slope, true);
                        sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, true);
                        sAvg = (sN + sNW + sW) / 3f;

                        if (slope < 1)
                        {

                            if (slope <= sAvg)
                            {
                                slope = slope * .6f;
                            }
                            else if (slope > sAvg)
                            {
                                slope = (slope + sAvg) / 2f;
                            }
                            s = Math.max(slope * .8f, .1f);
                            color.bevelSlope(s);

                        }
                        else if (slope > 1)
                        {

                            if (sAvg > 1)
                            {
                                if (slope >= sAvg)
                                {
                                    slope = slope * 1.2f;
                                }
                            }
                            s = slope * 1.2f;
                            s = Math.min(s, 1.4f);
                            color.bevelSlope(s);
                        }
                    }

                    // Adjust color for light level
                    if (caveLighting && lightLevel < 15)
                    {
                        float factor = Math.min(1F, (lightLevel / 16F));
                        color.darken(factor);
                    }

                    // Draw block
                    paintBlock(x, z, color.toColor(), g2D);
                    chunkOk = true;

                }
                catch (Throwable t)
                {
                    timer.cancel();
                    paintBadBlock(x, vSlice, z, g2D);
                    String error = Constants.getMessageJMERR07("x,vSlice,z = " + x + "," //$NON-NLS-1$ //$NON-NLS-2$
                            + vSlice + "," + z + " : " + LogFormatter.toString(t)); //$NON-NLS-1$ //$NON-NLS-2$
                    JourneyMap.getLogger().severe(error);
                }

            }
        }
        timer.stop();
        return chunkOk;

    }

    protected void paintDimSurface(int x, int z, final Graphics2D g2D)
    {
        if (!caveGreySurface)
        {
            g2D.setComposite(BlockUtils.SLIGHTLYCLEAR);
            g2D.setColor(Color.black);
            g2D.fillRect(x, z, 1, 1);
            g2D.setComposite(BlockUtils.OPAQUE);
        }
    }

    protected void paintDepth(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z, final Graphics2D g2D, final boolean useLighting)
    {
        // See how deep the alpha goes
        Stack<BlockMD> stack = new Stack<BlockMD>();
        stack.push(blockMD);
        int maxDepth = 5;
        int down = y;
        while (down > 0)
        {
            down--;
            BlockMD lowerBlock = BlockMD.getBlockMD(chunkMd, x, down, z);
            if (lowerBlock != null)
            {
                stack.push(lowerBlock);

                if (lowerBlock.isAir())
                {
                    maxDepth++;
                }

                if (lowerBlock.getAlpha() == 1f || y - down > maxDepth)
                {
                    break;
                }

            }
            else
            {
                break;
            }

        }

        RGB color;
        boolean isWater = blockMD.isWater();

        // Get color for bottom of stack
        color = getBaseBlockColor(chunkMd, stack.peek(), null, x, y, z);

        if (useLighting)
        {
            adjustColorForDepth(chunkMd, color, x, down + 1, z);
        }
        else if (isWater)
        {
            float factor = .68f;
            color.darken(factor);
        }

        g2D.setComposite(BlockUtils.OPAQUE);
        g2D.setPaint(color.toColor());
        g2D.fillRect(x, z, 1, 1);

        // If bottom block is same as the top, don't bother with transparency
        if (stack.peek().getBlock() != blockMD.getBlock())
        {
            stack.pop(); // already used it

            ArrayList<RGB> colors = new ArrayList<RGB>();
            colors.add(color);

            while (!stack.isEmpty())
            {
                BlockMD lowerBlock = stack.pop();
                if (lowerBlock.isAir())
                {
                    continue;
                }
                color = getBaseBlockColor(chunkMd, lowerBlock, null, x, y, z);

                if (useLighting)
                {
                    adjustColorForDepth(chunkMd, color, x, ++down, z);
                }
                else if (isWater)
                {
                    float factor = .7f;
                    color.darken(factor);
                }
                colors.add(color);
            }

            g2D.setComposite(BlockUtils.OPAQUE);
            g2D.setPaint(RGB.average(colors).toColor());
            g2D.fillRect(x, z, 1, 1);
        }
    }

    protected void adjustColorForDepth(ChunkMD chunkMd, RGB color, int x, int y, int z)
    {
        int lightLevel = Math.max(0, chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y, z));
        if (lightLevel < 15)
        {
            float diff = Math.min(1F, (lightLevel / 15F) + .05f);
            if (diff != 1.0)
            {
                color.moonlight(diff);
            }
        }
    }

    protected int getHeightInSlice(final ChunkMD chunkMd, final int x, final int z, final int sliceMinY, final int sliceMaxY)
    {
        return BlockUtils.ceiling(chunkMd, x, sliceMaxY, z) + 1;
    }

    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     *
     * @param x
     * @param z
     * @param offsetX
     * @param offsetz
     * @param currentChunk
     * @param neighbors
     * @param defaultVal
     * @return
     */
    protected Float getBlockHeight(int x, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, float defaultVal, final int sliceMinY, final int sliceMaxY)
    {
        int newX = x + offsetX;
        int newZ = z + offsetz;

        if (newX == -1)
        {
            newX = 15;
        }
        else if (newX == 16)
        {
            newX = 0;
        }
        if (newZ == -1)
        {
            newZ = 15;
        }
        else if (newZ == 16)
        {
            newZ = 0;
        }

        ChunkMD chunk = getChunk(x, z, offsetX, offsetz, currentChunk, neighbors);

        if (chunk != null)
        {
            return (float) getHeightInSlice(chunk, newX, newZ, sliceMinY, sliceMaxY);
        }
        else
        {
            return defaultVal;
        }
    }

}
