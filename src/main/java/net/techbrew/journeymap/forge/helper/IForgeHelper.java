package net.techbrew.journeymap.forge.helper;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.journeymap.model.BlockMD;

/**
 * Interface used to encapsulate compile-time differences between Minecraft/Forge versions.
 */
public interface IForgeHelper
{
    public Minecraft getClient();

    public EnumSkyBlock getSkyBlock();

    public FontRenderer getFontRenderer();

    public int getPlayerDimension();

    public boolean hasNoSky(World world);

    public World getWorld(Chunk chunk);

    public RenderManager getRenderManager();

    public String getEntityName(Entity entity);

    public boolean hasCustomName(Entity entity);

    public int getLightOpacity(World world, BlockMD blockMD, int blockX, int blockY, int blockZ);

    public AxisAlignedBB getBoundingBox(EntityPlayer player, double lateralDistance, double verticalDistance);

    public AxisAlignedBB getEntityBoundingBox(EntityLivingBase entity);

    public Vec3 getEntityPositionVector(Entity entity);

    public String getRealmsServerName();

    public Tessellator getTessellator();

    public int getDimension(World world);

    public int getDimension(WorldProvider worldProvider);

    public int getSavedLightValue(Chunk chunk, int x, int y, int z);

    public boolean canBlockSeeTheSky(Chunk chunk, int x, int y, int z);

    public int getHeightValue(Chunk chunk, int x, int z);

    public int getLightOpacity(Chunk chunk, Block block, int localX, int y, int localZ);

    public int getAbsoluteHeightValue(Chunk chunk, int x, int z);

    public int getPrecipitationHeight(Chunk chunk, int x, int z);

    public TileEntity getTileEntity(World world, int blockX, int y, int blockZ);

    public String getBlockName(Block block, int meta);

    public BiomeGenBase getBiome(World world, int x, int y, int z);

    public int getBlockMeta(Chunk chunk, final int x, int y, final int z);

    public boolean hasNoSky(Entity entity);

    public int getFoliageColor(BiomeGenBase biome, int x, int y, int z);

    public int getGrassColor(BiomeGenBase biome, int x, int y, int z);

    public int getColorMultiplier(World world, Block block, int x, int y, int z);

    public int getRenderColor(BlockMD blockMD);

    public boolean hasChunkData(Chunk chunk);
}
