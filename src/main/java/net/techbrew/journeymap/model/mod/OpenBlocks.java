package net.techbrew.journeymap.model.mod;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.waypoint.WaypointStore;

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
            TileEntity tileEntity = chunkMD.getWorldObj().getTileEntity(blockX, y, blockZ);

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

                Waypoint waypoint = new Waypoint(playerName + " " + blockMD.getName(), new ChunkCoordinates(blockX, y, blockZ), Color.red, Waypoint.Type.Death, chunkMD.getWorldObj().provider.dimensionId);
                WaypointStore.instance().add(waypoint);
            }

            return blockMD;
        }
    }
}
