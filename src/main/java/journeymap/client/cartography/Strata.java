/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography;

import journeymap.client.log.JMLogger;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;

import java.util.Stack;

/**
 * Stack of Stratum with simplistic object pooling to avoid heap thrash, since Stratum are used quite a bit.
 */
public class Strata
{
    /**
     * The Name.
     */
    final String name;
    /**
     * The Initial pool size.
     */
    final int initialPoolSize;
    /**
     * The Pool growth increment.
     */
    final int poolGrowthIncrement;
    private final boolean underground;
    private boolean mapCaveLighting = Journeymap.getClient().getCoreProperties().mapCaveLighting.get();
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
    private Stack<Stratum> unusedStack = new Stack<Stratum>();
    private Stack<Stratum> stack = new Stack<Stratum>();

    /**
     * Instantiates a new Strata.
     *
     * @param name                the name
     * @param initialPoolSize     the initial pool size
     * @param poolGrowthIncrement the pool growth increment
     * @param underground         the underground
     */
    public Strata(String name, int initialPoolSize, int poolGrowthIncrement, boolean underground)
    {
        this.name = name;
        this.underground = underground;
        this.initialPoolSize = initialPoolSize;
        this.poolGrowthIncrement = poolGrowthIncrement;
        growFreePool(initialPoolSize);
    }

    private Stratum allocate()
    {
        if (unusedStack.isEmpty())
        {
            int amount = stack.isEmpty() ? initialPoolSize : poolGrowthIncrement;
            growFreePool(amount);
            Journeymap.getLogger().debug(String.format("Grew Strata pool for '%s' by '%s'. Free: %s, Used: %s", name, amount, unusedStack.size(), stack.size()));
        }

        stack.push(unusedStack.pop());
        return stack.peek();
    }

    private void growFreePool(int amount)
    {
        for (int i = 0; i < amount; i++)
        {
            unusedStack.push(new Stratum());
        }
    }

    /**
     * Reset.
     */
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

        mapCaveLighting = Journeymap.getClient().getCoreProperties().mapCaveLighting.get();

