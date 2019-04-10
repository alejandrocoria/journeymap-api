/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.client.data.WorldData;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.option.KeyedEnum;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.DimensionManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        return WorldData.getTime("h:mm:ss aa");
    }

    private static String getGameTime24h()
    {
        return WorldData.getTime("HH:mm:ss");
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
        int dimId = Minecraft.getMinecraft().player.dimension;
        String dimName = DimensionManager.getProvider(dimId).getDimensionType().getName();
        return dimName + " (" + dimId + ")";
    }

    private static String getLightLevel()
    {
        return WorldData.getLightLevel();
    }

    private static String getRegion()
    {
        return WorldData.getRegion();
    }
}
