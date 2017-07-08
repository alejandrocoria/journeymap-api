/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.mod;

import journeymap.client.mod.impl.Bibliocraft;
import journeymap.client.mod.impl.BiomesOPlenty;
import journeymap.client.mod.impl.TerraFirmaCraft;
import journeymap.client.mod.vanilla.VanillaBlockColorProxy;
import journeymap.client.mod.vanilla.VanillaBlockHandler;
import journeymap.client.mod.vanilla.VanillaBlockSpriteProxy;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Delegates the handling of some mods' blocks to a special handler.  The handling may or may not be related
 * to how the blocks are colored on the map.  For example, a certain block might trigger creation of a waypoint.
 */
public enum ModBlockDelegate {
    INSTANCE;

    private final Logger logger = Journeymap.getLogger();
    private final HashMap<String, Class<? extends IModBlockHandler>> handlerClasses = new HashMap<>();
    private final HashMap<String, IModBlockHandler> handlers = new HashMap<>(10);
    private VanillaBlockHandler commonBlockHandler;
    private IBlockColorProxy defaultBlockColorProxy;
    private IBlockSpritesProxy defaultBlockSpritesProxy;

    ModBlockDelegate() {
        reset();
    }

    /**
     * Register special block handlers when this class is initialized.
     */
    public void reset() {
        commonBlockHandler = new VanillaBlockHandler();
        defaultBlockColorProxy = new VanillaBlockColorProxy();
        defaultBlockSpritesProxy = new VanillaBlockSpriteProxy();

        //TODO:  Move modid to an annotation used to autoload these classes.

        handlerClasses.clear();
        handlerClasses.put("BiblioCraft", Bibliocraft.class);
        handlerClasses.put("BiomesOPlenty", BiomesOPlenty.class);
        handlerClasses.put("terrafirmacraft", TerraFirmaCraft.class);
        handlerClasses.put("tfc2", TerraFirmaCraft.class);

        for (Map.Entry<String, Class<? extends IModBlockHandler>> entry : handlerClasses.entrySet()) {
            String modId = entry.getKey();
            Class<? extends IModBlockHandler> handlerClass = entry.getValue();
            if (Loader.isModLoaded(modId) || Loader.isModLoaded(modId.toLowerCase())) {
                modId = modId.toLowerCase();
                try {
                    handlers.put(modId, handlerClass.newInstance());
                    logger.info("Custom modded block handling enabled for " + modId);
                } catch (Exception e) {
                    logger.error(String.format("Couldn't initialize modded block handler for %s: %s",
                            modId, LogFormatter.toPartialString(e)));
                }
            }
        }
    }

    /**
     * Call handlers to initialize their blocks' flags with the cache.
     *
     * @param blockMD the block md
     */
    public void initialize(BlockMD blockMD) {
        if (commonBlockHandler == null) {
            reset();
        }

        // Set default proxies
        blockMD.setBlockSpritesProxy(defaultBlockSpritesProxy);
        blockMD.setBlockColorProxy(defaultBlockColorProxy);

        // Initialize with common block handler first
        initialize(commonBlockHandler, blockMD);

        // Initialize with mod-specific block handler if available
        IModBlockHandler modBlockHandler = handlers.get(blockMD.getBlockDomain().toLowerCase());
        if (modBlockHandler != null) {
            modBlockHandler.initialize(blockMD);
        }

        // Clean up
        commonBlockHandler.postInitialize(blockMD);
    }

    /**
     * Initialize a IModBlockHandler and register blocks to be handled by it.
     */
    private void initialize(IModBlockHandler handler, BlockMD blockMD) {
        try {
            handler.initialize(blockMD);
        } catch (Throwable t) {
            logger.error(String.format("Couldn't initialize IModBlockHandler '%s' for %s: %s",
                    handler.getClass(),
                    blockMD,
                    LogFormatter.toPartialString(t)));
        }
    }

    public IModBlockHandler getCommonBlockHandler() {
        return commonBlockHandler;
    }

    public IBlockSpritesProxy getDefaultBlockSpritesProxy() {
        return defaultBlockSpritesProxy;
    }

    public IBlockColorProxy getDefaultBlockColorProxy() {
        return defaultBlockColorProxy;
    }
}
