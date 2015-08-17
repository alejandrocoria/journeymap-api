/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import journeymap.client.JourneymapClient;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.client.model.mod.ModBlockDelegate;
import journeymap.common.Journeymap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.*;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Block MetaData Cache
 */
public class BlockMDCache extends CacheLoader<Block, HashMap<Integer, BlockMD>>
{
    public final BlockMD AIRBLOCK;
    public final BlockMD VOIDBLOCK;
    private final HashMap<Block, EnumSet<BlockMD.Flag>> blockFlags;
    private final HashMap<Block, Float> blockAlphas;
    private final ModBlockDelegate modBlockDelegate = new ModBlockDelegate();

    public BlockMDCache()
    {
        GameRegistry.UniqueIdentifier airUid = new GameRegistry.UniqueIdentifier(GameData.getBlockRegistry().getNameForObject(Blocks.air));
        AIRBLOCK = new BlockMD("Air", airUid, Blocks.air, 0, 0f, EnumSet.of(BlockMD.Flag.HasAir));

        GameRegistry.UniqueIdentifier voidUid = new GameRegistry.UniqueIdentifier("journeymap:void");
        VOIDBLOCK = new BlockMD("Void", voidUid, null, 0, 1f, null);

        blockFlags = new HashMap<Block, EnumSet<BlockMD.Flag>>(64);
        blockAlphas = new HashMap<Block, Float>(8);
    }

