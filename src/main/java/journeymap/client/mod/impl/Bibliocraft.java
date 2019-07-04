/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.mod.impl;

import com.google.common.base.Strings;
import journeymap.client.cartography.color.ColoredSprite;
import journeymap.client.mod.IBlockSpritesProxy;
import journeymap.client.mod.IModBlockHandler;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.mod.ModPropertyEnum;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Special handling required for Bibliocraft blocks.
 */
public class Bibliocraft implements IModBlockHandler, IBlockSpritesProxy {
    List<ModPropertyEnum<String>> colorProperties = new ArrayList<>(2);

    public Bibliocraft() {
        colorProperties.add(new ModPropertyEnum<>("jds.bibliocraft.blocks.BiblioColorBlock", "COLOR", "getWoolTextureString", String.class));
        colorProperties.add(new ModPropertyEnum<>("jds.bibliocraft.blocks.BiblioWoodBlock", "WOOD_TYPE", "getTextureString", String.class));
    }

    @Override
    public void initialize(BlockMD blockMD) {
        blockMD.setBlockSpritesProxy(this);
    }

    @Nullable
    @Override
    public Collection<ColoredSprite> getSprites(BlockMD blockMD, @Nullable ChunkMD chunkMD,  @Nullable BlockPos blockPos) {
        IBlockState blockState = blockMD.getBlockState();
        String textureString = ModPropertyEnum.getFirstValue(colorProperties, blockState);
        if (!Strings.isNullOrEmpty(textureString)) {
            try {
                ResourceLocation loc = new ResourceLocation(textureString);
                TextureAtlasSprite tas = FMLClientHandler.instance().getClient().getTextureMapBlocks().getAtlasSprite(loc.toString());
                return Collections.singletonList(new ColoredSprite(tas, null));
            } catch (Exception e) {
                Journeymap.getLogger().error(String.format("Error getting sprite from %s: %s", textureString, LogFormatter.toPartialString(e)));
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockSpritesProxy().getSprites(blockMD, chunkMD, blockPos);
    }
}
