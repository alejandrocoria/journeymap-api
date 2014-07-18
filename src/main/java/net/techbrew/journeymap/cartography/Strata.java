/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography;

import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Stack of Stratum with simplistic object pooling to avoid heap thrash, since Stratum are used quite a bit.
 */
public class Strata
{
    final DataCache dataCache = DataCache.instance();
    private final boolean mapCaveLighting = JourneyMap.getInstance().coreProperties.mapCaveLighting.get();
    final String name;
    final int initialPoolSize;
    final int poolGrowthIncrement;

    final List<Stratum> poolFree;
    final List<Stratum> poolUsed;
    private final boolean underground;
    private Integer topY = null;
    private Integer bottomY = null;
    private Integer topWaterY = null;
    private Integer bottomWaterY = null;
    private Integer maxLightLevel = null;
    private Integer waterColor = null;
    private Integer renderDayColor = null;
    private Integer renderNightColor = null;
    private Integer renderCaveColor = null;
    private int lightAttenuation = 0;
    private boolean blocksFound = false;
    private Stack<Stratum> stack = new Stack<Stratum>();

    public Strata(String name, int initialPoolSize, int poolGrowthIncrement, boolean underground)
    {
        this.name = name;
        this.underground = underground;
        this.initialPoolSize = initialPoolSize;
        this.poolGrowthIncrement = poolGrowthIncrement;
        poolFree = new ArrayList<Stratum>(initialPoolSize);
        poolUsed = new ArrayList<Stratum>(initialPoolSize);
        growFreePool(initialPoolSize);
    }

    private Stratum allocate()
    {
        if (poolFree.isEmpty())
        {
            int amount = poolUsed.isEmpty() ? initialPoolSize : poolGrowthIncrement;
            growFreePool(amount);
            JourneyMap.getLogger().info(String.format("Grew Strata pool for '%s' by '%s'. Free: %s, Used: %s", name, amount, poolFree.size(), poolUsed.size()));
        }

        Stratum bc = poolFree.remove(0);
        poolUsed.add(bc);
        return bc;
    }

    private void growFreePool(int amount)
    {
        for (int i = 0; i < amount; i++)
        {
            poolFree.add(new Stratum());
        }
    }

    public void reset()
    {
        setTopY(null);
        setBottomY(null);
        setTopWaterY(null);
        setBottomWaterY(null);
        setMaxLightLevel(null);
        setWaterColor(null);
        setRenderDayColor(null);
        setRenderNightColor(null);
        setRenderCaveColor(null);
        setLightAttenuation(0);
        setBlocksFound(false);

        stack.clear();

        while (!poolUsed.isEmpty())
        {
            release(poolUsed.get(0));
        }
    }

    public void release(Stratum stratum)
    {
        stratum.set(null, null, -1, -1, -1, null);
        poolUsed.remove(stratum);
        poolFree.add(stratum);
    }

    public Stratum push(final ChunkMD.Set neighbors, ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        return push(neighbors, chunkMd, blockMD, x, y, z, null);
    }

    public Stratum push(final ChunkMD.Set neighbors, ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z, Integer lightLevel)
    {
        try
        {
//            StatTimer timer = blockMD.isWater() ? StatTimer.get("Strata.push-water") : StatTimer.get("Strata.push");
//            timer.start();

            // Alocate from pool, push to stack, and set vars on stratum
            Stratum stratum = stack.push(allocate().set(chunkMd, blockMD, x, y, z, lightLevel));

            // Update Strata's basic data
            setTopY((getTopY() == null) ? y : Math.max(getTopY(), y));
            setBottomY((getBottomY() == null) ? y : Math.min(getBottomY(), y));
            setMaxLightLevel((getMaxLightLevel() == null) ? stratum.getLightLevel() : Math.max(getMaxLightLevel(), stratum.getLightLevel()));
            setLightAttenuation(getLightAttenuation() + stratum.getLightOpacity());
            setBlocksFound(true);

            // Update Strata's water data
            if (blockMD.isWater())
            {
                setTopWaterY((getTopWaterY() == null) ? y : Math.max(getTopWaterY(), y));
                setBottomWaterY((getBottomWaterY() == null) ? y : Math.min(getBottomWaterY(), y));
                if (getWaterColor() == null)
                {
                    setWaterColor(getAverageWaterColor(neighbors, (chunkMd.coord.chunkXPos << 4) + x, y, (chunkMd.coord.chunkZPos << 4) + z));
                    if (getWaterColor() == null)
                    {
                        // This shouldn't happen. But if it did, it'd be too spammy to log.
                        setWaterColor(0x2525CD);
                    }

                    setWaterColor(RGB.darken(getWaterColor(), .85f)); // magic # to match how it looks in game
                }
            }

            //timer.stop();
            return stratum;
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe("Couldn't push Stratum into stack: " + t);
            return null;
        }
    }

