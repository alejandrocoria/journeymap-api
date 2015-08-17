/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import journeymap.client.JourneymapClient;
import journeymap.client.model.BlockMD;
import journeymap.client.model.BlockMDCache;
import journeymap.client.model.ChunkMD;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Sets flags for vanilla Minecraft blocks and blocks inherited from them.
 */
public class Vanilla
{
    public static class CommonHandler implements ModBlockDelegate.IModBlockHandler
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
            blockMDCache.setFlags(Blocks.fire, NoShadow, Side2Texture);
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
            blockMDCache.setFlags(Blocks.web, OpenToSky, Side2Texture);

            // Set flags based on inheritance
            for (Block block : GameData.getBlockRegistry().typeSafeIterable())
            {
                if (block.getMaterial() == Material.air)
                {
                    blockMDCache.setFlags(block, HasAir, OpenToSky, NoShadow);
                    continue;
                }

                if(block instanceof BlockLog)
                {
                    blockMDCache.setFlags(block, OpenToSky, CustomBiomeColor, NoTopo);
                }

                // TODO: Check if this will work in 1.7.10
                // If not, then need to uncomment fence lines at the top of this method
                if (block instanceof BlockFence || block instanceof BlockFenceGate)
                {
                    blockMDCache.setAlpha(block, .4F);
                    blockMDCache.setFlags(block, TransparentRoof);
                }

                if (block instanceof BlockGrass || block instanceof BlockTallGrass || block instanceof BlockDoublePlant)
                {
                    blockMDCache.setFlags(block, Grass);
                }

                if (block instanceof BlockLeavesBase)
                {
                    blockMDCache.setFlags(block, OpenToSky, Foliage, NoTopo);
                }

                if (block instanceof BlockVine)
                {
                    blockMDCache.setAlpha(block, .2F);
                    blockMDCache.setFlags(block, OpenToSky, Foliage, NoTopo, NoShadow);
                }

                // TODO: use foliage?
                if (block instanceof BlockLilyPad)
                {
                    blockMDCache.setFlags(block, CustomBiomeColor, NoTopo);
                }

                if (block instanceof BlockCrops)
                {
                    blockMDCache.setFlags(block, Side2Texture, Crop, NoTopo);
                    if (!JourneymapClient.getCoreProperties().mapPlantShadows.get())
                    {
                        blockMDCache.setFlags(block, NoShadow);
                    }
                }
                else if (block instanceof BlockBush || block instanceof BlockCactus || block instanceof BlockDeadBush)
                {
                    blockMDCache.setFlags(block, Side2Texture, Plant, NoTopo);
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

            return null;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            // No blocks need special handling
            return blockMD;
        }
    }
}
