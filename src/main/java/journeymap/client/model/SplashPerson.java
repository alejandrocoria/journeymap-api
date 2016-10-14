/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import journeymap.client.Constants;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.component.Button;
import net.minecraft.client.gui.FontRenderer;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

/**
 * Created by Mark on 5/8/2016.
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
        if (titleKey != null)
        {
            this.title = Constants.getString(titleKey);
        }
        else
        {
            this.title = "";
        }
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
        return TextureCache.getPlayerSkin(ign);
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

    public void avoid(List<SplashPerson> devs)
    {
        for (SplashPerson dev : devs)
        {
            if (this == dev)
            {
                continue;
            }

            Rectangle2D thisBounds = new Rectangle2D.Double(button.getX(), button.getY(), button.width, button.height);
            Rectangle2D thatBounds = new Rectangle2D.Double(dev.button.getX(), dev.button.getY(), dev.button.width, dev.button.height);
            if (thisBounds.intersects(thatBounds))
            {
                this.moveDistance *= 2;
                if (new Random().nextBoolean())
                {
                    reverseX();
                }
                else
                {
                    reverseY();
                }
                break;
            }
        }
    }

    public static class Fake extends SplashPerson
    {
        private TextureImpl texture;

        public Fake(String name, String title, TextureImpl texture)
        {
            super(name, title, null);
            this.texture = texture;
        }

        public TextureImpl getSkin()
        {
            return texture;
        }
    }
}
