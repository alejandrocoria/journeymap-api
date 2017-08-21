package journeymap.client.model;

/**
 * Flags indicating special behaviors or handling of Blocks/Blockstates.
 */
public enum BlockFlag {
    /**
     * IBlockState shouldn't be mapped.
     */
    Ignore,

    /**
     * Color is determined by biome foliage multiplier
     */
    Foliage,

    /**
     * Color is determined by biome grass multiplier
     */
    Grass,

    /**
     * Color is determined by biome water multiplier
     */
    Water,

    /**
     * Color is fluid based
     */
    Fluid,

    /**
     * Block doesn't count as overhead cover.
     */
    OpenToSky,

    /**
     * Block shouldn't cast a shadow.
     */
    NoShadow,

    /**
     * Block isn't opaque.
     */
    Transparency,

    /**
     * Block was processed with errors.
     */
    Error,

    /**
     * Block is a non-crop plant.
     */
    Plant,

    /**
     * Block is a crop.
     */
    Crop,

    /**
     * Block should be ignored in topological maps.
     */
    NoTopo;
}
