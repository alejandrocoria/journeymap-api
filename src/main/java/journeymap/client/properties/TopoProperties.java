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

    private static final String DEFAULT_LAND_CONTOUR_COLOR = "#3F250B";

    private static final String DEFAULT_WATER_CONTOUR_COLOR = "#000066";

    private static final String DEFAULT_LAND_COLORS = "#000800,#000f00,#001700,#001f00,#002700,#002e00,#003600,#003e00," +
            "#004600,#004d00,#005500,#036103,#066e06,#097a09,#0c860c,#0f930f,#139f13,#16ac16,#19b819,#1cc41c,#1fd11f," +
            "#22dd22,#36e036,#4ae34a,#5ee65e,#72e972,#86ec86,#9bf09b,#aff3af,#c3f6c3,#d7f9d7,#ebfceb";

    private static final String DEFAULT_WATER_COLORS = "#000040,#02024e,#03035d,#05056b,#070779,#080887,#0a0a96,#0b0ba4," +
            "#1a1aaa,#2a2aaf,#3939b5,#4848bb,#5757c0,#6767c6,#7676cc,#8585d2,#9494d7,#a4a4dd,#b3b3e3,#c2c2e8,#d1d1ee," +
            "#d7d7f0,#ddddf2,#e2e2f4,#e8e8f6,#eeeef9,#f4f4fb,#f9f9ff,#f9f9ff,#f9f9ff,#f9f9ff,#f9f9ff";

    public final BooleanField showContour = new BooleanField(Category.Hidden, true);
    public final StringField landContour = new StringField(Category.Hidden, "").set(DEFAULT_LAND_CONTOUR_COLOR);
    public final StringField waterContour = new StringField(Category.Hidden, "").set(DEFAULT_WATER_CONTOUR_COLOR);
    public final StringField land = new StringField(Category.Hidden, "").multiline(true).set(DEFAULT_LAND_COLORS);
    public final StringField water = new StringField(Category.Hidden, "").multiline(true).set(DEFAULT_WATER_COLORS);

    private transient Integer[] landColors;
    private transient Integer[] waterColors;
    private transient Integer landContourColor;
    private transient Integer waterContourColor;

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

    public Integer getLandContourColor()
    {
        if (!showContour.get())
        {
            return null;
        }
        if (landContourColor == null)
        {
            landContourColor = RGB.hexToInt(landContour.get());
        }
        return landContourColor;
    }

    public Integer getWaterContourColor()
    {
        if (!showContour.get())
        {
            return null;
        }
        if (waterContourColor == null)
        {
            waterContourColor = RGB.hexToInt(waterContour.get());
        }
        return waterContourColor;
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
        landContourColor = null;
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
