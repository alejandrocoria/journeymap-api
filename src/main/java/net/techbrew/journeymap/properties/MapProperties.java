package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shared Properties for the various map types.
 */
public abstract class MapProperties extends PropertiesBase implements Comparable<MapProperties>
{
    public final AtomicBoolean showMobs = new AtomicBoolean(true);
    public final AtomicBoolean showAnimals = new AtomicBoolean(true);
    public final AtomicBoolean showVillagers = new AtomicBoolean(true);
    public final AtomicBoolean showPets = new AtomicBoolean(true);
    public final AtomicBoolean showPlayers = new AtomicBoolean(true);
    public final AtomicBoolean showWaypoints = new AtomicBoolean(true);
    public final AtomicBoolean showSelf = new AtomicBoolean(true);
    public final AtomicInteger zoomLevel = new AtomicInteger(0);

    protected MapProperties()
    {
    }

    public abstract AtomicReference<String> getEntityIconSetName();

    @Override
    protected boolean validate()
    {
        boolean saveNeeded = super.validate();

        AtomicReference<String> entityIconSetName = getEntityIconSetName();

        if(entityIconSetName.get()==null || !FileHandler.getMobIconSetNames().contains(entityIconSetName.get()))
        {
            JourneyMap.getLogger().warning(String.format("Entity Icon Set name '%s' is not valid, will use default instead.", entityIconSetName.get()));
            entityIconSetName.set(FileHandler.getMobIconSetNames().get(0));
            saveNeeded = true;
        }

        return saveNeeded;
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

        MapProperties that = (MapProperties) o;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = 31 * showMobs.hashCode();
        result = 31 * result + showAnimals.hashCode();
        result = 31 * result + showVillagers.hashCode();
        result = 31 * result + showPets.hashCode();
        result = 31 * result + showPlayers.hashCode();
        result = 31 * result + showWaypoints.hashCode();
        result = 31 * result + showSelf.hashCode();
        result = 31 * result + getEntityIconSetName().hashCode();
        return result;
    }

    @Override
    public int compareTo(MapProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }
}
