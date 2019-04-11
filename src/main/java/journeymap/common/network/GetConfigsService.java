package journeymap.common.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import journeymap.server.properties.DefaultDimensionProperties;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PermissionProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import static journeymap.common.Constants.ANIMAL_RADAR;
import static journeymap.common.Constants.CAVE_MAP;
import static journeymap.common.Constants.DEFAULT_DIM;
import static journeymap.common.Constants.DIMENSIONS;
import static journeymap.common.Constants.DIM_ID;
import static journeymap.common.Constants.DIM_NAME;
import static journeymap.common.Constants.ENABLED;
import static journeymap.common.Constants.GLOBAL;
import static journeymap.common.Constants.MOB_RADAR;
import static journeymap.common.Constants.OP_CAVE_MAP;
import static journeymap.common.Constants.OP_RADAR;
import static journeymap.common.Constants.OP_SURFACE_MAP;
import static journeymap.common.Constants.OP_TOPO_MAP;
import static journeymap.common.Constants.OP_TRACKING;
import static journeymap.common.Constants.PLAYER_RADAR;
import static journeymap.common.Constants.RADAR;
import static journeymap.common.Constants.SURFACE_MAP;
import static journeymap.common.Constants.TELEPORT;
import static journeymap.common.Constants.TOPO_MAP;
import static journeymap.common.Constants.TRACKING;
import static journeymap.common.Constants.TRACKING_TIME;
import static journeymap.common.Constants.USE_WORLD_ID;
import static journeymap.common.Constants.VILLAGER_RADAR;
import static journeymap.common.Constants.WORLD_ID;
import static journeymap.server.JourneymapServer.isOp;

public class GetConfigsService extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        EntityPlayerMP player = response.getContext().getServerHandler().player;
        if (isOp(player) || FMLCommonHandler.instance().getSide().isClient())
        {
            return collectServerSettings();
        }
        else
        {
            player.sendMessage(new TextComponentString("You do not have permission to adjust Journeymap's server settings!"));
        }
        return null;
    }

    private JsonObject collectServerSettings()
    {
        JsonObject serverConfigs = new JsonObject();
        JsonArray dimensionConfigs = new JsonArray();
        JsonObject globalConfig = new JsonObject();
        JsonObject defaultDimConfig = new JsonObject();
        Integer[] dimensions = DimensionManager.getStaticDimensionIDs();
        GlobalProperties globalProperties = PropertiesManager.getInstance().getGlobalProperties();
        DefaultDimensionProperties defaultDimensionProperties = PropertiesManager.getInstance().getDefaultDimensionProperties();

        // Global Properties.
        if (!FMLCommonHandler.instance().getSide().isClient())
        {
            globalConfig.addProperty(USE_WORLD_ID, globalProperties.useWorldId.get());
            globalConfig.addProperty(WORLD_ID, new WorldNbtIDSaveHandler().getWorldID());
        }
        else
        {
            globalConfig.addProperty(DIM_NAME, "global");
        }
        globalConfig.addProperty(OP_TRACKING, globalProperties.opPlayerTrackingEnabled.get());
        globalConfig.addProperty(TRACKING, globalProperties.playerTrackingEnabled.get());
        globalConfig.addProperty(TRACKING_TIME, globalProperties.playerTrackingUpdateTime.get());
        getCommonProperties(globalProperties, globalConfig);

        // Default Dimension properties
        defaultDimConfig.addProperty(ENABLED, defaultDimensionProperties.enabled.get());
        defaultDimConfig.addProperty(DIM_NAME, "default");
        getCommonProperties(defaultDimensionProperties, defaultDimConfig);

        for (int d : dimensions)
        {
            JsonObject dim = new JsonObject();
            DimensionProperties dimensionProperties = PropertiesManager.getInstance().getDimProperties(d);
            dim.addProperty(ENABLED, dimensionProperties.enabled.get());
            dim.addProperty(DIM_ID, d);
            dim.addProperty(DIM_NAME, DimensionManager.getProviderType(d).getName());
            getCommonProperties(dimensionProperties, dim);
            dimensionConfigs.add(dim);
        }
        serverConfigs.add(GLOBAL, globalConfig);
        serverConfigs.add(DEFAULT_DIM, defaultDimConfig);
        serverConfigs.add(DIMENSIONS, dimensionConfigs);
        return serverConfigs;
    }

    private void getCommonProperties(PermissionProperties from, JsonObject to)
    {
        to.addProperty(TELEPORT, from.teleportEnabled.get());
        to.addProperty(OP_SURFACE_MAP, from.opSurfaceMappingEnabled.get());
        to.addProperty(SURFACE_MAP, from.surfaceMappingEnabled.get());
        to.addProperty(OP_TOPO_MAP, from.opTopoMappingEnabled.get());
        to.addProperty(TOPO_MAP, from.topoMappingEnabled.get());
        to.addProperty(OP_CAVE_MAP, from.opCaveMappingEnabled.get());
        to.addProperty(CAVE_MAP, from.caveMappingEnabled.get());
        to.addProperty(OP_RADAR, from.opRadarEnabled.get());
        to.addProperty(RADAR, from.radarEnabled.get());
        to.addProperty(PLAYER_RADAR, from.playerRadarEnabled.get());
        to.addProperty(VILLAGER_RADAR, from.villagerRadarEnabled.get());
        to.addProperty(ANIMAL_RADAR, from.animalRadarEnabled.get());
        to.addProperty(MOB_RADAR, from.mobRadarEnabled.get());
    }


    @Override
    protected JsonObject onClient(Response response)
    {
        return null;
    }
}
