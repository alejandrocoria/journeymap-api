/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model.mod;


import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.world.JmBlockAccess;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.GameData;

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
        public boolean initialize(BlockMD blockMD)
        {
            String uid = blockMD.getUid();
            if (uid.startsWith(MODID))
            {
                if (uid.contains("blockCarpentersTorch"))
                {
                    blockMD.addFlags(HasAir, NoShadow);
                }
                else if (uid.contains("blockCarpentersLadder"))
                {
                    blockMD.addFlags(OpenToSky);
                    blockMD.setModBlockHandler(this);
                    return true;
                }
                else
                {
                    blockMD.setModBlockHandler(this);
                    return true;
                }
            }

            return false;
        }

        /**
         * Get the block flagged with used to color the carpenter's block.
         */
        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
        {
            final TileEntity tileEntity = JmBlockAccess.INSTANCE.getTileEntity(blockPos);
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
                        blockMD = BlockMD.get(block.getStateFromMeta(meta));
                    }
                }
            }
            return blockMD;
        }
    }
}
