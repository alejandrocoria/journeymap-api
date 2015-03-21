package net.techbrew.journeymap.model;

import net.minecraft.client.renderer.OpenGlHelper;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.lwjgl.opengl.GL11;

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
    private transient TextureImpl texture = null;

    public GridSpec(Style style, float red, float green, float blue, float alpha)
    {
        this.style = style;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    /**
     * MUST CALL GL11.glColor4f(1, 1, 1, alpha); when done
     */
    public void beginTexture(int textureWrap, float mapAlpha)
    {
        if (texture == null || texture.isDefunct())
        {
            texture = TextureCache.instance().getGrid(style.textureName);
        }
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());
        GL11.glColor4f(red, green, blue, alpha * mapAlpha);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);
    }

    public void finishTexture()
    {
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glClearColor(1, 1, 1, 1f); // defensive against shaders
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
