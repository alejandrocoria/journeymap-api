/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A single-block layer local to a chunk with color and transparency info assigned
 * so that the colors can be composited together in Strata.
 */
public class Stratum
{
    private static AtomicInteger IDGEN = new AtomicInteger(0);

    private final int id;

    private ChunkMD chunkMd;
    private BlockMD blockMD;
    private int localX;
    private int y;
    private int localZ;
    private int lightLevel;
    private int lightOpacity;
    private boolean isFluid;
    private Integer dayColor;
    private Integer nightColor;
    private Integer caveColor;
    private float worldAmbientLight;
    private boolean worldHasNoSky;
    private boolean uninitialized = true;

    /**
     * Instantiates a new Stratum.
     */
    Stratum()
    {
        this.id = IDGEN.incrementAndGet();
    }

    /**
     * Set stratum.
     *
     * @param chunkMd    the chunk md
     * @param blockMD    the block md
     * @param localX     the local x
     * @param y          the y
     * @param localZ     the local z
     * @param lightLevel the light level
     * @return the stratum
     */
    Stratum set(ChunkMD chunkMd, BlockMD blockMD, int localX, int y, int localZ, Integer lightLevel)
    {
        if (chunkMd == null || blockMD == null)
        {
            throw new IllegalStateException(String.format("Can't have nulls: %s, %s", chunkMd, blockMD));
        }
        try
        {
            this.setChunkMd(chunkMd);
            this.setBlockMD(blockMD);
            this.setX(localX);
            this.setY(y);
            this.setZ(localZ);
            this.setFluid(blockMD.isFluid() || blockMD.isFluid());
            if (blockMD.isLava())
            {
                this.setLightLevel(14);
            }
            else
            {
                this.setLightLevel((lightLevel != null) ? lightLevel : chunkMd.getSavedLightValue(localX, y + 1, localZ));
            }
            this.setLightOpacity(chunkMd.getLightOpacity(blockMD, localX, y, localZ));
            setDayColor(null);
            setNightColor(null);
            setCaveColor(null);
            this.uninitialized = false;

            // System.out.println("    SET " + this);
        }
        catch (RuntimeException t)
        {
            throw t;
        }
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Stratum that = (Stratum) o;

        if (getY() != that.getY())
        {
            return false;
        }
        if (getBlockMD() != null ? !getBlockMD().equals(that.getBlockMD()) : that.getBlockMD() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getBlockMD() != null ? getBlockMD().hashCode() : 0;
        result = 31 * result + getY();
        return result;
    }

    @Override
    public String toString()
    {
        String common = "Stratum{" + "id=" + id + ", uninitialized=" + uninitialized + "%s}";

        if (!uninitialized)
        {
            return String.format(common,
                    ", localX=" + getX() +
                            ", y=" + getY() +
                            ", localZ=" + getZ() +
                            ", lightLevel=" + getLightLevel() +
                            ", worldAmbientLight=" + getWorldAmbientLight() +
                            ", lightOpacity=" + getLightOpacity() +
                            ", isFluid=" + isFluid() +
                            ", dayColor=" + (getDayColor() == null ? null : new Color(getDayColor())) +
                            ", nightColor=" + (getNightColor() == null ? null : new Color(getNightColor())) +
                            ", caveColor=" + (getCaveColor() == null ? null : new Color(getCaveColor())));
        }
        else
        {
            return String.format(common, "");
        }
    }

    /**
     * Gets chunk md.
     *
     * @return the chunk md
     */
    public ChunkMD getChunkMd()
    {
        return chunkMd;
    }

    /**
     * Sets chunk md.
     *
     * @param chunkMd the chunk md
     */
    public void setChunkMd(ChunkMD chunkMd)
    {
        this.chunkMd = chunkMd;
        if (chunkMd != null)
        {
            this.worldAmbientLight = chunkMd.getWorld().getSunBrightness(1f) * 15;
            this.worldHasNoSky = chunkMd.hasNoSky();
        }
        else
        {
            this.worldAmbientLight = 15;
            this.worldHasNoSky = false;
        }
    }

    /**
     * Gets block md.
     *
     * @return the block md
     */
    public BlockMD getBlockMD()
    {
        if (blockMD.isFluid())
        {
            int xx = 0;
        }
        return blockMD;
    }

    /**
     * Sets block md.
     *
     * @param blockMD the block md
     */
    public void setBlockMD(BlockMD blockMD)
    {
        this.blockMD = blockMD;
    }

    /**
     * Gets world ambient light.
     *
     * @return the world ambient light
     */
    public float getWorldAmbientLight()
    {
        return this.worldAmbientLight;
    }

    /**
     * Gets world has no sky.
     *
     * @return the world has no sky
     */
    public boolean getWorldHasNoSky()
    {
        return worldHasNoSky;
    }

    /**
     * Gets x.
     *
     * @return the x
     */
    public int getX()
    {
        return localX;
    }

    /**
     * Sets x.
     *
     * @param x the x
     */
    public void setX(int x)
    {
        this.localX = x;
    }

    /**
     * Gets y.
     *
     * @return the y
     */
    public int getY()
    {
        return y;
    }

    /**
     * Sets y.
     *
     * @param y the y
     */
    public void setY(int y)
    {
        this.y = y;
    }

    /**
     * Gets z.
     *
     * @return the z
     */
    public int getZ()
    {
        return localZ;
    }

    /**
     * Sets z.
     *
     * @param z the z
     */
    public void setZ(int z)
    {
        this.localZ = z;
    }

    /**
     * Gets light level.
     *
     * @return the light level
     */
    public int getLightLevel()
    {
        return lightLevel;
    }

    /**
     * Sets light level.
     *
     * @param lightLevel the light level
     */
    public void setLightLevel(int lightLevel)
    {
        this.lightLevel = lightLevel;
    }

    /**
     * Gets light opacity.
     *
     * @return the light opacity
     */
    public int getLightOpacity()
    {
        return lightOpacity;
    }

    /**
     * Sets light opacity.
     *
     * @param lightOpacity the light opacity
     */
    public void setLightOpacity(int lightOpacity)
    {
        this.lightOpacity = lightOpacity;
    }

    /**
     * Is fluid boolean.
     *
     * @return the boolean
     */
    public boolean isFluid()
    {
        return isFluid;
    }

    /**
     * Sets isFluid.
     *
     * @param isFluid isFluid
     */
    public void setFluid(boolean isFluid)
    {
        this.isFluid = isFluid;
    }

    /**
     * Gets day color.
     *
     * @return the day color
     */
    public Integer getDayColor()
    {
        return dayColor;
    }

    /**
     * Sets day color.
     *
     * @param dayColor the day color
     */
    public void setDayColor(Integer dayColor)
    {
        this.dayColor = dayColor;
    }

    /**
     * Gets night color.
     *
     * @return the night color
     */
    public Integer getNightColor()
    {
        return nightColor;
    }

    /**
     * Sets night color.
     *
     * @param nightColor the night color
     */
    public void setNightColor(Integer nightColor)
    {
        this.nightColor = nightColor;
    }

    /**
     * Gets cave color.
     *
     * @return the cave color
     */
    public Integer getCaveColor()
    {
        return caveColor;
    }

    /**
     * Sets cave color.
     *
     * @param caveColor the cave color
     */
    public void setCaveColor(Integer caveColor)
    {
        this.caveColor = caveColor;
    }

    /**
     * Gets block pos.
     *
     * @return the block pos
     */
    public BlockPos getBlockPos()
    {
        return new BlockPos(chunkMd.getBlockPos(this.localX, y, this.localZ));
    }

    /**
     * Is uninitialized boolean.
     *
     * @return the boolean
     */
    public boolean isUninitialized()
    {
        return this.uninitialized;
    }

    /**
     * Clear.
     */
    public void clear()
    {
        this.uninitialized = true;
        this.worldAmbientLight = 15f;
        this.worldHasNoSky = false;
        this.setChunkMd(null);
        this.setBlockMD(null);
        this.setX(0);
        this.setY(-1);
        this.setZ(0);
        this.setFluid(false);
        this.setLightLevel(-1);
        this.setLightOpacity(-1);
        setDayColor(null);
        setNightColor(null);
        setCaveColor(null);

        // TODO REMOVE
        // System.out.println("CLEARED " + this);
    }
}
