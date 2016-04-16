/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.mod.vanilla.VanillaColorHandler;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Special handling required for TFC to set biome-related flags
 */
public class TerraFirmaCraft
{
    private static final String MODID = "terrafirmacraft";
    private static final String MODID2 = "tfc2";
    private static final int WATER_COLOR = 0x0b1940;

    public static class TfcBlockHandler implements ModBlockDelegate.IModBlockHandler
    {
        private final TfcWaterColorHandler waterColorHandler = new TfcWaterColorHandler();

        @Override
        public boolean initialize(BlockMD blockMD)
        {
            if (blockMD.getUid().startsWith(MODID) || blockMD.getUid().startsWith(MODID2))
            {
                String name = blockMD.getUid().toLowerCase();
                if (name.equals("looserock") || name.equals("loose_rock") || name.contains("rubble") || name.contains("vegetation"))
                {
                    blockMD.addFlags(HasAir, NoShadow, NoTopo);
                }
                else if (name.contains("seagrass"))
                {
                    blockMD.setTextureSide(2);
                    blockMD.addFlags(Plant);
                    //preloadColor(blockMD);
                }
                else if (name.contains("grass"))
                {
                    blockMD.addFlags(Grass);
                    //preloadColor(blockMD);
                }
                else if (name.contains("dirt"))
                {
                    //preloadColor(blockMD);
                }
                else if (name.contains("water"))
                {
                    blockMD.setAlpha(.3f);
                    blockMD.addFlags(Water, NoShadow);
                    blockMD.setBlockColorHandler(waterColorHandler);
                    if (blockMD.getColor() == null)
                    {
                        blockMD.setColor(WATER_COLOR);
                    }
                }
                else if (name.contains("leaves"))
                {
                    blockMD.addFlags(NoTopo, Foliage);
                }
            }

            return false;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            return blockMD;
        }

    }

    public static class TfcWaterColorHandler extends VanillaColorHandler
    {
        @Override
        protected Integer loadTextureColor(BlockMD blockMD, int globalX, int y, int globalZ)
        {
            return WATER_COLOR;
        }
    }
}
