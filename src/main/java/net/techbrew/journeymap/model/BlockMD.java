/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLeaves;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.cartography.RGB;
import net.techbrew.journeymap.data.DataCache;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;

/**
 * Block Metadata
 */
public class BlockMD
{
    /**
     * The Uid.
     */
    public final GameRegistry.UniqueIdentifier uid;
    /**
     * The Meta.
     */
    public final int meta;
    /**
     * The Name.
     */
    public final String name;
    private final EnumSet<Flag> flags;
    private transient Block block;
    private Integer color;
    private float alpha;

    /**
     * Instantiates a new BlockMD.
     *
     * @param displayName the display name
     * @param block       the block
     * @param meta        the meta
     * @param alpha       the alpha
     * @param flags       the flags
     */
    BlockMD(String displayName, Block block, int meta, float alpha, BlockMD.Flag... flags)
    {
        this(displayName, block, meta, alpha, flags.length == 0 ? EnumSet.noneOf(BlockMD.Flag.class) : EnumSet.copyOf(Arrays.asList(flags)));
    }

    /**
     * Instantiates a new BlockMD.
     *
     * @param displayName the display name
     * @param block       the block
     * @param meta        the meta
     * @param alpha       the alpha
     * @param flags       the flags
     */
    BlockMD(String displayName, Block block, int meta, float alpha, EnumSet<BlockMD.Flag> flags)
    {
        this(displayName, DataCache.instance().getBlockMetadata().findUniqueIdentifierFor(block), block, meta, alpha, flags);
    }

    /**
     * Instantiates a new BlockMD.
     *
     * @param displayName the display name
     * @param block       the block
     * @param meta        the meta
     * @param alpha       the alpha
     * @param flags       the flags
     */
    BlockMD(String displayName, GameRegistry.UniqueIdentifier uid, Block block, int meta, float alpha, EnumSet<BlockMD.Flag> flags)
    {
        this.uid = uid;
        this.meta = meta;
        this.block = block;
        this.name = (displayName == null) ? this.uid.name : displayName;
        this.flags = flags;
        this.alpha = alpha;
        if (block == null)
        {
            // TODO: Get this out of here.
            if ("Void".equals(name))
            {
                color = RGB.toInteger(17, 12, 25);
            }
        }
    }

    public static String getBlockName(Block block, int meta)
    {
        String name = block.getUnlocalizedName();
        try
        {
            // Gotta love this.
            Item item = Item.getItemFromBlock(block);
            if (item == null)
            {
                item = block.getItemDropped(0, new Random(), 0);
            }
            if (item != null)
            {
                ItemStack stack = new ItemStack(item, 1, block.damageDropped(meta));
                name = stack.getDisplayName();
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().debug("Displayname not available for " + name);
        }

        if (name.startsWith("tile"))
        {
            name = block.getClass().getSimpleName().replaceAll("Block", "");
        }
        return name;
    }

    /**
     * Whether BlockMD has the flag.
     *
     * @param checkFlag the flag to check for
     * @return true if found
     */
    public boolean hasFlag(Flag checkFlag)
    {
        return flags.contains(checkFlag);
    }

    /**
     * Add flags.
     *
     * @param addFlags the add flags
     */
    public void addFlags(Flag... addFlags)
    {
        for (Flag flag : addFlags)
        {
            this.flags.add(flag);
        }
    }

    /**
     * Gets block color using chunk-local coords (x and z in {0-15} )
     *
     * @param chunkMd the chunk md
     * @param x       the x
     * @param y       the y
     * @param z       the z
     * @return the color
     */
    public int getColor(ChunkMD chunkMd, int x, int y, int z)
    {
        if (this.color != null)
        {
            return this.color;
        }
        else
        {
            Integer color = ColorCache.instance().getBlockColor(chunkMd, this, x, y, z);
            if (color == null)
            {
                this.color = Color.black.getRGB();
                JourneyMap.getLogger().warn("Could not get color for " + block);
                addFlags(Flag.Error);
            }
            else if (!isBiomeColored())
            {
                this.color = color; // save
            }

            return color;
        }
    }

    /**
     * Gets alpha.
     *
     * @return the alpha
     */
    public float getAlpha()
    {
        return alpha;
    }

    /**
     * Sets alpha.
     *
     * @param alpha the alpha
     */
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

    /**
     * Gets block.
     *
     * @return the block
     */
    public Block getBlock()
    {
        if (block == null)
        {
            block = GameRegistry.findBlock(uid.modId, uid.name);
            if (block == null)
            {
                block = Blocks.air;
            }
        }
        return block;
    }

    /**
     * Has tranparency.
     *
     * @return the boolean
     */
    public boolean hasTranparency()
    {
        return hasFlag(Flag.Transparency);
    }

    /**
     * Is air.
     *
     * @return the boolean
     */
    public boolean isAir()
    {
        return getBlock() instanceof BlockAir || hasFlag(Flag.HasAir);
    }

    /**
     * Is ice.
     *
     * @return the boolean
     */
    public boolean isIce()
    {
        return block == Blocks.ice;
    }

    /**
     * Is torch.
     *
     * @return the boolean
     */
    public boolean isTorch()
    {
        getBlock();
        return block == Blocks.torch || block == Blocks.redstone_torch || block == Blocks.unlit_redstone_torch;
    }

    /**
     * Is water.
     *
     * @return the boolean
     */
    public boolean isWater()
    {
        getBlock();
        return block == Blocks.water || block == Blocks.flowing_water;
    }

    /**
     * Is transparent roof.
     *
     * @return the boolean
     */
    public boolean isTransparentRoof()
    {
        return hasFlag(Flag.TransparentRoof);
    }

    /**
     * Is lava.
     *
     * @return the boolean
     */
    public boolean isLava()
    {
        getBlock();
        return block == Blocks.lava || block == Blocks.flowing_lava;
    }

    /**
     * Is foliage.
     *
     * @return the boolean
     */
    public boolean isFoliage()
    {
        return getBlock() instanceof BlockLeaves;
    }

    /**
     * Is biome colored.
     *
     * @return the boolean
     */
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

    /**
     * To cache key string.
     *
     * @return the string
     */
    public String toCacheKeyString(GameRegistry.UniqueIdentifier uid, int meta)
    {
        return String.format("%s:%s:%s", uid.modId, uid.name, meta);
    }

    @Override
    public String toString()
    {
        return String.format("BlockMD [%s]", toCacheKeyString(uid, meta));
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Flags indicating special behaviors of Blocks.
     */
    public enum Flag
    {
        /**
         * Block should be treated like air.
         */
        HasAir,

        /**
         * Block color is determined by biome.
         */
        BiomeColor,

        /**
         * Block color is custom + determined by biome.
         */
        CustomBiomeColor,

        /**
         * Block doesn't count as overhead cover.
         */
        OpenToSky,

        /**
         * Block shouldn't cast a shadow.
         */
        NoShadow,

        /**
         * Block's color should come from the side 2 texture.
         */
        Side2Texture,

        /**
         * Block isn't opaque.
         */
        Transparency,

        /**
         * Block was processed with errors.
         */
        Error,

        /**
         * Block is transparent and is ignored by Minecraft's chunk heights.
         */
        TransparentRoof,

        /**
         * Block is a non-crop plant
         */
        Plant,

        /**
         * Block is a crop
         */
        Crop,

        /**
         * Block is a tile entity
         */
        TileEntity
    }


}
