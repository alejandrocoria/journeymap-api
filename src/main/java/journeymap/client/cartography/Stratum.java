/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
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
    private boolean isWater;
    private Integer dayColor;
    private Integer nightColor;
    private Integer caveColor;
    private boolean uninitialized = true;

    Stratum()
    {
        this.id = IDGEN.incrementAndGet();
    }

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
            this.setWater(blockMD.isWater());
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
                            ", lightOpacity=" + getLightOpacity() +
                            ", isWater=" + isWater() +
                            ", dayColor=" + (getDayColor() == null ? null : new Color(getDayColor())) +
                            ", nightColor=" + (getNightColor() == null ? null : new Color(getNightColor())) +
                            ", caveColor=" + (getCaveColor() == null ? null : new Color(getCaveColor())));
        }
        else
        {
            return String.format(common, "");
        }
    }

    public ChunkMD getChunkMd()
    {
        return chunkMd;
    }

    public void setChunkMd(ChunkMD chunkMd)
    {
        this.chunkMd = chunkMd;
    }

    public BlockMD getBlockMD()
    {
        return blockMD;
    }

    public void setBlockMD(BlockMD blockMD)
    {
        this.blockMD = blockMD;
    }

    public int getX()
    {
        return localX;
    }

    public void setX(int x)
    {
        this.localX = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getZ()
    {
        return localZ;
    }

    public void setZ(int z)
    {
        this.localZ = z;
    }

    public int getLightLevel()
    {
        return lightLevel;
    }

    public void setLightLevel(int lightLevel)
    {
        this.lightLevel = lightLevel;
    }

    public int getLightOpacity()
    {
        return lightOpacity;
    }

    public void setLightOpacity(int lightOpacity)
    {
        this.lightOpacity = lightOpacity;
    }

    public boolean isWater()
    {
        return isWater;
    }

    public void setWater(boolean isWater)
    {
        this.isWater = isWater;
    }

    public Integer getDayColor()
    {
        return dayColor;
    }

    public void setDayColor(Integer dayColor)
    {
        this.dayColor = dayColor;
    }

    public Integer getNightColor()
    {
        return nightColor;
    }

    public void setNightColor(Integer nightColor)
    {
        this.nightColor = nightColor;
    }

    public Integer getCaveColor()
    {
        return caveColor;
    }

    public void setCaveColor(Integer caveColor)
    {
        this.caveColor = caveColor;
    }

    public BlockPos getBlockPos()
    {
        return new BlockPos(chunkMd.getBlockPos(this.localX, y, this.localZ));
    }

    public boolean isUninitialized()
    {
        return this.uninitialized;
    }

    public void clear()
    {
        this.uninitialized = true;
        this.setChunkMd(null);
        this.setBlockMD(null);
        this.setX(0);
        this.setY(-1);
        this.setZ(0);
        this.setWater(false);
        this.setLightLevel(-1);
        this.setLightOpacity(-1);
        setDayColor(null);
        setNightColor(null);
        setCaveColor(null);

        // TODO REMOVE
        // System.out.println("CLEARED " + this);
    }
}
