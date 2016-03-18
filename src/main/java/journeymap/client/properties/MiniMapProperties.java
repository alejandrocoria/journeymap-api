/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.ui.minimap.Orientation;
import journeymap.client.ui.minimap.Position;
import journeymap.client.ui.minimap.ReticleOrientation;
import journeymap.client.ui.minimap.Shape;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;

import static journeymap.common.properties.Category.*;

/**
 * Properties for the minimap in-game.
 */
public class MiniMapProperties extends InGameMapProperties
{
    public final BooleanField enabled = new BooleanField(Inherit, "jm.minimap.enable_minimap", true, true);
    public final EnumField<Shape> shape = new EnumField<Shape>(Inherit, "jm.minimap.shape", Shape.Circle);
    public final EnumField<Position> position = new EnumField<Position>(Inherit, "jm.minimap.position", Position.TopRight);
    public final BooleanField showFps = new BooleanField(Inherit, "jm.minimap.show_fps", false);
    public final BooleanField showBiome = new BooleanField(Inherit, "jm.minimap.show_biome", true);
    public final BooleanField showLocation = new BooleanField(Inherit, "jm.minimap.show_location", true);
    public final IntegerField sizePercent = new IntegerField(Inherit, "jm.minimap.size", 1, 100, 30);
    public final IntegerField frameAlpha = new IntegerField(Inherit, "jm.minimap.frame_alpha", 0, 100, 100);
    public final IntegerField terrainAlpha = new IntegerField(Inherit, "jm.minimap.terrain_alpha", 0, 100, 100);
    public final EnumField<Orientation> orientation = new EnumField<Orientation>(Inherit, "jm.minimap.orientation.button", Orientation.PlayerHeading);
    public final IntegerField compassFontScale = new IntegerField(Inherit, "jm.minimap.compass_font_scale", 1, 4, 1);
    public final BooleanField showCompass = new BooleanField(Inherit, "jm.minimap.show_compass", true);
    public final BooleanField showReticle = new BooleanField(Inherit, "jm.minimap.show_reticle", true);
    public final EnumField<ReticleOrientation> reticleOrientation = new EnumField<ReticleOrientation>(Inherit, "jm.minimap.reticle_orientation", ReticleOrientation.Compass);

    protected boolean active = false;

    public MiniMapProperties()
    {
    }

    @Override
    public String getName()
    {
        return "minimap";
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

    public int getId()
    {
        return 1;
    }

    /**
     * Gets the size relative to current screen height.
     *
     * @return
     */
    public int getSize()
    {
        return (int) Math.max(128, Math.floor((sizePercent.get() / 100.0) * ForgeHelper.INSTANCE.getClient().displayHeight));
    }

    @Override
    public void newFileInit()
    {
        this.setActive(true);
        if (ForgeHelper.INSTANCE.getFontRenderer().getUnicodeFlag())
        {
            super.fontScale.set(2);
            compassFontScale.set(2);
        }
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
        result = 31 * result + showWaypointLabels.hashCode();
        result = 31 * result + sizePercent.hashCode();
        result = 31 * result + frameAlpha.hashCode();
        result = 31 * result + terrainAlpha.hashCode();
        result = 31 * result + orientation.hashCode();
        result = 31 * result + compassFontScale.hashCode();
        result = 31 * result + showCompass.hashCode();
        result = 31 * result + showReticle.hashCode();
        result = 31 * result + reticleOrientation.hashCode();
        result = 31 * result + entityIconSetName.hashCode();
        result = 31 * result + preferredMapType.hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return super.toStringHelper(this)
                .add("active", active)
                .add("compassFontScale", compassFontScale)
                .add("enabled", enabled)
                .add("frameAlpha", frameAlpha)
                .add("name", getName())
                .add("orientation", orientation)
                .add("position", position)
                .add("preferredMapType", preferredMapType)
                .add("reticleOrientation", reticleOrientation)
                .add("shape", shape)
                .add("showBiome", showBiome)
                .add("showCompass", showCompass)
                .add("showFps", showFps)
                .add("showLocation", showLocation)
                .add("showReticle", showReticle)
                .add("sizePercent", sizePercent)
                .add("terrainAlpha", terrainAlpha)
                .toString();
    }

}
