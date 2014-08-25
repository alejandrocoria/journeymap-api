/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ColorCache;

/**
 * Handle reloads/changes to resource packs.
 */
@SideOnly(Side.CLIENT)
public class ResourceManagerHandler implements IResourceManagerReloadListener
{
    private Minecraft mc = FMLClientHandler.instance().getClient();

    public ResourceManagerHandler()
    {
        IResourceManager rm = FMLClientHandler.instance().getClient().getResourceManager();
        if (rm instanceof IReloadableResourceManager)
        {
            ((IReloadableResourceManager) rm).registerReloadListener(this);
        }
        else
        {
            JourneyMap.getLogger().warn("Could not register ResourceManagerHandler.  Changing resource packs will require restart.");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onResourceManagerReload(IResourceManager p_110549_1_)
    {
        if (mc.theWorld == null)
        {
            // Can happen when resource packs are changed after quitting out of world.
            // This ensures the palette is rechecked when mapping starts
            ColorCache.instance().reset();
        }
        else
        {
            ColorCache.instance().ensureCurrent();
        }
    }
}
