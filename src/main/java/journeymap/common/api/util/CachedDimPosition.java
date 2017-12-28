package journeymap.common.api.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.function.Function;

/**
 * Caches frequently-used positions/vectors relative to a single dimension,
 * rather than calculating them on every use.  Useful during render calls.
 */
public class CachedDimPosition
{
    private Integer cachedDim;
    private BlockPos cachedPos;
    private Vec3d cachedVec;
    private Vec3d cachedCenteredVec;
    private final Function<Integer,BlockPos> valueSupplier;

    /**
     * Constructor.
     * @param valueSupplier function to get position value in a dimension.
     */
    public CachedDimPosition(Function<Integer, BlockPos> valueSupplier)
    {
        this.valueSupplier = valueSupplier;
    }

    /**
     * Reset cached values.
     */
    public void reset()
    {
        cachedDim = null;
        cachedPos = null;
        cachedVec = null;
        cachedCenteredVec = null;
    }

    /**
     * Ensure cached values are relative to the requested dimension.
     * @param dimension dimension
     */
    private void ensure(int dimension)
    {
        if (this.cachedDim == null || this.cachedDim != dimension)
        {
            this.cachedDim = dimension;
            this.cachedPos = valueSupplier.apply(dimension);
            this.cachedVec = new Vec3d(this.cachedPos.getX(), this.cachedPos.getY(), this.cachedPos.getZ());
            this.cachedCenteredVec = this.cachedVec.addVector(.5, .5, .5);
        }
    }

    /**
     * Gets position relative to dimension.
     * @param dimension targetDimension
     * @return position
     */
    public BlockPos getPosition(int dimension)
    {
        ensure(dimension);
        return cachedPos;
    }

    /**
     * Gets Vec3D position relative to dimension.
     *
     * @param dimension targetDimension
     * @return position
     */
    public Vec3d getVec(int dimension)
    {
        ensure(dimension);
        return cachedVec;
    }

    /**
     * Gets block-centered position as a Vec3D
     * @param dimension targetDimension
     * @return the position
     */
    public Vec3d getCenteredVec(int dimension)
    {
        ensure(dimension);
        return cachedCenteredVec;
    }
}
