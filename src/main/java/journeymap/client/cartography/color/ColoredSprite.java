package journeymap.client.cartography.color;

import journeymap.client.render.texture.TextureCache;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;

/**
 * Wrapper of sprite and potentially a color modifier.
 * Provides a BufferedImage from the wrapped sprite.
 * For now, the color modifier isn't implemented.
 */
@ParametersAreNonnullByDefault
public class ColoredSprite
{
    private static Logger logger = Journeymap.getLogger();
    private final Integer color;
    private final TextureAtlasSprite sprite;

    public ColoredSprite(TextureAtlasSprite sprite, @Nullable Integer color)
    {
        this.sprite = sprite;
        this.color = null;
    }

    public ColoredSprite(BakedQuad quad)
    {
        this.sprite = quad.getSprite();
        this.color = null;
    }

    public String getIconName()
    {
        return this.sprite.getIconName();
    }

    @Nullable
    public Integer getColor()
    {
        return this.color;
    }

    public boolean hasColor()
    {
        return this.color != null;
    }

    @Nullable
    public BufferedImage getColoredImage()
    {
        try
        {
            ResourceLocation resourceLocation = new ResourceLocation(getIconName());

            // Missing texture? Skip
            if (resourceLocation.equals(TextureMap.LOCATION_MISSING_TEXTURE))
            {
                return null;
            }

            // Plan A: Get image from texture data
            BufferedImage image = getFrameTextureData(sprite);
            if (image == null || image.getWidth() == 0)
            {
                // Plan B: Get image from texture's source PNG
                image = getImageResource(sprite);
            }
            if (image == null || image.getWidth() == 0)
            {
                return null;
            }
            return applyColor(image);
        }
        catch (Throwable e1)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("ColoredSprite: Error getting image for " + getIconName() + ": " + LogFormatter.toString(e1));
            }
            return null;
        }
    }

    /**
     * Primary means of getting block texture:  FrameTextureData
     */
    private BufferedImage getFrameTextureData(TextureAtlasSprite tas)
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
            logger.error(String.format("ColoredSprite: Unable to use frame data for %s: %s", tas.getIconName(), t.getMessage()));
        }
        return null;
    }

    /**
     * Secondary means of getting block texture:  derived ResourceLocation
     */
    private BufferedImage getImageResource(TextureAtlasSprite tas)
    {
        try
        {
            ResourceLocation iconNameLoc = new ResourceLocation(tas.getIconName());
            ResourceLocation fileLoc = new ResourceLocation(iconNameLoc.getResourceDomain(), "textures/" + iconNameLoc.getResourcePath() + ".png");
            return TextureCache.resolveImage(fileLoc);
        }
        catch (Throwable t)
        {
            logger.error(String.format("ColoredSprite: Unable to use texture file for %s: %s", tas.getIconName(), t.getMessage()));
        }
        return null;
    }

    /**
     * TODO: Tint?
     */
    private BufferedImage applyColor(BufferedImage original)
    {
        return original;
    }

}
