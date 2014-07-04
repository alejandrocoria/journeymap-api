package net.techbrew.journeymap.cartography;

import net.minecraft.block.Block;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Stack of Stratum with simplistic object pooling to avoid heap thrash, since Stratum are used quite a bit.
 */
class Strata
{
    final String name;
    final int initialPoolSize;
    final int poolGrowthIncrement;

    final List<Stratum> poolFree;
    final List<Stratum> poolUsed;

    Strata(String name, int initialPoolSize, int poolGrowthIncrement)
    {
        this.name = name;
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

    private Stack<Stratum> stack = new Stack<Stratum>();
    protected Integer topY = null;
    protected Integer bottomY = null;
    protected Integer topWaterY = null;
    protected Integer bottomWaterY = null;
    protected Integer maxLightLevel = null;
    protected Integer waterColor = null;
    protected Integer renderDayColor = null;
    protected Integer renderNightColor = null;
    protected int lightAttenuation = 0;

    void reset()
    {
        topY = null;
        bottomY = null;
        topWaterY = null;
        bottomWaterY = null;
        maxLightLevel = null;
        waterColor = null;
        renderDayColor = null;
        renderNightColor = null;
        lightAttenuation = 0;

        stack.clear();

        while (!poolUsed.isEmpty())
        {
            release(poolUsed.get(0));
        }
    }

    void release(Stratum stratum)
    {
        stratum.set(null, null, -1,-1,-1);
        poolUsed.remove(stratum);
        poolFree.add(stratum);
    }

    Stratum push(final ChunkMD.Set neighbors, ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        try
        {
            StatTimer timer = blockMD.isWater() ? StatTimer.get("Strata.push-water") : StatTimer.get("Strata.push");
            timer.start();

            // Alocate from pool, push to stack, and set vars on stratum
            Stratum stratum = stack.push(allocate().set(chunkMd, blockMD, x, y, z));

            // Update Strata's basic data
            topY = (topY == null) ? y : Math.max(topY, y);
            bottomY = (bottomY == null) ? y : Math.min(bottomY, y);
            maxLightLevel = (maxLightLevel == null) ? stratum.lightLevel : Math.max(maxLightLevel, stratum.lightLevel);
            lightAttenuation += stratum.lightOpacity;

            // Update Strata's water data
            if (blockMD.isWater())
            {
                topWaterY = (topWaterY == null) ? y : Math.max(topWaterY, y);
                bottomWaterY = (bottomWaterY == null) ? y : Math.min(bottomWaterY, y);
                if (waterColor == null)
                {
                    waterColor = getAverageWaterColor(neighbors, (chunkMd.coord.chunkXPos << 4) + x, y, (chunkMd.coord.chunkZPos << 4) + z);
                    if (waterColor == null)
                    {
                        // This shouldn't happen. But if it did, it'd be too spammy to log.
                        waterColor = 0x2525CD;
                    }

                    waterColor = RGB.darken(waterColor, .85f); // magic # to match how it looks in game
                }
            }

            timer.stop();
            return stratum;
        }
        catch(Throwable t)
        {
            JourneyMap.getLogger().severe("Couldn't push Stratum into stack: " + t);
            return null;
        }
    }

    Stratum pop(boolean ignoreMiddleWater)
    {
        Stratum stratum = stack.pop();
        if(stratum==null)
        {
            throw new IllegalStateException("Strata empty, can't pop");
        }

        lightAttenuation = Math.max(0, lightAttenuation-stratum.lightOpacity);

        // Skip middle water blocks
        if(ignoreMiddleWater && stratum.isWater && isWaterAbove(stratum))
        {
            return pop(true);
        }

        return setColors(stratum);
    }

    int depth()
    {
        return stack.isEmpty() ? 0 : topY-bottomY+1;
    }

    boolean isEmpty()
    {
        return stack.isEmpty();
    }

    boolean hasWater()
    {
        return topWaterY!=null;
    }

    private Stratum setColors(Stratum stratum)
    {
        if(stratum.lightLevel==null || stratum.y<0)
        {
            throw new IllegalStateException("Stratum wasn't initialized");
        }

        // Daylight is the greater of max light attenuated through the stack / the stratum's inherant light level
        float daylightDiff = stratum.y==topY ? 1f : Math.max(stratum.lightLevel, Math.max(1, 15-lightAttenuation)) / 15f;

        // Nightlight is the stratum's inherant light level, clamped between 20% and 80% of max light (magic #s to match how it looks in game)
        float nightLightDiff = Math.max(.2F, Math.min(.8F, ((Math.max(2, stratum.lightLevel)) / 15F)) );

        // Set basic block color
        if(stratum.isWater)
        {
            stratum.dayColor = waterColor;
        }
        else
        {
            stratum.dayColor = stratum.blockMD.getColor(stratum.chunkMd, stratum.x, stratum.y, stratum.z);

            // Brighten glowstone / redstone lamp
            if(stratum.blockMD.getBlock()== Block.glowStone || stratum.blockMD.getBlock()== Block.redstoneLampActive)
            {
                stratum.dayColor = RGB.darken(stratum.dayColor, 1.2f); // magic # to match how it looks in game
            }
        }

        if(isWaterAbove(stratum))
        {
            // Darken for daylight filtered down
            stratum.dayColor = RGB.darken(stratum.dayColor,  Math.max(daylightDiff, .25f)); // magic # to match how it looks in game

            // Darken for night light and blend with watercolor above
            stratum.nightColor =  RGB.moonlight(RGB.blendWith(waterColor, stratum.dayColor, nightLightDiff), nightLightDiff);

            if(depth()<3)
            {
                // magic # cheat to get bluer blend in shallow water
                daylightDiff = (1-(depth()/4f));
            }

            // Blend day color with watercolor above
            stratum.dayColor = RGB.blendWith(waterColor, stratum.dayColor, Math.max(daylightDiff, .2f));
        }
        else
        {
            // Just darken based on light levels
            stratum.nightColor = RGB.moonlight(stratum.dayColor, nightLightDiff);
            if(daylightDiff<1f)
            {
                stratum.dayColor = RGB.darken(stratum.dayColor, daylightDiff);
            }
        }


        return stratum;
    }

    boolean isWaterAbove(Stratum stratum)
    {
        return topWaterY!=null && topWaterY> stratum.y;
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
        ChunkMD chunk = neighbors.get(new ChunkCoordIntPair(blockX>>4, blockZ>>4));
        if(chunk!=null)
        {
            BlockMD block = BlockMD.getBlockMD(chunk, blockX & 15, blockY, blockZ & 15);
            if(block!=null && block.isWater())
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
                ", topY=" + topY +
                ", bottomY=" + bottomY +
                ", topWaterY=" + topWaterY +
                ", bottomWaterY=" + bottomWaterY +
                ", maxLightLevel=" + maxLightLevel +
                ", waterColor=" + RGB.toString(waterColor) +
                ", renderDayColor=" + RGB.toString(renderDayColor) +
                ", renderNightColor=" + RGB.toString(renderNightColor) +
                ", lightAttenuation=" + lightAttenuation +
                '}';
    }
}
