/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.common;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

/**
 * Forge Mod entry point
 */
@Mod(modid = CommonProxy.MOD_ID, name = CommonProxy.SHORT_MOD_NAME, version = "@JMVERSION@", canBeDeactivated = true)
public class JourneyMapMod
{
    @Mod.Instance(CommonProxy.MOD_ID)
    public static JourneyMapMod instance;

    @SidedProxy(clientSide = "net.techbrew.journeymap.JourneyMap", serverSide = "net.techbrew.journeymap.common.NoOp")
    public static CommonProxy proxy;

    @NetworkCheckHandler
    public boolean checkModLists(Map<String, String> modList, Side side)
    {
        // Don't require anything on either side
        return true;
    }

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) throws Throwable
    {
        proxy.initialize(event);
    }

    @Mod.EventHandler
    public void postInitialize(FMLPostInitializationEvent event) throws Throwable
    {
        proxy.postInitialize(event);
    }
}