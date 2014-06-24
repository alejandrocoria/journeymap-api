package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared Properties for in-game map types.
 */
public abstract class InGameMapProperties extends MapProperties
{
    public final AtomicBoolean forceUnicode = new AtomicBoolean(false);
    public final AtomicBoolean fontSmall = new AtomicBoolean(true);
    public final AtomicBoolean textureSmall = new AtomicBoolean(true);
    public final AtomicInteger terrainAlpha = new AtomicInteger(255);

    protected InGameMapProperties()
    {
    }

    @Override
    protected boolean validate()
    {
        boolean saveNeeded = super.validate();

        if(terrainAlpha.get()<0)
        {
            terrainAlpha.set(0);
            saveNeeded = true;
        }
        else if(terrainAlpha.get()>255)
        {
            terrainAlpha.set(255);
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

        InGameMapProperties that = (InGameMapProperties) o;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + forceUnicode.hashCode();
        result = 31 * result + fontSmall.hashCode();
        result = 31 * result + textureSmall.hashCode();
        result = 31 * result + terrainAlpha.hashCode();
        return result;
    }
}
