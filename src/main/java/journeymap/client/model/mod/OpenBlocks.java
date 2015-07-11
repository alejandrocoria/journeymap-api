/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraftforge.fml.common.registry.GameRegistry;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Mark on 3/29/2015.
 */
public class OpenBlocks
{
    public static class GraveHandler implements ModBlockDelegate.IModBlockHandler
    {
        public static final GameRegistry.UniqueIdentifier UID = new GameRegistry.UniqueIdentifier("OpenBlocks:grave");
        private static final String TAG_PLAYERNAME = "PlayerName";
        private static final String TAG_PLAYERUUID = "PlayerUUID";

        @Override
        public Collection<GameRegistry.UniqueIdentifier> getBlockUids()
        {
            return Arrays.asList(UID);
        }

        @Override
        public IIcon getIcon(BlockMD blockMD)
        {
            return null;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            int blockX = chunkMD.toBlockX(localX);
            int blockZ = chunkMD.toBlockZ(localZ);
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

                Waypoint waypoint = new Waypoint(playerName + " " + blockMD.getName(), new ChunkCoordinates(blockX, y, blockZ), Color.red, Waypoint.Type.Death, chunkMd.getDimension());
                WaypointStore.instance().add(waypoint);
            }

            return blockMD;
        }
    }
}
