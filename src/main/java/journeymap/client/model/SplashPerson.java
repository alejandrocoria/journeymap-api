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
    public double moveX;
    /**
     * The Move y.
     */
    public double moveY;
    private double moveDistance = 1;
    private Random r = new Random();

    /**
     * Instantiates a new AboutDialog person.
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
        return TextureCache.getPlayerSkin(null, ign);
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
        this.moveDistance = r.nextDouble() + .5;
        this.moveX = r.nextBoolean() ? moveDistance : -moveDistance;
        this.moveDistance = r.nextDouble() + .5;
        this.moveY = r.nextBoolean() ? moveDistance : -moveDistance;
    }

    /**
     * Adjust vector.
     *
     * @param screenBounds
     */
    public void adjustVector(Rectangle2D.Double screenBounds)
    {
        Rectangle2D.Double buttonBounds = button.getBounds();
        if(!screenBounds.contains(buttonBounds))
        {
            int xMargin = button.getWidth();
            int yMargin = button.getHeight();
            if (buttonBounds.getMinX() <= xMargin)
            {
                this.moveX = moveDistance;
            }
            else if (buttonBounds.getMaxX() >= screenBounds.getWidth() - xMargin)
            {
                this.moveX = -moveDistance;
            }

            if (buttonBounds.getMinY() <= yMargin)
            {
                this.moveY = moveDistance;
            }
            else if (buttonBounds.getMaxY() >= screenBounds.getHeight() - yMargin)
            {
                this.moveY = -moveDistance;
            }
        }

        continueVector();
    }

    public void continueVector()
    {
        button.setX((int) Math.round(button.x + moveX));
        button.setY((int) Math.round(button.y + moveY));
    }

    /**
     * Avoid.
     *
     * @param others the devs
     */
    public void avoid(List<SplashPerson> others)
    {
        for (SplashPerson other : others)
        {
            if (this == other)
            {
                continue;
            }

            if (this.getDistance(other)<=button.getWidth())
            {
                randomizeVector();
                break;
            }
        }
    }

    /**
     * Returns the squared distance to the other.
     */
    public double getDistance(SplashPerson other)
    {
        double px = this.button.getCenterX() - other.button.getCenterX();
        double py = this.button.getMiddleY() - other.button.getMiddleY();
        return Math.sqrt(px * px + py * py);
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
