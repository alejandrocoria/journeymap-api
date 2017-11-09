/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;

import journeymap.client.data.DataCache;
import journeymap.client.feature.FeatureManager;
import journeymap.common.Journeymap;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Stop mapping and reset features when world unloads.
 */
@SideOnly(Side.CLIENT)
public class WorldEventHandler implements EventHandlerManager.EventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onUnload(WorldEvent.Unload event)
    {
        try
        {
            World world = event.getWorld();
            if (DataCache.getPlayer().dimension == world.provider.getDimension())
            {
                Journeymap.getClient().stopMapping();
                FeatureManager.INSTANCE.reset();
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Error handling WorldEvent.Unload", e);
        }
    }
}
