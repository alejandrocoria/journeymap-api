/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.ui.theme.impl.FlatTheme;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Themes that come bundled with JourneyMap.
 * TODO: Auto-find these in resource pack structure
 */
public class ThemePresets
{
    public static String DEFAULT_DIRECTORY = "flat";

    /**
     * The default theme
     */
    public static Theme getDefault()
    {
        return FlatTheme.createOceanMonument();
    }

    /**
     * Gets preset dirs.
     *
     * @return the preset dirs
     */
    public static List<String> getPresetDirs()
    {
        return Collections.singletonList(getDefault().directory);
    }

    /**
     * Gets presets.
     *
     * @return the presets
     */
    public static List<Theme> getPresets()
    {
        return Arrays.asList(
                FlatTheme.createDesertTemple(),
                FlatTheme.EndCity(),
                FlatTheme.createForestMansion(),
                FlatTheme.createNetherFortress(),
                FlatTheme.createOceanMonument(),
                FlatTheme.createPurist(),
                FlatTheme.createStronghold()
        );
    }


}

