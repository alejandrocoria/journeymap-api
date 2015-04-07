package net.techbrew.journeymap.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

/**
 * Forge Mod entry point
 */
@Mod(modid = CommonProxy.MOD_ID, name = CommonProxy.SHORT_MOD_NAME, version = "@JMVERSION@", canBeDeactivated = true)
public class JourneyMapMod
{
    @Mod.Instance(CommonProxy.MOD_ID)
    public static JourneyMapMod instance;

    @SidedProxy(clientSide = "net.techbrew.journeymap.JourneyMap", serverSide = "net.techbrew.journeymap.common.CommonProxy.NoOp")
    public static CommonProxy proxy;

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