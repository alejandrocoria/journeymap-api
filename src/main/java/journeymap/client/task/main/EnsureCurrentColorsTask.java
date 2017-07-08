/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.color.ColorManager;
import journeymap.client.data.DataCache;
import journeymap.client.log.ChatLog;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.task.multi.MapPlayerTask;
import net.minecraft.client.Minecraft;

/**
 * Ensures color palette is current.
 */
public class EnsureCurrentColorsTask implements IMainThreadTask
{
    final boolean forceReset;
    final boolean announce;

    public EnsureCurrentColorsTask()
    {
        this(false, false);
    }

    public EnsureCurrentColorsTask(boolean forceReset, boolean announce)
    {
        this.forceReset = forceReset;
        this.announce = announce;
        if (announce)
        {
            ChatLog.announceI18N("jm.common.colorreset_start");
        }
    }

    @Override
    public IMainThreadTask perform(Minecraft mc, JourneymapClient jm)
    {
        if (forceReset)
        {
            DataCache.INSTANCE.resetBlockMetadata();
            ModBlockDelegate.INSTANCE.reset();
            ColorManager.INSTANCE.reset();
        }
        ColorManager.INSTANCE.ensureCurrent(forceReset);
        if (announce)
        {
            ChatLog.announceI18N("jm.common.colorreset_complete");
        }

        // Remap around player
        if (forceReset)
        {
            MapPlayerTask.forceNearbyRemap();
        }

        return null;
    }

    @Override
    public String getName()
    {
        return "EnsureCurrentColorsTask";
    }
}