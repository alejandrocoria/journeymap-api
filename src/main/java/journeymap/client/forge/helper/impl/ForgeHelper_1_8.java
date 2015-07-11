/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper.impl;

import com.google.common.base.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.GameData;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IForgeHelper;
import journeymap.client.model.BlockMD;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Random;

/**
 * Implementation to encapsulate uses of methods/fields that have changed in 1.8
 */
public class ForgeHelper_1_8 implements IForgeHelper
{
    @Override
    public Minecraft getClient()
    {
        return ForgeHelper.INSTANCE.getClient();
    }

    @Override
    public EnumSkyBlock getSkyBlock()
    {
        // 1.7
        // return EnumSkyBlock.Block;

        // 1.8
        return EnumSkyBlock.BLOCK;
    }

    @Override
    public FontRenderer getFontRenderer()
    {
        // 1.7
        // return getClient().fontRenderer;

        // 1.8
        return getClient().fontRendererObj;
    }

    @Override
    public int getPlayerDimension()
    {
        // 1.7
        //return getClient().thePlayer.worldObj.provider.dimension;

        // 1.8
        return getClient().thePlayer.worldObj.provider.getDimensionId();
    }

    @Override
    public boolean hasNoSky(World world)
    {
        // 1.7
        //return world.provider.hasNoSky;

        // 1.8
        return world.provider.getHasNoSky();
    }

    @Override
    public World getWorld(Chunk chunk)
    {
        // 1.7
        // return getChunk().worldObj;

        // 1.8
        return chunk.getWorld();
    }

    @Override
    public int getLightOpacity(World world, BlockMD blockMD, int blockX, int blockY, int blockZ)
    {
        // 1.7
        // return blockMD.getBlock().getLightOpacity(world, blockX, blockY, blockZ);

        // 1.8
        return blockMD.getBlock().getLightOpacity(world, new BlockPos(blockX, blockY, blockZ));
    }

    @Override
    public int getDimension(World world)
    {
        // 1.7
        // return world.provider.dimension;

        // 1.8
        return world.provider.getDimensionId();
    }

    @Override
    public int getDimension(WorldProvider worldProvider)
    {
        // 1.7
        // return worldProvider.dimensionId;

        return worldProvider.getDimensionId();
    }

    @Override
    public int getSavedLightValue(Chunk chunk, int x, int y, int z)
    {
        // 1.7
        // return chunk.getSavedLightValue(getSkyBlock(), x, y, z);

        // 1.8
        return chunk.getLightFor(getSkyBlock(), new BlockPos(x, y, z));
    }

    @Override
    public RenderManager getRenderManager()
    {
        // 1.7
        // return RenderManager.instance;

        // 1.8
        return getClient().getRenderManager();
    }

    /**
     * Gets the entity's name (player name) / command sender name.
     *
     * @param entity
     * @return
     */
    @Override
    public String getEntityName(Entity entity)
    {
        // 1.7
        // return entityLiving.getCommandSenderName();

        // 1.8
        return entity.getName();
    }

    @Override
    public boolean hasCustomName(Entity entity)
    {
        // 1.7
        // return entity.hasCustomNameTag();

        // 1.8
        return entity.hasCustomName();
    }

