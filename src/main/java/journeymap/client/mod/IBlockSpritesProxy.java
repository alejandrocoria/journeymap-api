package journeymap.client.mod;

import journeymap.client.cartography.color.ColoredSprite;
import journeymap.client.model.BlockMD;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Interface for a class that gets sprites from a Blockstate used to derive colors
 */
public interface IBlockSpritesProxy
{
    @Nullable
    Collection<ColoredSprite> getSprites(BlockMD blockMD);
}