    /**
     * Constructor
     */
    public void initialize()
    {
        StatTimer timer = StatTimer.get("BlockMDCache.ensureCurrent").start();

        blockAlphas.clear();
        blockFlags.clear();

        // Set alphas
        setAlpha(Blocks.air, 0F);
//        setAlpha(Blocks.fence, .4F);
//        setAlpha(Blocks.fence_gate, .4F);
        setAlpha(Blocks.flowing_water, .3F);
        setAlpha(Blocks.glass, .3F);
        setAlpha(Blocks.glass_pane, .3F);
        setAlpha(Blocks.ice, .8F);
        setAlpha(Blocks.iron_bars, .4F);
        setAlpha(Blocks.nether_brick_fence, .4F);
        setAlpha(Blocks.stained_glass, .5F);
        setAlpha(Blocks.stained_glass_pane, .5F);
        setAlpha(Blocks.torch, .5F);
        setAlpha(Blocks.vine, .2F);
        setAlpha(Blocks.water, .3F);

        // Set optional flags
        if (JourneymapClient.getCoreProperties().caveIgnoreGlass.get())
        {
            setFlags(Blocks.glass, OpenToSky);
            setFlags(Blocks.glass_pane, OpenToSky);
            setFlags(Blocks.stained_glass, OpenToSky);
            setFlags(Blocks.stained_glass, OpenToSky);
        }

        // Set manual flags
        setFlags(Blocks.air, HasAir, OpenToSky, NoShadow, OpenToSky);
        setFlags(Blocks.double_plant, BiomeColor);
//        setFlags(Blocks.fence, TransparentRoof);
        setFlags(Blocks.fire, NoShadow, Side2Texture);
        setFlags(Blocks.flowing_water, BiomeColor);
        setFlags(Blocks.flowing_lava, NoShadow);
        setFlags(Blocks.glass, TransparentRoof);
        setFlags(Blocks.glass_pane, TransparentRoof);
        setFlags(Blocks.grass, BiomeColor);
        setFlags(Blocks.iron_bars, TransparentRoof);
        setFlags(Blocks.ladder, OpenToSky);
        setFlags(Blocks.lava, NoShadow);
        setFlags(Blocks.leaves, OpenToSky, BiomeColor, NoTopo);
        setFlags(Blocks.leaves2, OpenToSky, BiomeColor, NoTopo);
        setFlags(Blocks.log, OpenToSky, BiomeColor, NoTopo);
        setFlags(Blocks.log2, OpenToSky, BiomeColor, NoTopo);
        setFlags(Blocks.redstone_torch, HasAir);
        setFlags(Blocks.snow_layer, NoTopo);
        setFlags(Blocks.stained_glass, TransparentRoof, Transparency);
        setFlags(Blocks.stained_glass_pane, TransparentRoof, Transparency);
        setFlags(Blocks.tallgrass, BiomeColor);
        setFlags(Blocks.torch, HasAir, NoShadow);
        setFlags(Blocks.tripwire, NoShadow);
        setFlags(Blocks.tripwire_hook, NoShadow);
        setFlags(Blocks.unlit_redstone_torch, HasAir, NoShadow);
        setFlags(Blocks.vine, OpenToSky, NoShadow, BiomeColor);
        setFlags(Blocks.water, NoShadow, BiomeColor);
        setFlags(Blocks.web, OpenToSky, Side2Texture);

        // Mod block ids which get specific flags
        HashMap<GameRegistry.UniqueIdentifier, Collection<BlockMD.Flag>> modBlockUIDs = new HashMap<GameRegistry.UniqueIdentifier, Collection<BlockMD.Flag>>();
        for (GameRegistry.UniqueIdentifier specialUid : ModBlockDelegate.Blocks.keySet())
        {
            modBlockUIDs.put(specialUid, EnumSet.of(BlockMD.Flag.SpecialHandling));
        }

        // Torches from mods shouldn't cast block-sized shadows
        List<String> torches = new ArrayList<String>(Arrays.asList("TConstruct:decoration.stonetorch",
                "CarpentersBlocks:blockCarpentersTorch",
                "ExtraUtilities:magnumTorch",
                "appliedenergistics2:tile.BlockQuartzTorch"));
        for (int i = 1; i <= 10; i++)
        {
            torches.add("chisel:torch" + i);
        }

        for (String torch : torches)
        {
            modBlockUIDs.put(new GameRegistry.UniqueIdentifier(torch), EnumSet.of(HasAir, NoShadow));
        }

        // TODO: Move into mod handler
        // TerraFirmaCraft blocks
        List<String> tfcBlocks = Arrays.asList("LooseRock", "Grass", "Grass2", "ClayGrass", "ClayGrass2", "DryGrass", "DryGrass2", "FreshWater",
                "FreshWaterStationary", "PeatGrass", "TallGrass", "SaltWater", "SaltWaterStationary", "SeaGrassStill");
        for(String tfcBlock : tfcBlocks)
        {
            modBlockUIDs.put(new GameRegistry.UniqueIdentifier("tfc2:" + tfcBlock), EnumSet.of(BiomeColor));
            modBlockUIDs.put(new GameRegistry.UniqueIdentifier("terrafirmacraft:" + tfcBlock), EnumSet.of(BiomeColor));
        }

        // More mod-specific overrides
        modBlockUIDs.put(new GameRegistry.UniqueIdentifier("Mariculture:kelp"), EnumSet.of(Side2Texture, Plant));
        modBlockUIDs.put(new GameRegistry.UniqueIdentifier("tfc2:LooseRock"), EnumSet.of(HasAir, NoShadow));
        modBlockUIDs.put(new GameRegistry.UniqueIdentifier("tfc2:SeaGrassStill"), EnumSet.of(Side2Texture));
        modBlockUIDs.put(new GameRegistry.UniqueIdentifier("terrafirmacraft:LooseRock"), EnumSet.of(HasAir, NoShadow));
        modBlockUIDs.put(new GameRegistry.UniqueIdentifier("terrafirmacraft:SeaGrassStill"), EnumSet.of(Side2Texture));
        modBlockUIDs.put(new GameRegistry.UniqueIdentifier("Thaumcraft:blockMagicalLeaves"), EnumSet.of(BiomeColor)); // Thaumcraft:blockMagicalLeaves Greatwood Leaves #2C6F0E
        modBlockUIDs.put(new GameRegistry.UniqueIdentifier("CarpentersBlocks:blockCarpentersLadder"), EnumSet.of(OpenToSky));

        // Set flags based on inheritance
        for (Block block : GameData.getBlockRegistry().typeSafeIterable())
        {
            if (block.getMaterial() == Material.air)
            {
                setFlags(block, HasAir, OpenToSky, NoShadow);
                continue;
            }

            // TODO: Check if this will work in 1.7.10
            // If not, then need to uncomment fence lines at the top of this method
            if (block instanceof BlockFence || block instanceof BlockFenceGate)
            {
                setAlpha(block, .4F);
                setFlags(block, TransparentRoof);
            }

            if (block instanceof BlockGrass)
            {
                setFlags(block, BiomeColor);
            }

            if (block instanceof BlockLeavesBase || block instanceof BlockVine || block instanceof BlockLilyPad)
            {
                setFlags(block, BiomeColor, NoTopo);
            }

            if (block instanceof BlockCrops)
            {
                setFlags(block, Side2Texture, Crop, NoTopo);
                if (!JourneymapClient.getCoreProperties().mapPlantShadows.get())
                {
                    setFlags(block, NoShadow);
                }
            }
            else if (block instanceof BlockBush || block instanceof BlockCactus || block instanceof BlockDeadBush)
            {
                setFlags(block, Side2Texture, Plant, NoTopo);
                if (!JourneymapClient.getCoreProperties().mapPlantShadows.get())
                {
                    setFlags(block, NoShadow);
                }
            }
            else if (block instanceof BlockRailBase)
            {
                setFlags(block, NoShadow, NoTopo);
            }

            GameRegistry.UniqueIdentifier uid = findUniqueIdentifierFor(block);
            if (modBlockUIDs.containsKey(uid))
            {
                setFlags(block, modBlockUIDs.get(uid));
            }
        }

        timer.stop();
    }

