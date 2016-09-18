/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.data;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.JMLogger;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.version.VersionCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
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
    String mod_name = JourneymapClient.MOD_NAME;
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
        WorldData world = DataCache.INSTANCE.getWorld(false);
        return world.hardcore && !world.singlePlayer;
    }

    private static String getServerName()
    {
        try
        {
            return ForgeHelper.INSTANCE.getRealmsServerName();
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Couldn't get service name: " + LogFormatter.toString(t));
            // Fallback
            return getLegacyServerName();
        }
    }

    public static String getLegacyServerName()
    {
        try
        {
            NetworkManager netManager = FMLClientHandler.instance().getClientToServerNetworkManager();
            if (netManager != null)
            {
                SocketAddress socketAddress = ForgeHelper.INSTANCE.getSocketAddress(netManager);
                if ((socketAddress != null && socketAddress instanceof InetSocketAddress))
                {
                    InetSocketAddress inetAddr = (InetSocketAddress) socketAddress;
                    return inetAddr.getHostName();
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Couldn't get server name: " + LogFormatter.toString(t));
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
                return mc.getIntegratedServer().getFolderName();
            }
        }
        else
        {
            worldName = mc.theWorld.getWorldInfo().getWorldName();
            String serverName = getServerName();

            if (serverName == null)
            {
                return "offline";
            }

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

    public static List<DimensionProvider> getDimensionProviders(List<Integer> requiredDimensionList)
    {
        try
        {
            HashSet<Integer> requiredDims = new HashSet<Integer>(requiredDimensionList);
            HashMap<Integer, DimensionProvider> dimProviders = new HashMap<Integer, DimensionProvider>();

            Level logLevel = Level.DEBUG;
            Journeymap.getLogger().log(logLevel, String.format("Required dimensions from waypoints: %s", requiredDimensionList));

            // DimensionIDs works for local servers
            Integer[] dims = DimensionManager.getIDs();
            Journeymap.getLogger().log(logLevel, String.format("DimensionManager has dims: %s", Arrays.asList(dims)));
            requiredDims.addAll(Arrays.asList(dims));

            // StaticDimensionIDs works for remote servers
            dims = DimensionManager.getStaticDimensionIDs();
            Journeymap.getLogger().log(logLevel, String.format("DimensionManager has static dims: %s", Arrays.asList(dims)));
            requiredDims.addAll(Arrays.asList(dims));

            // Use the player's provider
            WorldProvider playerProvider = FMLClientHandler.instance().getClient().thePlayer.worldObj.provider;
            int dimId = ForgeHelper.INSTANCE.getDimension();
            DimensionProvider playerDimProvider = new WrappedProvider(playerProvider);
            dimProviders.put(dimId, playerDimProvider);
            requiredDims.remove(dimId);
            Journeymap.getLogger().log(logLevel, String.format("Using player's provider for dim %s: %s", dimId, getSafeDimensionName(playerDimProvider)));

            // Get a provider for the rest
            for (int dim : requiredDims)
            {
                if (!dimProviders.containsKey(dim))
                {
                    if (DimensionManager.getWorld(dim) != null)
                    {
                        try
                        {
                            WorldProvider worldProvider = DimensionManager.getProvider(dim);
                            worldProvider.getDimensionType().getName(); // Force the name error.
                            DimensionProvider dimProvider = new WrappedProvider(worldProvider);
                            dimProviders.put(dim, dimProvider);
                            Journeymap.getLogger().log(logLevel, String.format("DimensionManager.getProvider(%s): %s", dim, getSafeDimensionName(dimProvider)));
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
                            provider.getDimensionType().getName(); // Force the name error
                            provider.setDimension(dim);
                            DimensionProvider dimProvider = new WrappedProvider(provider);
                            dimProviders.put(dim, dimProvider);
                            Journeymap.getLogger().log(logLevel, String.format("DimensionManager.createProviderFor(%s): %s", dim, getSafeDimensionName(dimProvider)));
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
                    dimProviders.put(dim, new DummyProvider(dim));
                    Journeymap.getLogger().warn(String.format("Used DummyProvider for required dim: %s", dim));
                }
            }

            // Sort by dim and return
            ArrayList<DimensionProvider> providerList = new ArrayList<DimensionProvider>(dimProviders.values());
            Collections.sort(providerList, new Comparator<DimensionProvider>()
            {
                @Override
                public int compare(DimensionProvider o1, DimensionProvider o2)
                {
                    return Integer.valueOf(o1.getDimension()).compareTo(o2.getDimension());
                }
            });

            return providerList;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Unexpected error in WorldData.getDimensionProviders(): ", t);
            return Collections.emptyList();
        }
    }

    public static String getSafeDimensionName(DimensionProvider dimensionProvider)
    {
        if (dimensionProvider == null || dimensionProvider.getName()==null)
        {
            return null;
        }

        try
        {
            return dimensionProvider.getName();
        }
        catch (Exception e)
        {
            return Constants.getString("jm.common.dimension", ForgeHelper.INSTANCE.getDimension());
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
        dimension = ForgeHelper.INSTANCE.getDimension();
        hardcore = worldInfo.isHardcoreModeEnabled();
        singlePlayer = !multiplayer;
        time = mc.theWorld.getWorldTime() % 24000L;
        features = FeatureManager.getAllowedFeatures();

        mod_name = JourneymapClient.MOD_NAME;
        jm_version = Journeymap.JM_VERSION.toString();
        latest_journeymap_version = VersionCheck.getVersionAvailable();
        mc_version = Display.getTitle().split("\\s(?=\\d)")[1];
        browser_poll = Math.max(1000, Journeymap.getClient().getCoreProperties().browserPoll.get());

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
     * Interface to abstract how ID and name are provided.
     */
    public static interface DimensionProvider
    {
        int getDimension();
        String getName();
    }

    /**
     * Wraps a world provider.
     */
    public static class WrappedProvider implements DimensionProvider
    {
        WorldProvider worldProvider;

        public WrappedProvider(WorldProvider worldProvider)
        {
            this.worldProvider = worldProvider;
        }

        @Override
        public int getDimension()
        {
            return worldProvider.getDimension();
        }

        @Override
        public String getName()
        {
            return worldProvider.getDimensionType().getName();
        }
    }

    /**
     * Stand-in for world provider that couldn't be found.
     */
    static class DummyProvider implements DimensionProvider
    {
        final int dim;

        DummyProvider(int dim)
        {
            this.dim = dim;
        }

        @Override
        public int getDimension()
        {
            return dim;
        }

        @Override
        public String getName()
        {
            return "Dimension " + dim;
        }
    }


}
