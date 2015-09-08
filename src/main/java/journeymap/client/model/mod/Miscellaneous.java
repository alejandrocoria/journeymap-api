/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import cpw.mods.fml.common.registry.GameRegistry;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.BlockMD;
import journeymap.client.model.BlockMDCache;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static journeymap.client.model.BlockMD.Flag.*;

//import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Special handlers for miscellaneous mods that don't really need their own impl.
 */
public class Miscellaneous
{
    public static class CommonHandler implements ModBlockDelegate.IModBlockHandler
    {
        @Override
        public List<GameRegistry.UniqueIdentifier> initialize(BlockMDCache cache, List<GameRegistry.UniqueIdentifier> registeredBlockIds)
        {
            // Torches from mods shouldn't cast block-sized shadows
            List<GameRegistry.UniqueIdentifier> torches = new ArrayList<GameRegistry.UniqueIdentifier>();
            torches.add(new GameRegistry.UniqueIdentifier("TConstruct:decoration.stonetorch"));
            torches.add(new GameRegistry.UniqueIdentifier("ExtraUtilities:magnumTorch"));
            torches.add(new GameRegistry.UniqueIdentifier("appliedenergistics2:tile.BlockQuartzTorch"));
            for (int i = 1; i <= 10; i++)
            {
                torches.add(new GameRegistry.UniqueIdentifier("chisel:torch" + i));
            }

            // Hide torches
            for(GameRegistry.UniqueIdentifier registeredBlockId : registeredBlockIds)
            {
                if(torches.contains(registeredBlockId)) {
                    cache.setFlags(registeredBlockId, HasAir, NoShadow);
                }
            }

            // Mariculture Kelp
            GameRegistry.UniqueIdentifier maricultureKelpId = new GameRegistry.UniqueIdentifier("Mariculture:kelp");
            if(registeredBlockIds.contains(maricultureKelpId))
            {
                cache.setFlags(maricultureKelpId, Plant);
                cache.setTextureSide(maricultureKelpId, 2);
            }

            // Thaumcraft leaves (greatwood, silverwood)
            GameRegistry.UniqueIdentifier thaumcraftLeavesId = new GameRegistry.UniqueIdentifier("Thaumcraft:blockMagicalLeaves");
            if(registeredBlockIds.contains(thaumcraftLeavesId))
            {
                cache.setFlags(thaumcraftLeavesId, NoTopo, Foliage);
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

    /**
     * Create a waypoint for an OpenBlocks.
     * TODO: Buggy, don't use until fixed
     */
    public static class OpenBlocksGraveHandler implements ModBlockDelegate.IModBlockHandler
    {
        public static final GameRegistry.UniqueIdentifier UID = new GameRegistry.UniqueIdentifier("OpenBlocks:grave");
        private static final String TAG_PLAYERNAME = "PlayerName";
        private static final String TAG_PLAYERUUID = "PlayerUUID";

        @Override
        public List<GameRegistry.UniqueIdentifier> initialize(BlockMDCache cache, List<GameRegistry.UniqueIdentifier> registeredBlockIds)
        {
            if(registeredBlockIds.contains(UID))
            {
                cache.setFlags(UID, BlockMD.Flag.SpecialHandling);
                return Arrays.asList(UID);
            }
            return null;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            int blockX = chunkMD.toWorldX(localX);
            int blockZ = chunkMD.toWorldZ(localZ);
            //String name = I18n.format("tile.openblocks.grave.name");
            TileEntity tileEntity = ForgeHelper.INSTANCE.getTileEntity(chunkMD.getWorld(), blockX, y, blockZ);

            if (tileEntity != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tileEntity.writeToNBT(tag);

                String playerName = null;
                if (tag.hasNoTags())
                {
                    playerName = "?";
                }
                else
                {
                    playerName = tag.getString(TAG_PLAYERNAME);
                }

                if (playerName == null)
                {
                    playerName = "";
                }

                Waypoint waypoint = new Waypoint(playerName + " " + blockMD.getName(), blockX, y, blockZ, Color.red, Waypoint.Type.Death, chunkMD.getDimension());
                WaypointStore.instance().add(waypoint);
            }

            return blockMD;
        }
    }
}