    @Override
    public HashMap<Integer, BlockMD> load(Block key) throws Exception
    {
        return new HashMap<Integer, BlockMD>(16);
    }


    public GameRegistry.UniqueIdentifier findUniqueIdentifierFor(Block block)
    {
//        GameRegistry.UniqueIdentifier uid = blockUids.get(block);
//        if (uid == null)
//        {
//            uid = new GameRegistry.UniqueIdentifier(GameData.getBlockRegistry().getNameForObject(block));
//            blockUids.put(block, uid);
//        }
//        return uid;
        return new GameRegistry.UniqueIdentifier(GameData.getBlockRegistry().getNameForObject(block));
    }

    /**
     * Produces a BlockMD instance from chunk-local coords.
     */
    public BlockMD getBlockMD(LoadingCache<Block, HashMap<Integer, BlockMD>> cache, ChunkMD chunkMd, int x, int y, int z)
    {
        try
        {
            if (y >= 0)
            {
                Block block = chunkMd.getBlock(x, y, z);

                if (block instanceof BlockAir)
                {
                    return AIRBLOCK;
                }
                else
                {
                    int meta = chunkMd.getBlockMeta(x, y, z);
                    return getBlockMD(cache, chunkMd, block, meta, x, y, z);
                }
            }
            else
            {
                return VOIDBLOCK;
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't get blockId/meta for chunk %s,%s block %s,%s,%s : %s", chunkMd.getChunk().xPosition, chunkMd.getChunk().zPosition, x, y, z, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    /**
     * Produces a BlockMD instance from chunk-local coords.
     * Handles blocks with SpecialBlockHandler as needed.
     */
    public BlockMD getBlockMD(LoadingCache<Block, HashMap<Integer, BlockMD>> cache, ChunkMD chunkMD, Block block, int meta, int x, int y, int z)
    {
        BlockMD blockMD = getBlockMD(cache, block, meta);
        if (blockMD.hasFlag(SpecialHandling))
        {
            blockMD = modBlockDelegate.handleBlock(chunkMD, blockMD, x, y, z);
        }
        return blockMD;
    }

    /**
     * Produces a BlockMD instance from Block and Meta
     */
    public BlockMD getBlockMD(LoadingCache<Block, HashMap<Integer, BlockMD>> cache, Block block, int meta)
    {
        try
        {
            if (block instanceof BlockAir)
            {
                return AIRBLOCK;
            }
            else
            {
                HashMap<Integer, BlockMD> map = cache.get(block);
                BlockMD blockMD = map.get(meta);

                synchronized (this)
                {
                    if (blockMD == null)
                    {
                        String displayName = BlockMD.getBlockName(block, meta);
                        blockMD = new BlockMD(displayName, block, meta, getAlpha(block), getFlags(block));
                        map.put(meta, blockMD);
                    }
                }

                return blockMD;
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't get blockId/meta for block %s meta %s : %s", block, meta, LogFormatter.toString(e)));
            return AIRBLOCK;
        }
    }

    public EnumSet<BlockMD.Flag> getFlags(Block block)
    {
        EnumSet<BlockMD.Flag> flags = blockFlags.get(block);
        return flags == null ? EnumSet.noneOf(BlockMD.Flag.class) : flags;
    }

    public void setFlags(Block block, BlockMD.Flag... flags)
    {
        setFlags(block, Arrays.asList(flags));
    }

    public void setFlags(Block block, Collection<BlockMD.Flag> flags)
    {
        EnumSet<BlockMD.Flag> eset = getFlags(block);
        eset.addAll(flags);
        blockFlags.put(block, eset);
        Journeymap.getLogger().debug(block.getUnlocalizedName() + " flags set: " + eset);
    }

    public boolean hasFlag(Block block, BlockMD.Flag flag)
    {
        EnumSet<BlockMD.Flag> flags = blockFlags.get(block);
        return flags != null && flags.contains(flag);
    }

    public boolean hasAnyFlags(Block block, BlockMD.Flag... flags)
    {
        EnumSet<BlockMD.Flag> flagSet = blockFlags.get(block);
        if (flagSet == null)
        {
            return false;
        }
        for (BlockMD.Flag flag : flags)
        {
            if (flagSet.contains(flag))
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasAlpha(Block block)
    {
        return blockAlphas.containsKey(block);
    }

    public float getAlpha(Block block)
    {
        Float alpha = blockAlphas.get(block);
        return alpha == null ? 1F : alpha;
    }

    public void setAlpha(Block block, Float alpha)
    {
        blockAlphas.put(block, alpha);
    }

    public HashMap getFlagsMap()
    {
        return blockFlags;
    }

    public HashMap getAlphaMap()
    {
        return blockAlphas;
    }

    public ModBlockDelegate getModBlockDelegate()
    {
        return modBlockDelegate;
    }
}