package journeymap.client.mod.impl;

import journeymap.client.mod.IModBlockHandler;
import journeymap.client.model.BlockMD;

import static journeymap.client.model.BlockFlag.NoShadow;
import static journeymap.client.model.BlockFlag.Water;

public class Streams implements IModBlockHandler
{
    private static final int WATER_COLOR = 4210943;

    public Streams()
    {
    }

    @Override
    public void initialize(BlockMD blockMD)
    {
        String name = blockMD.getBlockId().toLowerCase();
        if (name.contains("water"))
        {
            blockMD.setAlpha(.25f);
            blockMD.addFlags(Water, NoShadow);
            blockMD.setColor(WATER_COLOR);
        }
    }
}
