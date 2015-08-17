/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.BlockMDCache;
import journeymap.client.model.ChunkMD;
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

    public static class CommonHandler implements ModBlockDelegate.IModBlockHandler
    {
        @Override
        public List<GameRegistry.UniqueIdentifier> initialize(BlockMDCache cache, List<GameRegistry.UniqueIdentifier> registeredBlockIds)
        {
            for (GameRegistry.UniqueIdentifier uid : registeredBlockIds)
            {
                if(uid.modId.equals(MODID) || uid.modId.equals(MODID2))
                {
                    String name = uid.name.toLowerCase();
                    if(name.equals("looserock") || name.equals("loose_rock"))
                    {
                        cache.setFlags(uid, HasAir, NoShadow, NoTopo);
                    }
                    else if(name.contains("seagrass"))
                    {
                        cache.setFlags(uid, Side2Texture, Plant);
                    }
                    else if(name.contains("grass") || name.contains("dirt"))
                    {
                        cache.setFlags(uid, Grass);
                    }
                    else if(name.contains("water"))
                    {
                        cache.setFlags(uid, Water);
                        cache.setAlpha(uid, .3f);
                    }
                    else if(name.contains("leaves"))
                    {
                        cache.setFlags(uid, NoTopo, Foliage);
                    }
                    else if(name.contains("rubble"))
                    {
                        cache.setAlpha(uid, 1f);
                    }
                }
            }

            return null;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            // Should never be called
            return blockMD;
        }
    }
}
