package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.StringField;
import journeymap.server.nbt.WorldNbtIDSaveHandler;

import static journeymap.server.properties.ServerCategory.General;

/**
 * Properties which can be applied globally (unless overridden by a specific DimensionProperties.)
 */
public class GlobalProperties extends PermissionProperties
{
    public final BooleanField useWorldID = new BooleanField(General, "Use World ID", false);
    public final StringField worldID = new StringField(General, "World ID");
    public final BooleanField saveInWorldFolder = new BooleanField(General, "Save configs in world folder", false);

    /**
     * Constructor.
     */
    public GlobalProperties()
    {
        super("Global Server Configuration", "Applies to all dimensions unless overridden.");
    }


    public String getName()
    {
        return "global";
    }

    @Override
    protected void postLoad(boolean isNew)
    {
        super.postLoad(isNew);

        if (this.saveInWorldFolder.get())
        {
            WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            this.worldID.set(worldSaveHandler.getWorldID());
        }
    }

    @Override
    protected void preSave()
    {
        super.preSave();
        if (this.saveInWorldFolder.get())
        {
            WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            worldSaveHandler.setWorldID(worldID.get());
        }
    }
}
