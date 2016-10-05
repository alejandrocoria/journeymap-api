/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper;

import com.google.common.base.Strings;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import journeymap.client.data.DataCache;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetworkManager;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;

/**
 * Singleton that encapsulates methods/fields that have changed frequently since 1.7.10.
 */
public enum ForgeHelper
{
    INSTANCE;

    private IBlockAccess blockAccess = new JmBlockAccess();

    public IBlockAccess getIBlockAccess()
    {
        return blockAccess;
    }


    public ScaledResolution getScaledResolution()
    {
        Minecraft mc = FMLClientHandler.instance().getClient();

        // 1.7.10, 1.8
        // return new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        // 1.8.8
        return new ScaledResolution(mc);
    }


    public EnumSkyBlock getSkyBlock()
    {
        // 1.7
        // return EnumSkyBlock.Block;

        // 1.8
        return EnumSkyBlock.BLOCK;
    }


    public FontRenderer getFontRenderer()
    {
        // 1.7
        // return FMLClientHandler.instance().getClient().fontRenderer;

        // 1.8
        return FMLClientHandler.instance().getClient().fontRendererObj;
    }


    public int getPlayerDimension()
    {
        // 1.7
        //return FMLClientHandler.instance().getClient().thePlayer.worldObj.provider.dimension;

        // 1.8
        return FMLClientHandler.instance().getClient().thePlayer.worldObj.provider.getDimension();
    }


    public boolean hasNoSky(World world)
    {
        // 1.7
        //return world.provider.hasNoSky;

        // 1.8
        return world.provider.getHasNoSky();
    }


    public World getWorld()
    {
        return FMLClientHandler.instance().getClient().theWorld;
    }
//
//    
//    public World getWorld(Chunk chunk)
//    {
//        // 1.7
//        // return getChunk().worldObj;
//
//        // 1.8
//        return chunk.getWorld();
//    }


    public int getLightOpacity(BlockMD blockMD, BlockPos blockPos)
    {
        return blockMD.getBlockState().getBlock().getLightOpacity(blockMD.getBlockState(), blockAccess, blockPos);
    }


    public int getDimension()
    {
        return getWorld().provider.getDimension();
    }


    public int getSavedLightValue(Chunk chunk, BlockPos blockPos)
    {
        // 1.7
        // return chunk.getSavedLightValue(getSkyBlock(), x, y, z);

        // 1.8
        try
        {
            return chunk.getLightFor(getSkyBlock(), blockPos);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            // Encountered on a custom-gen world where the value was 16
            return 1; // At least let it show up
        }
    }


    public RenderManager getRenderManager()
    {
        // 1.7
        // return RenderManager.instance;

        // 1.8
        return FMLClientHandler.instance().getClient().getRenderManager();
    }

    /**
     * Gets the entity's name (player name) / command sender name.
     *
     * @param entity
     * @return
     */

    public String getEntityName(Entity entity)
    {
        // 1.7
        // return entityLiving.getCommandSenderName();

        // 1.8, 1.8.8
        return entity.getName();
    }


    public boolean hasCustomName(Entity entity)
    {
        // 1.7
        // return entity.hasCustomNameTag();

        // 1.8
        return entity.hasCustomName();
    }


    public AxisAlignedBB getBoundingBox(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        // 1.7
        // return AxisAlignedBB.getBoundingBox(x1, y1, z1, x2, y2, z2);

        // 1.8
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }


