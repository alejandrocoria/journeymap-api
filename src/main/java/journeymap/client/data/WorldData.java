/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.data;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.log.JMLogger;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.version.VersionCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.Display;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


/**
 * Provides world properties
 *
 * @author techbrew
 */
public class WorldData extends CacheLoader<Class, WorldData>
{
    private static String DAYTIME = Constants.getString("jm.theme.labelsource.gametime.day");
    private static String SUNRISE = Constants.getString("jm.theme.labelsource.gametime.sunrise");
    private static String SUNSET = Constants.getString("jm.theme.labelsource.gametime.sunset");
    private static String NIGHT = Constants.getString("jm.theme.labelsource.gametime.night");

    /**
     * The Name.
     */
    String name;
    /**
     * The Dimension.
     */
    int dimension;
    /**
     * The Time.
     */
    long time;
    /**
     * The Hardcore.
     */
    boolean hardcore;
    /**
     * The Single player.
     */
    boolean singlePlayer;
    /**
     * The Features.
     */
    Map<Feature, Boolean> features;
    /**
     * The Jm version.
     */
    String jm_version;
    /**
     * The Latest journeymap version.
     */
    String latest_journeymap_version;
    /**
     * The Mc version.
     */
    String mc_version;
    /**
     * The Mod name.
     */
    String mod_name = JourneymapClient.MOD_NAME;
    /**
     * The Icon set name.
     */
    String iconSetName;
    /**
     * The Icon set names.
     */
    String[] iconSetNames;
    /**
     * The Browser poll.
     */
    int browser_poll;

    /**
     * Constructor.
     */
    public WorldData()
    {
    }

    /**
     * Is hardcore and multiplayer boolean.
     *
     * @return the boolean
     */
    public static boolean isHardcoreAndMultiplayer()
    {
        WorldData world = DataCache.INSTANCE.getWorld(false);
        return world.hardcore && !world.singlePlayer;
    }

