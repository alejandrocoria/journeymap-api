package net.techbrew.journeymap.data;

import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
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
import net.techbrew.journeymap.log.LogFormatter;
import org.lwjgl.opengl.Display;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;
import java.util.logging.Level;

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
    Map<Feature,Boolean> features;
    String jm_version;
    String latest_journeymap_version;
    String mc_version;
    String mod_name = JourneyMap.MOD_NAME;
    int browser_poll;

    /**
     * Constructor.
     */
    public WorldData()
    {
    }

    @Override
    public WorldData load(Class aClass) throws Exception
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        WorldInfo worldInfo = mc.theWorld.getWorldInfo();

        IntegratedServer server = mc.getIntegratedServer();
        boolean multiplayer = server == null || server.getPublic();

        name = getWorldName(mc);
        dimension = mc.theWorld.provider.dimensionId;
        hardcore = worldInfo.isHardcoreModeEnabled();
        singlePlayer = !multiplayer;
        time = mc.theWorld.getWorldTime() % 24000L;
        features = FeatureManager.instance().getAllowedFeatures();

        mod_name = JourneyMap.MOD_NAME;
        jm_version = JourneyMap.JM_VERSION;
        latest_journeymap_version = VersionCheck.getVersionAvailable();
        mc_version = Display.getTitle().split("\\s(?=\\d)")[1];
        browser_poll = Math.max(1000, JourneyMap.getInstance().webMapProperties.browserPoll.get());

        return this;
    }


    public static boolean isHardcoreAndMultiplayer()
    {
        WorldData world = DataCache.instance().getWorld(false);
        return world.hardcore && !world.singlePlayer;
    }

    private static String getServerHash()
    {
        String serverName = getServerName();
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            if (md5 != null)
            {
                byte[] bServerName = serverName.getBytes("UTF-8");
                byte[] hashed = md5.digest(bServerName);
                BigInteger bigInt = new BigInteger(1, hashed);
                String md5Hash = bigInt.toString(16);
                while (md5Hash.length() < 32)
                {
                    md5Hash = "0" + md5Hash;
                }
                return md5Hash;
            }
        }
        catch (Exception ex)
        {
        }
        return serverName;
    }

    private static String getServerName()
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
            JourneyMap.getLogger().severe("Couldn't get server name: " + LogFormatter.toString(t));
        }
        return "server";
    }

    public static int getServerPort()
    {
        try
        {
            NetHandlerPlayClient sendQueue = Minecraft.getMinecraft().getNetHandler();
            SocketAddress socketAddress = sendQueue.getNetworkManager().getSocketAddress();
            if ((socketAddress != null && socketAddress instanceof InetSocketAddress))
            {
                InetSocketAddress inetAddr = (InetSocketAddress) socketAddress;
                return inetAddr.getPort();
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe("Couldn't get server port: " + LogFormatter.toString(t));
        }
        return 0;
    }

    /**
     * Get the current world name.
     *
     * @param mc
     * @return
     */
    public static String getWorldName(Minecraft mc)
    {

        // Get the name
        String worldName = null;
        if (mc.isSingleplayer())
        {
            worldName = mc.getIntegratedServer().getWorldName();
        }
        else
        {
            if (mc.theWorld == null)
            {
                return "offline";
            }
            worldName = mc.theWorld.getWorldInfo().getWorldName();
            if (!"MpServer".equals(worldName))
            {
                worldName = getServerName() + "_" + worldName;
            }
            else
            {
                worldName = getServerName();
            }
        }

        // Clean it up for display
        try
        {
            worldName = URLEncoder.encode(worldName, "UTF-8").replaceAll("\\+", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        catch (UnsupportedEncodingException e)
        {
            // Shouldn't happen
            worldName = "Minecraft";    //$NON-NLS-1$
        }
        return worldName;
    }

    public static List<WorldProvider> getDimensionProviders(List<Integer> requiredDimensionList)
    {
        HashSet<Integer> requiredDims = new HashSet<Integer>(requiredDimensionList);
        HashMap<Integer, WorldProvider> dimProviders = new HashMap<Integer, WorldProvider>();

        Level logLevel = Level.FINE;
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
        JourneyMap.getLogger().log(logLevel, String.format("Using player's provider for dim %s: %s", playerProvider.dimensionId, playerProvider.getDimensionName()));

        // Get a provider for the rest
        for(int dim : requiredDims)
        {
            if(!dimProviders.containsKey(dim))
            {
                if (DimensionManager.getWorld(dim) != null)
                {
                    try
                    {
                        WorldProvider dimProvider = DimensionManager.getProvider(dim);
                        dimProviders.put(dim, dimProvider);
                        JourneyMap.getLogger().log(logLevel, String.format("DimensionManager.getProvider(%s): %s", dim, dimProvider.getDimensionName()));
                    }
                    catch (Throwable t)
                    {
                        JourneyMap.getLogger().warning(String.format("Couldn't DimensionManager.getProvider(%s) because of error: %s", dim, t.getMessage()));
                    }
                }
                else
                {
                    WorldProvider provider;
                    try
                    {
                        provider = DimensionManager.createProviderFor(dim);
                        provider.dimensionId = dim;
                        dimProviders.put(dim, provider);
                        JourneyMap.getLogger().log(logLevel, String.format("DimensionManager.createProviderFor(%s): %s", dim, provider.getDimensionName()));
                    }
                    catch (Throwable t)
                    {
                        JourneyMap.getLogger().warning(String.format("Couldn't DimensionManager.createProviderFor(%s) because of error: %s", dim, t.getMessage()));
                    }
                }
            }
        }

        // Remove required dims that have been found
        requiredDims.removeAll(dimProviders.keySet());

        // Make sure required dimensions are added. Since we got this far without finding providers for them, use fake providers.
        for(int dim : requiredDims)
        {
            if(!dimProviders.containsKey(dim))
            {
                WorldProvider provider = new FakeDimensionProvider(dim);
                dimProviders.put(dim, provider);
                JourneyMap.getLogger().warning(String.format("Used fake provider for required dim: %s", dim));
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
            return Constants.getString("JourneyMap.dimension", this.dimensionId);
        }
    }
}
