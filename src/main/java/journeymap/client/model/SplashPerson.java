/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import journeymap.client.Constants;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.component.Button;
import net.minecraft.client.gui.FontRenderer;

import java.util.Random;

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
    public int moveX;
    public int moveY;
    private int moveDistance = 1;

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
        randomizeVector();
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

    public void randomizeVector()
    {
        this.moveDistance = new Random().nextInt(2) + 1;
        this.moveX = new Random().nextBoolean() ? moveDistance : -moveDistance;
        this.moveY = new Random().nextBoolean() ? moveDistance : -moveDistance;
    }

    private void reverseX()
    {
        this.moveDistance = new Random().nextInt(2) + 1;
        this.moveX = (moveX < 0) ? moveDistance : -moveDistance;
    }

    private void reverseY()
    {
        this.moveDistance = new Random().nextInt(2) + 1;
        this.moveY = (moveY < 0) ? moveDistance : -moveDistance;
    }

    public void adjustVector(int screenWidth, int screenHeight)
    {
        if (button.xPosition <= moveDistance || button.xPosition + button.getWidth() >= screenWidth - moveDistance)
        {
            reverseX();
        }

        if (button.yPosition <= moveDistance || button.yPosition + button.getHeight() >= screenHeight - moveDistance)
        {
            reverseY();
        }
        button.xPosition += moveX;
        button.yPosition += moveY;
    }
}
