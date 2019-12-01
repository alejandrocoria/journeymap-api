package journeymap.common.util;

import com.google.gson.JsonObject;
import journeymap.client.feature.FeatureManager;
import journeymap.common.Journeymap;
import journeymap.common.network.impl.Response;
import journeymap.server.Constants;
import journeymap.server.config.ForgeConfig;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PermissionProperties;
import journeymap.server.properties.Permissions;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import static journeymap.common.network.Constants.DIM;
import static journeymap.common.network.Constants.SERVER_ADMIN;
import static journeymap.common.network.Constants.SETTINGS;
import static journeymap.common.network.Constants.TELEPORT;
import static journeymap.common.network.Constants.TRACKING;
import static journeymap.common.network.Constants.WORLD_ID;
import static journeymap.server.JourneymapServer.isOp;

public class PlayerConfigController
{
    private static PlayerConfigController INSTANCE;

    public static PlayerConfigController getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new PlayerConfigController();
        }
        return INSTANCE;
    }

    public JsonObject getPlayerConfig(EntityPlayerMP player)
    {
        JsonObject config = new JsonObject();
        JsonObject settings = new JsonObject();
        if (PropertiesManager.getInstance().getGlobalProperties().useWorldId.get() && !FMLCommonHandler.instance().getSide().isClient())
        {
            WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            String worldID = worldSaveHandler.getWorldID();
            settings.addProperty(WORLD_ID, worldID);
        }
        settings.addProperty(TELEPORT, canTeleport(player));
        settings.addProperty(TRACKING, canPlayerTrack(player));
        settings.addProperty(SERVER_ADMIN, canServerAdmin(player));
        config.add(SETTINGS, settings);
        config.addProperty(DIM, getDimProperties(player));
        return config;
    }

    public boolean canServerAdmin(EntityPlayerMP player)
    {
        String[] admins = ForgeConfig.playerNames;
        for (String admin : admins)
        {
            if (player.getUniqueID().toString().equals(admin)
                    || player.getName().equalsIgnoreCase(admin)
                    || Constants.debugOverride(player))
            {
                return true;
            }
        }
        if (isOp(player)) {
            return ForgeConfig.opAccess;
        }
        return false;
    }

    private String getDimProperties(EntityPlayerMP player)
    {
        DimensionProperties dimensionProperties = PropertiesManager.getInstance().getDimProperties(player.dimension);
        PermissionProperties prop;
        try
        {
            /*
             * Cloning since we do not want to modify the permission properties,
             * We want a brand new copy to send to the client
             */
            if (dimensionProperties.enabled.get())
            {
                prop = (DimensionProperties) dimensionProperties.clone();
            }
            else
            {
                prop = (GlobalProperties) PropertiesManager.getInstance().getGlobalProperties().clone();
            }

            /*
             * If player is op, set the cave and radar options on the packet to send.
             * The client only reads radarEnabled and caveMappingEnabled, it ignores the
             */
            if (isOp(player))
            {
                prop.radarEnabled.set(prop.opRadarEnabled.get());
                prop.caveMappingEnabled.set(prop.opCaveMappingEnabled.get());
                prop.surfaceMappingEnabled.set(prop.opSurfaceMappingEnabled.get());
                prop.topoMappingEnabled.set(prop.opTopoMappingEnabled.get());
            }
            return prop.toJsonString(false);
        }
        catch (CloneNotSupportedException e)
        {
            Journeymap.getLogger().error("CloneNotSupportedException: ", e);
            return null;
        }
    }

    private boolean canTeleport(EntityPlayerMP player)
    {
        if (PropertiesManager.getInstance().getDimProperties(player.dimension).enabled.get())
        {
            return PropertiesManager.getInstance().getDimProperties(player.dimension).teleportEnabled.get();
        }
        else if (PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get())
        {
            return true;
        }
        else
        {
            return isOp(player);
        }
    }

    private boolean canPlayerTrack(EntityPlayerMP player)
    {
        if (PropertiesManager.getInstance().getGlobalProperties().playerTrackingEnabled.get())
        {
            return true;
        }
        else
        {
            return PropertiesManager.getInstance().getGlobalProperties().opPlayerTrackingEnabled.get() && isOp(player);
        }
    }

    public void updateClientConfigs(Response response) {
        if (response.getAsJson().get(SETTINGS) != null)
        {
            JsonObject settings = response.getAsJson().get(SETTINGS).getAsJsonObject();
            if (settings.get(WORLD_ID) != null)
            {
                Journeymap.getClient().setCurrentWorldId(settings.get(WORLD_ID).getAsString());
            }
            if ((settings.get(TELEPORT) != null))
            {
                Journeymap.getClient().setTeleportEnabled(settings.get(TELEPORT).getAsBoolean());
            }
            if ((settings.get(TRACKING) != null))
            {
                Journeymap.getClient().setPlayerTrackingEnabled(settings.get(TRACKING).getAsBoolean());
            }
            if ((settings.get(SERVER_ADMIN) != null))
            {
                Journeymap.getClient().setServerAdmin(settings.get(SERVER_ADMIN).getAsBoolean());
            }
            String dimProperties = response.getAsJson().get(DIM).getAsString();
            PermissionProperties prop = new Permissions().load(dimProperties, false);
            FeatureManager.INSTANCE.updateDimensionFeatures(prop);
        }
    }
}
