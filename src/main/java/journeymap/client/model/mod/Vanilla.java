/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ColorCache;
import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IColorHelper;
import journeymap.client.forge.helper.IForgeHelper;
import journeymap.client.log.JMLogger;
import journeymap.client.model.BlockMD;
import journeymap.client.model.BlockMDCache;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Sets flags for vanilla Minecraft blocks and blocks inherited from them.
 */
public class Vanilla
{
    public static class CommonColorHandler implements ModBlockDelegate.IModBlockColorHandler
    {
        public static final CommonColorHandler INSTANCE = new CommonColorHandler();

        private final IForgeHelper forgeHelper = ForgeHelper.INSTANCE;
        private final IColorHelper colorHelper = forgeHelper.getColorHelper();

        /**
         * Get the block's tint based on the biome position it's in.
         */
        public Integer getCustomBiomeColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
        {
            Integer color = ColorCache.instance().getBaseColor(blockMD, x, y, z);
            int tint = getTint(blockMD, x, y, z);

            if (!RGB.isWhite(tint) && !RGB.isBlack(tint))
            {
                color = RGB.multiply(color, tint);
                if (!blockMD.hasFlag(BlockMD.Flag.CustomBiomeColor))
                {
                    blockMD.addFlags(BlockMD.Flag.CustomBiomeColor);
                    Journeymap.getLogger().info("Custom biome tint set for " + blockMD + " in " + biome.biomeName);
                }
            }
            else
            {
                Journeymap.getLogger().debug("Custom biome tint not found for " + blockMD + " in " + biome.biomeName);
            }
            return color;
        }

        /**
         * Get the foliage color for the block.
         */
        public Integer getFoliageColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
        {
            return RGB.adjustBrightness(RGB.multiply(ColorCache.instance().getBaseColor(blockMD, x, y, z), getTint(blockMD, x, y, z)), .8f);
        }

        /**
         * Get the grass color for the block.
         */
        public Integer getGrassColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
        {
            // Base color is just a grey that gets the tint close to the averaged texture color on screen. - tb
            return RGB.multiply(0x929292, getTint(blockMD, x, y, z));
        }

        /**
         * Get the water color for the block.
         */
        public Integer getWaterColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
        {
            return RGB.multiply(ColorCache.instance().getBaseColor(blockMD, x, y, z), getTint(blockMD, x, y, z));
        }

        /**
         * Get the tint (color multiplier) for the block.
         */
        public Integer getTint(BlockMD blockMD, int x, int y, int z)
        {
            try
            {
                return colorHelper.getColorMultiplier(forgeHelper.getWorld(), blockMD.getBlock(), x, y, z);
            }
            catch (Exception e)
            {
                // Bugfix for NPE thrown by uk.co.shadeddimensions.ep3.block.BlockFrame.func_71920_b
                JMLogger.logOnce("Block throws exception when calling colorMultiplier(): " + blockMD.getBlock().getUnlocalizedName(), e);
                return RGB.WHITE_ARGB;
            }
        }
    }

