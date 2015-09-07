/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import journeymap.client.data.DataCache;
import journeymap.client.model.BlockMD;
import journeymap.client.model.BlockMDCache;
import journeymap.client.model.ChunkMD;
import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Special handling required for TFC to set biome-related flags
 */
public class TerraFirmaCraft
{
    private static final String MODID = "terrafirmacraft";
    private static final String MODID2 = "tfc2";

    public static class TfcBlockHandler implements ModBlockDelegate.IModBlockHandler
    {
        private final TfcWaterColorHandler waterColorHandler = new TfcWaterColorHandler();

        @Override
        public List<GameRegistry.UniqueIdentifier> initialize(BlockMDCache cache, List<GameRegistry.UniqueIdentifier> registeredBlockIds)
        {
            BlockMDCache blockMdCache = DataCache.instance().getBlockMetadata();
            for (GameRegistry.UniqueIdentifier uid : registeredBlockIds)
            {
                if(uid.modId.equals(MODID) || uid.modId.equals(MODID2))
                {
                    String name = uid.name.toLowerCase();
                    Block block = GameData.getBlockRegistry().getObject(uid.toString());
                    if (name.equals("looserock") || name.equals("loose_rock") || name.contains("rubble") || name.contains("vegetation"))
                    {
                        blockMdCache.preloadBlock(block, HasAir, NoShadow, NoTopo);
                    }
                    else if(name.contains("seagrass"))
                    {
                        cache.setTextureSide(uid, 2);
                        blockMdCache.preloadBlock(block, Plant);
                    }
                    else if (name.contains("grass"))
                    {
                        blockMdCache.preloadBlock(block, null, 1f, Grass);
                    }
                    else if (name.contains("dirt"))
                    {
                        blockMdCache.preloadBlock(block);
                    }
                    else if(name.contains("water"))
                    {
                        blockMdCache.preloadBlock(block, null, .3f, Water, NoShadow);
                        for (int meta : BlockMD.getMetaValuesForBlock(block))
                        {
                            BlockMD blockMD = DataCache.instance().getBlockMD(block, meta);
                            if (blockMD.getBaseColor() == null)
                            {
                                blockMD.setBaseColor(0x0b1940);
                            }
                            blockMD.setBlockColorHandler(waterColorHandler);
                        }
                    }
                    else if(name.contains("leaves"))
                    {
                        blockMdCache.preloadBlock(block, NoTopo, Foliage);
                    }
                }
            }

            return null;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            return blockMD;
        }

    }

    public static class TfcWaterColorHandler extends Vanilla.CommonColorHandler
    {
        public Integer getWaterColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
        {
            return blockMD.getBaseColor();
        }
    }
}
