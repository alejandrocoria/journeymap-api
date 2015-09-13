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
import journeymap.client.data.DataCache;
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
                    blockMDCache.setFlags(block, HasAir, NoTopo);
                }
                else if(block instanceof BlockDoublePlant)
                {
                    blockMDCache.setFlags(block, Plant, NoTopo);
                    blockMDCache.setTextureSide(block, 2);
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
                else if (block instanceof BlockFlower)
                {
                    blockMDCache.setFlags(block, Plant, NoTopo);
                }
                else if (block instanceof BlockHugeMushroom)
                {
                    // 1.8 : 14 gets "all_outside" texture

                    int overrideMeta = block.getMetaFromState(block.getDefaultState());
                    blockMDCache.setOverrideMeta(block, overrideMeta);
                }
                else if (block instanceof BlockBush || block instanceof BlockCactus)
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

            // Flower colors look bad because the stem color is averaged in, overriding them is easier.
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.POPPY.getMeta()).setBaseColor(0x980406);
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta()).setBaseColor(0x1E7EB6);
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.ALLIUM.getMeta()).setBaseColor(0x8549B6);
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta()).setBaseColor(0x9DA1A7);
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.RED_TULIP.getMeta()).setBaseColor(0x980406);
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta()).setBaseColor(0xA3581A);
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta()).setBaseColor(0xB0B0B0);
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta()).setBaseColor(0xB09AB0);
            DataCache.instance().getBlockMD(Blocks.red_flower, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta()).setBaseColor(0xB3B3B3);
            DataCache.instance().getBlockMD(Blocks.yellow_flower, BlockFlower.EnumFlowerType.DANDELION.getMeta()).setBaseColor(0xAFB401);

            // Double-tall grass should be treated like BlockTallGrass:  ignored
            DataCache.instance().getBlockMD(Blocks.double_plant, 2).addFlags(HasAir, NoTopo);

            // Ferns unlike other BlockTallGrass will be treated like plants
            DataCache.instance().getBlockMD(Blocks.tallgrass, 2).addFlags(Plant, CustomBiomeColor);



            return null;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            return blockMD;
        }
    }
}
