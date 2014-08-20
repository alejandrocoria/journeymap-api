/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.io;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.McoServer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.realms.RealmsScreen;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;

import java.util.List;

/**
 * Encapsulates whatever fun is required to identify Realms-related stuff.
 */
public class RealmsHelper
{
    /**
     * Returns Realms server name if connected, null otherwise.
     * @return
     */
    public static String getRealmsServerName()
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        if(!mc.isSingleplayer())
        {
            try
            {
                NetHandlerPlayClient netHandler = mc.getNetHandler();
                GuiScreen netHandlerGui = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, netHandler, "field_147307_j", "guiScreenServer");

                if (netHandlerGui instanceof GuiScreenRealmsProxy)
                {
                    RealmsScreen realmsScreen = ((GuiScreenRealmsProxy) netHandlerGui).func_154321_a();
                    if (realmsScreen instanceof RealmsMainScreen)
                    {
                        RealmsMainScreen mainScreen = (RealmsMainScreen) realmsScreen;
                        long selectedServerId = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "selectedServerId");
                        List<McoServer> mcoServers = ReflectionHelper.getPrivateValue(RealmsMainScreen.class, mainScreen, "mcoServers");
                        for (McoServer mcoServer : mcoServers)
                        {
                            if (mcoServer.id == selectedServerId)
                            {
                                return mcoServer.name;
                            }
                        }
                    }
                }
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().error("Unable to get Realms server name: " + LogFormatter.toString(t));
            }
        }

        return null;
    }
}
