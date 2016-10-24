/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Special handling required for BoP plants, etc.
 */
public class BiomesOPlenty
{
    private static final String MODID = "biomesoplenty";

    public static class BopBlockHandler implements ModBlockDelegate.IModBlockHandler
    {
        private List<String> plants = Arrays.asList("flower", "mushroom", "sapling");
        private List<String> crops = Collections.singletonList("turnip");
        private List<String> biomeColoredPlants = Arrays.asList("plant", "ivy", "waterlily", "moss");

        @Override
        public boolean initialize(BlockMD blockMD)
        {
            String uid = blockMD.getUid();
            if (uid.startsWith(MODID))
            {
                String name = blockMD.getUid().toLowerCase();
                for (String plant : plants)
                {
                    if (name.contains(plant))
                    {
                        //blockMD.setTextureSide(2);
                        blockMD.addFlags(Plant);
                        break;
                    }
                }

                for (String crop : crops)
                {
                    if (name.contains(crop))
                    {
                        //blockMD.setTextureSide(2);
                        blockMD.addFlags(Crop);
                        break;
                    }
                }

                for (String biomeColoredPlant : biomeColoredPlants)
                {
                    if (name.contains(biomeColoredPlant))
                    {
                        //blockMD.setTextureSide(2);
                        blockMD.addFlags(Plant, CustomBiomeColor);
                        break;
                    }
                }

                if (name.contains("grass"))
                {
                    blockMD.addFlags(Grass, CustomBiomeColor);
                }
            }

            return false;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
        {
            return blockMD;
        }

    }
}
