package journeymap.client.mod.vanilla;

import journeymap.client.cartography.color.ColoredSprite;
import journeymap.client.mod.IBlockSpritesProxy;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Default handler for deriving sprites from block models.
 */
public class VanillaBlockSpriteProxy implements IBlockSpritesProxy
{
    private static Logger logger = Journeymap.getLogger();
    BlockModelShapes bms = FMLClientHandler.instance().getClient().getBlockRendererDispatcher().getBlockModelShapes();

    @Nullable
    @Override
    public Collection<ColoredSprite> getSprites(BlockMD blockMD)
    {
        IBlockState blockState = blockMD.getBlockState();
        Block block = blockState.getBlock();

        if (block instanceof IFluidBlock)
        {
            ResourceLocation loc = ((IFluidBlock) block).getFluid().getStill();
            TextureAtlasSprite tas = FMLClientHandler.instance().getClient().getTextureMapBlocks().getAtlasSprite(loc.toString());
            return Collections.singletonList(new ColoredSprite(tas, null));
        }

        // Always get the upper portion of a double plant for rendering
        if (blockState.getProperties().containsKey(BlockDoublePlant.HALF))
        {
            blockState = blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);
        }

        HashMap<String, ColoredSprite> map = new HashMap<>();
        try
        {
            IBlockState upFacing = BlockMD.getUpFacing(blockMD.getBlockState(), null);
            if (upFacing != null)
            {
                blockState = upFacing;
            }

            IBakedModel model = bms.getModelForState(blockState);

            outer:
            for (IBlockState state : new IBlockState[]{blockState, null})
            {
                for (EnumFacing facing : new EnumFacing[]{EnumFacing.UP, null})
                {
                    if (getSprites(blockMD, model, state, facing, map))
                    {
                        break outer;
                    }
                }
            }

            if (map.isEmpty())
            {
                TextureAtlasSprite defaultSprite = bms.getTexture(blockState);
                if (defaultSprite != null)
                {
                    map.put(defaultSprite.getIconName(), new ColoredSprite(defaultSprite, null));
                    if (!blockMD.isVanillaBlock() && logger.isDebugEnabled())
                    {
                        logger.debug(String.format("Resorted to using BlockModelStates.getTexture() to use %s as color for %s", defaultSprite.getIconName(), blockState));
                    }
                }
                else
                {
                    logger.warn(String.format("Unable to get any texture to use as color for %s", blockState));
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Unexpected error during getSprites(): " + LogFormatter.toPartialString(e));
        }

        return map.values();
    }


    private boolean getSprites(BlockMD blockMD, IBakedModel model, @Nullable IBlockState blockState, @Nullable EnumFacing facing, HashMap<String, ColoredSprite> map)
    {

        BlockRenderLayer originalLayer = MinecraftForgeClient.getRenderLayer();

        boolean success = false;
        try
        {
            for (BlockRenderLayer layer : BlockRenderLayer.values())
            {
                if (blockMD.getBlock().canRenderInLayer(blockState, layer))
                {
                    ForgeHooksClient.setRenderLayer(layer);
                    List<BakedQuad> quads = model.getQuads(blockState, facing, 0);
                    if (addSprites(map, quads))
                    {
                        if (!blockMD.isVanillaBlock() && logger.isDebugEnabled())
                        {
                            logger.debug(String.format("Success during [%s] %s.getQuads(%s, %s, %s)", layer, model.getClass(), blockState, facing, 0));
                        }
                        success = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error(String.format("Error during [%s] %s.getQuads(%s, %s, %s): %s", MinecraftForgeClient.getRenderLayer(), model.getClass(), blockState, facing, 0, LogFormatter.toPartialString(e)));
            }
        }
        finally
        {
            ForgeHooksClient.setRenderLayer(originalLayer);
        }
        return success;
    }

    public boolean addSprites(HashMap<String, ColoredSprite> sprites, List<BakedQuad> quads)
    {
        boolean added = false;
        if (quads != null)
        {
            for (BakedQuad quad : quads)
            {
                TextureAtlasSprite sprite = quad.getSprite();
                if (sprite != null)
                {
                    String iconName = quad.getSprite().getIconName();
                    if (!sprites.containsKey(iconName))
                    {
                        ResourceLocation resourceLocation = new ResourceLocation(iconName);
                        if (resourceLocation.equals(TextureMap.LOCATION_MISSING_TEXTURE))
                        {
                            continue;
                        }

                        sprites.put(iconName, new ColoredSprite(quad));
                        added = true;
                    }
                }
            }
        }
        return added;
    }

}
