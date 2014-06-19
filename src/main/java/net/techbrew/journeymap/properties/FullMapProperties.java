package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Properties for the full map in-game.
 */
public class FullMapProperties extends InGameMapProperties
{
    protected transient static final int CURRENT_REVISION = 3;
    protected transient final String name = "fullmap";
    protected AtomicInteger revision = new AtomicInteger(CURRENT_REVISION);

    public final AtomicBoolean showCaves = new AtomicBoolean(true);
    public final AtomicBoolean showGrid = new AtomicBoolean(true);
    public final AtomicReference<String> entityIconSetName = new AtomicReference<String>("3D");

    public FullMapProperties()
    {
    }

    @Override
    public AtomicReference<String> getEntityIconSetName()
    {
        return entityIconSetName;
    }

    @Override
    public String getName()
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
        return revision.get();
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
        result = 31 * result + name.hashCode();
        result = 31 * result + revision.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "FullMapProperties: " +
                "revision=" + revision +
                ", showCaves=" + showCaves +
                ", showMobs=" + showMobs +
                ", showAnimals=" + showAnimals +
                ", showVillagers=" + showVillagers +
                ", showPets=" + showPets +
                ", showPlayers=" + showPlayers +
                ", showWaypoints=" + showWaypoints +
                ", showGrid=" + showGrid +
                ", forceUnicode=" + forceUnicode +
                ", fontSmall=" + fontSmall +
                ", entityIconSetName=" + entityIconSetName;
    }
}
