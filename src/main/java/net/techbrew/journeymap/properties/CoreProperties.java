package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Properties for basic mod configuration.
 */
public class CoreProperties extends PropertiesBase implements Comparable<CoreProperties>
{
    protected transient static final int CURRENT_REVISION = 2;
    protected transient final String name = "core";
    protected final AtomicInteger revision = new AtomicInteger(CURRENT_REVISION);
    public final AtomicReference<String> logLevel = new AtomicReference<String>("INFO");
    public final AtomicInteger chunkOffset = new AtomicInteger(8);
    public final AtomicInteger entityPoll = new AtomicInteger(1800);
    public final AtomicInteger playerPoll = new AtomicInteger(1900);
    public final AtomicInteger chunkPoll = new AtomicInteger(2000);
    public final AtomicInteger cacheAnimalsData = new AtomicInteger(3100);
    public final AtomicInteger maxAnimalsData = new AtomicInteger(32);
    public final AtomicInteger cacheMobsData = new AtomicInteger(3000);
    public final AtomicInteger maxMobsData = new AtomicInteger(32);
    public final AtomicInteger cachePlayerData = new AtomicInteger(1000);
    public final AtomicInteger cachePlayersData = new AtomicInteger(2000);
    public final AtomicInteger maxPlayersData = new AtomicInteger(32);
    public final AtomicInteger cacheVillagersData = new AtomicInteger(2200);
    public final AtomicInteger maxVillagersData = new AtomicInteger(32);
    public final AtomicBoolean caveLighting = new AtomicBoolean(true);
    public final AtomicBoolean announceMod = new AtomicBoolean(true);
    public final AtomicBoolean checkUpdates = new AtomicBoolean(true);
    public final AtomicBoolean caveGreySurface = new AtomicBoolean(false);
    public final AtomicBoolean caveIgnoreGlass = new AtomicBoolean(true);

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
    public int compareTo(CoreProperties o)
    {
        return Integer.compare(this.hashCode(), o.hashCode());
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

        CoreProperties that = (CoreProperties) o;
        return 0 == that.compareTo(this);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + revision.hashCode();
        result = 31 * result + logLevel.hashCode();
        result = 31 * result + chunkOffset.hashCode();
        result = 31 * result + entityPoll.hashCode();
        result = 31 * result + playerPoll.hashCode();
        result = 31 * result + chunkPoll.hashCode();
        result = 31 * result + cacheAnimalsData.hashCode();
        result = 31 * result + maxAnimalsData.hashCode();
        result = 31 * result + cacheMobsData.hashCode();
        result = 31 * result + maxMobsData.hashCode();
        result = 31 * result + cachePlayerData.hashCode();
        result = 31 * result + cachePlayersData.hashCode();
        result = 31 * result + maxPlayersData.hashCode();
        result = 31 * result + cacheVillagersData.hashCode();
        result = 31 * result + maxVillagersData.hashCode();
        result = 31 * result + caveLighting.hashCode();
        result = 31 * result + announceMod.hashCode();
        result = 31 * result + checkUpdates.hashCode();
        result = 31 * result + caveGreySurface.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "CoreProperties: " +
                "revision=" + revision +
                ", logLevel=" + logLevel +
                ", chunkOffset=" + chunkOffset +
                ", entityPoll=" + entityPoll +
                ", playerPoll=" + playerPoll +
                ", chunkPoll=" + chunkPoll +
                ", cacheAnimalsData=" + cacheAnimalsData +
                ", maxAnimalsData=" + maxAnimalsData +
                ", cacheMobsData=" + cacheMobsData +
                ", maxMobsData=" + maxMobsData +
                ", cachePlayerData=" + cachePlayerData +
                ", cachePlayersData=" + cachePlayersData +
                ", maxPlayersData=" + maxPlayersData +
                ", cacheVillagersData=" + cacheVillagersData +
                ", maxVillagersData=" + maxVillagersData +
                ", caveLighting=" + caveLighting +
                ", caveGreySurface=" + caveGreySurface +
                ", announceMod=" + announceMod +
                ", checkUpdates=" + checkUpdates;
    }
}
