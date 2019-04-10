/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.nbt;

import journeymap.common.Journeymap;
import journeymap.server.Constants;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Created by Mysticdrew on 10/27/2014.
 */
public class WorldNbtIDSaveHandler
{
    private static final String DAT_FILE = "WorldUUID";
    private static final String WORLD_ID_KEY = "world_uuid";
    private NBTWorldSaveDataHandler data;

    private World world;

    public WorldNbtIDSaveHandler()
    {
        try
        {
            world = Constants.SERVER.getEntityWorld();
            data = (NBTWorldSaveDataHandler) world.getPerWorldStorage().getOrLoadData(NBTWorldSaveDataHandler.class, DAT_FILE);
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Error in worldID handler", e);
        }
    }

    public String getWorldID()
    {
        return getNBTWorldID();
    }

    private String getNBTWorldID()
    {
        if (data == null)
        {
            return createNewWorldID();
        }

        if (data.getData().hasKey(WORLD_ID_KEY))
        {
            return data.getData().getString(WORLD_ID_KEY);
        }
        return "noWorldIDFound";
    }

    private String createNewWorldID()
    {
        String worldID = UUID.randomUUID().toString();
        data = new NBTWorldSaveDataHandler(DAT_FILE);
        world.getPerWorldStorage().setData(WORLD_ID_KEY, data);
        saveWorldID(worldID);
        return worldID;
    }

    private void saveWorldID(String worldID)
    {
        if (data != null)
        {
            data.getData().setString(WORLD_ID_KEY, worldID);
            data.markDirty();
        }
    }
}
