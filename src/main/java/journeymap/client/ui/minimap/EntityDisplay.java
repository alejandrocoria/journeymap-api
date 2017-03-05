/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import com.google.common.base.Strings;
import journeymap.client.Constants;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.option.KeyedEnum;
import net.minecraft.util.ResourceLocation;

/**
 * Enum for showing mobs as icons or dots
 */
public enum EntityDisplay implements KeyedEnum
{
    /**
     * Small dots entity display.
     */
    SmallDots("jm.common.entity_display.small_dots"),
    /**
     * Large dots entity display.
     */
    LargeDots("jm.common.entity_display.large_dots"),
    /**
     * Small icons entity display.
     */
    SmallIcons("jm.common.entity_display.small_icons"),
    /**
     * Large icons entity display.
     */
    LargeIcons("jm.common.entity_display.large_icons");

    /**
     * The Key.
     */
    public final String key;

    EntityDisplay(String key)
    {
        this.key = key;
    }

    /**
     * Gets locator texture.
     *
     * @param entityDisplay the entity display
     * @param showHeading   the show heading
     * @return the locator texture
     */
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

    /**
     * Gets entity texture.
     *
     * @param entityDisplay the entity display
     * @return the entity texture
     */
    public static TextureImpl getEntityTexture(EntityDisplay entityDisplay)
    {
        return getEntityTexture(entityDisplay, (String) null);
    }

    /**
     * Gets entity texture.
     *
     * @param entityDisplay the entity display
     * @param playerName    the player name
     * @return the entity texture
     */
    public static TextureImpl getEntityTexture(EntityDisplay entityDisplay, String playerName)
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
            return TextureCache.getPlayerSkin(playerName);
        }

        return null;
    }

    /**
     * Gets entity texture.
     *
     * @param entityDisplay the entity display
     * @param iconLocation  the icon location
     * @return the entity texture
     */
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

    /**
     * Is dots boolean.
     *
     * @return the boolean
     */
    public boolean isDots()
    {
        return this == LargeDots || this == SmallDots;
    }

    /**
     * Is large boolean.
     *
     * @return the boolean
     */
    public boolean isLarge()
    {
        return this == LargeDots || this == LargeIcons;
    }
}
