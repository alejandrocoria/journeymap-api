package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.StringField;
import journeymap.server.nbt.WorldNbtIDSaveHandler;

import static journeymap.server.properties.ServerCategory.General;

/**
 * Port of oldservercode.config.Configuration
 */
public class GlobalProperties extends DimensionProperties
{
    protected BooleanField useWorldID = new BooleanField(General, "Use World ID", false);
    protected StringField worldID = new StringField(General, "World ID");
    protected BooleanField saveInWorldFolder = new BooleanField(General, "Save configs in world folder", false);

    public GlobalProperties()
    {
        super(null, "Global Server Configuration", "Applies to all dimensions unless overridden.");
    }

    @Override
    public String getName()
    {
        return "global";
    }

    public String getWorldID()
    {
        if (this.saveInWorldFolder.get())
        {
            WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            return worldSaveHandler.getWorldID();
        }
        return this.worldID.get();
    }

    public void setWorldID(String worldID)
    {
        if (this.saveInWorldFolder.get())
        {
            WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            worldSaveHandler.setWorldID(worldID);
            this.worldID.set(null);
            return;
        }
        this.worldID.set(worldID);
    }
}
