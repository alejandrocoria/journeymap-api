/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.mod.impl;

import journeymap.client.mod.IModBlockHandler;
import journeymap.client.model.BlockMD;

import static journeymap.client.model.BlockFlag.*;

/**
 * Special handling required for TFC to set biome-related flags
 */
public class TerraFirmaCraft implements IModBlockHandler
{
    private static final int WATER_COLOR = 0x0b1940;

    public TerraFirmaCraft()
    {
    }

    @Override
    public void initialize(BlockMD blockMD)
    {
        String name = blockMD.getBlockId().toLowerCase();
        if (name.contains("looserock") || name.contains("loose_rock") || name.contains("rubble") || name.contains("vegetation"))
        {
            blockMD.addFlags(Ignore, NoShadow, NoTopo);
        }
        else if (name.contains("seagrass"))
        {
            blockMD.addFlags(Plant);
        }
        else if (name.contains("grass"))
        {
            blockMD.addFlags(Grass);
        }
        else if (name.contains("water"))
        {
            blockMD.setAlpha(.3f);
            blockMD.addFlags(Water, NoShadow);
            blockMD.setColor(WATER_COLOR);
        }
        else if (name.contains("leaves"))
        {
            blockMD.addFlags(NoTopo, Foliage);
        }
    }
}
