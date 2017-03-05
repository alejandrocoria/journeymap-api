/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

/**
 * Proxy to provide a common interface for initializing client-side or server-side.
 */
public interface CommonProxy
{
    /**
     * Pre-initialize the side.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    public void preInitialize(FMLPreInitializationEvent event) throws Throwable;

    /**
     * Initialize the side.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    public void initialize(FMLInitializationEvent event) throws Throwable;

    /**
     * Post-initialize the side.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    public void postInitialize(FMLPostInitializationEvent event) throws Throwable;

    /**
     * Whether this side will accept being connected to the other side.
     * Since we don't care if the other side has JourneyMap or some other mod, always return true.
     *
     * @param modList the mod list
     * @param side    the side
     * @return the boolean
     */
    public boolean checkModLists(Map<String, String> modList, Side side);

    /**
     * Whether the update check is enabled.
     *
     * @return boolean
     */
    public boolean isUpdateCheckEnabled();

    /**
     * Handles the response when a world ID packet is received.
     *
     * @param message      the message
     * @param playerEntity the player entity
     */
    public void handleWorldIdMessage(String message, EntityPlayerMP playerEntity);
}
