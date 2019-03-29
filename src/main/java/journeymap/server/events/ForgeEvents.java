/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.events;

import journeymap.common.network.Configuration;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Mysticdrew on 5/5/2018.
 */
public class ForgeEvents
{
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP)
        {

        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {

        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            new Configuration().sendToPlayer(null, (EntityPlayerMP) event.player);
        }
    }

}
