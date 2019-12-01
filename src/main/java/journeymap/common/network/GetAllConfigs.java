package journeymap.common.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import journeymap.common.Journeymap;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.common.network.impl.utils.Compressor;
import journeymap.common.util.PlayerConfigController;
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

import java.io.IOException;

import static journeymap.common.network.Constants.ANIMAL_RADAR;
import static journeymap.common.network.Constants.CAVE_MAP;
import static journeymap.common.network.Constants.DEFAULT_DIM;
import static journeymap.common.network.Constants.DIMENSIONS;
import static journeymap.common.network.Constants.DIM_ID;
import static journeymap.common.network.Constants.DIM_NAME;
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
import static journeymap.common.network.Constants.SURFACE_MAP;
import static journeymap.common.network.Constants.TELEPORT;
import static journeymap.common.network.Constants.TOPO_MAP;
import static journeymap.common.network.Constants.TRACKING;
import static journeymap.common.network.Constants.TRACKING_UPDATE_TIME;
import static journeymap.common.network.Constants.USE_WORLD_ID;
import static journeymap.common.network.Constants.VILLAGER_RADAR;
import static journeymap.common.network.Constants.WORLD_ID;

public class GetAllConfigs extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        EntityPlayerMP player = response.getContext().getServerHandler().player;
        if (PlayerConfigController.getInstance().canServerAdmin(player) || FMLCommonHandler.instance().getSide().isClient())
        {
            return collectServerSettings();
        }
        else
        {
            player.sendMessage(new TextComponentString("You do not have permission to modify Journeymap's server options!"));
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
        globalConfig.addProperty(TRACKING_UPDATE_TIME, globalProperties.playerTrackingUpdateTime.get());
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
        try
        {
            serverConfigs.addProperty(DIMENSIONS, Compressor.compress(dimensionConfigs.toString()));
        }
        catch (IOException e)
        {
            Journeymap.getLogger().error("ERROR: Unable to compress server options dimension array");
        }
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
