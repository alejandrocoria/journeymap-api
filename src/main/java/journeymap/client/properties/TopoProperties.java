/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import com.google.common.base.Objects;
import journeymap.client.cartography.RGB;
import journeymap.common.properties.Category;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.StringField;

/**
 * Properties for topo map configuration.
 */
public class TopoProperties extends ClientPropertiesBase implements Comparable<TopoProperties>
{
    private static final int MAX_COLORS = 128;

    private static final String DEFAULT_CONTOUR_COLOR = "#222222";

    private static final String DEFAULT_LAND_COLORS =
            "#010101, #0a465a, #14505a, #1e5a64, #286464, #326e64, #468264, #508c64, " +
                    "#5a9664, #64a76b, #acd0a5, #94bf8b, #a8c68f, #bdcc96, #d1d7ab, #e1e4b5, " +
                    "#efebc0, #e8e1b6, #ded6a3, #d3ca9d, #cab982, #c3a76b, #b9985a, #aa8753, " +
                    "#ac9a7c, #baae9a, #cac3b8, #e0c3d8, #e0ded8, #e0f4d8, #f5f4f2, #ffffff";

    private static final String DEFAULT_WATER_COLORS =
            "#0b0b93, #0d1499, #13239b, #1b36ad, #2353c3, #1e73d5, #2e84df, #4b9be3, " +
                    "#54a3e3, #63abeb, #73b3eb, #7bbbeb, #8bc9eb, #91cfeb, #9edaee, #b3ebf3";

    public final BooleanField showContour = new BooleanField(Category.Hidden, true).set(true);
    public final StringField contour = new StringField(Category.Hidden, "").set(DEFAULT_CONTOUR_COLOR);
    public final StringField land = new StringField(Category.Hidden, "").multiline(true).set(DEFAULT_LAND_COLORS);
    public final StringField water = new StringField(Category.Hidden, "").multiline(true).set(DEFAULT_WATER_COLORS);

    private transient Integer[] landColors;
    private transient Integer[] waterColors;
    private transient Integer contourColor;

    public TopoProperties()
    {
    }

    @Override
    public String getName()
    {
        return "topo";
    }

    public Integer[] getLandColors()
    {
        if (landColors == null)
        {
            landColors = getColors("land", land.get());
            if (landColors == null || landColors.length == 0)
            {
                error("TopoProperties reverting to default land colors");
                landColors = getColors("land", DEFAULT_LAND_COLORS);
            }
        }

        return landColors;
    }

    public Integer[] getWaterColors()
    {
        if (waterColors == null)
        {
            waterColors = getColors("water", water.get());
            if (waterColors == null || waterColors.length == 0)
            {
                error("TopoProperties reverting to default water colors");
                waterColors = getColors("water", DEFAULT_WATER_COLORS);
            }
        }

        return waterColors;
    }

    public Integer getContourColor()
    {
        if (!showContour.get())
        {
            return null;
        }
        if (contourColor == null)
        {
            contourColor = RGB.hexToInt(contour.get());
        }
        return contourColor;
    }

    private Integer[] getColors(String name, String colorString)
    {
        String[] colors = colorString.split(",");
        int size = Math.min(MAX_COLORS, colors.length);
        if (size == 0)
        {
            error(String.format("TopoProperties bad value for %s: %s", name, colorString));
            return null;
        }
        else if (size > MAX_COLORS)
        {
            warn(String.format("TopoProperties will ignore more than %s colors for %s. Found: %s", MAX_COLORS, name, size));
        }

        Integer[] colorInts = new Integer[size];
        for (int i = 0; i < colors.length; i++)
        {
            colorInts[i] = RGB.hexToInt(colors[i].trim());
        }
        return colorInts;
    }

    @Override
    public int compareTo(TopoProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }

    @Override
    public <T extends PropertiesBase> void updateFrom(T otherInstance)
    {
        super.updateFrom(otherInstance);
    }

    @Override
    protected void postLoad(boolean isNew)
    {
        super.postLoad(isNew);
        landColors = null;
        waterColors = null;
        contourColor = null;
    }

    @Override
    public boolean isValid(boolean fix)
    {
        boolean valid = super.isValid(fix);
        valid = isValid(water.get(), fix) && valid;
        valid = isValid(land.get(), fix) && valid;
        return valid;
    }

    @Override
    protected void preSave()
    {
        super.preSave();
    }

    /**
     * Check validity of colors
     *
     * @param colorString
     * @param fix
     * @return
     */
    private boolean isValid(String colorString, boolean fix)
    {
        boolean valid = true;

        String[] colors = colorString.split(",");
        for (int i = 0; i < colors.length; i++)
        {
            String colorStr = colors[i];
            try
            {
                colorStr = RGB.toHexString(Integer.parseInt(colorStr));
            }
            catch (Exception e)
            {
                colorStr = "0x000000";
            }

            if (!Objects.equal(colorStr, colors[i]))
            {
                if (fix)
                {
                    colors[i] = colorStr;
                }
                else
                {
                    valid = false;
                }
            }
        }
        return valid;
    }
}
