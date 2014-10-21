/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.EnumSet;

/**
 * Waypoint for player death
 */
@SideOnly(Side.CLIENT)
public class PlayerDeathEvent implements EventHandlerManager.EventHandler
{
    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        onDeath(event.entityLiving);
    }

    public static void onDeath(final EntityLivingBase entity)
    {
        if(entity==null)
        {
            return;
        }

        try
        {
            if (!(entity instanceof EntityPlayer))
            {
                return;
            }

            EntityPlayer player = (EntityPlayer) entity;
            JourneyMap.getLogger().info(String.format("%s died at %s,%s,%s",player.getCommandSenderName(),
                    MathHelper.floor_double(player.posX),
                    MathHelper.floor_double(player.posY),
                    MathHelper.floor_double(player.posZ)));

            if (player.getCommandSenderName().equals(JourneyMap.getInstance().getPlayerName()))
            {
                if (JourneyMap.getWaypointProperties().managerEnabled.get() && JourneyMap.getWaypointProperties().createDeathpoints.get())
                {

                    WaypointStore.instance().save(Waypoint.deathOf(player));
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Unexpected Error in onLivingDeath(): " + LogFormatter.toString(t));
        }
    }
}
