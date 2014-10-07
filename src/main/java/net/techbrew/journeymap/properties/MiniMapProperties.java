/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import com.google.common.base.Objects;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.ui.minimap.Orientation;
import net.techbrew.journeymap.ui.minimap.Position;
import net.techbrew.journeymap.ui.minimap.ReticleOrientation;
import net.techbrew.journeymap.ui.minimap.Shape;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.techbrew.journeymap.properties.Config.Category.Inherit;

/**
 * Properties for the minimap in-game.
 */
public class MiniMapProperties extends InGameMapProperties
{
    protected transient static final int CODE_REVISION = 8;

    @Config(category = Inherit, master = true, key = "jm.minimap.enable_minimap")
    public final AtomicBoolean enabled = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.shape", defaultEnum = "Circle")
    public final AtomicReference<Shape> shape = new AtomicReference<Shape>(Shape.Circle);

    @Config(category = Inherit, key = "jm.minimap.position", defaultEnum = "TopRight")
    public final AtomicReference<Position> position = new AtomicReference<Position>(Position.TopRight);

    @Config(category = Inherit, key = "jm.minimap.show_fps", defaultBoolean = false)
    public final AtomicBoolean showFps = new AtomicBoolean(false);

    @Config(category = Inherit, key = "jm.minimap.show_biome")
    public final AtomicBoolean showBiome = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.show_location")
    public final AtomicBoolean showLocation = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.hotkeys")
    public final AtomicBoolean enableHotkeys = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.show_waypointlabels")
    public final AtomicBoolean showWaypointLabels = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.size", minValue = 128, maxValue = 2048, defaultValue = 192)
    public final AtomicInteger customSize = new AtomicInteger(192);

    @Config(category = Inherit, key = "jm.minimap.frame_alpha", minValue = 0, maxValue = 100, defaultValue = 100)
    public final AtomicInteger frameAlpha = new AtomicInteger(100);

    @Config(category = Inherit, key = "jm.minimap.terrain_alpha", minValue = 0, maxValue = 100, defaultValue = 100)
    public final AtomicInteger terrainAlpha = new AtomicInteger(100);

    @Config(category = Inherit, key = "jm.minimap.orientation.button", defaultEnum = "North")
    public final AtomicReference<Orientation> orientation = new AtomicReference<Orientation>(Orientation.North);

    @Config(category = Inherit, key = "jm.minimap.compass_font", defaultBoolean = false)
    public final AtomicBoolean compassFontSmall = new AtomicBoolean(false);

    @Config(category = Inherit, key = "jm.minimap.show_compass")
    public final AtomicBoolean showCompass = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.show_reticle")
    public final AtomicBoolean showReticle = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.reticle_orientation", defaultEnum = "Compass")
    public final AtomicReference<ReticleOrientation> reticleOrientation = new AtomicReference<ReticleOrientation>(ReticleOrientation.Compass);

    public final AtomicReference<String> renderOverlayEventTypeName = new AtomicReference<String>(RenderGameOverlayEvent.ElementType.HOTBAR.name());
    public final AtomicBoolean renderOverlayPreEvent = new AtomicBoolean(true);

    public final AtomicReference<Constants.MapType> preferredMapType = new AtomicReference<Constants.MapType>(Constants.MapType.day);
    protected transient final String name;
    protected boolean active = true;

    public MiniMapProperties()
    {
        this("minimap");
    }

    protected MiniMapProperties(String name)
    {
        this.name = name;
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

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
        save();
    }

    public String getId()
    {
        return "1";
    }

    public RenderGameOverlayEvent.ElementType getRenderOverlayEventType()
    {
        return Enum.valueOf(RenderGameOverlayEvent.ElementType.class, renderOverlayEventTypeName.get());
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
        else if (frameAlpha.get() > 100)
        {
            frameAlpha.set(100);
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
        else if (terrainAlpha.get() > 100)
        {
            terrainAlpha.set(100);
            saveNeeded = true;
        }

        try
        {
            Enum.valueOf(RenderGameOverlayEvent.ElementType.class, renderOverlayEventTypeName.get());
        }
        catch (Exception e)
        {
            renderOverlayEventTypeName.set(RenderGameOverlayEvent.ElementType.HOTBAR.name());
            renderOverlayPreEvent.set(true);
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
        return Objects.toStringHelper(this)
                .add("enabled", enabled)
                .add("shape", shape)
                .add("position", position)
                .add("showFps", showFps)
                .add("showBiome", showBiome)
                .add("showLocation", showLocation)
                .add("enableHotkeys", enableHotkeys)
                .add("showWaypointLabels", showWaypointLabels)
                .add("customSize", customSize)
                .add("frameAlpha", frameAlpha)
                .add("terrainAlpha", terrainAlpha)
                .add("orientation", orientation)
                .add("compassFontSmall", compassFontSmall)
                .add("showCompass", showCompass)
                .add("showReticle", showReticle)
                .add("reticleOrientation", reticleOrientation)
                .add("preferredMapType", preferredMapType)
                .add("name", name)
                .toString();
    }
}
