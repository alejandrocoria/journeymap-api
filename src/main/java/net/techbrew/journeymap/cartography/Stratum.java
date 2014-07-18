/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography;

import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;

/**
 * Created by mwoodman on 7/3/2014.
 */
public class Stratum
{
    private ChunkMD chunkMd;
    private BlockMD blockMD;
    private int x;
    private int y;
    private int z;
    private Integer lightLevel;
    private Integer lightOpacity;
    private boolean isWater;
    private Integer dayColor;
    private Integer nightColor;
    private Integer caveColor;

    Stratum set(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z, Integer lightLevel)
    {
        this.setChunkMd(chunkMd);
        this.setBlockMD(blockMD);
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setWater(blockMD != null && blockMD.isWater());
        this.setLightLevel((blockMD == null) ? null : (lightLevel != null) ? lightLevel : chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z));
        this.setLightOpacity((blockMD == null) ? null : chunkMd.getLightOpacity(blockMD, x, y, z));
        setDayColor(null);
        setNightColor(null);
        setCaveColor(null);
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
        return "Stratum{" +
                "x=" + getX() +
                ", y=" + getY() +
                ", z=" + getZ() +
                ", lightLevel=" + getLightLevel() +
                ", lightOpacity=" + getLightOpacity() +
                ", isWater=" + isWater() +
                ", dayColor=" + (getDayColor() == null ? null : new Color(getDayColor())) +
                ", nightColor=" + (getNightColor() == null ? null : new Color(getNightColor())) +
                ", caveColor=" + (getCaveColor() == null ? null : new Color(getCaveColor())) +
                '}';
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
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
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
        return z;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public Integer getLightLevel()
    {
        return lightLevel;
    }

    public void setLightLevel(Integer lightLevel)
    {
        this.lightLevel = lightLevel;
    }

    public Integer getLightOpacity()
    {
        return lightOpacity;
    }

    public void setLightOpacity(Integer lightOpacity)
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
}
