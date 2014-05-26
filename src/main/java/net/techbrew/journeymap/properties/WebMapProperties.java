package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Properties for the web map in browser.
 */
public class WebMapProperties extends MapProperties
{
    protected transient static final int CURRENT_REVISION = 1;
    protected final String name = "webmap";
    protected AtomicInteger revision = new AtomicInteger(CURRENT_REVISION);

    public final AtomicBoolean enabled = new AtomicBoolean(true);
    public final AtomicInteger port = new AtomicInteger(8080);
    public final AtomicInteger browserPoll = new AtomicInteger(2000);

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
        if (!super.equals(o))
        {
            return false;
        }

        WebMapProperties that = (WebMapProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + revision.hashCode();
        result = 31 * result + enabled.hashCode();
        result = 31 * result + port.hashCode();
        result = 31 * result + browserPoll.hashCode();
        return result;
    }
}