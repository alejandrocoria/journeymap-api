/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.mod.vanilla;

import journeymap.client.cartography.color.RGB;
import journeymap.client.mod.IBlockColorProxy;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.world.JmBlockAccess;
import net.minecraft.block.BlockBed;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Example of how BlockMD handling can be customized.
 */
public enum BedBlockProxy implements IBlockColorProxy
{
    INSTANCE;

    @Override
    public int deriveBlockColor(BlockMD blockMD, @Nullable ChunkMD chunkMD, @Nullable BlockPos blockPos)
    {
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().deriveBlockColor(blockMD, chunkMD, blockPos);
    }

    @Override
    public int getBlockColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        if (blockMD.getBlock() instanceof BlockBed)
        {
            TileEntity tileentity = JmBlockAccess.INSTANCE.getTileEntity(blockPos);
            if (tileentity instanceof TileEntityBed)
            {
                int bedColor = ((TileEntityBed) tileentity).getColor().getColorValue();
                if (blockMD.getBlockState().getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)
                {
                    return RGB.multiply(0xcccccc, bedColor);
                }
                else
                {
                    return RGB.multiply(0xffffff, bedColor);
                }
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().getBlockColor(chunkMD, blockMD, blockPos);
    }
}
