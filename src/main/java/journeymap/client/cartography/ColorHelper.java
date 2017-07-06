/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography;

import com.google.common.base.Joiner;
import journeymap.client.log.JMLogger;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton that encapsulates color-related functions that have changed frequently since 1.7.10.
 */
public enum ColorHelper
{
    /**
     * Instance color helper.
     */
    INSTANCE;

    /**
     * The Sprite facings.
     */
    static List<EnumFacing> spriteFacings = Arrays.asList(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST);

    /**
     * The Logger.
     */
    Logger logger = Journeymap.getLogger();
    /**
     * The Failed.
     */
    HashSet<BlockMD> failed = new HashSet<BlockMD>();
    /**
     * The Block colors.
     */
    BlockColors blockColors = FMLClientHandler.instance().getClient().getBlockColors();
    /**
     * The Icon color cache.
     */
    HashMap<String, float[]> iconColorCache = new HashMap<>();

    /**
     * Reset icon color cache.
     */
    public void resetIconColorCache()
    {
        iconColorCache.clear();
        failed.clear();
    }

    /**
     * Has cached icon colors boolean.
     *
     * @return the boolean
     */
    public boolean hasCachedIconColors()
    {
        return !iconColorCache.isEmpty();
    }

    /**
     * Cached icon colors int.
     *
     * @return the int
     */
    public int cachedIconColors()
    {
        return iconColorCache.size();
    }

    /**
     * Failed for boolean.
     *
     * @param blockMD the block md
     * @return the boolean
     */
    public boolean failedFor(BlockMD blockMD)
    {
        return failed.contains(blockMD);
    }

    /**
     * Gets color multiplier.
     *
     * @param chunkMD  the chunk md
     * @param blockMD  the block md
     * @param blockPos the block pos
     * @return the color multiplier
     */
    public int getColorMultiplier(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        if (chunkMD == null || !chunkMD.hasChunk())
        {
            return blockColors.colorMultiplier(blockMD.getBlockState(), JmBlockAccess.INSTANCE, blockPos, 2);
        }
        else
        {
            IBlockState blockState = chunkMD.getChunk().getBlockState(blockPos);
            if (Blocks.AIR.getDefaultState().equals(blockState))
            {
                return RGB.WHITE_RGB;
            }
            else
            {
                return blockColors.colorMultiplier(blockMD.getBlockState(), JmBlockAccess.INSTANCE, blockPos, 2);
            }
        }
    }


    /**
     * Gets map color.
     *
     * @param blockMD the block md
     * @return the map color
     */
    public int getMapColor(BlockMD blockMD)
    {
        return blockMD.getBlockState().getMaterial().getMaterialMapColor().colorValue;
    }

    /**
     * Derive block color from the corresponding texture.
     *
     * @param blockMD the block md
     * @return the texture color
     */
    public Integer getTextureColor(BlockMD blockMD)
    {
        try
        {
            // Invisible or Air?
            IBlockState blockState = blockMD.getBlockState();
            if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE
                    || Material.AIR.equals(blockState.getMaterial()))
            {
                setBlockColorToAir(blockMD);
                return blockMD.getColor();
            }

            Collection<TextureAtlasSprite> sprites = blockMD.getBlockColorHandler().getSprites(blockMD);
            if (!sprites.isEmpty())
            {
                // Collate name
                List<String> names = sprites.stream().map(TextureAtlasSprite::getIconName).collect(Collectors.toList());
                Collections.sort(names);
                String name = Joiner.on(",").join(names);
                blockMD.setIconName(name);

                float[] rgba = null;
                if (iconColorCache.containsKey(name))
                {
                    rgba = iconColorCache.get(name);
                }
                else
                {
                    rgba = getAverageColor(blockMD, sprites);
                    if (rgba != null)
                    {
                        iconColorCache.put(name, rgba);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(String.format("Cached color %s for %s", RGB.toHexString(RGB.toInteger(rgba)), name));
                        }
                    }
                }

                if (rgba != null)
                {
                    blockMD.setColor(RGB.toInteger(rgba));
                    return blockMD.getColor();
                }
            }