        while (!stack.isEmpty())
        {
            release(stack.peek());
        }
    }

    /**
     * Release.
     *
     * @param stratum the stratum
     */
    public void release(Stratum stratum)
    {
        if (stratum == null)
        {
            Journeymap.getLogger().warn("Null stratum in pool.");
            return;
        }
        else
        {
            stratum.clear();
            unusedStack.add(0, stack.pop());
        }
    }

    /**
     * Push stratum.
     *
     * @param chunkMd the chunk md
     * @param blockMD the block md
     * @param x       the x
     * @param y       the y
     * @param z       the z
     * @return the stratum
     */
    public Stratum push(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        return push(chunkMd, blockMD, x, y, z, null);
    }

    /**
     * Push stratum.
     *
     * @param chunkMd    the chunk md
     * @param blockMD    the block md
     * @param localX     the local x
     * @param y          the y
     * @param localZ     the local z
     * @param lightLevel the light level
     * @return the stratum
     */
    public Stratum push(ChunkMD chunkMd, BlockMD blockMD, int localX, int y, int localZ, Integer lightLevel)
    {
        try
        {
            // Alocate to stack, and set vars on stratum
            Stratum stratum = allocate();
            stratum.set(chunkMd, blockMD, localX, y, localZ, lightLevel);

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
                    setWaterColor(blockMD.getColor(chunkMd, chunkMd.getBlockPos(localX, y, localZ)));
                }
            }

            return stratum;
        }
        catch (ChunkMD.ChunkMissingException e)
        {
            throw e;
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Couldn't push Stratum into stack: " + t.getMessage(), t);
            return null;
        }
    }

    /**
     * Next up stratum.
     *
     * @param renderer          the renderer
     * @param ignoreMiddleWater the ignore middle water
     * @return the stratum
     */
    public Stratum nextUp(IChunkRenderer renderer, boolean ignoreMiddleWater)
    {
        Stratum stratum = null;
        try
        {
            stratum = stack.peek();
            if (stratum.isUninitialized())
            {
                throw new IllegalStateException("Stratum wasn't initialized for Strata.nextUp()");
            }

            setLightAttenuation(Math.max(0, getLightAttenuation() - stratum.getLightOpacity()));

            // Skip middle water blocks
            if (ignoreMiddleWater && stratum.isWater() && isWaterAbove(stratum) && !stack.isEmpty())
            {
                release(stratum);
                return nextUp(renderer, true);
            }

            renderer.setStratumColors(stratum, getLightAttenuation(), getWaterColor(), isWaterAbove(stratum), isUnderground(), isMapCaveLighting());
            return stratum;
        }
        catch (RuntimeException t)
        {
            throw t;
        }
    }

    /**
     * Depth int.
     *
     * @return the int
     */
    int depth()
    {
        return stack.isEmpty() ? 0 : getTopY() - getBottomY() + 1;
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty()
    {
        return stack.isEmpty();
    }

    /**
     * Has water boolean.
     *
     * @return the boolean
     */
    boolean hasWater()
    {
        return getTopWaterY() != null;
    }

    /**
     * Is water above boolean.
     *
     * @param stratum the stratum
     * @return the boolean
     */
    boolean isWaterAbove(Stratum stratum)
    {
        return getTopWaterY() != null && getTopWaterY() > stratum.getY();
    }

    @Override
    public String toString()
    {
        return "Strata{" +
                "name='" + name + '\'' +
                ", initialPoolSize=" + initialPoolSize +
                ", poolGrowthIncrement=" + poolGrowthIncrement +
                ", stack=" + stack.size() +
                ", unusedStack=" + unusedStack.size() +
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

    /**
     * Is map cave lighting boolean.
     *
     * @return the boolean
     */
    public boolean isMapCaveLighting()
    {
        return mapCaveLighting;
    }

    /**
     * Is underground boolean.
     *
     * @return the boolean
     */
    public boolean isUnderground()
    {
        return underground;
    }

    /**
     * Gets top y.
     *
     * @return the top y
     */
    public Integer getTopY()
    {
        return topY;
    }

    /**
     * Sets top y.
     *
     * @param topY the top y
     */
    public void setTopY(Integer topY)
    {
        this.topY = topY;
    }

    /**
     * Gets bottom y.
     *
     * @return the bottom y
     */
    public Integer getBottomY()
    {
        return bottomY;
    }

    /**
     * Sets bottom y.
     *
     * @param bottomY the bottom y
     */
    public void setBottomY(Integer bottomY)
    {
        this.bottomY = bottomY;
    }

    /**
     * Gets top water y.
     *
     * @return the top water y
     */
    public Integer getTopWaterY()
    {
        return topWaterY;
    }

    /**
     * Sets top water y.
     *
     * @param topWaterY the top water y
     */
    public void setTopWaterY(Integer topWaterY)
    {
        this.topWaterY = topWaterY;
    }

    /**
     * Gets bottom water y.
     *
     * @return the bottom water y
     */
    public Integer getBottomWaterY()
    {
        return bottomWaterY;
    }

    /**
     * Sets bottom water y.
     *
     * @param bottomWaterY the bottom water y
     */
    public void setBottomWaterY(Integer bottomWaterY)
    {
        this.bottomWaterY = bottomWaterY;
    }

    /**
     * Gets max light level.
     *
     * @return the max light level
     */
    public Integer getMaxLightLevel()
    {
        return maxLightLevel;
    }

    /**
     * Sets max light level.
     *
     * @param maxLightLevel the max light level
     */
    public void setMaxLightLevel(Integer maxLightLevel)
    {
        this.maxLightLevel = maxLightLevel;
    }

    /**
     * Gets water color.
     *
     * @return the water color
     */
    public Integer getWaterColor()
    {
        return waterColor;
    }

    /**
     * Sets water color.
     *
     * @param waterColor the water color
     */
    public void setWaterColor(Integer waterColor)
    {
        this.waterColor = waterColor;
    }

    /**
     * Gets render day color.
     *
     * @return the render day color
     */
    public Integer getRenderDayColor()
    {
        return renderDayColor;
    }

    /**
     * Sets render day color.
     *
     * @param renderDayColor the render day color
     */
    public void setRenderDayColor(Integer renderDayColor)
    {
        this.renderDayColor = renderDayColor;
    }

    /**
     * Gets render night color.
     *
     * @return the render night color
     */
    public Integer getRenderNightColor()
    {
        return renderNightColor;
    }

    /**
     * Sets render night color.
     *
     * @param renderNightColor the render night color
     */
    public void setRenderNightColor(Integer renderNightColor)
    {
        this.renderNightColor = renderNightColor;
    }

    /**
     * Gets render cave color.
     *
     * @return the render cave color
     */
    public Integer getRenderCaveColor()
    {
        return renderCaveColor;
    }

    /**
     * Sets render cave color.
     *
     * @param renderCaveColor the render cave color
     */
    public void setRenderCaveColor(Integer renderCaveColor)
    {
        this.renderCaveColor = renderCaveColor;
    }

    /**
     * Gets light attenuation.
     *
     * @return the light attenuation
     */
    public int getLightAttenuation()
    {
        return lightAttenuation;
    }

    /**
     * Sets light attenuation.
     *
     * @param lightAttenuation the light attenuation
     */
    public void setLightAttenuation(int lightAttenuation)
    {
        this.lightAttenuation = lightAttenuation;
    }

    /**
     * Is blocks found boolean.
     *
     * @return the boolean
     */
    public boolean isBlocksFound()
    {
        return blocksFound;
    }

    /**
     * Sets blocks found.
     *
     * @param blocksFound the blocks found
     */
    public void setBlocksFound(boolean blocksFound)
    {
        this.blocksFound = blocksFound;
    }
}
