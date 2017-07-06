/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.util.math.BlockPos;

/**
 * Special handling required for Mekanism blocks, etc.
 */
public class Chisel
{
    private static final String MODID = "chisel";

    /**
     * Chisel block handler.
     */
    public static class ChiselBlockHandler implements ModBlockDelegate.IModBlockHandler
    {
        @Override
        public boolean initialize(BlockMD blockMD)
        {
            String uid = blockMD.getUid();
            if (uid.startsWith(MODID + ":"))
            {
                if (uid.contains("testblock"))
                {
                    blockMD.addFlags(BlockMD.Flag.HasAir);
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
