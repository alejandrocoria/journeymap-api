/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import journeymap.client.Constants;
import journeymap.client.cartography.RGB;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Provides encapsulation of how to render the grid overlay in a Tile.
 */
public class GridSpec
{
    public final Style style;
    public final float red;
    public final float green;
    public final float blue;
    public final float alpha;
    private int colorX = -1;
    private int colorY = -1;
    //private transient TextureImpl texture = null;

    public GridSpec(Style style, Color color, float alpha)
    {
        this.style = style;
        float[] rgb = RGB.floats(color.getRGB());
        this.red = rgb[0];
        this.green = rgb[1];
        this.blue = rgb[2];
        if (alpha < 0)
        {
            alpha = 0f;
        }
        while (alpha > 1)
        {
            alpha = alpha / 100f;
        }
        this.alpha = alpha;
        assert (alpha <= 1);
    }

    public GridSpec(Style style, float red, float green, float blue, float alpha)
    {
        this.style = style;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        assert (alpha <= 1);
    }

    public GridSpec setColorCoords(int x, int y)
    {
        this.colorX = x;
        this.colorY = y;
        return this;
    }

    /**
     * MUST CALL GlStateManager.color(1, 1, 1, alpha); when done
     */
    public void beginTexture(int textureWrap, float mapAlpha)
    {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.enableTexture2D();

        GlStateManager.bindTexture(getTexture().getGlTextureId());
        GlStateManager.color(red, green, blue, alpha * mapAlpha);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);
    }

    public TextureImpl getTexture()
    {
        return TextureCache.instance().getGrid(style.textureName);
    }

    public GridSpec clone()
    {
        return new GridSpec(style, red, green, blue, alpha).setColorCoords(colorX, colorY);
    }

    public void finishTexture()
    {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.clearColor(1, 1, 1, 1f); // defensive against shaders
    }

    public Integer getColor()
    {
        return RGB.toInteger(red, green, blue);
    }

    public int getColorX()
    {
        return colorX;
    }

    public int getColorY()
    {
        return colorY;
    }

    public enum Style
    {
        Squares("jm.common.grid_style_squares", TextureCache.Name.GridSquares),
        Dots("jm.common.grid_style_dots", TextureCache.Name.GridDots),
        Checkers("jm.common.grid_style_checkers", TextureCache.Name.GridCheckers);

        private final String key;
        private final TextureCache.Name textureName;

        private Style(String key, TextureCache.Name textureName)
        {
            this.key = key;
            this.textureName = textureName;
        }

        public String displayName()
        {
            return Constants.getString(key);
        }
    }
}
