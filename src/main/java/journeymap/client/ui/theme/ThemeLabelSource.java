/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.client.data.WorldData;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.option.KeyedEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Supplier;

/**
 * Enum for the source of the label text.  Each enum value provides an implementation of getLabelText() matching its description.
 * Uses caching times to avoid unnecessary processing / string manipulation.
 */
public enum ThemeLabelSource implements KeyedEnum
{
    FPS("jm.theme.labelsource.fps", 100, 1, ThemeLabelSource::getFps),
    GameTime("jm.theme.labelsource.gametime", 0, 1000, ThemeLabelSource::getGameTime),
    GameTime12("jm.theme.labelsource.gametime12", 0, 1000, ThemeLabelSource::getGameTime12h),
    GameTime24("jm.theme.labelsource.gametime24", 0, 1000, ThemeLabelSource::getGameTime24h),
    RealTime("jm.theme.labelsource.realtime", 0, 1000, ThemeLabelSource::getRealTime),
    Location("jm.theme.labelsource.location", 1000, 1, ThemeLabelSource::getLocation),
    Biome("jm.theme.labelsource.biome", 1000, 1, ThemeLabelSource::getBiome),
    Dimension("jm.theme.labelsource.dimension", 1000, 1, ThemeLabelSource::getDimension),
    Region("jm.theme.labelsource.region", 1000, 1, ThemeLabelSource::getRegion),
    LightLevel("jm.theme.labelsource.lightlevel", 100, 100, ThemeLabelSource::getLightLevel),
    Blank("jm.theme.labelsource.blank", 0, 1, () -> "");

    private static DateFormat timeFormat = new SimpleDateFormat("h:mm:ss a");

    private final String key;
    private final Supplier<String> supplier;
    private final long cacheMillis;
    private final long granularityMillis;
    private long lastCallTime;
    private String lastValue = "";

    ThemeLabelSource(String key, long cacheMillis, long granularityMillis, Supplier<String> supplier)
    {
        this.key = key;
        this.cacheMillis = cacheMillis;
        this.granularityMillis = granularityMillis;
        this.supplier = supplier;
    }

    /**
     * Reset cached values and times.
     */
    public static void resetCaches()
    {
        for (ThemeLabelSource source : values())
        {
            source.lastCallTime = 0;
            source.lastValue = "";
        }
    }

    /**
     * Get the text for the label corresponding to the enum name.
     *
     * @return
     */
    public String getLabelText(long currentTimeMillis)
    {
        try
        {
            // Effectively rounds/truncates the time by granularityMillis
            long now = granularityMillis * (currentTimeMillis / granularityMillis);
            if (now - lastCallTime <= cacheMillis)
            {
                return lastValue;
            }
            lastCallTime = now;
            lastValue = supplier.get();
            return lastValue;
        }
        catch (Exception e)
        {
            return "?";
        }
    }

    public boolean isShown()
    {
        return this != Blank;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return Constants.getString(this.key);
    }

    private static String getFps()
    {
        return Minecraft.getDebugFPS() + " fps";
    }

    private static String getGameTime()
    {
        return WorldData.getGameTime();
    }

    private static String getGameTime12h()
    {
        return getTime("h:mm:ss aa");
    }

    private static String getGameTime24h()
    {
        return getTime("HH:mm:ss");
    }


    //: TODO clean up!
    // This is some pretty ugly code. Needs to be cleaned up!
    private static String getTime(String format)
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

    private static String getRealTime()
    {
        return timeFormat.format(new Date());
    }

    private static String getLocation()
    {
        return UIManager.INSTANCE.getMiniMap().getLocation();
    }

    private static String getBiome()
    {
        return UIManager.INSTANCE.getMiniMap().getBiome();
    }

    private static String getDimension()
    {
        return "Dim: " + Minecraft.getMinecraft().player.dimension;
    }

    private static String getLightLevel()
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

    private static String getRegion()
    {
        BlockPos blockpos = Minecraft.getMinecraft().player.getPosition();
        Chunk chunk = Minecraft.getMinecraft().world.getChunkFromBlockCoords(blockpos);
        RegionCoord regionCoord = RegionCoord.fromChunkPos(null, MapType.none(), chunk.x, chunk.z);
        return "Region: x:" + regionCoord.regionX + " z:" + regionCoord.regionZ;
    }
}
