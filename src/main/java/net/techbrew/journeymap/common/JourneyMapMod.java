package net.techbrew.journeymap.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

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