    private static String getServerName()
    {
        try
        {
            String serverName = null;
            Minecraft mc = FMLClientHandler.instance().getClient();
            if (!mc.isSingleplayer())
            {
                try
                {
                    NetHandlerPlayClient netHandler = mc.getConnection();
                    GuiScreen netHandlerGui = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, netHandler, "field_147307_j", "guiScreenServer");

                    if (netHandlerGui instanceof GuiScreenRealmsProxy)
                    {
                        RealmsScreen realmsScreen = ((GuiScreenRealmsProxy) netHandlerGui).getProxy();
                        if (realmsScreen instanceof RealmsMainScreen)
                        {
                            RealmsMainScreen mainScreen = (RealmsMainScreen) realmsScreen;
                            long selectedServerId = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "selectedServerId");
                            List<RealmsServer> mcoServers = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "mcoServers");
                            for (RealmsServer mcoServer : mcoServers)
                            {
                                if (mcoServer.id == selectedServerId)
                                {
                                    serverName = mcoServer.name;
                                    break;
                                }
                            }
                        }
                    }
                }
                catch (Throwable t)
                {
                    Journeymap.getLogger().error("Unable to get Realms server name: " + LogFormatter.toString(t));
                }
            }

            if (serverName != null)
            {
                return serverName;
            }
            else
            {
                mc = FMLClientHandler.instance().getClient();
                ServerData serverData = mc.getCurrentServerData(); // 1.8 getServerData()

                if (serverData != null)
                {
                    serverName = serverData.serverName;
                    if (serverName != null)
                    {
                        serverName = serverName.replaceAll("\\W+", "~").trim();

                        if (Strings.isNullOrEmpty(serverName.replaceAll("~", "")))
                        {
                            serverName = serverData.serverIP;
                        }
                        return serverName;
                    }
                }
            }
            return null;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Couldn't get service name: " + LogFormatter.toString(t));
            // Fallback
            return getLegacyServerName();
        }
    }

    /**
     * Gets legacy server name.
     *
     * @return the legacy server name
     */
    public static String getLegacyServerName()
    {
        try
        {
            NetworkManager netManager = FMLClientHandler.instance().getClientToServerNetworkManager();
            if (netManager != null)
            {
                SocketAddress socketAddress = netManager.getRemoteAddress();
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
     * @param mc            the mc
     * @param useLegacyName the use legacy name
     * @return world name
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
            worldName = mc.world.getWorldInfo().getWorldName();
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

    /**
     * Gets dimension providers.
     *
     * @param requiredDimensionList the required dimension list
     * @return the dimension providers
     */
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
            Minecraft mc = FMLClientHandler.instance().getClient();
            WorldProvider playerProvider = mc.player.world.provider;
            int dimId = mc.player.dimension;
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

    /**
     * Gets safe dimension name.
     *
     * @param dimensionProvider the dimension provider
     * @return the safe dimension name
     */
    public static String getSafeDimensionName(DimensionProvider dimensionProvider)
    {
        if (dimensionProvider == null || dimensionProvider.getName() == null)
        {
            return null;
        }

        try
        {
            return dimensionProvider.getName();
        }
        catch (Exception e)
        {
            Minecraft mc = FMLClientHandler.instance().getClient();
            return Constants.getString("jm.common.dimension", mc.world.provider.getDimension());
        }
    }

    public static String getDimension()
    {
        int dimId = Minecraft.getMinecraft().player.dimension;
        String dimName = getSafeDimensionName(new WorldData.WrappedProvider(FMLClientHandler.instance().getClient().player.world.provider));
        return dimName + " (" + dimId + ")";
    }

    @Override
    public WorldData load(Class aClass) throws Exception
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        WorldInfo worldInfo = mc.world.getWorldInfo();

        IntegratedServer server = mc.getIntegratedServer();
        boolean multiplayer = server == null || server.getPublic();

        name = getWorldName(mc, false);
        dimension = mc.world.provider.getDimension();
        hardcore = worldInfo.isHardcoreModeEnabled();
        singlePlayer = !multiplayer;
        time = mc.world.getWorldTime() % 24000L;
        features = FeatureManager.getAllowedFeatures();

        mod_name = JourneymapClient.MOD_NAME;
        jm_version = Journeymap.JM_VERSION.toString();
        latest_journeymap_version = VersionCheck.getVersionAvailable();
        mc_version = Display.getTitle().split("\\s(?=\\d)")[1];
        browser_poll = Math.max(1000, Journeymap.getClient().getCoreProperties().browserPoll.get());

        return this;
    }

    public static String getLightLevel()
    {
        BlockPos blockpos = Minecraft.getMinecraft().player.getPosition();
        World world = Minecraft.getMinecraft().world;
        Chunk chunk = world.getChunkFromBlockCoords(blockpos);
        int light = chunk.getLightSubtracted(blockpos, 0);
        int lightSky = chunk.getLightFor(EnumSkyBlock.SKY, blockpos);
        int lightBlock = chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos);
        String lightLevels = String.format("Light: %s (%s sky, %s block)", light, lightSky, lightBlock);
        return lightLevels;
    }

    public static String getRegion()
    {
        BlockPos blockpos = Minecraft.getMinecraft().player.getPosition();
        Chunk chunk = Minecraft.getMinecraft().world.getChunkFromBlockCoords(blockpos);
        RegionCoord regionCoord = RegionCoord.fromChunkPos(null, MapType.none(), chunk.x, chunk.z);
        return "Region: x:" + regionCoord.regionX + " z:" + regionCoord.regionZ;
    }

    // TODO: clean up!
    // This is some pretty ugly code. Needs to be cleaned up!
    public static String getTime(String format)
    {
        long time = (FMLClientHandler.instance().getClient().world.getWorldTime() % 24000L);
        final int ticksAtMidnight = 18000;
        final int ticksPerDay = 24000;
        final int ticksPerHour = 1000;
        final double ticksPerMinute = 1000d / 60d;
        final double ticksPerSecond = 1000d / 60d / 60d;
        final int offset = 6000;
        time = time - ticksAtMidnight + ticksPerDay + offset;
        time -= (time / ticksPerDay) * ticksPerDay;
        final long hours = (time / ticksPerHour);
        time -= (time / ticksPerHour) * ticksPerHour;
        final long minutes = (long) Math.floor(time / ticksPerMinute);
        final double dticks = time - minutes * ticksPerMinute;
        final long seconds = (long) Math.floor(dticks / ticksPerSecond);
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
        cal.setLenient(true);
        cal.set(0, Calendar.JANUARY, 1, 0, 0, 0);
        cal.add(Calendar.DAY_OF_YEAR, (int) (time / ticksPerDay));
        cal.add(Calendar.HOUR_OF_DAY, (int) hours);
        cal.add(Calendar.MINUTE, (int) minutes);
        cal.add(Calendar.SECOND, (int) seconds);
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        return sdf.format(date);
    }

    /**
     * 0 is the start of daytime, 12000 is the start of sunset, 13800 is
     * the start of nighttime, 22200 is the start of sunrise, and 24000 is daytime again.
     */
    public static String getGameTime()
    {
        long worldTime = (FMLClientHandler.instance().getClient().world.getWorldTime() % 24000L);
        String label;
        if (worldTime < 12000)
        {
            label = DAYTIME;
        }
        else if (worldTime < 13800)
        {
            label = SUNSET;
        }
        else if (worldTime < 22200)
        {
            label = NIGHT;
        }
        else
        {
            label = SUNRISE;
        }

        long allSecs = worldTime / 20;
        return String.format("%02d:%02d %s", (long) Math.floor(allSecs / 60), (long) Math.ceil(allSecs % 60), label);
    }

    public static boolean isDay(long worldTime)
    {
        return (worldTime % 24000L) < 13800;
    }

    public static boolean isNight(long worldTime)
    {
        return (worldTime % 24000L) >= 13800;
    }

    /**
     * Return length of time in millis data should be kept.
     *
     * @return the ttl
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
        /**
         * Gets dimension.
         *
         * @return the dimension
         */
        int getDimension();

        /**
         * Gets name.
         *
         * @return the name
         */
        String getName();
    }

    /**
     * Wraps a world provider.
     */
    public static class WrappedProvider implements DimensionProvider
    {
        /**
         * The World provider.
         */
        WorldProvider worldProvider;

        /**
         * Instantiates a new Wrapped provider.
         *
         * @param worldProvider the world provider
         */
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
        /**
         * The Dim.
         */
        final int dim;

        /**
         * Instantiates a new Dummy provider.
         *
         * @param dim the dim
         */
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
