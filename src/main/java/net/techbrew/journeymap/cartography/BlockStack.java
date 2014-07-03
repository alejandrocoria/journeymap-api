package net.techbrew.journeymap.cartography;

import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Stack of value holders for colors derived at a given elevation.
 */
class BlockStack
{
    static final int POOL_SIZE = 40;
    static final int POOL_GROWSIZE = 4;

    static final List<BlockPos> POOL_FREE = new ArrayList<BlockPos>(POOL_SIZE);
    static final List<BlockPos> POOL_USED = new ArrayList<BlockPos>(POOL_SIZE);

    BlockPos allocate()
    {
        if (POOL_FREE.isEmpty())
        {
            int amount = POOL_USED.isEmpty() ? POOL_SIZE : POOL_GROWSIZE;
            for (int i = 0; i < amount; i++)
            {
                POOL_FREE.add(new BlockPos());
            }
            JourneyMap.getLogger().info("Grew BlockColors.pool by " + amount);
        }

        BlockPos bc = POOL_FREE.remove(0);
        POOL_USED.add(bc);
        return bc;
    }

    private Stack<BlockPos> stack = new Stack<BlockPos>();
    protected Integer topWaterY = null;
    protected Integer bottomY = null;
    protected Integer topY = null;
    protected Integer maxLightLevel = null;
    protected Integer waterColor = null;
    protected Integer renderDayColor = null;
    protected Integer renderNightColor = null;
    protected int lightAttenuation = 0;

    void reset()
    {
        topWaterY = null;
        topY = null;
        maxLightLevel = null;
        bottomY = null;
        waterColor = null;
        renderDayColor = null;
        renderNightColor = null;
        lightAttenuation = 0;

        stack.clear();
        while (!POOL_USED.isEmpty())
        {
            POOL_USED.get(0).release();
        }
    }

    BlockPos push(final ChunkMD.Set neighbors, ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
    {
        BlockPos blockPos = stack.push(allocate().set(chunkMd, blockMD, x, y, z));                

        if(blockMD.isWater())
        {
            topWaterY = (topWaterY==null) ? y : Math.max(topWaterY, y);
            if(waterColor==null)
            {
                waterColor = RGB.darken(getAverageWaterColor(neighbors, (chunkMd.coord.chunkXPos<<4) + x, y, (chunkMd.coord.chunkZPos<<4) + z), .85f);
            }
        }

        topY = (topY==null) ? y : Math.max(topY, y);
        bottomY = (bottomY==null) ? y : Math.min(bottomY, y);
        maxLightLevel = (maxLightLevel==null) ? blockPos.lightLevel : Math.max(maxLightLevel, blockPos.lightLevel);
        lightAttenuation += blockPos.lightOpacity;

        return blockPos;
    }

    BlockPos pop(boolean ignoreMiddleWater)
    {
        BlockPos blockPos = stack.pop();
        lightAttenuation -= blockPos.lightOpacity;

        // Skip middle water blocks
        while(ignoreMiddleWater && blockPos.isWater && isWaterAbove(blockPos))
        {
            blockPos.release();
            blockPos = stack.pop();
            lightAttenuation -= blockPos.lightOpacity;
        }

        return setColors(blockPos);
    }

    int depth()
    {
        return stack.isEmpty() ? 0 : topY-bottomY+1;
    }

    boolean isEmpty()
    {
        return stack.isEmpty();
    }

    boolean isUnderWater(int y)
    {
        return topWaterY!=null && y<=topWaterY;
    }

    boolean hasWater()
    {
        return topWaterY!=null && topWaterY>=bottomY;
    }

    private BlockPos setColors(BlockPos blockPos)
    {
        // Calculate effective light levels
        boolean waterAbove = isWaterAbove(blockPos);
        int blockLight =  blockPos.lightLevel;

        float daylightDiff = blockPos.y==topY ? 1f : Math.max(blockPos.lightLevel, Math.max(1, 15-lightAttenuation)) / 15f;
        float nightLightDiff = Math.max(.2F, Math.min(.8F, (Math.max(2, blockPos.lightLevel)) / 15F));

        // Set basic block color
        if(blockPos.isWater)
        {
            blockPos.dayColor = waterColor;
        }
        else
        {
            blockPos.dayColor = blockPos.blockMD.getColor(blockPos.chunkMd, blockPos.x, blockPos.y, blockPos.z);

            // Brighten glowstone
            if(blockPos.blockMD.getBlock()== Blocks.glowstone)
            {
                blockPos.dayColor = RGB.darken(blockPos.dayColor, 1.2f);
            }
        }

        if(waterAbove)
        {
            // Darken for daylight filtered down
            blockPos.dayColor = RGB.darken(blockPos.dayColor,  Math.max(daylightDiff, .25f));

            // Darken for night light and blend with watercolor above
            blockPos.nightColor =  RGB.moonlight(RGB.blendWith(waterColor, blockPos.dayColor, nightLightDiff), nightLightDiff);

            if(depth()<3)
            {
                // Cheat to get bluer blend in shallow water
                daylightDiff = (1-(depth()/4f));
            }

            // Blend day color with watercolor above
            blockPos.dayColor = RGB.blendWith(waterColor, blockPos.dayColor, Math.max(daylightDiff, .2f));
        }
        else
        {
            // Just darken based on light levels
            blockPos.nightColor = RGB.moonlight(blockPos.dayColor, nightLightDiff);
            if(daylightDiff<1f)
            {
                blockPos.dayColor = RGB.darken(blockPos.dayColor, daylightDiff);
            }
        }


        return blockPos;
    }

    boolean isWaterAbove(BlockPos blockPos)
    {
        return topWaterY!=null && topWaterY>blockPos.y;
    }

    /**
     * Requires absolute x,y,z coordinates
     */
    static int getAverageWaterColor(final ChunkMD.Set neighbors, int blockX, int blockY, int blockZ)
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
    static Integer getWaterColor(final ChunkMD.Set neighbors, int blockX, int blockY, int blockZ)
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

    static class BlockPos
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

        BlockPos set(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z)
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

        void release()
        {
            set(null, null, -1,-1,-1);
            POOL_USED.remove(this);
            POOL_FREE.add(this);
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

            BlockPos that = (BlockPos) o;

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
            return blockMD.toString();
        }
    }
}
