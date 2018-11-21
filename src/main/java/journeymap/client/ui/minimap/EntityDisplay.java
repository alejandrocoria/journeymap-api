/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import com.google.common.base.Strings;
import journeymap.client.Constants;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.option.KeyedEnum;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

/**
 * Enum for showing mobs as icons or dots
 */
public enum EntityDisplay implements KeyedEnum
{
    SmallDots("jm.common.entity_display.small_dots"),
    LargeDots("jm.common.entity_display.large_dots"),
    SmallIcons("jm.common.entity_display.small_icons"),
    LargeIcons("jm.common.entity_display.large_icons");

    public final String key;

    EntityDisplay(String key)
    {
        this.key = key;
    }

    public static TextureImpl getLocatorTexture(EntityDisplay entityDisplay, boolean showHeading)
    {
        ResourceLocation texLocation = null;
        switch (entityDisplay)
        {
            case LargeDots:
            {
                texLocation = showHeading ? TextureCache.MobDotArrow_Large : TextureCache.MobDot_Large;
                break;
            }
            case SmallDots:
            {
                texLocation = showHeading ? TextureCache.MobDotArrow : TextureCache.MobDot;
                break;
            }
            case LargeIcons:
            {
                texLocation = showHeading ? TextureCache.MobIconArrow_Large : null;
                break;
            }
            case SmallIcons:
            {
                texLocation = showHeading ? TextureCache.MobIconArrow : null;
                break;
            }
        }
        return TextureCache.getTexture(texLocation);
    }

    public static TextureImpl getEntityTexture(EntityDisplay entityDisplay)
    {
        return getEntityTexture(entityDisplay, (UUID) null, (String) null);
    }

    public static TextureImpl getEntityTexture(EntityDisplay entityDisplay, UUID entityId, String playerName)
    {
        switch (entityDisplay)
        {
            case LargeDots:
            {
                return TextureCache.getTexture(TextureCache.MobDotChevron_Large);
            }
            case SmallDots:
            {
                return TextureCache.getTexture(TextureCache.MobDotChevron);
            }
        }

        if (!Strings.isNullOrEmpty(playerName))
        {
            return TextureCache.getPlayerSkin(entityId, playerName);
        }

        return null;
    }

    public static TextureImpl getEntityTexture(EntityDisplay entityDisplay, ResourceLocation iconLocation)
    {
        switch (entityDisplay)
        {
            case LargeDots:
            {
                return TextureCache.getTexture(TextureCache.MobDotChevron_Large);
            }
            case SmallDots:
            {
                return TextureCache.getTexture(TextureCache.MobDotChevron);
            }
        }
        return TextureCache.getTexture(iconLocation);
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

    public boolean isDots()
    {
        return this == LargeDots || this == SmallDots;
    }

    public boolean isLarge()
    {
        return this == LargeDots || this == LargeIcons;
    }
}
