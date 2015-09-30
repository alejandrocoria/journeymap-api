/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IColorHelper;
import journeymap.client.forge.helper.IForgeHelper;
import journeymap.client.model.BlockMD;
import journeymap.client.task.multi.InitBlockColorsTask;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;

import java.util.Map;

/**
 * Manages of block colors derived from the current texture pack.
 *
 * @author mwoodman
 */
public class ColorManager
{
    private final IForgeHelper forgeHelper = ForgeHelper.INSTANCE;
    private volatile IColorHelper colorHelper = forgeHelper.getColorHelper();
    private volatile ColorPalette currentPalette;
    private String lastResourcePackNames;
    private String lastModNames;

    public static ColorManager instance()
    {
        return Holder.INSTANCE;
    }

    /**
     * Ensure the colors in the cache match the current resource packs.
     */
    public void ensureCurrent()
    {
        String currentResourcePackNames = Constants.getResourcePackNames();
        String currentModNames = Constants.getModNames();

        boolean resourcePackSame = false;
        boolean modPackSame = false;

        if (currentResourcePackNames.equals(lastResourcePackNames) && colorHelper != null)
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

            // Reload all BlockMDs
            BlockMD.reset();

            // Ensure blocks texture initialized
            colorHelper.initBlocksTexture();

            if (JourneymapClient.getInstance().isMapping())
            {
                // Init colors off thread
                JourneymapClient.getInstance().toggleTask(InitBlockColorsTask.Manager.class, true, Boolean.TRUE);
            }
            else
            {
                // Do it now
                ColorManager.instance().initBlockColors();
            }
        }

        if (!colorHelper.hasBlocksTexture())
        {
            // Ensure blocks texture initialized
            colorHelper.initBlocksTexture();
        }
    }

    /**
     * Get the current palette.
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
    public void initBlockColors()
    {
        // Start with existing palette colors and set them on the corresponding BlockMDs
        ColorPalette palette = ColorPalette.getActiveColorPalette();
        boolean standard = true;
        boolean permanent = false;
        if (palette != null)
        {
            standard = palette.isStandard();
            permanent = palette.isPermanent();
            try
            {
                long start = System.currentTimeMillis();
                for (Map.Entry<BlockMD, Integer> entry : palette.getBasicColorMap().entrySet())
                {
                    entry.getKey().setColor(entry.getValue());
                }
                long elapsed = System.currentTimeMillis() - start;
                Journeymap.getLogger().info(String.format("Existing color palette loaded %d colors in %dms from file: %s", palette.size(), elapsed, palette.getOrigin()));
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn("Could not load color palette: " + e);
            }
        }

        // Load textures for the others
        long start = System.currentTimeMillis();
        int count = 0;
        for (BlockMD blockMD : BlockMD.getAll())
        {
            if (blockMD.ensureColor())
            {
                count++;
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        if (count > 0)
        {
            Journeymap.getLogger().info(String.format("Initialized %s block colors from resource packs in %sms", count, elapsed));
            this.currentPalette = ColorPalette.create(standard, permanent);
        }
        else
        {
            Journeymap.getLogger().info("No other block colors needed to be initialized.");
            this.currentPalette = palette;
        }

        // Remap around player
        MapPlayerTask.forceNearbyRemap();
    }

    /**
     * Singleton
     */
    private static class Holder
    {
        private static final ColorManager INSTANCE = new ColorManager();
    }

}
