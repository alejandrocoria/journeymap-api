package net.techbrew.journeymap.forgehandler;


import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventBus;
import net.minecraftforge.common.MinecraftForge;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * Created by mwoodman on 1/29/14.
 */
public class EventHandlerManager {

    public enum BusType {
        FMLCommonHandlerBus(FMLCommonHandler.instance().bus()),
        MinecraftForgeBus(MinecraftForge.EVENT_BUS),
        NetworkRegistry(null);

        protected final EventBus eventBus;
        private BusType(EventBus eventBus)
        {
            this.eventBus = eventBus;
        }
    }

    private static HashMap<Class<? extends EventHandler>, EventHandler> handlers = new HashMap<Class<? extends EventHandler>, EventHandler>();

    public static interface EventHandler
    {
        EnumSet<BusType> getBus();
    }

    public static interface SelfRegister
    {
        void register();
        void unregister();
    }

    public static void registerGeneralHandlers()
    {
        register(new NetworkHandler());
        register(new StateTickHandler());
        register(new WorldEventHandler());
        register(new ChunkUpdateHandler());
        register(new WaypointBeaconHandler());
    }

    public static void registerGuiHandlers()
    {
        register(new MiniMapOverlayHandler());        
        register(new KeyEventHandler());
    }

    public static void unregisterAll()
    {
        ArrayList<Class<? extends EventHandler>> list = new ArrayList<Class<? extends EventHandler>>(handlers.keySet());
        for(Class<? extends EventHandler> handlerClass : list)
        {
            unregister(handlerClass);
        }
    }

    private static void register(EventHandler handler)
    {
        if(handlers.containsKey(handler.getClass()))
        {
            JourneyMap.getLogger().warning("Handler already registered: " + handler.getClass().getName());
            return;
        }

        boolean registered = false;
        EnumSet<BusType> buses = handler.getBus();
        for(BusType busType : handler.getBus())
        {
            String name = handler.getClass().getName();
            try
            {
                if(handler instanceof SelfRegister)
                {
                    ((SelfRegister) handler).register();
                    registered = true;
                    JourneyMap.getLogger().fine(name + " registered in " + busType);
                }
                else if(busType.eventBus!=null)
                {
                    busType.eventBus.register(handler);
                    registered = true;
                    JourneyMap.getLogger().fine(name + " registered in " + busType);
                }
            }
            catch(Throwable t)
            {
                JourneyMap.getLogger().severe(name + " registration FAILED in " + busType + ": " + LogFormatter.toString(t));
            }
        }

        if(registered)
        {
            handlers.put(handler.getClass(), handler);
        }
        else
        {
            JourneyMap.getLogger().warning("Handler was not registered at all: " + handler.getClass().getName());
        }
    }

    public static void unregister(Class<? extends EventHandler> handlerClass)
    {
        EventHandler handler = handlers.remove(handlerClass);
        if(handler!=null)
        {
            String name = handler.getClass().getName();

            if(handler instanceof SelfRegister)
            {
                ((SelfRegister) handler).unregister();
                JourneyMap.getLogger().fine(name + " unregistered.");
                return;
            }

            EnumSet<BusType> buses = handler.getBus();
            for(BusType busType : handler.getBus())
            {
                try
                {
                    boolean unregistered = false;
                    switch(busType) {
                        case MinecraftForgeBus:
                            MinecraftForge.EVENT_BUS.unregister(handler);
                            unregistered = true;
                            break;
                    }
                    if(unregistered) {
                        JourneyMap.getLogger().fine(name + " unregistered from " + busType);
                    }
                }
                catch(Throwable t)
                {
                    JourneyMap.getLogger().severe(name + " unregistration FAILED from " + busType + ": " + LogFormatter.toString(t));
                }
            }
        }
    }
}
