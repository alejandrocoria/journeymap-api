package journeymap.client.cartography.color;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.annotations.Since;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;

/**
 * Color data for a BlockState.
 */
class BlockStateColor implements Comparable<BlockStateColor> {
    /**
     * The Block id.
     */
    @Since(5.45)
    String block;

    /**
     * The Blockstate id.
     */
    @Since(5.45)
    String state;

    /**
     * The Name.
     */
    @Since(5.2)
    String name;

    /**
     * The Color.
     */
    @Since(5.2)
    String color;

    /**
     * The Alpha.
     */
    @Since(5.2)
    Float alpha;

    /**
     * Instantiates a new Block color.
     */
    BlockStateColor(BlockMD blockMD) {
        this(blockMD, blockMD.getTextureColor());
    }

    /**
     * Instantiates a new BlockStateColor.
     *
     * @param blockMD the block md
     * @param color   the color
     */
    BlockStateColor(BlockMD blockMD, Integer color) {
        if (Journeymap.getClient().getCoreProperties().verboseColorPalette.get()) {
            this.block = blockMD.getBlockId();
            this.state = blockMD.getBlockStateId();
            this.name = blockMD.getName();
        }
        this.color = RGB.toHexString(color);
        if (blockMD.getAlpha() != 1f) {
            this.alpha = blockMD.getAlpha();
        }
    }

    /**
     * Instantiates a new BlockStateColor.
     *
     * @param color the color
     */
    BlockStateColor(String color, Float alpha) {
        this.color = color;
        this.alpha = (alpha == null) ? 1F : alpha;
    }

    @Override
    public int compareTo(BlockStateColor that) {
        Ordering ordering = Ordering.natural().nullsLast();
        return ComparisonChain.start()
                .compare(this.name, that.name, ordering)
                .compare(this.block, that.block, ordering)
                .compare(this.state, that.state, ordering)
                .compare(this.color, that.color, ordering)
                .compare(this.alpha, that.alpha, ordering)
                .result();
    }
}
