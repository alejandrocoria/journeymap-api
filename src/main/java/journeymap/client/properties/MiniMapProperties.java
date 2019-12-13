/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */
package journeymap.client.properties;


import journeymap.client.ui.minimap.Orientation;
import journeymap.client.ui.minimap.Position;
import journeymap.client.ui.minimap.ReticleOrientation;
import journeymap.client.ui.minimap.Shape;
import journeymap.client.ui.option.TimeFormat;
import journeymap.client.ui.theme.ThemeLabelSource;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;
import net.minecraftforge.fml.client.FMLClientHandler;

import static journeymap.common.properties.Category.Inherit;

/**
 * Properties for the minimap in-game.
 */
public class MiniMapProperties extends InGameMapProperties
{

    public final StringField gameTimeRealFormat = new StringField(Inherit, "jm.common.time_format", TimeFormat.Provider.class);

    public final StringField systemTimeRealFormat = new StringField(Inherit, "jm.common.system_time_format", TimeFormat.Provider.class);

    /**
     * The Enabled.
     */
    public final BooleanField enabled = new BooleanField(Inherit, "jm.minimap.enable_minimap", true, true);
    /**
     * The Shape.
     */
    public final EnumField<Shape> shape = new EnumField<Shape>(Inherit, "jm.minimap.shape", Shape.Circle);
    /**
     * The Position.
     */
    public final EnumField<Position> position = new EnumField<Position>(Inherit, "jm.minimap.position", Position.TopRight);

    /**
     * Whether to auto-switch between day and night.
     */
    public final BooleanField showDayNight = new BooleanField(Inherit, "jm.common.show_day_night", true);

    /**
     * Info Slot 1
     */
    public final EnumField<ThemeLabelSource> info1Label = new EnumField<>(Inherit, "jm.minimap.info1_label.button", ThemeLabelSource.Blank);

    /**
     * Info Slot 2
     */
    public final EnumField<ThemeLabelSource> info2Label = new EnumField<>(Inherit, "jm.minimap.info2_label.button", ThemeLabelSource.GameTime);

    /**
     * Info Slot 3
     */
    public final EnumField<ThemeLabelSource> info3Label = new EnumField<>(Inherit, "jm.minimap.info3_label.button", ThemeLabelSource.Location);

    /**
     * Info Slot 4
     */
    public final EnumField<ThemeLabelSource> info4Label = new EnumField<>(Inherit, "jm.minimap.info4_label.button", ThemeLabelSource.Biome);

    /**
     * The Size percent.
     */
    public final IntegerField sizePercent = new IntegerField(Inherit, "jm.minimap.size", 1, 100, 30);
    /**
     * The Frame alpha in percent
     */
    public final IntegerField frameAlpha = new IntegerField(Inherit, "jm.minimap.frame_alpha", 0, 100, 100);
    /**
     * The Terrain alpha in percent
     */
    public final IntegerField terrainAlpha = new IntegerField(Inherit, "jm.minimap.terrain_alpha", 0, 100, 100);
    /**
     * The Orientation.
     */
    public final EnumField<Orientation> orientation = new EnumField<Orientation>(Inherit, "jm.minimap.orientation.button", Orientation.North);
    /**
     * The Compass font scale.
     */
    public final IntegerField compassFontScale = new IntegerField(Inherit, "jm.minimap.compass_font_scale", 1, 4, 1);
    /**
     * The Show compass.
     */
    public final BooleanField showCompass = new BooleanField(Inherit, "jm.minimap.show_compass", true);
    /**
     * The Show reticle.
     */
    public final BooleanField showReticle = new BooleanField(Inherit, "jm.minimap.show_reticle", true);
    /**
     * The Reticle orientation.
     */
    public final EnumField<ReticleOrientation> reticleOrientation = new EnumField<ReticleOrientation>(Inherit, "jm.minimap.reticle_orientation", ReticleOrientation.Compass);

    /**
     * The Id.
     */
    protected final transient int id;
    /**
     * The Active.
     */
    protected boolean active = false;

    /**
     * Instantiates a new Mini map properties.
     *
     * @param id the id
     */
    public MiniMapProperties(int id)
    {
        this.id = id;
    }

    @Override
    public String getName()
    {
        return String.format("minimap%s", (id > 1) ? id : "");
    }

    /**
     * Is active boolean.
     *
     * @return the boolean
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * Sets active.
     *
     * @param active the active
     */
    public void setActive(boolean active)
    {
        if (this.active != active)
        {
            this.active = active;
            save();
        }
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    @Override
    public <T extends PropertiesBase> void updateFrom(T otherInstance)
    {
        super.updateFrom(otherInstance);
        if (otherInstance instanceof MiniMapProperties)
        {
            setActive(((MiniMapProperties) otherInstance).isActive());
        }
    }

    /**
     * Gets the size relative to current screen height.
     *
     * @return the size
     */
    public int getSize()
    {
        return (int) Math.max(128, Math.floor((sizePercent.get() / 100.0) * FMLClientHandler.instance().getClient().displayHeight));
    }

    @Override
    protected void postLoad(boolean isNew)
    {
        super.postLoad(isNew);

        if (isNew)
        {
            if (getId() == 1)
            {
                this.setActive(true);
                if (FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().fontRenderer.getUnicodeFlag())
                {
                    super.fontScale.set(2);
                    compassFontScale.set(2);
                }
            }
            else
            {
                // Initial settings to give people an idea of what can be done
                this.setActive(false);
                this.position.set(Position.TopRight);
                this.shape.set(Shape.Rectangle);
                this.frameAlpha.set(100);
                this.terrainAlpha.set(100);
                this.orientation.set(Orientation.North);
                this.reticleOrientation.set(ReticleOrientation.Compass);
                this.sizePercent.set(30);
                if (FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().fontRenderer.getUnicodeFlag())
                {
                    super.fontScale.set(2);
                    compassFontScale.set(2);
                }

            }
        }
    }
}
