package net.techbrew.journeymap.data;

import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
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
import java.util.Arrays;
import java.util.Map;

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
        features = FeatureManager.getAllowedFeatures();

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

    public static Integer[] getDimensions()
    {
        Integer[] dims = DimensionManager.getIDs();
        if (dims.length == 0)
        {
            dims = new Integer[]{0, -1, 1};
        }
        Arrays.sort(dims);
        return dims;
    }

    /**
     * Get the name of the provided dimension.
     *
     * @param dimension
     * @return
     */
    public static String getDimensionName(final int dimension)
    {
        if (DimensionManager.getWorld(dimension) != null)
        {
            return DimensionManager.getProvider(dimension).getDimensionName();
        }

        World world = Minecraft.getMinecraft().theWorld;
        if (world != null && world.provider.dimensionId == dimension)
        {
            return world.provider.getDimensionName();
        }
        else
        {
            switch (dimension)
            {
                case 0:
                    return new WorldProviderSurface().getDimensionName();
                case 1:
                    return new WorldProviderEnd().getDimensionName();
                case -1:
                    return new WorldProviderHell().getDimensionName();
            }
        }

        return Integer.toString(dimension);
    }

    /**
     * Return length of time in millis data should be kept.
     */
    public long getTTL()
    {
        return 1000;
    }
}