    @Override
    public AxisAlignedBB getBoundingBox(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        // 1.7
        // return AxisAlignedBB.getBoundingBox(x1, y1, z1, x2, y2, z2);

        // 1.8
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public AxisAlignedBB getBoundingBox(EntityPlayer player, double lateralDistance, double verticalDistance)
    {
        // 1.7
        // return AxisAlignedBB.getBoundingBox(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(lateralDistance, verticalDistance, lateralDistance);

        // 1.8
        return new AxisAlignedBB(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(lateralDistance, verticalDistance, lateralDistance);
    }

    @Override
    public Vec3 newVec3(double x, double y, double z)
    {
        // 1.7
        // return Vec3.createVectorHelper(x, y, z);

        // 1.8
        return new Vec3(x, y, z);
    }

    /**
     * Gets the entity's bounding box.
     */
    @Override
    public AxisAlignedBB getEntityBoundingBox(EntityLivingBase entity)
    {
        // 1.7
        // return entity.boundingBox

        // 1.8
        return entity.getBoundingBox();
    }

    /**
     * TODO
     */
    @Override
    public String getRealmsServerName()
    {
        // 1.7
        String serverName = null;
//        Minecraft mc = ForgeHelper.INSTANCE.getClient();
//        if(!mc.isSingleplayer())
//        {
//            try
//            {
//                NetHandlerPlayClient netHandler = mc.getNetHandler();
//                GuiScreen netHandlerGui = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, netHandler, "field_147307_j", "guiScreenServer");
//
//                if (netHandlerGui instanceof GuiScreenRealmsProxy)
//                {
//                    RealmsScreen realmsScreen = ((GuiScreenRealmsProxy) netHandlerGui).func_154321_a();
//                    if (realmsScreen instanceof RealmsMainScreen)
//                    {
//                        RealmsMainScreen mainScreen = (RealmsMainScreen) realmsScreen;
//                        long selectedServerId = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "selectedServerId");
//                        List<McoServer> mcoServers = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "mcoServers");
//                        for (McoServer mcoServer : mcoServers)
//                        {
//                            if (mcoServer.id == selectedServerId)
//                            {
//                                serverName = mcoServer.name;
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//            catch (Throwable t)
//            {
//                JourneyMap.getLogger().error("Unable to get Realms server name: " + LogFormatter.toString(t));
//            }
//        }
//
//        if (serverName != null)
//        {
//            return serverName;
//        }
//        else
//        {
//            Minecraft mc = ForgeHelper.INSTANCE.getClient();
//            ServerData serverData = mc.func_147104_D(); // 1.8 getServerData()
//
//            if (serverData != null)
//            {
//                serverName = serverData.serverName;
//                if (serverName != null)
//                {
//                    serverName = serverName.replaceAll("\\W+", "~").trim();
//
//                    if (Strings.isNullOrEmpty(serverName.replaceAll("~", "")))
//                    {
//                        serverName = serverData.serverIP;
//                    }
//                    return serverName;
//                }
//            }
//        }

        // 1.8 TODO
        return null;
    }

    @Override
    public Vec3 getEntityPositionVector(Entity entity)
    {
        // 1.7
        // return entity.getPosition(1);

        // 1.8
        return entity.getPositionVector();
    }

    @Override
    public Tessellator getTessellator()
    {
        // 1.7
        // return Tessellator.instance;

        // 1.8
        return Tessellator.getInstance();
    }

    @Override
    public boolean canBlockSeeTheSky(Chunk chunk, int x, int y, int z)
    {
        // 1.7
        // return chunk.canBlockSeeTheSky(x, y, z);

        // 1.8
        return chunk.canSeeSky(new BlockPos(x, y, z));
    }

    @Override
    public int getHeightValue(Chunk chunk, int x, int z)
    {
        // 1.7
        // return chunk.getHeightValue(x, z);

        // 1.8
        return chunk.getHeight(x, z);
    }

    @Override
    public int getAbsoluteHeightValue(Chunk chunk, int x, int z)
    {
        // 1.7
        // return chunk.getPrecipitationHeight(x, z);

        // 1.8
        return chunk.getPrecipitationHeight(new BlockPos((chunk.xPosition << 4) + x, 0, (chunk.zPosition << 4) + z)).getY();
    }

    @Override
    public int getPrecipitationHeight(Chunk chunk, int x, int z)
    {
        // 1.7
        // return chunk.getPrecipitationHeight(x, z);

        // 1.8
        return chunk.getPrecipitationHeight(new BlockPos((chunk.xPosition << 4) + x, 0, (chunk.zPosition << 4) + z)).getY();
    }

    @Override
    public int getLightOpacity(Chunk chunk, Block block, int localX, int y, int localZ)
    {
        // 1.7
        // return block.getLightOpacity(chunk.getWorld(), (this.getCoord().chunkXPos << 4) + localX, y, (this.getCoord().chunkZPos << 4) + localZ);

        // 1.8
        return block.getLightOpacity(chunk.getWorld(), new BlockPos((chunk.xPosition << 4) + localX, y, (chunk.zPosition << 4) + localZ));
    }

    @Override
    public TileEntity getTileEntity(World world, int blockX, int y, int blockZ)
    {
        // 1.7
        // return world.getTileEntity(blockX, y, blockZ);

        // 1.8
        return world.getTileEntity(new BlockPos(blockX, y, blockZ));
    }

    @Override
    public String getBlockName(Block block, int meta)
    {
        // Gotta love this.
        Item item = Item.getItemFromBlock(block);
        if (item == null)
        {
            // 1.7
            // item = block.getItemDropped(0, new Random(), 0);

            // 1.8
            item = block.getItemDropped(block.getStateFromMeta(0), new Random(), 0);
        }
        if (item != null)
        {
            // 1.7
            // ItemStack stack = new ItemStack(item, 1, block.damageDropped(meta));

            // 1.8
            ItemStack stack = new ItemStack(item, 1, block.damageDropped(block.getStateFromMeta(meta)));

            String displayName = stack.getDisplayName();
            if (!Strings.isNullOrEmpty(displayName))
            {
                return displayName;
            }
        }
        return null;
    }

    @Override
    public BiomeGenBase getBiome(World world, int x, int y, int z)
    {
        // 1.7
        // return world.getBiomeGenForCoords(x, y, z);

        // 1.7
        return world.getBiomeGenForCoords(new BlockPos(x, y, z));
    }

    @Override
    public int getBlockMeta(Chunk chunk, final int x, int y, final int z)
    {
        // 1.7
        // return chunk.getBlockMetadata(x,y,z);

        // 1.8
        return chunk.getBlockMetadata(new BlockPos(x, y, z));
    }

    @Override
    public boolean hasNoSky(Entity entity)
    {
        // 1.7
        // return hasNoSky(entity.worldObj);

        // 1.8
        return hasNoSky(entity.getEntityWorld());
    }

    @Override
    public int getFoliageColor(BiomeGenBase biome, int x, int y, int z)
    {
        // 1.7
        //return biome.getBiomeFoliageColor(x, y, z);

        // 1.8
        return biome.getFoliageColorAtPos(new BlockPos(x, y, z));
    }

    @Override
    public int getGrassColor(BiomeGenBase biome, int x, int y, int z)
    {
        // 1.7
        //return biome.getBiomeGrassColor(x, y, z);

        // 1.8
        return biome.getGrassColorAtPos(new BlockPos(x, y, z));
    }

    @Override
    public int getColorMultiplier(World world, Block block, int x, int y, int z)
    {
        // 1.7
        // return block.colorMultiplier(world, x, 78, z)

        // 1.8
        return block.colorMultiplier(world, new BlockPos(x, y, z));
    }

    @Override
    public int getRenderColor(BlockMD blockMD)
    {
        // 1.7
        // return blockMD.getBlock().getRenderColor(blockMD.meta);

        // 1.8
        Block block = blockMD.getBlock();
        IBlockState blockState = block.getStateFromMeta(blockMD.meta);
        return block.getRenderColor(blockState);
    }

    @Override
    public boolean hasChunkData(Chunk chunk)
    {
        // 1.7
        // return (chunk.isChunkLoaded && !chunk.isEmpty());

        // 1.8
        return (chunk.isLoaded() && !chunk.isEmpty());
    }

    @Override
    public Iterator<Block> getRegisteredBlocks()
    {
        // package change
        return GameData.getBlockRegistry().iterator();
    }

    @Override
    public SocketAddress getSocketAddress(NetworkManager netManager)
    {
        // 1.7
        // return netManager.getSocketAddress();

        // 1.8
        return netManager.getRemoteAddress();
    }
}
