/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.IconSetFileHandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Properties for basic mod configuration.
 */
public class CoreProperties extends PropertiesBase implements Comparable<CoreProperties>
{
    protected transient static final int CODE_REVISION = 3;
    public final AtomicReference<String> logLevel = new AtomicReference<String>("INFO");
    public final AtomicInteger chunkOffset = new AtomicInteger(8);
    public final AtomicInteger entityPoll = new AtomicInteger(1800);
    public final AtomicInteger playerPoll = new AtomicInteger(1900);
    public final AtomicInteger chunkPoll = new AtomicInteger(2000);
    public final AtomicInteger autoMapPoll = new AtomicInteger(2000);
    public final AtomicInteger cacheAnimalsData = new AtomicInteger(3100);
    public final AtomicInteger maxAnimalsData = new AtomicInteger(32);
    public final AtomicInteger cacheMobsData = new AtomicInteger(3000);
    public final AtomicInteger maxMobsData = new AtomicInteger(32);
    public final AtomicInteger cachePlayerData = new AtomicInteger(1000);
    public final AtomicInteger cachePlayersData = new AtomicInteger(2000);
    public final AtomicInteger maxPlayersData = new AtomicInteger(32);
    public final AtomicInteger cacheVillagersData = new AtomicInteger(2200);
    public final AtomicInteger maxVillagersData = new AtomicInteger(32);
    public final AtomicBoolean announceMod = new AtomicBoolean(true);
    public final AtomicBoolean checkUpdates = new AtomicBoolean(true);
    public final AtomicBoolean caveIgnoreGlass = new AtomicBoolean(true);
    public final AtomicBoolean recordCacheStats = new AtomicBoolean(false);
    public final AtomicBoolean mapBathymetry = new AtomicBoolean(false);
    public final AtomicBoolean mapTransparency = new AtomicBoolean(true);
    public final AtomicBoolean mapCaveLighting = new AtomicBoolean(true);
    public final AtomicBoolean mapAntialiasing = new AtomicBoolean(true);
    public final AtomicBoolean mapPlantShadows = new AtomicBoolean(false);
    public final AtomicBoolean mapPlants = new AtomicBoolean(true);
    public final AtomicBoolean mapCrops = new AtomicBoolean(true);
    public final AtomicBoolean mapSurfaceAboveCaves = new AtomicBoolean(true);
    public final AtomicReference<String> skinIconSetName = new AtomicReference<String>("Victorian1_0");
    protected transient final String name = "core";

    public CoreProperties()
    {
    }

    @Override
    protected boolean validate()
    {
        boolean saveNeeded = super.validate();

        List<String> validNames = IconSetFileHandler.getSkinIconSetNames();
        if (skinIconSetName.get() == null || !validNames.contains(skinIconSetName.get()))
        {
            JourneyMap.getLogger().warn(String.format("Skin Icon Set name '%s' is not valid, will use default instead.", skinIconSetName.get()));
            skinIconSetName.set(validNames.get(0));
            saveNeeded = true;
        }

        return saveNeeded;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getCodeRevision()
    {
        return CODE_REVISION;
    }

    @Override
    public int compareTo(CoreProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        CoreProperties that = (CoreProperties) o;
        return 0 == that.compareTo(this);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + fileRevision;
        result = 31 * result + logLevel.hashCode();
        result = 31 * result + chunkOffset.hashCode();
        result = 31 * result + entityPoll.hashCode();
        result = 31 * result + playerPoll.hashCode();
        result = 31 * result + chunkPoll.hashCode();
        result = 31 * result + cacheAnimalsData.hashCode();
        result = 31 * result + maxAnimalsData.hashCode();
        result = 31 * result + cacheMobsData.hashCode();
        result = 31 * result + maxMobsData.hashCode();
        result = 31 * result + cachePlayerData.hashCode();
        result = 31 * result + cachePlayersData.hashCode();
        result = 31 * result + maxPlayersData.hashCode();
        result = 31 * result + cacheVillagersData.hashCode();
        result = 31 * result + maxVillagersData.hashCode();
        result = 31 * result + announceMod.hashCode();
        result = 31 * result + checkUpdates.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "CoreProperties{" +
                "name='" + name + '\'' +
                ", logLevel=" + logLevel +
                ", chunkOffset=" + chunkOffset +
                ", entityPoll=" + entityPoll +
                ", playerPoll=" + playerPoll +
                ", chunkPoll=" + chunkPoll +
                ", cacheAnimalsData=" + cacheAnimalsData +
                ", maxAnimalsData=" + maxAnimalsData +
                ", cacheMobsData=" + cacheMobsData +
                ", maxMobsData=" + maxMobsData +
                ", cachePlayerData=" + cachePlayerData +
                ", cachePlayersData=" + cachePlayersData +
                ", maxPlayersData=" + maxPlayersData +
                ", cacheVillagersData=" + cacheVillagersData +
                ", maxVillagersData=" + maxVillagersData +
                ", announceMod=" + announceMod +
                ", checkUpdates=" + checkUpdates +
                ", caveIgnoreGlass=" + caveIgnoreGlass +
                ", recordCacheStats=" + recordCacheStats +
                ", mapBathymetry=" + mapBathymetry +
                ", mapTransparency=" + mapTransparency +
                ", mapCaveLighting=" + mapCaveLighting +
                '}';
    }
}
