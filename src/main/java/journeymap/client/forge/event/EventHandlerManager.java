/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;

import journeymap.client.cartography.ColorManager;
import journeymap.client.command.ClientCommandInvoker;
import journeymap.client.command.CmdChatPosition;
import journeymap.client.command.CmdEditWaypoint;
import journeymap.client.world.ChunkMonitor;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author techbrew 1/29/14.
 */
public class EventHandlerManager
{
    private static HashMap<Class<? extends EventHandler>, EventHandler> handlers = new HashMap<>();

    /**
     * Marker interface for now
     */
    public static interface EventHandler
    {
    }

    public static void registerGeneralHandlers()
    {
        register(new ChatEventHandler());
        register(new StateTickHandler());
        register(new WorldEventHandler());
        register(new WaypointBeaconHandler());
        register(new TextureAtlasHandler());

        // TODO: Why does this need to be forced?
        ColorManager.instance();

        ClientCommandInvoker clientCommandInvoker = new ClientCommandInvoker();
        clientCommandInvoker.register(new CmdChatPosition());
        clientCommandInvoker.register(new CmdEditWaypoint());
        ClientCommandHandler.instance.registerCommand(clientCommandInvoker);

        register(ChunkMonitor.INSTANCE);
    }

    public static void registerGuiHandlers()
    {
        register(new MiniMapOverlayHandler());
        KeyEventHandler.initKeyBindings();
        register(new KeyEventHandler());
    }

    public static void unregisterAll()
    {
        ArrayList<Class<? extends EventHandler>> list = new ArrayList<Class<? extends EventHandler>>(handlers.keySet());
        for (Class<? extends EventHandler> handlerClass : list)
        {
            unregister(handlerClass);
        }
    }

    private static void register(EventHandler handler)
    {
        Class<? extends EventHandler> handlerClass = handler.getClass();
        if (handlers.containsKey(handlerClass))
        {
            Journeymap.getLogger().warn("Handler already registered: " + handlerClass.getName());
            return;
        }

        try
        {
            MinecraftForge.EVENT_BUS.register(handler);
            Journeymap.getLogger().debug("Handler registered: " + handlerClass.getName());
            handlers.put(handler.getClass(), handler);
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(handlerClass.getName() + " registration FAILED: " + LogFormatter.toString(t));
        }
    }

    public static void unregister(Class<? extends EventHandler> handlerClass)
    {
        EventHandler handler = handlers.remove(handlerClass);
        if (handler != null)
        {
            try
            {
                MinecraftForge.EVENT_BUS.unregister(handler);
                Journeymap.getLogger().debug("Handler unregistered: " + handlerClass.getName());
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error(handler + " unregistration FAILED: " + LogFormatter.toString(t));
            }
        }
    }
}