    public final static class CommonBlockHandler implements ModBlockDelegate.IModBlockHandler
    {
        @Override
        public List<GameRegistry.UniqueIdentifier> initialize(BlockMDCache blockMDCache, List<GameRegistry.UniqueIdentifier> registeredBlockIds)
        {
            // Set alphas
            blockMDCache.setAlpha(Blocks.air, 0F);
//        blockMDCache.setAlpha(Blocks.fence, .4F);
//        blockMDCache.setAlpha(Blocks.fence_gate, .4F);
            blockMDCache.setAlpha(Blocks.flowing_water, .3F);
            blockMDCache.setAlpha(Blocks.glass, .3F);
            blockMDCache.setAlpha(Blocks.glass_pane, .3F);
            blockMDCache.setAlpha(Blocks.ice, .8F);
            blockMDCache.setAlpha(Blocks.iron_bars, .4F);
            blockMDCache.setAlpha(Blocks.nether_brick_fence, .4F);
            blockMDCache.setAlpha(Blocks.stained_glass, .5F);
            blockMDCache.setAlpha(Blocks.stained_glass_pane, .5F);
            blockMDCache.setAlpha(Blocks.torch, .5F);
            blockMDCache.setAlpha(Blocks.water, .3F);

            // Set optional flags
            if (JourneymapClient.getCoreProperties().caveIgnoreGlass.get())
            {
                blockMDCache.setFlags(Blocks.glass, OpenToSky);
                blockMDCache.setFlags(Blocks.glass_pane, OpenToSky);
                blockMDCache.setFlags(Blocks.stained_glass, OpenToSky);
                blockMDCache.setFlags(Blocks.stained_glass, OpenToSky);
            }

            // Set manual flags
            blockMDCache.setFlags(Blocks.air, HasAir, OpenToSky, NoShadow, OpenToSky);
            // 1.7 only
            // blockMDCache.setFlags(Blocks.fence, TransparentRoof);
            blockMDCache.setFlags(Blocks.fire, NoShadow);
            blockMDCache.setTextureSide(Blocks.fire, 2);

            blockMDCache.setFlags(Blocks.flowing_water, Water);
            blockMDCache.setFlags(Blocks.flowing_lava, NoShadow);
            blockMDCache.setFlags(Blocks.glass, TransparentRoof);
            blockMDCache.setFlags(Blocks.glass_pane, TransparentRoof);
            blockMDCache.setFlags(Blocks.iron_bars, TransparentRoof);
            blockMDCache.setFlags(Blocks.ladder, OpenToSky);
            blockMDCache.setFlags(Blocks.lava, NoShadow);
            blockMDCache.setFlags(Blocks.redstone_torch, HasAir, NoShadow, NoTopo);
            blockMDCache.setFlags(Blocks.snow_layer, NoTopo);
            blockMDCache.setFlags(Blocks.stained_glass, TransparentRoof, Transparency);
            blockMDCache.setFlags(Blocks.stained_glass_pane, TransparentRoof, Transparency);
            blockMDCache.setFlags(Blocks.torch, HasAir, NoShadow, NoTopo);
            blockMDCache.setFlags(Blocks.tripwire, NoShadow);
            blockMDCache.setFlags(Blocks.tripwire_hook, NoShadow);
            blockMDCache.setFlags(Blocks.unlit_redstone_torch, HasAir, NoShadow);
            blockMDCache.setFlags(Blocks.water, NoShadow, Water);
            blockMDCache.setFlags(Blocks.web, OpenToSky);
            blockMDCache.setTextureSide(Blocks.web, 2);

            blockMDCache.VOIDBLOCK.setBaseColor(0x110C19);

            // Set flags based on inheritance
            for (Block block : GameData.getBlockRegistry().typeSafeIterable())
            {
                String name = blockMDCache.findUniqueIdentifierFor(block).name;

                if (block.getMaterial() == Material.air)
                {
                    blockMDCache.setFlags(block, HasAir, OpenToSky, NoShadow);
                    continue;
                }
                else if(block instanceof BlockLog)
                {
                    blockMDCache.setFlags(block, OpenToSky, CustomBiomeColor, NoTopo);
                }
                // TODO: Check if this will work in 1.7.10
                // If not, then need to uncomment fence lines at the top of this method
                else if (block instanceof BlockFence || block instanceof BlockFenceGate)
                {
                    blockMDCache.setAlpha(block, .4F);
                    blockMDCache.setFlags(block, TransparentRoof);
                }
                else if (block instanceof BlockGrass)
                {
                    blockMDCache.setFlags(block, Grass);
                }
                else if (block instanceof BlockTallGrass)
                {

                    if(name.contains("fern"))
                    {
                        blockMDCache.setFlags(block, Plant, CustomBiomeColor);
                        blockMDCache.setTextureSide(block, 2);
                    }
                    else
                    {
                        blockMDCache.setFlags(block, HasAir, NoTopo);
                    }
                }
                else if(block instanceof BlockDoublePlant)
                {
                    blockMDCache.setFlags(block, Plant);
                }
                else if (block instanceof BlockLeavesBase)
                {
                    blockMDCache.setFlags(block, OpenToSky, Foliage, NoTopo);
                }
                else if (block instanceof BlockVine)
                {
                    blockMDCache.setAlpha(block, .2F);
                    blockMDCache.setFlags(block, OpenToSky, Foliage, NoTopo, NoShadow);
                }
                // TODO: use foliage?
                else if (block instanceof BlockLilyPad)
                {
                    blockMDCache.setFlags(block, CustomBiomeColor, NoTopo);
                }
                else if (block instanceof BlockCrops)
                {
                    blockMDCache.setFlags(block, Crop, NoTopo);
                    blockMDCache.setTextureSide(block, 2);
                    if (!JourneymapClient.getCoreProperties().mapPlantShadows.get())
                    {
                        blockMDCache.setFlags(block, NoShadow);
                    }
                }
                else if (block instanceof BlockBush || block instanceof BlockCactus || block instanceof BlockDeadBush)
                {
                    blockMDCache.setFlags(block, Plant, NoTopo);
                    blockMDCache.setTextureSide(block, 2);
                    if (!JourneymapClient.getCoreProperties().mapPlantShadows.get())
                    {
                        blockMDCache.setFlags(block, NoShadow);
                    }
                }
                else if (block instanceof BlockRailBase)
                {
                    blockMDCache.setFlags(block, NoShadow, NoTopo);
                }
            }

            // Giant mushrooms get special handling
            // 1.7 ??
            // 1.8
            blockMDCache.setTextureSide(Blocks.brown_mushroom_block, BlockHugeMushroom.EnumType.ALL_OUTSIDE.getMetadata());
            blockMDCache.setTextureSide(Blocks.red_mushroom_block, BlockHugeMushroom.EnumType.ALL_OUTSIDE.getMetadata());
            return null;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            return blockMD;
        }
    }
}
