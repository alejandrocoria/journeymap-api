package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Properties for in-game waypoint management and display.
 */
public class WaypointProperties extends PropertiesBase implements Comparable<WaypointProperties>
{
    protected transient static final int CODE_REVISION = 3;
    protected transient final String name = "waypoint";

    public final AtomicBoolean managerEnabled = new AtomicBoolean(true);
    public final AtomicBoolean beaconEnabled = new AtomicBoolean(true);
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
    public String getName()
    {
        return name;
    }

    @Override
    public int getCodeRevision()
    {
        return CODE_REVISION;
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
        result = 31 * result + fileRevision;
        result = 31 * result + managerEnabled.hashCode();
        result = 31 * result + beaconEnabled.hashCode();
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
    public int compareTo(WaypointProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }

    @Override
    public String toString()
    {
        return "WaypointProperties: " +
                "fileRevision=" + fileRevision +
                ", managerEnabled=" + managerEnabled +
                ", beaconEnabled=" + beaconEnabled +
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
