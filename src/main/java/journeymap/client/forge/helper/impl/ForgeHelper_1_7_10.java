/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper.impl;

import com.google.common.base.Strings;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.McoServer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.ReflectionHelper;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IColorHelper;
import journeymap.client.forge.helper.IForgeHelper;
import journeymap.client.forge.helper.IRenderHelper;
import journeymap.client.log.LogFormatter;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Implementation to encapsulate uses of methods/fields that differ from 1.8
 */
public class ForgeHelper_1_7_10 implements IForgeHelper
{
    private IRenderHelper renderHelper = new RenderHelper_1_7_10();
    private IBlockAccess blockAccess = new JmBlockAccess();

    @Override
    public IRenderHelper getRenderHelper()
    {
        return renderHelper;
    }

    @Override
    public IBlockAccess getIBlockAccess()
    {
        return blockAccess;
    }

    @Override
    public IColorHelper getColorHelper()
    {
        return new ColorHelper_1_7_10();
    }

    @Override
    public Minecraft getClient()
    {
        return FMLClientHandler.instance().getClient();
    }

    @Override
    public EnumSkyBlock getSkyBlock()
    {
        // 1.7
        return EnumSkyBlock.Block;
    }

    @Override
    public FontRenderer getFontRenderer()
    {
        // 1.7
        return getClient().fontRendererObj;
    }

    @Override
    public int getPlayerDimension()
    {
        // 1.7
        return getClient().thePlayer.worldObj.provider.dimensionId;
    }

    @Override
    public boolean hasNoSky(World world)
    {
        // 1.7
        return world.provider.hasNoSky;
    }

    @Override
    public World getWorld()
    {
        return getClient().theWorld;
    }

    @Override
    public World getWorld(Chunk chunk)
    {
        // 1.7
        return chunk.worldObj;
    }

    @Override
    public int getLightOpacity(BlockMD blockMD, int x, int y, int z)
    {
        // 1.7
        return blockMD.getBlock().getLightOpacity(blockAccess, x & 15, y, z & 15);
    }

    @Override
    public int getDimension(World world)
    {
        // 1.7
        return world.provider.dimensionId;
    }

    @Override
    public int getDimension(WorldProvider worldProvider)
    {
        // 1.7
        return worldProvider.dimensionId;
    }

    @Override
    public int getSavedLightValue(Chunk chunk, int localX, int y, int localZ)
    {
        // 1.7
        return chunk.getSavedLightValue(getSkyBlock(), localX & 15, y, localZ & 15);
    }

    @Override
    public RenderManager getRenderManager()
    {
        // 1.7
        return RenderManager.instance;
    }

    /**
     * Gets the entity's name (player name) / command sender name.
     */
    @Override
    public String getEntityName(Entity entity)
    {
        // 1.7
        return entity.getCommandSenderName();
    }

    @Override
    public boolean hasCustomName(Entity entity)
    {
        // 1.7
        return ((EntityLiving) entity).hasCustomNameTag();
    }

