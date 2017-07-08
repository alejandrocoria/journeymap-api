/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography.color;

import com.google.common.base.Joiner;
import journeymap.client.Constants;
import journeymap.client.model.BlockMD;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.resources.ResourcePackRepository;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages of block colors derived from the current texture pack.
 *
 * @author techbrew
 */
@ParametersAreNonnullByDefault
public enum ColorManager
{
    INSTANCE;

    private Logger logger = Journeymap.getLogger();
    private volatile ColorPalette currentPalette;
    private String lastResourcePackNames;
    private String lastModNames;
    private double lastPaletteVersion;
    private HashMap<String, float[]> iconColorCache = new HashMap<>();

    /**
     * Instance color manager.
     *
     * @return the color manager
     */
    public void reset()
    {
        lastResourcePackNames = null;
        lastModNames = null;
        lastPaletteVersion = 0;
        currentPalette = null;
        iconColorCache.clear();
    }

    /**
     * Get a list of all resource pack names.
     *
     * @return resource pack names
     */
    public static String getResourcePackNames()
    {
        List<ResourcePackRepository.Entry> entries = Constants.getResourcePacks();

        String packs;
        if (entries.isEmpty())
        {
            packs = Constants.RESOURCE_PACKS_DEFAULT;
        }
        else
        {
            ArrayList<String> entryStrings = new ArrayList<String>(entries.size());
            for (ResourcePackRepository.Entry entry : entries)
            {
                entryStrings.add(entry.toString());
            }
            Collections.sort(entryStrings);
            packs = Joiner.on(", ").join(entryStrings);
        }
        return packs;
    }

    /**
     * Ensure the colors in the cache match the current resource packs.
     * Must be called on main Minecraft thread in case the blocks texture
     * is stiched.
     */
    public void ensureCurrent(boolean forceReset)
    {
        try
        {
            if (!Display.isCurrent())
            {
                logger.error("ColorManager.ensureCurrent() must be called on main thread!");
            }
        }
        catch (LWJGLException e)
        {
            e.printStackTrace();
            return;
        }

        String currentResourcePackNames = getResourcePackNames();
        String currentModNames = Constants.getModNames();
        double currentPaletteVersion = (currentPalette == null) ? 0 : currentPalette.getVersion();

        if (currentPalette != null && !forceReset)
        {
            if (!currentResourcePackNames.equals(lastResourcePackNames) && !iconColorCache.isEmpty())
            {
                logger.debug("Resource Pack(s) changed: " + currentResourcePackNames);
                forceReset = true;
            }

            if (!currentModNames.equals(lastModNames))
            {
                logger.debug("Mod Pack(s) changed: " + currentModNames);
                forceReset = true;
            }

            if (currentPaletteVersion != lastPaletteVersion)
            {
                logger.debug("Color Palette version changed: " + currentPaletteVersion);
                forceReset = true;
            }
        }

        if (forceReset || iconColorCache.isEmpty())
        {
            logger.debug("Building color palette...");

            // Init colors
            initBlockColors(forceReset);
        }

        // Update latest values
        lastModNames = currentModNames;
        lastResourcePackNames = currentResourcePackNames;
        lastPaletteVersion = currentPalette == null ? 0 : currentPalette.getVersion();
    }

    /**
     * Get the current palette.
     *
     * @return current palette
     */
    public ColorPalette getCurrentPalette()
    {
        return currentPalette;
    }

//    /**
//     * Applies color from palette to block.
//     * @param blockMD
//     * @param createIfMissing
//     * @return true if palette contained a color or was able to create one
//     */
//    public boolean applyColor(BlockMD blockMD, boolean createIfMissing)
//    {
//        return currentPalette.applyColor(blockMD, createIfMissing);
//    }

