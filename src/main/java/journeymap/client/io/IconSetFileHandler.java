/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.io;

import journeymap.common.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.common.log.LogFormatter;
import journeymap.client.ui.option.StringListProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * IconSet file management.
 */
public class IconSetFileHandler
{
    public static final String ASSETS_JOURNEYMAP_ICON_ENTITY = "/assets/journeymap/icon/entity";
    public final static String MOB_ICON_SET_2D = "2D";
    public final static String MOB_ICON_SET_3D = "3D";
    public final static List<String> MOB_ICON_SETS = Arrays.asList(MOB_ICON_SET_2D, MOB_ICON_SET_3D);

    public static void initialize()
    {
        JourneymapClient.getLogger().info("Initializing icon sets...");

        // Mob icons
        for (String setName : MOB_ICON_SETS)
        {
            FileHandler.copyResources(getEntityIconDir(), ASSETS_JOURNEYMAP_ICON_ENTITY, setName, false);
        }
    }

    public static File getEntityIconDir()
    {
        File dir = new File(ForgeHelper.INSTANCE.getClient().mcDataDir, Constants.ENTITY_ICON_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }

    public static ArrayList<String> getEntityIconSetNames()
    {
        return getIconSetNames(getEntityIconDir(), MOB_ICON_SETS);
    }

    public static ArrayList<String> getIconSetNames(File parentDir, List<String> defaultIconSets)
    {
        try
        {
            // Initialize entity iconset folders
            for (String iconSetName : defaultIconSets)
            {
                File iconSetDir = new File(parentDir, iconSetName);
                if (iconSetDir.exists() && !iconSetDir.isDirectory())
                {
                    iconSetDir.delete();
                }
                iconSetDir.mkdirs();
            }
        }
        catch (Throwable t)
        {
            JourneymapClient.getLogger().error("Could not prepare iconset directories for " + parentDir + ": " + LogFormatter.toString(t));
        }

        // Create list of icon set names
        ArrayList<String> names = new ArrayList<String>();
        for (File iconSetDir : parentDir.listFiles())
        {
            if (iconSetDir.isDirectory())
            {
                names.add(iconSetDir.getName());
            }
        }
        Collections.sort(names);

        return names;
    }

    public static class IconSetStringListProvider implements StringListProvider
    {
        @Override
        public List<String> getStrings()
        {
            return IconSetFileHandler.getEntityIconSetNames();
        }

        @Override
        public String getDefaultString()
        {
            return MOB_ICON_SET_2D;
        }
    }

}
