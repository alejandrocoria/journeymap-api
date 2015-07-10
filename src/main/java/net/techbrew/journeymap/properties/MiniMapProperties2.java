/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import com.google.common.base.Objects;
import net.techbrew.journeymap.forge.helper.ForgeHelper;
import net.techbrew.journeymap.ui.minimap.Orientation;
import net.techbrew.journeymap.ui.minimap.Position;
import net.techbrew.journeymap.ui.minimap.ReticleOrientation;
import net.techbrew.journeymap.ui.minimap.Shape;

/**
 * Properties for the minimap in-game.
 */
public class MiniMapProperties2 extends MiniMapProperties
{
    public MiniMapProperties2()
    {
        super("minimap2");
    }

    @Override
    public void newFileInit()
    {
        // Initial settings to give people an idea of what can be done
        this.position.set(Position.TopCenter);
        this.shape.set(Shape.Rectangle);
        this.frameAlpha.set(60);
        this.terrainAlpha.set(60);
        this.orientation.set(Orientation.PlayerHeading);
        this.reticleOrientation.set(ReticleOrientation.Compass);
        this.sizePercent.set(30);
        if (ForgeHelper.INSTANCE.getFontRenderer().getUnicodeFlag())
        {
            super.fontScale.set(2);
            compassFontScale.set(2);
        }
        this.setActive(false);
    }

    @Override
    public int getId()
    {
        return 2;
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
                .add("showWaypointLabels", showWaypointLabels)
                .add("sizePercent", sizePercent)
                .add("frameAlpha", frameAlpha)
                .add("terrainAlpha", terrainAlpha)
                .add("orientation", orientation)
                .add("compassFontScale", compassFontScale)
                .add("showCompass", showCompass)
                .add("showReticle", showReticle)
                .add("reticleOrientation", reticleOrientation)
                .add("preferredMapType", preferredMapType)
                .add("name", name)
                .add("id", getId())
                .toString();
    }
}
