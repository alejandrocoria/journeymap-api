/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.io;

import journeymap.client.Constants;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.StringField;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.util.*;

/**
 * IconSet file management.
 */
public class IconSetFileHandler
{
    public static final ResourceLocation ASSETS_JOURNEYMAP_ICON_ENTITY = new ResourceLocation(Journeymap.MOD_ID, "icon/entity");
    public final static String MOB_ICON_SET_DEFAULT = "Default";

    private static final Set<String> modUpdatedSetNames = new HashSet<>();
    private static final Set<ResourceLocation> entityIconLocations = new HashSet<>();

    static
    {
        registerEntityIconDirectory(ASSETS_JOURNEYMAP_ICON_ENTITY);
    }

    public static void initialize()
    {
        // Assume all entityIconLocations registered by now
        modUpdatedSetNames.add(MOB_ICON_SET_DEFAULT);
    }

    public static boolean registerEntityIconDirectory(ResourceLocation resourceLocation)
    {
        boolean valid = addEntityIcons(resourceLocation, MOB_ICON_SET_DEFAULT, false);
        if (valid)
        {
            entityIconLocations.add(resourceLocation);
        }
        return valid;
    }

    public static void ensureEntityIconSet(String setName)
    {
        ensureEntityIconSet(setName, false);
    }

    public static void ensureEntityIconSet(String setName, boolean overwrite)
    {
        if (!modUpdatedSetNames.contains(setName))
        {
            // No need to repeat. Just once per runtime.
            for (ResourceLocation resourceLocation : entityIconLocations)
            {
                addEntityIcons(resourceLocation, setName, overwrite);
            }
            modUpdatedSetNames.add(setName);
        }

        try
        {
            ResourcePackRepository rpr = FMLClientHandler.instance().getClient().getResourcePackRepository();
            for (ResourcePackRepository.Entry entry : rpr.getRepositoryEntries())
            {
                IResourcePack pack = entry.getResourcePack();
                for (String domain : pack.getResourceDomains())
                {
                    ResourceLocation domainEntityIcons = new ResourceLocation(domain, "textures/entity_icons");
                    if (pack.resourceExists(domainEntityIcons))
                    {
                        addEntityIcons(domainEntityIcons, setName, true);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("Can't get entity icon from resource packs: %s", LogFormatter.toString(t)));
        }
    }

    private static boolean addEntityIcons(ResourceLocation resourceLocation, String setName, boolean overwrite)
    {
        boolean result = false;
        try
        {
            result = FileHandler.copyResources(getEntityIconDir(), resourceLocation, setName, overwrite);
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Error adding entity icons: " + t.getMessage(), t);
        }
        Journeymap.getLogger().info(String.format("Added entity icons from %s. Success: %s", resourceLocation, result));
        return result;
    }

    public static File getEntityIconDir()
    {
        File dir = new File(FileHandler.getMinecraftDirectory(), Constants.ENTITY_ICON_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }

    public static ArrayList<String> getEntityIconSetNames()
    {
        return getIconSetNames(getEntityIconDir(), Collections.singletonList(MOB_ICON_SET_DEFAULT));
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
            Journeymap.getLogger().error("Could not prepare iconset directories for " + parentDir + ": " + LogFormatter.toString(t));
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

    public static class IconSetValuesProvider implements StringField.ValuesProvider
    {
        @Override
        public List<String> getStrings()
        {
            if (FMLClientHandler.instance().getClient() != null)
            {
                return IconSetFileHandler.getEntityIconSetNames();
            }
            else
            {
                return Collections.singletonList(MOB_ICON_SET_DEFAULT);
            }
        }

        @Override
        public String getDefaultString()
        {
            return MOB_ICON_SET_DEFAULT;
        }
    }

}