    public AxisAlignedBB getBoundingBox(EntityPlayer player, double lateralDistance, double verticalDistance)
    {
        // 1.7
        // return AxisAlignedBB.getBoundingBox(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(lateralDistance, verticalDistance, lateralDistance);

        // 1.8
        return new AxisAlignedBB(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(lateralDistance, verticalDistance, lateralDistance);
    }


    public Vec3d newVec3(double x, double y, double z)
    {
        // 1.7
        // return Vec3d.createVectorHelper(x, y, z);

        // 1.8
        return new Vec3d(x, y, z);
    }

    /**
     * Gets the entity's bounding box.
     */

    public AxisAlignedBB getEntityBoundingBox(EntityLivingBase entity)
    {
        // 1.7
        // return entity.boundingBox

        // 1.8
        return entity.getEntityBoundingBox();
    }

    /**
     * Gets the server name.
     */

    public String getRealmsServerName()
    {
        String serverName = null;
        Minecraft mc = FMLClientHandler.instance().getClient();
        if (!mc.isSingleplayer())
        {
            try
            {
                NetHandlerPlayClient netHandler = mc.getConnection();
                GuiScreen netHandlerGui = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, netHandler, "field_147307_j", "guiScreenServer");

                if (netHandlerGui instanceof GuiScreenRealmsProxy)
                {
                    RealmsScreen realmsScreen = ((GuiScreenRealmsProxy) netHandlerGui).getProxy();
                    if (realmsScreen instanceof RealmsMainScreen)
                    {
                        RealmsMainScreen mainScreen = (RealmsMainScreen) realmsScreen;
                        long selectedServerId = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "selectedServerId");
                        List<RealmsServer> mcoServers = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "mcoServers");
                        for (RealmsServer mcoServer : mcoServers)
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
            mc = FMLClientHandler.instance().getClient();
            ServerData serverData = mc.getCurrentServerData(); // 1.8 getServerData()

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


    public Vec3d getEntityPositionVector(Entity entity)
    {
        // 1.7
        // return entity.getPosition(1);

        // 1.8
        return entity.getPositionVector();
    }


    public Tessellator getTessellator()
    {
        // 1.7
        // return Tessellator.instance;

        // 1.8
        return Tessellator.getInstance();
    }


    public boolean canBlockSeeTheSky(Chunk chunk, BlockPos blockPos)
    {
        // 1.7
        // return chunk.canBlockSeeTheSky(x, y, z);

        // 1.8
        return chunk.canSeeSky(blockPos);
    }

    public int toWorldX(Chunk chunk, int localX)
    {
        return (chunk.xPosition << 4) + localX;
    }

    public int toWorldZ(Chunk chunk, int localZ)
    {
        return (chunk.zPosition << 4) + localZ;
    }


    public int getHeight(Chunk chunk, BlockPos blockPos)
    {
        return chunk.getHeight(blockPos);
    }


    public int getPrecipitationHeight(BlockPos blockPos)
    {
        return getWorld().getPrecipitationHeight(blockPos).getY();
    }


    public TileEntity getTileEntity(BlockPos blockPos)
    {
        return blockAccess.getTileEntity(blockPos);
    }


    public Biome getBiome(BlockPos blockPos)
    {
        return getWorld().getBiomeForCoordsBody(blockPos);
    }


    public boolean hasNoSky(Entity entity)
    {
        return hasNoSky(entity.getEntityWorld());
    }


    public boolean hasChunkData(Chunk chunk)
    {
        return (chunk != null && chunk.isLoaded() && !(chunk instanceof EmptyChunk));
    }


    public Iterator<Block> getRegisteredBlocks()
    {
        // package change
        return GameData.getBlockRegistry().iterator();
    }


    public SocketAddress getSocketAddress(NetworkManager netManager)
    {
        return netManager.getRemoteAddress();
    }


    public String getFPS()
    {
        return String.format("%s fps", Minecraft.getDebugFPS());
    }

    private ChunkMD getChunkMDFromBlockCoords(BlockPos pos)
    {
        return DataCache.INSTANCE.getChunkMD(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }

    class JmBlockAccess implements IBlockAccess
    {
        public TileEntity getTileEntity(BlockPos pos)
        {
            return ForgeHelper.INSTANCE.getWorld().getTileEntity(pos);
        }

        public int getCombinedLight(BlockPos pos, int min)
        {
            return ForgeHelper.INSTANCE.getWorld().getCombinedLight(pos, min);
        }

        public IBlockState getBlockState(BlockPos pos)
        {
            if (!this.isValid(pos))
            {
                return Blocks.AIR.getDefaultState();
            }
            else
            {
                ChunkMD chunkMD = getChunkMDFromBlockCoords(pos);
                if (chunkMD != null && chunkMD.hasChunk())
                {
                    return chunkMD.getChunk().getBlockState(new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15));
                }
                return Blocks.AIR.getDefaultState();
            }
        }

        public boolean isAirBlock(BlockPos pos)
        {
            return ForgeHelper.INSTANCE.getWorld().isAirBlock(pos);
        }

        public Biome getBiomeGenForCoords(BlockPos pos)
        {
            ChunkMD chunkMD = getChunkMDFromBlockCoords(pos);
            if (chunkMD != null && chunkMD.hasChunk())
            {
                try
                {
                    Chunk chunk = chunkMD.getChunk();
                    Biome biome = chunk.getBiome(pos, ForgeHelper.INSTANCE.getWorld().getBiomeProvider());
                    if (biome == null)
                    {
                        return null;
                    }
                    return biome;
                }
                catch (Throwable throwable)
                {
                    Journeymap.getLogger().error("Error in getBiome(): " + throwable);
                    return ForgeHelper.INSTANCE.getWorld().getBiomeGenForCoords(pos);
                }
            }
            else
            {
                // 1.8
                // return ForgeHelper.INSTANCE.getWorld().getWorldChunkManager().func_180300_a(pos, Biome.plains);

                // 1.8.8
                return ForgeHelper.INSTANCE.getWorld().getBiomeProvider().getBiomeGenerator(pos, Biomes.PLAINS);
            }
        }

        // Not needed in 1.10.2
        @Override
        public boolean extendedLevelsInChunkCache()
        {
            return ForgeHelper.INSTANCE.getWorld().extendedLevelsInChunkCache();
        }


        public int getStrongPower(BlockPos pos, EnumFacing direction)
        {
            return ForgeHelper.INSTANCE.getWorld().getStrongPower(pos, direction);
        }


        public WorldType getWorldType()
        {
            return ForgeHelper.INSTANCE.getWorld().getWorldType();
        }


        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
        {
            return ForgeHelper.INSTANCE.getWorld().isSideSolid(pos, side, _default);
        }

        /**
         * Check if the given BlockPos has valid coordinates
         */
        private boolean isValid(BlockPos pos)
        {
            return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000 && pos.getY() >= 0 && pos.getY() < 256;
        }
    }
}
