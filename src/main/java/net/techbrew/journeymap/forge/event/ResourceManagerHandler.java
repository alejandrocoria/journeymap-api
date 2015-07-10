/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ColorCache;
import net.techbrew.journeymap.forge.helper.ForgeHelper;

/**
 * Handle reloads/changes to resource packs.
 */
@SideOnly(Side.CLIENT)
public class ResourceManagerHandler implements IResourceManagerReloadListener
{
    private Minecraft mc = ForgeHelper.INSTANCE.getClient();

    public ResourceManagerHandler()
    {
        IResourceManager rm = ForgeHelper.INSTANCE.getClient().getResourceManager();
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
