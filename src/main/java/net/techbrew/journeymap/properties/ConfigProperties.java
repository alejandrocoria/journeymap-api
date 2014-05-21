package net.techbrew.journeymap.properties;

/**
 * Properties for basic mod configuration.
 */
public class ConfigProperties extends PropertiesBase implements Comparable<ConfigProperties>
{
    protected transient static final int CURRENT_REVISION = 1;
    protected final String name = "config";
    protected int revision = CURRENT_REVISION;

    protected String logLevel = "INFO";
    protected int chunkOffset = 5;
    protected int entityPoll = 1800;
    protected int playerPoll = 1900;
    protected int chunkPoll = 2000;
    protected boolean caveLighting = true;
    protected boolean announceMod = true;
    protected boolean checkUpdates = true;
    protected boolean waypointManagementEnabled = true;

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

    public String getLogLevel()
    {
        return logLevel;
    }

    public void setLogLevel(String logLevel)
    {
        this.logLevel = logLevel;
    }

    public int getChunkOffset()
    {
        return chunkOffset;
    }

    public void setChunkOffset(int chunkOffset)
    {
        this.chunkOffset = chunkOffset;
    }

    public int getEntityPoll()
    {
        return entityPoll;
    }

    public void setEntityPoll(int entityPoll)
    {
        this.entityPoll = entityPoll;
    }

    public int getPlayerPoll()
    {
        return playerPoll;
    }

    public void setPlayerPoll(int playerPoll)
    {
        this.playerPoll = playerPoll;
    }

    public int getChunkPoll()
    {
        return chunkPoll;
    }

    public void setChunkPoll(int chunkPoll)
    {
        this.chunkPoll = chunkPoll;
    }

    public boolean isCaveLighting()
    {
        return caveLighting;
    }

    public void setCaveLighting(boolean caveLighting)
    {
        this.caveLighting = caveLighting;
    }

    public boolean isAnnounceMod()
    {
        return announceMod;
    }

    public void setAnnounceMod(boolean announceMod)
    {
        this.announceMod = announceMod;
    }

    public boolean isCheckUpdates()
    {
        return checkUpdates;
    }

    public void setCheckUpdates(boolean checkUpdates)
    {
        this.checkUpdates = checkUpdates;
    }

    public boolean isWaypointManagementEnabled()
    {
        return waypointManagementEnabled;
    }

    public void setWaypointManagementEnabled(boolean waypointManagementEnabled)
    {
        this.waypointManagementEnabled = waypointManagementEnabled;
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

        ConfigProperties that = (ConfigProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + logLevel.hashCode();
        result = 31 * result + revision;
        result = 31 * result + chunkOffset;
        result = 31 * result + entityPoll;
        result = 31 * result + playerPoll;
        result = 31 * result + chunkPoll;
        result = 31 * result + (caveLighting ? 1 : 0);
        result = 31 * result + (announceMod ? 1 : 0);
        result = 31 * result + (checkUpdates ? 1 : 0);
        result = 31 * result + (waypointManagementEnabled ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(ConfigProperties o)
    {
        return Integer.compare(this.hashCode(), o.hashCode());
    }
}
