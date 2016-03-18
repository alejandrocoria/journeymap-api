/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.MapType;
import journeymap.common.properties.config.StringField;

/**
 * Properties for the full map in-game.
 */
public class FullMapProperties extends InGameMapProperties
{
    public FullMapProperties()
    {
    }

    @Override
    public void newFileInit()
    {
        if (ForgeHelper.INSTANCE.getFontRenderer().getUnicodeFlag())
        {
            super.fontScale.set(2);
        }
    }

    @Override
    public String getName()
    {
        return "fullmap";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        FullMapProperties that = (FullMapProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + showGrid.hashCode();
        result = 31 * result + showCaves.hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return super.toStringHelper(this)
                .add("preferredMapType", preferredMapType)
                .add("showGrid", showGrid)
                .toString();
    }
}