    /**
     * Load color palette.  Needs to be called on the main thread
     * so the texture atlas can be loaded.
     */
    private void initBlockColors(boolean forceReset)
    {
        try
        {
            long start = System.currentTimeMillis();
            ColorPalette palette = ColorPalette.getActiveColorPalette();
            Collection<BlockMD> blockMDs;

            if (Journeymap.getClient().isMapping())
            {
                blockMDs = BlockMD.getAllValid();
            }
            else
            {
                blockMDs = BlockMD.getAllMinecraft();
            }

            if (forceReset || palette == null)
            {
                logger.debug("Color palette update required.");
                iconColorCache.clear();
                blockMDs.forEach(BlockMD::clearColor);
            }

            // Check current palette state
            boolean standard = true;
            boolean permanent = false;
            if (palette != null)
            {
                standard = palette.isStandard();
                permanent = palette.isPermanent();

                if (permanent && forceReset)
                {
                    logger.debug("Applying permanent palette colors before updating");
                }

                if (permanent || !forceReset)
                {
                    try
                    {
                        int count = palette.applyColors(blockMDs, true);
                        logger.debug(String.format("Loaded %d block colors from %s", count, palette.getOrigin()));
                    }
                    catch (Exception e)
                    {
                        logger.warn(String.format("Could not load block colors from %s: %s", palette.getOrigin(), e));
                    }
                }
            }

            if (forceReset || palette == null)
            {
                palette = ColorPalette.create(standard, permanent);
            }

            this.currentPalette = palette;

            // Ensure all BlockMDs have a color
            for (BlockMD blockMD : blockMDs)
            {
                if (!blockMD.hasColor())
                {
                    blockMD.getTextureColor();
                    currentPalette.applyColor(blockMD, true);
                }

                if (!blockMD.hasColor())
                {
                    logger.warn("Could not derive color for " + blockMD.getBlockState());
                }
            }

            if (currentPalette.isDirty())
            {
                long elapsed = System.currentTimeMillis() - start;
                currentPalette.writeToFile();
                logger.info(String.format("Updated color palette for %s blockstates in %sms: %s", this.currentPalette.size(), elapsed, this.currentPalette.getOrigin()));
            }
            else
            {
                long elapsed = System.currentTimeMillis() - start;
                logger.info(String.format("Loaded color palette for %s blockstates in %sms", this.currentPalette.size(), elapsed));
            }

            // Remap around player
            MapPlayerTask.forceNearbyRemap();
        }
        catch (Throwable t)
        {
            logger.error("ColorManager.initBlockColors() encountered an unexpected error: " + LogFormatter.toPartialString(t));
        }
    }



    /**
     * Gets the average rgba of a collection of sprites, backed by a cache.
     *
     * @param sprites spritres
     * @return average rgba
     */
    @Nullable
    public float[] getAverageColor(Collection<ColoredSprite> sprites)
    {
        if (sprites == null || sprites.isEmpty())
        {
            return null;
        }

        // Collate name
        List<String> names = sprites.stream().map(ColoredSprite::getIconName).collect(Collectors.toList());
        Collections.sort(names);
        String name = Joiner.on(",").join(names);

        float[] rgba;
        if (iconColorCache.containsKey(name))
        {
            rgba = iconColorCache.get(name);
        }
        else
        {
            rgba = calculateAverageColor(sprites);
            if (rgba != null)
            {
                iconColorCache.put(name, rgba);
                if (logger.isDebugEnabled())
                {
                    logger.debug(String.format("Cached color %s for %s", RGB.toHexString(RGB.toInteger(rgba)), name));
                }
            }
        }
        return rgba;
    }

    /**
     * Calculates average rgba of sprites
     *
     * @param sprites
     * @return
     */
    private float[] calculateAverageColor(Collection<ColoredSprite> sprites)
    {
        List<BufferedImage> images = new ArrayList<>(sprites.size());
        for (ColoredSprite coloredSprite : sprites)
        {
            BufferedImage img = coloredSprite.getColoredImage();
            if (img != null)
            {
                images.add(img);
            }
        }

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

}
