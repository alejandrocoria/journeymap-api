package net.techbrew.journeymap.data;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.VersionCheck;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.IconSetFileHandler;
import net.techbrew.journeymap.io.RealmsHelper;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.log.LogFormatter;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.Display;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.util.*;

/**
 * Provides world properties
 *
 * @author mwoodman
 */
public class WorldData extends CacheLoader<Class, WorldData>
{
    String name;
    int dimension;
    long time;
    boolean hardcore;
    boolean singlePlayer;
    Map<Feature, Boolean> features;
    String jm_version;
    String latest_journeymap_version;
    String mc_version;
    String mod_name = JourneyMap.MOD_NAME;
    String iconSetName;
    String[] iconSetNames;
    int browser_poll;

    /**
     * Constructor.
     */
    public WorldData()
    {
    }

    public static boolean isHardcoreAndMultiplayer()
    {
        WorldData world = DataCache.instance().getWorld(false);
        return world.hardcore && !world.singlePlayer;
    }

    private static String getServerName()
    {
        try
        {
            String serverName = RealmsHelper.getRealmsServerName();

            if (serverName != null)
            {
                return serverName;
            }
            else
            {
                Minecraft mc = FMLClientHandler.instance().getClient();
                ServerData serverData = mc.func_147104_D(); // getServerData()

                if (serverData != null)
                {
                    serverName = serverData.serverName;
                    if (serverName != null)
                    {
                        if (Strings.isNullOrEmpty(serverName.trim()))
                        {
                            serverName = serverData.serverIP;
                        }
                        return serverName;
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Couldn't get server name: " + LogFormatter.toString(t));
        }

        // Fallback
        return getLegacyServerName();
    }

    public static String getLegacyServerName()
    {
        try
        {
            NetworkManager netManager = FMLClientHandler.instance().getClientToServerNetworkManager();
            if (netManager != null)
            {
                SocketAddress socketAddress = netManager.getSocketAddress();
                if ((socketAddress != null && socketAddress instanceof InetSocketAddress))
                {
                    InetSocketAddress inetAddr = (InetSocketAddress) socketAddress;
                    return inetAddr.getHostName();
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Couldn't get server name: " + LogFormatter.toString(t));
        }
        return "server";
    }

    /**
     * Get the current world name.
     *
     * @param mc
     * @return
     */
    public static String getWorldName(Minecraft mc, boolean useLegacyName)
    {
        // Get the name
        String worldName = null;
        if (mc.isSingleplayer())
        {
            if (useLegacyName)
            {
                worldName = mc.getIntegratedServer().getWorldName();
            }
            else
            {
                worldName = mc.getIntegratedServer().getFolderName();
            }
        }
        else
        {
            if (mc.theWorld == null)
            {
                return "offline";
            }
            worldName = mc.theWorld.getWorldInfo().getWorldName();
            String serverName = getServerName();

            if (!"MpServer".equals(worldName))
            {
                worldName = serverName + "_" + worldName;
            }
            else
            {
                worldName = serverName;
            }
        }

        if (useLegacyName)
        {
            worldName = getLegacyUrlEncodedWorldName(worldName);
        }
        else
        {
            worldName = worldName.trim();
        }

        if (Strings.isNullOrEmpty(worldName.trim()))
        {
            worldName = "unnamed";
        }

        return worldName;
    }

    private static String getLegacyUrlEncodedWorldName(String worldName)
    {
        try
        {
            return URLEncoder.encode(worldName, "UTF-8").replace("+", " ");
        }
        catch (UnsupportedEncodingException e)
        {
            return worldName;
        }
    }

    public static List<WorldProvider> getDimensionProviders(List<Integer> requiredDimensionList)
    {
        try
        {
            HashSet<Integer> requiredDims = new HashSet<Integer>(requiredDimensionList);
            HashMap<Integer, WorldProvider> dimProviders = new HashMap<Integer, WorldProvider>();

            Level logLevel = Level.DEBUG;
            JourneyMap.getLogger().log(logLevel, String.format("Required dimensions from waypoints: %s", requiredDimensionList));

            // DimensionIDs works for local servers
            Integer[] dims = DimensionManager.getIDs();
            JourneyMap.getLogger().log(logLevel, String.format("DimensionManager has dims: %s", Arrays.asList(dims)));
            requiredDims.addAll(Arrays.asList(dims));

            // StaticDimensionIDs works for remote servers
            dims = DimensionManager.getStaticDimensionIDs();
            JourneyMap.getLogger().log(logLevel, String.format("DimensionManager has static dims: %s", Arrays.asList(dims)));
            requiredDims.addAll(Arrays.asList(dims));

            // Use the player's provider
            WorldProvider playerProvider = FMLClientHandler.instance().getClient().thePlayer.worldObj.provider;
            dimProviders.put(playerProvider.dimensionId, playerProvider);
            requiredDims.remove(playerProvider.dimensionId);
            JourneyMap.getLogger().log(logLevel, String.format("Using player's provider for dim %s: %s", playerProvider.dimensionId, getSafeDimensionName(playerProvider)));

            // Get a provider for the rest
            for (int dim : requiredDims)
            {
                if (!dimProviders.containsKey(dim))
                {
                    if (DimensionManager.getWorld(dim) != null)
                    {
                        try
                        {
                            WorldProvider dimProvider = DimensionManager.getProvider(dim);
                            dimProvider.getDimensionName(); // Force the name error
                            dimProviders.put(dim, dimProvider);
                            JourneyMap.getLogger().log(logLevel, String.format("DimensionManager.getProvider(%s): %s", dim, getSafeDimensionName(dimProvider)));
                        }
                        catch (Throwable t)
                        {
                            JMLogger.logOnce(String.format("Couldn't DimensionManager.getProvider(%s) because of error: %s", dim, t), t);
                        }
                    }
                    else
                    {
                        WorldProvider provider;
                        try
                        {
                            provider = DimensionManager.createProviderFor(dim);
                            provider.getDimensionName(); // Force the name error
                            provider.dimensionId = dim;
                            dimProviders.put(dim, provider);
                            JourneyMap.getLogger().log(logLevel, String.format("DimensionManager.createProviderFor(%s): %s", dim, getSafeDimensionName(playerProvider)));
                        }
                        catch (Throwable t)
                        {
                            JMLogger.logOnce(String.format("Couldn't DimensionManager.createProviderFor(%s) because of error: %s", dim, t), t);
                        }
                    }
                }
            }

            // Remove required dims that have been found
            requiredDims.removeAll(dimProviders.keySet());

            // Make sure required dimensions are added. Since we got this far without finding providers for them, use fake providers.
            for (int dim : requiredDims)
            {
                if (!dimProviders.containsKey(dim))
                {
                    WorldProvider provider = new FakeDimensionProvider(dim);
                    dimProviders.put(dim, provider);
                    JourneyMap.getLogger().warn(String.format("Used FakeDimensionProvider for required dim: %s", dim));
                }
            }

            // Sort by dim and return
            ArrayList<WorldProvider> providerList = new ArrayList<WorldProvider>(dimProviders.values());
            Collections.sort(providerList, new Comparator<WorldProvider>()
            {
                @Override
                public int compare(WorldProvider o1, WorldProvider o2)
                {
                    return Integer.valueOf(o1.dimensionId).compareTo(o2.dimensionId);
                }
            });

            return providerList;
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Unexpected error in WorldData.getDimensionProviders(): ", t);
            return Collections.emptyList();
        }
    }

    public static String getSafeDimensionName(WorldProvider worldProvider)
    {
        if (worldProvider == null)
        {
            return null;
        }

        try
        {
            return worldProvider.getDimensionName();
        }
        catch (Exception e)
        {
            return Constants.getString("jm.common.dimension", worldProvider.dimensionId);
        }
    }

    @Override
    public WorldData load(Class aClass) throws Exception
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        WorldInfo worldInfo = mc.theWorld.getWorldInfo();

        IntegratedServer server = mc.getIntegratedServer();
        boolean multiplayer = server == null || server.getPublic();

        name = getWorldName(mc, false);
        dimension = mc.theWorld.provider.dimensionId;
        hardcore = worldInfo.isHardcoreModeEnabled();
        singlePlayer = !multiplayer;
        time = mc.theWorld.getWorldTime() % 24000L;
        features = FeatureManager.getAllowedFeatures();

        mod_name = JourneyMap.MOD_NAME;
        jm_version = JourneyMap.JM_VERSION.toString();
        latest_journeymap_version = VersionCheck.getVersionAvailable();
        mc_version = Display.getTitle().split("\\s(?=\\d)")[1];
        browser_poll = Math.max(1000, JourneyMap.getCoreProperties().browserPoll.get());

        iconSetName = JourneyMap.getFullMapProperties().getEntityIconSetName().get();
        iconSetNames = IconSetFileHandler.getEntityIconSetNames().toArray(new String[0]);

        return this;
    }

    /**
     * Return length of time in millis data should be kept.
     */
    public long getTTL()
    {
        return 1000;
    }

    /**
     * Stand-in for world provider that couldn't be found.
     */
    static class FakeDimensionProvider extends WorldProvider
    {
        FakeDimensionProvider(int dimension)
        {
            this.dimensionId = dimension;
        }

        @Override
        public String getDimensionName()
        {
            return Constants.getString("jm.common.dimension", this.dimensionId);
        }
    }
}
