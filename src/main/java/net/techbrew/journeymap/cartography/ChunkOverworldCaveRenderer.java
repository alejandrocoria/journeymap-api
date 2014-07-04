package net.techbrew.journeymap.cartography;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Renders chunk image for caves in the overworld.
 */
public class ChunkOverworldCaveRenderer extends BaseRenderer implements IChunkRenderer
{
    protected ChunkOverworldSurfaceRenderer surfaceRenderer;
    protected StatTimer renderCaveTimer = StatTimer.get("ChunkOverworldCaveRenderer.render");

    public ChunkOverworldCaveRenderer(ChunkOverworldSurfaceRenderer surfaceRenderer)
    {
        this.surfaceRenderer = surfaceRenderer;
    }

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground,
                          final Integer vSlice, final ChunkMD.Set neighbors)
    {

        if(!underground || vSlice==null)
        {
            JourneyMap.getLogger().warning(String.format("ChunkOverworldCaveRenderer is for caves.  Y u do dis? (%s,%s)", underground, vSlice));
            return false;
        }

        boolean ok = false;

        if(!chunkMd.hasNoSky)
        {
            // Initialize ChunkSub slopes if needed
            if (chunkMd.surfaceSlopes == null)
            {
                surfaceRenderer.initSurfaceSlopes(chunkMd, neighbors);
            }

            ok = surfaceRenderer.renderSurface(g2D, chunkMd, vSlice, neighbors, true);
            if (!ok)
            {
                JourneyMap.getLogger().fine("The surface chunk didn't paint: " + chunkMd.toString());
            }
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
     * Get the color for a block based on its chunk-local coords, neighbor slopes.
     */
    protected int getBaseBlockColor(final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int y, int z)
    {
        return getBaseBlockColor(chunkMd, blockMD, neighbors, x, y, z, true);
    }

    /**
     * Get the color for a block based on its chunk-local coords
     */
    protected int getBaseBlockColor(final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int y, int z, boolean averageWater)
    {
        if (averageWater && blockMD.isWater())
        {
            return getAverageWaterColor(neighbors, (chunkMd.coord.chunkXPos<<4) + x, y, (chunkMd.coord.chunkZPos<<4) + z);
        }
        else
        {
            return blockMD.getColor(chunkMd, x, y, z);
        }
    }

    /**
     * Requires absolute x,y,z coordinates
     */
    protected int getAverageWaterColor(final ChunkMD.Set neighbors, int blockX, int blockY, int blockZ)
    {
        return RGB.average(
                getWaterColor(neighbors, blockX, blockY, blockZ),
                getWaterColor(neighbors, blockX - 1, blockY, blockZ),
                getWaterColor(neighbors, blockX + 1, blockY, blockZ),
                getWaterColor(neighbors, blockX, blockY - 1, blockZ),
                getWaterColor(neighbors, blockX, blockY + 1, blockZ)
        );
    }

    /**
     * Requires absolute x,y,z coordinates
     */
    protected int getWaterColor(final ChunkMD.Set neighbors, int blockX, int blockY, int blockZ)
    {
        ChunkMD chunk = neighbors.get(new ChunkCoordIntPair(blockX>>4, blockZ>>4));
        if(chunk!=null)
        {
            BlockMD block = BlockMD.getBlockMD(chunk, blockX & 15, blockY, blockZ & 15);
            if(block!=null && block.isWater())
            {
                return block.getColor(chunk, blockX & 15, blockY, blockZ & 15);
            }
        }
        return 0;
    }

    /**
     * Render blocks in the chunk for underground.
     */
    protected boolean renderUnderground(final Graphics2D g2D, final ChunkMD chunkMd, final int vSlice, final int sliceMinY, final int sliceMaxY, final ChunkMD.Set neighbors)
    {
        renderCaveTimer.start();

        boolean hasSolid;
        boolean hasAir;
        BlockMD blockMD;

        int paintY;
        int lightLevel;

        boolean chunkOk = false;

        for (int z = 0; z < 16; z++)
        {
            blockLoop: for (int x = 0; x < 16; x++)
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
                        continue;
                    }

                    // Skip if top block is open to the sky
                    if (BlockUtils.skyAbove(chunkMd, x, Math.min(ceiling, sliceMinY), z))
                    {
                        chunkOk = true;
                        paintDimSurface(x, z, g2D);
                        continue;
                    }

                    if (ceiling <= sliceMinY)
                    {
                        chunkOk = true;
                        paintDimSurface(x, z, g2D);
                        continue;
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
                            int color = getDepthColor(chunkMd, BlockMD.getBlockMD(chunkMd, x, y, z), neighbors, x, paintY, z, caveLighting);
                            paintBlock(x, z, color, g2D);
                            chunkOk = true;
                            continue blockLoop;
                        }

                        // Lava shortcut
                        if (blockMD.isLava())
                        {
                            if (!hasAir)
                            {
                                paintBlock(x, z, 0, g2D);
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
                        paintBlock(x, z, 0, g2D);
                        continue blockLoop;
                    }

                    // Get block color
                    blockMD = BlockMD.getBlockMD(chunkMd, x, paintY, z);
                    int color = getBaseBlockColor(chunkMd, blockMD, neighbors, x, paintY, z);

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
                            color = RGB.bevelSlope(color, s);

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
                            color = RGB.bevelSlope(color, s);
                        }
                    }

                    // Adjust color for light level
                    if (caveLighting && lightLevel < 15)
                    {
                        float factor = Math.min(1F, (lightLevel / 16F));
                        color = RGB.darken(color, factor);
                    }

                    // Draw block
                    paintBlock(x, z, color, g2D);
                    chunkOk = true;

                }
                catch (Throwable t)
                {
                    renderCaveTimer.cancel();
                    paintBadBlock(x, vSlice, z, g2D);
                    String error = Constants.getMessageJMERR07("x,vSlice,z = " + x + "," //$NON-NLS-1$ //$NON-NLS-2$
                            + vSlice + "," + z + " : " + LogFormatter.toString(t)); //$NON-NLS-1$ //$NON-NLS-2$
                    JourneyMap.getLogger().severe(error);
                }

            }
        }
        renderCaveTimer.stop();
        return chunkOk;

    }

    protected void paintDimSurface(int x, int z, final Graphics2D g2D)
    {
        g2D.setComposite(BlockUtils.SLIGHTLYCLEAR);
        g2D.setColor(Color.black);
        g2D.fillRect(x, z, 1, 1);
        g2D.setComposite(BlockUtils.OPAQUE);
    }


    protected int getDepthColor(ChunkMD chunkMd, BlockMD blockMD, final ChunkMD.Set neighbors, int x, int y, int z, final boolean useLighting)
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

        Integer waterColor = null;
        boolean isWater = blockMD.isWater();

        ArrayList<Integer> colors = new ArrayList<Integer>();

        // Get color for bottom of stack
        int bottomColor = getBaseBlockColor(chunkMd, stack.peek(), neighbors, x, down, z);

        if (useLighting)
        {
            bottomColor = adjustColorForDepth(chunkMd, bottomColor, x, down + 1, z);
        }
        else if (isWater)
        {
            //float factor = .5f;
            bottomColor = RGB.darken(bottomColor, .65f);
        }

        if(isWater)
        {
         //   waterColor = bottomColor;
        }

        colors.add(bottomColor);

        // If bottom block is same as the top, don't bother with transparency
        if (stack.peek().getBlock() != blockMD.getBlock())
        {
            stack.pop(); // already used it

            while (!stack.isEmpty())
            {
                down++;
                Integer color = null;
                BlockMD lowerBlock = stack.pop();
                if (lowerBlock.isAir())
                {
                    continue;
                }

//                if(lowerBlock.isWater())
//                {
//                    if(waterColor==null)
//                    {
//                        waterColor = getBaseBlockColor(chunkMd, blockMD, neighbors, x, y, z, true);
//                        waterColor = dataCache.getColor(waterColor.darken(.6f));
//                    }
//                    color = waterColor;
//                }
//                else
//                {
//                    color = getBaseBlockColor(chunkMd, lowerBlock, null, x, y, z);
//                }

                color = getBaseBlockColor(chunkMd, lowerBlock, neighbors, x, down, z, false);

                if (useLighting)
                {
                    color = adjustColorForDepth(chunkMd, color, x, ++down, z);
                }

                if(color==null)
                {
                    continue;
                }
                else
                {
                    colors.add(color);
                }
            }
        }

        if(colors.size()>1)
        {
            if(isWater)
            {
                colors.add(getBaseBlockColor(chunkMd, blockMD, neighbors, x, y, z, true));
            }

            return RGB.average(colors);
        }
        else
        {
            return bottomColor;
        }
    }

    protected int adjustColorForDepth(ChunkMD chunkMd, int color, int x, int y, int z)
    {
        int lightLevel = Math.max(0, chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y, z));
        if (lightLevel < 15)
        {
            float diff = Math.min(1F, (lightLevel / 15F) + .05f);
            if (diff != 1.0)
            {
                return RGB.moonlight(color, diff);
            }
        }
        return color;
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
