package net.techbrew.journeymap.properties;

/**
 * Properties for the web map in browser.
 */
public class WebMapProperties extends MapProperties
{
    protected transient static final int CURRENT_REVISION = 1;
    protected final String name = "webmap";
    protected int revision = CURRENT_REVISION;

    protected boolean enabled = false;
    protected int port = 8080;
    protected int browserPoll = 2000;

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

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean toggleEnabled()
    {
        setEnabled(!enabled);
        return enabled;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public int getBrowserPoll()
    {
        return browserPoll;
    }

    public void setBrowserPoll(int browserPoll)
    {
        this.browserPoll = browserPoll;
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
        return 0==this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + revision;
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + port;
        result = 31 * result + browserPoll;
        return result;
    }
}
