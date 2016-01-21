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
import net.minecraftforge.fml.common.registry.GameRegistry;

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
        GameRegistry.UniqueIdentifier maricultureKelpId = new GameRegistry.UniqueIdentifier("Mariculture:kelp");

        // Thaumcraft leaves (greatwood, silverwood)
        GameRegistry.UniqueIdentifier thaumcraftLeavesId = new GameRegistry.UniqueIdentifier("Thaumcraft:blockMagicalLeaves");

        List<GameRegistry.UniqueIdentifier> torches = new ArrayList<GameRegistry.UniqueIdentifier>();

        public CommonHandler()
        {
            torches.add(new GameRegistry.UniqueIdentifier("TConstruct:decoration.stonetorch"));
            torches.add(new GameRegistry.UniqueIdentifier("ExtraUtilities:magnumTorch"));
            torches.add(new GameRegistry.UniqueIdentifier("appliedenergistics2:tile.BlockQuartzTorch"));
            for (int i = 1; i <= 10; i++)
            {
                torches.add(new GameRegistry.UniqueIdentifier("chisel:torch" + i));
            }
        }

        @Override
        public boolean initialize(BlockMD blockMD)
        {
            GameRegistry.UniqueIdentifier uid = blockMD.getUid();
            if (torches.contains(uid))
            {
                blockMD.addFlags(HasAir, NoShadow);
            }
            else if (uid.equals(maricultureKelpId))
            {
                blockMD.addFlags(Plant);
                blockMD.setTextureSide(2);
            }
            else if (uid.equals(thaumcraftLeavesId))
            {
                blockMD.addFlags(NoTopo, Foliage);
            }
            return false;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            // Should never be called
            return blockMD;
        }
    }
}
