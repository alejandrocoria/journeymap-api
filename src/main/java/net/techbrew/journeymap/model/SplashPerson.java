/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.model;

import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.component.Button;

/**
 * Created by Mark on 5/8/2015.
 */
public class SplashPerson
{
    public final String name;
    public final String ign;
    public final String title;
    public Button button;
    public int width;

    public SplashPerson(String ign, String name, String titleKey)
    {
        this.ign = ign;
        this.name = name;
        this.title = Constants.getString(titleKey);
    }

    public Button getButton()
    {
        return button;
    }

    public void setButton(Button button)
    {
        this.button = button;
    }

    public TextureImpl getSkin()
    {
        return TextureCache.instance().getPlayerSkin(ign);
    }

    public int getWidth(FontRenderer fr)
    {
        width = fr.getStringWidth(title);
        String[] nameParts = name.trim().split(" ");
        for (String part : nameParts)
        {
            width = Math.max(width, fr.getStringWidth(part));
        }
        return width;
    }

    public void setWidth(int minWidth)
    {
        this.width = minWidth;
    }
}