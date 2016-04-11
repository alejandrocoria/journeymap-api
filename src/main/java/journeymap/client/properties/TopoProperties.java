/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import com.google.common.base.Objects;
import journeymap.client.Constants;
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
    // Headers to output before file
    private static final String[] HEADERS = {
            "// " + Constants.getString("jm.config.file_header_1"),
            "// " + Constants.getString("jm.config.file_header_5", "http://journeymap.info/Topographic")
    };

    private static final int MAX_COLORS = 128;

    private static final String DEFAULT_LAND_CONTOUR_COLOR = "#3f250b";
    private static final String DEFAULT_WATER_CONTOUR_COLOR = "#222266";

    private static final String DEFAULT_LAND_COLORS = "#000800,#000f00,#001700,#001f00,#002700,#002e00,#002e00,#114016,#23512c,#356343,#467559,#57866f,#699885,#70a18c,#76a992,#7daf96,#83b59b,#94bba1,#a5c0a7,#bcc4ad,#d3c9b3,#d3c1ac,#d4b8a4,#d4bcad,#d4c0b5,#d5c9c2,#d6d1ce,#dad7d6,#deddde,#e6e6e6,#eeeeee,#f2f3f2";

    private static final String DEFAULT_WATER_COLORS = "#000040,#02024e,#03035d,#05056b,#070779,#080887,#0a0a96,#0b0ba4,#1a1aaa,#2a2aaf,#3939b5,#4848bb,#5757c0,#6767c6,#7676cc,#8585d2,#9494d7,#a4a4dd,#b3b3e3,#c2c2e8,#d1d1ee,#d7d7f0,#ddddf2,#e2e2f4,#e8e8f6,#eeeef9,#f4f4fb,#f9f9ff,#f9f9ff,#f9f9ff,#f9f9ff,#f9f9ff";

    public final BooleanField showContour = new BooleanField(Category.Hidden, true).set(true);
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
        waterContourColor = null;
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

    @Override
    public String[] getHeaders()
    {
        return HEADERS;
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
