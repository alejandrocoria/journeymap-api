/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.io.IconSetFileHandler;
import net.techbrew.journeymap.ui.minimap.DisplayVars;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.techbrew.journeymap.properties.Config.Category.General;
import static net.techbrew.journeymap.properties.Config.Category.MiniMap;

/**
 * Properties for the minimap in-game.
 */
public class MiniMapProperties extends InGameMapProperties
{
    protected transient static final int CODE_REVISION = 6;

    @Config(category = General, key = "jm.minimap.enable_minimap")
    public final AtomicBoolean enabled = new AtomicBoolean(true);

    @Config(category = MiniMap, key = "jm.minimap.shape")
    public final AtomicReference<DisplayVars.Shape> shape = new AtomicReference<DisplayVars.Shape>(DisplayVars.Shape.Square);

    @Config(category = MiniMap, key = "jm.minimap.position")
    public final AtomicReference<DisplayVars.Position> position = new AtomicReference<DisplayVars.Position>(DisplayVars.Position.TopRight);

    @Config(category = MiniMap, key = "jm.minimap.show_fps", defaultBoolean = false)
    public final AtomicBoolean showFps = new AtomicBoolean(false);

    @Config(category = MiniMap, key = "jm.minimap.show_biome")
    public final AtomicBoolean showBiome = new AtomicBoolean(true);

    @Config(category = MiniMap, key = "jm.minimap.show_location")
    public final AtomicBoolean showLocation = new AtomicBoolean(true);

    @Config(category = MiniMap, key = "jm.minimap.hotkeys")
    public final AtomicBoolean enableHotkeys = new AtomicBoolean(true);

    @Config(category = MiniMap, key = "jm.minimap.show_waypointlabels")
    public final AtomicBoolean showWaypointLabels = new AtomicBoolean(true);

    @Config(category = MiniMap, key = "jm.minimap.size", minInt = 128, maxInt = 768, defaultInt = 192)
    public final AtomicInteger customSize = new AtomicInteger(192);

    @Config(category = MiniMap, key = "jm.minimap.frame_alpha", minInt = 0, maxInt = 255, defaultInt = 255)
    public final AtomicInteger frameAlpha = new AtomicInteger(255);

    @Config(category = MiniMap, key = "jm.minimap.terrain_alpha", minInt = 0, maxInt = 255, defaultInt = 255)
    public final AtomicInteger terrainAlpha = new AtomicInteger(255);

    @Config(category = MiniMap, key = "jm.minimap.orientation.button", defaultEnum = "North")
    public final AtomicReference<DisplayVars.Orientation> orientation = new AtomicReference<DisplayVars.Orientation>(DisplayVars.Orientation.North);

    @Config(category = MiniMap, key = "jm.minimap.compass_font")
    public final AtomicBoolean compassFontSmall = new AtomicBoolean(true);

    @Config(category = MiniMap, key = "jm.minimap.show_compass")
    public final AtomicBoolean showCompass = new AtomicBoolean(true);

    @Config(category = MiniMap, key = "jm.minimap.show_reticle")
    public final AtomicBoolean showReticle = new AtomicBoolean(true);

    @Config(category = MiniMap, key = "jm.minimap.reticle_orientation")
    public final AtomicReference<DisplayVars.ReticleOrientation> reticleOrientation = new AtomicReference<DisplayVars.ReticleOrientation>(DisplayVars.ReticleOrientation.Compass);

    @Config(category = MiniMap, key = "jm.common.mob_icon_set", stringListProvider = IconSetFileHandler.IconSetStringListProvider.class)
    public final AtomicReference<String> entityIconSetName = new AtomicReference<String>("2D");

    public final AtomicReference<Constants.MapType> preferredMapType = new AtomicReference<Constants.MapType>(Constants.MapType.day);

    protected transient final String name = "minimap";

    public MiniMapProperties()
    {
    }

    @Override
    public AtomicReference<String> getEntityIconSetName()
    {
        return entityIconSetName;
    }

    @Override
    public AtomicReference<Constants.MapType> getPreferredMapType()
    {
        return preferredMapType;
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
    protected boolean validate()
    {
        boolean saveNeeded = super.validate();

        if (frameAlpha.get() < 0)
        {
            frameAlpha.set(0);
            saveNeeded = true;
        }
        else if (frameAlpha.get() > 255)
        {
            frameAlpha.set(255);
            saveNeeded = true;
        }

        if (customSize.get() == 0)
        {
            this.customSize.set(256);
            saveNeeded = true;
        }

        if (customSize.get() < 128)
        {
            customSize.set(128);
            saveNeeded = true;
        }

        if (customSize.get() > 768)
        {
            customSize.set(768);
            saveNeeded = true;
        }

        if (terrainAlpha.get() < 0)
        {
            terrainAlpha.set(0);
            saveNeeded = true;
        }
        else if (terrainAlpha.get() > 255)
        {
            terrainAlpha.set(255);
            saveNeeded = true;
        }

        return saveNeeded;
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
        if (!super.equals(o))
        {
            return false;
        }

        MiniMapProperties that = (MiniMapProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (enabled != null ? enabled.hashCode() : 0);
        result = 31 * result + (shape != null ? shape.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (showFps != null ? showFps.hashCode() : 0);
        result = 31 * result + (showBiome != null ? showBiome.hashCode() : 0);
        result = 31 * result + (showLocation != null ? showLocation.hashCode() : 0);
        result = 31 * result + (enableHotkeys != null ? enableHotkeys.hashCode() : 0);
        result = 31 * result + showWaypointLabels.hashCode();
        result = 31 * result + customSize.hashCode();
        result = 31 * result + frameAlpha.hashCode();
        result = 31 * result + terrainAlpha.hashCode();
        result = 31 * result + orientation.hashCode();
        result = 31 * result + compassFontSmall.hashCode();
        result = 31 * result + showCompass.hashCode();
        result = 31 * result + showReticle.hashCode();
        result = 31 * result + reticleOrientation.hashCode();
        result = 31 * result + entityIconSetName.hashCode();
        result = 31 * result + preferredMapType.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "MiniMapProperties: " +
                "fileRevision=" + fileRevision +
                ", showSelf=" + showSelf +
                ", showMobs=" + showMobs +
                ", showAnimals=" + showAnimals +
                ", showVillagers=" + showVillagers +
                ", showPets=" + showPets +
                ", showPlayers=" + showPlayers +
                ", showWaypoints=" + showWaypoints +
                ", managerEnabled=" + enabled +
                ", shape=" + shape +
                ", position=" + position +
                ", showFps=" + showFps +
                ", enableHotkeys=" + enableHotkeys +
                ", showWaypointLabels=" + showWaypointLabels +
                ", forceUnicode=" + forceUnicode +
                ", fontSmall=" + fontSmall +
                ", textureSmall=" + textureSmall +
                ", terrainAlpha=" + terrainAlpha +
                ", entityIconSetName=" + entityIconSetName;
    }
}
