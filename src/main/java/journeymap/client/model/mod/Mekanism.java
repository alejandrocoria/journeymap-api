/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.mod.vanilla.VanillaColorHandler;
import journeymap.common.Journeymap;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.Collection;
import java.util.Collections;

/**
 * Special handling required for Mekanism blocks, etc.
 */
public class Mekanism
{
    private static final String MODID = "mekanism";

    static MekanismColorHandler mekanismColorHandler = new MekanismColorHandler();

    /**
     * Mekanism block handler.
     */
    public static class MekanismBlockHandler implements ModBlockDelegate.IModBlockHandler
    {
        @Override
        public boolean initialize(BlockMD blockMD)
        {
            String uid = blockMD.getUid();
            if (uid.startsWith(MODID + ":"))
            {
                blockMD.setBlockColorHandler(mekanismColorHandler);
            }
            return false;
        }

        @Override
        public BlockMD handleBlock(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
        {
            return blockMD;
        }
    }

    /**
     * Color handler.
     */
    public static class MekanismColorHandler extends VanillaColorHandler
    {
        @Override
        public Collection<TextureAtlasSprite> getSprites(BlockMD blockMD)
        {
            try
            {
                BlockModelShapes bms = FMLClientHandler.instance().getClient().getBlockRendererDispatcher().getBlockModelShapes();
                return Collections.singleton(bms.getTexture(blockMD.getBlockState()));
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error("Couldn't get default texture for " + blockMD, e);
                return super.getSprites(blockMD);
            }
        }
    }
}
