package net.techbrew.mcjm;


import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.forgehandler.*;

@Mod(modid = JourneyMap.SHORT_MOD_NAME, name = JourneyMap.SHORT_MOD_NAME, version = JourneyMap.JM_VERSION)
public class JourneyMapLoader {

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event)
	{
        if(event.getSide()==Side.CLIENT) {
            try {
                JourneyMap instance = JourneyMap.getInstance();
                instance.initialize(Minecraft.getMinecraft());

                if(instance.enableMapGui) {
                    TickRegistry.registerTickHandler(new MiniMapTickHandler(), Side.CLIENT);
                    KeyBindingRegistry.registerKeyBinding(new KeyHandler());
                }

                TickRegistry.registerScheduledTickHandler(new TaskTickHandler(), Side.CLIENT);
                TickRegistry.registerScheduledTickHandler(new StateTickHandler(), Side.CLIENT);

                NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());

            } catch(Throwable t) {
                System.err.println("Error loading " + JourneyMap.MOD_NAME + " for Minecraft " + JourneyMap.MC_VERSION + ". Ensure compatible Minecraft/Modloader/Forge versions.");
                t.printStackTrace(System.err);
            }
        }
	}
}