            if (blockState.getBlock() instanceof ITileEntityProvider)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Ignoring TitleEntity without standard block texture: " + blockMD);
                }
                blockMD.addFlags(BlockMD.Flag.TileEntity);
                setBlockColorToAir(blockMD);
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(String.format("ColorHelper.setBlockColor(): Using MaterialMapColor for: %s", blockMD));
                }
                setBlockColorToMaterial(blockMD);
            }
            return blockMD.getColor();
        }
        catch (Throwable e1)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Error deriving color for " + blockMD + ": " + LogFormatter.toString(e1));
            }
            setBlockColorToMaterial(blockMD);
            return blockMD.getColor();
        }
    }

    private float[] getAverageColor(BlockMD blockMD, Collection<TextureAtlasSprite> sprites)
    {
        List<BufferedImage> images = getImages(blockMD, sprites);

        if (images.isEmpty())
        {
            return null;
        }

        int a, r, g, b, count, alpha;
        a = r = g = b = count = 0;
        for (BufferedImage image : images)
        {
            try
            {
                int[] argbInts = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
                for (int argb : argbInts)
                {
                    alpha = (argb >> 24) & 0xFF;
                    if (alpha > 0)
                    {
                        count++;
                        a += alpha;
                        r += (argb >> 16) & 0xFF;
                        g += (argb >> 8) & 0xFF;
                        b += (argb) & 0xFF;
                    }
                }
            }
            catch (Exception e)
            {
                continue;
            }
        }

        if (count > 0)
        {
            int rgb = RGB.toInteger(r / count, g / count, b / count);
            return RGB.floats(rgb, a / count);
        }
        return null;
    }


    public Collection<TextureAtlasSprite> getSprites(BlockMD blockMD)
    {
        IBlockState blockState = blockMD.getBlockState();
        Block block = blockState.getBlock();

        // Always get the upper portion of a double plant for rendering
        if (block instanceof BlockDoublePlant)
        {
            if (blockState.getValue(BlockDoublePlant.HALF).toString().equals("lower"))
            {
                // cycle to upper
                blockState = blockState.cycleProperty(BlockDoublePlant.HALF);
            }
        }

        if (block instanceof IFluidBlock)
        {
            ResourceLocation loc = ((IFluidBlock) block).getFluid().getStill();
            TextureAtlasSprite tas = FMLClientHandler.instance().getClient().getTextureMapBlocks().getAtlasSprite(loc.toString());
            return Collections.singletonList(tas);
        }

        if (blockState.getMaterial() == Material.GRASS)
        {
            blockMD.addFlags(BlockMD.Flag.Grass);
        }

        return getSprites(blockState);
    }

    private Collection<TextureAtlasSprite> getSprites(IBlockState blockState)
    {
        HashMap<String, TextureAtlasSprite> sprites = new HashMap<>();
        BlockModelShapes bms = FMLClientHandler.instance().getClient().getBlockRendererDispatcher().getBlockModelShapes();
        IBakedModel model = bms.getModelForState(blockState);
        if (model != null)
        {
            PropertyDirection propertyDirection = PropertyDirection.create("facing");
            if (blockState.getPropertyNames().contains(propertyDirection))
            {
                // Works for directional blocks only
                blockState = blockState.withProperty(propertyDirection, EnumFacing.UP);
            }
            else
            {
                blockState = blockState;
            }

            for (EnumFacing face : Arrays.asList(EnumFacing.UP, null))
            {
                try
                {
                    List<BakedQuad> quads = new ArrayList<>(0);

                    BlockRenderLayer originalLayer = MinecraftForgeClient.getRenderLayer();
                    try
                    {
                        for (BlockRenderLayer layer : BlockRenderLayer.values())
                        {
                            ForgeHooksClient.setRenderLayer(layer);
                            try
                            {
                                List<BakedQuad> faceInLayerQuads = model.getQuads(blockState, face, 0);
                                if (faceInLayerQuads != null && !faceInLayerQuads.isEmpty())
                                {
                                    quads.addAll(faceInLayerQuads);
                                }
                                else
                                {
                                    blockState = blockState;
                                }
                            }
                            catch (Exception e)
                            {
                                JMLogger.logOnce(String.format("Error calling %s.getQuads(IBlockState, %s, 0) in BlockRenderLayer %s",
                                        model.getClass().getName(), face, layer), e);
                            }
                        }
                    }
                    finally
                    {
                        ForgeHooksClient.setRenderLayer(originalLayer);
                    }

                    if (!quads.isEmpty())
                    {
                        for (BakedQuad quad : quads)
                        {
                            TextureAtlasSprite sprite = quad.getSprite();
                            if (!sprites.containsKey(sprite.getIconName()))
                            {
                                ResourceLocation resourceLocation = new ResourceLocation(sprite.getIconName());
                                if (resourceLocation.equals(TextureMap.LOCATION_MISSING_TEXTURE))
                                {
                                    continue;
                                }

                                sprites.put(sprite.getIconName(), sprite);
                            }
                        }
                    }

                    if (!sprites.isEmpty())
                    {
                        break;
                    }
                    else
                    {
                        continue;
                    }
                }
                catch (Exception e)
                {
                    Journeymap.getLogger().error(String.format("Error getting EnumFacing.%s for %s:\n%s\n\t%s\n\t%s",
                            face, blockState, e, e.getStackTrace()[0], e.getStackTrace()[1]));
                    break;
                }
            }
        }

        if (sprites.isEmpty())
        {
            TextureAtlasSprite sprite = bms.getTexture(blockState);
            if (!sprite.getIconName().startsWith("minecraft:"))
            {
                Journeymap.getLogger().warn(String.format("Falling back to particle texture for block color in %s", blockState));
            }
            sprites.put(sprite.getIconName(), sprite);
        }
        return sprites.values();
    }


    private void setBlockColorToError(BlockMD blockMD)
    {
        blockMD.setAlpha(0);
        blockMD.addFlags(BlockMD.Flag.HasAir, BlockMD.Flag.Error);
        blockMD.setColor(null);
        failed.add(blockMD);
    }

    private void setBlockColorToAir(BlockMD blockMD)
    {
        blockMD.setAlpha(0);
        blockMD.addFlags(BlockMD.Flag.HasAir);
        blockMD.setColor(RGB.WHITE_RGB);
    }

    private void setBlockColorToMaterial(BlockMD blockMD)
    {
        try
        {
            blockMD.setAlpha(1);
            blockMD.addFlags(BlockMD.Flag.HasAir);
            blockMD.setColor(getMapColor(blockMD));
        }
        catch (Exception e)
        {
            logger.warn(String.format("Failed to use MaterialMapColor, marking as error: %s", blockMD));
            setBlockColorToError(blockMD);
        }
    }

    private List<BufferedImage> getImages(BlockMD blockMD, Collection<TextureAtlasSprite> sprites)
    {
        List<BufferedImage> images = new ArrayList<>(sprites.size());
        for (TextureAtlasSprite icon : sprites)
        {
            final String iconName = icon.getIconName();
            try
            {
                ResourceLocation resourceLocation = new ResourceLocation(iconName);

                // Missing texture? Skip
                if (resourceLocation.equals(TextureMap.LOCATION_MISSING_TEXTURE))
                {
                    continue;
                }

                // Plan A: Get image from texture data
                BufferedImage image = getImageFromFrameTextureData(icon);
                if (image == null || image.getWidth() == 0)
                {
                    // Plan B: Get image from texture's source PNG
                    image = getImageFromResourceLocation(icon);
                }
                if (image == null || image.getWidth() == 0)
                {
                    continue;
                }

                images.add(image);
            }
            catch (Throwable e1)
            {
                if (logger.isDebugEnabled())
                {
                    logger.error("Error getting image for " + iconName + ": " + LogFormatter.toString(e1));
                }
                continue;
            }
        }
        return images;
    }

    /**
     * Primary means of getting block texture:  FrameTextureData
     */
    private BufferedImage getImageFromFrameTextureData(TextureAtlasSprite tas)
    {
        try
        {
            if (tas.getFrameCount() > 0)
            {
                final int[] rgb = tas.getFrameTextureData(0)[0];
                if (rgb.length > 0)
                {
                    int width = tas.getIconWidth();
                    int height = tas.getIconHeight();
                    BufferedImage textureImg = new BufferedImage(width, height, 2);
                    textureImg.setRGB(0, 0, width, height, rgb, 0, width);
                    return textureImg;
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("ColorHelper.getColorForIcon(): Unable to use frame data for %s: %s", tas.getIconName(), t.getMessage()));
        }
        return null;
    }

    /**
     * Secondary means of getting block texture:  derived ResourceLocation
     */
    private BufferedImage getImageFromResourceLocation(TextureAtlasSprite tas)
    {
        try
        {
            ResourceLocation iconNameLoc = new ResourceLocation(tas.getIconName());
            ResourceLocation fileLoc = new ResourceLocation(iconNameLoc.getResourceDomain(), "textures/" + iconNameLoc.getResourcePath() + ".png");
            return TextureCache.resolveImage(fileLoc);
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().debug(String.format("ColorHelper.getColorForIcon(): Unable to use texture file for %s: %s", tas.getIconName(), t.getMessage()));
        }
        return null;
    }
}
