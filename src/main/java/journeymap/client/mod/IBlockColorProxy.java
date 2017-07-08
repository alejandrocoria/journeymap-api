package journeymap.client.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Provides positional Blockstate color.
 */
public interface IBlockColorProxy {
    /**
     * Gets color for block based on its textures.
     *
     * @param blockMD the block md
     * @return the texture color
     */
    @Nullable
    int deriveBlockColor(BlockMD blockMD);

    /**
     * Get color for block based on world position.
     *
     * @param chunkMD  the chunk md
     * @param blockMD  the block md
     * @param blockPos the block pos
     * @return the color at the given position
     */
    int getBlockColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos);
}
