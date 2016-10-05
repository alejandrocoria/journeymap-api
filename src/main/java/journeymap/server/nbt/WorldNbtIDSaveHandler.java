/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.server.nbt;

import journeymap.server.Constants;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.UUID;

/**
 * Created by Mysticdrew on 10/27/2014.
 */
public class WorldNbtIDSaveHandler
{
    private static final String LEGACY_DAT_FILE = "JourneyMapWorldID";
    private static final String LEGACY_WORLD_ID_KEY = "JourneyMapWorldID";
    private static final String DAT_FILE = "WorldUUID";
    private static final String WORLD_ID_KEY = "world_uuid";
    private NBTWorldSaveDataHandler data;
    private NBTWorldSaveDataHandler legacyData;
    private World world;

    public WorldNbtIDSaveHandler()
    {
        world = Constants.SERVER.getEntityWorld();
        legacyData = (NBTWorldSaveDataHandler) world.getPerWorldStorage().getOrLoadData(NBTWorldSaveDataHandler.class, LEGACY_DAT_FILE);
        data = (NBTWorldSaveDataHandler) world.getPerWorldStorage().getOrLoadData(NBTWorldSaveDataHandler.class, DAT_FILE);
    }

    public String getWorldID()
    {
        return getNBTWorldID();
    }


    public void setWorldID(String worldID)
    {
        saveWorldID(worldID);
    }

    private String getNBTWorldID()
    {

        // TODO: Remove this migration when we update to MC 1.9+
        // Migrate old worldID to new system.
        if (legacyData != null && legacyData.getData().hasKey(LEGACY_WORLD_ID_KEY))
        {
            String worldId = legacyData.getData().getString(LEGACY_WORLD_ID_KEY);

            legacyData.getData().removeTag(LEGACY_WORLD_ID_KEY);
            legacyData.markDirty();

            data = new NBTWorldSaveDataHandler(DAT_FILE);
            world.getPerWorldStorage().setData(WORLD_ID_KEY, data);
            saveWorldID(worldId);

            return worldId;
        }

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