    @Override
    public AxisAlignedBB getBoundingBox(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        // 1.7
        return AxisAlignedBB.getBoundingBox(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public AxisAlignedBB getBoundingBox(EntityPlayer player, double lateralDistance, double verticalDistance)
    {
        // 1.7
        return AxisAlignedBB.getBoundingBox(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(lateralDistance, verticalDistance, lateralDistance);
    }

    @Override
    public Vec3 newVec3(double x, double y, double z)
    {
        // 1.7
        return Vec3.createVectorHelper(x, y, z);
    }

    /**
     * Gets the entity's bounding box.
     */
    @Override
    public AxisAlignedBB getEntityBoundingBox(EntityLivingBase entity)
    {
        // 1.7
        return entity.boundingBox;
    }

    /**
     * Gets the server name.
     */
    @Override
    public String getRealmsServerName()
    {
        // 1.7
        String serverName = null;
        Minecraft mc = ForgeHelper.INSTANCE.getClient();
        if(!mc.isSingleplayer())
        {
            try
            {
                NetHandlerPlayClient netHandler = mc.getNetHandler();
                GuiScreen netHandlerGui = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, netHandler, "field_147307_j", "guiScreenServer");

                if (netHandlerGui instanceof GuiScreenRealmsProxy)
                {
                    RealmsScreen realmsScreen = ((GuiScreenRealmsProxy) netHandlerGui).func_154321_a();
                    if (realmsScreen instanceof RealmsMainScreen)
                    {
                        RealmsMainScreen mainScreen = (RealmsMainScreen) realmsScreen;
                        long selectedServerId = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "selectedServerId");
                        List<McoServer> mcoServers = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "mcoServers");
                        for (McoServer mcoServer : mcoServers)
                        {
                            if (mcoServer.id == selectedServerId)
                            {
                                serverName = mcoServer.name;
                                break;
                            }
                        }
                    }
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error("Unable to get Realms server name: " + LogFormatter.toString(t));
            }
        }

        if (serverName != null)
        {
            return serverName;
        }
        else
        {
            ServerData serverData = mc.getCurrentServerData();

            if (serverData != null)
            {
                serverName = serverData.serverName;
                if (serverName != null)
                {
                    serverName = serverName.replaceAll("\\W+", "~").trim();

                    if (Strings.isNullOrEmpty(serverName.replaceAll("~", "")))
                    {
                        serverName = serverData.serverIP;
                    }
                    return serverName;
                }
            }
        }

        if (serverName != null)
        {
            return serverName;
        }
        else
        {
            mc = ForgeHelper.INSTANCE.getClient();
            ServerData serverData = mc.getCurrentServerData();

            if (serverData != null)
            {
                serverName = serverData.serverName;
                if (serverName != null)
                {
                    serverName = serverName.replaceAll("\\W+", "~").trim();

                    if (Strings.isNullOrEmpty(serverName.replaceAll("~", "")))
                    {
                        serverName = serverData.serverIP;
                    }
                    return serverName;
                }
            }
        }
        return null;
    }

    @Override
    public Vec3 getEntityPositionVector(Entity entity)
    {
        return Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
    }

    @Override
    public Tessellator getTessellator()
    {
        return Tessellator.instance;
    }

    @Override
    public boolean canBlockSeeTheSky(Chunk chunk, int x, int y, int z)
    {
        return chunk.canBlockSeeTheSky(x, y, z);
    }

    @Override
    public int getHeightValue(Chunk chunk, int x, int z)
    {
        return chunk.getHeightValue(x, z);
    }

    @Override
    public int getAbsoluteHeightValue(Chunk chunk, int x, int z)
    {
        return chunk.getPrecipitationHeight(x, z);
    }

    @Override
    public int getPrecipitationHeight(Chunk chunk, int x, int z)
    {
        return chunk.getPrecipitationHeight(x, z);
    }

    @Override
    public int getLightOpacity(Chunk chunk, Block block, int localX, int y, int localZ)
    {
        return block.getLightOpacity(chunk.worldObj, (chunk.xPosition << 4) + localX, y, (chunk.zPosition << 4) + localZ);
    }

    @Override
    public TileEntity getTileEntity(int localX, int y, int localZ)
    {
        return blockAccess.getTileEntity(localX, y, localZ);
    }

    @Override
    public String getBlockName(Block block, int meta)
    {
        // Gotta love this.
        Item item = Item.getItemFromBlock(block);
        if (item == null)
        {
            item = block.getItemDropped(0, new Random(), 0);
        }
        if (item != null)
        {
            ItemStack stack = new ItemStack(item, 1, block.damageDropped(meta));

            String displayName = stack.getDisplayName();
            if (!Strings.isNullOrEmpty(displayName))
            {
                return displayName;
            }
        }
        return null;
    }

    @Override
    public BiomeGenBase getBiome(ChunkMD chunkMD, int x, int y, int z)
    {
        if (chunkMD != null && chunkMD.hasChunk())
        {
            try
            {
                Chunk chunk = chunkMD.getChunk();
                BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(x & 15, z & 15, ForgeHelper.INSTANCE.getWorld().getWorldChunkManager());
                if (biome != null)
                {
                    return biome;
                }
            }
            catch (Throwable throwable)
            {
                Journeymap.getLogger().error("Error in getBiome(): " + throwable);
            }
        }
        return ForgeHelper.INSTANCE.getWorld().getBiomeGenForCoords(x, z);
    }

    @Override
    public BiomeGenBase getBiome(int x, int y, int z)
    {
        ChunkMD chunkMD = DataCache.instance().getChunkMD(new ChunkCoordIntPair(x >> 4, z >> 4));
        return getBiome(chunkMD, x, y, z);
    }

    @Override
    public int getBlockMeta(Chunk chunk, final int x, int y, final int z)
    {
        return chunk.getBlockMetadata(x,y,z);
    }

    @Override
    public boolean hasNoSky(Entity entity)
    {
        return hasNoSky(entity.worldObj);
    }

    @Override
    public boolean hasChunkData(Chunk chunk)
    {
        return (chunk.isChunkLoaded && !chunk.isEmpty());
    }

    @Override
    public Iterator<Block> getRegisteredBlocks()
    {
        return GameData.getBlockRegistry().iterator();
    }

    @Override
    public SocketAddress getSocketAddress(NetworkManager netManager)
    {
        return netManager.getRemoteAddress();
    }

    class JmBlockAccess implements IBlockAccess
    {
        private Chunk getChunk(int x, int z)
        {
            ChunkMD chunkMD = DataCache.instance().getChunkMD(new ChunkCoordIntPair(x >> 4, z >> 4));
            if (chunkMD != null && chunkMD.hasChunk())
            {
                return chunkMD.getChunk();
            }
            return null;
        }

        @Override
        public Block getBlock(int x, int y, int z)
        {
            if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000 && y >= 0 && y < 256)
            {
                Chunk chunk = getChunk(x, z);
                if (chunk != null)
                {
                    return chunk.getBlock(x & 15, y, z & 15);
                }
            }
            return Blocks.air;
        }

        @Override
        public TileEntity getTileEntity(int x, int y, int z)
        {
            return ForgeHelper.INSTANCE.getWorld().getTileEntity(x, y, z);
        }

        @Override
        public int getLightBrightnessForSkyBlocks(int x, int y, int z, int min)
        {
            return ForgeHelper.INSTANCE.getWorld().getLightBrightnessForSkyBlocks(x, y, z, min);
        }

        @Override
        public int getBlockMetadata(int x, int y, int z)
        {
            if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000)
            {
                if (y >= 0 && y < 256)
                {
                    Chunk chunk = getChunk(x, z);
                    if (chunk != null)
                    {
                        x &= 15;
                        z &= 15;
                        return chunk.getBlockMetadata(x, y, z);
                    }
                }
            }
            return 0;
        }

