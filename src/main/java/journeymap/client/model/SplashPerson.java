/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
 * @author techbrew 5/8/2016.
 */
public class SplashPerson
{
    /**
     * The Name.
     */
    public final String name;
    /**
     * The Ign.
     */
    public final String ign;
    /**
     * The Title.
     */
    public final String title;
    /**
     * The Button.
     */
    public Button button;
    /**
     * The Width.
     */
    public int width;
    /**
     * The Move x.
     */
    public int moveX;
    /**
     * The Move y.
     */
    public int moveY;
    private int moveDistance = 1;

    /**
     * Instantiates a new Splash person.
     *
     * @param ign      the ign
     * @param name     the name
     * @param titleKey the title key
     */
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

    /**
     * Gets button.
     *
     * @return the button
     */
    public Button getButton()
    {
        return button;
    }

    /**
     * Sets button.
     *
     * @param button the button
     */
    public void setButton(Button button)
    {
        this.button = button;
        randomizeVector();
    }

    /**
     * Gets skin.
     *
     * @return the skin
     */
    public TextureImpl getSkin()
    {
        return TextureCache.getPlayerSkin(ign);
    }

    /**
     * Gets width.
     *
     * @param fr the fr
     * @return the width
     */
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

    /**
     * Sets width.
     *
     * @param minWidth the min width
     */
    public void setWidth(int minWidth)
    {
        this.width = minWidth;
    }

    /**
     * Randomize vector.
     */
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

    /**
     * Adjust vector.
     *
     * @param screenWidth  the screen width
     * @param screenHeight the screen height
     */
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

    /**
     * Avoid.
     *
     * @param devs the devs
     */
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

    /**
     * The type Fake.
     */
    public static class Fake extends SplashPerson
    {
        private TextureImpl texture;

        /**
         * Instantiates a new Fake.
         *
         * @param name    the name
         * @param title   the title
         * @param texture the texture
         */
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
