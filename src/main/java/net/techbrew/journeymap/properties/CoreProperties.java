package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Properties for basic mod configuration.
 */
public class CoreProperties extends PropertiesBase implements Comparable<CoreProperties>
{
    protected transient static final int CURRENT_REVISION = 1;
    protected final String name = "core";
    protected final AtomicInteger revision = new AtomicInteger(CURRENT_REVISION);
    public final AtomicReference<String> logLevel = new AtomicReference<String>("INFO");
    public final AtomicInteger chunkOffset = new AtomicInteger(5);
    public final AtomicInteger entityPoll = new AtomicInteger(1800);
    public final AtomicInteger playerPoll = new AtomicInteger(1900);
    public final AtomicInteger chunkPoll = new AtomicInteger(2000);
    public final AtomicBoolean caveLighting = new AtomicBoolean(true);
    public final AtomicBoolean announceMod = new AtomicBoolean(true);
    public final AtomicBoolean checkUpdates = new AtomicBoolean(true);

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

        CoreProperties that = (CoreProperties) o;
        return 0 == this.compareTo(that);
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
        result = 31 * result + caveLighting.hashCode();
        result = 31 * result + announceMod.hashCode();
        result = 31 * result + checkUpdates.hashCode();
        return result;
    }

    @Override
    public int compareTo(CoreProperties o)
    {
        return Integer.compare(this.hashCode(), o.hashCode());
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
                ", caveLighting=" + caveLighting +
                ", announceMod=" + announceMod +
                ", checkUpdates=" + checkUpdates;
    }
}
