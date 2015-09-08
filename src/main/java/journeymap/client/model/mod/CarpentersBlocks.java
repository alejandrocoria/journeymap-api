/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.BlockMD;
import journeymap.client.model.BlockMDCache;
import journeymap.client.model.ChunkMD;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
//import net.minecraftforge.fml.common.registry.GameData;
//import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

import static journeymap.client.model.BlockMD.Flag.*;

/**
 * Special handling for CarpentersBlocks to get the proper color of the wrapped block, etc.
 */
public class CarpentersBlocks
{
    public static class CommonHandler implements ModBlockDelegate.IModBlockHandler
    {
        private static final String MODID = "CarpentersBlocks";
        private static final String TAG_ATTR_LIST = "cbAttrList";
        private static final String TAG_ID = "id";
        private static final String TAG_DAMAGE = "Damage";

        /**
         * Sets special handling for all CarpentersBlocks except torches, which are
         * treated like vanilla torches and not mapped.
         */
        @Override
        public List<GameRegistry.UniqueIdentifier> initialize(BlockMDCache cache, List<GameRegistry.UniqueIdentifier> registeredBlockIds)
        {
            List<GameRegistry.UniqueIdentifier> specialHandlingUids = new ArrayList<GameRegistry.UniqueIdentifier>(16);
            for (GameRegistry.UniqueIdentifier uid : registeredBlockIds)
            {
                if(uid.modId.equals(MODID))
                {
                    if(uid.name.equals("blockCarpentersTorch"))
                    {
                        cache.setFlags(uid, HasAir, NoShadow);
                    }
                    else if(uid.name.equals("blockCarpentersLadder"))
                    {
                        cache.setFlags(uid, SpecialHandling, OpenToSky);
                        specialHandlingUids.add(uid);
                    }
                    else
                    {
                        cache.setFlags(uid, SpecialHandling);
                        specialHandlingUids.add(uid);
                    }
                }
            }
            return specialHandlingUids;
        }

        /**
         * Get the block flagged with used to color the carpenter's block.
         *
         * @param chunkMD   Containing chunk
         * @param blockMD   CarpentersBlock flagged with SpecialHandling
         * @param localX    x local to chunk
         * @param y         y
         * @param localZ    z local to chunk
         * @return          block used to provide color
         */
        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, int localX, int y, int localZ)
        {
            final int blockX = chunkMD.toWorldX(localX);
            final int blockZ = chunkMD.toWorldZ(localZ);
            final TileEntity tileEntity = ForgeHelper.INSTANCE.getTileEntity(chunkMD.getWorld(), blockX, y, blockZ);
            if (tileEntity != null)
            {
                final NBTTagCompound tag = new NBTTagCompound();
                tileEntity.writeToNBT(tag);

                if (!tag.hasNoTags())
                {
                    int id;
                    int meta = 0;
                    NBTTagList attrs = tag.getTagList(TAG_ATTR_LIST, 10);
                    String idString = attrs.getCompoundTagAt(0).getString(TAG_ID);

                    if (idString.length() > 0)
                    {
                        id = Integer.parseInt(idString.substring(0, idString.length() - 1));
                        String idMeta = attrs.getCompoundTagAt(0).getString(TAG_DAMAGE);
                        if (idMeta.length() > 0)
                        {
                            meta = Integer.parseInt(idMeta.substring(0, idMeta.length() - 1));
                        }
                        Block block = GameData.getBlockRegistry().getObjectById(id);
                        blockMD = DataCache.instance().getBlockMD(block, meta);
                    }
                }
            }
            return blockMD;
        }
    }
}