        @Override
        public int isBlockProvidingPowerTo(int x, int y, int z, int directionIn)
        {
            return ForgeHelper.INSTANCE.getWorld().isBlockProvidingPowerTo(x, y, z, directionIn);
        }

        @Override
        public boolean isAirBlock(int x, int y, int z)
        {
            Block block = this.getBlock(x, y, z);
            return block.isAir(this, x, y, z);
        }

        @Override
        public BiomeGenBase getBiomeGenForCoords(int x, int z)
        {
            Chunk chunk = getChunk(x, z);
            if (chunk != null)
            {
                try
                {
                    BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(x & 15, z & 15, ForgeHelper.INSTANCE.getWorld().getWorldChunkManager());
                    if (biome == null)
                    {
                        return null;
                    }
                    return biome;
                }
                catch (Throwable throwable)
                {
                    Journeymap.getLogger().error("Error in getBiomeGenForCoords(): " + throwable);
                    return ForgeHelper.INSTANCE.getWorld().getBiomeGenForCoords(x, z);
                }
            }
            else
            {
                return ForgeHelper.INSTANCE.getWorld().provider.getBiomeGenForCoords(x, z);
            }
        }

        @Override
        public int getHeight()
        {
            return ForgeHelper.INSTANCE.getWorld().getHeight();
        }

        @Override
        public boolean extendedLevelsInChunkCache()
        {
            return ForgeHelper.INSTANCE.getWorld().extendedLevelsInChunkCache();
        }

        @Override
        public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default)
        {
            if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000)
            {
                return _default;
            }

            Chunk chunk = getChunk(x >> 4, z >> 4);
            if (chunk == null || chunk.isEmpty())
            {
                return _default;
            }
            return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
        }
    }
}