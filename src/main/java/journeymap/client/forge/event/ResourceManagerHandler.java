/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

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
            Journeymap.getLogger().warn("Could not register ResourceManagerHandler.  Changing resource packs will require restart.");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
//        try
//        {
//            ColorManager.instance().ensureCurrent();
//        }
//        catch (Exception e)
//        {
//            Journeymap.getLogger().warn("Error calling ColorManager.ensureCurrent(): " + LogFormatter.toString(e));
//        }
    }
}
