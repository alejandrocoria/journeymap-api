/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper;

import journeymap.client.model.BlockMD;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import java.net.SocketAddress;
import java.util.Iterator;

/**
 * Interface used to encapsulate compile-time differences between Minecraft/Forge versions.
 */
public interface IForgeHelper
{
    public IBlockAccess getIBlockAccess();

    public IColorHelper getColorHelper();

    //public Minecraft getClient();

    public ScaledResolution getScaledResolution();

    public EnumSkyBlock getSkyBlock();

    public FontRenderer getFontRenderer();

    public int getPlayerDimension();

    public boolean hasNoSky(World world);

    public World getWorld();

    //public World getWorld(Chunk chunk);

    public RenderManager getRenderManager();

    public String getEntityName(Entity entity);

    public boolean hasCustomName(Entity entity);

    public int getLightOpacity(BlockMD blockMD, BlockPos blockPos);

    public AxisAlignedBB getBoundingBox(EntityPlayer player, double lateralDistance, double verticalDistance);

    public AxisAlignedBB getEntityBoundingBox(EntityLivingBase entity);

    public AxisAlignedBB getBoundingBox(int x1, int y1, int z1, int x2, int y2, int z2);

    public Vec3 newVec3(double x, double y, double z);

    public Vec3 getEntityPositionVector(Entity entity);

    public String getRealmsServerName();

    public Tessellator getTessellator();

    public int getDimension();

    public int getSavedLightValue(Chunk chunk, BlockPos blockPos);

    public boolean canBlockSeeTheSky(Chunk chunk, BlockPos blockPos);

    public int getHeight(Chunk chunk, BlockPos blockPos);

    public int getPrecipitationHeight(BlockPos blockPos);

    // public int getLightOpacity(Chunk chunk, Block block, int localX, int y, int localZ);

    public TileEntity getTileEntity(BlockPos blockPos);

    public BiomeGenBase getBiome(BlockPos blockPos);

    public boolean hasNoSky(Entity entity);

    public boolean hasChunkData(Chunk chunk);

    public Iterator<Block> getRegisteredBlocks();

    public SocketAddress getSocketAddress(NetworkManager netManager);

    public String getFPS();
}
