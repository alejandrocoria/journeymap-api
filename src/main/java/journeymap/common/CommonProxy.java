/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common;

import journeymap.common.network.WorldIDPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

/**
 * Proxy to provide a common interface for initializing client-side or server-side.
 */
public interface CommonProxy
{
    /**
     * Initialize the side.
     * @param event
     * @throws Throwable
     */
    public void initialize(FMLInitializationEvent event) throws Throwable;

    /**
     * Post-initialize the side.
     * @param event
     * @throws Throwable
     */
    public void postInitialize(FMLPostInitializationEvent event) throws Throwable;

    /**
     * Whether this side will accept being connected to the other side.
     * Since we don't care if the other side has JourneyMap or some other mod, always return true.
     */
    public boolean checkModLists(Map<String, String> modList, Side side);

    /**
     * Whether the update check is enabled.
     *
     * @return
     */
    public boolean isUpdateCheckEnabled();

    /**
     * Handles the response when a world ID packet is received.
     * @param message
     * @param playerEntity
     */
    public void handleWorldIdMessage(String message, EntityPlayerMP playerEntity);
}
