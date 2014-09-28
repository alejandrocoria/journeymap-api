package net.techbrew.journeymap.ui.minimap;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.ui.config.KeyedEnum;

/**
 * Position of minimap on screen
 */
public enum Position implements KeyedEnum
{
    TopRight("jm.minimap.position_topright"),
    BottomRight("jm.minimap.position_bottomright"),
    BottomLeft("jm.minimap.position_bottomleft"),
    TopLeft("jm.minimap.position_topleft"),
    Center("jm.minimap.position_center");

    public final String key;

    Position(String key)
    {
        this.key = key;
    }

    public static Position getPreferred()
    {
        final MiniMapProperties miniMapProperties = JourneyMap.getMiniMapProperties();

        Position position = null;
        try
        {
            position = miniMapProperties.position.get();
        }
        catch (IllegalArgumentException e)
        {
            JourneyMap.getLogger().warn("Not a valid minimap position in : " + miniMapProperties.getFile());
        }

        if (position == null)
        {
            position = Position.TopRight;
            miniMapProperties.position.set(position);
            miniMapProperties.save();
        }
        return position;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return Constants.getString(this.key);
    }

    public static Position safeValueOf(String name)
    {
        Position value = null;
        try
        {
            value = Position.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            JourneyMap.getLogger().warn("Not a valid minimap position: " + name);
        }

        if (value == null)
        {
            value = Position.TopRight;
        }
        return value;
    }
}
