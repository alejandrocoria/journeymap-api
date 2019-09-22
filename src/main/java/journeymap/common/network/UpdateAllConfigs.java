package journeymap.common.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import journeymap.client.feature.FeatureManager;
import journeymap.common.Journeymap;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.common.util.PlayerConfigController;
import journeymap.server.properties.DefaultDimensionProperties;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PermissionProperties;
import journeymap.server.properties.Permissions;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import static journeymap.common.network.Constants.ANIMAL_RADAR;
import static journeymap.common.network.Constants.CAVE_MAP;
import static journeymap.common.network.Constants.DEFAULT_DIM;
import static journeymap.common.network.Constants.DIM;
import static journeymap.common.network.Constants.DIMENSIONS;
import static journeymap.common.network.Constants.DIM_ID;
import static journeymap.common.network.Constants.ENABLED;
import static journeymap.common.network.Constants.GLOBAL;
import static journeymap.common.network.Constants.MOB_RADAR;
import static journeymap.common.network.Constants.OP_CAVE_MAP;
import static journeymap.common.network.Constants.OP_RADAR;
import static journeymap.common.network.Constants.OP_SURFACE_MAP;
import static journeymap.common.network.Constants.OP_TOPO_MAP;
import static journeymap.common.network.Constants.OP_TRACKING;
import static journeymap.common.network.Constants.PLAYER_RADAR;
import static journeymap.common.network.Constants.RADAR;
import static journeymap.common.network.Constants.SERVER_ADMIN;
import static journeymap.common.network.Constants.SETTINGS;
import static journeymap.common.network.Constants.SURFACE_MAP;
import static journeymap.common.network.Constants.TELEPORT;
import static journeymap.common.network.Constants.TOPO_MAP;
import static journeymap.common.network.Constants.TRACKING;
import static journeymap.common.network.Constants.TRACKING_UPDATE_TIME;
import static journeymap.common.network.Constants.USE_WORLD_ID;
import static journeymap.common.network.Constants.VILLAGER_RADAR;
import static journeymap.common.network.Constants.WORLD_ID;

public class UpdateAllConfigs extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        EntityPlayerMP player = response.getContext().getServerHandler().player;
        if (PlayerConfigController.getInstance().canServerAdmin(player) || FMLCommonHandler.instance().getSide().isClient())
        {
            // Save the properties!
            JsonObject prop = response.getAsJson();
            if (prop.get(GLOBAL) != null)
            {
                JsonObject global = prop.get(GLOBAL).getAsJsonObject();
                GlobalProperties properties = PropertiesManager.getInstance().getGlobalProperties();
                if (!FMLCommonHandler.instance().getSide().isClient())
                {
                    if (global.get(USE_WORLD_ID) != null)
                    {
                        properties.useWorldId.set(global.get(USE_WORLD_ID).getAsBoolean());
                    }
                    if (global.get(OP_TRACKING) != null)
                    {
                        properties.opPlayerTrackingEnabled.set(global.get(OP_TRACKING).getAsBoolean());
                    }
                    if (global.get(TRACKING) != null)
                    {
                        properties.playerTrackingEnabled.set(global.get(TRACKING).getAsBoolean());
                    }
                    if (global.get(TRACKING_UPDATE_TIME) != null)
                    {
                        properties.playerTrackingUpdateTime.set(global.get(TRACKING_UPDATE_TIME).getAsInt());
                    }
                }
                updateCommonProperties(properties, global);
                properties.save();
            }

            if (prop.get(DEFAULT_DIM) != null)
            {
                JsonObject dDim = prop.get(DEFAULT_DIM).getAsJsonObject();
                DefaultDimensionProperties properties = PropertiesManager.getInstance().getDefaultDimensionProperties();
                properties.enabled.set(dDim.get(ENABLED).getAsBoolean());
                updateCommonProperties(properties, dDim);
                properties.save();
            }
            if (prop.get(DIMENSIONS) != null)
            {
                JsonArray dimArray = prop.get(DIMENSIONS).getAsJsonArray();
                for (JsonElement element : dimArray)
                {
                    JsonObject dimProp = element.getAsJsonObject();
                    DimensionProperties properties = PropertiesManager.getInstance().getDimProperties(dimProp.get(DIM_ID).getAsInt());
                    properties.enabled.set(dimProp.get(ENABLED).getAsBoolean());
                    updateCommonProperties(properties, dimProp);
                    properties.save();
                }
            }


            // force update players with the new configs!

            for (EntityPlayerMP playerTo : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
            {
                JsonObject config = PlayerConfigController.getInstance().getPlayerConfig(playerTo);
                this.sendToPlayer(config, playerTo);
            }
        }
        else
        {
            player.sendMessage(new TextComponentString("You do not have permission to modify Journeymap's server options!"));
        }
        return null;
    }

    private void updateCommonProperties(PermissionProperties to, JsonObject from)
    {
        to.teleportEnabled.set(from.get(TELEPORT).getAsBoolean());
        to.opSurfaceMappingEnabled.set(from.get(OP_SURFACE_MAP).getAsBoolean());
        to.surfaceMappingEnabled.set(from.get(SURFACE_MAP).getAsBoolean());
        to.opTopoMappingEnabled.set(from.get(OP_TOPO_MAP).getAsBoolean());
        to.topoMappingEnabled.set(from.get(TOPO_MAP).getAsBoolean());
        to.opCaveMappingEnabled.set(from.get(OP_CAVE_MAP).getAsBoolean());
        to.caveMappingEnabled.set(from.get(CAVE_MAP).getAsBoolean());
        to.opRadarEnabled.set(from.get(OP_RADAR).getAsBoolean());
        to.radarEnabled.set(from.get(RADAR).getAsBoolean());
        to.playerRadarEnabled.set(from.get(PLAYER_RADAR).getAsBoolean());
        to.villagerRadarEnabled.set(from.get(VILLAGER_RADAR).getAsBoolean());
        to.animalRadarEnabled.set(from.get(ANIMAL_RADAR).getAsBoolean());
        to.mobRadarEnabled.set(from.get(MOB_RADAR).getAsBoolean());
    }

    @Override
    protected JsonObject onClient(Response response)
    {
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
            Journeymap.getClient().setJourneyMapServerConnection(true);
            String dimProperties = response.getAsJson().get(DIM).getAsString();
            PermissionProperties prop = new Permissions().load(dimProperties, false);
            FeatureManager.INSTANCE.updateDimensionFeatures(prop);
        }
        return null;
    }
}