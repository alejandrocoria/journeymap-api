/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Special handlers for miscellaneous mods that don't really need their own impl.
 */
public class Miscellaneous
{
    /**
     * The type Common handler.
     */
    public static class CommonHandler implements ModBlockDelegate.IModBlockHandler
    {
        /**
         * The Mariculture kelp id.
         */
// Mariculture Kelp
        String maricultureKelpId = "Mariculture:kelp";

        /**
         * The Thaumcraft leaves id.
         */
// Thaumcraft leaves (greatwood, silverwood)
        String thaumcraftLeavesId = "Thaumcraft:blockMagicalLeaves";

        /**
         * The Torches.
         */
        List<String> torches = new ArrayList<String>();

        /**
         * Instantiates a new Common handler.
         */
        public CommonHandler()
        {
            torches.add("TConstruct:decoration.stonetorch");
            torches.add("ExtraUtilities:magnumTorch");
            torches.add("appliedenergistics2:tile.BlockQuartzTorch");
            for (int i = 1; i <= 10; i++)
            {
                torches.add("chisel:torch" + i);
            }
        }

        @Override
        public boolean initialize(BlockMD blockMD)
        {
            String uid = blockMD.getUid();
            if (torches.contains(uid))
            {
                blockMD.addFlags(HasAir, NoShadow);
            }
            else if (uid.equals(maricultureKelpId))
            {
                blockMD.addFlags(Plant);
                //blockMD.setTextureSide(2);
            }
            else if (uid.equals(thaumcraftLeavesId))
            {
                blockMD.addFlags(NoTopo, Foliage);
            }
            return false;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
        {
            // Should never be called
            return blockMD;
        }
    }
}
