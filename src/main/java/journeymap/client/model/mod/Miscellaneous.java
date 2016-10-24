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

import java.util.ArrayList;
import java.util.List;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Special handlers for miscellaneous mods that don't really need their own impl.
 */
public class Miscellaneous
{
    public static class CommonHandler implements ModBlockDelegate.IModBlockHandler
    {
        // Mariculture Kelp
        String maricultureKelpId = "Mariculture:kelp";

        // Thaumcraft leaves (greatwood, silverwood)
        String thaumcraftLeavesId = "Thaumcraft:blockMagicalLeaves";

        List<String> torches = new ArrayList<String>();

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
