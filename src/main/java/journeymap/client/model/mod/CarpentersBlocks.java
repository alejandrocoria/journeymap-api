/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model.mod;

import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Delegate getting the block used to texture a CarpentersBlock
 */
public class CarpentersBlocks
{
    public static class CommonHandler implements ModBlockDelegate.IModBlockHandler
    {
        private static final String MODPREFIX = "CarpentersBlocks:";

        private static final List<String> BLOCKNAMES = Arrays.asList(
                "blockCarpentersBlock", "blockCarpentersBarrier",
                "blockCarpentersBed", "blockCarpentersCollapsibleBlock", "blockCarpentersDaylightSensor", "blockCarpentersDoor",
                "blockCarpentersFlowerPot", "blockCarpentersGarageDoor", "blockCarpentersGate", "blockCarpentersHatch",
                "blockCarpentersPressurePlate", "blockCarpentersSafe", "blockCarpentersSlope", "blockCarpentersStairs");

        private static final Collection<GameRegistry.UniqueIdentifier> UIDS = initUIDs();
        private static final String TAG_ATTR_LIST = "cbAttrList";
        private static final String TAG_ID = "id";
        private static final String TAG_DAMAGE = "Damage";

        private static Collection<GameRegistry.UniqueIdentifier> initUIDs()
        {
            List<GameRegistry.UniqueIdentifier> list = new ArrayList<GameRegistry.UniqueIdentifier>(BLOCKNAMES.size());
            for (String name : BLOCKNAMES)
            {
                list.add(new GameRegistry.UniqueIdentifier(MODPREFIX + name));
            }
            return list;
        }

        @Override
        public Collection<GameRegistry.UniqueIdentifier> getBlockUids()
        {
            return UIDS;
        }

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

//        @Override
//        public IIcon getIcon(BlockMD blockMD)
//        {
//            // com.carpentersblocks.util.BlockProperties.MASK_DEFAULT_ICON = 16;
//            return blockMD.getBlock().getIcon(1, 16);
//        }
    }
}
