package net.techbrew.journeymap.cartography;

import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;

/**
* Created by mwoodman on 7/3/2014.
*/
class Stratum
{
    ChunkMD chunkMd;
    BlockMD blockMD;
    int x;
    int y;
    int z;
    Integer lightLevel;
    Integer lightOpacity;
    boolean isWater;
    Integer dayColor;
    Integer nightColor;

    Stratum set(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        this.chunkMd = chunkMd;
        this.blockMD = blockMD;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isWater = blockMD!=null && blockMD.isWater();
        this.lightLevel = (blockMD==null) ? null : chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z);
        this.lightOpacity = (blockMD==null) ? null : chunkMd.getLightOpacity(blockMD, x, y, z);
        dayColor = null;
        nightColor = null;
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

        if (y != that.y)
        {
            return false;
        }
        if (blockMD != null ? !blockMD.equals(that.blockMD) : that.blockMD != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = blockMD != null ? blockMD.hashCode() : 0;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString()
    {
        return "Stratum{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", lightLevel=" + lightLevel +
                ", lightOpacity=" + lightOpacity +
                ", isWater=" + isWater +
                ", dayColor="  + (dayColor==null ? null : new Color(dayColor)) +
                ", nightColor="  + (nightColor==null ? null : new Color(nightColor)) +
                '}';
    }
}
