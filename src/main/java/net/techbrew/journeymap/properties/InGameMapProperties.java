package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared Properties for in-game map types.
 */
public abstract class InGameMapProperties extends MapProperties
{
    public final AtomicBoolean forceUnicode = new AtomicBoolean(false);
    public final AtomicBoolean fontSmall = new AtomicBoolean(true);
    public final AtomicBoolean textureSmall = new AtomicBoolean(true);

    protected InGameMapProperties()
    {
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
        return result;
    }
}
