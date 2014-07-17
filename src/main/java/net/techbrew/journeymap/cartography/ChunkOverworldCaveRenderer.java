package net.techbrew.journeymap.cartography;

import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.properties.CoreProperties;

import java.awt.*;

/**
 * Renders chunk image for caves in the overworld.
 */
public class ChunkOverworldCaveRenderer extends BaseRenderer implements IChunkRenderer
{
    protected CoreProperties coreProperties = JourneyMap.getInstance().coreProperties;
    protected ChunkOverworldSurfaceRenderer surfaceRenderer;
    protected StatTimer renderCaveTimer = StatTimer.get("ChunkOverworldCaveRenderer.render");

    protected Strata strata = new Strata("OverworldCave", 40,8, true);
    protected float defaultDim = .8f;


    /**
     * Takes an instance of the surface renderer in order to do a prepass when the surface
     * intersects the slice being mapped.
     */
    public ChunkOverworldCaveRenderer(ChunkOverworldSurfaceRenderer surfaceRenderer)
    {
        this.surfaceRenderer = surfaceRenderer;
        updateOptions();
    }

    /**
     * Render chunk image for caves in the overworld.
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

        updateOptions();
        boolean ok = false;

        // Surface prepass
        if(!chunkMd.hasNoSky && surfaceRenderer!=null)
        {
            ok = surfaceRenderer.render(g2D, chunkMd, true, vSlice, neighbors, true);
            if (!ok)
            {
                JourneyMap.getLogger().fine("The surface chunk didn't paint: " + chunkMd.toString());
            }
        }

        renderCaveTimer.start();

        try
        {
            // Init slopes within slice
            if (chunkMd.sliceSlopes.get(vSlice) == null)
            {
                populateSlopes(chunkMd, vSlice, neighbors);
            }

            // Render that lovely cave action
           ok = renderUnderground(g2D, chunkMd, vSlice, neighbors);

            if (!ok)
            {
                JourneyMap.getLogger().fine("The underground chunk didn't paint: " + chunkMd.toString());
            }
            return ok;
        }
        finally
        {
            renderCaveTimer.stop();
        }
    }

    /**
     * Render blocks in the chunk for underground.
     */
    protected boolean renderUnderground(final Graphics2D g2D, final ChunkMD chunkMd, final int vSlice, final ChunkMD.Set neighbors)
    {
        final int[] sliceBounds = getVSliceBounds(chunkMd, vSlice);
        final int sliceMinY = sliceBounds[0];
        final int sliceMaxY = sliceBounds[1];

        int y;

        boolean chunkOk = false;

        for (int z = 0; z < 16; z++)
        {
            blockLoop: for (int x = 0; x < 16; x++)
            {
                strata.reset();

                try
                {
                    final int ceiling = chunkMd.hasNoSky ? sliceMaxY : chunkMd.ceiling(x, z);

                    // Oh look, a hole in the world.
                    if (ceiling < 0)
                    {
                        chunkOk = true;
                        paintVoidBlock(x, z, g2D);
                        continue;
                    }

                    // Nothing even in this slice.
                    if (ceiling<sliceMinY)
                    {
                        if(surfaceRenderer!=null)
                        {
                            // Should be painted by surface renderer already.
                            paintDimOverlay(x, z, defaultDim, g2D);
                        }
                        else
                        {
                            paintBlackBlock(x, z, g2D);
                        }
                        chunkOk = true;
                        continue;
                    }
                    else if(ceiling>sliceMaxY)
                    {
                        // Solid stuff above the slice. Shouldn't be painted by surface renderer.
                        y = sliceMaxY;
                    }
                    else
                    {
                        // Ceiling within slice. Should be painted by by surface renderer... should we dim it?
                        y = ceiling;
                    }

                    buildStrata(strata, neighbors, sliceMinY, chunkMd, x, y, z);

                    // No lit blocks
                    if(strata.isEmpty())
                    {
                        // No surface?
                        if(surfaceRenderer==null)
                        {
                            if(strata.blocksFound)
                            {
                                paintBlackBlock(x, z, g2D);
                            }
                            else
                            {
                                paintVoidBlock(x, z, g2D);
                            }
                        }
                        else if(ceiling>sliceMaxY)
                        {
                            int distance = ceiling-y;
                            if(distance<16)
                            {
                                // Show dimmed surface above
                                paintDimOverlay(x, z, Math.max(defaultDim, distance / 16), g2D);
                            }
                            else
                            {
                                // Or not.
                                paintBlackBlock(x, z, g2D);
                            }
                        }
                        else
                        {
                            paintDimOverlay(x, z, defaultDim, g2D);
                        }

                        chunkOk = true;
                    }
                    else
                    {
                        // Paint that action
                        chunkOk = paintStrata(strata, g2D, chunkMd, vSlice, neighbors, x, ceiling, z) || chunkOk;
                    }

                }
                catch (Throwable t)
                {
                    paintBadBlock(x, vSlice, z, g2D);
                    String error = Constants.getMessageJMERR07("x,vSlice,z = " + x + "," //$NON-NLS-1$ //$NON-NLS-2$
                            + vSlice + "," + z + " : " + LogFormatter.toString(t)); //$NON-NLS-1$ //$NON-NLS-2$
                    JourneyMap.getLogger().severe(error);
                }
            }
        }
        strata.reset();
        return chunkOk;
    }

