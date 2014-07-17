package net.techbrew.journeymap.model;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.cartography.RGB;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Block Metadata
 */
public class BlockMD
{
    public final GameRegistry.UniqueIdentifier uid;
    public final int meta;
    public final String name;
    private final EnumSet<Flag> flags;
    private transient Block block;
    private Integer color;
    private float alpha;


    BlockMD(Block block, int meta, float alpha, BlockMD.Flag... flags)
    {
        this(null, block, meta, alpha, flags.length == 0 ? EnumSet.noneOf(BlockMD.Flag.class) : EnumSet.copyOf(Arrays.asList(flags)));
    }

    BlockMD(String displayName, Block block, int meta, float alpha, BlockMD.Flag... flags)
    {
        this(displayName, block, meta, alpha, flags.length == 0 ? EnumSet.noneOf(BlockMD.Flag.class) : EnumSet.copyOf(Arrays.asList(flags)));
    }

    BlockMD(String displayName, Block block, int meta, float alpha, EnumSet<BlockMD.Flag> flags)
    {
        this.uid = GameRegistry.findUniqueIdentifierFor(block);
        this.meta = meta;
        this.block = block;
        this.name = (displayName == null) ? this.uid.name : displayName;
        this.flags = flags;
        this.alpha = alpha;
        if (block == null)
        {
            if("Void".equals(name))
            {
                color = RGB.toInteger(17, 12, 25);
            }
            else
            {
                color = Color.black.getRGB();
            }
        }
    }

    public boolean hasFlag(Flag... checkFlags)
    {
        for (Flag flag : checkFlags)
        {
            if (flags.contains(flag))
            {
                return true;
            }
        }
        return false;
    }

    public void addFlags(Flag... addFlags)
    {
        for (Flag flag : addFlags)
        {
            this.flags.add(flag);
        }
    }

    /**
     * Gets block color using chunk-local coords (x and z in {0-15} )
     */
    public int getColor(ChunkMD chunkMd, int x, int y, int z)
    {
        if (this.color != null)
        {
            return this.color;
        }
        else
        {
            Integer color = ColorCache.getInstance().getBlockColor(chunkMd, this, x, y, z);
            if (color == null)
            {
                this.color = Color.black.getRGB();
                JourneyMap.getLogger().warning("Could not get color for " + block);
                addFlags(Flag.Error);
            }
            else if (!isBiomeColored())
            {
                this.color = color; // save
            }

            return color;
        }
    }

    public float getAlpha()
    {
        return alpha;
    }

    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
        if (alpha < 1f)
        {
            this.flags.add(Flag.Transparency);
        }
        else
        {
            if (this.hasFlag(Flag.Transparency))
            {
                this.flags.remove(Flag.Transparency);
            }
        }
    }

    public Block getBlock()
    {
        if(block==null){
            block = GameRegistry.findBlock(uid.modId, uid.name);
            if(block==null){
                block = Blocks.air;
            }
        }
        return block;
    }

    public boolean hasTranparency()
    {
        return hasFlag(Flag.Transparency);
    }

    public boolean isAir()
    {
        return getBlock() instanceof BlockAir || hasFlag(Flag.HasAir) || block.getMaterial() == Material.air;
    }

    public boolean isIce()
    {
        return block==Blocks.ice;
    }

    public boolean isTorch()
    {
        getBlock();
        return block== Blocks.torch||block==Blocks.redstone_torch||block==Blocks.unlit_redstone_torch;
    }

    public boolean isWater()
    {
        getBlock();
        return block== Blocks.water||block==Blocks.flowing_water;
    }

    public boolean isTransparentRoof()
    {
        return hasFlag(Flag.TransparentRoof);
    }

    public boolean isLava()
    {
        getBlock();
        return block== Blocks.lava||block==Blocks.flowing_lava;
    }

    public boolean isFoliage()
    {
        return getBlock() instanceof BlockLeaves;
    }

    public boolean isBiomeColored()
    {
        return flags.contains(Flag.BiomeColor) || flags.contains(Flag.CustomBiomeColor);
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

        BlockMD blockMD = (BlockMD) o;

        if (meta != blockMD.meta)
        {
            return false;
        }
        if (!uid.equals(blockMD.uid))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = uid.hashCode();
        result = 31 * result + meta;
        return result;
    }

    public String toCacheKeyString()
    {
        return BlockMDCache.toCacheKeyString(uid, meta);
    }

    @Override
    public String toString()
    {
        return String.format("BlockMD [%s]", BlockMDCache.toCacheKeyString(uid, meta));
    }

    public String getName()
    {
        return name;
    }

    public enum Flag
    {
        HasAir, BiomeColor, CustomBiomeColor, OpenToSky, NoShadow, Side2Texture, Transparency, Error, TransparentRoof
    }


}
