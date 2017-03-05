/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.option;

import journeymap.client.Constants;
import journeymap.client.ui.component.ListPropertyButton;
import journeymap.common.Journeymap;
import journeymap.common.properties.config.StringField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Encapsulates the variations of location formats.
 */
public class LocationFormat
{
    private static String[] locationFormatIds = new String[]{"xzyv", "xyvz", "xzy", "xyz", "xz"};
    private HashMap<String, LocationFormatKeys> idToFormat = new HashMap<String, LocationFormatKeys>();

    /**
     * Instantiates a new Location format.
     */
    public LocationFormat()
    {
        for (String id : locationFormatIds)
        {
            idToFormat.put(id, new LocationFormatKeys(id));
        }
    }

    /**
     * Gets format keys.
     *
     * @param id the id
     * @return the format keys
     */
    public LocationFormatKeys getFormatKeys(String id)
    {
        LocationFormatKeys locationLocationFormatKeys = idToFormat.get(id);
        if (locationLocationFormatKeys == null)
        {
            Journeymap.getLogger().warn("Invalid location format id: " + id);
            locationLocationFormatKeys = idToFormat.get(locationFormatIds[0]);
        }

        return locationLocationFormatKeys;
    }

    /**
     * Gets label.
     *
     * @param id the id
     * @return the label
     */
    public String getLabel(String id)
    {
        return Constants.getString(getFormatKeys(id).label_key);
    }

    /**
     * The type Id provider.
     */
    public static class IdProvider implements StringField.ValuesProvider
    {
        /**
         * Instantiates a new Id provider.
         */
        public IdProvider()
        {
        }

        @Override
        public List<String> getStrings()
        {
            return Arrays.asList(locationFormatIds);
        }

        @Override
        public String getDefaultString()
        {
            return locationFormatIds[0];
        }
    }

    /**
     * The type Location format keys.
     */
    public static class LocationFormatKeys
    {
        /**
         * The Id.
         */
        final String id;
        /**
         * The Label key.
         */
        final String label_key;
        /**
         * The Verbose key.
         */
        final String verbose_key;
        /**
         * The Plain key.
         */
        final String plain_key;

        /**
         * Instantiates a new Location format keys.
         *
         * @param id the id
         */
        LocationFormatKeys(String id)
        {
            this.id = id;
            this.label_key = String.format("jm.common.location_%s_label", id);
            this.verbose_key = String.format("jm.common.location_%s_verbose", id);
            this.plain_key = String.format("jm.common.location_%s_plain", id);
        }

        /**
         * Format string.
         *
         * @param verbose the verbose
         * @param x       the x
         * @param z       the z
         * @param y       the y
         * @param vslice  the vslice
         * @return the string
         */
        public String format(boolean verbose, int x, int z, int y, int vslice)
        {
            if (verbose)
            {
                return Constants.getString(verbose_key, x, z, y, vslice);
            }
            else
            {
                return Constants.getString(plain_key, x, z, y, vslice);
            }
        }
    }

    /**
     * The type Button.
     */
    public static class Button extends ListPropertyButton<String>
    {
        /**
         * The Location format.
         */
        LocationFormat locationFormat;

        /**
         * Instantiates a new Button.
         *
         * @param valueHolder the value holder
         */
        public Button(StringField valueHolder)
        {
            super(Arrays.asList(locationFormatIds), Constants.getString("jm.common.location_format"), valueHolder);
            if (locationFormat == null)
            {
                locationFormat = new LocationFormat();
            }
        }

        @Override
        public String getFormattedLabel(String id)
        {
            if (locationFormat == null)
            {
                locationFormat = new LocationFormat();
            }
            return String.format(labelPattern, baseLabel, glyph, locationFormat.getLabel(id));
        }

        /**
         * Gets label.
         *
         * @param id the id
         * @return the label
         */
        public String getLabel(String id)
        {
            return locationFormat.getLabel(id);
        }
    }
}
