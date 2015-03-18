package net.techbrew.journeymap.model;

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
    private TextureImpl texture = null;

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
    public void bindTexture(int textureWrap, float mapAlpha)
    {
        if (texture == null || texture.isUnused())
        {
            texture = TextureCache.instance().getGrid(style.textureName);
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());
        GL11.glColor4f(red, green, blue, alpha * mapAlpha);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);
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
