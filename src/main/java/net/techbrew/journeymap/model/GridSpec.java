package net.techbrew.journeymap.model;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.render.texture.TextureCache;

/**
 * Provides encapsulation of how to render the grid overlay in a Tile.
 */
public class GridSpec
{
    public final Style style;
    public final transient int glTextureId;
    public final float red;
    public final float green;
    public final float blue;
    public final float alpha;

    public GridSpec(Style style, float red, float green, float blue, float alpha)
    {
        this.style = style;
        this.glTextureId = TextureCache.instance().getGrid(style.textureName).getGlTextureId();
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
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