    public Stratum pop(IChunkRenderer renderer, boolean ignoreMiddleWater)
    {
        Stratum stratum = stack.pop();
        if (stratum == null)
        {
            throw new IllegalStateException("Strata empty, can't pop");
        }

        setLightAttenuation(Math.max(0, getLightAttenuation() - stratum.getLightOpacity()));

        // Skip middle water blocks
        if (ignoreMiddleWater && stratum.isWater() && isWaterAbove(stratum))
        {
            return pop(renderer, true);
        }

        renderer.setStratumColors(stratum, getLightAttenuation(), getWaterColor(), isWaterAbove(stratum), isUnderground(), isMapCaveLighting());
        return stratum;
    }

    int depth()
    {
        return stack.isEmpty() ? 0 : getTopY() - getBottomY() + 1;
    }

    public boolean isEmpty()
    {
        return stack.isEmpty();
    }

    boolean hasWater()
    {
        return getTopWaterY() != null;
    }

    boolean isWaterAbove(Stratum stratum)
    {
        return getTopWaterY() != null && getTopWaterY() > stratum.getY();
    }

    /**
     * Requires absolute x,y,z coordinates. Returns null if water not at checked locations.
     */
    Integer getAverageWaterColor(final ChunkMD.Set neighbors, int blockX, int blockY, int blockZ)
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
     * Requires absolute x,y,z coordinates. Returns null if water not at that location.
     */
    Integer getWaterColor(final ChunkMD.Set neighbors, int blockX, int blockY, int blockZ)
    {
        ChunkMD chunk = neighbors.get(new ChunkCoordIntPair(blockX >> 4, blockZ >> 4));
        if (chunk != null)
        {
            BlockMD block = dataCache.getBlockMD(chunk, blockX & 15, blockY, blockZ & 15);
            if (block != null && block.isWater())
            {
                return block.getColor(chunk, blockX & 15, blockY, blockZ & 15);
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return "Strata{" +
                "name='" + name + '\'' +
                ", initialPoolSize=" + initialPoolSize +
                ", poolGrowthIncrement=" + poolGrowthIncrement +
                ", poolFree=" + poolFree.size() +
                ", poolUsed=" + poolUsed.size() +
                ", stack=" + stack.size() +
                ", topY=" + getTopY() +
                ", bottomY=" + getBottomY() +
                ", topWaterY=" + getTopWaterY() +
                ", bottomWaterY=" + getBottomWaterY() +
                ", maxLightLevel=" + getMaxLightLevel() +
                ", waterColor=" + RGB.toString(getWaterColor()) +
                ", renderDayColor=" + RGB.toString(getRenderDayColor()) +
                ", renderNightColor=" + RGB.toString(getRenderNightColor()) +
                ", lightAttenuation=" + getLightAttenuation() +
                '}';
    }

    public boolean isMapCaveLighting()
    {
        return mapCaveLighting;
    }

    public boolean isUnderground()
    {
        return underground;
    }

    public Integer getTopY()
    {
        return topY;
    }

    public void setTopY(Integer topY)
    {
        this.topY = topY;
    }

    public Integer getBottomY()
    {
        return bottomY;
    }

    public void setBottomY(Integer bottomY)
    {
        this.bottomY = bottomY;
    }

    public Integer getTopWaterY()
    {
        return topWaterY;
    }

    public void setTopWaterY(Integer topWaterY)
    {
        this.topWaterY = topWaterY;
    }

    public Integer getBottomWaterY()
    {
        return bottomWaterY;
    }

    public void setBottomWaterY(Integer bottomWaterY)
    {
        this.bottomWaterY = bottomWaterY;
    }

    public Integer getMaxLightLevel()
    {
        return maxLightLevel;
    }

    public void setMaxLightLevel(Integer maxLightLevel)
    {
        this.maxLightLevel = maxLightLevel;
    }

    public Integer getWaterColor()
    {
        return waterColor;
    }

    public void setWaterColor(Integer waterColor)
    {
        this.waterColor = waterColor;
    }

    public Integer getRenderDayColor()
    {
        return renderDayColor;
    }

    public void setRenderDayColor(Integer renderDayColor)
    {
        this.renderDayColor = renderDayColor;
    }

    public Integer getRenderNightColor()
    {
        return renderNightColor;
    }

    public void setRenderNightColor(Integer renderNightColor)
    {
        this.renderNightColor = renderNightColor;
    }

    public Integer getRenderCaveColor()
    {
        return renderCaveColor;
    }

    public void setRenderCaveColor(Integer renderCaveColor)
    {
        this.renderCaveColor = renderCaveColor;
    }

    public int getLightAttenuation()
    {
        return lightAttenuation;
    }

    public void setLightAttenuation(int lightAttenuation)
    {
        this.lightAttenuation = lightAttenuation;
    }

    public boolean isBlocksFound()
    {
        return blocksFound;
    }

    public void setBlocksFound(boolean blocksFound)
    {
        this.blocksFound = blocksFound;
    }
}
