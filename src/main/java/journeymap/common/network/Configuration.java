package journeymap.common.network;

import com.google.gson.JsonObject;
import journeymap.common.Journeymap;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PermissionProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import static journeymap.common.Constants.DIM;
import static journeymap.common.Constants.SETTINGS;
import static journeymap.common.Constants.TELEPORT;
import static journeymap.common.Constants.TRACKING;
import static journeymap.common.Constants.WORLD_ID;
import static journeymap.server.JourneymapServer.isOp;

public class Configuration extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        EntityPlayerMP player = response.getContext().getServerHandler().player;
        JsonObject reply = new JsonObject();
        JsonObject settings = new JsonObject();
        if (PropertiesManager.getInstance().getGlobalProperties().useWorldId.get() && !FMLCommonHandler.instance().getSide().isClient())
        {
            WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            String worldID = worldSaveHandler.getWorldID();
            settings.addProperty(WORLD_ID, worldID);
        }
        settings.addProperty(TELEPORT, canTeleport(player));
        settings.addProperty(TRACKING, canPlayerTrack(player));
        reply.add(SETTINGS, settings);
        reply.addProperty(DIM, getDimProperties(player));
        return reply;
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

    @Override
    public JsonObject onClient(Response response)
    {
        Journeymap.getClient().setJourneyMapServerConnection(true);
        // do nothing, handled by the callback.
        return null;
    }
}
