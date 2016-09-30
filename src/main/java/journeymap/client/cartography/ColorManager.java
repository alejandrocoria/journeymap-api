/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import com.google.common.base.Joiner;
import journeymap.client.Constants;
import journeymap.client.forge.helper.ColorHelper;
import journeymap.client.model.BlockMD;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.resources.ResourcePackRepository;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages of block colors derived from the current texture pack.
 *
 * @author mwoodman
 */
public class ColorManager
{
    private volatile ColorPalette currentPalette;
    private String lastResourcePackNames;
    private String lastModNames;

    public static ColorManager instance()
    {
        return Holder.INSTANCE;
    }

    /**
     * Get a list of all resource pack names.
     *
     * @return
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
    public void ensureCurrent()
    {
        try
        {
            if (!Display.isCurrent())
            {
                Journeymap.getLogger().error("ColorManager.ensureCurrent() must be called on main thread!");
            }
        }
        catch (LWJGLException e)
        {
            e.printStackTrace();
            return;
        }

        String currentResourcePackNames = getResourcePackNames();
        String currentModNames = Constants.getModNames();

        boolean resourcePackSame = false;
        boolean modPackSame = false;
        boolean blocksTextureChanged = false;

        if (currentResourcePackNames.equals(lastResourcePackNames) && ColorHelper.INSTANCE.hasCachedIconColors())
        {
            Journeymap.getLogger().debug("Resource Pack(s) unchanged: " + currentResourcePackNames);
            resourcePackSame = true;
        }

        if (currentModNames.equals(lastModNames))
        {
            Journeymap.getLogger().debug("Mod Pack(s) unchanged: " + currentModNames);
            modPackSame = true;
        }

        if (!resourcePackSame || !modPackSame)
        {
            lastResourcePackNames = currentResourcePackNames;
            lastModNames = currentModNames;
            blocksTextureChanged = true;
        }

        Journeymap.getLogger().info("Loading blocks and textures...");

        boolean isMapping = Journeymap.getClient().isMapping();
        if (isMapping)
        {
            Journeymap.getClient().stopMapping();
        }

        // Reload all BlockMDs
        BlockMD.reset();

        // Init colors
        initBlockColors(blocksTextureChanged);

        if (isMapping)
        {
            Journeymap.getClient().startMapping();
        }
    }

    /**
     * Get the current palette.
     *
     * @return
     */
    public ColorPalette getCurrentPalette()
    {
        return currentPalette;
    }

    /**
     * Load color palette.  Needs to be called on the main thread
     * so the texture atlas can be loaded.
     */
    private void initBlockColors(boolean currentPaletteInvalid)
    {
        try
        {
            // Start with existing palette colors and set them on the corresponding BlockMDs
            ColorPalette palette = ColorPalette.getActiveColorPalette();
            boolean standard = true;
            boolean permanent = false;
            if (palette != null)
            {
                standard = palette.isStandard();
                permanent = palette.isPermanent();
                if (currentPaletteInvalid && !permanent)
                {
                    Journeymap.getLogger().info("New color palette will be created");
                    palette = null;
                }
                else
                {
                    try
                    {
                        long start = System.currentTimeMillis();
                        palette.updateColors();
                        long elapsed = System.currentTimeMillis() - start;
                        Journeymap.getLogger().info(String.format("Loaded %d block colors from color palette file in %dms: %s", palette.size(), elapsed, palette.getOrigin()));
                    }
                    catch (Exception e)
                    {
                        Journeymap.getLogger().warn("Could not load existing color palette, new one will be created: " + e);
                        palette = null;
                    }
                }
            }

            // Load textures for the others
            ColorHelper.INSTANCE.resetIconColorCache();
            long start = System.currentTimeMillis();
            int count = 0;
            for (BlockMD blockMD : BlockMD.getAll())
            {
                if (!blockMD.isAir() && blockMD.ensureColor())
                {
                    count++;
                }
            }
            long elapsed = System.currentTimeMillis() - start;

            Journeymap.getLogger().info("Cached colors from TextureAtlasSprites: " + ColorHelper.INSTANCE.cachedIconColors());

            if (count > 0 || palette == null)
            {
                Journeymap.getLogger().info(String.format("Initialized %s block colors from mods and resource packs in %sms", count, elapsed));
                this.currentPalette = ColorPalette.create(standard, permanent);
                Journeymap.getLogger().info(String.format("Updated color palette file: %s", this.currentPalette.getOrigin()));
            }
            else
            {
                this.currentPalette = palette;
                Journeymap.getLogger().info(String.format("Color palette was sufficient: %s", this.currentPalette.getOrigin()));
            }

            // Remap around player
            MapPlayerTask.forceNearbyRemap();
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("ColorManager.initBlockColors() encountered an unexpected error: " + LogFormatter.toPartialString(t));
        }
    }

    /**
     * Singleton
     */
    private static class Holder
    {
        private static final ColorManager INSTANCE = new ColorManager();
    }

}
