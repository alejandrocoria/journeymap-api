package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.StringField;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

import static journeymap.server.properties.ServerCategory.General;

/**
 * Properties which can be applied globally (unless overridden by a specific DimensionProperties.)
 */
public class GlobalProperties extends PermissionProperties
{
    public final StringField worldID = new StringField(General, "World ID ");
    public static final BooleanField teleportEnabled = new BooleanField(ServerCategory.General, "Enable Players to teleport", false);

    /**
     * Constructor.
     */
    public GlobalProperties()
    {
        super("Global Server Configuration", "Applies to all dimensions unless overridden. 'WorldID is Read Only'");
    }


    public String getName()
    {
        return "global";
    }

    /**
     * @param worldId
     */
    @SideOnly(Side.SERVER)
    public void setWorldID(String worldId)
    {
        this.worldID.set(worldId);
        this.preSave();
    }

    @SideOnly(Side.SERVER)
    public String getWorldID()
    {
        WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
        return worldSaveHandler.getWorldID();
    }

    @Override
    protected void postLoad(boolean isNew)
    {
        super.postLoad(isNew);
        WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
        this.worldID.set(worldSaveHandler.getWorldID());

    }

    @Override
    protected void preSave()
    {
        super.preSave();
        if (worldID.get() == null || worldID.get().isEmpty() || worldID.get().equals(""))
        {
            worldID.set(UUID.randomUUID().toString());
        }
        WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
        worldSaveHandler.setWorldID(worldID.get());

    }
}
