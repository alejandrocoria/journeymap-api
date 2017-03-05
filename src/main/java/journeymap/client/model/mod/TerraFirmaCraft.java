/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.mod.vanilla.VanillaColorHandler;
import net.minecraft.util.math.BlockPos;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Special handling required for TFC to set biome-related flags
 */
public class TerraFirmaCraft
{
    private static final String MODID = "terrafirmacraft";
    private static final String MODID2 = "tfc2";
    private static final int WATER_COLOR = 0x0b1940;

    /**
     * The type Tfc block handler.
     */
    public static class TfcBlockHandler implements ModBlockDelegate.IModBlockHandler
    {
        private final TfcWaterColorHandler waterColorHandler = new TfcWaterColorHandler();

        @Override
        public boolean initialize(BlockMD blockMD)
        {
            if (blockMD.getUid().startsWith(MODID) || blockMD.getUid().startsWith(MODID2))
            {
                String name = blockMD.getUid().toLowerCase();
                if (name.contains("looserock") || name.contains("loose_rock") || name.contains("rubble") || name.contains("vegetation"))
                {
                    blockMD.addFlags(HasAir, NoShadow, NoTopo);
                }
                else if (name.contains("seagrass"))
                {
                    //blockMD.setTextureSide(2);
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
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
        {
            return blockMD;
        }

    }

    /**
     * The type Tfc water color handler.
     */
    public static class TfcWaterColorHandler extends VanillaColorHandler
    {
        @Override
        protected Integer loadTextureColor(BlockMD blockMD, BlockPos blockPos)
        {
            return WATER_COLOR;
        }
    }
}
