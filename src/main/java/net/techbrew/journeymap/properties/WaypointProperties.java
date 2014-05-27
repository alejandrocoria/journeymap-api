package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Properties for in-game waypoint management and display.
 */
public class WaypointProperties extends PropertiesBase implements Comparable<WaypointProperties>
{
    protected transient static final int CURRENT_REVISION = 1;
    protected final String name = "waypoint";
    protected int revision = CURRENT_REVISION;

    public final AtomicBoolean enabled = new AtomicBoolean(true);
    public final AtomicBoolean showTexture = new AtomicBoolean(true);
    public final AtomicBoolean showStaticBeam = new AtomicBoolean(true);
    public final AtomicBoolean showRotatingBeam = new AtomicBoolean(true);
    public final AtomicBoolean showName = new AtomicBoolean(true);
    public final AtomicBoolean showDistance = new AtomicBoolean(true);
    public final AtomicBoolean autoHideLabel = new AtomicBoolean(true);
    public final AtomicBoolean boldLabel = new AtomicBoolean(false);
    public final AtomicBoolean forceUnicode = new AtomicBoolean(false);
    public final AtomicBoolean fontSmall = new AtomicBoolean(true);
    public final AtomicBoolean textureSmall = new AtomicBoolean(true);
    public final AtomicInteger maxDistance = new AtomicInteger(0);

    @Override
    protected String getName()
    {
        return name;
    }

    @Override
    public int getCurrentRevision()
    {
        return CURRENT_REVISION;
    }

    @Override
    public int getRevision()
    {
        return revision;
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

        WaypointProperties that = (WaypointProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + revision;
        result = 31 * result + enabled.hashCode();
        result = 31 * result + showTexture.hashCode();
        result = 31 * result + showStaticBeam.hashCode();
        result = 31 * result + showRotatingBeam.hashCode();
        result = 31 * result + showName.hashCode();
        result = 31 * result + showDistance.hashCode();
        result = 31 * result + autoHideLabel.hashCode();
        result = 31 * result + boldLabel.hashCode();
        result = 31 * result + forceUnicode.hashCode();
        result = 31 * result + fontSmall.hashCode();
        result = 31 * result + textureSmall.hashCode();
        result = 31 * result + maxDistance.hashCode();
        return result;
    }

    @Override
    public int compareTo(WaypointProperties o)
    {
        return Integer.compare(this.hashCode(), o.hashCode());
    }

    @Override
    public String toString()
    {
        return "WaypointProperties: " +
                "revision=" + revision +
                ", enabled=" + enabled +
                ", showTexture=" + showTexture +
                ", showStaticBeam=" + showStaticBeam +
                ", showRotatingBeam=" + showRotatingBeam +
                ", showName=" + showName +
                ", showDistance=" + showDistance +
                ", autoHideLabel=" + autoHideLabel +
                ", boldLabel=" + boldLabel +
                ", forceUnicode=" + forceUnicode +
                ", fontSmall=" + fontSmall +
                ", textureSmall=" + textureSmall +
                ", maxDistance=" + maxDistance;
    }
}
