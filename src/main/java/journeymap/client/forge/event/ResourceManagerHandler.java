/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;

// 1.7.10
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import journeymap.client.cartography.ColorCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
// 1.8
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;

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
