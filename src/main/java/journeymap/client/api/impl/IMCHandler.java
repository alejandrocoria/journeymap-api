/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.api.impl;

import journeymap.common.Journeymap;
import net.minecraftforge.fml.common.event.FMLInterModComms;

/**
 * Handles InterModComm events.  Currently unused.
 */
public class IMCHandler
{
    /**
     * Handle an InterModComms event.
     *
     * @param event
     */
    public static void handle(FMLInterModComms.IMCEvent event)
    {
        try
        {
            for (FMLInterModComms.IMCMessage message : event.getMessages())
            {
                final String key = message.key.toLowerCase();
                switch (key)
                {
                    // TODO
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Error processing IMCEvent: " + t, t);
        }
    }

//    private static void todo(FMLInterModComms.IMCMessage message)
//    {
//        boolean result = false;
//        if (message.isResourceLocationMessage())
//        {
//        }
//
//        FMLInterModComms.sendMessage(message.getSender(), MESSAGE_KEY, Boolean.toString(result));
//        return;
//    }
}
