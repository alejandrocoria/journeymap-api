package net.techbrew.journeymap.model.mod;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

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
            final int blockX = chunkMD.toBlockX(localX);
            final int blockZ = chunkMD.toBlockZ(localZ);
            final TileEntity tileEntity = chunkMD.getWorldObj().getTileEntity(blockX, y, blockZ);
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