    /**
     * Create Strata for caves, using first lit blocks found.
     */
    protected void buildStrata(Strata strata, final ChunkMD.Set neighbors, int minY, ChunkMD chunkMd, int x, final int topY, int z)
    {
        BlockMD blockMD;
        BlockMD blockAboveMD;
        BlockMD lavaBlockMD = null;

        try
        {
            int lightLevel;
            int y = topY;

            while (y > 0)
            {
                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);

                if (!blockMD.isAir())
                {
                    strata.blocksFound = true;
                    blockAboveMD = dataCache.getBlockMD(chunkMd, x, y + 1, z);

                    if(blockMD.isLava() && blockAboveMD.isLava())
                    {
                        // Ignores the myriad tiny one-block pockets of lava in the Nether
                        lavaBlockMD = blockMD;
                    }

                    if(blockAboveMD.isAir() || blockAboveMD.hasFlag(BlockMD.Flag.OpenToSky))
                    {
                        if (chunkMd.hasNoSky || !chunkMd.stub.canBlockSeeTheSky(x, y + 1, z))
                        {
                            lightLevel = getSliceLightLevel(chunkMd, x, y, z, true);

                            if (lightLevel > 0)
                            {
                                strata.push(neighbors, chunkMd, blockMD, x, y, z, lightLevel);
                                if (blockMD.getAlpha() == 1f || !mapTransparency)
                                {
                                    break;
                                }
                            }
                            else if (y < minY)
                            {
                                break;
                            }
                        }
                    }
                }
                y--;
            }
        }
        finally
        {
            // Corner case where the column has lava but no air in it.
            // This is a nether thing
            if(chunkMd.hasNoSky && strata.isEmpty() && lavaBlockMD!=null)
            {
                strata.push(neighbors, chunkMd, lavaBlockMD, x, topY, z, 14);
            }
        }
    }

    /**
     * Paint the image with the color derived from a BlockStack
     */
    protected boolean paintStrata(final Strata strata, final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice, final ChunkMD.Set neighbors, final int x, final int y, final int z)
    {
        if (strata.isEmpty())
        {
            paintBadBlock(x, y, z, g2D);
            return false;
        }

        try
        {
            Stratum stratum = null;
            BlockMD blockMD = null;

            while (!strata.isEmpty())
            {
                stratum = strata.pop(this, true);

                // Simple surface render
                if (strata.renderCaveColor == null)
                {
                    strata.renderCaveColor = stratum.caveColor;
                }
                else
                {
                    strata.renderCaveColor = RGB.blendWith(strata.renderCaveColor, stratum.caveColor, stratum.blockMD.getAlpha());
                }

                blockMD = stratum.blockMD;
                strata.release(stratum);

            } // end color stack

            // Shouldn't happen
            if (strata.renderCaveColor == null)
            {
                paintBadBlock(x, y, z, g2D);
                return false;
            }

            // Now add bevel for slope
            if (!(blockMD.hasFlag(BlockMD.Flag.NoShadow)))
            {
                float slope = getSlope(chunkMd, blockMD, neighbors, x, vSlice, z);
                if (slope != 1f)
                {
                    strata.renderCaveColor = RGB.bevelSlope(strata.renderCaveColor, slope);
                }
            }

            // And draw to the actual chunkimage
            g2D.setComposite(OPAQUE);
            g2D.setPaint(RGB.paintOf(strata.renderCaveColor));
            g2D.fillRect(x, z, 1, 1);
        }
        catch (RuntimeException e)
        {
            paintBadBlock(x, y, z, g2D);
            throw e;
        }

        return true;
    }

    /**
     * Get block height within slice.
     */
    @Override
    protected int getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY, boolean ignoreWater)
    {
        Integer[][] blockSliceHeights = chunkMd.sliceHeights.get(vSlice);
        if(blockSliceHeights==null)
        {
            blockSliceHeights = new Integer[16][16];
            chunkMd.sliceHeights.put(vSlice, blockSliceHeights);
        }

        Integer y = blockSliceHeights[x][z];

        if(y!=null)
        {
            return y;
        }

        try
        {
            y = sliceMaxY-1;

            BlockMD blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
            BlockMD blockMDAbove = dataCache.getBlockMD(chunkMd, x, y+1, z);

            while (y > 0)
            {
                if(ignoreWater && blockMD.isWater())
                {
                    y--;
                }

                if (blockMDAbove.isAir() || blockMDAbove.hasTranparency() || blockMDAbove.hasFlag(BlockMD.Flag.OpenToSky))
                {
                    if(!blockMD.isAir())
                    {
                        break;
                    }
                }

                y--;

                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
                blockMDAbove = dataCache.getBlockMD(chunkMd, x, y+1, z);
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warning("Couldn't get safe slice block height at " + x + "," + z + ": " + e);
            y = sliceMaxY;
        }

        blockSliceHeights[x][z] = y;
        return y;
    }

    /**
     * Get the light level for the block in the slice.  Can be overridden to provide an ambient light minimum.
     */
    protected int getSliceLightLevel(ChunkMD chunkMd, int x, int y, int z, boolean adjusted)
    {
        return mapCaveLighting ? chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z) : 15;
    }
}
