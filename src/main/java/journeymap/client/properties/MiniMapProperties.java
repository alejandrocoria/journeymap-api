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
import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;

import static journeymap.common.properties.Category.Inherit;

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
    protected final transient int id;
    protected boolean active = false;

    public MiniMapProperties(int id)
    {
        this.id = id;
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
        return id;
    }

    @Override
    protected <T extends PropertiesBase> void updateFrom(T otherInstance)
    {
        super.updateFrom(otherInstance);
        if (otherInstance instanceof MiniMapProperties)
        {
            setActive(((MiniMapProperties) otherInstance).isActive());
        }
    }

    /**
     * Gets the size relative to current screen height.
     */
    public int getSize()
    {
        return (int) Math.max(128, Math.floor((sizePercent.get() / 100.0) * ForgeHelper.INSTANCE.getClient().displayHeight));
    }

    @Override
    public void newFileInit()
    {
        super.newFileInit();
        if (getId() == 1)
        {
            this.setActive(true);
            if (ForgeHelper.INSTANCE.getFontRenderer().getUnicodeFlag())
            {
                super.fontScale.set(2);
                compassFontScale.set(2);
            }
        }
        else
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
    }
}
